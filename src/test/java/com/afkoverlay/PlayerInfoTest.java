package com.afkoverlay;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlayerInfoTest {

    @Test
    public void getCannonText_notDeployed_showsNotDeployed() {
        PlayerInfo p = new PlayerInfo();
        p.setCannonDeployed(false);
        assertEquals("Not deployed", p.getCannonText());
    }

    @Test
    public void getCannonText_deployed_showsAmmoAndMinutes() {
        PlayerInfo p = new PlayerInfo();
        p.setCannonDeployed(true);
        p.setCannonAmmo(20);
        p.setCannonMinutesDeployed(3);
        assertEquals("20 (3m)", p.getCannonText());
    }

    @Test
    public void cannonIdle_notDeployed_false() {
        PlayerInfo p = new PlayerInfo();
        p.setCannonDeployed(false);
        p.setCannonAmmo(0);
        p.setCannonMinutesDeployed(99);
        assertFalse(p.isCannonIdle(5, 20));
    }

    @Test
    public void cannonIdle_lowAmmo_true() {
        PlayerInfo p = new PlayerInfo();
        p.setCannonDeployed(true);
        p.setCannonAmmo(5);
        p.setCannonMinutesDeployed(0);
        assertTrue(p.isCannonIdle(5, 20));
    }

    @Test
    public void cannonIdle_enoughAmmo_false() {
        PlayerInfo p = new PlayerInfo();
        p.setCannonDeployed(true);
        p.setCannonAmmo(15);
        p.setCannonMinutesDeployed(0);
        assertFalse(p.isCannonIdle(5, 20));
    }

    @Test
    public void cannonIdle_overTime_true() {
        PlayerInfo p = new PlayerInfo();
        p.setCannonDeployed(true);
        p.setCannonAmmo(30);
        p.setCannonMinutesDeployed(20);
        assertTrue(p.isCannonIdle(5, 20));
    }

    @Test
    public void cannonIdle_underTime_false() {
        PlayerInfo p = new PlayerInfo();
        p.setCannonDeployed(true);
        p.setCannonAmmo(30);
        p.setCannonMinutesDeployed(19);
        assertFalse(p.isCannonIdle(5, 20));
    }
}
