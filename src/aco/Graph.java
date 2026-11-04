package aco;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Среда для муравьёв: полный неориентированный граф с весами-расстояниями. */
public class Graph {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Edge[][] matrix;

    public Graph(int nodeCount, double width, double height, long seed, double initialPheromone) {
        Random rnd = new Random(seed);
        double margin = 40;
        for (int i = 0; i < nodeCount; i++) {
            nodes.add(new Node(i,
                    margin + rnd.nextDouble() * (width - 2 * margin),
                    margin + rnd.nextDouble() * (height - 2 * margin)));
        }
        matrix = new Edge[nodeCount][nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            for (int j = i + 1; j < nodeCount; j++) {
                Edge e = new Edge(nodes.get(i), nodes.get(j), initialPheromone);
                edges.add(e);
                matrix[i][j] = e;
                matrix[j][i] = e;
            }
        }
    }

    public int size() { return nodes.size(); }
    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
    public Node getNode(int id) { return nodes.get(id); }

    public Edge getEdge(int i, int j) { return matrix[i][j]; }

    public void resetPheromone(double value) {
        for (Edge e : edges) {
            e.setPheromone(value);
            e.resetVisits();
        }
    }

    public double maxPheromone() {
        double max = 0;
        for (Edge e : edges) max = Math.max(max, e.getPheromone());
        return max;
    }

    /** Длина замкнутого маршрута (последний узел → первый). */
    public double tourLength(List<Integer> tour) {
        double sum = 0;
        for (int i = 0; i < tour.size(); i++) {
            int from = tour.get(i);
            int to = tour.get((i + 1) % tour.size());
            sum += matrix[from][to].getLength();
        }
        return sum;
    }
}
