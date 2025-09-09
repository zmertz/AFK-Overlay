package com.afkoverlay;

import net.runelite.client.config.*;

import java.awt.Color;

@ConfigGroup("afkoverlay")
public interface AFKOverlayConfig extends Config {

    // --- Hitpoints Section ---
    @ConfigSection(
        name = "Hitpoints",
        description = "Highlight background when hitpoints are low.",
        position = 20
    )
    String hitpointsSection = "hitpointsSection";

    @ConfigItem(
        keyName = "showHp",
        name = "Show Hitpoints",
        description = "Display hitpoints in the overlay.",
        section = hitpointsSection,
        position = 1
    )
default boolean showHp() { return true; }

    @ConfigItem(
        keyName = "highlightHpBackground",
        name = "Highlight background when low",
        description = "Highlight background when hitpoints are low.",
        section = hitpointsSection,
        position = 2
    )
    default boolean highlightHpBackground() { return false; }

    @ConfigItem(
        keyName = "lowHpThresholdValue",
        name = "Threshold value",
        description = "Show highlight when HP is at or below this value.",
        section = hitpointsSection,
        position = 3
    )
    @Range(min = 1, max = 99)
    default int lowHpThresholdValue() { return 10; }

    @Alpha
    @ConfigItem(
        keyName = "lowHpOverlayColor",
        name = "Threshold color",
        description = "Background color when HP is low.",
        section = hitpointsSection,
        position = 4
    )
    default Color lowHpOverlayColor() { return new Color(0xFF020421); }

    @ConfigItem(
        keyName = "playHpSound",
        name = "Play sound",
        description = "Play a sound when HP is at or below the threshold.",
        section = hitpointsSection,
        position = 5
    )
    default boolean playHpSound() { return false; }

    // --- Prayer Section ---
    @ConfigSection(
        name = "Prayer",
        description = "Highlight background when prayer is low.",
        position = 30
    )
    String prayerSection = "prayerSection";

    @ConfigItem(
        keyName = "showPrayer",
        name = "Show Prayer",
        description = "Display prayer points in the overlay.",
        section = prayerSection,
        position = 1
    )
    default boolean showPrayer() { return true; }

    @ConfigItem(
        keyName = "highlightPrayerBackground",
        name = "Highlight background when low",
        description = "Highlight background when prayer is low.",
        section = prayerSection,
        position = 2
    )
    default boolean highlightPrayerBackground() { return false; }

    @ConfigItem(
        keyName = "lowPrayerThresholdValue",
        name = "Threshold value",
        description = "Show highlight when prayer is at or below this value.",
        section = prayerSection,
        position = 3
    )
    @Range(min = 1, max = 99)
    default int lowPrayerThresholdValue() { return 10; }

    @Alpha
    @ConfigItem(
        keyName = "lowPrayerOverlayColor",
        name = "Threshold color",
        description = "Background color when prayer is low.",
        section = prayerSection,
        position = 4
    )
    default Color lowPrayerOverlayColor() { return new Color(0xFF020421); }

    @ConfigItem(
        keyName = "playPrayerSound",
        name = "Play sound",
        description = "Play a sound when prayer is at or below the threshold.",
        section = prayerSection,
        position = 5
    )
    default boolean playPrayerSound() { return false; }

    // --- Inventory Section ---
    @ConfigSection(
        name = "Inventory",
        description = "Highlight background based on inventory items.",
        position = 40
    )
    String inventorySection = "inventorySection";

    // --- Special Attack Section ---
    @ConfigSection(
        name = "Special Attack",
        description = "Highlight background when special attack is high.",
        position = 45
    )
    String specialAttackSection = "specialAttackSection";

    @ConfigItem(
        keyName = "showSpecialAttack",
        name = "Show Special Attack",
        description = "Display special attack energy in the overlay.",
        section = specialAttackSection,
        position = 1
    )
    default boolean showSpecialAttack() { return true; }

    @ConfigItem(
        keyName = "highlightSpecialAttackBackground",
        name = "Highlight background when high",
        description = "Highlight background when special attack is high.",
        section = specialAttackSection,
        position = 2
    )
    default boolean highlightSpecialAttackBackground() { return false; }

    @ConfigItem(
        keyName = "highSpecialAttackThresholdValue",
        name = "Threshold value",
        description = "Show highlight when Special Attack is at or above this value.",
        section = specialAttackSection,
        position = 3
    )
    @Range(min = 1, max = 100)
    default int highSpecialAttackThresholdValue() { return 50; }

    @Alpha
    @ConfigItem(
        keyName = "highSpecialAttackOverlayColor",
        name = "Threshold color",
        description = "Background color when Special Attack is high.",
        section = specialAttackSection,
        position = 4
    )
    default Color highSpecialAttackOverlayColor() { return new Color(0xFF020421); }

    @ConfigItem(
        keyName = "playSpecialAttackSound",
        name = "Play sound",
        description = "Play a sound when Special Attack is at or above the threshold.",
        section = specialAttackSection,
        position = 5
    )
    default boolean playSpecialAttackSound() { return false; }

    @ConfigItem(
        keyName = "showInventory",
        name = "Show Inventory",
        description = "Display inventory usage in the overlay.",
        section = inventorySection,
        position = 1
    )
    default boolean showInventory() { return true; }

