"""Ageless armor shapes in vanilla ModelPart pixel units. Front is negative Z.

Locations are the cube centers relative to the equipped player's vanilla joint.
Angles are degrees in vanilla X/Y/Z order. Each rotating shell has a short
descriptive name, a material swatch, and its own cube UV rectangle.
"""
from collections import namedtuple
Box=namedtuple('Box','joint name material center size turn inflate')
pieces={}
sets={
    'kabr':['battlecage_of_kabr','kabrs_wrath','light_of_the_great_prism','kabrs_forceful_greaves'],
    'willbreaker':['willbreakers_watch','willbreakers_resolve','mark_of_the_pit','willbreakers_greaves'],
    'rull':['mask_of_rull','chassis_of_rull','shadows_mark','greaves_of_rull'],
    'saint14':['helm_of_saint_14'],'armamentarium':['the_armamentarium'],'bakris':['mask_of_bakris']}
def start(i):
    global shapes
    shapes=pieces[i]=[]
def b(j,n,m,x,y,z,w,h,d,rx=0,ry=0,rz=0,inf=0):
    shapes.append(Box(j,n,m,(x,y,z),(w,h,d),(rx,ry,rz),inf))
def head(n,m,x,y,z,w,h,d,*rot,**kw):b('head',n,m,x,y,z,w,h,d,*rot,**kw)
def body(n,m,x,y,z,w,h,d,*rot,**kw):b('body',n,m,x,y,z,w,h,d,*rot,**kw)
def arm(s,n,m,x,y,z,w,h,d,*rot,**kw):b('left_arm' if s==1 else 'right_arm',n,m,x*s,y,z,w,h,d,*rot,**kw)
def leg(s,n,m,x,y,z,w,h,d,*rot,**kw):b('left_leg' if s==1 else 'right_leg',n,m,x*s,y,z,w,h,d,*rot,**kw)
def shell(material='cloth'):head('enclosed_shell',material,0,-4,0,8,8,8,inf=.34)
def liner(material='cloth'):
    body('body_liner',material,0,5.9,0,8,12,4,inf=.24)
    for s in [-1,1]:arm(s,'sleeve',material,1,4,0,4,12,4,inf=.20)
def trousers():
    for s in [-1,1]:leg(s,'thigh_liner','cloth',0,3.3,0,4,7,4,inf=.21)
    body('belt','trim',0,11.25,0,8,2,4,inf=.42)
def boots():
    for s in [-1,1]:leg(s,'greave_liner','cloth',0,8.8,0,4,6,4,inf=.24)

# Kabr. Brass casings enclose a red optic, with cyan Vex conductor forks projecting
# from one gauntlet and both greaves. The dusty blue chest keeps its angular white arc.
start(sets['kabr'][0]);shell()
head('rounded_visor','dark',0,-3.7,-3.5,6,5,2,inf=.08)
head('crown_rail','metal',0,-7.9,-.1,5,1,7,8)
head('rear_enclosure','metal',0,-4.7,3.7,7,5,2)
for s in [-1,1]:
    head('temple_casing','metal',s*3.45,-4,.0,2,7,7,0,0,s*8)
    head('cheek_cage','trim',s*2.8,-2.3,-3.8,2,4,2,0,s*-13,s*14)
    head('brow','metal',s*2.6,-6.1,-4,3,1,2,0,0,s*-19)
head('optic_housing','trim',0,-4.1,-4.55,3,2,1)
head('scarlet_optic','lens',0,-4.1,-5.1,2,1,1,inf=-.12)
head('breather','iron',0,-.7,-3.7,4,1,2)
start(sets['kabr'][1]);liner()
body('blue_thorax','plate',0,4.0,-1,8,7,4,inf=.3);body('rear_manifold','metal',0,4,2.4,7,6,2)
for s in [-1,1]:
    body('upper_collars','mark',s*2,1.35,-3.3,4,1,1,0,0,-s*28)
    body('sternum_chevron','mark',s*1.6,4.55,-3.5,4,1,1,0,0,-s*35)
    body('rivet','trim',s*3.05,6.1,-3.4,1,1,1,inf=-.12)
    body('rib_guard','metal',s*2.4,8,-2.15,3,2,2,-9,0,s*12)
    arm(s,'rounded_pauldron','plate',1.5,-.25,0,5,4,5,0,0,s*-8)
    arm(s,'pauldron_rim','metal',2,1.35,.08,5,1,5,0,0,s*-8)
    arm(s,'vambrace','metal',1.1,6.5,0,4,4,4,inf=.38)
    arm(s,'wrist_band','iron',1.05,8.8,0,4,1,4,inf=.38)
