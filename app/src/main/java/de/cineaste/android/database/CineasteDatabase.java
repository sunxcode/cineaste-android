package de.cineaste.android.database;

import android.arch.persistence.room.Room;
import android.content.Context;

import de.cineaste.android.database.dao.EpisodeDao;
import de.cineaste.android.database.dao.MovieDao;
import de.cineaste.android.database.dao.SeasonDao;
import de.cineaste.android.database.dao.SeriesDao;
import de.cineaste.android.database.dao.UserDao;

import static de.cineaste.android.database.CineasteDatabaseAbstract.MIGRATION_1_2;
import static de.cineaste.android.database.CineasteDatabaseAbstract.MIGRATION_2_3;
import static de.cineaste.android.database.CineasteDatabaseAbstract.MIGRATION_3_4;
import static de.cineaste.android.util.Constants.DATABASE_NAME;

public class CineasteDatabase {

    private static CineasteDatabase instance;

    private CineasteDatabaseAbstract cineasteDatabaseAbstract;

    public static CineasteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new CineasteDatabase(context);
        }
        return instance;
    }

    private CineasteDatabase(Context context) {
        cineasteDatabaseAbstract = Room.databaseBuilder(
                context,
                CineasteDatabaseAbstract.class,
                DATABASE_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build();
    }

    public MovieDao getMovieDao() {
        return cineasteDatabaseAbstract.getMovieDao();
    }

    public SeriesDao getSeriesDao() {
        return cineasteDatabaseAbstract.getSeriesDao();
    }

    public SeasonDao getSeasonDao() {
        return cineasteDatabaseAbstract.getSeasonDao();
    }

    public EpisodeDao getEpisodeDao() {
        return cineasteDatabaseAbstract.getEpisodeDao();
    }

    public UserDao getUserDao() {
        return cineasteDatabaseAbstract.getUserDao();
    }
}
