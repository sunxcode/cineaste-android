package de.cineaste.android.database;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;

import de.cineaste.android.database.dao.EpisodeDao;
import de.cineaste.android.database.dao.MovieDao;
import de.cineaste.android.database.dao.SeasonDao;
import de.cineaste.android.database.dao.SeriesDao;
import de.cineaste.android.database.dao.UserDao;
import de.cineaste.android.entity.User;
import de.cineaste.android.entity.movie.Movie;
import de.cineaste.android.entity.series.Episode;
import de.cineaste.android.entity.series.Season;
import de.cineaste.android.entity.series.Series;
import de.cineaste.android.util.Constants;

@Database(entities = {Movie.class, Series.class, Season.class, Episode.class, User.class},
        version = Constants.DATABASE_VERSION)
@TypeConverters({DateTypeConverter.class})
public abstract class CineasteDatabaseAbstract extends RoomDatabase {

    public abstract MovieDao getMovieDao();

    public abstract SeriesDao getSeriesDao();

    public abstract SeasonDao getSeasonDao();

    public abstract EpisodeDao getEpisodeDao();

    public abstract UserDao getUserDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE movie ADD COLUMN releaseDate TEXT");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE movie ADD COLUMN listPosition INTEGER");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(SQL_CREATE_SERIES_ENTRIES);
            database.execSQL(SQL_CREATE_SEASON_ENTRIES);
            database.execSQL(SQL_CREATE_EPISODE_ENTRIES);
        }
    };

    private static final String SQL_CREATE_SERIES_ENTRIES =
            "CREATE TABLE IF NOT EXISTS series (" +
                    "_id INTEGER PRIMARY KEY," +
                    "seriesName TEXT," +
                    "voteAverage REAL," +
                    "voteCount INTEGER," +
                    "description TEXT," +
                    "releaseDate TEXT," +
                    "inProduction INTEGER," +
                    "numberOfEpisodes INTEGER," +
                    "numberOfSeasons INTEGER," +
                    "posterPath TEXT," +
                    "backdropPath TEXT," +
                    "seriesWatched INTEGER," +
                    "listPosition INTEGER )";

    private static final String SQL_CREATE_SEASON_ENTRIES =
            "CREATE TABLE IF NOT EXISTS season (" +
                    "_id INTEGER PRIMARY KEY," +
                    "releaseDate TEXT," +
                    "episodenCount INTEGER," +
                    "posterPath TEXT," +
                    "seasonNumber INTEGER," +
                    "seasonWatched INTEGER," +
                    "seriesId INTEGER )";

    private static final String SQL_CREATE_EPISODE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS episode (" +
                    "_id INTEGER PRIMARY KEY," +
                    "episodeNumber INTEGER," +
                    "name TEXT," +
                    "description TEXT," +
                    "seriesId INTEGER," +
                    "seasonId INTEGER," +
                    "watched INTEGER )";

}
