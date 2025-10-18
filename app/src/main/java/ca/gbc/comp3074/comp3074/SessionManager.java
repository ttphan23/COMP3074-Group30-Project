package ca.gbc.comp3074.comp3074;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SessionManager {
    private static final String PREF_NAME = "vgj_session_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_REVIEWS = "reviews_json";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // --- Login / Logout ---
    public void login(String username) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_USERNAME)
                .remove(KEY_REVIEWS)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    // --- Review-related storage (supports multiple reviews) ---
    public void saveReview(String gameTitle, String reviewText, float rating) {
        try {
            JSONArray reviews = getAllReviewsArray();

            JSONObject newReview = new JSONObject();
            newReview.put("title", gameTitle);
            newReview.put("text", reviewText);
            newReview.put("rating", rating);
            newReview.put("user", getUsername().isEmpty() ? "Guest" : getUsername());

            reviews.put(newReview);

            prefs.edit().putString(KEY_REVIEWS, reviews.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
}
