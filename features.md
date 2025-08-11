# Burrowed Launcher Features

## Icon Size Customization

**Status: ✅ Completed**

The launcher now includes functionality to customize the icon size and adapt the font size proportionally for accessibility purposes.

### Implementation Details

- **Settings Location**: Settings → Icon Size (slider control)
- **Range**: 32dp to 96dp (current default: 44dp)
- **Font Scaling**: Font size scales proportionally with icon size
- **Accessibility**: Maximum size of 96dp provides better visibility for users with visual impairments

### Technical Implementation

1. **SettingsManager**: Added `getIconSize()` and `setIconSize()` methods with persistent storage
2. **SliderSettingsItem**: New custom component for intuitive size adjustment via slider
3. **MainActivity**: Dynamic icon sizing and proportional font scaling for both app icons and settings icon
4. **SettingsActivity**: Added slider control in settings menu

### Usage

1. Open Settings (long press on home screen or via settings icon)
2. Navigate to "Icon Size" setting
3. Use the slider to adjust icon size from 32dp to 96dp
4. Changes apply immediately when returning to the home screen

### Benefits

- **Accessibility**: Larger icons improve visibility for users with visual impairments
- **Customization**: Users can adjust to their preferred icon density
- **Proportional Design**: Font size automatically scales to maintain visual harmony
- **Persistent**: Settings are saved and persist across app restarts

## Search Functionality

**Status: ✅ Completed**

Replaced pull-to-search with an inline search bar for better usability with alphabetical navigation.

### Implementation Details

- **Location**: Below time/date display, above app list
- **Trigger**: Click the search icon to show/hide search bar
- **Functionality**: Real-time filtering as you type
- **Clear**: X button to clear search and return to full list

### Technical Implementation

1. **MainActivity**: Added search EditText with real-time filtering
2. **Adaptive Sizing**: Search bar height and font size match app icon settings
3. **Keyboard Integration**: Automatic keyboard show/hide
4. **Back Button**: Returns to full app list when search is active

### Usage

1. **Open Search**: Click the search icon below the time/date
2. **Type to Filter**: Start typing to filter apps by name or package
3. **Clear Search**: Click the X button or press back button
4. **Close Search**: Click search icon again or press back

### Benefits

- **No Interference**: Doesn't conflict with alphabetical navigation
- **Immediate Feedback**: Real-time filtering as you type
- **Easy Access**: One click to open, one click to close
- **E-ink Optimized**: Minimal visual impact, high contrast
- **Adaptive**: Search bar size matches your icon size preference
