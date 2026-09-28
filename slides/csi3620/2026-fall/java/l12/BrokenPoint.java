package l12;

/** A key class that breaks the contract: equals is overridden, hashCode is not. */
@SuppressWarnings("overrides") // deliberately broken to show the bug; javac -Xlint:all warns without this
public final class BrokenPoint {
    private final int x;
    private final int y;

    public BrokenPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BrokenPoint)) {
            return false;
        }
        BrokenPoint p = (BrokenPoint) o;
        return x == p.x && y == p.y;
    }
    // hashCode() is inherited from Object:
    // equal points usually get different hash codes
}
