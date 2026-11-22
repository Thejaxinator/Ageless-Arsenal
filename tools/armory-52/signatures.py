"""Distinct load-bearing frames for the four Rising items and two exotic snipers."""
def finish(w):
 from weapons import DARK,PLATE,TRIM,METAL,LIGHT,RUBBER,EDGE,MARK
 f=w.feature
 def p(n,y,t,width,h,d,m,a=0):w.box(n,0,y,t,width,h,d,m,a)
 def side(n,x,y,t,width,h,d,m,a=0):
  for s in (-1,1):w.box(n,x*s,y,t,width,h,d,m,a)
 def remove(*names):w.parts[:]=[e for e in w.parts if e['name'] not in names]
 if f=='izanagi':
  remove('heel','comb','lower stock brace','chisel face','chisel cheek','blue armory crest','grip shoe')
  p('black stock beam',8.90,20.05,1.32,.85,6.55,DARK)
  p('steel stock link',7.42,20.08,1.03,.52,6.48,METAL,-22.5)
  p('dark butt cap',7.27,23.25,1.85,3.70,.88,DARK,-22.5)
  p('grip support',6.16,17.66,1.48,2.85,.94,DARK)
  p('dark scabbard toe',8.78,-.42,2.85,3.0,1.05,DARK)
  side('sheath nose facet',1.38,8.78,.32,.26,1.90,1.96,EDGE,-22.5)
 elif f=='taken':
  remove('heel','comb','lower stock brace','black flared stock','white muzzle tendril','deep white quill')
  p('Taken stock comb',8.97,20.45,2.35,1.28,6.42,DARK)
  p('Taken stock heel',7.69,23.12,2.67,3.15,1.62,DARK)
  p('Taken stock sweep',6.60,20.50,1.64,.96,5.73,DARK,-22.5)
  p('bone stock web',7.45,18.39,1.69,2.85,.99,PLATE,22.5)
  for s in (-1,1):
   for j,(t,y,z,ang) in enumerate(((0,8.2,3.6,-22.5),(.7,9.02,5.3,22.5),(1.8,9.77,4.9,22.5),(2.6,8.36,4.2,-22.5),(3.8,9.61,3.15,22.5))):
    x=s*(1.25+.11*(j%2));w.box('white splinter root',x,y,t,.48,.52,z,LIGHT,ang)
    w.box('white splinter tip',x,y+(.38 if ang>0 else -.38),t-.35*z,.28,.24,z*.55,LIGHT,ang)
 if not f.startswith('eclipse'):return
 # The sights and their camera axis are shared. All other visible shell, stock and lower-frame
 # pieces below follow the corresponding Rising inspection image.
 prefixes=('optic ','rear notch','notch bridge','rear dovetail','front blade','front bead','front saddle','hood','sill','rim bevel','scope ','ocular ')
 w.parts[:]=[e for e in w.parts if e['name'].startswith(prefixes)]
 ar=f=='eclipse_ar';pistol=f=='eclipse_hc';sniper=f=='eclipse_long';shot=f=='eclipse_sg'
 if pistol:
  p('internal grip frame',7.98,14.9,1.82,2.0,9.1,DARK)
  p('raked grip',5.78,19.35,1.73,4.03,1.99,RUBBER,22.5)
  side('hinged grip scale',.82,5.95,19.25,.35,2.9,1.38,PLATE,22.5)
  p('grip cartridge',5.0,19.62,1.36,2.5,1.29,METAL,22.5);p('cartridge floorplate',3.86,20.13,1.84,.46,1.82,TRIM)
  p('upper barrel',9.35,10.7,2.15,1.44,15.85,PLATE);p('ivory snout',9.13,3.10,1.66,.91,5.35,TRIM)
  side('raised ivory lip',.93,9.88,7,.37,.87,9.4,TRIM,-22.5)
  p('crown',9.24,.94,1.45,.64,1.44,METAL);p('bore',9.28,.18,.64,.4,.15,DARK)
  p('red lower channel',8.47,6.5,1.04,.36,9.8,LIGHT)
 else:
  p('short receiver',8.52,16.39,2.60,2.72,6.55,PLATE)
  p('curved inner frame',7.38,16.1,1.64,1.52,5.2,DARK,-22.5)
  p('raked grip',5.81,17.63,1.56,4.14,1.68,PLATE,22.5)
  side('dark grip inlet',.79,5.69,17.61,.24,2.23,.89,DARK,22.5)
  p('short ivory comb',9.29,20.0,1.85,1.08,5.30,PLATE)
  p('hooked shoulder plate',8.18,22.26,2.08,3.16,1.25,TRIM,-22.5)
  p('shoulder cutout return',7.43,20.93,1.20,.51,3.32,METAL,-22.5)
  if ar:
   p('open silver throat',8.94,5.59,1.89,1.61,10.56,METAL)
   p('ivory upper beak',9.94,7.57,2.28,.96,14.01,TRIM);p('ivory lower beak',7.92,5.72,1.80,.55,10.86,PLATE)
   p('middle shell',9.45,13.14,2.55,2.04,8.15,PLATE);p('red insert',7.95,15.23,1.87,.86,5.51,LIGHT)
   p('magazine',5.75,13.2,1.63,4.07,1.93,DARK);p('magazine floor',3.71,13.2,1.94,.45,2.04,METAL)
  if sniper:
   p('long silver barrel',8.72,6.96,1.36,1.34,14.58,METAL);p('muzzle ring',8.72,-.34,1.56,1.51,.70,METAL);p('bore',8.72,-.72,.88,.84,.10,DARK)
   p('ivory underrail',7.91,9.57,1.62,.75,11.15,TRIM)
   p('ruby heat channel',9.41,10.98,1.84,1.00,7.42,LIGHT)
   for j in range(4):p('sloping red dorsal fin',9.31+.22*j,7.53+j*1.64,1.64,.51+.32*j,1.82,LIGHT)
   p('tapered nose of fin',9.12,6.41,1.24,.45,1.42,LIGHT)
   side('ivory shoulder shell',1.05,8.76,14.12,.53,2.45,3.53,TRIM,22.5)
   p('power cassette',6.45,16.83,1.41,1.85,1.52,METAL,22.5)
  if shot:
   for y in (8.11,9.69):
    p('individual bore',y,6.47,1.91,1.46,13.73,METAL);p('dark bore',y,-.46,1.31,.87,.17,DARK)
    side('engraved barrel jacket',.94,y,5.97,.16,.93,12.00,METAL)
   p('lower muzzle spur',6.99,1.19,1.32,1.96,.92,METAL,22.5)
   p('front saddle bridge',10.05,2.3,1.56,.51,.98,TRIM)
   p('ivory fore-end',7.02,9.4,1.58,.81,7.20,TRIM)
  if not shot:
   p('bolt carrier',9.31,16.65,1.90,.60,2.29,METAL)
 # Each side pivot is mounted on the shell. The fine rods meet a fixed shoe at the ends.
 c=15.8 if not pistol else 15.03
 side('ivory carapace',1.04,9.07,c,.56,2.20,3.56,TRIM)
 side('hinge boss',1.37,9.14,c,.24,1.55,1.55,PLATE,45)
 side('hinge pin',1.53,9.14,c,.11,.64,.64,METAL,45)
 if not shot:
  mount=6.55 if not pistol else 9.61
  p('strut shoe',7.95,mount,1.75,.58,1.16,METAL)
  for s in (-1,1):
   # The positive X-axis rotation places the upper end rearward, at the shoe.
   height=2.42 if pistol else 2.94
   w.box('tapered sensor strut',s*.49,7.96-height*.46,mount-height*.18/(w.length/24),.15,height,.18,METAL,22.5)
 if shot:p('moving breech latch',9.67,15.1,1.08,.60,2.20,METAL)