for idx in range(3):
    arm(-1,'conductor_root','metal',3.2,4.7+idx*.8,-1+idx,1,2,1,-7,-6,-20)
    arm(-1,'conductor_tine','signal',3.65,3.15+idx*.8,-1+idx,1,3,1,-7,-6,-20,inf=-.17)
body('abdomen_plate','iron',0,9.6,-2.18,4,2,1)
start(sets['kabr'][2]);trousers()
for s in [-1,1]:
    leg(s,'cuirass_skirt','metal',s*0.0,2.75,-1.9,4,5,2,-7,0,-s*8,inf=.12)
    leg(s,'engraved_thigh','plate',.2,2.0,-3.0,3,3,1,0,0,-s*8)
body('waist_clasp','mark',-.2,11.3,-2.6,3,1,1)
leg(-1,'prism_mark','plate',1.7,3.5,-2.6,3,7,1,0,-12,4)
leg(-1,'prism_stripe','mark',1.75,3.35,-3.25,1,5,1,0,-12,4,inf=-.14)
start(sets['kabr'][3]);boots()
for s in [-1,1]:
    leg(s,'knee_shell','metal',0,5.6,-2.0,4,3,2,-9,0,-s*6)
    leg(s,'blue_shin','plate',0,8.65,-2.0,4,5,2,5,0,s*4)
    leg(s,'toe_cap','iron',0,11.45,-.55,4,2,5,inf=.25)
    leg(s,'conductor_socket','metal',2.05,7.0,.55,1,3,2,0,0,-s*12)
    leg(s,'outer_conductor','signal',2.55,5.25,.52,1,3,1,0,0,-s*12,inf=-.12)

# Willbreaker. Interlocking red-stained Hive bone sweeps around a black-and-green
# cross visor. The lightly pitched shoulder thorns and rib flanges keep clear of the head.
start(sets['willbreaker'][0]);shell('iron')
head('face_shadow','dark',0,-3.9,-4.05,7,5,1)
head('green_slit','signal',0,-4.55,-4.75,6,1,1,inf=-.12)
head('split_visor','lens',0,-3.9,-4.81,1,3,1,inf=-.12)
for s in [-1,1]:
    head('bone_brow','bone',s*2.2,-6.15,-3.23,4,3,3,7,0,s*9)
    head('forehead_scar','mark',s*2.8,-6.5,-4.74,2,1,1,0,0,s*13,inf=-.1)
    head('jaw_plate','bone',s*2.7,-2.12,-3.35,3,3,3,-8,0,-s*19)
    head('temple_plate','plate',s*3.52,-4.6,.7,2,6,6,0,0,s*7)
    head('crown_spur','bone',s*3.4,-7.85,1.2,2,3,2,23,0,s*17)
head('crown_ridge','mark',0,-8.05,0,2,1,7,inf=.08)
head('chin_fang','bone',0,-.88,-3.6,3,2,2)
start(sets['willbreaker'][1]);liner()
body('rear_carapace','plate',0,3.6,2.35,8,7,2)
body('heart_chitin','plate',0,4.55,-2.05,7,7,3,inf=.12)
body('rib_core','dark',0,4.2,-3.65,3,5,1)
body('green_sternum','signal',0,4.05,-4.28,1,4,1,inf=-.16)
for s in [-1,1]:
    for i in range(3):
        body('skeletal_rib','bone' if i<2 else 'plate',s*(2.7-i*.27),2.1+i*1.9,-3.11,4,1,2,-8,0,s*-29)
    arm(s,'humeral_carapace','plate',1.7,-.25,0,5,4,5,0,0,s*-12,inf=.12)
    arm(s,'bone_rim','bone',2.7,1.2,0,3,2,5,0,0,s*-12)
    arm(s,'thick_horn','bone',3.7,-1.7,.35,2,4,2,12,0,s*30)
    arm(s,'horn_tip','signal',4.7,-3.65,.55,1,2,1,12,0,s*30,inf=-.1)
    arm(s,'small_horn','bone',2.1,-2.35,1.55,1,3,1,23,0,s*15)
    arm(s,'talon_guard','plate',1.15,6.25,-.35,4,5,4,inf=.4)
    arm(s,'bracer_spur','bone',2.5,5.55,-1.8,2,4,2,-14,0,s*16)
