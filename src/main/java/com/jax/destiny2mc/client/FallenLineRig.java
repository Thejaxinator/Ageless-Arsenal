package com.jax.destiny2mc.client;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import com.jax.destiny2mc.client.model.*;
/** Project and render-slot IDs are separate from server entity kind IDs. */
record FallenLineRig(String id,float rankScale,float shadow) {
    static FallenLineRig spec(int kind) {
        return switch(kind) {
            case 0 -> new FallenLineRig("fallen_dreg",0.98F,0.5F);
            case 1 -> new FallenLineRig("fallen_wretch",0.98F,0.5F);
            case 2 -> new FallenLineRig("fallen_vandal_sniper",1.06F,0.5F);
            case 3 -> new FallenLineRig("fallen_captain_rifle",1.24F,0.5F);
            case 4 -> new FallenLineRig("fallen_captain_scorch",1.24F,0.5F);
            case 5 -> new FallenLineRig("fallen_shank",0.98F,0.5F);
            case 6 -> new FallenLineRig("fallen_bruiser",1.16F,0.5F);
            case 101 -> new FallenLineRig("devil_walker",1.45F,1.25F);
            case 102 -> new FallenLineRig("sepiks_prime",1.4F,0.95F);
            case 103 -> new FallenLineRig("blast_servitor",0.82F,0.95F);
            case 104 -> new FallenLineRig("vanok_arclite",1.25F,0.65F);
            case 105 -> new FallenLineRig("exploder_shank",1F,0.375F);
            case 106 -> new FallenLineRig("tracer_shank",1F,0.375F);
            case 107 -> new FallenLineRig("heavy_shank",1.55F,0.625F);
            case 108 -> new FallenLineRig("devils_servitor",1.25F,0.675F);
            case 109 -> new FallenLineRig("archon_priest",1.42F,0.525F);
            case 110 -> new FallenLineRig("taniks",1F,0.65F);
            default -> throw new IllegalArgumentException("Unknown Fallen kind "+kind);
        };
    }
    static LayerDefinition geometry(int kind) {
        return switch(kind) {
            case 0 -> ConvertedRig0.create();
            case 1 -> ConvertedRig1.create();
            case 2 -> ConvertedRig2.create();
            case 3 -> ConvertedRig3.create();
            case 4 -> ConvertedRig4.create();
            case 5 -> ConvertedRig5.create();
            case 6 -> ConvertedRig6.create();
            case 101 -> ConvertedRig201.create();
            case 102 -> ConvertedRig202.create();
            case 103 -> ConvertedRig203.create();
            case 104 -> ConvertedRig204.create();
            case 105 -> ConvertedRig205.create();
            case 106 -> ConvertedRig206.create();
            case 107 -> ConvertedRig207.create();
            case 108 -> ConvertedRig208.create();
            case 109 -> ConvertedRig209.create();
            case 110 -> ConvertedRig210.create();
            default -> throw new IllegalArgumentException("Unknown Fallen kind "+kind);
        };
    }
}
