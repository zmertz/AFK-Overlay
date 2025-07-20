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

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.io.IOException;

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

    private FloatingOverlayWindow floatingWindow;
    private PlayerInfo playerInfo;

    @Override
    protected void startUp() throws Exception {
        log.info("AFK Overlay plugin started!");
        
        // Initialize player info
        playerInfo = new PlayerInfo();
        
        // Create and show floating overlay window
        SwingUtilities.invokeLater(() -> {
            floatingWindow = new FloatingOverlayWindow(playerInfo, config);
            
            // Set custom icon for the window (using the plugin hub icon)
            try {
                Image icon = ImageIO.read(getClass().getResourceAsStream("/icon.png"));
                if (icon != null) {
                    floatingWindow.setIconImage(icon);
                    // Also try setting it as a list for better compatibility
                    floatingWindow.setIconImages(java.util.Arrays.asList(icon));
                }
            } catch (IOException | IllegalArgumentException e) {
                // Silently fall back to default icon
            }
            
            floatingWindow.setVisible(true);
        });
        
        // Add overlay to overlay manager (for potential future use)
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("AFK Overlay plugin stopped!");
        
        // Remove overlay from overlay manager
        overlayManager.remove(overlay);
        
        // Dispose of floating window
        if (floatingWindow != null) {
            SwingUtilities.invokeLater(() -> {
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
            // Update the floating window when config changes
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

        // Update idle status - check if player is performing any action
        boolean isIdle = isPlayerIdle(player);
        playerInfo.setIdle(isIdle);

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
        // Check if player is performing any action
        if (player.getAnimation() != -1) {
            return false; // Player is animating (fishing, woodcutting, etc.)
        }
        
        // Check if player is in combat or interacting
        if (player.getInteracting() != null) {
            return false; // Player is interacting with something (combat, etc.)
        }
        
        // If no animation, no interaction, player is idle
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
                int totalSlots = 28; // RuneScape inventory has 28 slots
                int usagePercentage = (usedSlots * 100) / totalSlots;
                playerInfo.setInventoryUsage(usagePercentage);
                log.debug("Inventory: {} used slots out of {}, {}%", usedSlots, totalSlots, usagePercentage);
            } else {
                playerInfo.setInventoryUsage(0);
                log.debug("Inventory container is null or has no items");
            }
        } catch (Exception e) {
            // If there's any error, set to 0
            playerInfo.setInventoryUsage(0);
            log.debug("Error getting inventory: {}", e.getMessage());
        }
    }

    private void updateCharacterName(Player player) {
        String name = player.getName();
        if (!name.equals(playerInfo.getCharacterName())) {
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