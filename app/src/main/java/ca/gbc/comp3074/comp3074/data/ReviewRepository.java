package ca.gbc.comp3074.comp3074.data;

import android.content.Context;

import java.util.List;

import ca.gbc.comp3074.comp3074.data.local.AppDatabase;
import ca.gbc.comp3074.comp3074.data.local.dao.ReviewDao;
import ca.gbc.comp3074.comp3074.data.local.entities.ReviewEntity;

public class ReviewRepository {

    private final ReviewDao reviewDao;

    public ReviewRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.reviewDao = db.reviewDao();
    }

    // Create a new review for a user+game.
    // Return false if user already reviewed game
    public boolean addReview(String username, String gameTitle, String reviewText, float rating) {
        ReviewEntity existing = reviewDao.getReviewForUserAndGame(username, gameTitle);
        if (existing != null) {
            // already exists for this user+game
            return false;
        }

        ReviewEntity entity = new ReviewEntity();
        entity.username = username;
        entity.gameTitle = gameTitle;
        entity.reviewText = reviewText;
        entity.rating = rating;
        entity.createdAt = System.currentTimeMillis();

        reviewDao.insertReview(entity);
        return true;
    }

    // Update existing review for a user+game (if found)
    public void updateReview(String username, String gameTitle, String newText, float newRating) {
        ReviewEntity existing = reviewDao.getReviewForUserAndGame(username, gameTitle);
        if (existing != null) {
            existing.reviewText = newText;
            existing.rating = newRating;
            existing.createdAt = System.currentTimeMillis(); // bump timestamp
            reviewDao.updateReview(existing);
        }
    }

    // Delete review for a user+game (if found)
    public void deleteReview(String username, String gameTitle) {
        ReviewEntity existing = reviewDao.getReviewForUserAndGame(username, gameTitle);
        if (existing != null) {
            reviewDao.deleteReview(existing);
        }
    }

    // All reviews for a specific user (for the Review tab)
    public List<ReviewEntity> getReviewsForUser(String username) {
        return reviewDao.getReviewsForUser(username);
    }

    // All reviews from everyone (for the Feed tab)
    public List<ReviewEntity> getAllReviews() {
        return reviewDao.getAllReviews();
    }

    // Nuke everything (for “clear all reviews”)
    public void deleteAllReviews() {
        reviewDao.deleteAllReviews();
    }
}
