"""Inspection-model sculpts for eight energy weapons. Z increases from muzzle to stock."""
from PIL import ImageDraw

DARK, PLATE, TRIM, METAL, LIGHT, RUBBER, EDGE, MARK = range(8)

# Dark chassis, shell, secondary shell, metal, luminous material, grip, edge, secondary edge.
PALETTES = {
    "telesto": ("202531","eeeef0","c1cbd7","4b5265","c7a6ff","282735","cba85e","fcf4d9"),
    "merciless": ("182633","38809b","7bbfd3","384956","ffb36e","152636","cbd2d7","fff0b5"),
    "riptide": ("242833","f34226","a82522","5f6369","ff9b48","202025","deddd7","ff7541"),
    "jotunn": ("252527","37383c","886242","5b5d61","ffad4e","201e20","afb0aa","cb9461"),
    "sleeper_simulant": ("262730","36383e","a29b95","5a5457","e96042","323139","c6b6a8","d68a49"),
    "coldheart": ("232537","564965","928599","6e7982","83e3ff","30323c","d5d6dd","f4fbff"),
    "prometheus_lens": ("232935","d1d3d1","e9c645","505966","ffdfa0","343744","f3f0e6","86cede"),
    "vex_mythoclast": ("26252b","b68a4d","91683d","4b4647","ff7861","342f30","e5c77d","e9d29a"),
}
OPTIC = ("optic ", "rear notch", "rear dovetail", "front blade", "front bead", "front saddle",
         "notch bridge", "hood", "sill", "rim bevel", "scope ", "ocular ")

class Frame:
    def __init__(self, w):
        self.w = w
        self.tiles = []
        w.energy_tiles = self.tiles

    def part(self, name, y, t, width, height, depth, mat, tilt=0, x=0, axis='x', art=None):
        self.w.box(name, x, y, t, width, height, depth, mat, tilt, axis)
        if art is None:
            return
        tile = (mat, art)
        if tile not in self.tiles:
            self.tiles.append(tile)
        i = self.tiles.index(tile)
        assert i < 8
        for face in ('east','west'):
            u1,u2=(15.95,.05) if face=='east' else (.05,15.95)
            self.w.parts[-1]['faces'][face]={'texture':'#detail','uv':[u1,2*i+.05,u2,2*i+1.95]}

    def flanks(self, name, x, y,t,width,height,depth,mat, tilt=0, art=None):
        for side in (-1,1):
            self.part(name,y,t,width,height,depth,mat,tilt,x*side,art=art)

    def collar(self,name,y,t,width,height,depth,mat):
        # Two intersecting boxes form a beveled solid without stacking square rings.
        self.part(name,y,t,width,height*.66,depth,mat)
        self.part(name,y,t,width*.72,height,depth,mat)
        corner=min(height*.25,width*.24)
        for sign in (-1,1):
            for v in (-1,1):
                self.part(name,y+v*(height*.34),t,corner,corner,depth,mat,45,sign*width*.35,'z')

    def plate(self, name, x, y,t,size,mat):
        self.flanks(name,x,y,t,.23,size*.75,size,mat)
        self.flanks(name,x,y,t,.23,size,size*.69,mat)

