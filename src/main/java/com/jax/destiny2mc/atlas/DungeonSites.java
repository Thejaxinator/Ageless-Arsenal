package com.jax.destiny2mc.atlas;
import java.util.*;
/** Small permanent places in the Overworld. No instances or player teleportation. */
public final class DungeonSites {
 public record Site(String id,String name,String boss,String origin,String habitat,int color){}
 public static final List<Site> ALL=List.of(
  new Site("petition_court","Court of Petitions","vorgeth","The Shattered Throne","Forests, dark forests and plains",0xffb8a8dd),
  new Site("heresy_pit","Cradle of Embers","zulmak","Pit of Heresy","Badlands, deserts and savannas",0xffd08a66),
  new Site("avarice_vault","Rusted Vault","phryzhia","Grasp of Avarice","Plains, taiga and snowy plains",0xffd0ad68),
  new Site("siren_spire","Siren's Watch","akelous","Spire of the Watcher","Deserts, badlands and savannas",0xff8acbc9));
 public static Site site(String id){return ALL.stream().filter(s->s.id.equals(id)).findFirst().orElse(null);}
 public static Site boss(String id){return ALL.stream().filter(s->s.boss.equals(id)).findFirst().orElse(null);}
 public static final List<String> RUINS=List.of("cosmodrome_remnant","vex_conduit","warmind_relay","exodus_bulkhead","creation_annex");
 public static boolean locatable(String id){return site(id)!=null||RUINS.contains(id);}
}
