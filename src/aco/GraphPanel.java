package aco;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Визуализация: рёбра рисуются тем толще и ярче, чем больше на них феромона;
 * лучший найденный маршрут подсвечен красным; муравьи — оранжевые точки.
 */
public class GraphPanel extends JPanel {
    private final Colony colony;
    private boolean showBest = true;

    public GraphPanel(Colony colony) {
        this.colony = colony;
        setBackground(Color.WHITE);
    }

    public void setShowBest(boolean showBest) { this.showBest = showBest; }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Graph graph = colony.getGraph();

        // 1. Феромон на рёбрах
        double max = Math.max(graph.maxPheromone(), 1e-9);
        for (Edge e : graph.getEdges()) {
            double rel = e.getPheromone() / max;               // 0..1
            int alpha = (int) Math.min(255, 15 + rel * 240);
            g.setColor(new Color(40, 90, 200, alpha));
            g.setStroke(new BasicStroke((float) (0.5 + rel * 7)));
            g.drawLine((int) e.getA().getX(), (int) e.getA().getY(),
                       (int) e.getB().getX(), (int) e.getB().getY());
        }

        // 2. Лучший маршрут
        List<Integer> best = colony.getBestTour();
        if (showBest && best.size() > 1) {
            g.setColor(new Color(220, 30, 30, 200));
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[]{8f, 6f}, 0f));
            for (int i = 0; i < best.size(); i++) {
                Node a = graph.getNode(best.get(i));
                Node b = graph.getNode(best.get((i + 1) % best.size()));
                g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
            }
        }

        // 3. Узлы
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        for (Node n : graph.getNodes()) {
            int x = (int) n.getX(), y = (int) n.getY();
            g.setColor(Color.BLACK);
            g.fillOval(x - 8, y - 8, 16, 16);
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.valueOf(n.getId() + 1), x + 11, y - 9);
        }

        // 4. Муравьи
        g.setStroke(new BasicStroke(1f));
        for (Ant ant : colony.getAnts()) {
            if (ant.isFinished()) continue;
            double[] p = ant.position(graph);
            g.setColor(new Color(255, 140, 0));
            g.fillOval((int) p[0] - 5, (int) p[1] - 5, 10, 10);
            g.setColor(Color.BLACK);
            g.drawOval((int) p[0] - 5, (int) p[1] - 5, 10, 10);
        }
    }
}
