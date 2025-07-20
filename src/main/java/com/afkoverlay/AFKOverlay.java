package com.afkoverlay;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;

public class AFKOverlay extends Overlay {
    private final Client client;
    private final PlayerInfo playerInfo;

    @Inject
    public AFKOverlay(Client client, PlayerInfo playerInfo) {
        this.client = client;
        this.playerInfo = playerInfo;
        
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (client.getGameState() != net.runelite.api.GameState.LOGGED_IN) {
            return null;
        }

        // This overlay is currently not used since we're using a floating window
        // but it's here for potential future use or as a fallback
        return null;
    }
} 