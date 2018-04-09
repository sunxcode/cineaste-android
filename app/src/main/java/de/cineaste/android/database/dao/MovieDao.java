package de.cineaste.android.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.cineaste.android.entity.movie.Movie;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie WHERE _id = :movieId")
    Movie readMovie(long movieId);

    @Query("SELECT * FROM movie")
    List<Movie> readAllMovies();

    @Query("SELECT * FROM movie WHERE watched = :watchState")
    List<Movie> readMoviesByWatchStatus(boolean watchState);

    @Query("UPDATE movie SET listPosition = (SELECT COUNT(*) FROM movie AS t2 WHERE t2.title <= movie.title) WHERE watched = :watchState")
    void reorderAlphabetical(boolean watchState);

    @Query("UPDATE movie SET listPosition = (SELECT COUNT(*) FROM movie AS t2 WHERE t2.releaseDate <= movie.releaseDate) WHERE watched = :watchState")
    void reorderByReleaseDate(boolean watchState);

    @Query("UPDATE movie SET listPosition = (SELECT COUNT(*) FROM movie AS t2 WHERE t2.runtime <= movie.runtime) WHERE watched = :watchState")
    void reorderByRuntime(boolean watchState);

    @Query("UPDATE movie SET listPosition = :listPosition WHERE _id = :movieId")
    void updatePosition(int listPosition, long movieId);

    @Delete
    void delete(Movie movie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void create(Movie movie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void create(List<Movie> movies);

    @Update
    void update(Movie movie);

    @Query("SELECT MAX(listPosition) FROM movie WHERE watched = :watchState")
    int getHighestListPosition(boolean watchState);
}
