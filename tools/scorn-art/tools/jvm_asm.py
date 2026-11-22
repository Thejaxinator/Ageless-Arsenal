#!/usr/bin/env python3
"""
Clean JVM ClassFile Assembler (Java 17 / major 61.0) with Automatic Dataflow
StackMapTable Generation, InterfaceMethodref Support, and RuntimeVisibleAnnotations.
Verified against OpenJDK HotSpot `java -Xverify:all`.
"""
import struct
from typing import Dict, List, Optional, Tuple, Union

# Access flags
ACC_PUBLIC = 0x0001
ACC_PRIVATE = 0x0002
ACC_PROTECTED = 0x0004
ACC_STATIC = 0x0008
ACC_FINAL = 0x0010
ACC_SUPER = 0x0020
ACC_VOLATILE = 0x0040
ACC_VARARGS = 0x0080
ACC_INTERFACE = 0x0200
ACC_ABSTRACT = 0x0400
ACC_SYNTHETIC = 0x1000
ACC_ENUM = 0x4000

# Verification type tags (JVMS 4.7.4)
ITEM_Top = 0
ITEM_Integer = 1
ITEM_Float = 2
ITEM_Double = 3
ITEM_Long = 4
ITEM_Null = 5
ITEM_UninitializedThis = 6
ITEM_Object = 7  # (7, internal_class_name)
ITEM_Uninitialized = 8  # (8, new_offset)

VType = Union[int, Tuple[int, Union[str, int]]]
SECOND_SLOT = "SECOND_SLOT"  # internal marker for 2nd slot of Long/Double


def parse_descriptor_types(desc: str) -> Tuple[List[VType], Optional[VType]]:
    """Parse method descriptor `(args...)ret` into verification types."""
    assert desc.startswith("(")
    idx = 1
    args: List[VType] = []

    def read_one(i: int) -> Tuple[Optional[VType], int]:
        ch = desc[i]
        if ch == "V":
            return None, i + 1
        if ch in ("B", "C", "I", "S", "Z"):
            return ITEM_Integer, i + 1
        if ch == "F":
            return ITEM_Float, i + 1
        if ch == "J":
            return ITEM_Long, i + 1
        if ch == "D":
            return ITEM_Double, i + 1
        if ch == "L":
            end = desc.index(";", i)
            cls_name = desc[i + 1 : end]
            return (ITEM_Object, cls_name), end + 1
        if ch == "[":
            j = i
            while desc[j] == "[":
                j += 1
            if desc[j] == "L":
                end = desc.index(";", j) + 1
            else:
                end = j + 1
            arr_desc = desc[i:end]
            return (ITEM_Object, arr_desc), end
        raise ValueError(f"Unknown descriptor char {ch} in {desc} at {i}")

    while desc[idx] != ")":
        vt, idx = read_one(idx)
        assert vt is not None
        args.append(vt)
    idx += 1  # skip ')'
    ret, _ = read_one(idx)
    return args, ret


def parse_field_type(desc: str) -> VType:
    vt, _ = parse_descriptor_types(f"(){desc}")[1], 0
    assert vt is not None
    return vt


def is_wide(vt: VType) -> bool:
    return vt == ITEM_Long or vt == ITEM_Double


class ConstantPool:
    def __init__(self) -> None:
        self.entries: List[Optional[bytes]] = [None]  # 1-indexed
        self.cache: Dict[Tuple, int] = {}

    def _add(self, key: Tuple, raw: bytes, wide: bool = False) -> int:
        if key in self.cache:
            return self.cache[key]
        idx = len(self.entries)
        self.entries.append(raw)
        if wide:
            self.entries.append(None)
        self.cache[key] = idx
        return idx

    def utf8(self, s: str) -> int:
        b = s.encode("utf-8")
        return self._add(("Utf8", s), struct.pack(">BH", 1, len(b)) + b)

    def integer(self, v: int) -> int:
        v32 = v & 0xFFFFFFFF
        return self._add(("Int", v32), struct.pack(">BI", 3, v32))

    def float_(self, v: float) -> int:
        raw_f = struct.pack(">f", v)
        return self._add(("Float", raw_f), b"\x04" + raw_f)

    def long_(self, v: int) -> int:
        v64 = v & 0xFFFFFFFFFFFFFFFF
        return self._add(("Long", v64), struct.pack(">BQ", 5, v64), wide=True)

    def double_(self, v: float) -> int:
        raw_d = struct.pack(">d", v)
        return self._add(("Double", raw_d), b"\x06" + raw_d, wide=True)

    def class_ref(self, internal_name: str) -> int:
        u = self.utf8(internal_name)
        return self._add(("Class", internal_name), struct.pack(">BH", 7, u))

    def string(self, s: str) -> int:
        u = self.utf8(s)
        return self._add(("String", s), struct.pack(">BH", 8, u))

    def name_and_type(self, name: str, desc: str) -> int:
        n = self.utf8(name)
        d = self.utf8(desc)
        return self._add(("NaT", name, desc), struct.pack(">BHH", 12, n, d))

    def fieldref(self, cls: str, name: str, desc: str) -> int:
        c = self.class_ref(cls)
        nt = self.name_and_type(name, desc)
        return self._add(("Field", cls, name, desc), struct.pack(">BHH", 9, c, nt))

    def methodref(self, cls: str, name: str, desc: str) -> int:
        c = self.class_ref(cls)
        nt = self.name_and_type(name, desc)
        return self._add(("Method", cls, name, desc), struct.pack(">BHH", 10, c, nt))

    def interface_methodref(self, cls: str, name: str, desc: str) -> int:
        c = self.class_ref(cls)
        nt = self.name_and_type(name, desc)
        return self._add(("IMethod", cls, name, desc), struct.pack(">BHH", 11, c, nt))

    def serialize(self) -> bytes:
        out = [struct.pack(">H", len(self.entries))]
        for e in self.entries[1:]:
            if e is not None:
                out.append(e)
        return b"".join(out)


