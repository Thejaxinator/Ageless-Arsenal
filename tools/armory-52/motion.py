"""Split the static assembled item into named rigid pieces for continuous first-person motion."""
import json
from pathlib import Path
def groups(w):
 k=w.kind;f=w.feature
 if k=='bow': action='DRAW';reload='NOCK'
 elif k=='rotary':action='SPIN';reload='CELL'
 elif k=='revolver':action='HAMMER';reload='CYLINDER'
 elif k in ('slab','claw'):action='SLIDE';reload='CELL'
 elif k in ('twin','breach'):action='LATCH';reload='BREAK'
 elif k=='lever':action='LEVER';reload='GATE'
 elif k in ('pump','cluster'):action='PUMP';reload='GATE'
 elif k in ('sniper','sheath','worm','rod'):action='BOLT';reload='CELL'
 elif k in ('fusion','trace','rail','grav','gauntlet'):action='CHARGE';reload='CELL'
 elif k in ('launcher','dual'):action='LATCH';reload='PORT'
 else:action='BOLT';reload='CELL'
 rnames={'CELL':{'magazine','magazine floor','long box magazine','power cassette','grip cartridge','cartridge floorplate','drum','drum axle'},
   'CYLINDER':{'cylinder body','cylinder flats','flutes','cylinder bevel','spade suit','spade point'},
   'BREAK':{'barrel','muzzle shadow','muzzle ferrule','individual bore','dark bore','root','elemental root','lower ember tube','engraved barrel jacket','muzzle','fore-end','break-action chamber','paired barrel bridge','lower muzzle spur','front saddle bridge','ivory fore-end','cast band','front blade','front bead','front saddle','reinforcing band'},
   'GATE':{'loading gate'}, 'PORT':{'rear loading collar'},'NOCK':{'shaft','broadhead'}}
 anames={'SPIN':{'individual bore','dark bore','reinforcing band','cast band'},
   'SLIDE':{'upper barrel','raised ivory lip','ivory snout','slide panels','crown','bore','rear shoulder','golden upper vane','hot upper blade','ragged muzzle tooth'},
   'BOLT':{'bolt carrier','bolt knob','moving breech latch'},
   'LATCH':{'moving breech latch'},'CHARGE':{'charge chamber','charge cell window','exposed hot cell','emitter point','central focusing crystal','hot chamber','forge core','cryogenic chamber','refrigerant lines','splitter point','glass blight vessel'},
   'HAMMER':{'hammer'},'LEVER':{'lever loop heel','lever return','guard','finger bow'},'PUMP':{'fore-end','bronze slide','dangling foregrip','pump sleeve'},'DRAW':{'upper string'}}
 if reload=='CELL' and not any(e['name'] in rnames[reload] for e in w.parts):w.box('power cassette',0,6.68,13.8,2.28,1.58,3.42,2)
 if reload=='GATE':w.box('loading gate',1.4,7.6,13.5,.15,.88,3.1,3)
 if not any(e['name'] in anames[action] for e in w.parts):
  n={'PUMP':'pump sleeve','BOLT':'bolt carrier','LATCH':'moving breech latch','CHARGE':'charge chamber'}[action];w.box(n,1.26 if action!='PUMP' else 0,8.8 if action!='PUMP' else 7.12,16 if action!='PUMP' else 7.6,.65 if action!='PUMP' else 2.93,.72 if action!='PUMP' else 1.43,2.7 if action!='PUMP' else 4.45,3)
 parts={'body':[],'action':[],'load':[]}
 if k=='bow':parts['lower_string']=[]
 for e in w.parts:
  n=e['name'];target='load' if n in rnames[reload] else 'action' if n in anames[action] else 'body'
  parts['lower_string' if n=='lower string' else target].append(e)
 assert all(parts.values()),(w.id,action,reload)
 return action,reload,parts
def bake(w,model,parts,assets):
 dest=assets/'models/armory'/w.id;dest.mkdir(parents=True,exist_ok=True)
 for key,els in parts.items():
  (dest/(key+'.json')).write_text(json.dumps({'textures':model['textures'],'elements':els,'ambientocclusion':False},separators=(',',':')))
