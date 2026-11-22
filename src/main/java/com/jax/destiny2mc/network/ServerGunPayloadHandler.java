package com.jax.destiny2mc.network;
import com.jax.destiny2mc.frontier.FrontierBallistics;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.*;
public class ServerGunPayloadHandler implements IPayloadHandler<GunActionPayload> {
 @Override public void handle(GunActionPayload data,IPayloadContext context){context.enqueueWork(()->{if(context.player() instanceof ServerPlayer p)FrontierBallistics.action(p,data.action,data.yaw,data.pitch,data.ads);});}
}
