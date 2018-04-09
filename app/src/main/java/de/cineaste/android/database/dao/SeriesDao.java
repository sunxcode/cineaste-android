package de.cineaste.android.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.cineaste.android.entity.series.Series;

@Dao
public interface SeriesDao {

    @Query("SELECT * FROM series WHERE _id = :seriesId")
    Series getSeriesById(long seriesId);

    @Query("SELECT * FROM series")
    List<Series> getAllSeries();

    @Query("SELECT * FROM series WHERE watched = :watchedState")
    List<Series> getSeriesByWatchedState(Boolean watchedState);

    @Query("UPDATE series SET listPosition = (SELECT COUNT(*) FROM series AS t2 WHERE t2.seriesName <= series.seriesName) WHERE watched = :watchedState")
    void reorderAlphabetical(boolean watchedState);

    @Query("UPDATE series SET listPosition = (SELECT COUNT(*) FROM series AS t2 WHERE t2.releaseDate <= series.releaseDate) WHERE watched = :watchedState")
    void reorderByReleaseDate(boolean watchedState);

    @Update
    void update(Series series);

    @Delete
    void delete(Series series);

    @Query("SELECT MAX(listPosition) FROM series WHERE watched = :watchState")
    int getHighestListPosition(boolean watchState);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void create(Series series);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void create(List<Series> series);
}