def telesto(g):
    p=g.part; side=g.flanks
    p('gold rail spine',9.61,8.75,1.25,.62,15.4,EDGE)
    p('insulated black top rib',9.96,7.45,1.98,.86,13.45,DARK,art='telesto_rib')
    p('top ivory muzzle ferrule',9.98,1.7,2.22,1.03,3.51,PLATE)
    p('lower fork',7.48,6.65,1.67,.72,13.68,TRIM)
    p('silver fork toe',7.52,1.64,1.88,.84,3.34,METAL)
    p('mouth interior',8.65,4.85,1.24,1.83,.83,DARK)
    p('faceted ivory belly',7.86,10.10,2.47,1.43,10.39,PLATE,art='telesto_shell')
    p('rear of upper fairing',9.19,16.67,2.93,2.74,4.93,PLATE)
    side('diamond cheek',1.26,9.47,16.29,.65,1.86,3.76,TRIM,-22.5)
    p('central lower fairing',6.48,13.30,2.30,2.43,4.42,TRIM,-22.5)
    p('power cassette',4.94,13.08,1.89,2.9,1.63,TRIM,22.5,art='purple_diag')
    p('rear grip',5.63,18.89,1.50,4.59,1.53,RUBBER,-22.5)
    p('guard bottom',3.37,15.94,1.72,.73,6.23,DARK)
    p('guard toe',3.89,13.09,1.82,1.07,1.35,DARK,-22.5)
    p('short ivory butt root',9.22,21.00,2.04,1.47,4.35,PLATE)
    p('sculpted white counterweight',7.77,22.12,2.62,3.41,2.85,PLATE,-22.5)
    p('gold rail clasp',9.85,14.39,2.34,1.08,1.22,EDGE)
    g.plate('gold breech cap',1.44,8.11,17.2,1.53,EDGE)
    side('charge cell window',1.21,8.51,11.41,.23,.48,3.77,LIGHT)
    p('charge chamber',8.63,8.15,1.22,.62,7.44,METAL)
    # The tiny, regularly spaced gold rail electrodes read even from behind the sight.
    for t in (4.1,6.0,7.9,9.8,11.7,13.3):
        p('ion contact',10.44,t,.70,.16,.34,EDGE)

def merciless(g):
    p=g.part;side=g.flanks
    p('blue upper bridge',9.98,8.65,2.41,1.28,16.82,TRIM,art='blue_bridge')
    p('vented front cap',9.90,.74,2.59,1.44,1.39,PLATE,art='grille')
    p('bottom chassis rail',6.91,8.65,2.13,.82,16.9,PLATE)
    p('rear breech block',8.36,15.79,2.62,3.51,4.59,PLATE)
    p('rear tank cradle',8.50,20.49,1.75,.83,7.05,PLATE)
    p('butt end',6.85,23.27,1.81,3.55,1.08,DARK)
    p('open stock return',7.48,21.91,1.20,.67,3.57,PLATE,45)
    p('raked grip',5.84,17.68,1.35,3.76,1.27,RUBBER,-22.5)
    p('lower guard',4.33,16.35,1.24,.46,3.63,PLATE)
    p('magazine well',5.64,13.11,2.06,3.49,3.27,PLATE)
    p('magazine',3.50,13.14,1.73,1.94,2.70,DARK)
    p('spindle',8.44,7.69,.85,.91,12.38,DARK)
    for t in (3.1,5.6,8.1):
        g.collar('exposed hot cell',8.40,t,1.72,1.42,2.09,LIGHT)
        g.collar('charge chamber',8.40,t+1.13,1.84,1.52,.32,METAL)
    g.collar('black flux drum',8.40,12.09,2.08,1.61,2.61,DARK)
    for t in (11.5,12.0,12.5):g.collar('copper brush',8.40,t,2.15,1.67,.14,EDGE)
    for t in (12.48,14.14,15.8):
        p('selector block',10.79,t,1.06,.32,.95,EDGE)
        p('selector inset',10.97,t,.48,.09,.45,METAL)

def riptide(g):
    p=g.part;side=g.flanks
    p('black block receiver',8.83,9.60,2.94,2.79,16.40,DARK)
    p('recessed muzzle face',8.84,1.04,2.92,2.82,.66,RUBBER,art='grille')
    p('red canted rail top',10.28,8.43,2.49,.61,15.45,PLATE)
    side('scarlet casing',1.42,9.31,6.66,.44,1.78,9.85,PLATE,art='crucible')
    side('charge cell window',1.70,9.16,6.45,.28,.51,2.93,METAL)
    p('red lower emitter',7.00,7.29,1.91,.75,12.44,PLATE,art='grille')
    g.collar('emitter point',7.03,.88,1.67,1.36,.97,METAL)
    p('underbody elbow',6.27,12.23,2.16,1.98,2.89,PLATE,22.5)
    p('transfer handle root',5.32,13.13,1.73,1.82,1.43,DARK)
    p('rear power connector',9.41,19.56,1.81,1.51,4.30,METAL)
    p('red rear sleeve',9.23,18.17,2.65,1.91,2.57,PLATE)
    side('side power pipe',1.59,8.51,15.17,.26,.35,5.93,METAL,-22.5)
    p('stock top',9.43,22.02,2.03,1.28,3.03,DARK)
    p('stock heel',7.48,22.88,2.08,3.84,1.32,DARK,22.5)
    p('stock lower brace',5.91,20.68,1.28,.67,4.89,METAL,-22.5)
    p('rubber pistol grip',6.3,18.08,1.47,3.74,1.55,RUBBER,-22.5)
    g.collar('magazine',3.99,15.36,2.64,1.93,7.79,PLATE)
    g.collar('magazine floor',4.00,11.42,2.66,1.97,.56,TRIM)
    p('magazine',4.72,16.41,1.73,.54,3.59,METAL)
    p('bottom pickup',4.87,18.03,1.04,1.44,1.08,DARK)
    p('hot chamber',8.48,14.88,2.62,1.24,2.57,METAL)

