import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import javax.swing.JScrollBar;

public class DrawingApp extends Frame {
    private int lastX, lastY;
    private Color currentColor = Color.BLACK;
    private int pencilSize = 1;
    private Label redLabel, blueLabel, greenLabel, blackLabel, currentColorLabel;
    private Button randomColorButton, eraserButton;
    private JScrollBar pencilSizeScrollBar;
    private Button undoButton, redoButton;
    private Image offScreenImage;
    private Graphics2D offScreenGraphics;
    private Stack<Image> undoStack = new Stack<>();
    private Stack<Image> redoStack = new Stack<>();
    private Canvas drawingCanvas;
    private int mouseX, mouseY; // Track current mouse position

    public DrawingApp() {
        super("Simple Drawing App (by @SouravBarui2024)");
        prepareGUI();
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                // Handle Windows/Linux shortcuts (Ctrl)
                if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                    if (e.getKeyCode() == KeyEvent.VK_Z) {
                        undo();
                        return true;
                    } else if (e.getKeyCode() == KeyEvent.VK_Y) {
                        redo();
                        return true;
                    }
                }
                // Handle Mac shortcuts (Command)
                if ((e.getModifiersEx() & KeyEvent.META_DOWN_MASK) != 0) {
                    if (e.getKeyCode() == KeyEvent.VK_Z) {
                        // Command + Shift + Z for redo on Mac
                        if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
                            redo();
                        } else {
                            undo();
                        }
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void prepareGUI() {
        setSize(800, 800);
        setLayout(new BorderLayout());

        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridLayout(1, 9, 5, 0));

        // Create drawing canvas
        drawingCanvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                if (offScreenImage != null) {
                    g.drawImage(offScreenImage, 0, 0, this);
                }
                // Draw guide circle at mouse position
                g.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), 
                                   currentColor.getBlue(), 128));
                g.fillOval(mouseX - pencilSize/2, mouseY - pencilSize/2, 
                          pencilSize, pencilSize);
            }
        };
        drawingCanvas.setBackground(Color.WHITE);
        
        // Initialize offscreen buffer with proper size
        initializeOffScreenBuffer();

        redLabel = createRoundedColorLabel(Color.RED);
        buttonPanel.add(redLabel);

        blueLabel = createRoundedColorLabel(Color.BLUE);
        buttonPanel.add(blueLabel);

        greenLabel = createRoundedColorLabel(Color.GREEN);
        buttonPanel.add(greenLabel);

        blackLabel = createRoundedColorLabel(Color.BLACK);
        buttonPanel.add(blackLabel);

        randomColorButton = new Button("Random");
        randomColorButton.addActionListener(e -> {
            currentColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
            currentColorLabel.repaint();
            if (offScreenGraphics != null) {
                offScreenGraphics.setColor(currentColor);
            }
        });
        buttonPanel.add(randomColorButton);

        eraserButton = new Button("Eraser");
        eraserButton.addActionListener(e -> {
            currentColor = Color.WHITE;
            currentColorLabel.repaint();
            if (offScreenGraphics != null) {
                offScreenGraphics.setColor(currentColor);
            }
        });
        buttonPanel.add(eraserButton);

        currentColorLabel = new Label("") {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(currentColor);
                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2d.fillOval(x, y, size, size);
            }
        };
        currentColorLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        currentColorLabel.setPreferredSize(new Dimension(20, 20));
        buttonPanel.add(currentColorLabel);

        // Panel for pencil size with 2 columns
        Panel pencilPanel = new Panel(new GridLayout(2, 1));
        Label pencilLabel = new Label("Size: " + pencilSize, Label.CENTER);
        pencilSizeScrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 1, 0, 0, 50);
        pencilSizeScrollBar.addAdjustmentListener(e -> {
            pencilSize = pencilSizeScrollBar.getValue();
            pencilLabel.setText("Size: " + pencilSize);
            if (offScreenGraphics != null) {
                offScreenGraphics.setStroke(new BasicStroke(pencilSize));
            }
            drawingCanvas.repaint(); // Update guide circle
        });
        pencilPanel.add(pencilLabel);
        pencilPanel.add(pencilSizeScrollBar);
        buttonPanel.add(pencilPanel);

        Panel undoRedoPanel = new Panel(new GridLayout(2, 1, 0, 2));
        undoButton = new Button("Undo");
        undoButton.addActionListener(e -> undo());
        redoButton = new Button("Redo");
        redoButton.addActionListener(e -> redo());
        undoRedoPanel.add(undoButton);
        undoRedoPanel.add(redoButton);
        buttonPanel.add(undoRedoPanel);

        drawingCanvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initializeOffScreenBufferIfNeeded();
                saveStateForUndo();
                lastX = e.getX();
                lastY = e.getY();
                // Draw a point when mouse is pressed
                if (offScreenGraphics != null) {
                    offScreenGraphics.drawLine(lastX, lastY, lastX, lastY);
                    drawingCanvas.repaint();
                }
            }
        });

        drawingCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                drawingCanvas.repaint();
            }
            
            public void mouseDragged(MouseEvent e) {
                initializeOffScreenBufferIfNeeded();
                if (offScreenGraphics != null) {
                    int x = e.getX();
                    int y = e.getY();
                    offScreenGraphics.drawLine(lastX, lastY, x, y);
                    lastX = x;
                    lastY = y;
                    mouseX = x;
                    mouseY = y;
                    drawingCanvas.repaint();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        add(buttonPanel, BorderLayout.NORTH);
        add(drawingCanvas, BorderLayout.CENTER);

        setVisible(true);
    }

    private Label createRoundedColorLabel(Color color) {
        Label label = new Label("") {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setPreferredSize(new Dimension(30, 30));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                currentColor = color;
                currentColorLabel.repaint();
                if (offScreenGraphics != null) {
                    offScreenGraphics.setColor(currentColor);
                }
            }
        });
        return label;
    }

    private void initializeOffScreenBuffer() {
        offScreenImage = createImage(800, 800);
        if (offScreenImage != null) {
            offScreenGraphics = (Graphics2D) offScreenImage.getGraphics();
            offScreenGraphics.setColor(Color.WHITE);
            offScreenGraphics.fillRect(0, 0, 800, 800);
            offScreenGraphics.setColor(currentColor);
            offScreenGraphics.setStroke(new BasicStroke(pencilSize));
        }
    }

    private void initializeOffScreenBufferIfNeeded() {
        if (offScreenGraphics == null) {
            initializeOffScreenBuffer();
        }
    }

    private void saveStateForUndo() {
        if (offScreenImage != null) {
            undoStack.push(copyImage(offScreenImage));
            redoStack.clear();
        }
    }

    private Image copyImage(Image img) {
        Image copy = createImage(800, 800);
        if (copy != null) {
            Graphics g = copy.getGraphics();
            g.drawImage(img, 0, 0, this);
            g.dispose(); // Clean up graphics resources
        }
        return copy;
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(copyImage(offScreenImage));
            offScreenImage = undoStack.pop();
            offScreenGraphics = (Graphics2D) offScreenImage.getGraphics();
            offScreenGraphics.setColor(currentColor);
            offScreenGraphics.setStroke(new BasicStroke(pencilSize));
            drawingCanvas.repaint();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(copyImage(offScreenImage));
            offScreenImage = redoStack.pop();
            offScreenGraphics = (Graphics2D) offScreenImage.getGraphics();
            offScreenGraphics.setColor(currentColor);
            offScreenGraphics.setStroke(new BasicStroke(pencilSize));
            drawingCanvas.repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    public static void main(String[] args) {
        new DrawingApp();
    }
}