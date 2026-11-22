"""Author the seven salvage tags and the eighteen network-synchronized gunsmith blueprints."""
import json
from pathlib import Path
HERE = Path(__file__).resolve().parent
STAGE = HERE / 'stage'
NS = 'destiny2mc'
def write(path, value):
    path.parent.mkdir(exist_ok=True, parents=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n')

# Entity lists use the actual registered names. Not every legacy Fallen has a "fallen_" prefix.
# Hive ghosts, moths, Oracles and Dread Geists are summoned support entities, not farmable parts.
FACTIONS = {
 'fallen': ('ether_induction_coil', 'Ether Induction Coil', 'Eliksni copper windings around an ether-lit armature.', '''
 fallen_dreg fallen_wretch fallen_vandal_sniper fallen_captain_rifle fallen_captain_scorch fallen_shank fallen_bruiser
 devil_walker sepiks_prime blast_servitor vanok_arclite exploder_shank tracer_shank heavy_shank devils_servitor archon_priest taniks'''),
 'vex': ('conduit_lens', 'Conduit Lens', 'A three-sided Vex housing focuses a milk-white radiolarian lens.', '''
 vex_goblin vex_hobgoblin vex_harpy vex_minotaur vex_hydra vex_wyvern persys_boss atheon_boss protheon_boss vex_sphinx templar akelous'''),
 'hive': ('runed_marrow', 'Runed Marrow', 'Hive runes burn along a fluted sliver of hollow bone.', '''
 hive_thrall hive_cursed_thrall hive_acolyte hive_knight hive_wizard hive_ogre hive_shrieker lucent_knight oryx_boss alak_hul hashladun crota golgoroth zulmak phryzhia'''),
 'cabal': ('breech_seal', 'Cabal Breech Seal', 'A vermilion pressure collar cut from a Cabal breech.', '''
 cabal_legionary cabal_phalanx cabal_psion cabal_centurion cabal_colossus cabal_warbeast cabal_incendior valus_ta_aurc bracus_zahn'''),
 'taken': ('inversion_knot', 'Inversion Knot', 'A white corona folds through the void left by a Taken combatant.', '''
 taken_thrall taken_acolyte taken_goblin taken_captain taken_knight taken_phalanx vorgeth'''),
 'scorn': ('dark_ether_bellows', 'Dark-Ether Bellows', 'Scorn brass ribs clamp a bellows swollen with dark ether.', '''
 scorn_stalker scorn_raider scorn_lurker scorn_ravager scorn_chieftain scorn_screeb scorn_abomination fikrul'''),
 'dread': ('resonant_tine', 'Resonant Tine', 'Gold pulses divide the black facets of a Dread tuning fork.', '''
 dread_grim dread_husk dread_attendant dread_weaver dread_omen dread_harbinger dread_tormentor rhulk nezarec'''),
}
# weapon, first ingredient/count, second ingredient/count, salvage faction/count, four
# curated sets of digits. Digit 1 in the magazine column selects 201, in trait 2 selects 401.
BLUEPRINTS = [
 ('wire_rifle','iron_ingot',10,'amethyst_shard',4,'fallen',4,'6153 1346 5623 6142'),
 ('vex_slap_rifle','copper_ingot',12,'quartz',6,'vex',4,'4326 6213 3251 6412'),
 ('hive_boomer','iron_ingot',12,'bone',8,'hive',4,'3621 5134 2134 4531'),
 ('cabal_slug_rifle','iron_ingot',14,'gunpowder',8,'cabal',4,'4231 5321 3152 4152'),
 ('taken_shrapnel_launcher','iron_ingot',12,'obsidian',3,'taken',4,'6423 5426 2536 3546'),
 ('scorn_crossbow','copper_ingot',10,'string',6,'scorn',4,'1563 1362 5243 6142'),
 ('subjugator_glaive','iron_ingot',12,'amethyst_shard',6,'dread',4,'3621 3412 3645 1634'),
 ('outbreak_perfected','iron_ingot',18,'redstone_block',3,'fallen',8,'4136 6132 3524 6412'),
 ('graviton_lance','obsidian',8,'ender_pearl',4,'taken',8,'3156 6135 4531 6142'),
 ('riskrunner','copper_ingot',18,'lightning_rod',2,'fallen',8,'4236 3251 3251 4126'),
 ('dead_mans_tale','iron_ingot',18,'dark_oak_log',4,'scorn',8,'6153 1432 4562 6412'),
 ('forerunner','iron_ingot',18,'quartz',12,'cabal',8,'6143 1354 5421 6142'),
 ('arbalest','iron_ingot',18,'end_rod',2,'vex',8,'1563 1342 5463 6142'),
 ('hammerhead','iron_ingot',16,'gunpowder',8,'cabal',5,'4361 2351 3521 4162'),
 ('midnight_coup','gold_ingot',8,'iron_ingot',12,'cabal',5,'6134 1342 4536 6142'),
 ('the_recluse','iron_ingot',12,'string',8,'hive',5,'4263 3256 3521 4126'),
 ('hung_jury_sr4','iron_ingot',14,'quartz',8,'dread',5,'1563 1346 4532 6142'),
 ('bxr_55_battler','copper_ingot',14,'redstone',10,'vex',5,'4163 6312 2354 6412'),
]

def build():
    lang = {}
    all_mobs = set()
    for faction, (ident, label, lore, sources) in FACTIONS.items():
        names = sources.split()
        assert not all_mobs.intersection(names)
        all_mobs.update(names)
        tag = {'replace': False, 'values': [f'{NS}:{n}' for n in names]}
        write(STAGE / f'data/{NS}/tags/entity_type/salvage/{faction}.json', tag)
        write(STAGE / f'assets/{NS}/models/item/{ident}.json',
              {'parent':'minecraft:item/generated','textures':{'layer0':f'{NS}:item/{ident}'}})
        lang['item.'+NS+'.'+ident] = label
        lang['salvage.'+NS+'.'+faction] = lore
    perks = {}
    for gun, metal, amt, fitting, units, faction, salvage, opts in BLUEPRINTS:
        component = FACTIONS[faction][0]
        costs = [{'item': 'minecraft:'+metal, 'count': amt}, {'item':'minecraft:'+fitting,'count':units},
                 {'item':NS+':'+component, 'count':salvage}]
        write(STAGE / f'data/{NS}/recipe/gunsmith/{gun}.json', {'type':NS+':gunsmith','result':NS+':'+gun,'costs':costs})
        perks[gun] = [[(i+1)*100 + int(digit) for digit in group] for i,group in enumerate(opts.split())]
        assert all(len(set(row)) == 4 for row in perks[gun])
    write(STAGE / f'assets/{NS}/perks/ageless_pools.json', perks)
    write(HERE / 'lang.json', lang)

if __name__ == '__main__':
    build()
