import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ImageProcessor extends JFrame {
    private JLabel imageLabel;
    private BufferedImage image;
    private BufferedImage originalImage;
    private List<Point> selectedPoints = new ArrayList<>();
    private boolean splitMode = false;
    private int splitX = -1;

    public ImageProcessor() {
        super("Image Processor");
        setLayout(new BorderLayout());

        JButton openButton = new JButton("פתח תמונה");
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = fileChooser.getSelectedFile();
                        image = ImageIO.read(file);
                        originalImage = ImageIO.read(file); // Keep a copy of the original image
                        ImageIcon imageIcon = new ImageIcon(image);
                        imageLabel.setIcon(imageIcon);
                        selectedPoints.clear();
                        splitMode = false;
                        splitX = -1;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JPanel panel = new JPanel();
        panel.add(openButton);

        JButton clearButton = new JButton("נקה נקודות");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedPoints.clear();
                splitMode = false;
                splitX = -1;
                imageLabel.repaint();
            }
        });

        panel.add(clearButton);

        imageLabel = new JLabel();
        imageLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!splitMode && selectedPoints.size() < 4) {
                    selectedPoints.add(e.getPoint());
                    imageLabel.repaint();
                }
            }
        });

        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                if (splitMode) {
                    splitX = e.getX();
                    imageLabel.repaint();
                }
            }
        });

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(10, 2));

        String[] manipulations = {
                "Black-White", "Grayscale", "Posterize", "Tint",
                "Color Shift Right", "Color Shift Left", "Mirror",
                "Pixelate", "Show Borders", "Eliminate Red",
                "Eliminate Green", "Eliminate Blue", "Negative",
                "Contrast", "Sepia", "Lighter", "Darker", "Vignette",
                "Add Noise", "Solarize", "Vintage", "Split Mode"
        };

        for (String manipulation : manipulations) {
            JButton button = new JButton(manipulation);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (manipulation.equals("Split Mode")) {
                        splitMode = true;
                        selectedPoints.clear();
                    } else {
                        applyManipulation(manipulation);
                    }
                }
            });
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void applyManipulation(String manipulation) {
        if (image == null) return;
        BufferedImage manipulatedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        if (splitMode && splitX >= 0) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < splitX; x++) {
                    Color color = new Color(image.getRGB(x, y));
                    color = applyColorManipulation(color, manipulation, x, y);
                    manipulatedImage.setRGB(x, y, color.getRGB());
                }
                for (int x = splitX; x < image.getWidth(); x++) {
                    manipulatedImage.setRGB(x, y, image.getRGB(x, y));
                }
            }
        } else if (selectedPoints.size() == 4) {
            int minX = Math.min(Math.min(selectedPoints.get(0).x, selectedPoints.get(1).x), Math.min(selectedPoints.get(2).x, selectedPoints.get(3).x));
            int maxX = Math.max(Math.max(selectedPoints.get(0).x, selectedPoints.get(1).x), Math.max(selectedPoints.get(2).x, selectedPoints.get(3).x));
            int minY = Math.min(Math.min(selectedPoints.get(0).y, selectedPoints.get(1).y), Math.min(selectedPoints.get(2).y, selectedPoints.get(3).y));
            int maxY = Math.max(Math.max(selectedPoints.get(0).y, selectedPoints.get(1).y), Math.max(selectedPoints.get(2).y, selectedPoints.get(3).y));

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                        Color color = new Color(image.getRGB(x, y));
                        color = applyColorManipulation(color, manipulation, x, y);
                        manipulatedImage.setRGB(x, y, color.getRGB());
                    } else {
                        manipulatedImage.setRGB(x, y, image.getRGB(x, y));
                    }
                }
            }
        } else {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    Color color = new Color(image.getRGB(x, y));
                    color = applyColorManipulation(color, manipulation, x, y);
                    manipulatedImage.setRGB(x, y, color.getRGB());
                }
            }
        }

        imageLabel.setIcon(new ImageIcon(manipulatedImage));
    }

    private Color applyColorManipulation(Color color, String manipulation, int x, int y) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        switch (manipulation) {
            case "Black-White":
                int avg = (red + green + blue) / 3;
                int bw = avg > 128 ? 255 : 0;
                return new Color(bw, bw, bw);
            case "Grayscale":
                int gray = (red + green + blue) / 3;
                return new Color(gray, gray, gray);
            case "Posterize":
                red = (red / 64) * 64;
                green = (green / 64) * 64;
                blue = (blue / 64) * 64;
                return new Color(red, green, blue);
            case "Tint":
                return new Color(Math.min(255, red + 50), green, blue);
            case "Color Shift Right":
                return new Color(blue, red, green);
            case "Color Shift Left":
                return new Color(green, blue, red);
            case "Mirror":
                return new Color(image.getRGB(image.getWidth() - 1 - x, y));
            case "Pixelate":
                int pixelSize = 10;
                int newX = (x / pixelSize) * pixelSize;
                int newY = (y / pixelSize) * pixelSize;
                return new Color(image.getRGB(newX, newY));
            case "Show Borders":
                int edgeColor = Color.WHITE.getRGB();
                if (x > 0 && y > 0 && x < image.getWidth() - 1 && y < image.getHeight() - 1) {
                    int gx = (-1 * new Color(image.getRGB(x - 1, y - 1)).getRed()) +
                            (-2 * new Color(image.getRGB(x - 1, y)).getRed()) +
                            (-1 * new Color(image.getRGB(x - 1, y + 1)).getRed()) +
                            (1 * new Color(image.getRGB(x + 1, y - 1)).getRed()) +
                            (2 * new Color(image.getRGB(x + 1, y)).getRed()) +
                            (1 * new Color(image.getRGB(x + 1, y + 1)).getRed());
                    int gy = (-1 * new Color(image.getRGB(x - 1, y - 1)).getRed()) +
                            (-2 * new Color(image.getRGB(x, y - 1)).getRed()) +
                            (-1 * new Color(image.getRGB(x + 1, y - 1)).getRed()) +
                            (1 * new Color(image.getRGB(x - 1, y + 1)).getRed()) +
                            (2 * new Color(image.getRGB(x, y + 1)).getRed()) +
                            (1 * new Color(image.getRGB(x + 1, y + 1)).getRed());
                    int edgeMagnitude = (int) Math.sqrt((gx * gx) + (gy * gy));
                    edgeColor = edgeMagnitude > 255 ? 255 : edgeMagnitude;
                    return new Color(edgeColor, edgeColor, edgeColor);
                } else {
                    return new Color(edgeColor, edgeColor, edgeColor);
                }
            case "Eliminate Red":
                return new Color(0, green, blue);
            case "Eliminate Green":
                return new Color(red, 0, blue);
            case "Eliminate Blue":
                return new Color(red, green, 0);
            case "Negative":
                return new Color(255 - red, 255 - green, 255 - blue);
            case "Contrast":
                float factor = 1.2f;
                red = truncate((int)(factor * (red - 128) + 128));
                green = truncate((int)(factor * (green - 128) + 128));
                blue = truncate((int)(factor * (blue - 128) + 128));
                return new Color(red, green, blue);
            case "Sepia":
                int tr = (int)(0.393 * red + 0.769 * green + 0.189 * blue);
                int tg = (int)(0.349 * red + 0.686 * green + 0.168 * blue);
                int tb = (int)(0.272 * red + 0.534 * green + 0.131 * blue);
                red = Math.min(255, tr);
                green = Math.min(255, tg);
                blue = Math.min(255, tb);
                return new Color(red, green, blue);
            case "Lighter":
                return new Color(Math.min(255, red + 30), Math.min(255, green + 30), Math.min(255, blue + 30));
            case "Darker":
                return new Color(Math.max(0, red - 30), Math.max(0, green - 30), Math.max(0, blue - 30));
            case "Vignette":
                double distance = Math.sqrt(Math.pow((x - image.getWidth() / 2), 2) + Math.pow((y - image.getHeight() / 2), 2));
                double maxDistance = Math.sqrt(Math.pow((image.getWidth() / 2), 2) + Math.pow((image.getHeight() / 2), 2));
                double ratio = distance / maxDistance;
                red = (int)(red * (1 - ratio));
                green = (int)(green * (1 - ratio));
                blue = (int)(blue * (1 - ratio));
                return new Color(red, green, blue);
            case "Add Noise":
                Random random = new Random();
                int noise = random.nextInt(50) - 25;
                return new Color(truncate(red + noise), truncate(green + noise), truncate(blue + noise));
            case "Solarize":
                int threshold = 128;
                red = red > threshold ? 255 - red : red;
                green = green > threshold ? 255 - green : green;
                blue = blue > threshold ? 255 - blue : blue;
                return new Color(red, green, blue);
            case "Vintage":
                tr = (int)(0.393 * red + 0.769 * green + 0.189 * blue);
                tg = (int)(0.349 * red + 0.686 * green + 0.168 * blue);
                tb = (int)(0.272 * red + 0.534 * green + 0.131 * blue);
                red = Math.min(255, tr);
                green = Math.min(255, tg);
                blue = Math.min(255, tb);
                random = new Random();
                noise = random.nextInt(50) - 25;
                return new Color(truncate(red + noise), truncate(green + noise), truncate(blue + noise));
        }

        return color;
    }

    private int truncate(int value) {
        if (value < 0) return 0;
        if (value > 255) return 255;
        return value;
    }

    public static void main(String[] args) {
        new ImageProcessor();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (selectedPoints.size() == 4) {
            g.setColor(Color.RED);
            for (int i = 0; i < 4; i++) {
                Point p1 = selectedPoints.get(i);
                Point p2 = selectedPoints.get((i + 1) % 4);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        } else if (splitMode && splitX >= 0) {
            g.setColor(Color.RED);
            g.drawLine(splitX, 0, splitX, getHeight());
        }
    }
}
