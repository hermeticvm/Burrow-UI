# Release v1.5.1 - Dark Mode LCD Support

## 🎯 What's New
This patch release adds comprehensive dark mode support optimized for amber backlit greyscale transflective LCD displays.

## ✨ Features Added
- **Android System Dark Mode Support** - Automatically switches based on system settings
- **High-Contrast Amber LCD Theme** - Black background with amber text for maximum visibility
- **Enhanced Toggle Icons** - Dark mode versions with amber indicators for clear state recognition
- **Improved Borders** - 3dp amber borders for better element separation
- **Optimized Contrast** - All UI elements now clearly visible on transflective displays

## 🔧 Technical Details
- Uses Android's built-in `values-night/` resource system
- No app-specific settings required - respects system dark mode
- Backward compatible - light mode remains unchanged
- Zero configuration - works immediately when system dark mode is enabled

## 📱 How to Use
1. Enable dark mode in Android Settings → Display → Dark theme
2. Or use Quick Settings dark mode toggle
3. The app will automatically switch to amber LCD-optimized theme

## 📦 Files Changed
- Added `values-night/colors.xml` - Dark mode color scheme
- Added `drawable-night/` - Dark mode toggle icons
- Updated border drawables for better visibility
- Enhanced UI element styling for high contrast

## 🎯 Target Devices
Optimized for amber backlit greyscale transflective LCD displays commonly found in:
- Industrial equipment
- Marine navigation systems
- Outdoor displays
- Low-light environments

## 📋 Installation
Download `burrowed-launcher-debug-1.5.1.apk` from the release assets and install on your Android device.
