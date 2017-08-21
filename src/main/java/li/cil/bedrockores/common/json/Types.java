package li.cil.bedrockores.common.json;

import com.google.gson.reflect.TypeToken;
import li.cil.bedrockores.common.config.VeinConfig;

import java.lang.reflect.Type;
import java.util.List;

public class Types {
    public static final Type ORE_LIST = new TypeToken<List<VeinConfig.Ore>>() {}.getType();
}
