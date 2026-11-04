package aco;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Муравей: выбирает следующий путь, перемещается по рёбрам, запоминает маршрут. */
public class Ant {
    public enum State { CHOOSING, MOVING, FINISHED }

    private final int id;
    private final int startNode;
    private final boolean[] visited;
    private final List<Integer> tour = new ArrayList<>();

    private State state;
    private int current;
    private int next = -1;
    private double progress;      // 0..1 — доля пройденного текущего ребра
    private double tourLength;

    public Ant(int id, int startNode, int nodeCount) {
        this.id = id;
        this.startNode = startNode;
        this.visited = new boolean[nodeCount];
        reset();
    }

    /** Подготовка к новому циклу: муравей возвращается в свой начальный узел. */
    public void reset() {
        java.util.Arrays.fill(visited, false);
        tour.clear();
        current = startNode;
        next = -1;
        progress = 0;
        tourLength = 0;
        visited[startNode] = true;
        tour.add(startNode);
        state = State.CHOOSING;
    }

    /**
     * Выбор следующего узла по вероятности
     * P(r,u) = τ(r,u)^α · η(r,u)^β / Σ τ(r,k)^α · η(r,k)^β,
     * где сумма берётся по ещё не посещённым узлам.
     */
    public void chooseNext(Graph graph, double alpha, double beta, Random rnd) {
        int n = graph.size();
        double[] weights = new double[n];
        double total = 0;
        for (int u = 0; u < n; u++) {
            if (visited[u]) continue;
            Edge e = graph.getEdge(current, u);
            weights[u] = Math.pow(e.getPheromone(), alpha) * Math.pow(e.getVisibility(), beta);
            total += weights[u];
        }
        if (total == 0) {
            // все посещены — возвращаемся в начальный узел
            next = startNode;
        } else {
            double r = rnd.nextDouble() * total;
            double acc = 0;
            next = -1;
            for (int u = 0; u < n; u++) {
                if (visited[u]) continue;
                acc += weights[u];
                if (r <= acc) { next = u; break; }
            }
            if (next == -1) { // защита от погрешности округления
                for (int u = n - 1; u >= 0; u--) if (!visited[u]) { next = u; break; }
            }
        }
        progress = 0;
        state = State.MOVING;
    }

    /**
     * Перемещение вдоль текущего ребра.
     * @param distance сколько «пикселей» проходит муравей за шаг (Double.MAX_VALUE — мгновенно)
     * @return true, если муравей прибыл в следующий узел
     */
    public boolean advance(Graph graph, double distance) {
        Edge e = graph.getEdge(current, next);
        progress += distance / e.getLength();
        if (progress < 1.0) return false;

        tourLength += e.getLength();
        e.markVisited();
        current = next;
        progress = 0;
        next = -1;
        if (current == startNode && tour.size() == graph.size()) {
            state = State.FINISHED;       // обошёл все узлы и вернулся
        } else {
            visited[current] = true;
            tour.add(current);
            state = State.CHOOSING;
        }
        return true;
    }

    /** Сколько феромона муравей оставит на каждом ребре своего маршрута: Δτ = Q / L. */
    public double pheromoneAmount(double q) { return q / tourLength; }

    public int getId() { return id; }
    public State getState() { return state; }
    public boolean isFinished() { return state == State.FINISHED; }
    public int getCurrent() { return current; }
    public int getNext() { return next; }
    public double getProgress() { return progress; }
    public double getTourLength() { return tourLength; }
    public List<Integer> getTour() { return tour; }

    /** Координаты муравья на экране (интерполяция между узлами). */
    public double[] position(Graph graph) {
        Node a = graph.getNode(current);
        if (next < 0) return new double[]{a.getX(), a.getY()};
        Node b = graph.getNode(next);
        return new double[]{
                a.getX() + (b.getX() - a.getX()) * progress,
                a.getY() + (b.getY() - a.getY()) * progress};
    }
}
