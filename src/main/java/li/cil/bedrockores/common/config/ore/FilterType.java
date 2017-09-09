package li.cil.bedrockores.common.config.ore;

public enum FilterType {
    Whitelist(value -> value),
    Blacklist(value -> !value);

    // --------------------------------------------------------------------- //

    private final BooleanPredicate predicate;

    // --------------------------------------------------------------------- //

    FilterType(final BooleanPredicate predicate) {
        this.predicate = predicate;
    }

    public FilterType getOpposite() {
        switch (this) {
            case Whitelist:
                return Blacklist;
            case Blacklist:
                return Whitelist;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    boolean filter(final boolean value) {
        return predicate.test(value);
    }

    // --------------------------------------------------------------------- //

    @FunctionalInterface
    private interface BooleanPredicate {
        boolean test(final boolean value);
    }
}
