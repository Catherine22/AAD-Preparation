package com.catherine.materialdesignapp.jetpack.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.catherine.materialdesignapp.jetpack.entities.Playlist;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Playlist playlist);

    @Query("DELETE FROM playlist_table")
    void deleteAll();

    @Query("SELECT * from playlist_table ORDER BY `index` ASC")
    LiveData<List<Playlist>> getAllPlaylists();
}
