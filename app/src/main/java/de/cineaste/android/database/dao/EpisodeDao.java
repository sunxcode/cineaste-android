package de.cineaste.android.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.cineaste.android.entity.series.Episode;

@Dao
public interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void create(Episode episode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void create(List<Episode> episodes);

    @Query("SELECT * FROm episode WHERE _id = :episodeId")
    Episode getById(long episodeId);

    @Query("SELECT * FROM episode WHERE seasonId = :seasonId")
    List<Episode> readEpisodesBySeasonId(long seasonId);

    @Query("DELETE FROM episode WHERE seriesId = :seriesId")
    void deleteBySeriesId(long seriesId);

    @Update
    void update(Episode episode);

    @Update
    void update(List<Episode> episodes);

    @Query("UPDATE episode SET watched = :watchState WHERE seriesId = :seriesId")
    void updateWatchStateForSeries(boolean watchState, long seriesId);

    @Query("UPDATE episode SET watched = :watchState WHERE seasonId = :seasonId")
    void updateWatchStateForSeason(boolean watchState, long seasonId);
}
