package ca.gbc.comp3074.comp3074.data.local.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class ReviewEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "game_title")
    public String gameTitle;

    @ColumnInfo(name = "review_text")
    public String reviewText;

    @ColumnInfo(name = "rating")
    public float rating;

    @ColumnInfo(name = "created_at")
    public long createdAt;   // store System.currentTimeMillis()
}