body('tapered_abdomen','bone',0,8.7,-2.2,4,3,1)
start(sets['willbreaker'][2]);trousers()
leg(1,'pit_mark','mark',.15,3.2,-2.9,4,7,1,-2,0,-5)
leg(1,'ragged_taper','mark',.35,6.6,-3.05,2,2,1,0,0,-6)
leg(1,'claw_sigil','bone',.15,2.4,-3.48,1,4,1,0,0,-8,inf=-.14)
for s in [-1,1]:
    leg(s,'outer_thigh_plate','plate',1.32,3.3,-.6,2,5,4,0,0,-s*7)
body('carved_buckle','bone',0,11.32,-2.55,4,2,1,0,0,-4)
start(sets['willbreaker'][3]);boots()
for s in [-1,1]:
    leg(s,'skeletal_knee','bone',.1,5.75,-2,4,3,2,-9,0,-s*8)
    leg(s,'chitin_shin','plate',0,8.6,-1.8,4,5,2,4,0,s*6)
    leg(s,'shin_keel','bone',.24,8.4,-3.0,1,5,1,6,0,s*6)
    leg(s,'bone_toe','bone',0,11.45,-.5,4,2,5,inf=.26)
    leg(s,'heel_fang','plate',1.5,9.0,1.65,2,4,2,-17,0,s*8)

# Rull. Rounded ivory and polished gold, broad shouldered and ornamental. The gilded
# diagonals cross on the mask, while the white mark hangs over an imperial purple sash.
start(sets['rull'][0]);shell('plate')
head('crown','bone',0,-7.95,-.1,7,1,7,inf=.14)
head('smooth_mask','bone',0,-4.2,-4.1,6,6,1,inf=.25)
for s in [-1,1]:
    head('casket_cheek','plate',s*3.45,-3.0,-2.2,2,5,4,0,s*8,-s*9)
    head('gold_diagonal','trim',s*.75,-4.8,-4.91,1,7,1,0,0,s*35,inf=-.12)
    head('gold_cheek_edge','trim',s*2.95,-2.3,-4.3,1,3,1,0,0,-s*16,inf=-.12)
head('mouth_panel','iron',0,-1.2,-4.55,3,1,1,inf=-.1)
start(sets['rull'][1]);liner()
body('ivory_breastplate','bone',0,4,-1.1,8,7,4,inf=.32)
body('imperial_spine','plate',0,4.25,2.35,7,6,2)
body('gorget','trim',0,.6,0,7,2,5,inf=.12)
for s in [-1,1]:
    body('raised_gold_band','trim',s*1.63,4.65,-3.38,1,6,1,0,0,s*-30)
    arm(s,'mantle_foundation','trim',1.72,.05,.05,6,4,5,0,0,-s*12)
    arm(s,'imperial_pauldron','bone',2.2,-.85,-.07,6,4,6,0,0,-s*14)
    arm(s,'outer_fin','plate',3.2,.05,.4,3,4,5,0,0,-s*25)
    arm(s,'inlaid_fin','trim',3.2,-.8,-2.73,3,1,1,0,0,-s*14,inf=-.13)
    arm(s,'ivory_guard','bone',1.06,6.4,0,4,5,4,0,0,-s*3,inf=.4)
    arm(s,'vambrace_band','trim',1.15,7.7,-.1,4,1,4,0,0,-s*3,inf=.44)
body('central_medallion','trim',0,5.6,-3.55,3,3,1,0,0,45,inf=-.25)
body('purple_seal','mark',0,5.65,-4.02,2,2,1,0,0,45,inf=-.24)
start(sets['rull'][2]);trousers()
body('sash','mark',0,10.8,0,8,3,4,0,0,-4,inf=.45)
body('gold_clasp','trim',-2.35,10.95,-2.72,2,2,1,0,0,45)
for s in [-1,1]:
    leg(s,'etched_cuisse','plate',.4,3,-1.8,4,5,2,2,0,-s*6)
    leg(s,'thigh_inlay','trim',1.4,3.25,-2.85,1,4,1,0,0,-s*7,inf=-.1)
