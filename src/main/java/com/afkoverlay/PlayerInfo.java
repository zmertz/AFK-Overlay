package com.afkoverlay;

import lombok.Data;

@Data
public class PlayerInfo {
    private int currentHp = 0;
    private int maxHp = 0;
    private int currentPrayer = 0;
    private int maxPrayer = 0;
    private boolean idle = false;
    private int inventoryUsage = 0;
    private String characterName = "";
    private String activeProtectionPrayer = ""; // "melee", "magic", "ranged", or empty string

    public int getHpPercentage() {
        if (maxHp == 0) return 0;
        return (currentHp * 100) / maxHp;
    }

    public int getPrayerPercentage() {
        if (maxPrayer == 0) return 0;
        return (currentPrayer * 100) / maxPrayer;
    }

    public String getStatusText() {
        return idle ? "IDLE" : "ACTIVE";
    }

    public String getHpText() {
        return String.format("%d/%d (%d%%)", currentHp, maxHp, getHpPercentage());
    }

    public String getPrayerText() {
        return String.format("%d/%d (%d%%)", currentPrayer, maxPrayer, getPrayerPercentage());
    }

    public String getInventoryText() {
        int usedSlots = (inventoryUsage * 28) / 100;
        return String.format("%d/28 (%d%%)", usedSlots, inventoryUsage);
    }
    
    public String getActiveProtectionPrayer() {
        return activeProtectionPrayer;
    }
    
    public void setActiveProtectionPrayer(String prayer) {
        this.activeProtectionPrayer = prayer;
    }
} 