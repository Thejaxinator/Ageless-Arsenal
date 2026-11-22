package com.jax.destiny2mc.client;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
/** All four registrations use their own native rig and unique texture atlas. */
@SuppressWarnings("rawtypes")
public class StrikeBossRendererProvider implements EntityRendererProvider {
    private final int bossKind;
    public StrikeBossRendererProvider(int bossKind){this.bossKind=bossKind;}
    @Override public EntityRenderer create(EntityRendererProvider.Context context) {
        return new FallenStudyRenderer(context,101+bossKind);
    }
}
