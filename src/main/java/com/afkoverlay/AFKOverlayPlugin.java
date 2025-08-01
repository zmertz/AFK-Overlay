package com.afkoverlay;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PlayerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.time.Instant;

@Slf4j
@PluginDescriptor(
    name = "AFK Overlay",
    description = "Displays player information in a floating overlay window",
    tags = {"overlay", "player", "stats", "afk"}
)
public class AFKOverlayPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private AFKOverlayConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AFKOverlay overlay;

    @Inject
    private ConfigManager configManager;

    private FloatingOverlayWindow floatingWindow;
    private PlayerInfo playerInfo;
    // Track the last time the player was active
    private Instant lastActive = Instant.now();
    // Track if window was closed by user
    private boolean windowClosedByUser = false;

    @Override
    protected void startUp() throws Exception {
        log.info("AFK Overlay plugin started!");
        
        // Initialize player info
        playerInfo = new PlayerInfo();        
        // Create and show floating overlay window
        createAndShowWindow();
        
        // Add overlay to overlay manager (for potential future use)
        overlayManager.add(overlay);
    }

    private void createAndShowWindow() {
        SwingUtilities.invokeLater(() -> {
            floatingWindow = new FloatingOverlayWindow(playerInfo, config, configManager);
            
            // Set custom icon for the window (using the plugin hub icon)
            try {
                Image icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
                if (icon != null) {
                    floatingWindow.setIconImage(icon);
                    // Also try setting it as a list for better compatibility
                    floatingWindow.setIconImages(java.util.Arrays.asList(icon));
                }
            } catch ( IllegalArgumentException e) {
                // Silently fall back to default icon
            }
            
            // Add window listener to track when it's closed
            floatingWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    windowClosedByUser = true;
                }
            });
            
            floatingWindow.setVisible(true);
            windowClosedByUser = false;
        });
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("AFK Overlay plugin stopped!");
        
        // Remove overlay from overlay manager
        overlayManager.remove(overlay);
        
        // Save window position and size before disposing
        if (floatingWindow != null) {
            SwingUtilities.invokeLater(() -> {
                floatingWindow.savePositionAndSize();
                floatingWindow.dispose();
                floatingWindow = null;
            });
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        updatePlayerInfo();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN) {
            updatePlayerInfo();
        }
    }

    @Subscribe
    public void onPlayerChanged(PlayerChanged event) {
        updatePlayerInfo();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("afkoverlay")) {
            // Handle show overlay button
            if (event.getKey().equals("showOverlay") && config.showOverlay()) {
                if (floatingWindow == null) {
                    createAndShowWindow();
                } else if (!floatingWindow.isVisible()) {
                    floatingWindow.setVisible(true);
                }
                configManager.setConfiguration("afkoverlay", "showOverlay", false);
            }
            
            // Handle display options changes
            if (event.getKey().startsWith("show") && 
                (event.getKey().equals("showHp") || 
                event.getKey().equals("showPrayer") || 
                event.getKey().equals("showStatus") || 
                event.getKey().equals("showInventory"))) {
                if (floatingWindow != null) {
                    SwingUtilities.invokeLater(() -> floatingWindow.updateConfig());
                }
            }
            
            // Update the floating window when other config changes
            if (floatingWindow != null) {
                SwingUtilities.invokeLater(() -> floatingWindow.updateConfig());
            }
        }
    }

    private void updatePlayerInfo() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        // Update HP using skill levels
        int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
        if (currentHp > 0 && maxHp > 0) {
            playerInfo.setCurrentHp(currentHp);
            playerInfo.setMaxHp(maxHp);
        }

        // Update Prayer
        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
        if (currentPrayer > 0 && maxPrayer > 0) {
            playerInfo.setCurrentPrayer(currentPrayer);
            playerInfo.setMaxPrayer(maxPrayer);
        }

        // Idle detection using Instant.now()
        boolean isIdleNow = isPlayerIdle(player);
        Instant now = Instant.now();
        if (!isIdleNow) {
            lastActive = now;
            playerInfo.setIdle(false);
        } else {
            long millisSinceActive = java.time.Duration.between(lastActive, now).toMillis();
            int idleThreshold = config.idleThresholdMs();
            if (millisSinceActive >= idleThreshold) {
                playerInfo.setIdle(true);
            } else {
                playerInfo.setIdle(false);
            }
        }

        // Update inventory usage
        updateInventoryUsage();

        // Update character name
        updateCharacterName(player);
        
        // Update protection prayer
        updateProtectionPrayer(player);

        // Update the floating window
        if (floatingWindow != null) {
            SwingUtilities.invokeLater(() -> {
                floatingWindow.updateDisplay();
                floatingWindow.updateCharacterName(playerInfo.getCharacterName());
            });
        }
    }

    private boolean isPlayerIdle(Player player) {
        // Get the player's current animation, pose, and idle pose
        int animation = player.getAnimation();
        int pose = player.getPoseAnimation();
        int idlePose = player.getIdlePoseAnimation();

        // If the player is performing any animation (e.g., skilling, combat), they are active
        if (animation != -1) {
            return false;
        }

        // If the player's pose is not the idle pose, they are moving (walking/running), so they are active
        if (pose != idlePose) {
            return false;
        }

        // Use isInteracting() method from Actor class for more accurate interaction detection
        if (player.isInteracting()) {
            return false;
        }

        // If no animation, pose is idle, and not interacting, player is idle
        return true;
    }

    private void updateInventoryUsage() {
        try {
            ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
            if (inventory != null && inventory.getItems() != null) {
                Item[] items = inventory.getItems();
                int usedSlots = 0;
                for (Item item : items) {
                    if (item != null && item.getId() != -1) {
                        usedSlots++;
                    }
                }
                // Fix: Calculate percentage based on used slots, not total slots
                int totalSlots = 28; // RuneScape inventory has 28 slots
                playerInfo.setInventoryUsedSlots(usedSlots);
                log.debug("Inventory: {} used slots out of {}", usedSlots, totalSlots);
            } else {
                playerInfo.setInventoryUsedSlots(0);
                log.debug("Inventory container is null or has no items");
            }
        } catch (Exception e) {
            // If there's any error, set to 0
            playerInfo.setInventoryUsedSlots(0);
            log.debug("Error getting inventory: {}", e.getMessage());
        }
    }

    private void updateCharacterName(Player player) {
        String name = player.getName();
        if (name != null && !name.equals(playerInfo.getCharacterName())) {
            playerInfo.setCharacterName(name);
            log.debug("Character name updated: {}", name);
        }
    }
    
    private void updateProtectionPrayer(Player player) {
        // Check player's overhead icon to determine active protection prayer
        HeadIcon overheadIcon = player.getOverheadIcon();
        String activePrayer = "";
        
        // Check the HeadIcon enum to determine active protection prayer
        if (overheadIcon != null) {
            String iconName = overheadIcon.name();
            
            if (iconName.contains("MELEE")) {
                activePrayer = "melee";
            } else if (iconName.contains("MISSILES") || iconName.contains("RANGED")) {
                activePrayer = "ranged";
            } else if (iconName.contains("MAGIC")) {
                activePrayer = "magic";
            } else {
                activePrayer = ""; // No protection prayer active
            }
        } else {
            activePrayer = ""; // No protection prayer active
        }
        
        playerInfo.setActiveProtectionPrayer(activePrayer);
        log.debug("Protection prayer: {}", activePrayer);
    }

    @Provides
    AFKOverlayConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AFKOverlayConfig.class);
    }
} 