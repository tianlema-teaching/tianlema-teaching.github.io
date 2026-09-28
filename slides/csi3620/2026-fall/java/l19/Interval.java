package l19;

/** A half-open time interval [start, finish). */
public record Interval(String name, int start, int finish) {
    public Interval {
        if (finish <= start) throw new IllegalArgumentException("empty interval " + name);
    }

    /** True if the two intervals share no time; touching ends are fine. */
    public boolean compatibleWith(Interval other) {
        return finish <= other.start || other.finish <= start;
    }

    public int length() { return finish - start; }

    @Override
    public String toString() { return name + "[" + start + "," + finish + ")"; }
}