class MethodAssembler:
    """High-level JVM method assembler with labels & automatic StackMapTable calculation."""

    def __init__(self, cp: ConstantPool, owner_cls: str, access: int, name: str, desc: str, super_cls: str = "java/lang/Object") -> None:
        self.cp = cp
        self.owner_cls = owner_cls
        self.super_cls = super_cls
        self.access = access
        self.name = name
        self.desc = desc
        self.insns: List[Tuple] = []
        self.annotations: List[Tuple[str, Dict[str, str]]] = []
        self._label_counter = 0

    def new_label(self, prefix: str = "L") -> str:
        self._label_counter += 1
        return f"{prefix}_{self._label_counter}"

    def label(self, name: str) -> "MethodAssembler":
        self.insns.append(("LABEL", name))
        return self

    def add_annotation(self, desc: str, values: Optional[Dict[str, str]] = None) -> "MethodAssembler":
        self.annotations.append((desc, values or {}))
        return self

    # --- Constants ---
    def aconst_null(self) -> "MethodAssembler":
        self.insns.append(("OP", 0x01, "aconst_null"))
        return self

    def iconst(self, v: int) -> "MethodAssembler":
        if -1 <= v <= 5:
            self.insns.append(("OP", 0x03 + v, f"iconst_{v}"))
        elif -128 <= v <= 127:
            self.insns.append(("BIPUSH", v))
        elif -32768 <= v <= 32767:
            self.insns.append(("SIPUSH", v))
        else:
            idx = self.cp.integer(v)
            self.insns.append(("LDC", idx, ITEM_Integer))
        return self

    def lconst(self, v: int) -> "MethodAssembler":
        if v == 0:
            self.insns.append(("OP", 0x09, "lconst_0"))
        elif v == 1:
            self.insns.append(("OP", 0x0A, "lconst_1"))
        else:
            idx = self.cp.long_(v)
            self.insns.append(("LDC2_W", idx, ITEM_Long))
        return self

    def fconst(self, v: float) -> "MethodAssembler":
        if v == 0.0 and str(v) == "0.0":
            self.insns.append(("OP", 0x0B, "fconst_0"))
        elif v == 1.0:
            self.insns.append(("OP", 0x0C, "fconst_1"))
        elif v == 2.0:
            self.insns.append(("OP", 0x0D, "fconst_2"))
        else:
            idx = self.cp.float_(v)
            self.insns.append(("LDC", idx, ITEM_Float))
        return self

    def dconst(self, v: float) -> "MethodAssembler":
        if v == 0.0 and str(v) == "0.0":
            self.insns.append(("OP", 0x0E, "dconst_0"))
        elif v == 1.0:
            self.insns.append(("OP", 0x0F, "dconst_1"))
        else:
            idx = self.cp.double_(v)
            self.insns.append(("LDC2_W", idx, ITEM_Double))
        return self

    def ldc_str(self, s: str) -> "MethodAssembler":
        idx = self.cp.string(s)
        self.insns.append(("LDC", idx, (ITEM_Object, "java/lang/String")))
        return self

    def ldc_class(self, internal_name: str) -> "MethodAssembler":
        idx = self.cp.class_ref(internal_name)
        self.insns.append(("LDC", idx, (ITEM_Object, "java/lang/Class")))
        return self

    # --- Locals ---
    def iload(self, slot: int) -> "MethodAssembler":
        self.insns.append(("LOAD", "I", slot))
        return self

    def lload(self, slot: int) -> "MethodAssembler":
        self.insns.append(("LOAD", "J", slot))
        return self

    def fload(self, slot: int) -> "MethodAssembler":
        self.insns.append(("LOAD", "F", slot))
        return self

    def dload(self, slot: int) -> "MethodAssembler":
        self.insns.append(("LOAD", "D", slot))
        return self

    def aload(self, slot: int) -> "MethodAssembler":
        self.insns.append(("LOAD", "A", slot))
        return self

    def istore(self, slot: int) -> "MethodAssembler":
        self.insns.append(("STORE", "I", slot))
        return self

    def lstore(self, slot: int) -> "MethodAssembler":
        self.insns.append(("STORE", "J", slot))
        return self

    def fstore(self, slot: int) -> "MethodAssembler":
        self.insns.append(("STORE", "F", slot))
        return self

    def dstore(self, slot: int) -> "MethodAssembler":
        self.insns.append(("STORE", "D", slot))
        return self

    def astore(self, slot: int) -> "MethodAssembler":
        self.insns.append(("STORE", "A", slot))
        return self

    def iinc(self, slot: int, delta: int) -> "MethodAssembler":
        assert 0 <= slot <= 255 and -128 <= delta <= 127
        self.insns.append(("IINC", slot, delta))
        return self

    # --- Stack ops ---
    def pop(self) -> "MethodAssembler":
        self.insns.append(("OP", 0x57, "pop"))
        return self

    def pop2(self) -> "MethodAssembler":
        self.insns.append(("OP", 0x58, "pop2"))
        return self

    def dup(self) -> "MethodAssembler":
        self.insns.append(("OP", 0x59, "dup"))
        return self

    # --- Arithmetic & conversions ---
    def op(self, mnemonic: str) -> "MethodAssembler":
        OPS = {
            "iadd": 0x60, "ladd": 0x61, "fadd": 0x62, "dadd": 0x63,
            "isub": 0x64, "lsub": 0x65, "fsub": 0x66, "dsub": 0x67,
            "imul": 0x68, "lmul": 0x69, "fmul": 0x6A, "dmul": 0x6B,
            "idiv": 0x6C, "ldiv": 0x6D, "fdiv": 0x6E, "ddiv": 0x6F,
            "irem": 0x70, "lrem": 0x71, "frem": 0x72, "drem": 0x73,
            "ineg": 0x74, "lneg": 0x75, "fneg": 0x76, "dneg": 0x77,
            "ishl": 0x78, "lshl": 0x79, "ishr": 0x7A, "lshr": 0x7B,
            "iushr": 0x7C, "lushr": 0x7D, "iand": 0x7E, "land": 0x7F,
            "ior": 0x80, "lor": 0x81, "ixor": 0x82, "lxor": 0x83,
            "i2l": 0x85, "i2f": 0x86, "i2d": 0x87,
            "l2i": 0x88, "l2f": 0x89, "l2d": 0x8A,
            "f2i": 0x8B, "f2l": 0x8C, "f2d": 0x8D,
            "d2i": 0x8E, "d2l": 0x8F, "d2f": 0x90,
            "i2b": 0x91, "i2c": 0x92, "i2s": 0x93,
            "lcmp": 0x94, "fcmpl": 0x95, "fcmpg": 0x96, "dcmpl": 0x97, "dcmpg": 0x98,
            "iaload": 0x2E, "laload": 0x2F, "faload": 0x30, "daload": 0x31, "aaload": 0x32, "baload": 0x33,
            "iastore": 0x4F, "lastore": 0x50, "fastore": 0x51, "dastore": 0x52, "aastore": 0x53, "bastore": 0x54,
            "arraylength": 0xBE,
            "athrow": 0xBF,
            "ireturn": 0xAC, "lreturn": 0xAD, "freturn": 0xAE, "dreturn": 0xAF, "areturn": 0xB0, "return": 0xB1,
        }
        self.insns.append(("OP", OPS[mnemonic], mnemonic))
        return self

    # --- Branches ---
    def jump(self, cond: str, target_label: str) -> "MethodAssembler":
        JUMPS = {
            "ifeq": 0x99, "ifne": 0x9A, "iflt": 0x9B, "ifge": 0x9C, "ifgt": 0x9D, "ifle": 0x9E,
            "if_icmpeq": 0x9F, "if_icmpne": 0xA0, "if_icmplt": 0xA1, "if_icmpge": 0xA2, "if_icmpgt": 0xA3, "if_icmple": 0xA4,
            "if_acmpeq": 0xA5, "if_acmpne": 0xA6,
            "goto": 0xA7,
            "ifnull": 0xC6, "ifnonnull": 0xC7,
        }
        self.insns.append(("JUMP", JUMPS[cond], cond, target_label))
        return self

    # --- Fields & Methods & Objects ---
    def getstatic(self, cls: str, name: str, desc: str) -> "MethodAssembler":
        idx = self.cp.fieldref(cls, name, desc)
        self.insns.append(("FIELD", 0xB2, "getstatic", idx, cls, name, desc))
        return self

    def putstatic(self, cls: str, name: str, desc: str) -> "MethodAssembler":
        idx = self.cp.fieldref(cls, name, desc)
        self.insns.append(("FIELD", 0xB3, "putstatic", idx, cls, name, desc))
        return self

    def getfield(self, cls: str, name: str, desc: str) -> "MethodAssembler":
        idx = self.cp.fieldref(cls, name, desc)
        self.insns.append(("FIELD", 0xB4, "getfield", idx, cls, name, desc))
        return self

    def putfield(self, cls: str, name: str, desc: str) -> "MethodAssembler":
        idx = self.cp.fieldref(cls, name, desc)
        self.insns.append(("FIELD", 0xB5, "putfield", idx, cls, name, desc))
        return self

    def invokevirtual(self, cls: str, name: str, desc: str) -> "MethodAssembler":
        idx = self.cp.methodref(cls, name, desc)
        self.insns.append(("INVOKE", 0xB6, "invokevirtual", idx, cls, name, desc))
        return self

    def invokespecial(self, cls: str, name: str, desc: str, is_interface: bool = False) -> "MethodAssembler":
        idx = self.cp.interface_methodref(cls, name, desc) if is_interface else self.cp.methodref(cls, name, desc)
        self.insns.append(("INVOKE", 0xB7, "invokespecial", idx, cls, name, desc))
        return self

    def invokestatic(self, cls: str, name: str, desc: str, is_interface: bool = False) -> "MethodAssembler":
        idx = self.cp.interface_methodref(cls, name, desc) if is_interface else self.cp.methodref(cls, name, desc)
        self.insns.append(("INVOKE", 0xB8, "invokestatic", idx, cls, name, desc))
        return self

    def invokeinterface(self, cls: str, name: str, desc: str) -> "MethodAssembler":
        idx = self.cp.interface_methodref(cls, name, desc)
        args, _ = parse_descriptor_types(desc)
        count = 1 + sum(2 if is_wide(a) else 1 for a in args)
        self.insns.append(("INVOKEINTERFACE", 0xB9, idx, count, cls, name, desc))
        return self

    def new_(self, cls: str) -> "MethodAssembler":
        idx = self.cp.class_ref(cls)
        self.insns.append(("NEW", idx, cls))
        return self

    def newarray(self, prim_type: str) -> "MethodAssembler":
        # T_BOOLEAN=4, T_CHAR=5, T_FLOAT=6, T_DOUBLE=7, T_BYTE=8, T_SHORT=9, T_INT=10, T_LONG=11
        MAP = {"Z": (4, "[Z"), "C": (5, "[C"), "F": (6, "[F"), "D": (7, "[D"),
               "B": (8, "[B"), "S": (9, "[S"), "I": (10, "[I"), "J": (11, "[J")}
        atype, arr_desc = MAP[prim_type]
        self.insns.append(("NEWARRAY", atype, arr_desc))
        return self

    def anewarray(self, cls: str) -> "MethodAssembler":
        idx = self.cp.class_ref(cls)
        arr_desc = f"[{cls}" if cls.startswith("[") else f"[L{cls};"
        self.insns.append(("ANEWARRAY", idx, arr_desc))
        return self

    def checkcast(self, cls: str) -> "MethodAssembler":
        idx = self.cp.class_ref(cls)
        vt_cls = cls
        self.insns.append(("CHECKCAST", idx, vt_cls))
        return self

    def instanceof_(self, cls: str) -> "MethodAssembler":
        idx = self.cp.class_ref(cls)
        self.insns.append(("INSTANCEOF", idx, cls))
        return self

    # --- Compilation & StackMapTable ---
    def _encode_load_store(self, kind: str, typ: str, slot: int) -> bytes:
        BASE_LOAD = {"I": (0x15, 0x1A), "J": (0x16, 0x1E), "F": (0x17, 0x22), "D": (0x18, 0x26), "A": (0x19, 0x2A)}
        BASE_STORE = {"I": (0x36, 0x3B), "J": (0x37, 0x3F), "F": (0x38, 0x43), "D": (0x39, 0x47), "A": (0x3A, 0x4B)}
        op_var, op_0 = (BASE_LOAD if kind == "LOAD" else BASE_STORE)[typ]
        if 0 <= slot <= 3:
            return bytes([op_0 + slot])
        elif slot <= 255:
            return bytes([op_var, slot])
        else:
            return struct.pack(">BBH", 0xC4, op_var, slot)

    def build_method_info(self) -> bytes:
        # 1. Assign bytecode offsets
        label_offsets: Dict[str, int] = {}
        concrete: List[Tuple[int, Tuple]] = []
        pc = 0
        for insn in self.insns:
            kind = insn[0]
            if kind == "LABEL":
                label_offsets[insn[1]] = pc
                continue
            concrete.append((pc, insn))
            if kind == "OP":
                pc += 1
            elif kind == "BIPUSH":
                pc += 2
            elif kind == "SIPUSH":
                pc += 3
            elif kind == "LDC":
                idx = insn[1]
                pc += 2 if idx <= 255 else 3
            elif kind == "LDC2_W":
                pc += 3
            elif kind in ("LOAD", "STORE"):
                pc += len(self._encode_load_store(kind, insn[1], insn[2]))
            elif kind == "IINC":
                pc += 3
            elif kind == "JUMP":
                pc += 3
            elif kind in ("FIELD", "INVOKE", "NEW", "ANEWARRAY", "CHECKCAST", "INSTANCEOF"):
                pc += 3
            elif kind == "NEWARRAY":
                pc += 2
            elif kind == "INVOKEINTERFACE":
                pc += 5
            else:
                raise ValueError(f"Unhandled insn {insn}")

        code_len = pc
        offset_to_idx = {off: i for i, (off, _) in enumerate(concrete)}

        # 2. Emit bytecode bytes & identify jump target offsets
        jump_targets = set()
        code_parts: List[bytes] = []
        for off, insn in concrete:
            kind = insn[0]
            if kind == "OP":
                code_parts.append(bytes([insn[1]]))
            elif kind == "BIPUSH":
                code_parts.append(struct.pack(">Bb", 0x10, insn[1]))
            elif kind == "SIPUSH":
                code_parts.append(struct.pack(">Bh", 0x11, insn[1]))
            elif kind == "LDC":
                idx = insn[1]
                if idx <= 255:
                    code_parts.append(struct.pack(">BB", 0x12, idx))
                else:
                    code_parts.append(struct.pack(">BH", 0x13, idx))
            elif kind == "LDC2_W":
                code_parts.append(struct.pack(">BH", 0x14, insn[1]))
            elif kind in ("LOAD", "STORE"):
                code_parts.append(self._encode_load_store(kind, insn[1], insn[2]))
            elif kind == "IINC":
                code_parts.append(struct.pack(">BBb", 0x84, insn[1], insn[2]))
            elif kind == "JUMP":
                target_off = label_offsets[insn[3]]
                jump_targets.add(target_off)
                rel = target_off - off
                assert -32768 <= rel <= 32767, f"Branch offset {rel} out of short range in {self.name}"
                code_parts.append(struct.pack(">Bh", insn[1], rel))
            elif kind in ("FIELD", "INVOKE"):
                code_parts.append(struct.pack(">BH", insn[1], insn[3]))
            elif kind == "INVOKEINTERFACE":
                code_parts.append(struct.pack(">BHBB", 0xB9, insn[2], insn[3], 0))
            elif kind == "NEW":
                code_parts.append(struct.pack(">BH", 0xBB, insn[1]))
            elif kind == "NEWARRAY":
                code_parts.append(struct.pack(">BB", 0xBC, insn[1]))
            elif kind == "ANEWARRAY":
                code_parts.append(struct.pack(">BH", 0xBD, insn[1]))
            elif kind == "CHECKCAST":
                code_parts.append(struct.pack(">BH", 0xC0, insn[1]))
            elif kind == "INSTANCEOF":
                code_parts.append(struct.pack(">BH", 0xC1, insn[1]))

        bytecode = b"".join(code_parts)
        assert len(bytecode) == code_len

        # 3. Abstract interpretation for max_stack, max_locals, and StackMapTable
        arg_vtypes, _ = parse_descriptor_types(self.desc)
        init_locals: List[Union[VType, str]] = []
        if not (self.access & ACC_STATIC):
            if self.name == "<init>":
                init_locals.append(ITEM_UninitializedThis)
            else:
                init_locals.append((ITEM_Object, self.owner_cls))
        for av in arg_vtypes:
            init_locals.append(av)
            if is_wide(av):
                init_locals.append(SECOND_SLOT)

        def merge_vtype(a: Union[VType, str], b: Union[VType, str]) -> Union[VType, str]:
            if a == b:
                return a
            if a == SECOND_SLOT or b == SECOND_SLOT:
                return ITEM_Top
            if a == ITEM_Null and isinstance(b, tuple) and b[0] == ITEM_Object:
                return b
            if b == ITEM_Null and isinstance(a, tuple) and a[0] == ITEM_Object:
                return a
            if isinstance(a, tuple) and a[0] == ITEM_Object and isinstance(b, tuple) and b[0] == ITEM_Object:
                return (ITEM_Object, "java/lang/Object")
            return ITEM_Top

        def merge_state(
            dst_locals: List[Union[VType, str]],
            dst_stack: List[VType],
            src_locals: List[Union[VType, str]],
            src_stack: List[VType],
        ) -> Tuple[bool, List[Union[VType, str]], List[VType]]:
            assert len(dst_stack) == len(src_stack), (
                f"Stack height mismatch in {self.owner_cls}.{self.name}: {dst_stack} vs {src_stack}"
            )
            changed = False
            max_l = max(len(dst_locals), len(src_locals))
            new_l: List[Union[VType, str]] = []
            for i in range(max_l):
                va = dst_locals[i] if i < len(dst_locals) else ITEM_Top
                vb = src_locals[i] if i < len(src_locals) else ITEM_Top
                vm = merge_vtype(va, vb)
                if i >= len(dst_locals) or vm != dst_locals[i]:
                    changed = True
                new_l.append(vm)
            # Clean broken wide pairs
            for i in range(len(new_l)):
                if new_l[i] in (ITEM_Long, ITEM_Double):
                    if i + 1 >= len(new_l) or new_l[i + 1] != SECOND_SLOT:
                        new_l[i] = ITEM_Top
                        changed = True
                elif new_l[i] == SECOND_SLOT:
                    if i == 0 or new_l[i - 1] not in (ITEM_Long, ITEM_Double):
                        new_l[i] = ITEM_Top
                        changed = True
            new_s: List[VType] = []
            for sa, sb in zip(dst_stack, src_stack):
                sm = merge_vtype(sa, sb)
                assert sm != SECOND_SLOT
                if sm != sa:
                    changed = True
                new_s.append(sm)  # type: ignore
            return changed, new_l, new_s

        states: Dict[int, Tuple[List[Union[VType, str]], List[VType]]] = {
            0: (list(init_locals), [])
        }
        worklist = [0]
        max_stack_slots = 0
        max_locals_slots = len(init_locals)

        def stack_slots(st: List[VType]) -> int:
            return sum(2 if is_wide(x) else 1 for x in st)

        def set_local(locs: List[Union[VType, str]], slot: int, vt: VType) -> None:
            nonlocal max_locals_slots
            need = slot + (2 if is_wide(vt) else 1)
            while len(locs) < need:
                locs.append(ITEM_Top)
            # If overwriting 2nd slot of a wide local, invalidate slot-1
            if slot > 0 and locs[slot] == SECOND_SLOT:
                locs[slot - 1] = ITEM_Top
            # If overwriting 1st slot of a wide local with narrow, invalidate slot+1
            if not is_wide(vt) and slot + 1 < len(locs) and locs[slot + 1] == SECOND_SLOT:
                locs[slot + 1] = ITEM_Top
            locs[slot] = vt
            if is_wide(vt):
                if slot + 2 < len(locs) and locs[slot + 2] == SECOND_SLOT:
                    locs[slot + 2] = ITEM_Top
                locs[slot + 1] = SECOND_SLOT
            if len(locs) > max_locals_slots:
                max_locals_slots = len(locs)

        def propagate(target_off: int, locs: List[Union[VType, str]], st: List[VType]) -> None:
            if target_off not in offset_to_idx:
                return
            if target_off not in states:
                states[target_off] = (list(locs), list(st))
                worklist.append(target_off)
            else:
                old_l, old_s = states[target_off]
                ch, nl, ns = merge_state(old_l, old_s, locs, st)
                if ch:
                    states[target_off] = (nl, ns)
                    if target_off not in worklist:
                        worklist.append(target_off)

        while worklist:
            off = worklist.pop(0)
            idx = offset_to_idx[off]
            _, insn = concrete[idx]
            next_off = concrete[idx + 1][0] if idx + 1 < len(concrete) else -1
            locs, st = [ x for x in states[off][0] ], [ x for x in states[off][1] ]
            max_stack_slots = max(max_stack_slots, stack_slots(st))
            max_locals_slots = max(max_locals_slots, len(locs))

            kind = insn[0]
            fallthrough = True

            if kind == "OP":
                mn = insn[2]
                if mn == "aconst_null":
                    st.append(ITEM_Null)
                elif mn.startswith("iconst_"):
                    st.append(ITEM_Integer)
                elif mn.startswith("lconst_"):
                    st.append(ITEM_Long)
                elif mn.startswith("fconst_"):
                    st.append(ITEM_Float)
                elif mn.startswith("dconst_"):
                    st.append(ITEM_Double)
                elif mn == "pop":
                    v = st.pop()
                    assert not is_wide(v)
                elif mn == "pop2":
                    v = st.pop()
                    if not is_wide(v):
                        v2 = st.pop()
                        assert not is_wide(v2)
                elif mn == "dup":
                    v = st[-1]
                    assert not is_wide(v)
                    st.append(v)
                elif mn in ("iadd", "isub", "imul", "idiv", "irem", "ishl", "ishr", "iushr", "iand", "ior", "ixor"):
                    st.pop(); st.pop(); st.append(ITEM_Integer)
                elif mn in ("ladd", "lsub", "lmul", "ldiv", "lrem", "land", "lor", "lxor"):
                    st.pop(); st.pop(); st.append(ITEM_Long)
                elif mn in ("lshl", "lshr", "lushr"):
                    st.pop(); st.pop(); st.append(ITEM_Long)
                elif mn in ("fadd", "fsub", "fmul", "fdiv", "frem"):
                    st.pop(); st.pop(); st.append(ITEM_Float)
                elif mn in ("dadd", "dsub", "dmul", "ddiv", "drem"):
                    st.pop(); st.pop(); st.append(ITEM_Double)
                elif mn in ("ineg", "i2b", "i2c", "i2s"):
                    st.pop(); st.append(ITEM_Integer)
                elif mn == "lneg":
                    st.pop(); st.append(ITEM_Long)
                elif mn == "fneg":
                    st.pop(); st.append(ITEM_Float)
                elif mn == "dneg":
                    st.pop(); st.append(ITEM_Double)
                elif mn == "i2l":
                    st.pop(); st.append(ITEM_Long)
                elif mn == "i2f":
                    st.pop(); st.append(ITEM_Float)
                elif mn == "i2d":
                    st.pop(); st.append(ITEM_Double)
                elif mn == "l2i":
                    st.pop(); st.append(ITEM_Integer)
                elif mn == "l2f":
                    st.pop(); st.append(ITEM_Float)
                elif mn == "l2d":
                    st.pop(); st.append(ITEM_Double)
                elif mn == "f2i":
                    st.pop(); st.append(ITEM_Integer)
                elif mn == "f2l":
                    st.pop(); st.append(ITEM_Long)
                elif mn == "f2d":
                    st.pop(); st.append(ITEM_Double)
                elif mn == "d2i":
                    st.pop(); st.append(ITEM_Integer)
                elif mn == "d2l":
                    st.pop(); st.append(ITEM_Long)
                elif mn == "d2f":
                    st.pop(); st.append(ITEM_Float)
                elif mn in ("lcmp", "fcmpl", "fcmpg", "dcmpl", "dcmpg"):
                    st.pop(); st.pop(); st.append(ITEM_Integer)
                elif mn in ("iaload", "baload"):
                    st.pop(); st.pop(); st.append(ITEM_Integer)
                elif mn == "laload":
                    st.pop(); st.pop(); st.append(ITEM_Long)
                elif mn == "faload":
                    st.pop(); st.pop(); st.append(ITEM_Float)
                elif mn == "daload":
                    st.pop(); st.pop(); st.append(ITEM_Double)
                elif mn == "aaload":
                    st.pop()
                    arr_t = st.pop()
                    if isinstance(arr_t, tuple) and arr_t[0] == ITEM_Object and isinstance(arr_t[1], str) and arr_t[1].startswith("["):
                        elem_d = arr_t[1][1:]
                        st.append(parse_field_type(elem_d))
                    else:
                        st.append((ITEM_Object, "java/lang/Object"))
                elif mn in ("iastore", "bastore", "lastore", "fastore", "dastore", "aastore"):
                    st.pop(); st.pop(); st.pop()
                elif mn == "arraylength":
                    st.pop(); st.append(ITEM_Integer)
                elif mn == "athrow":
                    st.pop()
                    fallthrough = False
                elif mn in ("ireturn", "lreturn", "freturn", "dreturn", "areturn"):
                    st.pop()
                    fallthrough = False
                elif mn == "return":
                    fallthrough = False
            elif kind in ("BIPUSH", "SIPUSH"):
                st.append(ITEM_Integer)
            elif kind in ("LDC", "LDC2_W"):
                st.append(insn[2])
            elif kind == "LOAD":
                typ, slot = insn[1], insn[2]
                vt = locs[slot]
                assert vt != SECOND_SLOT and vt != ITEM_Top, f"Invalid load slot {slot} in {self.name}: {locs}"
                st.append(vt)  # type: ignore
            elif kind == "STORE":
                typ, slot = insn[1], insn[2]
                vt = st.pop()
                set_local(locs, slot, vt)
            elif kind == "IINC":
                pass
            elif kind == "JUMP":
                cond, target_lbl = insn[2], insn[3]
                target_off = label_offsets[target_lbl]
                if cond == "goto":
                    fallthrough = False
                elif cond in ("ifeq", "ifne", "iflt", "ifge", "ifgt", "ifle", "ifnull", "ifnonnull"):
                    st.pop()
                else:
                    st.pop(); st.pop()
                propagate(target_off, locs, st)
            elif kind == "FIELD":
                mn, _, cls_n, f_name, f_desc = insn[2], insn[3], insn[4], insn[5], insn[6]
                f_vt = parse_field_type(f_desc)
                if mn == "getstatic":
                    st.append(f_vt)
                elif mn == "putstatic":
                    st.pop()
                elif mn == "getfield":
                    st.pop()
                    st.append(f_vt)
                elif mn == "putfield":
                    st.pop()
                    st.pop()
            elif kind in ("INVOKE", "INVOKEINTERFACE"):
                if kind == "INVOKE":
                    mn, m_cls, m_name, m_desc = insn[2], insn[4], insn[5], insn[6]
                else:
                    mn, m_cls, m_name, m_desc = "invokeinterface", insn[4], insn[5], insn[6]
                m_args, m_ret = parse_descriptor_types(m_desc)
                for _ in m_args:
                    st.pop()
                if mn != "invokestatic":
                    recv = st.pop()
                    if mn == "invokespecial" and m_name == "<init>":
                        if recv == ITEM_UninitializedThis:
                            new_obj: VType = (ITEM_Object, self.owner_cls)
                            locs = [new_obj if x == ITEM_UninitializedThis else x for x in locs]
                            st = [new_obj if x == ITEM_UninitializedThis else x for x in st]
                        elif isinstance(recv, tuple) and recv[0] == ITEM_Uninitialized:
                            new_obj = (ITEM_Object, m_cls)
                            locs = [new_obj if x == recv else x for x in locs]
                            st = [new_obj if x == recv else x for x in st]
                if m_ret is not None:
                    st.append(m_ret)
            elif kind == "NEW":
                st.append((ITEM_Uninitialized, off))
            elif kind == "NEWARRAY":
                st.pop()
                st.append((ITEM_Object, insn[2]))
            elif kind == "ANEWARRAY":
                st.pop()
                st.append((ITEM_Object, insn[2]))
            elif kind == "CHECKCAST":
                st.pop()
                st.append((ITEM_Object, insn[2]))
            elif kind == "INSTANCEOF":
                st.pop()
                st.append(ITEM_Integer)

            max_stack_slots = max(max_stack_slots, stack_slots(st))
            if fallthrough and next_off != -1:
                propagate(next_off, locs, st)

        # 4. Build StackMapTable attribute using canonical full_frame (tag 255)
        def encode_vtype(vt: VType) -> bytes:
            if isinstance(vt, int):
                return bytes([vt])
            tag, payload = vt
            if tag == ITEM_Object:
                cidx = self.cp.class_ref(str(payload))
                return struct.pack(">BH", 7, cidx)
            elif tag == ITEM_Uninitialized:
                return struct.pack(">BH", 8, int(payload))
            raise ValueError(f"Unknown vtype {vt}")

        sorted_targets = sorted(t for t in jump_targets if t in states)
        smt_attr = b""
        if sorted_targets:
            frames_bin: List[bytes] = []
            prev_off = -1
            for i, toff in enumerate(sorted_targets):
                offset_delta = toff if i == 0 else (toff - prev_off - 1)
                prev_off = toff
                t_locs, t_stack = states[toff]
                # Trim trailing ITEM_Top from locals
                trimmed_locs = list(t_locs)
                while trimmed_locs and trimmed_locs[-1] == ITEM_Top:
                    trimmed_locs.pop()
                enc_locs: List[bytes] = []
                li = 0
                while li < len(trimmed_locs):
                    lv = trimmed_locs[li]
                    assert lv != SECOND_SLOT
                    enc_locs.append(encode_vtype(lv))  # type: ignore
                    li += 2 if is_wide(lv) else 1  # type: ignore
                enc_stk = [encode_vtype(sv) for sv in t_stack]
                frame = struct.pack(">BHH", 255, offset_delta, len(enc_locs)) + b"".join(enc_locs) + struct.pack(">H", len(enc_stk)) + b"".join(enc_stk)
                frames_bin.append(frame)
            smt_payload = struct.pack(">H", len(frames_bin)) + b"".join(frames_bin)
            smt_name_idx = self.cp.utf8("StackMapTable")
            smt_attr = struct.pack(">HI", smt_name_idx, len(smt_payload)) + smt_payload

        code_attrs_count = 1 if smt_attr else 0
        code_payload = (
            struct.pack(">HHI", max_stack_slots, max_locals_slots, len(bytecode))
            + bytecode
            + struct.pack(">H", 0)  # exception_table_length = 0
            + struct.pack(">H", code_attrs_count)
            + smt_attr
        )
        code_attr_name = self.cp.utf8("Code")
        code_attr = struct.pack(">HI", code_attr_name, len(code_payload)) + code_payload

        method_attrs: List[bytes] = [code_attr]
        if self.annotations:
            ann_bin = _encode_annotations(self.cp, self.annotations)
            rva_name = self.cp.utf8("RuntimeVisibleAnnotations")
            method_attrs.append(struct.pack(">HI", rva_name, len(ann_bin)) + ann_bin)

        m_name_idx = self.cp.utf8(self.name)
        m_desc_idx = self.cp.utf8(self.desc)
        return struct.pack(">HHHH", self.access, m_name_idx, m_desc_idx, len(method_attrs)) + b"".join(method_attrs)


