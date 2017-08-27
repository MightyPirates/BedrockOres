package li.cil.bedrockores.common.config;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class OreConfig {
    public String[] comment;
    public boolean enabled = true;

    public WrappedBlockState state;

    public String dimension = "overworld";
    public int weight = 1;

    public int widthMin = 2;
    public int widthMax = 4;

    public int heightMin = 2;
    public int heightMax = 4;

    public int countMin = 5;
    public int countMax = 10;

    public int yieldMin = 100;
    public int yieldMax = 125;

    public String group;
    public int groupOrder;

    @Override
    public String toString() {
        return state.toString();
    }
}
