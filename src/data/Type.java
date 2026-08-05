package data;

public enum Type {
    STANDARD(false),
    DISABLED(true),
    FAMILY(true);

    public final boolean isExcusive;

    Type(boolean isExcusive) {
        this.isExcusive = isExcusive;
    }
}
