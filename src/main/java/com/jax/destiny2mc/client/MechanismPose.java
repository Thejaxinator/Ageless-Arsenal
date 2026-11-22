package com.jax.destiny2mc.client;

/** Normalized action channels from a single player's weapon; rotor angle is in degrees. */
public record MechanismPose(float reload,float stroke,float cycle,float charge,float rotorAngle) {}
