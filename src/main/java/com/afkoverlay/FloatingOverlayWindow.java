package com.afkoverlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.config.ConfigManager;

public class FloatingOverlayWindow extends JFrame {
    // Constants
    private static class Constants {
        // Window sizing
        static final int RESIZE_BORDER = 5;
        static final int MIN_WIDTH = 50;
        static final int MIN_HEIGHT = 50;
        static final int MAX_WIDTH = 400;
        static final int MAX_HEIGHT = 250;
        
        // Content sizing
        static final int MIN_ICON_SIZE = 18;
        static final int MAX_ICON_SIZE = 36;
        static final int MIN_FONT_SIZE = 14;
        static final int MAX_FONT_SIZE = 22;
        static final int SCALING_BUFFER = 20;
        static final double SCALING_FACTOR = 0.5;
        static final double MAX_SCALING = 1.8;
        
        // Layout
        static final int TITLE_BAR_HEIGHT = 20;
        static final int PADDING = 12;
        static final int COMPONENT_SPACING = 8;
        static final int ICON_TEXT_GAP = 6;
        static final int BUTTON_SIZE = 20;
        static final int BUTTON_SPACING = 4;
        static final int DRAG_AREA_HEIGHT = 30;
        
        // Colors
        static final Color DARK_BORDER_COLOR = new Color(60, 60, 60, 200);
        static final Color DARK_TEXT_COLOR = new Color(220, 220, 220);
        static final Color HP_COLOR = new Color(255, 120, 120);
        static final Color PRAYER_COLOR = new Color(100, 150, 255);
        static final Color IDLE_COLOR = new Color(255, 180, 100);
        static final Color ACTIVE_COLOR = new Color(120, 255, 120);
        static final Color WARNING_COLOR = new Color(255, 200, 100);
        static final Color DANGER_COLOR = new Color(255, 100, 100);
        static final Color WHITE = Color.WHITE;
    }

    // Instance variables
    private final PlayerInfo playerInfo;
    private final AFKOverlayConfig config;
    private final ConfigManager configManager;
    private JPanel contentPanel;
    private JPanel infoPanel;
    private JLabel hpLabel;
    private JLabel prayerLabel;
    private JLabel statusLabel;
    private JLabel inventoryLabel;
    private JPanel titleBar;
    private JLabel characterNameLabel;
    private Window runeliteWindow;
    
    // Icons
    private BufferedImage hpIcon;
    private BufferedImage prayerIcon;
    private BufferedImage inventoryIcon;
    private BufferedImage protectMeleeIcon;
    private BufferedImage protectMagicIcon;
    private BufferedImage protectRangedIcon;
    
    // Interaction state
    private Point dragPoint;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private int resizeEdge = 0; // 0=none, 1=right, 2=bottom, 3=corner

    public FloatingOverlayWindow(PlayerInfo playerInfo, AFKOverlayConfig config, ConfigManager configManager) {
        this.playerInfo = playerInfo;
        this.config = config;
        this.configManager = configManager;
        
        initializeWindow();
        loadIcons();
        setupContentPanel();
        setupLabels();
        setupLayout();
        setupEventListeners();
        
        // Load saved position and size, or use defaults
        if (!loadPositionAndSize()) {
            setSize(250, 150);
        }
        
        validatePosition();
        updateComponentSizes();
        updateDisplay();
    }
    
    private void initializeWindow() {
        setTitle("AFK Overlay");
        setUndecorated(true);
        setAlwaysOnTop(true);
        setType(Type.NORMAL);
        setResizable(false);
        setBackground(new Color(0, 0, 0, 0));
        
        // Try to find the RuneLite window
        try {
            this.runeliteWindow = findRuneLiteWindow();
        } catch (Exception e) {
            // Ignore if we can't find it
        }
    }
    
    private void setupContentPanel() {
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Create rounded rectangle background
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(), 12, 12);
                
                // Fill background with appropriate color
                g2d.setColor(getBackgroundColor());
                g2d.fill(roundedRectangle);
                
