package com.jax.destiny2mc.binder;

import java.util.List;
import java.util.Set;
import java.util.Map;

/** Private owner-only view. Deadlines are absolute game times so no per-tick network updates are needed. */
public record BinderSnapshot(Set<String> unlocked,List<String> active,List<String> passive,
                             Map<String,Long> readyAt,int motes,int levels,String message) {
    public BinderSnapshot {
        unlocked=Set.copyOf(unlocked);active=List.copyOf(active);passive=List.copyOf(passive);readyAt=Map.copyOf(readyAt);
        if (active.size()!=4 || passive.size()!=2) throw new IllegalArgumentException("Invalid slot count");
    }
    static final BinderSnapshot EMPTY=new BinderSnapshot(Set.of(),List.of("","","",""),List.of("",""),Map.of(),0,0,"");
}
