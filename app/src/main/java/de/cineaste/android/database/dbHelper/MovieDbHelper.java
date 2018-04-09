package de.cineaste.android.database.dbHelper;

import android.content.Context;

import java.util.List;

import de.cineaste.android.database.CineasteDatabase;
import de.cineaste.android.database.dao.MovieDao;
import de.cineaste.android.entity.movie.Movie;
import de.cineaste.android.fragment.WatchState;

public class MovieDbHelper {

    private static MovieDbHelper instance;

    private final MovieDao movieDao;

    private MovieDbHelper(Context context) {
        this.movieDao = CineasteDatabase.getInstance(context).getMovieDao();
    }

    public static MovieDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MovieDbHelper(context);
        }

        return instance;
    }

    public Movie readMovie(long movieId) {
        return movieDao.readMovie(movieId);
    }

    public List<Movie> readAllMovies() {
        return movieDao.readAllMovies();
    }

    public List<Movie> readMoviesByWatchStatus(WatchState state) {
        return movieDao.readMoviesByWatchStatus(extractWatchState(state));
    }

    public List<Movie> reorderAlphabetical(WatchState state) {
        movieDao.reorderAlphabetical(extractWatchState(state));
        return readMoviesByWatchStatus(state);
    }

    public List<Movie> reorderByReleaseDate(WatchState state) {
        movieDao.reorderByReleaseDate(extractWatchState(state));
        return readMoviesByWatchStatus(state);
    }

    public List<Movie> reorderByRuntime(WatchState state) {
        movieDao.reorderByRuntime(extractWatchState(state));
        return readMoviesByWatchStatus(state);
    }

    public void createOrUpdate(Movie movie) {
        Movie movie1 = readMovie(movie.getId());

        if (movie1 == null) {
            movieDao.create(movie);
        } else {
            movie.setListPosition(getNewPosition(movie, movie1));
            movieDao.update(movie);
        }
    }

    public void updatePosition(Movie movie) {
        movieDao.update(movie);
    }

    public void deleteMovieFromWatchlist(Movie movie) {
        movieDao.delete(movie);
    }


    private boolean extractWatchState(WatchState state) {
        return state != WatchState.WATCH_STATE;
    }

    private int getNewPosition(Movie updatedMovie, Movie oldMovie) {
        if (updatedMovie.isWatched() == oldMovie.isWatched()) {
            return oldMovie.getListPosition();
        }

        return movieDao.getHighestListPosition(updatedMovie.isWatched());
    }
}
