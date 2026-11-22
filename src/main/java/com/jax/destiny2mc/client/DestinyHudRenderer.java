/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package com.jax.destiny2mc.client;

import com.jax.destiny2mc.client.ClientWeaponState;
import com.jax.destiny2mc.client.HudConfig;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import com.jax.destiny2mc.weapon.WeaponSpec;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class DestinyHudRenderer {
    public static final ResourceLocation SIL_AUTO = ResourceLocation.fromNamespaceAndPath((String)"destiny2mc", (String)"textures/gui/silhouettes/sil_auto.png");
    public static final ResourceLocation SIL_HC = ResourceLocation.fromNamespaceAndPath((String)"destiny2mc", (String)"textures/gui/silhouettes/sil_hc.png");
    public static final ResourceLocation SIL_SNIPER = ResourceLocation.fromNamespaceAndPath((String)"destiny2mc", (String)"textures/gui/silhouettes/sil_sniper.png");
    public static final ResourceLocation SIL_SHOTGUN = ResourceLocation.fromNamespaceAndPath((String)"destiny2mc", (String)"textures/gui/silhouettes/sil_shotgun.png");

    public static ResourceLocation getSilhouette(int n) {
        String icon=switch(n){case 1->"hc";case 2->"sniper";case 3->"shotgun";case 4->"fusion";case 5->"linear";case 6->"trace";case 7->"rocket";case 8->"grenade";case 9->"bow";default->"auto";};
        return ResourceLocation.fromNamespaceAndPath("destiny2mc","textures/gui/silhouettes/sil_"+icon+".png");
    }

    public static int getEffectiveElement(ItemStack itemStack, WeaponSpec weaponSpec) {
        if (weaponSpec.perkId == 18 && GunDataHelper.getMag(itemStack, weaponSpec) == 1) {
            return 1;
        }
        return weaponSpec.element;
    }

    public static int getElementColor(int n) {
        block5: {
            block4: {
                block3: {
                    block2: {
                        block1: {
                            if (n == 1) break block1;
                            if (n == 2) break block2;
                            if (n == 3) break block3;
                            if (n == 4) break block4;
                            if (n != 5) {
                                return -723720;
                            }
                            break block5;
                        }
                        return -886475;
                    }
                    return -9910798;
                }
                return -5143355;
            }
            return -11695873;
        }
        return -12388994;
    }

    public static String getElementTag(int n) {
        block5: {
            block4: {
                block3: {
                    block2: {
                        block1: {
                            if (n == 1) break block1;
                            if (n == 2) break block2;
                            if (n == 3) break block3;
                            if (n == 4) break block4;
                            if (n != 5) {
                                return "KIN";
                            }
                            break block5;
                        }
                        return "SOL";
                    }
                    return "ARC";
                }
                return "VOID";
            }
            return "STA";
        }
        return "TOX";
    }

    public static String getBl4ModeLabel(ItemStack itemStack, WeaponSpec weaponSpec) {
        if(weaponSpec.perkId==5&&GunDataHelper.getInt(itemStack,"d2_honed",0)>1)return "Honed Edge";
        if(weaponSpec.perkId==20&&GunDataHelper.getInt(itemStack,"d2_alt",0)>0)return "Wolves Mode";
        if(weaponSpec.perkId==36&&GunDataHelper.getInt(itemStack,"d2_alt",0)>0)return "Linear Fusion";
        return switch(weaponSpec.archetype){case 1->"Hand Cannon";case 2->"Sniper Rifle";case 3->"Shotgun";case 4->"Fusion Rifle";case 5->"Linear Fusion";case 6->"Trace Rifle";case 7->"Rocket Launcher";case 8->"Grenade Launcher";case 9->"Combat Bow";default->"Automatic";};
    }

    public static void drawInfinityGlyph(GuiGraphics guiGraphics, int n, int n2, int n3) {
        guiGraphics.fill(n + 1, n2 + 0, n + 5, n2 + 1, n3);
        guiGraphics.fill(n + 1, n2 + 6, n + 5, n2 + 7, n3);
        guiGraphics.fill(n + 0, n2 + 1, n + 1, n2 + 6, n3);
        guiGraphics.fill(n + 10, n2 + 0, n + 14, n2 + 1, n3);
        guiGraphics.fill(n + 10, n2 + 6, n + 14, n2 + 7, n3);
        guiGraphics.fill(n + 14, n2 + 1, n + 15, n2 + 6, n3);
        guiGraphics.fill(n + 5, n2 + 1, n + 7, n2 + 3, n3);
        guiGraphics.fill(n + 8, n2 + 4, n + 10, n2 + 6, n3);
        guiGraphics.fill(n + 8, n2 + 1, n + 10, n2 + 3, n3);
        guiGraphics.fill(n + 5, n2 + 4, n + 7, n2 + 6, n3);
        guiGraphics.fill(n + 6, n2 + 3, n + 9, n2 + 4, n3);
    }

    public static void drawDiamond(GuiGraphics guiGraphics, int n, int n2, int n3) {
        guiGraphics.fill(n - 0, n2 + -3, n + 1, n2 + -2, n3);
        guiGraphics.fill(n - 1, n2 + -2, n + 2, n2 + -1, n3);
        guiGraphics.fill(n - 2, n2 + -1, n + 3, n2 + 0, n3);
        guiGraphics.fill(n - 3, n2 + 0, n + 4, n2 + 1, n3);
        guiGraphics.fill(n - 2, n2 + 1, n + 3, n2 + 2, n3);
        guiGraphics.fill(n - 1, n2 + 2, n + 2, n2 + 3, n3);
        guiGraphics.fill(n - 0, n2 + 3, n + 1, n2 + 4, n3);
    }

    public static void drawScaledText(GuiGraphics guiGraphics, Font font, String string, int n, int n2, float f, int n3) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((float)n, (float)n2, 0.0f);
        poseStack.scale(f, f, 1.0f);
        guiGraphics.drawString(font, string, 0, 0, n3, true);
        poseStack.popPose();
    }

    public static void drawOutlinedScaledText(GuiGraphics guiGraphics, Font font, String string, int n, int n2, float f, int n3, int n4) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((float)n, (float)n2, 0.0f);
        poseStack.scale(f, f, 1.0f);
        guiGraphics.drawString(font, string, -1, 0, n4, false);
        guiGraphics.drawString(font, string, 1, 0, n4, false);
        guiGraphics.drawString(font, string, 0, -1, n4, false);
        guiGraphics.drawString(font, string, 0, 1, n4, false);
        guiGraphics.drawString(font, string, -1, -1, n4, false);
        guiGraphics.drawString(font, string, 1, 1, n4, false);
        guiGraphics.drawString(font, string, 0, 0, n3, false);
        poseStack.popPose();
    }

    public static void drawSkewedBar(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9) {
        for (int i = 0; i < n4; ++i) {
            int n10 = (n4 - 1 - i) * 55 / 100;
            int n11 = n + n10;
            int n12 = n + n3 + n10 / 3;
            int n13 = n2 + i;
            if (n9 != 0) {
                guiGraphics.fill(n11 - 2, n13, n12 + 3, n13 + 1, n9);
            }
            guiGraphics.fill(n11, n13, n12, n13 + 1, n5);
            if (i < 2 || i >= n4 - 2) continue;
            guiGraphics.fill(n11 + 2, n13, n12 - 2, n13 + 1, n6);
            if (n8 > 0) {
                int n14 = n11 + 2 + n8;
                if (n14 > n12 - 2) {
                    n14 = n12 - 2;
                }
                guiGraphics.fill(n11 + 2, n13, n14, n13 + 1, n7);
            }
            if (i != 5) continue;
            guiGraphics.fill(n11 + 4, n13, n12 - 4, n13 + 1, 0x33FFFFFF);
        }
    }

    public static void drawBl2Icons(GuiGraphics guiGraphics, int n, int n2) {
        guiGraphics.fill(n - 2, n2 - 1, n + 9, n2 + 15, 1144248575);
        guiGraphics.fill(n + 2, n2 + 0, n + 8, n2 + 2, -591617);
        guiGraphics.fill(n + 2, n2 + 3, n + 8, n2 + 5, -591617);
        guiGraphics.fill(n + 1, n2 + 6, n + 7, n2 + 8, -591617);
        guiGraphics.fill(n + 0, n2 + 9, n + 6, n2 + 11, -591617);
        guiGraphics.fill(n + -1, n2 + 12, n + 5, n2 + 14, -591617);
    }

    public static void drawDestinyAbilitySquares(GuiGraphics guiGraphics, int n, int n2, boolean bl) {
        guiGraphics.fill(n, n2 - 5, n + 9, n2 - 2, -1315861);
        guiGraphics.fill(n, n2, n + 26, n2 + 26, -464944467);
        guiGraphics.renderOutline(n + 5, n2 + 5, 16, 16, -197379);
        guiGraphics.renderOutline(n + 8, n2 + 8, 10, 10, -197379);
        guiGraphics.fill(n + 11, n2 + 11, n + 15, n2 + 15, -1);
        int n3 = bl ? -459164552 : -464944467;
        guiGraphics.fill(n + 29, n2, n + 55, n2 + 26, n3);
        guiGraphics.fill(n + 39, n2 + 11, n + 46, n2 + 19, -1);
        guiGraphics.fill(n + 37, n2 + 8, n + 39, n2 + 13, -1);
        guiGraphics.fill(n + 40, n2 + 6, n + 42, n2 + 11, -1);
        guiGraphics.fill(n + 43, n2 + 6, n + 45, n2 + 11, -1);
        guiGraphics.fill(n + 46, n2 + 8, n + 48, n2 + 13, -1);
        guiGraphics.fill(n + 35, n2 + 13, n + 39, n2 + 15, -1);
        guiGraphics.fill(n + 47, n2 + 13, n + 50, n2 + 15, -1);
    }

    public static String trimName(String string, int n) {
        if (string.length() > n) {
            return string.substring(0, n);
        }
        return string;
    }

    public static void renderHitmarker(GuiGraphics guiGraphics, int n, int n2) {
        int n3 = ClientWeaponState.hitmarkerTicks;
        if (n3 > 0) {
            int n4 = ClientWeaponState.hitmarkerFlags;
            int n5 = (n4 & 1) != 0 ? -664490 : ((n4 & 2) != 0 ? -2079174 : -285212673);
            for (int i = 4; i <= 9; ++i) {
                guiGraphics.fill(n + i * -1, n2 + i * -1, n + i * -1 + 1, n2 + i * -1 + 1, n5);
                guiGraphics.fill(n + i * 1, n2 + i * -1, n + i * 1 + 1, n2 + i * -1 + 1, n5);
                guiGraphics.fill(n + i * -1, n2 + i * 1, n + i * -1 + 1, n2 + i * 1 + 1, n5);
                guiGraphics.fill(n + i * 1, n2 + i * 1, n + i * 1 + 1, n2 + i * 1 + 1, n5);
            }
            if ((n4 & 1) != 0) {
                guiGraphics.fill(n + -6, n2 + -5, n + -5, n2 + -4, -2079174);
                guiGraphics.fill(n + 6, n2 + -5, n + 7, n2 + -4, -2079174);
                guiGraphics.fill(n + -6, n2 + 5, n + -5, n2 + 6, -2079174);
                guiGraphics.fill(n + 6, n2 + 5, n + 7, n2 + 6, -2079174);
            }
        }
    }

    public static void renderFloatingNumbers(GuiGraphics guiGraphics, Minecraft minecraft, int n, int n2) {
        if (HudConfig.showDamageNums) {
            LocalPlayer localPlayer = minecraft.player;
            Font font = minecraft.font;
            for (int i = 0; i < 8; ++i) {
                int n3;
                int n4 = ClientWeaponState.POP_AGE[i];
                if (n4 >= 20) continue;
                double d = (double)ClientWeaponState.POP_X[i] - localPlayer.getX();
                double d2 = (double)ClientWeaponState.POP_Y[i] - localPlayer.getEyeY();
                double d3 = (double)ClientWeaponState.POP_Z[i] - localPlayer.getZ();
                double d4 = Math.toRadians(localPlayer.getYRot());
                double d5 = -(d * Math.cos(d4) + d3 * Math.sin(d4));
                double d6 = d3 * Math.cos(d4) - d * Math.sin(d4);
                if (d6 <= 0.5) continue;
                int n5 = n + (int)(d5 / d6 * 140.0) + ((i & 3) * 6 - 9);
                int n6 = n2 - 14 - n4 * 2;
                int n7 = Math.round(ClientWeaponState.POP_DMG[i] * 10.0f);
                String string = Integer.toString(n7);
                if (ClientWeaponState.POP_CRIT[i] != 0) {
                    string = string.concat("!");
                    n3 = -664490;
                } else {
                    n3 = -1;
                }
                guiGraphics.drawCenteredString(font, string, n5, n6, n3);
            }
        }
    }

    public static void renderCenterReticleAndScope(GuiGraphics guiGraphics, float f) {
        ItemStack itemStack;
        ClientWeaponState.lastPartialTick = f;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer != null && (itemStack = localPlayer.getItemInHand(InteractionHand.MAIN_HAND)).getItem() instanceof DestinyGunItem) {
            WeaponSpec weaponSpec = ((DestinyGunItem)itemStack.getItem()).spec;
            int n = guiGraphics.guiWidth();
            int n2 = guiGraphics.guiHeight();
            int n3 = n / 2;
            int n4 = n2 / 2;
            float f2 = ClientWeaponState.getSmoothAds(f);
            int n5 = minecraft.crosshairPickEntity instanceof LivingEntity ? -2079174 : -285212673;
            if (f2 > .72f) {
                if (weaponSpec.id.equals("darci") && minecraft.options.getCameraType().isFirstPerson()) DarciOptic.draw(guiGraphics,n3,n4);
                // One-pixel aiming point. The open 3-D sight frames the world at every zoom.
                guiGraphics.fill(n3, n4, n3+1, n4+1, n5);
            } else {
                int gap=(int)((1-f2*.72f)*(5+ClientWeaponState.bloom*9));
                guiGraphics.fill(n3,n4,n3+1,n4+1,n5);
                guiGraphics.fill(n3-gap-4,n4,n3-gap,n4+1,n5);
                guiGraphics.fill(n3+gap+1,n4,n3+gap+5,n4+1,n5);
                guiGraphics.fill(n3,n4-gap-4,n3+1,n4-gap,n5);
                guiGraphics.fill(n3,n4+gap+1,n3+1,n4+gap+5,n5);
            }
            DestinyHudRenderer.renderHitmarker(guiGraphics, n3, n4);
            DestinyHudRenderer.renderFloatingNumbers(guiGraphics, minecraft, n3, n4);
        }
    }

    public static void renderStrikeObjectiveHud(GuiGraphics guiGraphics, Font font) {
        // Compatibility entry point only. Mission HUD and mission state are removed.
    }

    public static void renderWeaponTelemetryHud(GuiGraphics guiGraphics, float f) {
        // A single layout owns the bottom-left anchor for the ammo card and the equipped powers.
        com.jax.destiny2mc.binder.client.BinderHud.render(guiGraphics, f);
    }

    public static void renderWeaponCardAt(GuiGraphics guiGraphics, Font font, ItemStack itemStack, WeaponSpec weaponSpec, long l, float f, int n, int n2, float f2, int n3) {
        PoseStack poseStack;
        block21: {
            String string;
            int n4;
            int n5;
            int n6;
            block20: {
                block19: {
                    float f3;
                    int n7;
                    block18: {
                        int n8;
                        block17: {
                            int n9;
                            block16: {
                                block15: {
                                    int n10 = DestinyHudRenderer.getEffectiveElement(itemStack, weaponSpec);
                                    n8 = DestinyHudRenderer.getElementColor(n10);
                                    String string2 = DestinyHudRenderer.getElementTag(n10);
                                    n9 = weaponSpec.isExotic ? -2047399 : -7713086;
                                    n6 = GunDataHelper.getMag(itemStack, weaponSpec);
                                    n5 = GunDataHelper.getReserve(itemStack, weaponSpec);
                                    n7 = WeaponPerkRegistry.getEffectiveMagSize(itemStack, weaponSpec);
                                    n4 = n6 * 4 <= n7 ? -1561558 : -1;
                                    f3 = -1.0f;
                                    long l2 = GunDataHelper.getLong(itemStack, "d2_rel_end");
                                    if (l2 > l) {
                                        float f4 = GunDataHelper.getInt(itemStack, "d2_rel_dur", 36);
                                        f3 = 1.0f - ((float)(l2 - l) - f) / f4;
                                        if (f3 < 0.0f) {
                                            f3 = 0.0f;
                                        }
                                        if (f3 > 1.0f) {
                                            f3 = 1.0f;
                                        }
                                    }
                                    string = DestinyHudRenderer.trimName(weaponSpec.displayName, 17);
                                    poseStack = guiGraphics.pose();
                                    poseStack.pushPose();
                                    poseStack.translate((float)n, (float)n2, 0.0f);
                                    poseStack.scale(f2, f2, 1.0f);
                                    if (HudConfig.showPerks) {
                                        DestinyHudRenderer.renderActivePerkBanner(guiGraphics, font, itemStack, weaponSpec, l, 0, -66);
                                    }
                                    if (n3 == 0) break block15;
                                    if (n3 == 1) break block16;
                                    if (n3 == 2) break block17;
                                    if (n3 == 3) break block18;
                                    if (n3 == 4) break block19;
                                    break block20;
                                }
                                guiGraphics.fill(0, -30, 134, 0, 1846547740);
                                guiGraphics.fill(0, -30, 134, -29, 0x38FFFFFF);
                                guiGraphics.fill(0, -1, 134, 0, 0x38FFFFFF);
                                guiGraphics.blit(DestinyHudRenderer.getSilhouette(weaponSpec.archetype), 4, -27, 0.0f, 0.0f, 54, 20, 54, 20);
                                String string3 = Integer.toString(n6);
                                int n11 = 95 - (int)((float)font.width(string3) * 1.85f);
                                DestinyHudRenderer.drawScaledText(guiGraphics, font, string3, n11, -23, 1.85f, n4);
                                guiGraphics.fill(100, -27, 101, -3, 0x66FFFFFF);
                                if (!weaponSpec.usesSpecialAmmo) {
                                    DestinyHudRenderer.drawInfinityGlyph(guiGraphics, 106, -24, -591617);
                                    guiGraphics.fill(132, -30, 134, 0, -1);
                                } else {
                                    guiGraphics.drawString(font, Integer.toString(n5), 105, -25, -591617, true);
                                    guiGraphics.fill(132, -30, 134, 0, -11870616);
                                }
                                DestinyHudRenderer.drawDiamond(guiGraphics, 110, -9, n8);
                                if (!(f3 < 0.0f)) {
                                    int n12 = (int)(f3 * 132.0f);
                                    guiGraphics.fill(0, -2, n12, 0, n8);
                                }
                                break block21;
                            }
                            guiGraphics.fill(0, -34, 128, 0, -285212672);
                            guiGraphics.fill(1, -33, 127, -1, -585168100);
                            guiGraphics.fill(1, -33, 127, -32, -1433695349);
                            guiGraphics.drawString(font, string, 5, -31, n9, true);
                            guiGraphics.renderFakeItem(itemStack, 5, -20);
                            DestinyHudRenderer.drawScaledText(guiGraphics, font, Integer.toString(n6), 27, -19, 1.35f, n4);
                            if (!weaponSpec.usesSpecialAmmo) {
                                DestinyHudRenderer.drawInfinityGlyph(guiGraphics, 70, -16, -2236963);
                            } else {
                                String string4 = "/ ".concat(Integer.toString(n5));
                                guiGraphics.drawString(font, string4, 64, -17, -11141291, true);
                            }
                            break block21;
                        }
                        guiGraphics.fill(0, -18, 3, -5, n8);
                        guiGraphics.drawString(font, string, 0, -28, -4471856, true);
                        DestinyHudRenderer.drawScaledText(guiGraphics, font, Integer.toString(n6), 7, -18, 1.45f, n4);
                        if (!weaponSpec.usesSpecialAmmo) {
                            DestinyHudRenderer.drawInfinityGlyph(guiGraphics, 44, -14, -2236963);
                        } else {
                            String string5 = "/ ".concat(Integer.toString(n5));
                            guiGraphics.drawString(font, string5, 42, -15, -7342680, true);
                        }
                        break block21;
                    }
                    int n13 = !(f3 < 0.0f) ? (int)(f3 * 118.0f) : n6 * 118 / n7;
                    DestinyHudRenderer.drawSkewedBar(guiGraphics, 2, -20, 122, 17, -16118768, -10982226, -7625238, n13, 1010296831);
                    String string6 = Integer.toString(n6).concat(" / ");
                    string6 = !weaponSpec.usesSpecialAmmo ? string6.concat(Integer.toString(n7 * 4)) : string6.concat(Integer.toString(n5));
                    int n14 = 118 - (int)((float)font.width(string6) * 1.18f);
                    DestinyHudRenderer.drawOutlinedScaledText(guiGraphics, font, string6, n14, -16, 1.18f, n4, -16118768);
                    DestinyHudRenderer.drawBl2Icons(guiGraphics, 130, -19);
                    break block21;
                }
                guiGraphics.fillGradient(0, -52, 142, -20, -301459422, -283951536);
                guiGraphics.renderOutline(0, -52, 142, 32, -862407484);
                guiGraphics.renderOutline(2, -50, 138, 28, 1715258596);
                guiGraphics.fill(3, -55, 10, -53, -34280);
                guiGraphics.fill(3, -19, 10, -17, -34280);
                guiGraphics.fill(18, -50, 54, -48, -723724);
                guiGraphics.fill(18, -22, 54, -20, -723724);
                String string7 = Integer.toString(n6);
                int n15 = n4 == -1 ? -658714 : n4;
                int n16 = 36 - (int)((float)font.width(string7) * 0.95f);
                DestinyHudRenderer.drawScaledText(guiGraphics, font, string7, n16, -43, 1.9f, n15);
                guiGraphics.fill(76, -45, 80, -44, -1434790678);
                guiGraphics.fill(76, -45, 77, -41, -1434790678);
                guiGraphics.fill(128, -45, 132, -44, -1434790678);
                guiGraphics.fill(131, -45, 132, -41, -1434790678);
                guiGraphics.fill(76, -28, 80, -27, -1434790678);
                guiGraphics.fill(76, -31, 77, -27, -1434790678);
                guiGraphics.fill(128, -28, 132, -27, -1434790678);
                guiGraphics.fill(131, -31, 132, -27, -1434790678);
                if (!weaponSpec.usesSpecialAmmo) {
                    DestinyHudRenderer.drawInfinityGlyph(guiGraphics, 96, -40, -4461834);
                } else {
                    String string8 = Integer.toString(n5);
                    DestinyHudRenderer.drawScaledText(guiGraphics, font, string8, 88, -42, 1.35f, -4461834);
                }
                guiGraphics.fill(0, -15, 78, -1, -586607845);
                String string9 = DestinyHudRenderer.getBl4ModeLabel(itemStack, weaponSpec);
                guiGraphics.drawString(font, string9, 5, -12, -2823950, true);
                guiGraphics.fill(81, -15, 83, -1, -14104854);
                guiGraphics.fill(86, -15, 142, -1, -586607845);
                guiGraphics.fill(86, -15, 89, -14, -197379);
                guiGraphics.fill(86, -2, 89, -1, -197379);
                guiGraphics.fill(139, -15, 142, -14, -197379);
                guiGraphics.fill(139, -2, 142, -1, -197379);
                guiGraphics.renderOutline(93, -13, 11, 10, -6708056);
                guiGraphics.fill(96, -10, 101, -6, -4602682);
                guiGraphics.fill(112, -13, 136, -3, -15512994);
                guiGraphics.renderOutline(112, -13, 24, 10, -13446412);
                guiGraphics.drawString(font, "R", 121, -12, -6490369, true);
                break block21;
            }
            guiGraphics.fill(0, -32, 136, 0, 1443500056);
            guiGraphics.fill(0, -3, 98, -2, -2838729);
            guiGraphics.fill(98, -4, 136, -2, -10690074);
            DestinyHudRenderer.drawDiamond(guiGraphics, 4, -8, -10690074);
            String string10 = string.concat(" [30]");
            guiGraphics.drawString(font, string10, 11, -30, -1380619, true);
            DestinyHudRenderer.drawScaledText(guiGraphics, font, Integer.toString(n6), 12, -19, 1.5f, n4);
            if (!weaponSpec.usesSpecialAmmo) {
                DestinyHudRenderer.drawInfinityGlyph(guiGraphics, 54, -15, -2838729);
            } else {
                String string11 = "/ ".concat(Integer.toString(n5));
                guiGraphics.drawString(font, string11, 52, -15, -10690074, true);
            }
        }
        poseStack.popPose();
    }

    public static void renderActivePerkBanner(GuiGraphics guiGraphics, Font font, ItemStack itemStack, WeaponSpec weaponSpec, long l, int n, int n2) {
        block9: {
            int n3;
            String string;
            block5: {
                int n4;
                block8: {
                    block7: {
                        block6: {
                            block4: {
                                string = null;
                                n3 = -2047399;
                                if (GunDataHelper.getLong(itemStack, "d2_kc_exp") <= l) break block4;
                                string = "KILL CLIP (+25%)";
                                n3 = -9910798;
                                break block5;
                            }
                            if (GunDataHelper.getLong(itemStack, "d2_rmp_exp") <= l || (n4 = GunDataHelper.getInt(itemStack, "d2_rmp_stk", 0)) <= 0) break block6;
                            string = "RAMPAGE x".concat(Integer.toString(n4));
                            n3 = -886475;
                            break block5;
                        }
                        if (GunDataHelper.getLong(itemStack, "d2_rh_exp") <= l || (n4 = GunDataHelper.getInt(itemStack, "d2_rh_stk", 0)) <= 0) break block7;
                        string = "RAPID HIT x".concat(Integer.toString(n4));
                        break block5;
                    }
                    if (GunDataHelper.getLong(itemStack, "d2_outlaw") <= l) break block8;
                    string = "OUTLAW READY";
                    break block5;
                }
                n4 = GunDataHelper.getInt(itemStack, "d2_pstack", 0);
                if (n4 <= 0) break block9;
                string = DestinyHudRenderer.trimName(weaponSpec.perkTitle, 14).concat(" x").concat(Integer.toString(n4));
            }
            if (string != null) {
                int n5 = font.width(string) + 16;
                guiGraphics.fill(n, n2, n + n5, n2 + 12, -870703064);
                DestinyHudRenderer.drawDiamond(guiGraphics, n + 5, n2 + 6, n3);
                guiGraphics.drawString(font, string, n + 12, n2 + 2, n3, true);
            }
        }
    }
}

