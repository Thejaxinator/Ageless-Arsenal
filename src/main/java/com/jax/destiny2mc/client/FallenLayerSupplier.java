package com.jax.destiny2mc.client;

import java.util.function.Supplier;
import net.minecraft.client.model.geom.builders.LayerDefinition;

/** The existing seven layer locations remain the entry points for resource reloads. */
public class FallenLayerSupplier implements Supplier<LayerDefinition> {
    private final int kind;

    public FallenLayerSupplier(int kind) {
        this.kind = kind;
    }

    @Override public LayerDefinition get() {
        return FallenLineRig.geometry(kind);
    }
}
