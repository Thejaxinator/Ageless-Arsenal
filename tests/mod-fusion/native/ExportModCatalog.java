import com.google.gson.*;
import com.jax.destiny2mc.tenno.mods.WeaponMod;
import java.nio.file.*;
import java.util.*;

/** Derive the offline reference from the production enum; effects use the same formatter as the HUD. */
public final class ExportModCatalog {
    public static void main(String[] args) throws Exception {
        var catalog = new ArrayList<Map<String,Object>>();
        for (var mod : WeaponMod.values()) {
            var row = new LinkedHashMap<String,Object>();
            row.put("id",mod.id()); row.put("title",mod.title()); row.put("category",mod.category());
            row.put("pack",mod.pack()); row.put("maxRank",mod.maxRank());
            row.put("rarity",List.of("Common","Uncommon","Rare").get(mod.rarity()));
            row.put("description",mod.description()); row.put("baseEffect",mod.effect(0)); row.put("maxEffect",mod.effect(mod.maxRank()));
            var effects = new ArrayList<String>();
            for(int r=0;r<=mod.maxRank();r++) effects.add(mod.effect(r));
            row.put("effects",effects); row.put("stats",mod.steps()); row.put("icon",mod.icon().toString());
            row.put("reference","https://wiki.warframe.com/w/"+mod.title().replace(" ","_")); catalog.add(row);
        }
        Files.writeString(Path.of(args[0]),new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(catalog)+"\n");
    }
}
