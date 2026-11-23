package com.jax.destiny2mc.binder;

import com.jax.destiny2mc.tenno.abilities.WarframeAbilityId;
import com.jax.destiny2mc.tenno.abilities.WarframeActiveAbilities;
import net.minecraft.server.level.ServerPlayer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Catalog keys are save-format identifiers. Names, prices, and descriptions may change independently.
 * Key slots only refer to an ID. They never imply a particular category, class or element. */
public final class ActiveAbilities {
    private ActiveAbilities() {}
    enum Action {
        SOLAR_GRENADE("Solar Grenade","solar","grenade",10,3,18,"A burning sun pulses four times over three seconds."),
        HEALING_GRENADE("Healing Grenade","solar","grenade",8,2,17,"Throw a restorative burst that heals nearby Guardians and grants regeneration."),
        PULSE_GRENADE("Pulse Grenade","arc","grenade",9,3,17,"An Arc field pulses four times. Each pulse chains to a nearby target."),
        FLASHBANG("Flashbang Grenade","arc","grenade",6,2,13,"An Arc flash blinds and disorients enemies within five blocks."),
        VORTEX_GRENADE("Vortex Grenade","void","grenade",12,3,20,"A dark well pulls enemies inward with five damaging pulses."),
        SUPPRESSOR_GRENADE("Suppressor Grenade","void","grenade",8,3,16,"Silence enemy damage for two seconds, then leave the targets weakened."),
        DUSKFIELD_GRENADE("Duskfield Grenade","stasis","grenade",10,3,19,"An expanding cold field slows and progressively freezes enemies."),
        COLDSNAP_GRENADE("Coldsnap Grenade","stasis","grenade",9,3,17,"Seeking ice snaps from one nearby enemy to the next, slowing up to three."),
        SHACKLE_GRENADE("Shackle Grenade","strand","grenade",10,3,20,"Suspend enemies in a small radius on impact."),
        THREADLING_GRENADE("Threadling Grenade","strand","grenade",12,4,20,"Three homing hatchlings pursue separate visible targets when available."),
        FREEZING_SINGULARITY("Freezing Singularity","prismatic","grenade",16,5,23,"A Void well reels enemies inward before its Stasis core bursts."),
        HAILFIRE_SPIKE("Hailfire Spike","prismatic","grenade",16,5,23,"An impact spike freezes nearby enemies, then ignites a Solar aftershock."),
        INCINERATOR_SNAP("Incinerator Snap","solar","melee",7,2,9,"Scorch a spreading cone of enemies in front of you."),
        THROWING_HAMMER("Throwing Hammer","solar","melee",10,3,12,"Throw a flaming hammer. Catch its returning light after impact to heal."),
        CHAIN_LIGHTNING("Chain Lightning","arc","melee",8,2,10,"Strike at close range and arc through two additional enemies."),
        THUNDERCLAP("Thunderclap","arc","melee",10,3,13,"A short windup releases a directional shock wave with heavy knockback."),
        SHIELD_THROW("Shield Throw","void","melee",10,3,14,"Throw a bouncing shield. Each connected hit grants a short absorption shield."),
        POCKET_SINGULARITY("Pocket Singularity","void","melee",8,2,11,"A quick Void bolt repels its target and nearby enemies."),
        SHIVER_STRIKE("Shiver Strike","stasis","melee",9,3,12,"Lunge forward and shatter chilled targets caught on your path."),
        PENUMBRAL_BLAST("Penumbral Blast","stasis","melee",10,3,13,"Fire a freezing staff projectile that locks a small group in place."),
        THREADED_SPIKE("Threaded Spike","strand","melee",9,3,12,"A ricocheting dart weakens up to four targets before tracing back."),
        ARCANE_NEEDLE("Arcane Needle","strand","melee",8,2,10,"A homing needle unravels its victim with two delayed bursts."),
        PRISM_CLEAVE("Prism Cleave","prismatic","melee",14,4,15,"Slice a cone with Stasis, then detonate a Solar fracture along the same arc."),
        STORM_SNARE("Storm Snare","prismatic","melee",14,4,15,"Tether the first target with Strand, then chain Arc to nearby enemies."),
        MARKSMANS_DODGE("Marksman's Dodge","arc","class",9,3,14,"Dodge in your movement direction and reload your held magazine weapon from reserves."),
        GAMBLERS_DODGE("Gambler's Dodge","void","class",10,3,16,"Dodge near an enemy to recover an equipped melee ability."),
        HEALING_RIFT("Healing Rift","solar","class",12,4,26,"Create a healing circle for nearby Guardians for eight seconds."),
        EMPOWERING_RIFT("Empowering Rift","arc","class",12,4,26,"Stand in the circle to deal 25% more direct or projectile damage for eight seconds."),
        TOWERING_BARRICADE("Towering Barricade","void","class",13,4,28,"Project a tall directional wall that intercepts incoming attacks for eight seconds."),
        RALLY_BARRICADE("Rally Barricade","solar","class",11,3,24,"Project a low wall for seven seconds. Crouch behind it for cover and faster reloads.");

        final BinderAbility definition;
        Action(String name,String element,String category,int motes,int levels,int seconds,String summary) {
            definition=new BinderAbility(name().toLowerCase(Locale.ROOT),name,element,category,summary,motes,levels,seconds*20);
        }
        String element(){return definition.element();}
    }

    private static final List<BinderAbility> CATALOG=Arrays.stream(Action.values()).map(a->a.definition).toList();
    public static List<BinderAbility> catalog(){return CATALOG;}

    /** Called only after player state validates the bind and cooldown. All origins and directions
     * come from the server player, never arbitrary client coordinates. */
    public static boolean cast(ServerPlayer owner,String id){
        if(id==null || !owner.isAlive() || owner.isSpectator()) return false;
        WarframeAbilityId warframeAbility = WarframeAbilityId.byKey(id);
        if (warframeAbility != null) {
            return WarframeActiveAbilities.castFromBinder(owner, warframeAbility);
        }
        Action action;
        try { action=Action.valueOf(id.toUpperCase(Locale.ROOT)); }
        catch(IllegalArgumentException ignored){return false;}
        return AbilityCombat.cast(owner,action);
    }
}
