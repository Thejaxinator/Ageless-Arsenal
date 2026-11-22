package com.jax.destiny2mc.atlas;
import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class BestiaryCatalog {
 public record Entry(String id,String name,String faction,String role,String site,String origin,String spawn,List<String> mechanics,List<String> threats,String reward){
  public String realm(){return faction.equalsIgnoreCase("corpus")||origin.toLowerCase(Locale.ROOT).contains("warframe")?"warframe":"destiny";}
  public boolean boss(){return role.toLowerCase(Locale.ROOT).contains("boss");}
  public boolean matches(String query){String s=(name+" "+id+" "+realm()+" "+faction+" "+role+" "+origin).toLowerCase(Locale.ROOT);return s.contains(query.toLowerCase(Locale.ROOT).strip());}
 }
 public static final List<Entry> ALL=load();
 private static List<Entry> load(){try(var stream=BestiaryCatalog.class.getResourceAsStream("/assets/destiny2mc/atlas/bestiary.json")){if(stream==null)throw new IOException("Missing bestiary");return List.of(new Gson().fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),Entry[].class));}catch(IOException e){throw new IllegalStateException("Could not load Ageless Arsenals Field Guide",e);}}
 public static Entry get(String id){return ALL.stream().filter(e->e.id.equals(id)).findFirst().orElse(null);}
 public static List<Entry> filter(String query,String faction,boolean bosses){return filter(query,"all",faction,bosses);}
 public static List<Entry> filter(String query,String realm,String faction,boolean bosses){return ALL.stream().filter(e->(realm.equals("all")||e.realm().equals(realm))&&(faction.equals("all")||e.faction.equals(faction))&&(!bosses||e.boss())&&e.matches(query)).toList();}
}
