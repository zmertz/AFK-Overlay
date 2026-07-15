package com.afkoverlay;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.PlayerChanged;
import net.runelite.api.gameval.NpcID;
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
import java.util.Optional;
import java.util.Set;

@Slf4j
@PluginDescriptor(
    name = "AFK Overlay",
    description = "Displays player information in a floating overlay window",
    tags = {"overlay", "player", "stats", "afk"}
)
public class AFKOverlayPlugin extends Plugin {

    private static final Set<Integer> EVENT_NPCS = ImmutableSet.of(
            NpcID.MACRO_BEEKEEPER_INVITATION,
            NpcID.MACRO_COMBILOCK_PIRATE,
            NpcID.MACRO_JEKYLL, NpcID.MACRO_JEKYLL_UNDERWATER,
            NpcID.MACRO_DWARF,
            NpcID.PATTERN_INVITATION,
            NpcID.MACRO_EVIL_BOB_OUTSIDE, NpcID.MACRO_EVIL_BOB_PRISON,
            NpcID.PINBALL_INVITATION,
            NpcID.MACRO_FORESTER_INVITATION,
            NpcID.MACRO_FROG_CRIER,
            NpcID.MACRO_GENI, NpcID.MACRO_GENI_UNDERWATER,
            NpcID.MACRO_GILES, NpcID.MACRO_GILES_UNDERWATER,
            NpcID.MACRO_GRAVEDIGGER_INVITATION,
            NpcID.MACRO_MILES, NpcID.MACRO_MILES_UNDERWATER,
            NpcID.MACRO_MYSTERIOUS_OLD_MAN, NpcID.MACRO_MYSTERIOUS_OLD_MAN_UNDERWATER,
            NpcID.MACRO_MAZE_INVITATION, NpcID.MACRO_MIME_INVITATION,
            NpcID.MACRO_NILES, NpcID.MACRO_NILES_UNDERWATER,
            NpcID.MACRO_PILLORY_GUARD,
            NpcID.GRAB_POSTMAN,
            NpcID.MACRO_MAGNESON_INVITATION,
            NpcID.MACRO_HIGHWAYMAN, NpcID.MACRO_HIGHWAYMAN_UNDERWATER,
            NpcID.MACRO_SANDWICH_LADY_NPC,
            NpcID.MACRO_DRILLDEMON_INVITATION,
            NpcID.MACRO_COUNTCHECK_SURFACE, NpcID.MACRO_COUNTCHECK_UNDERWATER
    );

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
    private PlayerInfo previousPlayerInfo;
    // Track the last time the player was active
    private Instant lastActive = Instant.now();
    // Track if window was closed by user
    private boolean windowClosedByUser = false;
    private Instant lastSoundPlayed = Instant.now();
    private static final int SOUND_ID = 3817;

    private Optional<NPC> currentRandomEvent = Optional.empty();


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

        currentRandomEvent = Optional.empty();
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        updatePlayerInfo();
        checkThresholdsAndPlaySounds();
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
                previousPlayerInfo = null;
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

        // Update special attack energy
        int specialAttackEnergy = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
        playerInfo.setSpecialAttackEnergy(specialAttackEnergy);

        // Update character name
        updateCharacterName(player);
        
        // Update protection prayer
        updateProtectionPrayer(player);

        playerInfo.setCurrentRandomEvent(currentRandomEvent.map(NPC::getName).orElse(null));

        // Update the floating window
        if (floatingWindow != null && (previousPlayerInfo == null || !previousPlayerInfo.equals(playerInfo))) {
            previousPlayerInfo = new PlayerInfo(playerInfo);
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

        // If no animation and pose is idle, player is idle
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

    private void checkThresholdsAndPlaySounds() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        Instant now = Instant.now();
        if (java.time.Duration.between(lastSoundPlayed, now).toMillis() < 2000) {
            return;
        }

        boolean playSound = false;

        // Check HP
        if (config.playHpSound() && playerInfo.getCurrentHp() > 0 && playerInfo.getCurrentHp() <= config.lowHpThresholdValue()) {
            playSound = true;
        }

        // Check Prayer
        if (config.playPrayerSound() && playerInfo.getCurrentPrayer() > 0 && playerInfo.getCurrentPrayer() <= config.lowPrayerThresholdValue()) {
            playSound = true;
        }

        // Check Special Attack
        if (config.playSpecialAttackSound() && playerInfo.getSpecialAttackEnergyPercentage() >= config.highSpecialAttackThresholdValue()) {
            playSound = true;
        }

        // Check Inventory
        if (config.playInvSound()) {
            int invCount = playerInfo.getInventoryUsedSlots();
            boolean invThresholdMet = false;
            switch (config.invHighlightMode()) {
                case ABOVE:
                    if (invCount > config.invThresholdValue()) invThresholdMet = true;
                    break;
                case BELOW:
                    if (invCount < config.invThresholdValue()) invThresholdMet = true;
                    break;
                case EQUALS:
                    if (invCount == config.invThresholdValue()) invThresholdMet = true;
                    break;
            }
            if (invThresholdMet) {
                playSound = true;
            }
        }

        // Check Idle Status
        if (config.playIdleSound() && playerInfo.isIdle()) {
            playSound = true;
        }

        if (playSound) {
            lastSoundPlayed = now;
            Preferences preferences = client.getPreferences();
            int previousVolume = preferences.getSoundEffectVolume();
            preferences.setSoundEffectVolume(config.soundVolume());
            client.playSoundEffect(SOUND_ID, config.soundVolume());
            preferences.setSoundEffectVolume(previousVolume);
        }
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        Actor source = event.getSource();
        Actor target = event.getTarget();
        Player player = client.getLocalPlayer();

        // Check that the npc is interacting with the player and the player isn't interacting with the npc, so
        // that the notification doesn't fire from talking to other user's randoms
        if (player == null
                || target != player
                || player.getInteracting() == source
                || !(source instanceof NPC)
                || !EVENT_NPCS.contains(((NPC) source).getId()))
        {
            return;
        }

        log.debug("Random event spawn: {}", source.getName());

        currentRandomEvent = Optional.of((NPC) source);
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned)
    {
        NPC npc = npcDespawned.getNpc();

        if (currentRandomEvent.isPresent() && npc == currentRandomEvent.get())
        {
            currentRandomEvent = Optional.empty();
        }
    }

}