def _encode_annotations(cp: ConstantPool, annotations: List[Tuple[str, Dict[str, str]]]) -> bytes:
    parts = [struct.pack(">H", len(annotations))]
    for desc, kv in annotations:
        type_idx = cp.utf8(desc)
        parts.append(struct.pack(">HH", type_idx, len(kv)))
        for k, v in kv.items():
            k_idx = cp.utf8(k)
            v_idx = cp.utf8(v)
            parts.append(struct.pack(">HBH", k_idx, ord("s"), v_idx))
    return b"".join(parts)


class ClassWriter:
    def __init__(self, access: int, name: str, super_name: str = "java/lang/Object", interfaces: Optional[List[str]] = None, major: int = 61) -> None:
        self.cp = ConstantPool()
        self.access = access
        self.name = name
        self.super_name = super_name
        self.interfaces = interfaces or []
        self.major = major
        self.fields: List[Tuple[int, str, str]] = []
        self.methods: List[MethodAssembler] = []
        self.annotations: List[Tuple[str, Dict[str, str]]] = []
        self.this_cls_idx = self.cp.class_ref(name)
        self.super_cls_idx = self.cp.class_ref(super_name)
        self.iface_indices = [self.cp.class_ref(i) for i in self.interfaces]

    def add_annotation(self, desc: str, values: Optional[Dict[str, str]] = None) -> "ClassWriter":
        self.annotations.append((desc, values or {}))
        return self

    def add_field(self, access: int, name: str, desc: str) -> "ClassWriter":
        self.cp.utf8(name)
        self.cp.utf8(desc)
        self.fields.append((access, name, desc))
        return self

    def add_method(self, access: int, name: str, desc: str) -> MethodAssembler:
        ma = MethodAssembler(self.cp, self.name, access, name, desc, self.super_name)
        self.methods.append(ma)
        return ma

    def to_bytes(self) -> bytes:
        fields_bin = [struct.pack(">H", len(self.fields))]
        for f_acc, f_name, f_desc in self.fields:
            fields_bin.append(struct.pack(">HHHH", f_acc, self.cp.utf8(f_name), self.cp.utf8(f_desc), 0))

        methods_bin = [struct.pack(">H", len(self.methods))]
        for m in self.methods:
            if m.access & (ACC_ABSTRACT | ACC_INTERFACE) and not m.insns:
                m_attrs: List[bytes] = []
                if m.annotations:
                    ann_bin = _encode_annotations(self.cp, m.annotations)
                    rva_name = self.cp.utf8("RuntimeVisibleAnnotations")
                    m_attrs.append(struct.pack(">HI", rva_name, len(ann_bin)) + ann_bin)
                methods_bin.append(
                    struct.pack(">HHHH", m.access, self.cp.utf8(m.name), self.cp.utf8(m.desc), len(m_attrs))
                    + b"".join(m_attrs)
                )
            else:
                methods_bin.append(m.build_method_info())

        cls_attrs: List[bytes] = []
        if self.annotations:
            ann_bin = _encode_annotations(self.cp, self.annotations)
            rva_name = self.cp.utf8("RuntimeVisibleAnnotations")
            cls_attrs.append(struct.pack(">HI", rva_name, len(ann_bin)) + ann_bin)

        header = struct.pack(">IHH", 0xCAFEBABE, 0, self.major)
        cp_bytes = self.cp.serialize()
        mid = struct.pack(">HHHH", self.access, self.this_cls_idx, self.super_cls_idx, len(self.iface_indices))
        ifaces_bytes = b"".join(struct.pack(">H", idx) for idx in self.iface_indices)
        tail = struct.pack(">H", len(cls_attrs)) + b"".join(cls_attrs)
        return header + cp_bytes + mid + ifaces_bytes + b"".join(fields_bin) + b"".join(methods_bin) + tail


