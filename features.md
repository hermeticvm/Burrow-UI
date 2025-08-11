# Burrow UI Features

## Icon Size Customization

**Status: ✅ Completed**

The launcher now includes functionality to customize the icon size and adapt the font size proportionally for accessibility purposes.

### Implementation Details

- **Settings Location**: Settings → Icon Size (slider control)
- **Range**: 24dp to 64dp (current default: 44dp)
- **Font Scaling**: Font size scales proportionally with icon size
- **Accessibility**: Maximum size of 64dp provides better visibility for users with visual impairments

### Technical Implementation

1. **SettingsManager**: Added `getIconSize()` and `setIconSize()` methods with persistent storage
2. **SliderSettingsItem**: New custom component for intuitive size adjustment via slider
3. **MainActivity**: Dynamic icon sizing and proportional font scaling for both app icons and settings icon
4. **SettingsActivity**: Added slider control in settings menu

### Usage

1. Open Settings (long press on home screen or via settings icon)
2. Navigate to "Icon Size" setting
3. Use the slider to adjust icon size from 24dp to 64dp
4. Changes apply immediately when returning to the home screen

### Benefits

- **Accessibility**: Larger icons improve visibility for users with visual impairments
- **Customization**: Users can adjust to their preferred icon density
- **Proportional Design**: Font size automatically scales to maintain visual harmony
- **Persistent**: Settings are saved and persist across app restarts
