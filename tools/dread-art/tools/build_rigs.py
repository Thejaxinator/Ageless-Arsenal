"""Compile articulated palette-cuboid rigs against the existing 1.21.1 renderer ABI."""
from pathlib import Path
import sys,json,math
ROOT=Path(__file__).resolve().parents[1];sys.path.insert(0,str(ROOT/'source'))
from jvm_asm import *
PKG='com/jax/destiny2mc/client';MODEL=PKG+'/model/ConvertedFactionModel'
MP='net/minecraft/client/model/geom/ModelPart';PD='net/minecraft/client/model/geom/builders/PartDefinition';CB='net/minecraft/client/model/geom/builders/CubeListBuilder';PP='net/minecraft/client/model/geom/PartPose';MD='net/minecraft/client/model/geom/builders/MeshDefinition';LD='net/minecraft/client/model/geom/builders/LayerDefinition';ENTITY='net/minecraft/world/entity/Entity';HM='net/minecraft/client/model/HierarchicalModel'
ORDER=['body','head','r_arm','l_arm','r_forearm','l_forearm','r_leg','l_leg','r_shin','l_shin','extra']
def build_geometry(data):
 name=PKG+'/model/ConvertedRig'+str(data['kind']);cw=ClassWriter(ACC_PUBLIC|ACC_SUPER,name)
 # One helper per anatomical part keeps Code attributes well below the JVM's 64 KiB limit.
 for part in data['parts']:
  m=cw.add_method(ACC_PUBLIC|ACC_STATIC,part['name'],f'(L{PD};)L{PD};')
  m.invokestatic(CB,'create',f'()L{CB};').astore(1)
  for mn,size,c in part['boxes']:
   # Split long spans so vanilla cube UVs cannot leak into another palette tile.
   dims=[max(1,math.ceil(v/15.5)) for v in size]
   for ix in range(dims[0]):
    for iy in range(dims[1]):
     for iz in range(dims[2]):
      dd=[size[i]/dims[i] for i in range(3)];pos=[mn[i]+[ix,iy,iz][i]*dd[i] for i in range(3)]
      m.aload(1).iconst(c%8*64).iconst(c//8*64).invokevirtual(CB,'texOffs',f'(II)L{CB};')
      for f in pos+dd:m.fconst(f)
      m.invokevirtual(CB,'addBox',f'(FFFFFF)L{CB};').pop()
  m.aload(0).ldc_str(part['name']).aload(1)
  for v in part['pivot']:m.fconst(v)
  
  if part.get('rotation'):
   for v in part['rotation']:m.fconst(v)
   m.invokestatic(PP,'offsetAndRotation',f'(FFFFFF)L{PP};')
  else:m.invokestatic(PP,'offset',f'(FFF)L{PP};')
  m.invokevirtual(PD,'addOrReplaceChild',f'(Ljava/lang/String;L{CB};L{PP};)L{PD};').op('areturn')
 m=cw.add_method(ACC_PUBLIC|ACC_STATIC,'create',f'()L{LD};');m.new_(MD).dup().invokespecial(MD,'<init>','()V').astore(0);m.aload(0).invokevirtual(MD,'getRoot',f'()L{PD};').astore(1)
 for i,p in enumerate(data['parts']):
  parent_slot=1 if p['parent'] is None else [v['name'] for v in data['parts']].index(p['parent'])+2
  m.aload(parent_slot).invokestatic(name,p['name'],f'(L{PD};)L{PD};').astore(i+2)
 m.aload(0).iconst(512).iconst(data.get('texture_height',192)).invokestatic(LD,'create',f'(L{MD};II)L{LD};').op('areturn')
 return name,cw.to_bytes()
def build_model():
 cw=ClassWriter(ACC_PUBLIC|ACC_SUPER,MODEL,HM)
 cw.add_field(ACC_PRIVATE|ACC_FINAL,'root',f'L{MP};');cw.add_field(ACC_PRIVATE|ACC_FINAL,'floating','Z')
 for p in ORDER:cw.add_field(ACC_PRIVATE|ACC_FINAL,p,f'L{MP};')
 m=cw.add_method(ACC_PUBLIC,'<init>',f'(L{MP};Z)V');m.aload(0).invokespecial(HM,'<init>','()V');m.aload(0).aload(1).putfield(MODEL,'root',f'L{MP};');m.aload(0).iload(2).putfield(MODEL,'floating','Z')
 for p in ORDER:
  parent={'r_forearm':'r_arm','l_forearm':'l_arm','r_shin':'r_leg','l_shin':'l_leg'}.get(p,'body')
  m.aload(0)
  if p=='body':m.aload(1)
  else:m.aload(0).getfield(MODEL,parent,f'L{MP};')
  m.ldc_str(p).invokevirtual(MP,'getChild',f'(Ljava/lang/String;)L{MP};').putfield(MODEL,p,f'L{MP};')
 m.op('return')
 m=cw.add_method(ACC_PUBLIC,'root',f'()L{MP};');m.aload(0).getfield(MODEL,'root',f'L{MP};').op('areturn')
 m=cw.add_method(ACC_PUBLIC,'setupAnim',f'(L{ENTITY};FFFFF)V')
 m.aload(0).getfield(MODEL,'head',f'L{MP};').fload(5).fconst(.0174533).op('fmul').putfield(MP,'yRot','F')
 m.aload(0).getfield(MODEL,'head',f'L{MP};').fload(6).fconst(.0174533).op('fmul').putfield(MP,'xRot','F')
 m.aload(0).getfield(MODEL,'floating','Z').jump('ifeq','walk')
 m.aload(0).getfield(MODEL,'body',f'L{MP};').fload(4).fconst(.08).op('fmul').op('f2d').invokestatic('java/lang/Math','sin','(D)D').op('d2f').fconst(.5).op('fmul').fconst(23).op('fadd').putfield(MP,'y','F')
 m.op('return');m.label('walk')
 m.fload(2).fconst(.6662).op('fmul').op('f2d').invokestatic('java/lang/Math','cos','(D)D').op('d2f').fload(3).fconst(.8).op('fmul').op('fmul').fstore(7)
 for part,mult in [('r_leg',1),('l_leg',-1),('r_arm',-.38),('l_arm',.38),('r_forearm',-.16),('l_forearm',.16),('r_shin',-.35),('l_shin',.35)]:
  m.aload(0).getfield(MODEL,part,f'L{MP};').fload(7).fconst(mult).op('fmul').putfield(MP,'xRot','F')
 m.op('return');return cw.to_bytes()
def build_all():
 data=[json.load(open(p)) for p in sorted((ROOT/'models').glob('entity_*.json'))]
 out=dict(build_geometry(d) for d in data);out[MODEL]=build_model()
 # Preserve existing Fallen rendering by delegating their provider to its renamed original.
 import mod_v200_factions_and_bosses as old
 src=Path(old.__file__).read_text();src=src.replace('FactionRendererProvider','LegacyFactionRendererProvider')
 ns={};exec(compile(src,'legacy_factions','exec'),ns);legacy=ns['build_v200_faction_models_and_renderers']()
 provider=next(k for k in legacy if k.endswith('LegacyFactionRendererProvider'));out[provider]=legacy[provider]
 target=provider.replace('LegacyFactionRendererProvider','FactionRendererProvider')
 ERP='net/minecraft/client/renderer/entity/EntityRendererProvider';CTX=ERP+'$Context';ER='net/minecraft/client/renderer/entity/EntityRenderer';EM='net/minecraft/client/model/EntityModel'
 renderer=next(k for k in legacy if k.endswith('/V200FactionRenderer'))
 cw=ClassWriter(ACC_PUBLIC|ACC_SUPER,target,'java/lang/Object',[ERP]);cw.add_field(ACC_PRIVATE|ACC_FINAL,'kind','I')
 m=cw.add_method(ACC_PUBLIC,'<init>','(I)V');m.aload(0).invokespecial('java/lang/Object','<init>','()V').aload(0).iload(1).putfield(target,'kind','I').op('return')
 m=cw.add_method(ACC_PUBLIC,'create',f'(L{CTX};)L{ER};')
 for d in data:
  k=d['kind'];m.aload(0).getfield(target,'kind','I').iconst(k).jump('if_icmpne',f'next_{k}')
  m.new_(PKG+'/combat/CombatRenderer').dup().aload(1).invokestatic(PKG+'/model/ConvertedRig'+str(k),'create',f'()L{LD};').invokevirtual(LD,'bakeRoot',f'()L{MP};').iconst(k).ldc_str(d['name']).invokespecial(PKG+'/combat/CombatRenderer','<init>',f'(L{CTX};L{MP};ILjava/lang/String;)V').op('areturn');m.label(f'next_{k}')
 m.new_(provider).dup().aload(0).getfield(target,'kind','I').invokespecial(provider,'<init>','(I)V').aload(1).invokevirtual(provider,'create',f'(L{CTX};)L{ER};').op('areturn')
 out[target]=cw.to_bytes();return out
if __name__=='__main__':
 for name,data in build_all().items():p=ROOT/'build/classes'/(name+'.class');p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
