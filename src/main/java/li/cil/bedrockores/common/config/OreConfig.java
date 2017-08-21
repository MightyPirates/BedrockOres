package li.cil.bedrockores.common.config;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class OreConfig {
    public String[] comment;
    public boolean enabled = true;

    public WrappedBlockState state;

    public String dimension = "overworld";
    public int weight = 1;

    public int widthMin = 4;
    public int widthMax = 6;

    public int heightMin = 2;
    public int heightMax = 4;

    public int countMin = 8;
    public int countMax = 12;

    public int yieldMin = 2000;
    public int yieldMax = 3000;

    public String group;
    public int groupOrder;
}
