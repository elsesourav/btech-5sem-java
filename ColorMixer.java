import java.awt.*;
import java.awt.event.*;

public class ColorMixer extends Frame {
   private Scrollbar redScrollbar, greenScrollbar, blueScrollbar;
   private Label redValueLabel, greenValueLabel, blueValueLabel;
   private Canvas colorDisplayCanvas;

   public ColorMixer() {
      super("Color Mixer (by @SouravBarui2024)");
      prepareGUI();
   }

   private void prepareGUI() {
      setSize(500, 500);
      setLayout(new BorderLayout(20, 20));

      Panel slidersPanel = new Panel(new GridLayout(3, 1, 5, 5));

      redScrollbar = createCenteredScrollbar();
      greenScrollbar = createCenteredScrollbar();
      blueScrollbar = createCenteredScrollbar();
      redValueLabel = createPaddedLabel("0");
      greenValueLabel = createPaddedLabel("0");
      blueValueLabel = createPaddedLabel("0");

      addComponentWithLabel(slidersPanel, "Red", redScrollbar, redValueLabel);
      addComponentWithLabel(slidersPanel, "Green", greenScrollbar, greenValueLabel);
      addComponentWithLabel(slidersPanel, "Blue", blueScrollbar, blueValueLabel);

      colorDisplayCanvas = new Canvas() {
         @Override
         public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillOval(0, 0, getWidth(), getHeight());
         }
      };
      colorDisplayCanvas.setPreferredSize(new Dimension(400, 300));
      colorDisplayCanvas.setBackground(new Color(0, 0, 0));

      Panel colorDisplayPanel = new Panel(new GridLayout(1, 1));
      colorDisplayPanel.add(colorDisplayCanvas);

      add(colorDisplayPanel, BorderLayout.NORTH);
      add(slidersPanel, BorderLayout.CENTER);

      Panel paddingPanel = new Panel();
      add(paddingPanel, BorderLayout.SOUTH);

      redScrollbar.addAdjustmentListener(e -> updateColor());
      greenScrollbar.addAdjustmentListener(e -> updateColor());
      blueScrollbar.addAdjustmentListener(e -> updateColor());

      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent we) {
            System.exit(0);
         }
      });

      pack();
      setVisible(true);
   }

   private Scrollbar createCenteredScrollbar() {
      Scrollbar scrollbar = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, 256);
      scrollbar.setUnitIncrement(1);
      scrollbar.setBlockIncrement(10);
      return scrollbar;
   }

   private void addComponentWithLabel(Panel panel, String label, Scrollbar scrollbar, Label valueLabel) {
      Panel subPanel = new Panel(new GridLayout(1, 3));
      subPanel.add(new Label(label, Label.CENTER));
      subPanel.add(scrollbar);
      subPanel.add(valueLabel);
      panel.add(subPanel);
   }

   private Label createPaddedLabel(String text) {
      Label label = new Label(text, Label.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 16));
      return label;
   }

   private void updateColor() {
      int red = redScrollbar.getValue();
      int green = greenScrollbar.getValue();
      int blue = blueScrollbar.getValue();

      redValueLabel.setText(String.valueOf(red));
      greenValueLabel.setText(String.valueOf(green));
      blueValueLabel.setText(String.valueOf(blue));

      Color newColor = new Color(red, green, blue);
      colorDisplayCanvas.setBackground(newColor);
      colorDisplayCanvas.repaint();
   }

   public static void main(String[] args) {
      new ColorMixer();
   }
}