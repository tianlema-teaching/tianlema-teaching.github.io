package l19;

/** A knapsack item with a positive weight. */
public record Item(String name, int value, int weight) {
    public Item {
        if (weight <= 0 || value < 0) throw new IllegalArgumentException("bad item " + name);
    }

    public double ratio() { return (double) value / weight; }
}
