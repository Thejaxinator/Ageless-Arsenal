"""Shared model-space authoring and Blockbench export for the Siegecast and Taken workshop."""
import base64
from io import BytesIO
import math
from uuid import NAMESPACE_URL, uuid5


class Rig:
    def __init__(self, kind, name):
        self.d = dict(name=name, kind=kind, parts=[], texture_width=512, texture_height=128, emissive_palette=[])
        self.parts = self.d["parts"]
        self.nodes = {}

    def joint(self, name, parent, x, y, z, rx=0, ry=0, rz=0):
        if name not in self.nodes:
            node = dict(name=name, boxes=[])
            self.nodes[name] = node
            self.parts.append(node)
        self.nodes[name].update(parent=parent, pivot=[x,y,z],
                                rotation=[round(math.radians(a),6) for a in (rx,ry,rz)])
        return name

    def box(self, node, x, y, z, width, height, depth, color):
        # Artists specify X and Z centers and the top edge in Y-down Minecraft model space.
        corner = [round(x-width/2,3), y, round(z-depth/2,3)]
        self.nodes[node]["boxes"].append([corner, [width,height,depth], color])


def ordered(parts):
    """Keep authored sibling order and place each parent before its children."""
    names = {p["name"] for p in parts}
    if len(parts) != len(names):
        raise ValueError("Duplicate bone name")
    output, seen = [], {None}
    while len(output) < len(parts):
        ready = [p for p in parts if p["parent"] in seen and p["name"] not in seen]
        if not ready:
            missing = [p["name"] for p in parts if p["name"] not in seen]
            raise ValueError(f"Missing parent or cycle among {missing}")
        output.extend(ready)
        seen.update(p["name"] for p in ready)
    return output


def split_long_faces(part):
    """Divide long cuboids at the material tile boundary used by the native model compiler."""
    output = []
    for corner, dims, color in part["boxes"]:
        counts = [max(1, math.ceil(v/15.5)) for v in dims]
        span = [v/n for v,n in zip(dims,counts)]
        for x in range(counts[0]):
            for y in range(counts[1]):
                for z in range(counts[2]):
                    origin = [a+b*i for a,b,i in zip(corner,span,(x,y,z))]
                    output.append([origin,span,color])
    part["boxes"] = output


def png(image):
    stream = BytesIO()
    image.save(stream, "PNG")
    return stream.getvalue()


def box_uv(start, end, material):
    w,h,d = (b-a for a,b in zip(start,end))
    u,v = material%8*64, material//8*64
    panels = {
        "north": (u+d,v+d,u+d+w,v+d+h),
        "south": (u+2*d+w,v+d,u+2*d+2*w,v+d+h),
        "east": (u+d+w,v+d,u+2*d+w,v+d+h),
        "west": (u,v+d,u+d,v+d+h),
        "up": (u+d+w,v+d,u+d,v),
        "down": (u+d+2*w,v,u+d+w,v+d),
    }
    return {name: {"texture":0, "uv":[round(c,4) for c in coords]} for name,coords in panels.items()}


def editor(rig, encoded_texture):
    """Keep native axes, pivots, materials and all hand/foregrip attachment nodes editable."""
    model = rig.d["name"]
    def stable_id(path):
        return str(uuid5(NAMESPACE_URL, f"destiny2mc/{model}/{path}"))

    origins = {}
    groups = {}
    outliner, elements = [], []
    for part in rig.parts:
        name, parent = part["name"], part["parent"]
        offset = origins.get(parent,[0,0,0])
        absolute = [x+y for x,y in zip(part["pivot"],offset)]
        origins[name] = absolute
        origin = [absolute[0],24-absolute[1],absolute[2]]
        x,y,z = [math.degrees(a) for a in part.get("rotation",(0,0,0))]
        group = dict(name=name, origin=origin, rotation=[-x,y,-z], uuid=stable_id("joint/"+name), children=[])
        (outliner if parent is None else groups[parent]["children"]).append(group)
        groups[name] = group
        for i,(corner,(w,h,d),material) in enumerate(part["boxes"]):
            x,y,z = [a+b for a,b in zip(corner,absolute)]
            start = [x,24-y-h,z]
            end = [x+w,24-y,z+d]
            cube = {
                "name":name, "uuid":stable_id(f"cube/{name}/{i}"), "box_uv":True,
                "uv_offset":[material%8*64, material//8*64],
                "origin":origin, "rotation":[0,0,0], "faces":box_uv(start,end,material),
                "from":start, "to":end,
            }
            elements.append(cube)
            group["children"].append(cube["uuid"])
    return {
        "meta":{"format_version":"4.10", "model_format":"free", "box_uv":True},
        "name":model, "resolution":{"width":rig.d["texture_width"],"height":rig.d["texture_height"]},
        "elements":elements, "outliner":outliner,
        "textures":[{"name":model+".png","id":"0","uuid":stable_id("texture"),
                     "source":"data:image/png;base64,"+base64.b64encode(encoded_texture).decode("ascii")}],
    }
