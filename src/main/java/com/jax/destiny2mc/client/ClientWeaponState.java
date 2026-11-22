/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.jax.destiny2mc.client;

import com.jax.destiny2mc.client.HudConfig;
import com.jax.destiny2mc.client.HudSettingsScreen;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.network.GunActionPayload;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import com.jax.destiny2mc.weapon.WeaponSpec;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientWeaponState {
    public static KeyMapping RELOAD_KEY;
    public static KeyMapping SETTINGS_KEY;
    public static boolean settingsHeldLast;
    public static float prevAds;
    public static float ads;
    public static float prevRecoil;
    public static float recoil;
    public static float prevRecoilRoll;
    public static float recoilRoll;
    public static float prevCamPitch;
    public static float camPitch;
    public static float pendingAimPitch;
    public static float pendingAimYaw;
    public static float spinUp;
    public static int burstRemaining;
    public static int burstDelayTicks;
    public static float bloom;
    public static float fireCooldown;
    public static boolean semiTriggerQueued;
    public static boolean attackHeldLastTick;
    public static int reloadHoldTicks;
    public static boolean altTriggered;
    public static int muzzleFlashTicks;
    public static int hitmarkerTicks;
    public static int hitmarkerFlags;
    public static float lastPartialTick;
    public static int popWriteIdx;
    public static float[] POP_X;
    public static float[] POP_Y;
    public static float[] POP_Z;
    public static float[] POP_DMG;
    public static int[] POP_CRIT;
    public static int[] POP_AGE;

    static {
        POP_X = new float[8];
        POP_Y = new float[8];
        POP_Z = new float[8];
        POP_DMG = new float[8];
        POP_CRIT = new int[8];
        POP_AGE = new int[8];
        for (int i = 0; i < 8; ++i) {
            ClientWeaponState.POP_AGE[i] = 99;
        }
    }

    public static float getSmoothAds(float f) {
        float f2 = prevAds + (ads - prevAds) * f;
        if (f2 < 0.0f) {
            f2 = 0.0f;
        }
        if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        return f2 * f2 * (3.0f - 2.0f * f2);
    }

    public static float getSmoothRecoil(float f) {
        return prevRecoil + (recoil - prevRecoil) * f;
    }

    public static void onAttackTriggered() {
        semiTriggerQueued = true;
    }

    public static void onCombatFeedback(float f, int n, float f2, float f3, float f4) {
        hitmarkerTicks = 7;
        hitmarkerFlags = n;
        int n2 = popWriteIdx;
        ClientWeaponState.POP_X[n2] = f2;
        ClientWeaponState.POP_Y[n2] = f3;
        ClientWeaponState.POP_Z[n2] = f4;
        ClientWeaponState.POP_DMG[n2] = f;
        ClientWeaponState.POP_CRIT[n2] = n & 1;
        ClientWeaponState.POP_AGE[n2] = 0;
        popWriteIdx = n2 + 1 & 7;
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            if ((n & 1) != 0) {
                localPlayer.playSound(SoundEvent.createVariableRangeEvent((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"entity.arrow.hit_player")), 0.45f, 1.85f);
            } else {
                localPlayer.playSound(SoundEvent.createVariableRangeEvent((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"block.note_block.hat")), 0.35f, 1.95f);
            }
        }
    }

    public static void sendAction(int n, LocalPlayer localPlayer, boolean bl) {
        PacketDistributor.sendToServer((CustomPacketPayload)new GunActionPayload((byte)n, localPlayer.getYRot(), localPlayer.getXRot(), (byte)(bl ? 1 : 0)), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void fireSingleShotAndKick(LocalPlayer localPlayer, ItemStack itemStack, WeaponSpec weaponSpec, float f) {
        boolean bl = f >= 0.55f;
        ClientWeaponState.sendAction(0, localPlayer, bl);
        kickOnly(localPlayer,itemStack,weaponSpec,f);
    }

    public static void kickOnly(LocalPlayer localPlayer,ItemStack itemStack,WeaponSpec weaponSpec,float f){
        recoil = 1.0f;
        muzzleFlashTicks = 2;
        float f2 = bloom + 0.45f;
        if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        bloom = f2;
        float f3 = weaponSpec.perkId == 14 ? 0.65f + 0.4f * f : 1.8f - 0.8f * f;
        f3 = HudConfig.getRecoilMultiplier() * f3;
        if (weaponSpec.perkId == 9) {
            f3 *= 1.0f - spinUp * 0.25f;
        } else if (weaponSpec.perkId == 5 && GunDataHelper.getInt(itemStack, "d2_honed", 0) > 1) {
            f3 *= 1.45f;
        }
        float f4 = weaponSpec.recoilPitch * (f3 *= WeaponSpec.getStabilityDamping(weaponSpec)) * WeaponPerkRegistry.getRecoilPitchMult(itemStack, weaponSpec) * (0.92f + (float)Math.random() * 0.16f);
        float f5 = f4 * 0.62f;
        float f6 = localPlayer.getXRot() - f5;
        if (f6 < -89.5f) {
            f6 = -89.5f;
        }
        localPlayer.setXRot(f6);
        pendingAimPitch += f4 - f5;
        camPitch += f4 * 0.2f;
        float f7 = (WeaponSpec.getRecoilYawTendency(weaponSpec) + (float)((Math.random() - 0.5) * 0.32)) * (weaponSpec.recoilYaw * f3 * WeaponPerkRegistry.getRecoilYawMult(itemStack, weaponSpec));
        localPlayer.setYRot(localPlayer.getYRot() + f7 * 0.65f);
        pendingAimYaw += f7 * 0.35f;
        recoilRoll = f7 * 1.15f;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void tickClient() {
        HudConfig.ensureLoaded();
        prevAds = ads;
        prevRecoil = recoil;
        prevCamPitch = camPitch;
        prevRecoilRoll = recoilRoll;
        recoil *= 0.6f;
        camPitch *= 0.52f;
        recoilRoll *= 0.55f;
        bloom *= 0.76f;
        if (fireCooldown > 0.0f) {
            fireCooldown -= 1.0f;
        }
        if (muzzleFlashTicks > 0) {
            --muzzleFlashTicks;
        }
        if (hitmarkerTicks > 0) {
            --hitmarkerTicks;
        }
        for (int i = 0; i < 8; ++i) {
            if (POP_AGE[i] >= 22) continue;
            ClientWeaponState.POP_AGE[i] = POP_AGE[i] + 1;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer != null) {
            KeyMapping keyMapping = SETTINGS_KEY;
            if (keyMapping != null) {
                boolean bl = keyMapping.isDown();
                if (bl && !settingsHeldLast && minecraft.screen == null) {
                    settingsHeldLast = true;
                    minecraft.setScreen((Screen)new HudSettingsScreen());
                    return;
                }
                settingsHeldLast = bl;
            }
            if (minecraft.screen == null) {
                if (pendingAimPitch > 0.01f) {
                    float f = pendingAimPitch * 0.68f;
                    float f2 = localPlayer.getXRot() - f;
                    if (f2 < -89.5f) {
                        f2 = -89.5f;
                    }
                    localPlayer.setXRot(f2);
                    pendingAimPitch -= f;
                } else {
                    pendingAimPitch = 0.0f;
                }
                float f = pendingAimYaw;
                if (!(f > 0.005f) && !(f < -0.005f)) {
                    pendingAimYaw = 0.0f;
                } else {
                    float f3 = f * 0.68f;
                    localPlayer.setYRot(localPlayer.getYRot() + f3);
                    pendingAimYaw = f - f3;
                }
                ItemStack itemStack = localPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof DestinyGunItem) {
                    float f4;
                    int n;
                    float f5;
                    WeaponSpec weaponSpec = ((DestinyGunItem)itemStack.getItem()).spec;
                    long l = localPlayer.level().getGameTime();
                    boolean bl = GunDataHelper.getLong(itemStack, "d2_rel_end") > l;
                    Options options = minecraft.options;
                    if (!bl && options.keyUse.isDown()) {
                        f5 = ads + 0.23f;
                        if (f5 > 1.0f) {
                            f5 = 1.0f;
                        }
                    } else {
                        f5 = ads - 0.25f;
                        if (f5 < 0.0f) {
                            f5 = 0.0f;
                        }
                    }
                    ads = f5;
                    KeyMapping keyMapping2 = RELOAD_KEY;
                    if (keyMapping2 != null) {
                        if (keyMapping2.isDown()) {
                            int n2 = ++reloadHoldTicks;
                            n = weaponSpec.perkId;
                            if (n != 5 && n != 20 && n != 36) {
                                if (n2 == 1) {
                                    ClientWeaponState.sendAction(1, localPlayer, false);
                                }
                            } else if (n2 >= 12 && !altTriggered) {
                                altTriggered = true;
                                ClientWeaponState.sendAction(2, localPlayer, false);
                            }
                        } else {
                            int n3 = reloadHoldTicks;
                            if (!(n3 <= 0 || (n = weaponSpec.perkId) != 5 && n != 36 && n != 20 || altTriggered)) {
                                ClientWeaponState.sendAction(1, localPlayer, false);
                            }
                            reloadHoldTicks = 0;
                            altTriggered = false;
                        }
                    }
                    if(com.jax.destiny2mc.client.ProgramInput.tick(localPlayer,itemStack,weaponSpec,bl,f5))return;
                    if(weaponSpec.index>=40) { com.jax.destiny2mc.client.frontier.FrontierWeaponInput.tick(localPlayer,itemStack,weaponSpec,bl,f5); return; }
                    if ((n = burstRemaining) > 0) {
                        if (!bl && GunDataHelper.getMag(itemStack, weaponSpec) > 0) {
                            if (--burstDelayTicks > 0) return;
                            burstRemaining = n - 1;
                            burstDelayTicks = 1;
                            ClientWeaponState.fireSingleShotAndKick(localPlayer, itemStack, weaponSpec, f5);
                            return;
                        }
                        burstRemaining = 0;
                    }
                    boolean bl2 = options.keyAttack.isDown();
                    if (weaponSpec.perkId == 9 && bl2 && !bl) {
                        f4 = spinUp + 0.038f;
                        if (f4 > 1.0f) {
                            f4 = 1.0f;
                        }
                    } else {
                        f4 = spinUp - 0.09f;
                        if (f4 < 0.0f) {
                            f4 = 0.0f;
                        }
                    }
                    spinUp = f4;
                    boolean bl3 = false;
                    if (weaponSpec.fullAuto) {
                        if (bl2 || semiTriggerQueued) {
                            bl3 = true;
                            semiTriggerQueued = false;
                        }
                    } else if (semiTriggerQueued || bl2 && !attackHeldLastTick) {
                        bl3 = true;
                    }
                    attackHeldLastTick = bl2;
                    if (!bl3) return;
                    if (!bl) {
                        if (fireCooldown > 0.05f) return;
                        semiTriggerQueued = false;
                        if (GunDataHelper.getMag(itemStack, weaponSpec) <= 0) {
                            ClientWeaponState.sendAction(1, localPlayer, false);
                            fireCooldown = weaponSpec.fireDelayTicks;
                            return;
                        }
                        float f6 = weaponSpec.fireDelayTicks;
                        if ((weaponSpec.perkId == 7 || weaponSpec.perkId == 29 || weaponSpec.perkId == 28) && GunDataHelper.getLong(itemStack, "d2_pexp") > l) {
                            f6 *= 0.7f;
                        }
                        if (weaponSpec.perkId == 9) {
                            f6 *= 1.0f - spinUp * 0.6f;
                            if (weaponSpec.perkId == 36 && GunDataHelper.getInt(itemStack, "d2_alt", 0) > 0) {
                                f6 = 13.0f;
                            }
                        }
                        if (!(fireCooldown < 0.0f)) {
                            fireCooldown = f6;
                        } else {
                            float f7 = fireCooldown + f6;
                            if (f7 < 0.5f) {
                                f7 = f6;
                            }
                            fireCooldown = f7;
                        }
                        int n4 = weaponSpec.burstCount;
                        if (weaponSpec.perkId == 20 && GunDataHelper.getInt(itemStack, "d2_alt", 0) > 0) {
                            n4 = 10;
                            fireCooldown = f6 * 1.35f;
                        }
                        if (n4 > 1) {
                            burstRemaining = n4 - 1;
                            burstDelayTicks = 1;
                        }
                        ClientWeaponState.fireSingleShotAndKick(localPlayer, itemStack, weaponSpec, f5);
                        return;
                    }
                    semiTriggerQueued = false;
                    return;
                }
            }
        }
        ads = 0.0f;
        spinUp = 0.0f;
        burstRemaining = 0;
        semiTriggerQueued = false;
        attackHeldLastTick = false;
        reloadHoldTicks = 0;
    }
}

