package ca.gbc.comp3074.comp3074;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ca.gbc.comp3074.comp3074.model.Game;

public class SessionManager {
    public static final String PREF_NAME = "vgj_session_prefs";
    public static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_GUEST = "is_guest";
    private static final String KEY_REVIEWS = "reviews_json";
    private static final String KEY_USERS = "users_json";
    private static final String KEY_REGISTERED_USERS = "registered_users_json";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_FOLLOWING = "following_json";
    private static final String KEY_TRENDING_GAMES = "trending_games_json";
    private static final String KEY_GAME_LIBRARY = "game_library";
    private static final String KEY_BEST_GAME = "best_game_title";
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        initializeDummyData();
    }

    // Initialize dummy data for demo purposes
    private void initializeDummyData() {
        if (prefs.getString(KEY_USERS, "").isEmpty()) {
            initializeDummyUsers();
        }
        if (prefs.getString(KEY_TRENDING_GAMES, "").isEmpty()) {
            initializeTrendingGames();
        }
    }

    // --- Login / Logout ---
    public void login(String username) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USERNAME, username)
                .putBoolean(KEY_IS_GUEST, false)
                .apply();
    }

    public void loginAsGuest() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USERNAME, "Guest")
                .putBoolean(KEY_IS_GUEST, true)
                .apply();
    }

    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .putBoolean(KEY_IS_GUEST, false)
                .remove(KEY_USERNAME)
                .remove(KEY_REVIEWS)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isGuest() {
        return prefs.getBoolean(KEY_IS_GUEST, false);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Guest");
    }

    // --- User Registration & Authentication ---
    public void registerUser(String username, String password) {
        // Backwards-compatible: call the extended registration method with empty full name/email
        registerUser(username, password, "", "");
    }

    // Extended register method to store additional user metadata
    public void registerUser(String username, String password, String fullName, String email) {
        try {
            JSONArray registeredUsers = getRegisteredUsersArray();

            JSONObject newUser = new JSONObject();
            newUser.put("username", username);
            newUser.put("password", password);
            newUser.put("fullName", fullName == null ? "" : fullName);
            newUser.put("email", email == null ? "" : email);

            registeredUsers.put(newUser);

            prefs.edit().putString(KEY_REGISTERED_USERS, registeredUsers.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Remember me preference
    public void setRememberMe(boolean remember) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, remember).apply();
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public boolean userExists(String username) {
        try {
            JSONArray registeredUsers = getRegisteredUsersArray();
            for (int i = 0; i < registeredUsers.length(); i++) {
                JSONObject user = registeredUsers.getJSONObject(i);
                if (user.getString("username").equals(username)) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean validateUserPassword(String username, String password) {
        try {
            JSONArray registeredUsers = getRegisteredUsersArray();
            for (int i = 0; i < registeredUsers.length(); i++) {
                JSONObject user = registeredUsers.getJSONObject(i);
                if (user.getString("username").equals(username)) {
                    return user.getString("password").equals(password);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private JSONArray getRegisteredUsersArray() {
        String json = prefs.getString(KEY_REGISTERED_USERS, "[]");
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    // --- Review-related storage (supports multiple reviews) ---
    public boolean saveReview(String gameTitle, String reviewText, float rating) {
        try {
            JSONArray reviews = getAllReviewsArray();
            String currentUser = getUsername().isEmpty() ? "Guest" : getUsername();

            for(int i = 0; i < reviews.length(); i++){
                JSONObject review = reviews.getJSONObject(i);
                if (review.getString("user").equals(currentUser) &&
                    review.getString("title").equalsIgnoreCase(gameTitle)) {
                    return false;
                }
            }
            JSONObject newReview = new JSONObject();
            newReview.put("title", gameTitle);
            newReview.put("text", reviewText);
            newReview.put("rating", rating);
            newReview.put("user", getUsername().isEmpty() ? "Guest" : getUsername());

            reviews.put(newReview);

            prefs.edit().putString(KEY_REVIEWS, reviews.toString()).apply();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateReview(String gameTitle, String newText, float newRating) {
        try{
            JSONArray reviews = getAllReviewsArray();
            String currentUser = getUsername().isEmpty() ? "Guest" : getUsername();

            for (int i = 0; i<reviews.length(); i++){
                JSONObject review = reviews.getJSONObject(i);
                if (review.getString("user").equals(currentUser) &&
                        review.getString("title").equalsIgnoreCase(gameTitle)) {

                    review.put("text", newText);
                    review.put("rating", newRating);
                    prefs.edit().putString(KEY_REVIEWS, reviews.toString()).apply();
                    return;
                }
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void deleteReview(String title) throws JSONException {
        JSONArray reviews = getAllReviewsArray();
        JSONArray updated = new JSONArray();
        String currentUser = getUsername().isEmpty() ? "Guest" : getUsername();

        for (int i = 0; i < reviews.length(); i++) {
            JSONObject review = reviews.getJSONObject(i);
            if (!(review.getString("title").equals(title) && review.getString("user").equals(currentUser))) {
                updated.put(review);
            }
        }

        prefs.edit().putString(KEY_REVIEWS, updated.toString()).apply();
    }

    public String getAllReviews() {
        JSONArray reviews = getAllReviewsArray();
        if (reviews.length() == 0) return "No reviews yet.";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < reviews.length(); i++) {
            try {
                JSONObject review = reviews.getJSONObject(i);
                builder.append("🎮 ")
                        .append(review.getString("title"))
                        .append(" — ")
                        .append(review.getDouble("rating"))
                        .append("★\n")
                        .append(review.getString("text"))
                        .append("\n\n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return builder.toString().trim();
    }

    public String getAllReviewsJSON() {
        return prefs.getString(KEY_REVIEWS, "[]");
    }

    private JSONArray getAllReviewsArray() {
        String json = prefs.getString(KEY_REVIEWS, "[]");
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public void clearReviews() {
        prefs.edit().remove(KEY_REVIEWS).apply();
    }

    // --- Multi-User System ---
    private void initializeDummyUsers() {
        try {
            JSONArray users = new JSONArray();
            
            // Create dummy users with their reviews
            String[] usernames = {"GamerAlice", "ProGamer123", "CasualBob", "SpeedRunner99", "RPGFanatic"};
            String[][] userGames = {
                {"Elden Ring", "Hollow Knight"},
                {"The Legend of Zelda: BOTW", "God of War"},
                {"Stardew Valley", "Hollow Knight"},
                {"Elden Ring", "Sekiro: Shadows Die Twice"},
                {"The Witcher 3", "Baldur's Gate 3"}
            };
            String[][] userReviews = {
                {"Amazing open world experience!", "Challenging but rewarding platformer"},
                {"Best Zelda game ever made", "Epic story and combat"},
                {"Perfect relaxing game", "Love the atmosphere"},
                {"Mastered it in 50 hours", "Incredible boss fights"},
                {"Best RPG of all time", "Can't stop playing!"}
            };
            float[][] userRatings = {
                {5.0f, 4.5f},
                {5.0f, 5.0f},
                {4.5f, 4.0f},
                {5.0f, 4.5f},
                {5.0f, 5.0f}
            };

            for (int i = 0; i < usernames.length; i++) {
                JSONObject user = new JSONObject();
                user.put("username", usernames[i]);
                
                JSONArray reviews = new JSONArray();
                for (int j = 0; j < userGames[i].length; j++) {
                    JSONObject review = new JSONObject();
                    review.put("title", userGames[i][j]);
                    review.put("text", userReviews[i][j]);
                    review.put("rating", userRatings[i][j]);
                    review.put("user", usernames[i]);
                    reviews.put(review);
                }
                user.put("reviews", reviews);
                users.put(user);
            }

            prefs.edit().putString(KEY_USERS, users.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getAllUsers() {
        String json = prefs.getString(KEY_USERS, "[]");
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    // --- Trending Games ---
    private void initializeTrendingGames() {
        try {
            JSONArray trendingGames = new JSONArray();
            
            String[] games = {
                "Elden Ring",
                "Baldur's Gate 3",
                "The Legend of Zelda: BOTW",
                "God of War",
                "Hollow Knight",
                "Stardew Valley"
            };
            
            String[] descriptions = {
                "Epic open-world RPG",
                "Immersive fantasy adventure",
                "Revolutionary open world",
                "Mythological action-adventure",
                "Challenging metroidvania",
                "Relaxing farming sim"
            };
            
            float[] ratings = {4.9f, 4.8f, 4.9f, 4.8f, 4.7f, 4.6f};
            
            for (int i = 0; i < games.length; i++) {
                JSONObject game = new JSONObject();
                game.put("title", games[i]);
                game.put("description", descriptions[i]);
                game.put("rating", ratings[i]);
                trendingGames.put(game);
            }
            
            prefs.edit().putString(KEY_TRENDING_GAMES, trendingGames.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getTrendingGames() {
        String json = prefs.getString(KEY_TRENDING_GAMES, "[]");
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    // --- Follow System ---
    public void followUser(String username) {
        try {
            JSONArray following = getFollowingArray();
            
            // Check if already following
            for (int i = 0; i < following.length(); i++) {
                if (following.getString(i).equals(username)) {
                    return; // Already following
                }
            }
            
            following.put(username);
            prefs.edit().putString(KEY_FOLLOWING, following.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void unfollowUser(String username) {
        try {
            JSONArray following = getFollowingArray();
            JSONArray newFollowing = new JSONArray();
            
            for (int i = 0; i < following.length(); i++) {
                String user = following.getString(i);
                if (!user.equals(username)) {
                    newFollowing.put(user);
                }
            }
            
            prefs.edit().putString(KEY_FOLLOWING, newFollowing.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isFollowing(String username) {
        try {
            JSONArray following = getFollowingArray();
            for (int i = 0; i < following.length(); i++) {
                if (following.getString(i).equals(username)) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getFollowingList() {
        List<String> list = new ArrayList<>();
        try {
            JSONArray following = getFollowingArray();
            for (int i = 0; i < following.length(); i++) {
                list.add(following.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private JSONArray getFollowingArray() {
        String json = prefs.getString(KEY_FOLLOWING, "[]");
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    // Get all reviews from followed users
    public JSONArray getFollowedUsersReviews() {
        JSONArray allReviews = new JSONArray();
        try {
            JSONArray users = getAllUsers();
            List<String> following = getFollowingList();
            
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String username = user.getString("username");
                
                if (following.contains(username)) {
                    JSONArray userReviews = user.getJSONArray("reviews");
                    for (int j = 0; j < userReviews.length(); j++) {
                        allReviews.put(userReviews.getJSONObject(j));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allReviews;
    }

    public void saveGameLibrary(List<Game> games) {
        JSONArray array = new JSONArray();

        for (Game g : games) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("title", g.getTitle());
                obj.put("status", g.getStatus());
                obj.put("emoji", g.getEmoji());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        prefs.edit()
                .putString("game_library", array.toString())
                .apply();
    }

    public List<Game> getGameLibrary() {
        List<Game> games = new ArrayList<>();
        String json = prefs.getString("game_library", "[]");

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                games.add(new Game(
                        obj.getString("title"),
                        obj.getString("status"),
                        obj.getString("emoji")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return games;
    }

    public void setBestGame(String title) {
        prefs.edit().putString(KEY_BEST_GAME, title).apply();
    }

    public String getBestGame() {
        return prefs.getString(KEY_BEST_GAME, "");
    }
    // Initialize default following for demo
    public void initializeDefaultFollowing() {
        if (getFollowingList().isEmpty()) {
            followUser("GamerAlice");
            followUser("ProGamer123");
        }
    }
}