    @ConfigItem(
        keyName = "highlightInvBackground",
        name = "Highlight background",
        description = "Highlight background based on inventory items.",
        section = inventorySection,
        position = 2
    )
    default boolean highlightInvBackground() { return false; }

    enum InventoryHighlightMode {
        ABOVE,
        BELOW,
        EQUALS
    }

    @ConfigItem(
        keyName = "invHighlightMode",
        name = "Highlight when",
        description = "Highlight when inventory items are above, below, or equal to the threshold.",
        section = inventorySection,
        position = 3
    )
    default InventoryHighlightMode invHighlightMode() { return InventoryHighlightMode.ABOVE; }

    @ConfigItem(
        keyName = "invThresholdValue",
        name = "Threshold value",
        description = "Number of inventory items to trigger highlight.",
        section = inventorySection,
        position = 4
    )
    @Range(min = 1, max = 28)
    default int invThresholdValue() { return 28; }

    @Alpha
    @ConfigItem(
        keyName = "invOverlayColor",
        name = "Threshold color",
        description = "Background color when inventory highlight is triggered.",
        section = inventorySection,
        position = 5
    )
    default Color invOverlayColor() { return new Color(0xFF020421); }

    @ConfigItem(
        keyName = "playInvSound",
        name = "Play sound",
        description = "Play a sound when inventory highlight is triggered.",
        section = inventorySection,
        position = 6
    )
    default boolean playInvSound() { return false; }

    // --- Status Section ---
    @ConfigSection(
        name = "Status",
        description = "Highlight background when idle.",
        position = 50
    )
    String statusSection = "statusSection";

    @ConfigItem(
        keyName = "showStatus",
        name = "Show Status",
        description = "Display status (active/idle) in the overlay.",
        section = statusSection,
        position = 1
    )
    default boolean showStatus() { return true; }

    @ConfigItem(
        keyName = "highlightIdleBackground",
        name = "Highlight background when idle",
        description = "Highlight background when status is idle.",
        section = statusSection,
        position = 2
    )
    default boolean highlightIdleBackground() { return false; }

    @Alpha
    @ConfigItem(
        keyName = "idleOverlayColor",
        name = "Threshold color",
        description = "Background color when idle status is triggered.",
        section = statusSection,
        position = 3
    )
    default Color idleOverlayColor() { return new Color(0xFF020421); }

    @ConfigItem(
        keyName = "idleThresholdMs",
        name = "Idle threshold (ms)",
        description = "Time in milliseconds before player is considered idle.",
        section = statusSection,
        position = 4
    )
    @Range(min = 500, max = 10000)
    default int idleThresholdMs() { return 1200; }

    @ConfigItem(
        keyName = "playIdleSound",
        name = "Play sound when idle",
        description = "Play a sound when the player is idle.",
        section = statusSection,
        position = 5
    )
    default boolean playIdleSound() { return false; }

    // --- Random Events Settings Section ---
    @ConfigSection(
        name = "Random Events",
        description = "Display current random events.",
        position = 60
    )
    String randomEventSection = "randomEventSection";

    @ConfigItem(
        keyName = "showRandomEvents",
        name = "Show Random Events",
        description = "Display current random event in the overlay.",
        section = randomEventSection,
        position = 1
    )
    default boolean showRandomEvents() { return true; }

    // --- Window Settings Section ---
    @ConfigSection(
        name = "General Settings",
        description = "Configure general settings.",
        position = 70
    )
    String windowSection = "windowSection";

    @ConfigItem(
        keyName = "opacity",
        name = "Opacity",
        description = "Set the opacity level of the overlay (0 = fully transparent, 255 = fully opaque)",
        section = windowSection,
        position = 1
    )
    @Range(min = 0, max = 255)
    default int opacity() {
        return 200;
    }

    @ConfigItem(
        keyName = "showCloseButton",
        name = "Show Close Button",
        description = "Show a close button on the overlay",
        section = windowSection,
        position = 2
    )
    default boolean showCloseButton() {
        return true;
    }

    @ConfigItem(
        keyName = "showMinimizeButton",
        name = "Show Minimize Button",
        description = "Show a minimize button on the overlay",
        section = windowSection,
        position = 3
    )
    default boolean showMinimizeButton() {
        return true;
    }


    @ConfigItem(
        keyName = "showCharacterName",
        name = "Show Character Name",
        description = "Show the character name in the overlay.",
        section = windowSection,
        position = 4
    )
    default boolean showCharacterName() { return true; }

    @ConfigItem(
        keyName = "showWindowBorder",
        name = "Show Window Border",
        description = "Show the window border.",
        section = windowSection,
        position = 5
    )
    default boolean showWindowBorder() { return true; }

    @ConfigItem(
        keyName = "soundVolume",
        name = "Sound Volume",
        description = "Volume for the threshold sounds.",
        section = windowSection,
        position = 6
    )
    @Range(min = 0, max = 100)
    default int soundVolume() { return 50; }

        @ConfigItem(
            keyName = "resetPosition",
            name = "Reset Position",
            description = "Click to reset the overlay window position to default (100, 100). (Check box on and off)",
            section = windowSection,
            position = 7
        )
        default boolean resetPosition() { return false; }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Restore Overlay",
        description = "Click to restore the overlay window if it was closed or hidden. (Check box on and off)",
        section = windowSection,
        position = 8
    )
    default boolean showOverlay() { return false; }

}
