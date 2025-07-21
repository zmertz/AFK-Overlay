package com.afkoverlay;

import net.runelite.client.config.*;

import java.awt.Color;

@ConfigGroup("afkoverlay")
public interface AFKOverlayConfig extends Config {

    @ConfigSection(
        name = "Appearance",
        description = "Appearance settings for the overlay",
        position = 1
    )
    String appearanceSection = "appearance";



    @ConfigItem(
        keyName = "showCloseButton",
        name = "Show Close Button",
        description = "Show a close button on the overlay",
        section = appearanceSection,
        position = 1
    )
    default boolean showCloseButton() {
        return true;
    }

    @ConfigItem(
        keyName = "showMinimizeButton",
        name = "Show Minimize Button",
        description = "Show a minimize button on the overlay",
        section = appearanceSection,
        position = 2
    )
    default boolean showMinimizeButton() {
        return true;
    }

    @ConfigItem(
        keyName = "opacity",
        name = "Opacity",
        description = "Set the opacity level of the overlay (0 = fully transparent, 255 = fully opaque)",
        section = appearanceSection,
        position = 3
    )
    @Range(min = 0, max = 255)
    default int opacity() {
        return 200;
    }

    // --- Hitpoints Section ---
    @ConfigSection(
        name = "Hitpoints",
        description = "Highlight background when hitpoints are low.",
        position = 20
    )
    String hitpointsSection = "hitpointsSection";

    @ConfigItem(
        keyName = "highlightHpBackground",
        name = "Highlight background when low",
        description = "Highlight background when hitpoints are low.",
        section = hitpointsSection,
        position = 1
    )
    default boolean highlightHpBackground() { return false; }

    @ConfigItem(
        keyName = "lowHpThresholdValue",
        name = "Threshold value",
        description = "Show highlight when HP is at or below this value.",
        section = hitpointsSection,
        position = 2
    )
    @Range(min = 1, max = 99)
    default int lowHpThresholdValue() { return 10; }

    @Alpha
    @ConfigItem(
        keyName = "lowHpOverlayColor",
        name = "Threshold color",
        description = "Background color when HP is low.",
        section = hitpointsSection,
        position = 3
    )
    default Color lowHpOverlayColor() { return new Color(0xFF020421); }

    // --- Prayer Section ---
    @ConfigSection(
        name = "Prayer",
        description = "Highlight background when prayer is low.",
        position = 30
    )
    String prayerSection = "prayerSection";

    @ConfigItem(
        keyName = "highlightPrayerBackground",
        name = "Highlight background when low",
        description = "Highlight background when prayer is low.",
        section = prayerSection,
        position = 1
    )
    default boolean highlightPrayerBackground() { return false; }

    @ConfigItem(
        keyName = "lowPrayerThresholdValue",
        name = "Threshold value",
        description = "Show highlight when prayer is at or below this value.",
        section = prayerSection,
        position = 2
    )
    @Range(min = 1, max = 99)
    default int lowPrayerThresholdValue() { return 10; }

    @Alpha
    @ConfigItem(
        keyName = "lowPrayerOverlayColor",
        name = "Threshold color",
        description = "Background color when prayer is low.",
        section = prayerSection,
        position = 3
    )
    default Color lowPrayerOverlayColor() { return new Color(0xFF020421); }

    // --- Status Section ---
    @ConfigSection(
        name = "Status",
        description = "Highlight background when idle.",
        position = 50
    )
    String statusSection = "statusSection";

    @ConfigItem(
        keyName = "highlightIdleBackground",
        name = "Highlight background when idle",
        description = "Highlight background when status is idle.",
        section = statusSection,
        position = 1
    )
    default boolean highlightIdleBackground() { return false; }

    @Alpha
    @ConfigItem(
        keyName = "idleOverlayColor",
        name = "Threshold color",
        description = "Background color when idle.",
        section = statusSection,
        position = 2
    )
    default Color idleOverlayColor() { return new Color(0xFF020421); }

    @ConfigItem(
        keyName = "idleThresholdMs",
        name = "Idle threshold (ms)",
        description = "How long (in milliseconds) the player must be idle before the overlay shows IDLE. Set to 0 for instant.",
        section = statusSection,
        position = 3
    )
    @Range(
        min = 0,
        max = 5000
    )
    default int idleThresholdMs() {
        return 1200;
    }

    // --- Inventory Section ---
    @ConfigSection(
        name = "Inventory",
        description = "Highlight background based on inventory items.",
        position = 40
    )
    String inventorySection = "inventorySection";

    @ConfigItem(
        keyName = "highlightInvBackground",
        name = "Highlight background",
        description = "Highlight background based on inventory items.",
        section = inventorySection,
        position = 1
    )
    default boolean highlightInvBackground() { return false; }

    enum InventoryHighlightMode {
        ABOVE,
        BELOW
    }

    @ConfigItem(
        keyName = "invHighlightMode",
        name = "Highlight when:",
        description = "Highlight when inventory items are above or below the threshold.",
        section = inventorySection,
        position = 2
    )
    default InventoryHighlightMode invHighlightMode() { return InventoryHighlightMode.ABOVE; }

    @ConfigItem(
        keyName = "invThresholdValue",
        name = "Threshold value",
        description = "Number of inventory items to trigger highlight.",
        section = inventorySection,
        position = 3
    )
    @Range(min = 1, max = 28)
    default int invThresholdValue() { return 28; }

    @Alpha
    @ConfigItem(
        keyName = "invOverlayColor",
        name = "Threshold color",
        description = "Background color when inventory highlight is triggered.",
        section = inventorySection,
        position = 4
    )
    default Color invOverlayColor() { return new Color(0xFF020421); }


}
