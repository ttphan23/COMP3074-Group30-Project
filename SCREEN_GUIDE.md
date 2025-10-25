# Screen-by-Screen Guide

## 🔐 Login Screen

**Layout**: `activity_login.xml`  
**Activity**: `LoginActivity.java`

### Visual Structure:
```
┌─────────────────────────────────────┐
│                                     │
│         🎮 (Game Controller)        │
│                                     │
│      VIDEO GAME JOURNAL             │
│   Track, Review, Share Your         │
│      Gaming Journey                 │
│                                     │
│    ┌─────────────────────────┐     │
│    │ 👤 Username            │     │
│    └─────────────────────────┘     │
│                                     │
│    ┌─────────────────────────┐     │
│    │ 🔒 Password            │     │
│    └─────────────────────────┘     │
│                                     │
│    ┌─────────────────────────┐     │
│    │       LOGIN             │     │
│    └─────────────────────────┘     │
│                                     │
│    ┌─────────────────────────┐     │
│    │      REGISTER           │     │
│    └─────────────────────────┘     │
│                                     │
│      Continue as Guest              │
│                                     │
└─────────────────────────────────────┘
```

### Features:
- Centered layout with logo
- Icon-enhanced input fields
- Two-tone button design (teal/gray)
- Guest login option
- Auto-login on return visits

---

## 🏠 Profile/Home Tab

**Layout**: `fragment_home.xml`  
**Fragment**: `HomeFragment.java`

### Visual Structure:
```
┌─────────────────────────────────────┐
│  ┌────────────────────────────────┐ │
│  │ 👤 User Profile                │ │
│  │ ○ Username         [Logout]    │ │
│  └────────────────────────────────┘ │
│                                     │
│  [Played] [Playing] [Backlog]       │
│                                     │
│  🔥 Trending Games                  │
│  ┌────────────────────────────────┐ │
│  │ 🎮 Elden Ring                  │ │
│  │    Epic open-world RPG • ⭐ 4.9│ │
│  │ ────────────────────────────── │ │
│  │ 🎮 Baldur's Gate 3             │ │
│  │    Immersive fantasy • ⭐ 4.8  │ │
│  │ ────────────────────────────── │ │
│  │ 🎮 Zelda: BOTW                 │ │
│  │    Revolutionary • ⭐ 4.9      │ │
│  │ ────────────────────────────── │ │
│  │ 🎮 God of War                  │ │
│  │    Mythological • ⭐ 4.8       │ │
│  └────────────────────────────────┘ │
│                                     │
│  📚 My Game Library                 │
│  ┌────────────────────────────────┐ │
│  │ 🎮 The Legend of Zelda: BOTW   │ │
│  │ ────────────────────────────── │ │
│  │ 🎮 Elden Ring                  │ │
│  │ ────────────────────────────── │ │
│  │ 🎮 Hollow Knight               │ │
│  └────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Features:
- Profile card with avatar
- Status filter buttons
- Dynamic trending games (top 4)
- Static game library
- Logout functionality

---

## 📰 Feed Tab

**Layout**: `fragment_dashboard.xml`  
**Fragment**: `DashboardFragment.java`

### Visual Structure:
```
┌─────────────────────────────────────┐
│      🌐 Activity Feed                │
│   What your friends are playing      │
│                                     │
│  👥 Suggested Friends               │
│  ┌────────────────────────────────┐ │
│  │ 👤 GamerAlice      [Follow]    │ │
│  │ ────────────────────────────── │ │
│  │ 👤 ProGamer123     [Unfollow]  │ │
│  │ ────────────────────────────── │ │
│  │ 👤 CasualBob       [Follow]    │ │
│  │ ────────────────────────────── │ │
│  │ 👤 SpeedRunner99   [Follow]    │ │
│  │ ────────────────────────────── │ │
│  │ 👤 RPGFanatic      [Follow]    │ │
│  └────────────────────────────────┘ │
│                                     │
│  📰 Recent Activity                 │
│                                     │
│  ┌────────────────────────────────┐ │
│  │ 👤 ProGamer123 reviewed        │ │
│  │    The Legend of Zelda: BOTW   │ │
│  │ ⭐ 5.0 / 5.0                   │ │
│  │ "Best Zelda game ever made"    │ │
│  └────────────────────────────────┘ │
│                                     │
│  ┌────────────────────────────────┐ │
│  │ 👤 ProGamer123 reviewed        │ │
│  │    God of War                   │ │
│  │ ⭐ 5.0 / 5.0                   │ │
│  │ "Epic story and combat"        │ │
│  └────────────────────────────────┘ │
│                                     │
│  ┌────────────────────────────────┐ │
│  │ 👤 GamerAlice reviewed         │ │
│  │    Elden Ring                   │ │
│  │ ⭐ 5.0 / 5.0                   │ │
│  │ "Amazing open world!"          │ │
│  └────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Features:
- Follow/unfollow buttons for each user
- Real-time button state updates
- Card-based review display
- Shows only followed users' reviews
- Auto-refreshes on follow changes

