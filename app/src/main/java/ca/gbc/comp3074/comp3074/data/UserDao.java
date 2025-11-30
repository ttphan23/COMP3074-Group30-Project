package ca.gbc.comp3074.comp3074.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) AND password = :password LIMIT 1")
    User validateUser(String username, String password);

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(:username)")
    int userExists(String username);
}
