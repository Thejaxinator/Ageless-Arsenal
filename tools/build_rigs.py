"""Compile articulated palette-cuboid rigs against the existing 1.21.1 renderer ABI."""
from pathlib import Path
import sys,json,math

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
