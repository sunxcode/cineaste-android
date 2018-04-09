package de.cineaste.android.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.cineaste.android.entity.series.Season;

@Dao
public interface SeasonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void create(Season season);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void create(List<Season> seasons);

    @Query("SELECT * FROM season WHERE _id = :seasonId")
    Season getById(long seasonId);

    @Query("SELECT * FROM season WHERE seriesId = :seriesId")
    List<Season> readEpisodesBySeriesId(long seriesId);

    @Query("DELETE FROM season where seriesId = :seriesId")
    void deleteBySeriesId(long seriesId);

    @Update
    void update(Season season);

    @Update
    void update(List<Season> seasons);

    @Query("UPDATE season SET seasonWatched = :watchState WHERE seriesId = :seriesId")
    void updateWatchState(boolean watchState, long seriesId);

}
