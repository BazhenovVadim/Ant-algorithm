package aco;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Муравьиная колония: управляет муравьями и обновлением феромона (Ant System). */
public class Colony {
    public static final double INITIAL_PHEROMONE = 1.0;
    public static final double MIN_PHEROMONE = 1e-6;

    private final Graph graph;
    private final List<Ant> ants = new ArrayList<>();
    private final Random rnd;

    private double alpha = 1.0;   // значимость феромона
    private double beta = 3.0;    // значимость расстояния
    private double rho = 0.5;     // коэффициент испарения
    private double q = 1000.0;    // константа Q

    private int cycle;
    private List<Integer> bestTour = new ArrayList<>();
    private double bestLength = Double.MAX_VALUE;
    private double lastCycleBest = Double.MAX_VALUE;

    public Colony(Graph graph, long seed) {
        this.graph = graph;
        this.rnd = new Random(seed);
        spawnAnts(graph.size());
    }

    /** Начальная популяция: муравьи равномерно распределены по узлам. */
    public void spawnAnts(int count) {
        ants.clear();
        for (int i = 0; i < count; i++) {
            ants.add(new Ant(i, i % graph.size(), graph.size()));
        }
    }

    public void reset() {
        graph.resetPheromone(INITIAL_PHEROMONE);
        cycle = 0;
        bestTour = new ArrayList<>();
        bestLength = Double.MAX_VALUE;
        lastCycleBest = Double.MAX_VALUE;
        convergence = 0;
        for (Ant a : ants) a.reset();
    }

    /** Один шаг анимации: каждый муравей проходит distance пикселей. */
    public void tick(double distance) {
        boolean allFinished = true;
        for (Ant ant : ants) {
            if (ant.isFinished()) continue;
            if (ant.getState() == Ant.State.CHOOSING) {
                ant.chooseNext(graph, alpha, beta, rnd);
            }
            ant.advance(graph, distance);
            if (!ant.isFinished()) allFinished = false;
        }
        if (allFinished) finishCycle();
    }

    /** Полный цикл без анимации (для консольного режима и тестов). */
    public void runCycle() {
        for (Ant ant : ants) {
            while (!ant.isFinished()) {
                if (ant.getState() == Ant.State.CHOOSING) {
                    ant.chooseNext(graph, alpha, beta, rnd);
                }
                ant.advance(graph, Double.MAX_VALUE);
            }
        }
        finishCycle();
    }

    public void run(int cycles) {
        for (int i = 0; i < cycles; i++) runCycle();
    }

    /** Конец цикла: испарение, откладывание феромона, поиск лучшего маршрута. */
    private void finishCycle() {
        for (Edge e : graph.getEdges()) e.evaporate(rho, MIN_PHEROMONE);

        lastCycleBest = Double.MAX_VALUE;
        for (Ant ant : ants) {
            double delta = ant.pheromoneAmount(q);
            List<Integer> t = ant.getTour();
            for (int i = 0; i < t.size(); i++) {
                graph.getEdge(t.get(i), t.get((i + 1) % t.size())).deposit(delta);
            }
            lastCycleBest = Math.min(lastCycleBest, ant.getTourLength());
            if (ant.getTourLength() < bestLength - 1e-9) {
                bestLength = ant.getTourLength();
                bestTour = new ArrayList<>(t);
            }
        }
        int onBest = 0;
        for (Ant ant : ants) {
            if (ant.getTourLength() <= bestLength + 1e-6) onBest++;
        }
        convergence = (double) onBest / ants.size();
        cycle++;
        for (Ant ant : ants) ant.reset();
    }

    /** Доля муравьёв, прошедших в последнем цикле по лучшему найденному маршруту. */
    public double convergence() { return convergence; }

    private double convergence;

    public Graph getGraph() { return graph; }
    public List<Ant> getAnts() { return ants; }
    public int getCycle() { return cycle; }
    public double getBestLength() { return bestLength; }
    public List<Integer> getBestTour() { return bestTour; }
    public double getLastCycleBest() { return lastCycleBest; }

    public double getAlpha() { return alpha; }
    public double getBeta() { return beta; }
    public double getRho() { return rho; }
    public double getQ() { return q; }
    public void setAlpha(double alpha) { this.alpha = alpha; }
    public void setBeta(double beta) { this.beta = beta; }
    public void setRho(double rho) { this.rho = rho; }
    public void setQ(double q) { this.q = q; }
}
