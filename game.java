import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class game extends JFrame {
    boolean[][] boxes;
    boolean[][] nextBoxes;
    static final int GRID_SIZE = 100;
    static final int CELL_SIZE = 8;
    boolean drawValue = true;
    Timer timer;
    GridPanel gridPanel;
    JSlider speedSlider;
    public Color cellColor = Color.WHITE;
    
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

        //
        // Menu at the top of the screen for color palette
        //
        // https://coderanch.com/t/332515/java/Color-Palette
        // https://mathbits.com/JavaBitsNotebook/Graphics/Color.html
        // https://www.geeksforgeeks.org/java/java-swing-jcolorchooser-class/#
        //
        JMenuBar menuBar = new JMenuBar();
        JMenu optionsMenu = new JMenu("Options");
        JMenu colorMenu = new JMenu("Color");
        optionsMenu.add(colorMenu);
        menuBar.add(optionsMenu);
        setJMenuBar(menuBar);

        // original palette (4x4)
        Color[] paletteColors = new Color[]
                {
            Color.WHITE, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY,
            Color.BLACK, Color.RED, Color.ORANGE, Color.YELLOW,
            Color.GREEN, Color.MAGENTA, Color.CYAN, Color.BLUE,
            new Color(139,69,19), new Color(255,192,203), new Color(128,0,128), new Color(0,128,128)
        };

        JPopupMenu palettePopup = new JPopupMenu();
        JPanel palettePanel = new JPanel(new GridLayout(4, 4, 4, 4));
        palettePanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        palettePanel.setBackground(Color.WHITE);

        Color[] lastHovered = new Color[1];

        for (Color c : paletteColors)
        {
            JLabel swatch = new JLabel();
            swatch.setOpaque(true);
            swatch.setBackground(c);
            swatch.setPreferredSize(new Dimension(24, 24));
            swatch.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            swatch.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    lastHovered[0] = c;
                    cellColor = c;
                    gridPanel.repaint();
                }

                @Override
                public void mousePressed(MouseEvent e)
                {
                    cellColor = c;
                    gridPanel.repaint();
                    palettePopup.setVisible(false);
                }

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    if (lastHovered[0] != null)
                    {
                        cellColor = lastHovered[0];
                        gridPanel.repaint();
                        palettePopup.setVisible(false);
                    }
                }
            });

            palettePanel.add(swatch);
        }

        palettePopup.add(palettePanel);

        // Show/hide the palette when the menu is opened/closed
        colorMenu.addMenuListener(new MenuListener() {
            @Override public void menuSelected(MenuEvent e) {
                int x = colorMenu.getX();
                int y = colorMenu.getY() + colorMenu.getHeight();
                palettePopup.show(menuBar, x, y);
            }
            @Override public void menuDeselected(MenuEvent e) { palettePopup.setVisible(false); }
            @Override public void menuCanceled(MenuEvent e) { palettePopup.setVisible(false); }
        });

        // Add a "Custom..." item to open a full JColorChooser dialog with live preview
        JMenuItem customItem = new JMenuItem("custom");
        customItem.addActionListener(ae -> {
            final Color previous = cellColor;
            final JColorChooser chooser = new JColorChooser(cellColor);
            chooser.getSelectionModel().addChangeListener(e -> {
                cellColor = chooser.getColor();
                gridPanel.repaint();
            });
            JDialog dialog = JColorChooser.createDialog(this, "Custom Color", true, chooser,
                ok -> { /* OK: color already set */ },
                cancel -> { cellColor = previous; gridPanel.repaint(); }
            );
            dialog.setVisible(true);
        });
        colorMenu.add(customItem);
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
            // check diff!
            g.setColor(cellColor);

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
