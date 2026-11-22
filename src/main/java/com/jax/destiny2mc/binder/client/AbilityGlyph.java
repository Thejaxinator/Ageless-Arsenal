package com.jax.destiny2mc.binder.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/** A stable power ID addresses an individually drawn, resource-pack replaceable GUI sprite. */
public final class AbilityGlyph {
    private static final String DIRECTORY = "binder/ability/";
    private static final ResourceLocation EMPTY = sprite("unbound");
    private static final ResourceLocation UNKNOWN = sprite("unidentified");
    // Cache names rather than sprites, since Minecraft can rebuild the GUI atlas on resource reload.
    private static final Map<String, ResourceLocation> names = new HashMap<>();

    private AbilityGlyph() {}

    private static ResourceLocation sprite(String id) {
        return ResourceLocation.fromNamespaceAndPath("destiny2mc", DIRECTORY + id);
    }

    public static void draw(GuiGraphics graphics, String abilityId, int x, int y, int size, int argb) {
        if (size < 1 || argb >>> 24 == 0) return;
        ResourceLocation name = abilityId == null || abilityId.isEmpty()
                ? EMPTY : names.computeIfAbsent(abilityId, id -> {
                    var path = ResourceLocation.tryBuild("destiny2mc", DIRECTORY + id);
                    return path == null ? UNKNOWN : path;
                });
        var atlas = Minecraft.getInstance().getGuiSprites();
        var icon = atlas.getSprite(name);
        if (icon.contents().name().equals(MissingTextureAtlasSprite.getLocation())) icon = atlas.getSprite(UNKNOWN);
        // Vertex coloring leaves the surrounding HUD's shader color and GUI pose intact.
        graphics.blit(x, y, 0, size, size, icon,
                ((argb >>> 16) & 255) / 255f, ((argb >>> 8) & 255) / 255f,
                (argb & 255) / 255f, (argb >>> 24) / 255f);
    }
}
