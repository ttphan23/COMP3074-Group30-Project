package ca.gbc.comp3074.comp3074.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class ReviewEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String user;        // username or "Guest"
    private String gameTitle;   // e.g. "Elden Ring"
    private String reviewText;  // body text
    private float rating;       // 0.0 - 5.0
    private long timestamp;     // for ordering newest first

    public ReviewEntity(String user, String gameTitle, String reviewText, float rating, long timestamp) {
        this.user = user;
        this.gameTitle = gameTitle;
        this.reviewText = reviewText;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // --- Getters & setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