leg(-1,'white_tabard','bone',-.2,3.8,-3.08,5,8,1,0,0,4)
leg(-1,'tabard_embroidery','trim',-.2,3.8,-3.65,1,5,1,0,0,4,inf=-.14)
start(sets['rull'][3]);boots()
for s in [-1,1]:
    leg(s,'ivory_greave','bone',.05,8.9,-1.72,4,6,2,3,0,-s*3,inf=.16)
    leg(s,'gold_knee','trim',.05,5.8,-2.17,4,2,2,-8,0,-s*7)
    leg(s,'shin_ornament','trim',.15,8.65,-2.95,1,4,1,3,0,-s*6,inf=-.11)
    leg(s,'slipper','plate',0,11.55,-.55,4,2,5,inf=.22)

# Saint's high fan faces edge-on in profile when viewed straight-on. Its fluting
# opens toward the back, with the violet, horizontal sensor line on a steel face.
start('helm_of_saint_14');shell('iron')
head('black_face','dark',0,-4.0,-3.82,7,5,2,inf=.05)
for s in [-1,1]:
    head('brow_plate','plate',s*2.1,-6,-3.55,4,3,3,7,0,-s*6)
    head('cheek_lappet','metal',s*3.6,-2.3,-1.9,2,5,5,7,0,s*7)
    head('cheek_ridge','trim',s*2.38,-2.05,-3.98,2,3,2,-9,0,s*15,inf=-.14)
head('purple_visor','signal',0,-4.7,-4.95,6,1,1,0,0,0,inf=-.18)
head('nose_bridge','plate',0,-3.65,-5.05,1,3,1,inf=-.1)
head('crest_root','trim',0,-8.18,-.1,3,1,7)
for i,(z,h,rx) in enumerate([(-2.85,2,-19),(-1.6,3,-12),(-.1,4,2),(1.4,5,10),(2.9,5,17),(4.15,4,24)]):
    head('swept_crest','metal' if i%2==0 else 'iron',0,-8.2-h/2,z,1,h,2,rx,0,0,inf=.1)
start('the_armamentarium');liner()
body('armored_carrier','plate',0,4,-.45,8,8,5,inf=.35)
body('upper_back','iron',0,4.6,2.62,8,7,2)
for s in [-1,1]:
    body('webbing','cloth2',s*2.6,2.8,-3.25,1,5,1,0,0,s*7)
    body('harness_buckle','trim',s*2.7,3.05,-3.85,2,1,1,inf=-.12)
    body('twin_grenade','metal',s*1.78,6.45,-3.87,2,3,2,0,0,s*8)
    body('grenade_fuse','trim',s*1.68,4.8,-3.83,1,1,2,0,0,s*8,inf=-.07)
    body('grenade_warhead_band','mark',s*1.78,6.06,-3.88,2,1,2,0,0,s*8,inf=.09)
    body('lower_pouch','cloth2',s*2.56,9.45,-2.6,3,3,2,inf=.06)
    arm(s,'shoulder_ballistic_plate','plate',1.3,-.25,0,5,3,5,0,0,-s*7)
    arm(s,'field_vambrace','iron',1.2,6.8,-.15,4,4,4,inf=.34)
body('center_pouch','cloth2',0,10.05,-2.85,2,2,2)
body('right_service_pouch','cloth2',4.25,6.5,.0,2,4,3)
start('mask_of_bakris');shell('cloth')
head('lower_respirator','iron',0,-2.15,-3.6,5,4,3)
head('stitched_forehead','bone',0,-6.4,-3.7,5,3,2,-9)
head('raised_skull','bone',0,-7.7,.05,6,1,6,-3)
for s in [-1,1]:
    head('torn_upper_face','plate',s*2.3,-4.65,-3.88,3,3,2,-5,s*7,-s*12)
    head('tear_cheek','bone',s*3.25,-2.8,-2.8,2,4,3,4,0,-s*17)
    head('outer_sensor_well','dark',s*2.5,-4.85,-4.86,2,2,1,0,0,-s*12,inf=-.10)
    head('upper_eye','signal',s*2.55,-4.89,-5.32,1,1,1,inf=-.1)
    head('lower_eye','signal',s*1.45,-3.5,-5.12,1,1,1,inf=-.18)
    head('jaw_filter','metal',s*2.65,-1.7,-3.91,2,2,3,12,s*9,-s*17)
    head('temple_fold','plate',s*3.75,-4.4,.72,2,5,5,-5,0,s*6)
    for j in range(2):head('temple_stitch','mark',s*4.7,-5.7+j*1.5,.2,1,1,2,0,0,s*14,inf=-.16)
