import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class game extends JFrame {
    boolean[][] boxes;
    boolean[][] nextBoxes;
    static final int GRID_SIZE = 100;
    static final int CELL_SIZE = 8;
    boolean drawValue = true;
    Timer timer;
    GridPanel gridPanel;
    JSlider speedSlider;
    
    public game() {
        boxes = new boolean[GRID_SIZE][GRID_SIZE];
        nextBoxes = new boolean[GRID_SIZE][GRID_SIZE];
        
        setTitle("Game of Life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        gridPanel = new GridPanel();
        
        JPanel controls = new JPanel();
        JButton play = new JButton("Play");
        JButton pause = new JButton("Pause");
        JButton reset = new JButton("Reset");
        
        speedSlider = new JSlider(10, 500, 100);
        speedSlider.setInverted(true);
        speedSlider.addChangeListener(e -> {
            if (timer != null) timer.setDelay(speedSlider.getValue());
        });
        
        play.addActionListener(e -> play());
        pause.addActionListener(e -> pause());
        reset.addActionListener(e -> reset());
        
        controls.add(play);
        controls.add(pause);
        controls.add(reset);
        controls.add(new JLabel("Speed:"));
        controls.add(speedSlider);
        
        add(gridPanel, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Custom panel for drawing the grid
    class GridPanel extends JPanel {
        public GridPanel() {
            setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
            setBackground(Color.BLACK);
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int col = e.getX() / CELL_SIZE;
                    int row = e.getY() / CELL_SIZE;
                    if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
                        boxes[row][col] = !boxes[row][col];
                        drawValue = boxes[row][col];
                        repaint();
                    }
                }
            });
            
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int col = e.getX() / CELL_SIZE;
                    int row = e.getY() / CELL_SIZE;
                    if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
                        boxes[row][col] = drawValue;
                        repaint();
                    }
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.WHITE);
            
            // Only draw alive cells
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (boxes[i][j]) {
                        g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }
    }
    
    void play() {
        if (timer == null) timer = new Timer(speedSlider.getValue(), e -> updateGeneration());
        timer.start();
    }
    
    void pause() {
        if (timer != null) timer.stop();
    }
    
    void updateGeneration() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int neighbors = countAliveNeighbors(i, j);
                nextBoxes[i][j] = boxes[i][j] ? (neighbors == 2 || neighbors == 3) : (neighbors == 3);
            }
        }
        boolean[][] temp = boxes;
        boxes = nextBoxes;
        nextBoxes = temp;
        gridPanel.repaint();
    }
    
    void reset() {
        pause();
        boxes = new boolean[GRID_SIZE][GRID_SIZE];
        nextBoxes = new boolean[GRID_SIZE][GRID_SIZE];
        gridPanel.repaint();
    }
    
    int countAliveNeighbors(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = row + dr;
                int c = col + dc;
                if (r >= 0 && r < GRID_SIZE && c >= 0 && c < GRID_SIZE && boxes[r][c]) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new game());
    }
}
