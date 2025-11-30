package ca.gbc.comp3074.comp3074.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReviewDao {

    @Insert
    void insert(Review review);

    @Update
    void update(Review review);

    @Delete
    void delete(Review review);

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    List<Review> getAllReviews();

    @Query("SELECT * FROM reviews WHERE username = :username ORDER BY timestamp DESC")
    List<Review> getReviewsForUser(String username);

    @Query("DELETE FROM reviews")
    void deleteAllReviews();

    @Query("DELETE FROM reviews WHERE username = :username")
    void deleteReviewsForUser(String username);
}
