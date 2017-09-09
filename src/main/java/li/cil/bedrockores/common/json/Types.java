package li.cil.bedrockores.common.json;

import com.google.gson.reflect.TypeToken;
import li.cil.bedrockores.common.config.ore.OreConfig;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public final class Types {
    public static final Type LIST_ORE = new TypeToken<List<OreConfig>>() {}.getType();
    public static final Type LIST_STRING = new TypeToken<List<String>>() {}.getType();
    public static final Type MAP_STRING_STRING = new TypeToken<Map<String, String>>() {}.getType();

    private Types() {
    }
}
