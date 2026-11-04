package aco;

/** Узел сети («город», который должен посетить муравей). */
public class Node {
    private final int id;
    private final double x;
    private final double y;

    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }

    public double distanceTo(Node other) {
        return Math.hypot(x - other.x, y - other.y);
    }

    @Override
    public String toString() {
        return "Node" + id;
    }
}
