package com.jax.destiny2mc.client;

import com.jax.destiny2mc.client.gunfeel.SniperScopeOverlay;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.GunDataHelper;
import com.jax.destiny2mc.weapon.WeaponPerkRegistry;
import com.jax.destiny2mc.weapon.WeaponSpec;
import com.mojang.blaze3d.systems.RenderSystem;
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
    public static final ResourceLocation SIL_AUTO = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/silhouettes/sil_auto.png");
    public static final ResourceLocation SIL_HC = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/silhouettes/sil_hc.png");
    public static final ResourceLocation SIL_SNIPER = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/silhouettes/sil_sniper.png");
    public static final ResourceLocation SIL_SHOTGUN = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/silhouettes/sil_shotgun.png");

    private static final ResourceLocation AMMO_PRIMARY = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/hud/ammo_primary.png");
    private static final ResourceLocation AMMO_SPECIAL = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/hud/ammo_special.png");
    private static final ResourceLocation AMMO_HEAVY = ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/hud/ammo_heavy.png");

    public static ResourceLocation getSilhouette(int n) {
        String icon = switch (n) {
            case 1 -> "hc";
            case 2 -> "sniper";
            case 3 -> "shotgun";
            case 4 -> "fusion";
            case 5 -> "linear";
            case 6 -> "trace";
            case 7 -> "rocket";
            case 8 -> "grenade";
            case 9 -> "bow";
            default -> "auto";
        };
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/silhouettes/sil_" + icon + ".png");
    }

    public static ResourceLocation getSilhouette(WeaponSpec spec) {
        String icon = switch (spec.id) {
            case "outbreak_perfected", "graviton_lance", "oversoul_edict", "bxr_55_battler" -> "pulse";
            case "tarnished_mettle", "total_lockdown", "fang_of_ir_yut", "vision_of_confluence", "dead_mans_tale", "hung_jury_sr4" -> "scout";
            case "blood_feud", "riskrunner", "the_recluse" -> "smg";
            case "brigands_law", "forerunner" -> "sidearm";
            case "song_of_ir_yut", "hammerhead" -> "mg";
            case "subjugator_glaive" -> "glaive";
            default -> switch (spec.archetype) {
                case 1 -> "hc";
                case 2 -> "sniper";
                case 3 -> "shotgun";
                case 4 -> "fusion";
                case 5 -> "linear";
                case 6 -> "trace";
                case 7 -> "rocket";
                case 8 -> "grenade";
                case 9 -> "bow";
                default -> "auto";
            };
        };
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/silhouettes/sil_" + icon + ".png");
    }

    public static ResourceLocation getElementIcon(int element) {
        String name = switch (element) {
            case 1 -> "solar";
            case 2 -> "arc";
            case 3 -> "void";
            case 4 -> "stasis";
            default -> "kinetic";
        };
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", "textures/gui/hud/elem_" + name + ".png");
    }

    private static boolean isHeavyWeapon(WeaponSpec spec) {
        return spec.archetype == 7
                || spec.id.equals("whisper_of_the_worm")
                || spec.id.equals("darci")
                || spec.id.equals("sleeper_simulant")
                || spec.id.equals("taipan_4fr")
                || spec.id.equals("song_of_ir_yut")
                || spec.id.equals("hammerhead")
                || spec.id.equals("tractor_cannon");
    }

    private static void drawTintedIcon(GuiGraphics gui, ResourceLocation tex, int x, int y, int size, int argb) {
        float a = ((argb >>> 24) & 0xFF) / 255.0f;
        float r = ((argb >>> 16) & 0xFF) / 255.0f;
        float g = ((argb >>> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        gui.setColor(r, g, b, a);
        gui.blit(tex, x, y, 0.0f, 0.0f, size, size, size, size);
        gui.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static int getEffectiveElement(ItemStack itemStack, WeaponSpec weaponSpec) {
        if (weaponSpec.perkId == 18 && GunDataHelper.getMag(itemStack, weaponSpec) == 1) {
            return 1;
        }
        return weaponSpec.element;
    }

    public static int getElementColor(int n) {
        return switch (n) {
            case 1 -> -886475;
            case 2 -> -9910798;
            case 3 -> -5143355;
            case 4 -> -11695873;
            case 5 -> -12388994;
            default -> -723720;
        };
    }

    public static String getElementTag(int n) {
        return switch (n) {
            case 1 -> "SOL";
            case 2 -> "ARC";
            case 3 -> "VOID";
            case 4 -> "STA";
            case 5 -> "TOX";
            default -> "KIN";
        };
    }

    public static String getBl4ModeLabel(ItemStack itemStack, WeaponSpec weaponSpec) {
        if (weaponSpec.perkId == 5 && GunDataHelper.getInt(itemStack, "d2_honed", 0) > 1) return "Honed Edge";
        if (weaponSpec.perkId == 20 && GunDataHelper.getInt(itemStack, "d2_alt", 0) > 0) return "Wolves Mode";
        if (weaponSpec.perkId == 36 && GunDataHelper.getInt(itemStack, "d2_alt", 0) > 0) return "Linear Fusion";
        return switch (weaponSpec.archetype) {
            case 1 -> "Hand Cannon";
            case 2 -> "Sniper Rifle";
            case 3 -> "Shotgun";
            case 4 -> "Fusion Rifle";
            case 5 -> "Linear Fusion";
            case 6 -> "Trace Rifle";
            case 7 -> "Rocket Launcher";
            case 8 -> "Grenade Launcher";
            case 9 -> "Combat Bow";
            default -> "Automatic";
        };
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
        poseStack.translate((float) n, (float) n2, 0.0f);
        poseStack.scale(f, f, 1.0f);
        guiGraphics.drawString(font, string, 0, 0, n3, true);
        poseStack.popPose();
    }

    public static void drawOutlinedScaledText(GuiGraphics guiGraphics, Font font, String string, int n, int n2, float f, int n3, int n4) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((float) n, (float) n2, 0.0f);
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
                double d = (double) ClientWeaponState.POP_X[i] - localPlayer.getX();
                double d2 = (double) ClientWeaponState.POP_Y[i] - localPlayer.getEyeY();
                double d3 = (double) ClientWeaponState.POP_Z[i] - localPlayer.getZ();
                double d4 = Math.toRadians(localPlayer.getYRot());
                double d5 = -(d * Math.cos(d4) + d3 * Math.sin(d4));
                double d6 = d3 * Math.cos(d4) - d * Math.sin(d4);
                if (d6 <= 0.5) continue;
                int n5 = n + (int) (d5 / d6 * 140.0) + ((i & 3) * 6 - 9);
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
            WeaponSpec weaponSpec = ((DestinyGunItem) itemStack.getItem()).spec;
            int n = guiGraphics.guiWidth();
            int n2 = guiGraphics.guiHeight();
            int n3 = n / 2;
            int n4 = n2 / 2;
            float f2 = ClientWeaponState.getSmoothAds(f);
            int n5 = minecraft.crosshairPickEntity instanceof LivingEntity ? -2079174 : -285212673;

            if (SniperScopeOverlay.renderIfScoped(guiGraphics, itemStack, f2)) {
                DestinyHudRenderer.renderHitmarker(guiGraphics, n3, n4);
                DestinyHudRenderer.renderFloatingNumbers(guiGraphics, minecraft, n3, n4);
                return;
            }

            if (f2 > .72f) {
                if (weaponSpec.id.equals("darci") && minecraft.options.getCameraType().isFirstPerson()) DarciOptic.draw(guiGraphics, n3, n4);
                guiGraphics.fill(n3, n4, n3 + 1, n4 + 1, n5);
            } else {
                int gap = (int) ((1 - f2 * .72f) * (5 + ClientWeaponState.bloom * 9));
                guiGraphics.fill(n3, n4, n3 + 1, n4 + 1, n5);
                guiGraphics.fill(n3 - gap - 4, n4, n3 - gap, n4 + 1, n5);
                guiGraphics.fill(n3 + gap + 1, n4, n3 + gap + 5, n4 + 1, n5);
                guiGraphics.fill(n3, n4 - gap - 4, n3 + 1, n4 - gap, n5);
                guiGraphics.fill(n3, n4 + gap + 1, n3 + 1, n4 + gap + 5, n5);
            }
            DestinyHudRenderer.renderHitmarker(guiGraphics, n3, n4);
            DestinyHudRenderer.renderFloatingNumbers(guiGraphics, minecraft, n3, n4);
        }
    }

    public static void renderStrikeObjectiveHud(GuiGraphics guiGraphics, Font font) {
        // Compatibility entry point only.
    }

    public static void renderWeaponTelemetryHud(GuiGraphics guiGraphics, float f) {
        com.jax.destiny2mc.binder.client.BinderHud.render(guiGraphics, f);
    }

    public static void renderWeaponCardAt(GuiGraphics guiGraphics, Font font, ItemStack itemStack, WeaponSpec weaponSpec, long l, float f, int n, int n2, float f2, int n3) {
        int effElem = DestinyHudRenderer.getEffectiveElement(itemStack, weaponSpec);
        int elemColor = DestinyHudRenderer.getElementColor(effElem);
        int rarityColor = weaponSpec.isExotic ? -2047399 : -7713086;
        int mag = GunDataHelper.getMag(itemStack, weaponSpec);
        int reserve = GunDataHelper.getReserve(itemStack, weaponSpec);
        int maxMag = WeaponPerkRegistry.getEffectiveMagSize(itemStack, weaponSpec);
        int magColor = mag * 4 <= maxMag ? -1561558 : -1;
        float reloadFrac = -1.0f;
        long relEnd = GunDataHelper.getLong(itemStack, "d2_rel_end");
        if (relEnd > l) {
            float relDur = GunDataHelper.getInt(itemStack, "d2_rel_dur", 36);
            reloadFrac = 1.0f - ((float) (relEnd - l) - f) / relDur;
            if (reloadFrac < 0.0f) reloadFrac = 0.0f;
            if (reloadFrac > 1.0f) reloadFrac = 1.0f;
        }
        String name = DestinyHudRenderer.trimName(weaponSpec.displayName, 17);
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((float) n, (float) n2, 0.0f);
        poseStack.scale(f2, f2, 1.0f);
        if (HudConfig.showPerks) {
            DestinyHudRenderer.renderActivePerkBanner(guiGraphics, font, itemStack, weaponSpec, l, 0, -66);
        }

        if (n3 == 0) {
            guiGraphics.fill(0, -30, 134, 0, 1846547740);
            guiGraphics.fill(0, -30, 134, -29, 0x38FFFFFF);
            guiGraphics.fill(0, -1, 134, 0, 0x38FFFFFF);
            RenderSystem.enableBlend();
            guiGraphics.blit(DestinyHudRenderer.getSilhouette(weaponSpec), 4, -27, 0.0f, 0.0f, 54, 20, 54, 20);
            String magStr = Integer.toString(mag);
            int magX = 95 - (int) ((float) font.width(magStr) * 1.85f);
            DestinyHudRenderer.drawScaledText(guiGraphics, font, magStr, magX, -23, 1.85f, magColor);
            guiGraphics.fill(100, -27, 101, -3, 0x66FFFFFF);
            boolean heavy = isHeavyWeapon(weaponSpec);
            if (!weaponSpec.usesSpecialAmmo) {
                DestinyHudRenderer.drawInfinityGlyph(guiGraphics, 106, -24, -591617);
                guiGraphics.fill(132, -30, 134, 0, -1);
                drawTintedIcon(guiGraphics, AMMO_PRIMARY, 118, -14, 10, 0xFFEAEAEA);
            } else {
                guiGraphics.drawString(font, Integer.toString(reserve), 105, -25, -591617, true);
                int ammoStrip = heavy ? 0xFFB066FF : -11870616;
                guiGraphics.fill(132, -30, 134, 0, ammoStrip);
                drawTintedIcon(guiGraphics, heavy ? AMMO_HEAVY : AMMO_SPECIAL, 118, -14, 10, heavy ? 0xFFC58CFF : 0xFF7BE88C);
            }
            drawTintedIcon(guiGraphics, DestinyHudRenderer.getElementIcon(effElem), 104, -14, 10, elemColor);
            if (!(reloadFrac < 0.0f)) {
                int bar = (int) (reloadFrac * 132.0f);
                guiGraphics.fill(0, -2, bar, 0, elemColor);
            }
        } else if (n3 == 1) {
            guiGraphics.fill(0, -34, 128, 0, -285212672);
            guiGraphics.fill(1, -33, 127, -1, -585168100);
            guiGraphics.fill(1, -33, 127, -32, -1433695349);
            guiGraphics.drawString(font, name, 5, -31, rarityColor, true);
            guiGraphics.renderFakeItem(itemStack, 5, -20);
            DestinyHudRenderer.drawScaledText(guiGraphics, font, Integer.toString(mag), 27, -19, 1.35f, magColor);
            if (!weaponSpec.usesSpecialAmmo) {
                DestinyHudRenderer.drawInfinityGlyph(guiGraphics, 70, -16, -2236963);
            } else {
                guiGraphics.drawString(font, "/ ".concat(Integer.toString(reserve)), 64, -17, -11141291, true);
            }
        } else if (n3 == 2) {
            guiGraphics.fill(0, -18, 3, -5, elemColor);
            guiGraphics.drawString(font, name, 0, -28, -4471856, true);
            DestinyHudRenderer.drawScaledText(guiGraphics, font, Integer.toString(mag), 7, -18, 1.45f, magColor);
            if (!weaponSpec.usesSpecialAmmo) {
                DestinyHudRenderer.drawInfinityGlyph(guiGraphics, 44, -14, -2236963);
            } else {
                guiGraphics.drawString(font, "/ ".concat(Integer.toString(reserve)), 42, -15, -7342680, true);
            }
        } else if (n3 == 3) {
            int bar = !(reloadFrac < 0.0f) ? (int) (reloadFrac * 118.0f) : mag * 118 / maxMag;
            DestinyHudRenderer.drawSkewedBar(guiGraphics, 2, -20, 122, 17, -16118768, -10982226, -7625238, bar, 1010296831);
            String ammoStr = Integer.toString(mag).concat(" / ").concat(!weaponSpec.usesSpecialAmmo ? Integer.toString(maxMag * 4) : Integer.toString(reserve));
            int tx = 118 - (int) ((float) font.width(ammoStr) * 1.18f);
            DestinyHudRenderer.drawOutlinedScaledText(guiGraphics, font, ammoStr, tx, -16, 1.18f, magColor, -16118768);
            DestinyHudRenderer.drawBl2Icons(guiGraphics, 130, -19);
        } else if (n3 == 4) {
            guiGraphics.fillGradient(0, -52, 142, -20, -301459422, -283951536);
            guiGraphics.renderOutline(0, -52, 142, 32, -862407484);
            guiGraphics.renderOutline(2, -50, 138, 28, 1715258596);
            guiGraphics.fill(3, -55, 10, -53, -34280);
            guiGraphics.fill(3, -19, 10, -17, -34280);
            guiGraphics.fill(18, -50, 54, -48, -723724);
            guiGraphics.fill(18, -22, 54, -20, -723724);
            String magStr = Integer.toString(mag);
            int c = magColor == -1 ? -658714 : magColor;
            int tx = 36 - (int) ((float) font.width(magStr) * 0.95f);
            DestinyHudRenderer.drawScaledText(guiGraphics, font, magStr, tx, -43, 1.9f, c);
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
                DestinyHudRenderer.drawScaledText(guiGraphics, font, Integer.toString(reserve), 88, -42, 1.35f, -4461834);
            }
            guiGraphics.fill(0, -15, 78, -1, -586607845);
            guiGraphics.drawString(font, DestinyHudRenderer.getBl4ModeLabel(itemStack, weaponSpec), 5, -12, -2823950, true);
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
        } else {
            guiGraphics.fill(0, -32, 136, 0, 1443500056);
            guiGraphics.fill(0, -3, 98, -2, -2838729);
            guiGraphics.fill(98, -4, 136, -2, -10690074);
            DestinyHudRenderer.drawDiamond(guiGraphics, 4, -8, -10690074);
            guiGraphics.drawString(font, name.concat(" [30]"), 11, -30, -1380619, true);
            DestinyHudRenderer.drawScaledText(guiGraphics, font, Integer.toString(mag), 12, -19, 1.5f, magColor);
            if (!weaponSpec.usesSpecialAmmo) {
                DestinyHudRenderer.drawInfinityGlyph(guiGraphics, 54, -15, -2838729);
            } else {
                guiGraphics.drawString(font, "/ ".concat(Integer.toString(reserve)), 52, -15, -10690074, true);
            }
        }
        poseStack.popPose();
    }

    public static void renderActivePerkBanner(GuiGraphics guiGraphics, Font font, ItemStack itemStack, WeaponSpec weaponSpec, long l, int n, int n2) {
        String label = null;
        int color = -2047399;
        if (GunDataHelper.getLong(itemStack, "d2_kc_exp") > l) {
            label = "KILL CLIP (+25%)";
            color = -9910798;
        } else if (GunDataHelper.getLong(itemStack, "d2_rmp_exp") > l && GunDataHelper.getInt(itemStack, "d2_rmp_stk", 0) > 0) {
            label = "RAMPAGE x" + GunDataHelper.getInt(itemStack, "d2_rmp_stk", 0);
            color = -886475;
        } else if (GunDataHelper.getLong(itemStack, "d2_rh_exp") > l && GunDataHelper.getInt(itemStack, "d2_rh_stk", 0) > 0) {
            label = "RAPID HIT x" + GunDataHelper.getInt(itemStack, "d2_rh_stk", 0);
        } else if (GunDataHelper.getLong(itemStack, "d2_outlaw") > l) {
            label = "OUTLAW READY";
        } else {
            int stacks = GunDataHelper.getInt(itemStack, "d2_pstack", 0);
            if (stacks > 0) {
                label = DestinyHudRenderer.trimName(weaponSpec.perkTitle, 14) + " x" + stacks;
            }
        }
        if (label != null) {
            int w = font.width(label) + 16;
            guiGraphics.fill(n, n2, n + w, n2 + 12, -870703064);
            DestinyHudRenderer.drawDiamond(guiGraphics, n + 5, n2 + 6, color);
            guiGraphics.drawString(font, label, n + 12, n2 + 2, color, true);
        }
    }
}
