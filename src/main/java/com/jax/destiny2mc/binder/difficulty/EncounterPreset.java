package com.jax.destiny2mc.binder.difficulty;

/** Hit-point caps are applied before armor and Protection. Keys also form the save and wire format. */
public enum EncounterPreset {
    EXPLORER("explorer","Explorer", .24f,.28f,3,4,.50f),
    PATROL("patrol","Patrol", .38f,.42f,4,6,.65f),
    VETERAN("veteran","Veteran", .68f,.72f,7,10,.85f),
    ORIGINAL("original","Original", 1,1,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,1);

    public final String id,title;
    public final float rankScale,bossScale,rankCap,bossCap,followUp;
    EncounterPreset(String id,String title,float rankScale,float bossScale,float rankCap,float bossCap,float followUp){
        this.id=id; this.title=title; this.rankScale=rankScale; this.bossScale=bossScale;
        this.rankCap=rankCap; this.bossCap=bossCap; this.followUp=followUp;
    }
    public static EncounterPreset find(String id){
        for(var profile:values()) if(profile.id.equals(id))return profile;
        return null;
    }
    public float incoming(float original, boolean boss, boolean underFire){
        float reduced=Math.min(original*(boss?bossScale:rankScale),boss?bossCap:rankCap);
        return underFire?reduced*followUp:reduced;
    }
}
