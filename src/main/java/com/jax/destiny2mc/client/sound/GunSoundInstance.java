package com.jax.destiny2mc.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

/**
 * TACZ-inspired gun sound instance with custom squared long-range distance falloff
 * (bypassing Minecraft's 16-block attenuation cap) and relative first-person playback.
 */
public final class GunSoundInstance extends AbstractSoundInstance {
    private final boolean canPlay;

    public GunSoundInstance(SoundEvent event, SoundSource source, float volume, float pitch,
                            Entity entity, int maxDistance, boolean relative) {
        super(event, source, RandomSource.create());
        this.attenuation = Attenuation.NONE;
        this.relative = relative;
        this.canPlay = !entity.isSilent();
        this.pitch = Math.max(0.05f, pitch);
        this.x = relative ? 0.0 : entity.getX();
        this.y = relative ? 0.0 : entity.getEyeY();
        this.z = relative ? 0.0 : entity.getZ();

        float resolvedVolume = volume;
        LocalPlayer listener = Minecraft.getInstance().player;
        if (listener != null && !relative && maxDistance > 0) {
            float dist = (float) Math.sqrt(listener.distanceToSqr(this.x, this.y, this.z));
            float factor = 1.0f - Math.min(1.0f, dist / maxDistance);
            resolvedVolume = volume * factor * factor;
        }
        this.volume = Math.max(0.0f, resolvedVolume);
    }

    public void stopNow() {
        Minecraft.getInstance().getSoundManager().stop(this);
    }

    @Override
    public boolean canPlaySound() {
        return this.canPlay && this.volume > 0.002f;
    }
}
