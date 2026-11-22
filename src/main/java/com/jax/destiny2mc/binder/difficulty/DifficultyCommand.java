package com.jax.destiny2mc.binder.difficulty;

import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="destiny2mc")
public final class DifficultyCommand {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("ageless").then(Commands.literal("difficulty").executes(c->{
            DifficultyNetwork.sync(c.getSource().getPlayerOrException(),true);return 1;
        })));
    }
}