def jotunn(g):
    p=g.part;side=g.flanks
    p('carved anvil spine',9.79,10.72,5.14,1.18,18.10,PLATE,art='anvil')
    p('overhung dorsal plate',10.32,12.18,4.99,.49,19.51,DARK)
    p('blunt square crown',8.61,2.30,5.42,3.98,3.50,DARK)
    p('face rim',8.56,.54,4.64,3.27,.32,TRIM)
    p('square furnace door',8.56,.29,3.86,2.78,.37,DARK)
    p('forge core',8.56,.05,1.39,2.14,.17,LIGHT,art='anvil')
    p('forearm channel',7.74,11.86,4.22,2.33,15.65,DARK)
    # Copper conductor wraps around the emblem; a deep rear cutout accepts the player's arm.
    side('copper upper elbow',2.41,9.55,6.19,.51,1.05,3.10,TRIM,-22.5)
    side('copper lower elbow',2.41,7.38,6.32,.51,1.05,3.06,TRIM,22.5)
    side('front of conductor',2.43,8.47,4.99,.49,1.66,1.0,TRIM)
    for y in (8.89,9.19,9.49):
        side('exposed hot cell',2.45,y,10.49,.31,.18,6.77,MARK)
    side('hammer plate',2.45,8.00,11.23,.48,2.64,6.12,PLATE,art='black_badge')
    p('hand stop',6.49,6.63,3.31,1.48,3.48,RUBBER,22.5)
    p('cuff bridge',9.02,20.35,4.05,.68,4.40,METAL)
    side('cuff side',2.02,7.74,20.75,.71,2.99,2.62,DARK,22.5)
    p('power cassette',6.00,12.83,2.85,1.64,3.43,METAL)
    side('iron fastener',2.71,8.97,8.79,.15,.37,.43,MARK)
    side('iron fastener',2.71,7.25,13.42,.15,.37,.43,MARK)

def sleeper(g):
    p=g.part;side=g.flanks
    p('linear emitter',8.74,8.45,1.64,.76,16.66,DARK)
    # Each floating diamond is bracketed above and below. The alternating light and dark diagonal faces are the D1 rail.
    for t in (1.98,5.06,8.14,11.22,14.30):
        p('coil jacket',8.74,t,2.96,2.25,2.25,TRIM,45)
        side('inset diamond',1.54,8.74,t,.34,1.65,1.65,DARK,45)
        side('warmind coil',1.74,8.74,t,.16,.83,.83,PLATE,45)
        p('platinum upper fin',10.40,t,2.81,.49,2.67,EDGE,art='warmind')
        p('lower black fin',7.08,t,2.63,.44,2.78,RUBBER,art='warmind')
    side('splitter point',.63,8.65,.03,.35,.56,2.15,LIGHT)
    p('accelerator saddle',8.22,16.75,2.75,2.24,4.28,PLATE,art='warmind_mark')
    p('long front battery well',6.06,15.56,2.37,3.00,2.52,PLATE)
    p('power cassette',3.97,15.54,2.21,1.89,2.28,DARK)
    p('receiver neck',8.78,19.52,1.50,.95,4.39,METAL)
    p('boxed cheek rest',9.09,22.22,2.04,1.77,3.70,PLATE,art='warmind')
    p('rear stock heel',8.27,23.41,1.92,2.72,.89,PLATE)
    p('lower stock strut',6.80,21.07,1.08,.53,4.98,MARK,-22.5)
    p('grip',6.83,18.78,1.40,2.92,1.05,DARK,22.5)

