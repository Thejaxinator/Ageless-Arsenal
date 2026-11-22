package com.jax.destiny2mc.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@SuppressWarnings("rawtypes")
public class FallenEntityRendererProvider implements EntityRendererProvider {
    private final int kind;

    public FallenEntityRendererProvider(int kind) {
        this.kind = kind;
    }

    @Override public EntityRenderer create(EntityRendererProvider.Context context) {
        return switch (kind) {
            case 0, 1, 2, 3, 4, 5, 6 -> new FallenStudyRenderer(context, kind);
            default -> kind < 10 ? new FallenMobRenderer(context, kind)
                    : new ThrownItemRenderer<>(context, kind == 14 ? 1.45F : 1.15F, true);
        };
    }
}
