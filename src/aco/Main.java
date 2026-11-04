package aco;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Лабораторная: алгоритм муравьиной колонии (задача коммивояжёра).
 *
 * Запуск:
 *   java -cp out aco.Main                  — окно с визуализацией
 *   java -cp out aco.Main --headless [N]   — консольный прогон N циклов + проверка перебором
 */
public class Main {
    static final int NODES = 10;
    static final int WIDTH = 760;
    static final int HEIGHT = 560;
    static final long SEED = 42;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--headless")) {
            int cycles = args.length > 1 ? Integer.parseInt(args[1]) : 200;
            headless(cycles);
        } else {
            SwingUtilities.invokeLater(Main::startGui);
        }
    }

    // ---------------------------------------------------------------- консоль
    static void headless(int cycles) {
        Graph graph = new Graph(NODES, WIDTH, HEIGHT, SEED, Colony.INITIAL_PHEROMONE);
        Colony colony = new Colony(graph, SEED);

        // Проверка п.4 задания: в начале муравьи ходят по всем дорогам случайно
        colony.runCycle();
        int used = 0;
        for (Edge e : graph.getEdges()) if (e.getVisits() > 0) used++;
        System.out.printf("После 1-го цикла использовано рёбер: %d из %d%n", used, graph.getEdges().size());

        for (int c = 2; c <= cycles; c++) {
            colony.runCycle();
            if (c % (cycles / 10 == 0 ? 1 : cycles / 10) == 0) {
                System.out.printf("цикл %4d | лучший: %8.2f | на лучшем пути: %3.0f%%%n",
                        c, colony.getBestLength(), colony.convergence() * 100);
            }
        }
        System.out.printf("Лучший маршрут муравьёв: %.2f  %s%n", colony.getBestLength(), oneBased(colony.getBestTour()));

        double optimal = bruteForce(graph);
        System.out.printf("Оптимум (полный перебор): %.2f%n", optimal);
        System.out.println(Math.abs(optimal - colony.getBestLength()) < 1e-6
                ? "OK: муравьи нашли кратчайший маршрут" : "РАЗНИЦА: " + (colony.getBestLength() - optimal));
    }

    static List<Integer> oneBased(List<Integer> tour) {
        List<Integer> r = new ArrayList<>();
        for (int v : tour) r.add(v + 1);
        return r;
    }

    /** Точное решение полным перебором (только для проверки при малом числе узлов). */
    static double bruteForce(Graph g) {
        int n = g.size();
        int[] perm = new int[n - 1];
        for (int i = 0; i < n - 1; i++) perm[i] = i + 1;
        double[] best = {Double.MAX_VALUE};
        permute(g, perm, 0, best);
        return best[0];
    }

    private static void permute(Graph g, int[] p, int k, double[] best) {
        if (k == p.length) {
            double len = g.getEdge(0, p[0]).getLength() + g.getEdge(p[p.length - 1], 0).getLength();
            for (int i = 0; i < p.length - 1; i++) len += g.getEdge(p[i], p[i + 1]).getLength();
            best[0] = Math.min(best[0], len);
            return;
        }
        for (int i = k; i < p.length; i++) {
            int t = p[k]; p[k] = p[i]; p[i] = t;
            permute(g, p, k + 1, best);
            t = p[k]; p[k] = p[i]; p[i] = t;
        }
    }

    // -------------------------------------------------------------------- GUI
    static void startGui() {
        Graph graph = new Graph(NODES, WIDTH, HEIGHT, SEED, Colony.INITIAL_PHEROMONE);
        Colony colony = new Colony(graph, SEED);
        GraphPanel panel = new GraphPanel(colony);
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JLabel status = new JLabel(" ");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JSlider speed = new JSlider(1, 60, 6);
        JSlider rho = new JSlider(5, 95, (int) (colony.getRho() * 100));
        JSlider alpha = new JSlider(0, 50, (int) (colony.getAlpha() * 10));
        JSlider beta = new JSlider(0, 80, (int) (colony.getBeta() * 10));
        JButton startStop = new JButton("Пуск");
        JButton reset = new JButton("Сброс");
        JCheckBox showBest = new JCheckBox("Показать лучший путь", true);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controls.add(startStop);
        controls.add(reset);
        controls.add(new JLabel("Скорость"));
        controls.add(speed);
        controls.add(new JLabel("ρ (испарение)"));
        controls.add(rho);
        controls.add(new JLabel("α"));
        controls.add(alpha);
        controls.add(new JLabel("β"));
        controls.add(beta);
        controls.add(showBest);

        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            // несколько шагов за кадр, чтобы можно было ускорять
            colony.tick(speed.getValue());
            status.setText(String.format(
                    "Цикл: %d   Лучший маршрут: %.1f   Муравьёв на лучшем пути: %.0f%%   Маршрут: %s",
                    colony.getCycle(),
                    colony.getBestLength() == Double.MAX_VALUE ? 0 : colony.getBestLength(),
                    colony.convergence() * 100,
                    oneBased(colony.getBestTour())));
            panel.repaint();
        });

        startStop.addActionListener(e -> {
            if (timer.isRunning()) { timer.stop(); startStop.setText("Пуск"); }
            else { timer.start(); startStop.setText("Пауза"); }
        });
        reset.addActionListener(e -> { colony.reset(); panel.repaint(); });
        rho.addChangeListener(e -> colony.setRho(rho.getValue() / 100.0));
        alpha.addChangeListener(e -> colony.setAlpha(alpha.getValue() / 10.0));
        beta.addChangeListener(e -> colony.setBeta(beta.getValue() / 10.0));
        showBest.addActionListener(e -> { panel.setShowBest(showBest.isSelected()); panel.repaint(); });

        JFrame frame = new JFrame("Алгоритм муравьиной колонии — задача коммивояжёра");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(controls, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(status, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
