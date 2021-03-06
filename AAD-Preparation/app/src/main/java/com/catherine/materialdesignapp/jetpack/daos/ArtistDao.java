package com.catherine.materialdesignapp.jetpack.daos;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.catherine.materialdesignapp.jetpack.entities.Artist;

import java.util.List;

@Dao
public interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Artist artist);

    @Query("DELETE FROM artist_table")
    void deleteAll();

    @Query("SELECT * from artist_table ORDER BY artist ASC")
    LiveData<List<Artist>> getAllArtists();

    @Update
    int updateAll(Artist... artists);

    @Query("SELECT * FROM artist_table ORDER BY artist ASC")
    DataSource.Factory<Integer, Artist> getArtistDataSource();


    @Query("SELECT * FROM artist_table WHERE artist LIKE '%' || :keyword || '%'  ORDER BY artist ASC")
    DataSource.Factory<Integer, Artist> getArtistDataSource(String keyword);


    @Query("SELECT COUNT(*) FROM artist_table")
    int count();


}