if __name__ == "__main__":
    import os, tempfile, subprocess
    with tempfile.TemporaryDirectory() as tmp:
        cw = ClassWriter(ACC_PUBLIC | ACC_SUPER, "SelfTest")
        init = cw.add_method(ACC_PUBLIC, "<init>", "()V")
        init.aload(0).invokespecial("java/lang/Object", "<init>", "()V").op("return")

        main = cw.add_method(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V")
        # Test loops, branches, floats, doubles, longs, arrays, objects!
        main.iconst(0).istore(1)      # sum = 0
        main.iconst(0).istore(2)      # i = 0
        main.dconst(1.5).dstore(3)    # d = 1.5 (slots 3,4)
        main.fconst(2.5).fstore(5)    # f = 2.5 (slot 5)
        main.label("loop")
        main.iload(2).iconst(10).jump("if_icmpge", "done")
        main.iload(2).iconst(2).op("irem").jump("ifeq", "even")
        main.iload(1).iload(2).op("iadd").istore(1)
        main.jump("goto", "next")
        main.label("even")
        main.iload(1).iload(2).iconst(2).op("imul").op("iadd").istore(1)
        main.label("next")
        main.dload(3).fload(5).op("f2d").op("dadd").dstore(3)
        main.iinc(2, 1)
        main.jump("goto", "loop")
        main.label("done")
        main.getstatic("java/lang/System", "out", "Ljava/io/PrintStream;")
        main.ldc_str("JVM_ASM_VERIFIED_OK")
        main.invokevirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V")
        main.op("return")

        p = os.path.join(tmp, "SelfTest.class")
        with open(p, "wb") as f:
            f.write(cw.to_bytes())
        res = subprocess.run(["java", "-Xverify:all", "-cp", tmp, "SelfTest"], capture_output=True, text=True)
        print("STDOUT:", res.stdout.strip())
        print("STDERR:", res.stderr.strip())
        assert res.returncode == 0 and "JVM_ASM_VERIFIED_OK" in res.stdout