                // Draw border
                g2d.setColor(Constants.DARK_BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(roundedRectangle);
                
                g2d.dispose();
            }
        };
        
        contentPanel.setLayout(new BorderLayout(Constants.COMPONENT_SPACING, Constants.COMPONENT_SPACING));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(Constants.PADDING, Constants.PADDING, Constants.PADDING, Constants.PADDING));
        contentPanel.setOpaque(false);
    }
    
    private Color getBackgroundColor() {
        // Default background
        Color backgroundColor = new Color(30, 30, 30, config.opacity());
        
        // Priority: HP > Prayer > Status > Inventory
        int hpValue = playerInfo.getCurrentHp();
        int prayerValue = playerInfo.getCurrentPrayer();
        int invCount = playerInfo.getInventoryUsedSlots();
        
        if (config.highlightHpBackground() && hpValue <= config.lowHpThresholdValue()) {
            backgroundColor = config.lowHpOverlayColor();
        } else if (config.highlightPrayerBackground() && prayerValue <= config.lowPrayerThresholdValue()) {
            backgroundColor = config.lowPrayerOverlayColor();
        } else if (config.highlightIdleBackground() && playerInfo.isIdle()) {
            backgroundColor = config.idleOverlayColor();
        } else if (config.highlightInvBackground()) {
            boolean highlight = false;
            switch (config.invHighlightMode()) {
                case ABOVE:
                    highlight = invCount > config.invThresholdValue();
                    break;
                case BELOW:
                    highlight = invCount < config.invThresholdValue();
                    break;
            }
            if (highlight) {
                backgroundColor = config.invOverlayColor();
            }
        }
        
        return backgroundColor;
    }
    
    private void setupLabels() {
        hpLabel = createLabel("", hpIcon);
        prayerLabel = createLabel("", prayerIcon);
        statusLabel = createLabel("Status: ACTIVE", null);
        inventoryLabel = createLabel("", inventoryIcon);
    }
    
    private void setupLayout() {
        // Create info panel
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        // Conditionally add components based on config
        addComponentIfVisible(config.showHp(), hpLabel);
        addComponentIfVisible(config.showPrayer(), prayerLabel);
        addComponentIfVisible(config.showInventory(), inventoryLabel);
        addComponentIfVisible(config.showStatus(), statusLabel);
        
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Create title bar
        titleBar = createTitleBar();
        contentPanel.add(titleBar, BorderLayout.NORTH);
        
        // Add content panel to frame
        setContentPane(contentPanel);
    }
    
    private void addComponentIfVisible(boolean isVisible, JComponent component) {
        if (isVisible) {
            infoPanel.add(component);
            infoPanel.add(Box.createVerticalStrut(Constants.COMPONENT_SPACING));
        }
    }
    
    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setPreferredSize(new Dimension(0, Constants.TITLE_BAR_HEIGHT));
        
        // Create character name label
        characterNameLabel = new JLabel("");
        characterNameLabel.setFont(new Font("Arial", Font.BOLD, Constants.MIN_FONT_SIZE));
        characterNameLabel.setForeground(Constants.DARK_TEXT_COLOR);
        characterNameLabel.setBorder(BorderFactory.createEmptyBorder(0, Constants.PADDING, 0, 0));
        
        // Add character name on the left
        titleBar.add(characterNameLabel, BorderLayout.WEST);
        
        // Add buttons on the right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Constants.BUTTON_SPACING, 0));
        buttonPanel.setOpaque(false);
        
        if (config.showMinimizeButton()) {
            buttonPanel.add(createCustomButton("−"));
        }
        
        if (config.showCloseButton()) {
            buttonPanel.add(createCustomButton("×"));
        }
        
        titleBar.add(buttonPanel, BorderLayout.EAST);
        return titleBar;
    }
    
    private JButton createCustomButton(String text) {
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
                g2d.setColor(Constants.DARK_TEXT_COLOR);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Better font
                
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(text)) / 2;
                int textY = (getHeight() + fm.getAscent()) / 2 - 1; // Better centering
                g2d.drawString(text, textX, textY);
                
                g2d.dispose();
            }
        };
        
        button.setPreferredSize(new Dimension(Constants.BUTTON_SIZE, Constants.BUTTON_SIZE));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        
        // Add action listeners based on button type
        if ("−".equals(text)) {
            button.addActionListener(e -> {
                setState(Frame.ICONIFIED);
                setAlwaysOnTop(false);
            });
        } else if ("×".equals(text)) {
            button.addActionListener(e -> setVisible(false));
        }
        
        return button;
    }
    
    private JLabel createLabel(String text, BufferedImage icon) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, Constants.MIN_FONT_SIZE));
        label.setForeground(Constants.DARK_TEXT_COLOR);
        
        // Set icon if available
        if (icon != null) {
            label.setIcon(new ImageIcon(icon));
            label.setIconTextGap(Constants.ICON_TEXT_GAP);
        }
        
        return label;
    }
    
    private void setupEventListeners() {
        // Window listeners
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeiconified(WindowEvent e) {
                setAlwaysOnTop(true);
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                savePositionAndSize();
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                savePositionAndSize();
            }
        });
        
        // Component listeners
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                savePositionAndSize();
            }
            
            @Override
            public void componentMoved(ComponentEvent e) {
                savePositionAndSize();
            }
        });
        
        // Mouse listeners for dragging and resizing
        addMouseListeners();
    }
    
    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int width = getWidth();
                int height = getHeight();
                
                // Check if clicking on resize areas
                if (p.x > width - Constants.RESIZE_BORDER && p.y > height - Constants.RESIZE_BORDER) {
                    isResizing = true;
                    resizeEdge = 3; // corner
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else if (p.x > width - Constants.RESIZE_BORDER) {
                    isResizing = true;
                    resizeEdge = 1; // right edge
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else if (p.y > height - Constants.RESIZE_BORDER) {
                    isResizing = true;
                    resizeEdge = 2; // bottom edge
                    setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                } else if (p.y < Constants.DRAG_AREA_HEIGHT) { // Top area for dragging
                    isDragging = true;
                    dragPoint = p;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging || isResizing) {
                    savePositionAndSize();
                    validatePosition();
                    updateComponentSizes();
                }
                
                isDragging = false;
                isResizing = false;
                resizeEdge = 0;
                setCursor(Cursor.getDefaultCursor());
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && !isResizing && !isDragging) {
                    focusRuneLiteWindow();
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
                        newWidth = Math.max(Constants.MIN_WIDTH, Math.min(Constants.MAX_WIDTH, p.x));
                    }
                    if (resizeEdge == 2 || resizeEdge == 3) { // bottom edge or corner
                        newHeight = Math.max(Constants.MIN_HEIGHT, Math.min(Constants.MAX_HEIGHT, p.y));
                    }
                    
                    setSize(newWidth, newHeight);
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int width = getWidth();
                int height = getHeight();
                
                if (p.x > width - Constants.RESIZE_BORDER && p.y > height - Constants.RESIZE_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else if (p.x > width - Constants.RESIZE_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else if (p.y > height - Constants.RESIZE_BORDER) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                } else if (p.y < Constants.DRAG_AREA_HEIGHT) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }
    
    private boolean loadPositionAndSize() {
        int x = 100;
        int y = 100;
        int width = 250;
        int height = 150;
        
        boolean loadedFromConfig = false;
        
        if (configManager != null) {
            try {
                String xStr = configManager.getConfiguration("afkoverlay", "windowX", String.class);
                String yStr = configManager.getConfiguration("afkoverlay", "windowY", String.class);
                String widthStr = configManager.getConfiguration("afkoverlay", "windowWidth", String.class);
                String heightStr = configManager.getConfiguration("afkoverlay", "windowHeight", String.class);
                
                if (xStr != null && yStr != null && widthStr != null && heightStr != null) {
                    x = Integer.parseInt(xStr);
                    y = Integer.parseInt(yStr);
                    width = Integer.parseInt(widthStr);
                    height = Integer.parseInt(heightStr);
                    
                    // Ensure minimum dimensions
                    width = Math.max(width, Constants.MIN_WIDTH);
                    height = Math.max(height, Constants.MIN_HEIGHT);
                    
                    setBounds(x, y, width, height);
                    
                    loadedFromConfig = true;
                } else {
                    setBounds(x, y, width, height);
                }
            } catch (Exception e) {
                setBounds(x, y, width, height);
            }
        } else {
            setBounds(x, y, width, height);
        }
        
        return loadedFromConfig;
    }
    
    public void savePositionAndSize() {
        if (configManager != null) {
            int x = getX();
            int y = getY();
            int width = getWidth();
            int height = getHeight();
            
            configManager.setConfiguration("afkoverlay", "windowX", String.valueOf(x));
            configManager.setConfiguration("afkoverlay", "windowY", String.valueOf(y));
            configManager.setConfiguration("afkoverlay", "windowWidth", String.valueOf(width));
            configManager.setConfiguration("afkoverlay", "windowHeight", String.valueOf(height));
        }
    }
    
    private void validatePosition() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        // Find the screen that contains the window
        Rectangle windowBounds = getBounds();
        Rectangle screenBounds = null;
        
        for (GraphicsDevice device : devices) {
            Rectangle deviceBounds = device.getDefaultConfiguration().getBounds();
            if (deviceBounds.intersects(windowBounds)) {
                screenBounds = deviceBounds;
                break;
            }
        }
        
        // If no screen contains the window, use the primary screen
        if (screenBounds == null) {
            screenBounds = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        }
        
        // Ensure window is within screen bounds
        int x = windowBounds.x;
        int y = windowBounds.y;
        int width = windowBounds.width;
        int height = windowBounds.height;
        
        // Adjust if window is outside screen bounds
        if (x + width > screenBounds.x + screenBounds.width) {
            x = screenBounds.x + screenBounds.width - width;
        }
        if (y + height > screenBounds.y + screenBounds.height) {
            y = screenBounds.y + screenBounds.height - height;
        }
        if (x < screenBounds.x) {
            x = screenBounds.x;
        }
        if (y < screenBounds.y) {
            y = screenBounds.y;
        }
        
        // Ensure minimum size
        if (width < Constants.MIN_WIDTH) width = Constants.MIN_WIDTH;
        if (height < Constants.MIN_HEIGHT) height = Constants.MIN_HEIGHT;
        
        setBounds(x, y, width, height);
    }
    
    public void resetPosition() {
        setLocation(100, 100);
        validatePosition();
        savePositionAndSize();
    }
    
    public void updateConfig() {
        if (config.resetPosition()) {
            resetPosition();
            if (configManager != null) {
                configManager.setConfiguration("afkoverlay", "resetPosition", false);
            }
        }
        
        // Update title bar
        contentPanel.remove(titleBar);
        titleBar = createTitleBar();
        contentPanel.add(titleBar, BorderLayout.NORTH);
        
        // Update component visibility
        hpLabel.setVisible(config.showHp());
        prayerLabel.setVisible(config.showPrayer());
        statusLabel.setVisible(config.showStatus());
        inventoryLabel.setVisible(config.showInventory());
        
        // Rebuild info panel
        rebuildInfoPanel();
        
        // Update colors
        updateLabelColors();
        
        // Ensure minimum dimensions
        ensureMinimumDimensions();
        
        // Update component sizes
        updateComponentSizes();
    }
    
    private void rebuildInfoPanel() {
        contentPanel.remove(infoPanel);
        infoPanel.removeAll();
        
        // Re-add components conditionally
        addComponentIfVisible(config.showHp(), hpLabel);
        addComponentIfVisible(config.showPrayer(), prayerLabel);
        addComponentIfVisible(config.showInventory(), inventoryLabel);
        addComponentIfVisible(config.showStatus(), statusLabel);
        
        contentPanel.add(infoPanel, BorderLayout.CENTER);
    }
    
    private void updateLabelColors() {
        hpLabel.setForeground(Constants.DARK_TEXT_COLOR);
        prayerLabel.setForeground(Constants.DARK_TEXT_COLOR);
        statusLabel.setForeground(Constants.DARK_TEXT_COLOR);
        inventoryLabel.setForeground(Constants.DARK_TEXT_COLOR);
        characterNameLabel.setForeground(Constants.DARK_TEXT_COLOR);
    }
    
    private void ensureMinimumDimensions() {
        int currentWidth = getWidth();
        int currentHeight = getHeight();
        
        if (currentWidth < Constants.MIN_WIDTH || currentHeight < Constants.MIN_HEIGHT) {
            int newWidth = Math.max(currentWidth, Constants.MIN_WIDTH);
            int newHeight = Math.max(currentHeight, Constants.MIN_HEIGHT);
            setSize(newWidth, newHeight);
            validatePosition();
            savePositionAndSize();
        }
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
                targetWindow.requestFocus();
            }
        } catch (Exception e) {
            // Ignore any errors
        }
    }
    
    private void loadIcons() {
        // Load main icons
        hpIcon = loadIcon("/com/icons/Hitpoints_icon.png", Constants.HP_COLOR);
        prayerIcon = loadIcon("/com/icons/Prayer_icon.png", Constants.PRAYER_COLOR);
        inventoryIcon = loadIcon("/com/icons/Inventory.png", new Color(150, 150, 150));
        
        // Load protection prayer icons
        protectMeleeIcon = loadIcon("/com/icons/prayers/Protect_from_Melee.png", new Color(255, 100, 100));
        protectMagicIcon = loadIcon("/com/icons/prayers/Protect_from_Magic.png", new Color(100, 100, 255));
        protectRangedIcon = loadIcon("/com/icons/prayers/Protect_from_Missiles.png", new Color(100, 255, 100));
    }
    
    private BufferedImage loadIcon(String path, Color fallbackColor) {
        try {
            return ImageUtil.loadImageResource(getClass(), path);
        } catch (IllegalArgumentException e) {
            return createPlaceholderIcon(16, 16, fallbackColor);
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
    
    public void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            updateHpDisplay();
            updatePrayerDisplay();
            updateStatusDisplay();
            updateInventoryDisplay();
            contentPanel.repaint();
        });
    }
    
    private void updateHpDisplay() {
        if (config.showHp()) {
            hpLabel.setText(playerInfo.getHpText());
            int hpPercent = playerInfo.getHpPercentage();
            hpLabel.setForeground(getColorForPercentage(hpPercent, Constants.DARK_TEXT_COLOR));
        }
    }
    
    private void updatePrayerDisplay() {
        if (config.showPrayer()) {
            prayerLabel.setText(playerInfo.getPrayerText());
            
            // Check if protection prayer is active
            String activePrayer = playerInfo.getActiveProtectionPrayer();
            boolean hasProtectionPrayer = !activePrayer.isEmpty();
            
            int prayerPercent = playerInfo.getPrayerPercentage();
            if (prayerPercent <= 10) {
                prayerLabel.setForeground(Constants.DANGER_COLOR);
            } else if (prayerPercent <= 50) {
                prayerLabel.setForeground(Constants.WARNING_COLOR);
            } else {
                // Normal prayer level - check if protection prayer is active
                prayerLabel.setForeground(hasProtectionPrayer ? Constants.PRAYER_COLOR : Constants.WHITE);
            }
            
            updatePrayerIcon();
        }
    }
    
    private void updateStatusDisplay() {
        if (config.showStatus()) {
            statusLabel.setText("Status: " + playerInfo.getStatusText());
            statusLabel.setForeground(playerInfo.isIdle() ? Constants.IDLE_COLOR : Constants.ACTIVE_COLOR);
        }
    }
    
    private void updateInventoryDisplay() {
        if (config.showInventory()) {
            inventoryLabel.setText(playerInfo.getInventoryText());
            int invPercent = (playerInfo.getInventoryUsedSlots() * 100) / 28;
            inventoryLabel.setForeground(getColorForPercentage(invPercent, Constants.DARK_TEXT_COLOR));
        }
    }
    
    private Color getColorForPercentage(int percentage, Color defaultColor) {
        if (percentage <= 10) {
            return Constants.DANGER_COLOR;
        } else if (percentage <= 50) {
            return Constants.WARNING_COLOR;
        } else {
            return defaultColor;
        }
    }
    
    private void updatePrayerIcon() {
        String activePrayer = playerInfo.getActiveProtectionPrayer();
        BufferedImage originalIcon = prayerIcon; // Default prayer icon
        
        switch (activePrayer) {
            case "melee":
                originalIcon = protectMeleeIcon;
                break;
            case "magic":
                originalIcon = protectMagicIcon;
                break;
            case "ranged":
                originalIcon = protectRangedIcon;
                break;
            default:
                originalIcon = prayerIcon;
                break;
        }
        
        // Resize the icon to the current icon size
        int iconSize = getScaledIconSize();
        prayerLabel.setIcon(resizeIcon(originalIcon, iconSize, iconSize));
    }
    
    private ImageIcon resizeIcon(BufferedImage originalIcon, int width, int height) {
        if (originalIcon == null) {
            return null;
        }
        Image scaledImage = originalIcon.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
    
    private void updateComponentSizes() {
        int currentHeight = getHeight();
        int minRequiredHeight = calculateMinimumRequiredHeight();
        
        // Calculate scaling factor
        double scalingFactor = 1.0; // Default to no scaling
        if (currentHeight > minRequiredHeight + Constants.SCALING_BUFFER) {
            double extraHeight = currentHeight - minRequiredHeight;
            scalingFactor = 1.0 + (extraHeight / 100.0) * Constants.SCALING_FACTOR;
            scalingFactor = Math.min(Constants.MAX_SCALING, scalingFactor);
        }
        
        // Calculate new sizes
        int iconSize = getScaledIconSize(scalingFactor);
        int fontSize = getScaledFontSize(scalingFactor);
        Font newFont = new Font("Arial", Font.BOLD, fontSize);
        
        // Update each label's font and icon (if visible)
        updateLabelSize(config.showHp(), hpLabel, newFont, hpIcon, iconSize);
        updateLabelSize(config.showPrayer(), prayerLabel, newFont, null, iconSize); // Icon updated separately
        updateLabelSize(config.showStatus(), statusLabel, newFont, null, iconSize);
        updateLabelSize(config.showInventory(), inventoryLabel, newFont, inventoryIcon, iconSize);
        
        // Update character name label
        characterNameLabel.setFont(new Font("Arial", Font.BOLD, fontSize));
        
        // Update the layout
        infoPanel.revalidate();
    }
    
    private int getScaledIconSize() {
        int currentHeight = getHeight();
        int minRequiredHeight = calculateMinimumRequiredHeight();
        
        double scalingFactor = 1.0; // Default to no scaling
        if (currentHeight > minRequiredHeight + Constants.SCALING_BUFFER) {
            double extraHeight = currentHeight - minRequiredHeight;
            scalingFactor = 1.0 + (extraHeight / 100.0) * Constants.SCALING_FACTOR;
            scalingFactor = Math.min(Constants.MAX_SCALING, scalingFactor);
        }
        
        int iconSize = (int) (Constants.MIN_ICON_SIZE * scalingFactor);
        return Math.max(Constants.MIN_ICON_SIZE, Math.min(Constants.MAX_ICON_SIZE, iconSize));
    }
    
    private int getScaledIconSize(double scalingFactor) {
        int iconSize = (int) (Constants.MIN_ICON_SIZE * scalingFactor);
        return Math.max(Constants.MIN_ICON_SIZE, Math.min(Constants.MAX_ICON_SIZE, iconSize));
    }
    
    private int getScaledFontSize(double scalingFactor) {
        int fontSize = (int) (Constants.MIN_FONT_SIZE * scalingFactor);
        return Math.max(Constants.MIN_FONT_SIZE, Math.min(Constants.MAX_FONT_SIZE, fontSize));
    }
    
    private void updateLabelSize(boolean isVisible, JLabel label, Font font, BufferedImage icon, int iconSize) {
        if (isVisible) {
            label.setFont(font);
            if (icon != null) {
                label.setIcon(resizeIcon(icon, iconSize, iconSize));
            }
        }
    }
    
    private int calculateMinimumRequiredHeight() {
        // Start with the title bar height
        int minHeight = Constants.TITLE_BAR_HEIGHT;
        
        // Add the height of each visible component
        int componentCount = 0;
        if (config.showHp()) componentCount++;
        if (config.showPrayer()) componentCount++;
        if (config.showInventory()) componentCount++;
        if (config.showStatus()) componentCount++;
        
        // Each component needs space for itself plus spacing
        Font minFont = new Font("Arial", Font.BOLD, Constants.MIN_FONT_SIZE);
        FontMetrics fm = hpLabel.getFontMetrics(minFont);
        int componentHeight = fm.getHeight();
        
        // Total height = (component height + spacing) for each component
        minHeight += componentCount * (componentHeight + Constants.COMPONENT_SPACING);
        
        // Add padding
        minHeight += Constants.PADDING * 2;
        
        return minHeight;
    }
}