def coldheart(g):
    p=g.part;side=g.flanks
    for y,sg in ((6.68,-1),(10.08,1)):
        p('forked shroud',y,5.33,2.41,.95,10.53,PLATE,art='cryo_slots')
        p('beveled front cap',y+sg*.21,.65,2.57,.85,1.02,METAL,-22.5*sg)
        side('mesh return',1.22,y-sg*.39,5.83,.16,.46,6.78,TRIM,art='mesh')
        p('taper to cold chamber',y+sg*.06,11.23,2.46,.88,2.84,PLATE,-22.5*sg)
    g.collar('cryogenic chamber',8.46,12.06,3.34,2.69,3.09,EDGE)
    g.collar('charge chamber',8.46,9.86,2.76,2.21,1.04,METAL)
    g.collar('emitter point',8.46,8.86,1.43,1.24,1.07,TRIM)
    g.collar('central focusing crystal',8.46,10.21,2.87,2.31,.38,LIGHT)
    g.plate('reservoir port',1.65,8.47,12.03,1.23,METAL)
    for s in (-1,1):
        side('refrigerant lines',1.21,8.45+s*.75,15.08,.39,.36,3.73,LIGHT,22.5*s)
        side('line sleeve',1.22,8.45+s*1.27,16.89,.58,.56,.82,MARK)
    p('violet tail',8.45,19.56,2.74,2.42,7.77,PLATE,art='cryo_slots')
    p('rear insulator cap',8.54,23.40,2.88,2.64,1.19,TRIM)
    p('feed grip',6.77,15.60,2.03,2.37,1.97,PLATE)
    p('magazine',4.70,15.60,1.81,2.49,1.77,DARK)
    p('grip',6.12,20.39,1.12,3.24,.86,DARK,-22.5)
    p('stock diagonal',5.97,21.9,.98,.60,3.61,METAL,-22.5)

def prometheus(g):
    p=g.part;side=g.flanks
    # Air runs completely through the slots rather than a dark painted indentation.
    for y in (6.58,10.01):
        for yy in (-.45,.45):p('silver aperture rail',y+yy,5.88,2.11,.32,10.93,PLATE)
        p('silver nose crossbar',y,.43,2.13,1.22,.69,TRIM if y<7 else PLATE,-22.5)
        p('rear of aperture',y,11.18,2.23,1.19,1.18,PLATE)
    p('silver bridge',9.95,14.34,2.84,1.68,6.80,PLATE,art='prism_shell')
    p('gold keel',7.01,14.62,2.11,1.83,6.89,TRIM)
    p('dark central prism seat',8.50,12.47,1.84,1.53,3.20,METAL)
    g.plate('focusing collar',1.27,8.47,12.16,2.92,EDGE)
    side('central focusing crystal',1.44,8.49,12.16,.32,1.59,1.59,LIGHT,45)
    side('cut prism facet',1.61,8.50,12.15,.13,.81,.81,MARK,45)
    p('gold rear chassis',8.36,20.17,1.72,.83,7.61,TRIM)
    p('silver display rail',9.21,20.35,1.94,.88,7.20,PLATE)
    side('telemetry window',1.02,9.09,22.13,.20,.72,2.94,MARK,art='digits')
    p('heel folded down',7.58,23.48,1.60,2.51,.91,TRIM)
    p('gold butt return',6.12,21.99,1.27,.79,3.58,TRIM,-22.5)
    p('grip',6.47,19.47,1.26,3.02,1.11,DARK,-22.5)
    p('magazine',4.92,15.15,1.95,3.16,2.02,METAL)
    p('magazine floor',3.40,15.16,2.03,.33,2.17,DARK)
    p('front stock pin',6.40,16.78,.86,1.50,1.17,TRIM,-22.5)

