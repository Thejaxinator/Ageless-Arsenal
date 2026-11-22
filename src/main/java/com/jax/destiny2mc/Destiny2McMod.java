/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.fml.loading.FMLEnvironment
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.jax.destiny2mc;

import com.jax.destiny2mc.CommonForgeEvents;
import com.jax.destiny2mc.ModBusEvents;
import com.jax.destiny2mc.ModRegistries;
import com.jax.destiny2mc.client.ClientForgeEvents;
import com.jax.destiny2mc.client.ClientModEvents;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value="destiny2mc")
public class Destiny2McMod {
    public Destiny2McMod(IEventBus iEventBus) {
        ModRegistries.BLOCKS.register(iEventBus);
        ModRegistries.ITEMS.register(iEventBus);
        ModRegistries.BLOCK_ENTITIES.register(iEventBus);
        ModRegistries.ENTITIES.register(iEventBus);
        ModRegistries.TABS.register(iEventBus);
        iEventBus.register(ModBusEvents.class);
        NeoForge.EVENT_BUS.register(CommonForgeEvents.class);
        
        if (FMLEnvironment.dist == Dist.CLIENT) {
            iEventBus.register(ClientModEvents.class);
            NeoForge.EVENT_BUS.register(ClientForgeEvents.class);
        }
    }
}

