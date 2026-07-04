package com.afkoverlay;

import lombok.Data;

@Data
public class PlayerInfo {
    public PlayerInfo() {
    }

    public PlayerInfo(PlayerInfo other) {
        this.currentHp = other.currentHp;
        this.maxHp = other.maxHp;
        this.currentPrayer = other.currentPrayer;
        this.maxPrayer = other.maxPrayer;
        this.idle = other.idle;
        this.inventoryUsedSlots = other.inventoryUsedSlots;
        this.specialAttackEnergy = other.specialAttackEnergy;
        this.characterName = other.characterName;
        this.activeProtectionPrayer = other.activeProtectionPrayer;
        this.enemyName = other.enemyName;
        this.enemyHealthPercent = other.enemyHealthPercent;
    }

    private int currentHp = 0;
    private int maxHp = 0;
    private int currentPrayer = 0;
    private int maxPrayer = 0;
    private boolean idle = false;
    private int inventoryUsedSlots = 0;
    private int specialAttackEnergy = 0;
    private String characterName = "";
    private String activeProtectionPrayer = ""; // "melee", "magic", "ranged", or empty string
    private String enemyName = "";
    private int enemyHealthPercent = -1; // -1 means no target

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
    int totalSlots = 28;
    int usagePercentage = (inventoryUsedSlots * 100) / totalSlots;
    return String.format("%d/28 (%d%%)", inventoryUsedSlots, usagePercentage);
}

public int getSpecialAttackEnergyPercentage() {
    return specialAttackEnergy;
}

public String getSpecialAttackText() {
    return String.format("%d%%", specialAttackEnergy);
}
    
    public String getActiveProtectionPrayer() {
        return activeProtectionPrayer;
    }
    
    public void setActiveProtectionPrayer(String prayer) {
        this.activeProtectionPrayer = prayer;
    }

    public String getEnemyHealthText() {
        if (enemyHealthPercent < 0 || enemyName.isEmpty()) {
            return "";
        }
        return String.format("%s: %d%%", enemyName, enemyHealthPercent);
    }

    public boolean hasEnemyTarget() {
        return enemyHealthPercent >= 0 && !enemyName.isEmpty();
    }
}