def mythoclast(g):
    p=g.part;side=g.flanks
    g.collar('long stripped muzzle',8.58,3.92,1.27,1.20,9.66,METAL)
    for t in (-.75,3.55,6.07,7.55):g.collar('conduit joint',8.58,t,1.48,1.40,.39,DARK)
    p('interrupted gold fore-end',8.58,8.95,1.76,1.50,3.49,PLATE,art='vex')
    p('underbarrel runner',7.61,11.39,1.27,.60,8.37,METAL)
    p('bronze lower jaw',6.72,12.76,1.91,.83,5.91,PLATE,22.5)
    p('bronze cheek beak',9.62,16.21,2.22,.96,4.72,PLATE,-22.5)
    p('high back',10.20,19.31,2.67,1.39,4.63,PLATE,art='vex')
    p('gold tail sweep',9.77,22.19,2.44,1.13,3.97,PLATE,22.5)
    p('rounded rear edge',9.21,23.63,1.78,1.10,1.12,TRIM)
    p('underhung rear frame',8.41,19.63,1.97,1.32,6.98,DARK,art='mesh')
    side('rounded side plate',1.10,9.48,18.63,.51,1.38,3.12,TRIM)
    g.plate('lateral Vex joint',1.45,9.48,17.67,.77,METAL)
    p('raked handgrip',6.54,19.52,1.46,3.58,1.52,RUBBER,-22.5,art='mesh')
    p('bronze grip heel',5.01,19.63,1.87,.82,3.02,PLATE)
    p('lower jaw pivot',7.75,14.72,1.81,2.02,1.42,DARK,-22.5)
    p('power cassette',8.24,20.94,1.66,1.51,2.74,DARK)
    side('bolt carrier',1.15,9.49,15.06,.23,.34,1.56,LIGHT)
    p('optic pedestal',10.62,16.30,1.71,1.16,1.29,PLATE)

SCULPTS={"telesto":telesto,"merciless":merciless,"riptide":riptide,"jotunn":jotunn,
         "sleeper_simulant":sleeper,"coldheart":coldheart,"prometheus_lens":prometheus,
         "vex_mythoclast":mythoclast}

def finish(w):
    """Replace the complete frame while retaining the tested clear optical aperture."""
    if w.id not in SCULPTS:return
    w.colors=[tuple(bytes.fromhex(v)) for v in PALETTES[w.id]]
    w.parts[:]=[e for e in w.parts if e['name'].startswith(OPTIC)]
    if w.id in ('merciless','telesto','prometheus_lens'):
        # Retain split posts for the inspection models with low-profile open sights.
        w.parts[:]=[e for e in w.parts if e['name'] not in ('hood','rim bevel')]
    frame=Frame(w)
    SCULPTS[w.id](frame)
    if w.id in ('vex_mythoclast','riptide','coldheart','merciless'):
        w.parts[:]=[e for e in w.parts if e['name'] not in ('optic wall','hood','rim bevel')]
        y=w.axis
        if w.id=='vex_mythoclast':
            # A rounded gold capsule surrounds the actual sight tunnel. Its central ray stays empty.
            frame.flanks('gold eyepiece flank',1.46,y,15.47,.55,1.56,3.40,PLATE)
            frame.flanks('ocular nose bevel',1.39,y+.04,13.77,.45,1.12,.91,TRIM,-22.5)
            frame.part('gold ocular crown',y+.98,15.45,2.92,.44,3.26,EDGE)
            frame.part('gold ocular sill',y-.94,15.25,2.91,.32,3.25,TRIM)
            frame.part('rear glass lip',y+.95,17.05,2.72,.27,.43,METAL)
        elif w.id=='riptide':
            frame.flanks('rear cheek of reflex hood',1.57,y-.05,16.87,.47,1.75,1.07,DARK)
            frame.flanks('sloped front of hood',1.57,y-.05,15.42,.42,1.81,.71,PLATE,45)
            frame.part('scarlet sight cap',y+.92,16.49,3.48,.43,1.65,PLATE)
        else:
            frame.flanks('split rear guide',1.48,y-.58,16.3,.40,.99,.82,METAL if w.id=='coldheart' else PLATE,-22.5)
            frame.flanks('fiber optic tip',1.46,y-.13,16.39,.27,.16,.50,LIGHT if w.id=='coldheart' else EDGE)

