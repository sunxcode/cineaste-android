package de.cineaste.android.database.dbHelper;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import de.cineaste.android.database.CineasteDatabase;
import de.cineaste.android.database.dao.EpisodeDao;
import de.cineaste.android.database.dao.SeasonDao;
import de.cineaste.android.database.dao.SeriesDao;
import de.cineaste.android.entity.series.Episode;
import de.cineaste.android.entity.series.Season;
import de.cineaste.android.entity.series.Series;
import de.cineaste.android.fragment.WatchState;

public class SeriesDbHelper {

    private static SeriesDbHelper instance;

    private final SeriesDao seriesDao;
    private final SeasonDao seasonDao;
    private final EpisodeDao episodeDao;

    private SeriesDbHelper(Context context) {
        CineasteDatabase db = CineasteDatabase.getInstance(context);
        seriesDao = db.getSeriesDao();
        seasonDao = db.getSeasonDao();
        episodeDao = db.getEpisodeDao();
    }

    public static SeriesDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SeriesDbHelper(context);
        }
        return instance;
    }

    public Series getSeriesById(long seriesId) {
        Series series = seriesDao.getSeriesById(seriesId);
        return loadAdditionalData(series);
    }

    public List<Series> getAllSeries() {
        return loadAdditionalData(seriesDao.getAllSeries());
    }

    public List<Series> getSeriesByWatchedState(WatchState watchedState) {
        return loadAdditionalData(seriesDao.getSeriesByWatchedState(extractWatchState(watchedState)));
    }

    public List<Episode> getEpisodesBySeasonId(long seasonId) {
        return episodeDao.readEpisodesBySeasonId(seasonId);
    }

    public List<Series> reorderAlphabetical(WatchState state) {
        seriesDao.reorderAlphabetical(extractWatchState(state));
        return loadAdditionalData(getSeriesByWatchedState(state));
    }

    public List<Series> reorderByReleaseDate(WatchState state) {
       seriesDao.reorderByReleaseDate(extractWatchState(state));
       return loadAdditionalData(getSeriesByWatchedState(state));
    }

    public void addToWatchList(Series series) {
        setForeignKeys(series);
        updateWatchedState(false, series);
    }

    public void addToHistory(Series series) {
        setForeignKeys(series);
        updateWatchedState(true, series);
    }

    public void moveToWatchList(Series series) {
        updateWatchedState(false, series);
    }

    public void moveToHistory(Series series) {
        updateWatchedState(true, series);
    }

    public void moveBackToWatchList(Series series, int prevSeason, int prevEpisode) {
        moveBackToList(series, prevSeason, prevEpisode, false);
    }

    public void moveBackToHistory(Series series, int prevSeason, int prevEpisode) {
        moveBackToList(series, prevSeason, prevEpisode, true);
    }

    private void moveBackToList(Series series, int prevSeason, int prevEpisode, boolean watchState) {
        updateWatchedState(watchState, series);
        for (Season season : series.getSeasons()) {
            if (season.getSeasonNumber() < prevSeason) {
                season.setWatched(!watchState);
                seasonDao.update(season);
                episodeDao.updateWatchStateForSeason(!watchState, season.getId());
            } else if (season.getSeasonNumber() == prevSeason) {
                for (Episode episode : season.getEpisodes()) {
                    if (episode.getEpisodeNumber() < prevEpisode) {
                        episode.setWatched(!watchState);
                        episodeDao.update(episode);
                    }
                }
            }
        }
    }

    public void delete(Series series) {
        seriesDao.delete(series);

        //todo check if seasons and episodes are deleted as well
    }

    public void delete(long seriesId) {
        seriesDao.delete(seriesDao.getSeriesById(seriesId));
    }

    public void episodeWatched(Series series) {
        List<Season> seasons = new ArrayList<>();
        for (Season season : series.getSeasons()) {
            if (season.isWatched()) {
                seasons.add(season);
            } else {
                List<Episode> episodes = new ArrayList<>();
                for (Episode episode : season.getEpisodes()) {
                    if (episode.isWatched()) {
                        episodes.add(episode);
                    } else {
                        episode.setWatched(true);
                        episodeDao.update(episode);
                        episodes.add(episode);
                        break;
                    }
                }

                if (episodes.size() == season.getEpisodes().size()) {
                    season.setWatched(true);
                    seasonDao.update(season);
                    seasons.add(season);
                }
                break;
            }
        }

        if (seasons.size() == series.getSeasons().size() && !series.isInProduction()) {
            series.setWatched(true);
            series.setListPosition(seriesDao.getHighestListPosition(true));
            seriesDao.update(series);
        }
    }

    public void episodeClicked(Episode episode) {
        episode.setWatched(!episode.isWatched());
        episodeDao.update(episode);

        if (!episode.isWatched()) {
            Series series = getSeriesById(episode.getSeriesId());
            series.setWatched(false);
            series.setListPosition(seriesDao.getHighestListPosition(false));
            seriesDao.update(series);

            Season season = seasonDao.getById(episode.getSeasonId());
            season.setWatched(false);
            seasonDao.update(season);
        }
    }

    public void importSeries(Series series) {
        Series oldSeries = getSeriesById(series.getId());

        List<Season> seasons = new ArrayList<>();
        List<Episode> episodes = new ArrayList<>();
        
        if (oldSeries == null) {
            seriesDao.create(series);
            for (Season season : series.getSeasons()) {
                season.setSeriesId(series.getId());
                seasons.add(season);
                for (Episode episode : season.getEpisodes()) {
                    episode.setSeriesId(series.getId());
                    episode.setSeasonId(season.getId());
                    episodes.add(episode);
                }
            }
            seasonDao.create(seasons);
            episodeDao.create(episodes);
        } else {
            seriesDao.update(series);
            for (Season season : series.getSeasons()) {
                seasons.add(season);
                episodes.addAll(season.getEpisodes());
            }

            seasonDao.update(seasons);
            episodeDao.update(episodes);
        }
    }

    public void update(Series series) {
        Series oldSeries = getSeriesById(series.getId());
        series.setWatched(oldSeries.isWatched());
        series.setListPosition(getNewPosition(series, oldSeries));

        seriesDao.update(series);

        List<Season> seasons = new ArrayList<>();
        List<Episode> episodes = new ArrayList<>();

        for (Season season : series.getSeasons()) {
            Season oldSeason = seasonDao.getById(season.getId());
            season.setWatched(oldSeason.isWatched());
            season.setSeriesId(series.getId());
            seasons.add(season);

            for (Episode episode : season.getEpisodes()) {
                Episode oldEpisode = episodeDao.getById(episode.getId());
                episode.setWatched(oldEpisode.isWatched());
                episode.setSeriesId(series.getId());
                episode.setSeasonId(season.getId());

                episodes.add(episode);
            }
        }

        seasonDao.update(seasons);
        episodeDao.update(episodes);
    }

    public void updatePosition(Series series) {
        Series oldSeries = getSeriesById(series.getId());
        series.setListPosition(getNewPosition(series, oldSeries));

        seriesDao.update(series);
    }

    private boolean extractWatchState(WatchState state) {
        return state != WatchState.WATCH_STATE;
    }

    @NonNull
    private Series loadAdditionalData(Series series) {
        List<Season> seasons = seasonDao.readEpisodesBySeriesId(series.getId());

        for (Season season : seasons) {
            season.setEpisodes(episodeDao.readEpisodesBySeasonId(season.getId()));
        }

        series.setSeasons(seasons);

        return series;
    }

    @NonNull
    private List<Series> loadAdditionalData(List<Series> series) {
        List<Series> seriesList = new ArrayList<>();
        for (Series series1 : series) {
            seriesList.add(loadAdditionalData(series1));
        }

        return seriesList;
    }

    private int getNewPosition(Series updatedSeries, Series oldSeries) {
        if (updatedSeries.isWatched() == oldSeries.isWatched()) {
            return updatedSeries.getListPosition();
        }

        return seriesDao.getHighestListPosition(updatedSeries.isWatched());
    }

    private void updateWatchedState(boolean watchState, Series series) {
        series.setListPosition(seriesDao.getHighestListPosition(watchState));
        series.setWatched(watchState);
        seasonDao.updateWatchState(watchState, series.getId());
        episodeDao.updateWatchStateForSeries(watchState, series.getId());

        seriesDao.update(series);
    }

    private void setForeignKeys(Series series) {
        List<Season> seasons = new ArrayList<>();
        for (Season season : series.getSeasons()) {
            season.setSeriesId(series.getId());
            List<Episode> episodes = new ArrayList<>();
            for (Episode episode : season.getEpisodes()) {
                episode.setSeasonId(season.getId());
                episode.setSeriesId(series.getId());
                episodes.add(episode);
            }
            season.setEpisodes(episodes);
            seasons.add(season);
        }
        series.setSeasons(seasons);
    }

}
