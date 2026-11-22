package com.jax.destiny2mc.client;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
@SuppressWarnings("rawtypes")
public class V200LegacyFactionRendererProvider implements EntityRendererProvider {
    private final int kind;
    public V200LegacyFactionRendererProvider(int kind){this.kind=kind;}
    @Override public EntityRenderer create(EntityRendererProvider.Context context) {
        if(kind>=16 && kind<=20) return new FallenStudyRenderer(context,kind+89);
        return new OriginalFactionRendererProvider(kind).create(context);
    }
}
