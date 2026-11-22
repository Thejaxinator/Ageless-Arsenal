package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.tenno.TennoGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The paired secondary follows the off-side bone in third person and in inventory.
 * If the player carries an actual offhand item its vanilla layer has priority. */
public final class TennoTwinLayer extends ItemInHandLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private TennoTwinLayer(PlayerRenderer player,ItemInHandRenderer items){super(player,items);}
    @EventBusSubscriber(modid="destiny2mc",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void add(EntityRenderersEvent.AddLayers event){
            for(var skin:event.getSkins()){
                if(event.getSkin(skin) instanceof PlayerRenderer player)
                    player.addLayer(new TennoTwinLayer(player,event.getContext().getItemInHandRenderer()));
            }
        }
    }
    @Override public void render(PoseStack pose,MultiBufferSource out,int light,AbstractClientPlayer player,
         float limb,float speed,float partial,float age,float yaw,float pitch){
        var stack=player.getMainHandItem();
        if(!player.getOffhandItem().isEmpty()||!(stack.getItem() instanceof TennoGunItem gun)||!gun.profile().dual())return;
        var hand=player.getMainArm().getOpposite();
        var context=hand==HumanoidArm.RIGHT?ItemDisplayContext.THIRD_PERSON_RIGHT_HAND:ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        renderArmWithItem(player,stack,context,hand,pose,out,light);
    }
}
