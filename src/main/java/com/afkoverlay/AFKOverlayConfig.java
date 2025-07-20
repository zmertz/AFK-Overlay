package com.afkoverlay;

import net.runelite.client.config.*;

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


}
