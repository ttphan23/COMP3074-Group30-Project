package ca.gbc.comp3074.comp3074.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ca.gbc.comp3074.comp3074.data.local.entities.ReviewEntity;

@Dao
public interface ReviewDao {

    @Insert
    long insertReview(ReviewEntity review);

    @Update
    int updateReview(ReviewEntity review);

    @Delete
    int deleteReview(ReviewEntity review);

    @Query("DELETE FROM reviews")
    void deleteAllReviews();

    @Query("SELECT * FROM reviews WHERE username = :username ORDER BY created_at DESC")
    List<ReviewEntity> getReviewsForUser(String username);

    @Query("SELECT * FROM reviews ORDER BY created_at DESC")
    List<ReviewEntity> getAllReviews();

    @Query("SELECT * FROM reviews WHERE username = :username AND game_title = :gameTitle LIMIT 1")
    ReviewEntity getReviewForUserAndGame(String username, String gameTitle);
}
