/*
 * Decompiled with CFR 0.152.
 */
package com.jax.destiny2mc.weapon;

public class WeaponSpec {
    public final int index;
    public final String id;
    public final String displayName;
    public final String frameLabel;
    public final int archetype;
    public final int element;
    public final boolean isExotic;
    public final boolean usesSpecialAmmo;
    public final int rpm;
    public final float fireDelayTicks;
    public final boolean fullAuto;
    public final int burstCount;
    public final int magSize;
    public final int reloadTicks;
    public final float baseDamage;
    public final float critMult;
    public final int pellets;
    public final float hipSpread;
    public final float adsSpread;
    public final float range;
    public final float fovZoom;
    public final float recoilPitch;
    public final float recoilYaw;
    public final int stability;
    public final int aimAssist;
    public final int recoilDirection;
    public final int perkId;
    public final String perkTitle;
    public final String perkDesc;
    public static final WeaponSpec[] ALL = new WeaponSpec[52 + com.jax.destiny2mc.ageless.PackagedWeapons.count()];

    public WeaponSpec(int n, String string, String string2, String string3, int n2, int n3, boolean bl, boolean bl2, int n4, float f, boolean bl3, int n5, int n6, int n7, float f2, float f3, int n8, float f4, float f5, float f6, float f7, float f8, float f9, int n9, int n10, int n11, int n12, String string4, String string5) {
        this.index = n;
        this.id = string;
        this.displayName = string2;
        this.frameLabel = string3;
        this.archetype = n2;
        this.element = n3;
        this.isExotic = bl;
        this.usesSpecialAmmo = bl2;
        this.rpm = n4;
        this.fireDelayTicks = f;
        this.fullAuto = bl3;
        this.burstCount = n5;
        this.magSize = n6;
        this.reloadTicks = n7;
        this.baseDamage = f2;
        this.critMult = f3;
        this.pellets = n8;
        this.hipSpread = f4;
        this.adsSpread = f5;
        this.range = f6;
        this.fovZoom = f7;
        this.recoilPitch = f8;
        this.recoilYaw = f9;
        this.stability = n9;
        this.aimAssist = n10;
        this.recoilDirection = n11;
        this.perkId = n12;
        this.perkTitle = string4;
        this.perkDesc = string5;
    }

