# AFK Overlay Plugin

A RuneLite plugin that displays your player stats in a floating overlay window that stays on top of other applications.

## Features

### **Real-time Stats Display**
- **HP**: Current/Max with percentage and color coding
- **Prayer**: Current/Max with percentage and color coding  
- **Special Attack**: Current percentage with color coding
- **Inventory**: Used slots out of 28 with percentage
- **Status**: Shows if you're Active or Idle
- **Character Name**: Displays at the top of the overlay

### **Sound Notifications**
- Plays a sound every 2 seconds when a stat reaches its configured threshold.
- Individual toggles for each stat.
- Adjustable volume.

### **Interactive Window**
- **Drag**: Click and hold the top area to move the overlay
- **Resize**: Drag edges or corners to resize
- **Click to Focus**: Click to bring RuneLite window to front
- **Always on Top**: Stays visible over other applications

## Configuration

### Appearance
- **Opacity**: Adjust window transparency (0 = fully transparent, 255 = fully opaque)
- **Show Character Name**: Show the character name in the overlay.
- **Show Window Border**: Show the window border.
- **Show Minimize Button**: Display minimize button
- **Show Close Button**: Display close button

### Sounds
- **Sound Volume**: Adjust the volume of the sound notifications.
- **Play sound when threshold reached**: Individual toggles for HP, Prayer, Special Attack, Inventory, and Idle status.

## Usage

1. **Enable the plugin** in RuneLite
2. **Position the overlay** where you want it on screen
3. **Resize if needed** by dragging the edges
4. **Monitor your stats** - colors will warn you when levels are low
5. **Watch for protection prayers** - the prayer icon changes when you use overhead protection prayers

The overlay automatically updates every game tick, so you'll always see current information without needing to check the main game window.
