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
        this.cannonDeployed = other.cannonDeployed;
        this.cannonAmmo = other.cannonAmmo;
        this.cannonMinutesDeployed = other.cannonMinutesDeployed;
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
    private boolean cannonDeployed = false;
    private int cannonAmmo = 0;
    private int cannonMinutesDeployed = 0;

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

    public String getCannonText() {
        if (!cannonDeployed) {
            return "Not deployed";
        }
        return String.format("%d (%dm)", cannonAmmo, cannonMinutesDeployed);
    }

    public boolean isCannonIdle(int ammoThreshold, int minutesThreshold) {
        if (!cannonDeployed) {
            return false;
        }
        return cannonAmmo <= ammoThreshold || cannonMinutesDeployed >= minutesThreshold;
    }
    
    public String getActiveProtectionPrayer() {
        return activeProtectionPrayer;
    }
    
    public void setActiveProtectionPrayer(String prayer) {
        this.activeProtectionPrayer = prayer;
    }
}