    static {
        WeaponSpec[] weaponSpecArray = ALL;
        weaponSpecArray[0] = new WeaponSpec(0, "khvostov_7g_0x", "Khvostov 7G-0X", "Exotic Kinetic Auto Rifle", 0, 0, true, false, 600, 2.0f, true, 1, 43, 34, 3.8f, 1.5f, 1, 1.45f, 0.14f, 64.0f, 0.8f, 1.45f, 0.42f, 78, 82, 95, 1, "The Right Choice", "Every 7th bullet deals 1.8x damage and ricochets to nearby foes.");
        weaponSpecArray[1] = new WeaponSpec(1, "ammit_ar2", "Ammit AR2", "Omolon Solar Auto Rifle", 0, 1, false, false, 450, 2.6666667f, true, 1, 32, 36, 4.6f, 1.55f, 1, 1.35f, 0.12f, 68.0f, 0.78f, 1.75f, 0.4f, 72, 70, 85, 2, "Incandescent", "Defeating a target spreads scorching Solar detonation to nearby enemies.");
        weaponSpecArray[2] = new WeaponSpec(2, "sweet_business", "Sweet Business", "Exotic Kinetic Gatling Auto Rifle", 0, 0, true, false, 360, 3.3333333f, true, 1, 150, 56, 3.3f, 1.45f, 1, 1.95f, 0.26f, 58.0f, 0.82f, 1.25f, 0.48f, 65, 62, 100, 9, "Payday & Business Time", "150-round rotary Gatling gun. Holding fire spins up from 360 to 900 RPM.");
        weaponSpecArray[3] = new WeaponSpec(3, "monte_carlo", "Monte Carlo", "Exotic Kinetic Bayonet Auto Rifle", 0, 0, true, false, 600, 2.0f, true, 1, 43, 32, 3.9f, 1.5f, 1, 1.3f, 0.13f, 62.0f, 0.8f, 1.4f, 0.38f, 76, 75, 80, 10, "Markov Chain", "Kills grant stacking damage (up to x5). Melee hits grant x5 and refill the magazine.");
        weaponSpecArray[4] = new WeaponSpec(4, "cerberus_plus_1", "Cerberus+1", "Exotic Four-Barrel Auto Rifle", 0, 0, true, false, 360, 3.3333333f, true, 1, 31, 38, 2.0f, 1.4f, 4, 4.4f, 2.0f, 44.0f, 0.82f, 2.2f, 0.68f, 48, 58, 75, 11, "Four-Headed Mongrel", "Shoots erratic kinetic bullets simultaneously from all four weapon barrels.");
        weaponSpecArray[5] = new WeaponSpec(5, "ace_of_spades", "Ace of Spades", "Exotic Kinetic Hand Cannon", 1, 0, true, false, 140, 8.571428f, false, 1, 13, 38, 7.2f, 1.65f, 1, 1.55f, 0.06f, 62.0f, 0.8f, 3.8f, 0.72f, 62, 74, 100, 3, "Memento Mori & Firefly", "Reloading after a kill loads 6x 1.3x high-damage rounds. Precision kills explode.");
        weaponSpecArray[6] = new WeaponSpec(6, "sunshot", "Sunshot", "Exotic Solar Hand Cannon", 1, 1, true, false, 150, 8.0f, false, 1, 12, 32, 6.8f, 1.6f, 1, 1.5f, 0.07f, 58.0f, 0.81f, 3.55f, 0.68f, 68, 65, 98, 4, "Sunburn & Sun Blast", "Fires explosive Solar rounds. Targets defeated with Sunshot erupt in Solar energy.");
        weaponSpecArray[7] = new WeaponSpec(7, "thorn", "Thorn", "Exotic Weapon of Sorrow Hand Cannon", 1, 5, true, false, 140, 8.571428f, false, 1, 11, 36, 6.8f, 1.65f, 1, 1.55f, 0.07f, 60.0f, 0.8f, 3.9f, 0.75f, 60, 82, 100, 12, "Mark of the Devourer", "Rounds pierce with Soulfire toxin. Kills empower poison (+30% dmg) and return 4 rounds.");
        weaponSpecArray[8] = new WeaponSpec(8, "crimson", "Crimson", "Exotic 3-Burst Bayonet Hand Cannon", 1, 0, true, false, 120, 10.0f, false, 3, 24, 34, 3.1f, 1.6f, 1, 1.35f, 0.07f, 56.0f, 0.8f, 1.55f, 0.34f, 88, 78, 85, 13, "Banned Weapon & Cruel Remedy", "Fires a 3-round burst. Kills heal the wielder; precision kills refill the magazine.");
        weaponSpecArray[9] = new WeaponSpec(9, "the_last_word", "The Last Word", "Exotic Fan-Fire Hand Cannon", 1, 0, true, false, 225, 5.3333335f, true, 1, 8, 28, 6.2f, 1.6f, 1, 0.45f, 0.3f, 44.0f, 0.85f, 4.1f, 0.58f, 52, 85, 100, 14, "Fan Fire", "Full-auto fan-hammer revolver. Superior hip-fire accuracy, magnetism, damage, and reload.");
        weaponSpecArray[10] = new WeaponSpec(10, "izanagis_burden", "Izanagi's Burden", "Exotic Kinetic Sniper Rifle", 2, 0, true, true, 90, 13.333333f, false, 1, 4, 44, 15.5f, 2.25f, 1, 5.8f, 0.01f, 135.0f, 0.34f, 5.8f, 0.9f, 54, 64, 78, 5, "Honed Edge", "Hold [R] to consume up to 4 rounds into a single 3.5x Honed Edge x4 super-shot.");
        weaponSpecArray[11] = new WeaponSpec(11, "cloudstrike", "Cloudstrike", "Exotic Arc Sniper Rifle", 2, 2, true, true, 140, 8.571428f, false, 1, 7, 40, 12.2f, 2.15f, 1, 5.2f, 0.01f, 130.0f, 0.38f, 4.3f, 0.72f, 60, 72, 85, 6, "Mortal Polarity & Stormbringer", "Precision kills summon Arc lightning. 3 rapid precision hits unleash an Arc storm.");
        weaponSpecArray[12] = new WeaponSpec(12, "whisper_of_the_worm", "Whisper of the Worm", "Exotic Taken Heavy Sniper Rifle", 2, 1, true, true, 72, 16.666666f, false, 1, 3, 46, 18.5f, 2.35f, 1, 6.2f, 0.01f, 145.0f, 0.32f, 6.8f, 1.0f, 45, 58, 75, 15, "White Nail", "3 rapid precision hits instantly refill all 3 rounds into the magazine from thin air.");
        weaponSpecArray[13] = new WeaponSpec(13, "darci", "D.A.R.C.I.", "Exotic Arc AI Rangefinder Sniper", 2, 2, true, true, 140, 8.571428f, false, 1, 7, 36, 11.8f, 2.15f, 1, 4.8f, 0.01f, 130.0f, 0.36f, 3.7f, 0.58f, 82, 76, 95, 16, "Personal Assistant", "ADS displays live target telemetry, boosts damage (+30%), and jolts on precision hits.");
        weaponSpecArray[14] = new WeaponSpec(14, "ice_breaker", "Ice Breaker", "Exotic Solar Regenerating Sniper", 2, 1, true, true, 72, 16.666666f, false, 1, 6, 48, 16.5f, 2.25f, 1, 5.9f, 0.01f, 135.0f, 0.35f, 6.5f, 0.95f, 42, 62, 80, 17, "No Backpack & Ice Breaker", "Passively regenerates 1 Solar round every 2.5s. Defeated targets explode in Solar fire.");
        weaponSpecArray[15] = new WeaponSpec(15, "the_chaperone", "The Chaperone", "Exotic Kinetic Slug Shotgun", 3, 0, true, true, 70, 17.142857f, false, 1, 6, 42, 14.0f, 2.05f, 1, 2.2f, 0.01f, 26.0f, 0.76f, 5.2f, 0.92f, 64, 78, 85, 7, "The Roadborn", "Fires a single precision slug. Precision kills boost range, fire rate & damage for 8s.");
        weaponSpecArray[16] = new WeaponSpec(16, "ikelos_sg_v103", "IKELOS_SG_v1.0.3", "Warmind Solar Rapid-Fire Shotgun", 3, 1, false, true, 140, 8.571428f, true, 1, 7, 46, 2.1f, 1.25f, 8, 7.8f, 5.1f, 18.0f, 0.82f, 4.8f, 1.05f, 55, 62, 75, 8, "Trench Barrel & Incandescent", "Melee hits empower the next 3 shots (+45% dmg). Kills scorch nearby foes.");
        weaponSpecArray[17] = new WeaponSpec(17, "conditional_finality", "Conditional Finality", "Exotic Stasis/Solar Dual Shotgun", 3, 4, true, true, 55, 21.818182f, false, 1, 2, 36, 2.2f, 1.25f, 10, 6.5f, 4.2f, 19.0f, 0.8f, 6.1f, 1.15f, 58, 68, 95, 18, "Paracausal Pellets", "Split double-barrel: Barrel 1 freezes targets in Stasis; Barrel 2 triggers Solar Ignition.");
        weaponSpecArray[18] = new WeaponSpec(18, "tractor_cannon", "Tractor Cannon", "Exotic Void Propulsor Shotgun", 3, 3, true, true, 80, 15.0f, false, 1, 7, 40, 2.6f, 1.2f, 6, 9.0f, 6.4f, 15.0f, 0.85f, 5.4f, 1.0f, 66, 75, 100, 19, "Repulsor Force", "Emits a wide Void gravity shockwave that violently launches enemies backward.");
        weaponSpecArray[19] = new WeaponSpec(19, "lord_of_wolves", "Lord of Wolves", "Exotic Eliksni Shrapnel Shotgun", 3, 1, true, true, 105, 11.428572f, true, 5, 30, 42, 2.8f, 1.3f, 1, 3.0f, 1.25f, 22.0f, 0.8f, 1.35f, 0.38f, 68, 72, 85, 20, "Release the Wolves", "Fires a 5-shot Solar flame burst. Hold [R] to toggle 10-shot Release the Wolves mode.");
        weaponSpecArray[20] = new WeaponSpec(20, "blood_feud", "Blood Feud", "Plunder Stasis Submachine Gun", 0, 4, false, false, 750, 1.6f, true, 1, 37, 30, 3.2f, 1.45f, 1, 1.45f, 0.16f, 44.0f, 0.82f, 1.3f, 0.42f, 64, 68, 92, 21, "Pugilist & Headstone", "Precision kills shatter foes in Stasis frost; melee hits restore magazine ammo.");
        weaponSpecArray[21] = new WeaponSpec(21, "brigands_law", "Brigand's Law", "Plunder Arc Rapid Sidearm", 1, 2, false, false, 450, 2.6666667f, true, 1, 19, 26, 4.5f, 1.5f, 1, 1.1f, 0.1f, 38.0f, 0.84f, 1.6f, 0.4f, 74, 80, 95, 22, "Voltshot", "Reloading after a kill overcharges the next round with chain Arc Jolt lightning.");
        weaponSpecArray[22] = new WeaponSpec(22, "tarnished_mettle", "Tarnished Mettle", "Plunder Arc Scout Rifle", 2, 2, false, false, 200, 6.0f, false, 1, 15, 32, 6.8f, 1.75f, 1, 2.0f, 0.02f, 105.0f, 0.52f, 2.6f, 0.5f, 66, 76, 85, 23, "Voltshot & Explosive Payload", "Rounds detonate with Arc splash; reloading after a kill loads a chain Jolt bolt.");
        weaponSpecArray[23] = new WeaponSpec(23, "no_reprieve", "No Reprieve", "Plunder Stasis Slug Shotgun", 3, 4, false, true, 65, 18.461538f, false, 1, 6, 40, 13.5f, 2.0f, 1, 2.2f, 0.01f, 25.0f, 0.76f, 5.1f, 0.9f, 60, 72, 80, 24, "Surrounded & Headstone", "Precision Stasis slug; precision kills trigger a Stasis crystal shatter burst.");
        weaponSpecArray[24] = new WeaponSpec(24, "wrongful_ingress", "Wrongful Ingress", "Eclipse Monolith Mythic Auto Rifle", 0, 1, true, false, 720, 1.6666666f, true, 1, 48, 32, 3.4f, 1.5f, 1, 1.3f, 0.14f, 58.0f, 0.8f, 1.25f, 0.38f, 74, 78, 95, 25, "Disturbance & Executioner", "Deals +35% damage to wounded targets (<50% HP); kills auto-return 12 rounds.");
        weaponSpecArray[25] = new WeaponSpec(25, "parole", "Parole", "Eclipse Monolith Mythic Hand Cannon", 1, 0, true, false, 120, 10.0f, false, 1, 10, 36, 7.6f, 1.65f, 1, 1.5f, 0.05f, 64.0f, 0.8f, 4.1f, 0.74f, 62, 76, 95, 26, "Fourth Sentence & Disturbance", "Every 4th round fired deals 2.2x execution damage; kills return 2 rounds.");
        weaponSpecArray[26] = new WeaponSpec(26, "total_lockdown", "Total Lockdown", "Eclipse Monolith Mythic Precision Rifle", 2, 2, true, false, 260, 4.6153846f, true, 1, 18, 32, 5.8f, 1.7f, 1, 1.85f, 0.02f, 102.0f, 0.48f, 2.15f, 0.44f, 72, 78, 85, 27, "Lockdown Piercer & Disturbance", "Rapid 260 RPM piercing precision rifle; hits ramp damage (+30%), kills return 4 rounds.");
        weaponSpecArray[27] = new WeaponSpec(27, "eternal_retribution", "Eternal Retribution", "Eclipse Monolith Mythic Shotgun", 3, 3, true, true, 140, 8.571428f, true, 1, 8, 42, 2.2f, 1.25f, 8, 7.2f, 4.6f, 19.0f, 0.82f, 4.6f, 1.0f, 58, 66, 85, 28, "Vengeful Cadence & Disturbance", "Pausing fire charges a +40% Void blast; kills return 2 shells.");
        weaponSpecArray[28] = new WeaponSpec(28, "necrochasm", "Necrochasm", "Exotic Weapon of Sorrow Auto Rifle", 0, 5, true, false, 720, 1.6666666f, true, 1, 51, 32, 3.5f, 1.55f, 1, 1.25f, 0.12f, 60.0f, 0.8f, 1.18f, 0.34f, 84, 85, 95, 29, "Cursebringer & Desperation", "Precision kills trigger a Cursed Thrall explosion, refill the mag & boost RPM to 900.");
        weaponSpecArray[29] = new WeaponSpec(29, "abyss_defiant", "Abyss Defiant", "Crota's End Solar Auto Rifle", 0, 1, false, false, 360, 3.3333333f, true, 1, 34, 36, 5.1f, 1.55f, 1, 1.4f, 0.11f, 72.0f, 0.76f, 1.85f, 0.42f, 68, 72, 85, 30, "Cursed Thrall & Incandescent", "Slow-firing Hive Solar rifle; kills scorch and detonate nearby combatants.");
        weaponSpecArray[30] = new WeaponSpec(30, "fang_of_ir_yut", "Fang of Ir Y\u00fbt", "Crota's End Soulfire Scout Rifle", 2, 5, false, false, 260, 4.6153846f, true, 1, 18, 30, 5.6f, 1.7f, 1, 1.75f, 0.02f, 104.0f, 0.5f, 2.05f, 0.4f, 74, 82, 95, 31, "Target Mark & Precision Instrument", "Rapid 260 RPM Hive scout; precision hits ramp Soulfire damage & return 2 rounds on kill.");
        weaponSpecArray[31] = new WeaponSpec(31, "oversoul_edict", "Oversoul Edict", "Crota's End Arc 3-Burst Pulse Rifle", 0, 2, false, false, 540, 2.2222223f, true, 3, 36, 32, 2.4f, 1.55f, 1, 1.3f, 0.09f, 68.0f, 0.76f, 1.05f, 0.28f, 78, 80, 85, 32, "Darkness Bolt & Voltshot", "Rapid 3-round Arc pulse rifle; kills discharge chain Arc Jolt lightning.");
        weaponSpecArray[32] = new WeaponSpec(32, "word_of_crota", "Word of Crota", "Crota's End Void 180 Hand Cannon", 1, 3, false, false, 180, 6.6666665f, false, 1, 14, 30, 6.0f, 1.6f, 1, 1.25f, 0.05f, 58.0f, 0.81f, 2.75f, 0.5f, 78, 80, 95, 33, "Phantom Gift & Destabilizing", "Precision 180 RPM Void revolver; crits ramp damage, kills trigger Void burst.");
        weaponSpecArray[33] = new WeaponSpec(33, "swordbreaker", "Swordbreaker", "Crota's End Soulfire Shotgun", 3, 5, false, true, 80, 15.0f, false, 1, 7, 38, 2.2f, 1.25f, 8, 7.0f, 4.5f, 19.5f, 0.8f, 4.9f, 0.95f, 62, 74, 85, 34, "Cursed Bayonet & One-Two Punch", "Lightweight Hive shotgun; kills restore 2 shells and unleash Soulfire poison.");
        weaponSpecArray[34] = new WeaponSpec(34, "song_of_ir_yut", "Song of Ir Y\u00fbt", "Crota's End Arc Machine Gun", 0, 2, false, true, 450, 2.6666667f, true, 1, 72, 54, 4.8f, 1.5f, 1, 1.65f, 0.14f, 76.0f, 0.78f, 1.55f, 0.42f, 74, 78, 95, 35, "Deathbringer Choir", "72-round Arc heavy machine gun; kills trigger Arc shockwaves and auto-load 8 rounds.");
        weaponSpecArray[35] = new WeaponSpec(35, "vex_mythoclast", "Vex Mythoclast", "Exotic Solar Vex Fusion-Auto Rifle", 0, 1, true, false, 360, 3.3333333f, true, 1, 32, 34, 5.2f, 1.55f, 1, 1.15f, 0.08f, 66.0f, 0.78f, 1.6f, 0.36f, 80, 86, 100, 36, "Timeless Mythoclast", "Full-auto Solar Vex bolts. Kills grant Overcharge (x3); Hold [R] swaps to Linear Fusion Mode!");
        weaponSpecArray[36] = new WeaponSpec(36, "fatebringer", "Fatebringer", "Vault of Glass Kinetic Hand Cannon", 1, 0, false, false, 140, 8.571428f, false, 1, 12, 34, 7.0f, 1.65f, 1, 1.45f, 0.05f, 64.0f, 0.8f, 3.65f, 0.65f, 68, 84, 98, 37, "Explosive Payload & Firefly", "Rounds detonate on impact; precision kills trigger a Solar Firefly explosion & Outlaw reload.");
        weaponSpecArray[37] = new WeaponSpec(37, "vision_of_confluence", "Vision of Confluence", "Vault of Glass Solar Scout Rifle", 2, 1, false, false, 180, 6.6666665f, true, 1, 17, 32, 6.8f, 1.75f, 1, 1.8f, 0.02f, 108.0f, 0.52f, 2.35f, 0.44f, 76, 80, 85, 38, "Zen Moment & Rewind Rounds", "Full-auto Hezen Solar scout rifle; kills restore rounds & scorch targets.");
        weaponSpecArray[38] = new WeaponSpec(38, "praedyths_revenge", "Praedyth's Revenge", "Vault of Glass Kinetic Sniper Rifle", 2, 0, false, true, 140, 8.571428f, false, 1, 6, 34, 12.0f, 2.15f, 1, 4.6f, 0.01f, 128.0f, 0.4f, 3.6f, 0.56f, 80, 78, 95, 39, "Time's Trick & Firefly", "Low-recoil rapid Vault sniper; precision kills detonate targets & grant Outlaw.");
        weaponSpecArray[39] = new WeaponSpec(39, "found_verdict", "Found Verdict", "Vault of Glass Arc Aggressive Shotgun", 3, 2, false, true, 55, 21.818182f, false, 1, 6, 44, 2.45f, 1.25f, 8, 6.8f, 4.3f, 20.5f, 0.8f, 5.7f, 1.08f, 52, 66, 80, 40, "Oracle Disruptor & Arc Verdict", "High-impact Hezen Arc shotgun; kills unleash chain Arc Jolt.");
        weaponSpecArray[40] = new WeaponSpec(40, "telesto", "Telesto", "Exotic Void Fusion Rifle", 4, 3, true, true, 46, 26.0f, false, 1, 6, 48, 4.7f, 1.0f, 1, 1.4f, 0.12f, 35.0f, 0.72f, 3.2f, 0.5f, 74, 72, 90, 140, "Unplanned Reprieve", "Hold fire to charge seven sticky Void bolts. Bolts detonate after a short delay.");
        weaponSpecArray[41] = new WeaponSpec(41, "jotunn", "Jötunn", "Exotic Solar Fusion Rifle", 4, 1, true, true, 43, 28.0f, false, 1, 5, 48, 20.0f, 1.0f, 1, 1.4f, 0.12f, 52.0f, 0.72f, 3.2f, 0.5f, 74, 72, 90, 141, "Charge Shot", "A charged tracking Solar bolt detonates and leaves a short burning patch.");
        weaponSpecArray[42] = new WeaponSpec(42, "merciless", "Merciless", "Exotic Solar Fusion Rifle", 4, 1, true, true, 46, 26.0f, false, 1, 8, 44, 4.0f, 1.0f, 1, 1.4f, 0.12f, 34.0f, 0.72f, 3.2f, 0.5f, 74, 72, 90, 142, "Conserve Momentum", "Nonlethal hits shorten the next charge, down to half the normal time. Kills reset it.");
        weaponSpecArray[43] = new WeaponSpec(43, "riptide", "Riptide", "Stasis Fusion Rifle", 4, 4, false, true, 67, 18.0f, false, 1, 8, 40, 3.1f, 1.0f, 1, 1.4f, 0.12f, 30.0f, 0.72f, 3.2f, 0.5f, 74, 72, 90, 143, "Chill Clip", "The top half of the magazine slows targets; repeated hits build freezing.");
        weaponSpecArray[44] = new WeaponSpec(44, "sleeper_simulant", "Sleeper Simulant", "Exotic Solar Linear Fusion Rifle", 5, 1, true, true, 40, 30.0f, false, 1, 4, 52, 29.0f, 2.1f, 1, 3.0f, 0.02f, 125.0f, 0.4f, 3.2f, 0.5f, 74, 72, 90, 144, "Dornröschen", "Charges a piercing Solar beam. Surviving targets can be struck through one another.");
        weaponSpecArray[45] = new WeaponSpec(45, "taipan_4fr", "Taipan-4fr", "Void Linear Fusion Rifle", 5, 3, false, true, 60, 20.0f, false, 1, 6, 44, 20.0f, 2.4f, 1, 3.0f, 0.02f, 115.0f, 0.4f, 3.2f, 0.5f, 74, 72, 90, 145, "Triple Tap", "Three precision hits refund one round.");
        weaponSpecArray[46] = new WeaponSpec(46, "coldheart", "Coldheart", "Exotic Arc Trace Rifle", 6, 2, true, true, 600, 2.0f, true, 1, 100, 48, 2.8f, 1.25f, 1, 1.4f, 0.12f, 48.0f, 0.72f, 0.25f, 0.12f, 74, 72, 90, 146, "Longest Winter", "A continuous beam ramps up damage against the same target.");
        weaponSpecArray[47] = new WeaponSpec(47, "prometheus_lens", "Prometheus Lens", "Exotic Solar Trace Rifle", 6, 1, true, true, 600, 2.0f, true, 1, 100, 48, 2.4f, 1.2f, 1, 1.4f, 0.12f, 44.0f, 0.72f, 0.25f, 0.12f, 74, 72, 90, 147, "Prismatic Inferno", "A sustained Solar beam scorches its target and nearby enemies.");
        weaponSpecArray[48] = new WeaponSpec(48, "gjallarhorn", "Gjallarhorn", "Exotic Solar Rocket Launcher", 7, 1, true, true, 46, 26.0f, false, 1, 2, 54, 24.0f, 1.0f, 1, 1.4f, 0.12f, 90.0f, 0.72f, 3.2f, 0.5f, 74, 72, 90, 148, "Wolfpack Rounds", "Tracking rockets burst into four smaller seeking Wolfpack rounds.");
        weaponSpecArray[49] = new WeaponSpec(49, "two_tailed_fox", "Two-Tailed Fox", "Exotic Void / Solar Rocket Launcher", 7, 3, true, true, 46, 26.0f, false, 1, 1, 58, 20.0f, 1.0f, 1, 1.4f, 0.12f, 90.0f, 0.72f, 3.2f, 0.5f, 74, 72, 90, 149, "Twintails", "Launches one Void rocket that weakens and one Solar rocket that burns.");
        weaponSpecArray[50] = new WeaponSpec(50, "witherhoard", "Witherhoard", "Exotic Taken Grenade Launcher", 8, 0, true, true, 50, 24.0f, false, 1, 1, 48, 8.0f, 1.0f, 1, 1.4f, 0.12f, 52.0f, 0.72f, 3.2f, 0.5f, 74, 72, 90, 150, "Primeval’s Torment", "Lobs a blight that leaves a damaging pool for six seconds.");
        weaponSpecArray[51] = new WeaponSpec(51, "le_monarque", "Le Monarque", "Exotic Void Combat Bow", 9, 3, true, false, 50, 24.0f, false, 1, 1, 12, 15.0f, 1.7f, 1, 1.4f, 0.12f, 85.0f, 0.72f, 3.2f, 0.5f, 74, 72, 90, 151, "Poison Arrows", "Hold fire to draw, release to fire. A freshly completed draw poisons the target.");
    }

    public static WeaponSpec byIndex(int n) {
        return ALL[n];
    }

    static { com.jax.destiny2mc.ageless.PackagedWeapons.append(ALL, 52); }

    public static float getRecoilYawTendency(WeaponSpec weaponSpec) {
        int n = weaponSpec.recoilDirection;
        return (float)(Math.sin((double)(n + 5) * 0.3141592653589793) * ((double)(100 - n) / 100.0));
    }

    public static float getStabilityDamping(WeaponSpec weaponSpec) {
        return 1.0f - (float)weaponSpec.stability * 0.0042f;
    }
}

