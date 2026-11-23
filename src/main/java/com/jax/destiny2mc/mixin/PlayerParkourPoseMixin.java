package com.jax.destiny2mc.mixin;

import com.jax.destiny2mc.tenno.parkour.ParkourClientState;
import com.jax.destiny2mc.tenno.parkour.ParkourPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerParkourPoseMixin<T extends LivingEntity> extends HumanoidModel<T> {
    @Shadow @Final public ModelPart leftSleeve;
    @Shadow @Final public ModelPart rightSleeve;
    @Shadow @Final public ModelPart leftPants;
    @Shadow @Final public ModelPart rightPants;
    @Shadow @Final public ModelPart jacket;

    protected PlayerParkourPoseMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void ageless$applyParkourPose(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (!(entity instanceof Player player)) {
            return;
        }
        ParkourClientState.Entry state = ParkourClientState.get(player.getUUID());
        if (state == null || state.pose == ParkourPose.NONE || state.blend <= 0.01f) {
            return;
        }

        float b = state.blend;
        switch (state.pose) {
            case BULLET_JUMP -> {
                float spin = (state.poseTicks * 0.65f) % Mth.TWO_PI;
                this.body.xRot = Mth.lerp(b, this.body.xRot, 0.85f);
                this.body.zRot = Mth.lerp(b, this.body.zRot, Mth.sin(spin) * 0.35f);
                this.rightArm.xRot = Mth.lerp(b, this.rightArm.xRot, -2.2f);
                this.leftArm.xRot = Mth.lerp(b, this.leftArm.xRot, -1.9f);
                this.rightLeg.xRot = Mth.lerp(b, this.rightLeg.xRot, 0.65f);
                this.leftLeg.xRot = Mth.lerp(b, this.leftLeg.xRot, 0.95f);
            }
            case DOUBLE_JUMP, WALL_HOP -> {
                this.body.xRot = Mth.lerp(b, this.body.xRot, 0.35f);
                this.rightLeg.xRot = Mth.lerp(b, this.rightLeg.xRot, -0.85f);
                this.leftLeg.xRot = Mth.lerp(b, this.leftLeg.xRot, 0.55f);
                this.rightArm.xRot = Mth.lerp(b, this.rightArm.xRot, -0.7f);
                this.leftArm.xRot = Mth.lerp(b, this.leftArm.xRot, 0.45f);
            }
            case AIM_GLIDE -> {
                this.body.xRot = Mth.lerp(b, this.body.xRot, 0.25f);
                this.rightArm.xRot = Mth.lerp(b, this.rightArm.xRot, -1.35f + headPitch * Mth.DEG_TO_RAD);
                this.leftArm.xRot = Mth.lerp(b, this.leftArm.xRot, -1.15f + headPitch * Mth.DEG_TO_RAD);
                this.rightLeg.xRot = Mth.lerp(b, this.rightLeg.xRot, 0.35f);
                this.leftLeg.xRot = Mth.lerp(b, this.leftLeg.xRot, -0.25f);
            }
            case WALL_LATCH -> {
                this.body.xRot = Mth.lerp(b, this.body.xRot, 0.15f);
                this.leftArm.xRot = Mth.lerp(b, this.leftArm.xRot, -1.8f);
                this.leftArm.zRot = Mth.lerp(b, this.leftArm.zRot, -0.35f);
                this.rightLeg.xRot = Mth.lerp(b, this.rightLeg.xRot, -0.65f);
                this.leftLeg.xRot = Mth.lerp(b, this.leftLeg.xRot, 0.3f);
            }
            case SLIDE -> {
                this.body.xRot = Mth.lerp(b, this.body.xRot, -0.45f);
                this.rightLeg.xRot = Mth.lerp(b, this.rightLeg.xRot, -1.15f);
                this.leftLeg.xRot = Mth.lerp(b, this.leftLeg.xRot, 0.75f);
                this.rightArm.xRot = Mth.lerp(b, this.rightArm.xRot, 0.4f);
                this.leftArm.xRot = Mth.lerp(b, this.leftArm.xRot, -0.6f);
            }
            case ROLL -> {
                float rollAngle = Math.min(Mth.TWO_PI, state.poseTicks * 0.62f);
                this.body.xRot = Mth.lerp(b, this.body.xRot, rollAngle);
                this.rightLeg.xRot = Mth.lerp(b, this.rightLeg.xRot, 1.1f);
                this.leftLeg.xRot = Mth.lerp(b, this.leftLeg.xRot, 1.1f);
                this.rightArm.xRot = Mth.lerp(b, this.rightArm.xRot, -1.2f);
                this.leftArm.xRot = Mth.lerp(b, this.leftArm.xRot, -1.2f);
            }
            case NONE -> {
            }
        }

        this.jacket.copyFrom(this.body);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
    }
}
