# AFK Overlay Plugin

A RuneLite plugin that displays your player stats in a floating overlay window that stays on top of other applications.

## Features

### **Real-time Stats Display**
- **HP**: Current/Max with percentage and color coding
- **Prayer**: Current/Max with percentage and color coding  
- **Inventory**: Used slots out of 28 with percentage
- **Status**: Shows if you're Active or Idle
- **Character Name**: Displays at the top of the overlay

### **Smart Color Coding**
- **Green/Normal**: Safe levels (>50% HP/Prayer, <50% Inventory)
- **Yellow/Warning**: Getting low (11-50% HP/Prayer, 50-89% Inventory)
- **Red/Danger**: Critical levels (≤10% HP/Prayer, ≥90% Inventory)
- **Blue**: Prayer shows in soft blue when above 50%

### **Interactive Window**
- **Drag**: Click and hold the top area to move the overlay
- **Resize**: Drag edges or corners to resize
- **Click to Focus**: Click anywhere else to bring RuneLite window to front
- **Always on Top**: Stays visible over other applications

## Configuration

Access settings via **RuneLite → Configuration → AFK Overlay**

### Appearance
- **Opacity**: Adjust window transparency (0 = fully transparent, 255 = fully opaque)
- **Show Minimize Button**: Display minimize button
- **Show Close Button**: Display close button

## Usage

1. **Enable the plugin** in RuneLite
2. **Position the overlay** where you want it on screen
3. **Resize if needed** by dragging the edges
4. **Monitor your stats** - colors will warn you when levels are low
5. **Watch for protection prayers** - the prayer icon changes when you use overhead protection prayers

The overlay automatically updates every game tick, so you'll always see current information without needing to check the main game window.

