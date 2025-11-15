Summary of Work Completed (Backend & Infrastructure Upgrade) - Thinh

1. feat: Add Room-backed reviews and polish profile/review flows

Replaced SharedPreferences reviews → Real Local Database (Room)
Added:
    ReviewEntity (Room @Entity)
    ReviewDao (CRUD operations)
    AppDatabase (Room database builder)
Migrated all review functions from SessionManager → proper Room queries.
Ensured:
    Add review
    Edit review
    Delete review
    Duplicate review detection
    Display reviews in NotificationsFragment are all now database-backed.

Refactored Review/Notifications Screen
Review submission now saves directly to Room.
Review list updates instantly after:
    Adding
    Editing
    Deleting
Fixed edit crashes (null references, wrong update call).
Cleaned up UI refresh functions while keeping all animations.

Improved Data Separation
Created new data.local package:
    entities/
    dao/
    AppDatabase
Prepared clean architecture direction for the final project.

2. Integrate Retrofit game API into Home + Review screens
Instead of relying on our dummy “trending” game list:
Added Retrofit networking layer
    ApiClient with logging interceptor
    GameApiService interface
    GameApiModel mapped to JSON fields

HomeFragment “Trending Games”
Now loads from:
    1. Local Node server: GET /games/trending
    2. If offline → falls back to SessionManager dummy trending list
This makes the app feel “online,” even if backend is unavailable.

Review Screen Game Selector
Spinner now uses API list instead of hardcoded games.
If API fails → fallback list still loads.

Confirmed
API loads successfully on emulator: 10.0.2.2:3000
When server is stopped → UI gracefully switches to fallback mode.
NOTE: If not running using server, fallback mode takes a few seconds to load before dummy data shows. 

Test server using:
http://localhost:3000/games/trending