package com.afkoverlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class FloatingOverlayWindow extends JFrame {
    private final PlayerInfo playerInfo;
    private AFKOverlayConfig config;
    private final JPanel contentPanel;
    private final JLabel hpLabel;
    private final JLabel prayerLabel;
    private final JLabel statusLabel;
    private final JLabel inventoryLabel;
    private JPanel titleBar;
    private JLabel characterNameLabel;
    private Window runeliteWindow; // Reference to RuneLite window
    
    // Icons
    private BufferedImage hpIcon;
    private BufferedImage prayerIcon;
    private BufferedImage inventoryIcon;
    private BufferedImage protectMeleeIcon;
    private BufferedImage protectMagicIcon;
    private BufferedImage protectRangedIcon;
    
    private Point dragPoint;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private int resizeEdge = 0; // 0=none, 1=right, 2=bottom, 3=corner
    
         private static final int RESIZE_BORDER = 5;
     private static final int MIN_WIDTH = 50; // Allow very thin windows
     private static final int MIN_HEIGHT = 80; // Reduced height too
     private static final int MAX_WIDTH = 400; // Maximum width
     private static final int MAX_HEIGHT = 250; // Maximum height
    
    // Colors for themes
    private static final Color DARK_BORDER_COLOR = new Color(60, 60, 60, 200);
    private static final Color DARK_TEXT_COLOR = new Color(220, 220, 220);
    private static final Color HP_COLOR = new Color(255, 120, 120); // Softer red
    private static final Color PRAYER_COLOR = new Color(100, 150, 255);
    private static final Color IDLE_COLOR = new Color(255, 180, 100); // Softer orange
    private static final Color ACTIVE_COLOR = new Color(120, 255, 120); // Softer green
    private static final Color WARNING_COLOR = new Color(255, 200, 100); // Yellow for 50% or below
    private static final Color DANGER_COLOR = new Color(255, 100, 100); // Red for 10% or below

    public FloatingOverlayWindow(PlayerInfo playerInfo, AFKOverlayConfig config) {
        this.playerInfo = playerInfo;
        this.config = config;
        
        // Load icons
        loadIcons();
        
        // Set window properties
        setAlwaysOnTop(true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        
        // Try to find the RuneLite window
        try {
            this.runeliteWindow = findRuneLiteWindow();
        } catch (Exception e) {
            // Ignore if we can't find it
        }
        
        // Create content panel with custom painting
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Create rounded rectangle background
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(), 12, 12);
                
                // Fill background with dark theme and opacity
                Color backgroundColor = new Color(30, 30, 30, config.opacity());
                g2d.setColor(backgroundColor);
                g2d.fill(roundedRectangle);
                
                // Draw border
                Color borderColor = DARK_BORDER_COLOR;
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(roundedRectangle);
                
                g2d.dispose();
            }
        };
        
        // Set layout
        contentPanel.setLayout(new BorderLayout(8, 8));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contentPanel.setOpaque(false);
        
                 // Create labels with icons (no text labels)
         hpLabel = createLabel("", hpIcon);
         prayerLabel = createLabel("", prayerIcon);
         statusLabel = createLabel("Status: ACTIVE", null);
         inventoryLabel = createLabel("", inventoryIcon);
        
        // Create info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        infoPanel.add(hpLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(prayerLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(inventoryLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(statusLabel);
        
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Create custom title bar with buttons
        titleBar = createTitleBar();
        contentPanel.add(titleBar, BorderLayout.NORTH);
        
        setContentPane(contentPanel);
        
        // Set initial size and position
        setSize(300, 180);
        setLocation(100, 100);
        
        // Add mouse listeners for dragging and resizing
        addMouseListeners();
        
        // Update display
        updateDisplay();
    }
    
    public void updateConfig() {
        // Update title bar (buttons)
        contentPanel.remove(titleBar);
        titleBar = createTitleBar();
        contentPanel.add(titleBar, BorderLayout.NORTH);
        
        // Update label colors for dark theme
        hpLabel.setForeground(DARK_TEXT_COLOR);
        prayerLabel.setForeground(DARK_TEXT_COLOR);
        statusLabel.setForeground(DARK_TEXT_COLOR);
        inventoryLabel.setForeground(DARK_TEXT_COLOR);
        characterNameLabel.setForeground(DARK_TEXT_COLOR);
        
        // Repaint to update transparency and theme
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    public void updateCharacterName(String name) {
        SwingUtilities.invokeLater(() -> {
            characterNameLabel.setText(name != null ? name : "");
        });
    }
    
    private Window findRuneLiteWindow() {
        try {
            // Try to get parent window first (most reliable)
            Window parent = SwingUtilities.getWindowAncestor(this);
            if (parent instanceof JFrame && parent.isVisible()) {
                String title = ((JFrame) parent).getTitle();
                if (title != null && title.toLowerCase().contains("runelite")
                        && !title.toLowerCase().contains("starting")
                        && !title.toLowerCase().contains("plugin")
                        && parent.getWidth() > 400 && parent.getHeight() > 400) {
                    return parent;
                }
            }

            // Fallback: search all windows
            for (Window window : Window.getWindows()) {
                if (window instanceof JFrame && window.isVisible()) {
                    String title = ((JFrame) window).getTitle();
                    if (title != null && title.toLowerCase().contains("runelite")
                            && !title.toLowerCase().contains("starting")
                            && !title.toLowerCase().contains("plugin")
                            && window.getWidth() > 400 && window.getHeight() > 400) {
                        return window;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore any errors
        }
        return null;
    }
    
    private void focusRuneLiteWindow() {
        try {
            Window targetWindow = runeliteWindow;
            if (targetWindow == null) {
                targetWindow = findRuneLiteWindow();
            }
            
            if (targetWindow != null) {
                // Unminimize if needed
                if (targetWindow instanceof Frame) {
                    Frame frame = (Frame) targetWindow;
                    if (frame.getState() == Frame.ICONIFIED) {
                        frame.setState(Frame.NORMAL);
                    }
                }
                
                // Bring to front and focus
                targetWindow.setVisible(true);
                targetWindow.toFront();
                targetWindow.requestFocus();
                targetWindow.requestFocusInWindow();
                
                // Force focus using multiple methods
                targetWindow.requestFocus();
                
                // Additional focus attempts
                try {
                    // Try to force focus again
                    targetWindow.requestFocus();
                } catch (Exception e) {
                    // Ignore any errors
                }
            }
        } catch (Exception e) {
            // Ignore any errors
        }
    }
    
    private void loadIcons() {
        // Load main icons
        try {
            hpIcon = ImageIO.read(getClass().getResourceAsStream("/com/icons/Hitpoints_icon.png"));
        } catch (IOException | IllegalArgumentException e) {
            hpIcon = createPlaceholderIcon(16, 16, HP_COLOR);
        }
        
        try {
            prayerIcon = ImageIO.read(getClass().getResourceAsStream("/com/icons/Prayer_icon.png"));
        } catch (IOException | IllegalArgumentException e) {
            prayerIcon = createPlaceholderIcon(16, 16, PRAYER_COLOR);
        }
        
        try {
            inventoryIcon = ImageIO.read(getClass().getResourceAsStream("/com/icons/Inventory.png"));
        } catch (IOException | IllegalArgumentException e) {
            inventoryIcon = createPlaceholderIcon(16, 16, new Color(150, 150, 150));
        }
        
        // Load protection prayer icons - if they fail, create colored versions
        try {
            protectMeleeIcon = ImageIO.read(getClass().getResourceAsStream("/com/icons/prayers/Protect_from_Melee.png"));
        } catch (IOException | IllegalArgumentException e) {
            protectMeleeIcon = createPlaceholderIcon(16, 16, new Color(255, 100, 100)); // Red for melee
        }
        
        try {
            protectMagicIcon = ImageIO.read(getClass().getResourceAsStream("/com/icons/prayers/Protect_from_Magic.png"));
        } catch (IOException | IllegalArgumentException e) {
            protectMagicIcon = createPlaceholderIcon(16, 16, new Color(100, 100, 255)); // Blue for magic
        }
        
        try {
            protectRangedIcon = ImageIO.read(getClass().getResourceAsStream("/com/icons/prayers/Protect_from_Missiles.png"));
        } catch (IOException | IllegalArgumentException e) {
            protectRangedIcon = createPlaceholderIcon(16, 16, new Color(100, 255, 100)); // Green for ranged
        }
    }
    
    private BufferedImage createPlaceholderIcon(int width, int height, Color color) {
        BufferedImage icon = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return icon;
    }
    
    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setPreferredSize(new Dimension(0, 20));
        
        // Create character name label
        characterNameLabel = new JLabel("");
        characterNameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        characterNameLabel.setForeground(DARK_TEXT_COLOR);
        characterNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        
        // Add character name on the left
        titleBar.add(characterNameLabel, BorderLayout.WEST);
        
        // Add buttons on the right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        buttonPanel.setOpaque(false);
        
                 if (config.showMinimizeButton()) {
             JButton minimizeButton = createCustomButton("−", null);
             minimizeButton.addActionListener(e -> setState(Frame.ICONIFIED));
             buttonPanel.add(minimizeButton);
         }
         
         if (config.showCloseButton()) {
             JButton closeButton = createCustomButton("×", null);
             closeButton.addActionListener(e -> dispose());
             buttonPanel.add(closeButton);
         }
        
        titleBar.add(buttonPanel, BorderLayout.EAST);
        return titleBar;
    }
    
         private JButton createCustomButton(String text, Color color) {
         JButton button = new JButton(text) {
             @Override
             protected void paintComponent(Graphics g) {
                 Graphics2D g2d = (Graphics2D) g.create();
                 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 
                 // Only show background on hover
                 if (getModel().isRollover()) {
                     g2d.setColor(new Color(0, 0, 0, 120)); // Semi-transparent black
                     g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                 }
                 
                 // Draw text in dark theme color
                 g2d.setColor(DARK_TEXT_COLOR);
                 g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Better font
                 
                 FontMetrics fm = g2d.getFontMetrics();
                 int textX = (getWidth() - fm.stringWidth(text)) / 2;
                 int textY = (getHeight() + fm.getAscent()) / 2 - 1; // Better centering
                 g2d.drawString(text, textX, textY);
                 
                 g2d.dispose();
             }
         };
         
         button.setPreferredSize(new Dimension(20, 20)); // Slightly larger
         button.setFocusPainted(false);
         button.setBorderPainted(false);
         button.setContentAreaFilled(false);
         button.setOpaque(false);
         
         return button;
     }
    
    private JLabel createLabel(String text, BufferedImage icon) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(DARK_TEXT_COLOR);
        label.setPreferredSize(new Dimension(0, 20));
        label.setMinimumSize(new Dimension(0, 20)); // Allow horizontal shrinking
        
        // Set icon if available
        if (icon != null) {
            label.setIcon(new ImageIcon(icon));
            label.setIconTextGap(6); // Space between icon and text
        }
        
        return label;
    }
    
    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int width = getWidth();
                int height = getHeight();
                
                // Check if clicking on resize areas
                if (p.x > width - RESIZE_BORDER && p.y > height - RESIZE_BORDER) {
                    isResizing = true;
                    resizeEdge = 3; // corner
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else if (p.x > width - RESIZE_BORDER) {
                    isResizing = true;
                    resizeEdge = 1; // right edge
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else if (p.y > height - RESIZE_BORDER) {
                    isResizing = true;
                    resizeEdge = 2; // bottom edge
                    setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                } else if (p.y < 30) { // Top area for dragging (like browser tab)
                    isDragging = true;
                    dragPoint = p;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
                // If not in top area, don't start dragging - allow clicking
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                isResizing = false;
                resizeEdge = 0;
                setCursor(Cursor.getDefaultCursor());
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Only focus if it's a left click and not on resize areas and not dragging
                if (e.getButton() == MouseEvent.BUTTON1 && !isResizing && !isDragging) {
                    // Try to focus the RuneLite window
                    try {
                        focusRuneLiteWindow();
                    } catch (Exception ex) {
                        // Ignore any errors
                    }
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    Point p = getLocation();
                    setLocation(p.x + e.getX() - dragPoint.x, p.y + e.getY() - dragPoint.y);
                } else if (isResizing) {
                    Point p = e.getPoint();
                    int newWidth = getWidth();
                    int newHeight = getHeight();
                    
                                         if (resizeEdge == 1 || resizeEdge == 3) { // right edge or corner
                         newWidth = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, p.x));
                     }
                     if (resizeEdge == 2 || resizeEdge == 3) { // bottom edge or corner
                         newHeight = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, p.y));
                     }
                    
                    setSize(newWidth, newHeight);
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int width = getWidth();
                int height = getHeight();
                
                if (p.x > width - RESIZE_BORDER && p.y > height - RESIZE_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else if (p.x > width - RESIZE_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else if (p.y > height - RESIZE_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                } else if (p.y < 30) { // Top area shows move cursor
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }
    
    public void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            hpLabel.setText(playerInfo.getHpText());
            prayerLabel.setText(playerInfo.getPrayerText());
            statusLabel.setText("Status: " + playerInfo.getStatusText());
            inventoryLabel.setText(playerInfo.getInventoryText());
            
            // Update prayer icon based on active protection prayer
            updatePrayerIcon();
            
            // Update colors based on status
            if (playerInfo.isIdle()) {
                statusLabel.setForeground(IDLE_COLOR);
            } else {
                statusLabel.setForeground(ACTIVE_COLOR);
            }
            
            // Update HP color based on percentage
             int hpPercent = playerInfo.getHpPercentage();
             if (hpPercent <= 10) {
                 hpLabel.setForeground(DANGER_COLOR);
             } else if (hpPercent <= 50) {
                 hpLabel.setForeground(WARNING_COLOR);
             } else {
                 hpLabel.setForeground(DARK_TEXT_COLOR);
             }
             
             // Update prayer color based on percentage
             int prayerPercent = playerInfo.getPrayerPercentage();
             if (prayerPercent <= 10) {
                 prayerLabel.setForeground(DANGER_COLOR);
             } else if (prayerPercent <= 50) {
                 prayerLabel.setForeground(WARNING_COLOR);
             } else {
                 prayerLabel.setForeground(PRAYER_COLOR); // Soft blue for normal prayer levels
             }
             
             // Update inventory color based on percentage (reversed logic)
             int invPercent = (playerInfo.getInventoryUsedSlots() * 100) / 28;
             if (invPercent <= 10) { // 10% or less is red (danger - almost empty)
                 inventoryLabel.setForeground(DANGER_COLOR);
             } else if (invPercent <= 50) {
                 inventoryLabel.setForeground(WARNING_COLOR);
             } else {
                 inventoryLabel.setForeground(DARK_TEXT_COLOR); // Green for 50%+ (good amount of items)
             }
         });
     }
     
         private void updatePrayerIcon() {
        String activePrayer = playerInfo.getActiveProtectionPrayer();
        BufferedImage iconToUse = prayerIcon; // Default prayer icon
        
        switch (activePrayer) {
            case "melee":
                iconToUse = protectMeleeIcon;
                break;
            case "magic":
                iconToUse = protectMagicIcon;
                break;
            case "ranged":
                iconToUse = protectRangedIcon;
                break;
            default:
                iconToUse = prayerIcon; // Default prayer icon when no protection prayer is active
                break;
        }
        
        prayerLabel.setIcon(new ImageIcon(iconToUse));
    }
} 