head('mask_seam','mark',.35,-6.4,-4.8,1,3,1,-9,0,11,inf=-.19)
head('filter_muzzle','dark',0,-1.25,-5.05,3,2,1)

# Rear crests and backplates are deliberately authored for the over-shoulder camera.
# Reopen each piece to keep its distinguishing front panels grouped together above.
shapes=pieces['battlecage_of_kabr']
head('rear_cradle','iron',0,-4.8,4.65,6,2,1)
head('cradle_core','dark',0,-4.6,5.08,2,2,1,0,0,45,inf=-.2)
shapes=pieces['kabrs_wrath']
body('generator_frame','iron',0,4.2,3.68,5,4,1,0,0,0,inf=.08)
body('generator_iris','trim',0,4.25,4.25,3,3,1,0,0,45,inf=-.12)
body('generator_lens','signal',0,4.25,4.84,1,1,1,0,0,45,inf=-.09)
for s in [-1,1]:
    body('rear_conductor','iron',s*2.15,7,2.88,1,3,1,0,0,-s*20)
    arm(s,'elbow_plate','plate',1.05,5.1,2.15,3,3,1,0,0,-s*5)
shapes=pieces['willbreakers_watch']
head('occipital_chitin','plate',0,-4.75,4.11,6,5,1,inf=.09)
for s in [-1,1]:
    head('cranial_suture','bone',s*1.38,-4.9,4.68,1,4,1,0,0,s*18,inf=-.12)
shapes=pieces['willbreakers_resolve']
body('dorsal_groove','dark',0,4.4,3.6,3,7,1)
for i in range(3):
    body('spinal_scute','bone',0,2.25+i*1.9,4.01,3 if i<2 else 2,1,2,-13)
for s in [-1,1]:
    body('rear_scapular_blade','plate',s*2.95,3.1,3.05,2,5,2,-6,0,s*14)
    arm(s,'elbow_fang','bone',1.2,5.4,2.04,2,4,1,-14,0,s*7)
shapes=pieces['mask_of_rull']
head('gold_nape_band','trim',0,-2.35,4.42,6,1,1,0,0,0,inf=-.06)
shapes=pieces['chassis_of_rull']
for s in [-1,1]:
    body('gilded_scapula','trim',s*1.8,4.0,3.45,1,6,1,0,0,s*37,inf=-.09)
    arm(s,'shoulder_scroll','trim',2.45,-.65,2.65,3,1,1,0,0,-s*12,inf=-.05)
body('imperial_seal','trim',0,5.78,3.65,2,2,1,0,0,45,inf=-.1)
shapes=pieces['shadows_mark']
leg(1,'rear_mark','mark',-.55,3.2,2.45,4,7,1,0,0,-4)
leg(1,'rear_mark_hem','trim',-.5,5.9,3.04,3,1,1,0,0,-4,inf=-.12)
shapes=pieces['helm_of_saint_14']
head('nape_ledge','metal',0,-2.4,4.22,7,2,1)
head('neck_seam','iron',0,-1.2,4.11,5,1,1,inf=.04)
shapes=pieces['the_armamentarium']
body('rear_haversack','cloth2',0,4.75,3.25,6,6,3,inf=.12)
body('haversack_lid','plate',0,2.65,4.4,6,2,2,-8,0,0,inf=.10)
for s in [-1,1]:
    body('rear_webbing','cloth',s*1.8,5.0,4.9,1,5,1,inf=-.09)
    body('rear_latch','trim',s*1.8,5.8,5.36,1,1,1,inf=-.12)
shapes=pieces['mask_of_bakris']
head('rear_strap','mark',0,-3.35,4.22,7,1,1)
head('rear_splice','iron',1.7,-3.25,4.72,2,2,1,0,0,9,inf=-.15)
