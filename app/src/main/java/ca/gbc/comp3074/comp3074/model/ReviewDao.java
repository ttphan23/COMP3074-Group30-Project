package ca.gbc.comp3074.comp3074.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(ReviewEntity review);

    @Update
    int update(ReviewEntity review);

    @Delete
    int delete(ReviewEntity review);

    @Query("SELECT * FROM reviews WHERE user = :user ORDER BY timestamp DESC")
    List<ReviewEntity> getReviewsForUser(String user);

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    List<ReviewEntity> getAllReviews();

    @Query("SELECT * FROM reviews WHERE user = :user AND LOWER(gameTitle) = LOWER(:gameTitle) LIMIT 1")
    ReviewEntity findUserReviewForGame(String user, String gameTitle);
}