---

## ✍️ Review Tab

**Layout**: `fragment_notifications.xml`  
**Fragment**: `NotificationsFragment.java`

### Visual Structure:
```
┌─────────────────────────────────────┐
│      ✍️ Write a Review               │
│   Share your gaming experience       │
│                                     │
│  ┌────────────────────────────────┐ │
│  │ 🎮 Select a Game               │ │
│  │ ┌────────────────────────────┐ │ │
│  │ │ Elden Ring            ▼    │ │ │
│  │ └────────────────────────────┘ │ │
│  │                                │ │
│  │ ⭐ Your Rating                 │ │
│  │ ★★★★★ (0.0)                   │ │
│  │                                │ │
│  │ Your Review                    │ │
│  │ ┌────────────────────────────┐ │ │
│  │ │ Share your thoughts...     │ │ │
│  │ │                            │ │ │
│  │ │                            │ │ │
│  │ │                            │ │ │
│  │ └────────────────────────────┘ │ │
│  │                                │ │
│  │ ┌────────────────────────────┐ │ │
│  │ │    SUBMIT REVIEW           │ │ │
│  │ └────────────────────────────┘ │ │
│  └────────────────────────────────┘ │
│                                     │
│  📝 My Recent Reviews               │
│  ┌────────────────────────────────┐ │
│  │ 🎮 Elden Ring — 5.0★           │ │
│  │ Amazing open world experience! │ │
│  │                                │ │
│  │ 🎮 Hollow Knight — 4.5★        │ │
│  │ Challenging platformer         │ │
│  │                                │ │
│  │ ┌────────────────────────────┐ │ │
│  │ │   CLEAR ALL REVIEWS        │ │ │
│  │ └────────────────────────────┘ │ │
│  └────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Features:
- Dropdown game selector (8+ games)
- Interactive star rating
- Large text input area
- Form validation with animations
- Success Snackbar on submit
- Review history display
- Clear all with confirmation

---

## 🎨 Visual Feedback Examples

### Snackbar Messages:
```
┌─────────────────────────────────────┐
│  ✅ Review submitted successfully!  │
│                           [VIEW] ✕  │
└─────────────────────────────────────┘
```

```
┌─────────────────────────────────────┐
│  ⚠️ Please write a review!          │
│                                  ✕  │
└─────────────────────────────────────┘
```

```
┌─────────────────────────────────────┐
│  Are you sure you want to delete?   │
│                        [DELETE] ✕   │
└─────────────────────────────────────┘
```

### Toast Messages:
```
   ┌─────────────────────┐
   │ 🎮 Review saved!   │
   └─────────────────────┘
```

```
   ┌─────────────────────┐
   │ Following GamerAlice│
   └─────────────────────┘
```

---

## 🎭 Animation Behaviors

### 1. Shake (Validation Error)
```
Input field → [left] [right] [left] [right] → center
Duration: 500ms
```

### 2. Bounce (Rating Change)
```
Star → [scale up 1.1x] → [bounce down] → normal
Duration: 300ms
```

### 3. Scale (Success)
```
Button → [1.05x] → [1.0x]
Duration: 200ms
```

### 4. Fade In (Content Update)
```
Text → [alpha 0] → [alpha 1]
Duration: 500ms
```

---

## 🎨 Color Usage by Screen

| Element | Color | Hex |
|---------|-------|-----|
| Primary buttons | Teal | #009688 |
| Accent elements | Orange | #FF5722 |
| Danger buttons | Red | #D32F2F |
| Secondary buttons | Gray | #455A64 |
| Background | Light Gray | #F5F5F5 |
| Cards | White | #FFFFFF |
| Primary text | Dark Gray | #212121 |
| Secondary text | Medium Gray | #757575 |

---

## 📏 Spacing Standards

| Size | Value | Usage |
|------|-------|-------|
| Small | 8dp | Tight spacing |
| Medium | 16dp | Standard spacing |
| Large | 24dp | Section separation |
| XLarge | 32dp | Major divisions |

---

## 🎯 Navigation Flow

```
          Login Screen
               ↓
        ┌─────┴─────┐
        │  MainActivity  │
        └─────┬─────┘
    ┌─────────┼─────────┐
    ↓         ↓         ↓
[Profile]  [Feed]  [Review]
    ↓
 Logout → Login Screen
```

---

## ✨ Interactive Elements

### Buttons:
- ✅ Ripple effect on tap
- ✅ Color-coded by function
- ✅ Rounded corners (8dp)
- ✅ Clear labels

### Cards:
- ✅ 4dp elevation (subtle shadow)
- ✅ 8dp corner radius
- ✅ White background
- ✅ Proper padding (16dp)

### Inputs:
- ✅ Outlined style
- ✅ Hint text
- ✅ Icon prefix (when applicable)
- ✅ Error states with animations

This guide provides a complete visual reference for all screens and interactions in the app! 🎮