def paint(w, image):
    """Paint the visible side panels in the registered secondary texture."""
    if w.id not in SCULPTS:return image
    d=ImageDraw.Draw(image)
    for i,(mat,panel) in enumerate(w.energy_tiles):
        y=16*i
        base=w.colors[mat]
        mix=lambda c:tuple(max(0,min(255,v+c)) for v in base)
        dark=mix(-44);highlight=mix(32)
        def line(points,c,width=1):
            d.line([(x,y+v) for x,v in points],fill=c,width=width)
        def rect(x1,y1,x2,y2,c):
            d.rectangle((x1,y+y1,x2,y+y2),fill=c)
        d.rectangle((0,y,127,y+15),fill=base)
        line([(0,1),(91,1),(96,3),(127,3)],highlight)
        line([(0,14),(55,14),(61,12),(125,12)],dark)
        if panel in ('grille','mesh','warmind','cryo_slots'):
            space=6 if panel=='mesh' else 11
            for x in range(4,120,space):
                line([(x,11),(x+5,4)],dark,2)
            if panel=='cryo_slots':
                for x in (34,57,80):rect(x,4,x+14,8,w.colors[LIGHT]);rect(x+1,4,x+12,4,w.colors[MARK])
            if panel=='warmind':line([(8,12),(32,3),(86,3),(115,11)],w.colors[EDGE],2)
        elif panel in ('telesto_rib','telesto_shell','purple_diag'):
            if panel=='telesto_rib':
                for x in range(3,125,9):rect(x,3,x+3,5,w.colors[EDGE])
            elif panel=='telesto_shell':
                line([(4,12),(19,4),(63,4),(74,9),(110,9)],w.colors[TRIM],2)
                line([(15,12),(26,5),(35,5),(28,12)],w.colors[METAL],2)
            else:
                for x in (17,24,31):line([(x,14),(x+20,2)],w.colors[EDGE],2)
                line([(56,12),(96,3)],w.colors[LIGHT],2)
        elif panel in ('crucible','blue_bridge','prism_shell'):
            line([(14,13),(25,4),(89,4),(99,10),(118,10)],dark)
            if panel=='crucible':
                line([(76,5),(82,12),(88,5),(94,11),(101,3)],w.colors[EDGE],2)
                for x in (10,17,24):line([(x,10),(x+4,6)],w.colors[TRIM],2)
            elif panel=='blue_bridge':
                for x in (7,13,19):rect(x,5,x+3,12,w.colors[METAL])
                rect(99,2,105,5,w.colors[MARK])
        elif panel in ('vex','warmind_mark','black_badge','anvil'):
            if panel=='black_badge':
                d.ellipse((42,y+1,77,y+14),outline=w.colors[EDGE],width=2)
                d.polygon([(58,y+2),(65,y+7),(61,y+12),(54,y+9)],fill=w.colors[TRIM])
                for x in (4,118):rect(x,3,x+3,5,w.colors[MARK]);rect(x,10,x+3,12,w.colors[MARK])
            elif panel=='warmind_mark':
                line([(39,4),(59,12),(79,4),(39,4)],w.colors[EDGE],2)
                line([(48,3),(61,8),(70,3)],w.colors[EDGE],2)
            elif panel=='vex':
                line([(5,10),(32,10),(43,6),(59,6),(64,4),(110,4)],w.colors[TRIM],2)
                rect(71,5,75,7,w.colors[DARK])
            else:
                for x in (12,21,30,39):line([(x,3),(x+4,11)],w.colors[METAL],2)
        elif panel=='digits':
            rect(0,0,127,15,(33,71,89))
            for x in (12,39,66,93):
                d.rounded_rectangle((x,y+2,x+16,y+13),radius=3,outline=w.colors[MARK],width=2)
                line([(x+3,11),(x+13,4)],w.colors[MARK])
    return image
