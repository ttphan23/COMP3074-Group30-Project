# Build & Run Instructions

## Prerequisites
- Android Studio (Arctic Fox or newer)
- Android SDK (API 21 minimum, API 33+ recommended)
- Java JDK 11 or higher
- Android device or emulator

## Building the Project

### Option 1: Using Android Studio (Recommended)
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the project root directory
4. Wait for Gradle sync to complete
5. Click the green "Run" button or press `Shift + F10`
6. Select your device/emulator
7. Wait for app to build and install

### Option 2: Using Command Line (In Cursor)

#### On Windows (PowerShell):
```powershell
# Build debug APK
.\gradlew.bat assembleDebug

# Install on connected device
.\gradlew.bat installDebug

# Or build and install in one step
.\gradlew.bat installDebug
```

#### On Mac/Linux:
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Running the App

### Using ADB (Android Debug Bridge):
```bash
# List connected devices
adb devices

# Install APK manually
adb install app/build/outputs/apk/debug/app-debug.apk

# Start the app
adb shell am start -n ca.gbc.comp3074.comp3074/.LoginActivity
```

## Testing the Features

### 1. Login
- Enter any username and password
- Click "Login" or "Register"
- Both work the same (prototype mode)

### 2. Profile/Home Tab
- View your username
- See trending games section
- Browse your game library
- Click "Logout" to return to login

### 3. Feed Tab
- Follow/unfollow users in "Suggested Friends"
- See reviews from followed users in "Recent Activity"
- Your own reviews also appear here

### 4. Review Tab
- Select a game from dropdown
- Give it a star rating (try clicking stars - they bounce!)
- Write your review
- Click "Submit Review"
- See success animation and Snackbar
- View your reviews in "My Recent Reviews"

## Troubleshooting

### Gradle Sync Issues:
1. File → Invalidate Caches / Restart
2. Clean build: `.\gradlew.bat clean`
3. Rebuild: Build → Rebuild Project

### Device Not Detected:
1. Enable Developer Options on device
2. Enable USB Debugging
3. Accept "Allow USB Debugging" prompt
4. Run `adb devices` to verify

### App Crashes:
1. Check Logcat in Android Studio
2. Common fix: Uninstall old version first
3. Run `adb uninstall ca.gbc.comp3074.comp3074`

### Build Errors:
1. Update Android SDK in SDK Manager
2. Check build.gradle.kts for correct versions
3. Sync Gradle files again

## Default Test Account
- **Username**: Any text (e.g., "TestUser")
- **Password**: Any text (e.g., "password")

## Pre-populated Data
The app comes with:
- 5 dummy users with reviews
- 6 trending games
- 8+ games in review dropdown
- Auto-follows 2 users on first login

## Key Interactions to Test

1. **Animations**:
   - Try submitting review without rating → Shake animation
   - Try submitting without text → Shake animation
   - Change star rating → Bounce animation
   - Submit valid review → Success animations

2. **Follow System**:
   - Follow a user → Button changes to "Unfollow"
   - Unfollow → Button changes back to "Follow"
   - Feed updates automatically

3. **Navigation**:
   - Switch between tabs
   - Data persists across tabs
   - Reviews appear in both Review tab and Feed tab

4. **Session Management**:
   - Login → Auto-logs in next time
   - Logout → Returns to login screen
   - Data persists until logout

## Expected Output
✅ Professional, modern UI
✅ Smooth animations and transitions
✅ Toast and Snackbar notifications
✅ Card-based layouts
✅ Follow/unfollow functionality
✅ Review submission with validation
✅ Trending games display
✅ Friend activity feed

## Performance Notes
- First launch may take 2-3 seconds to initialize dummy data
- All data stored in SharedPreferences (local only)
- No network calls (prototype mode)
- Lightweight and fast

Enjoy testing the Video Game Journal app! 🎮

