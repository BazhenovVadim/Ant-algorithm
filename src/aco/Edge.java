package aco;

/** Путь (ребро графа) между двумя городами. Хранит длину и количество феромона. */
public class Edge {
    private final Node a;
    private final Node b;
    private final double length;
    private double pheromone;
    private long visits;

    public Edge(Node a, Node b, double initialPheromone) {
        this.a = a;
        this.b = b;
        this.length = a.distanceTo(b);
        this.pheromone = initialPheromone;
    }

    public Node getA() { return a; }
    public Node getB() { return b; }
    public double getLength() { return length; }
    public double getPheromone() { return pheromone; }
    public long getVisits() { return visits; }

    /** η = 1 / d — «привлекательность» ребра по расстоянию. */
    public double getVisibility() { return 1.0 / length; }

    public void setPheromone(double value) { this.pheromone = value; }

    /** Испарение: τ = τ * (1 - ρ). */
    public void evaporate(double rho, double minPheromone) {
        pheromone = Math.max(minPheromone, pheromone * (1.0 - rho));
    }

    /** Откладывание феромона муравьём. */
    public void deposit(double amount) { pheromone += amount; }

    public void markVisited() { visits++; }

    public void resetVisits() { visits = 0; }
}
