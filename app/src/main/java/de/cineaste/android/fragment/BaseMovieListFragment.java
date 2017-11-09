package de.cineaste.android.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.cineaste.android.MainActivity;
import de.cineaste.android.MovieClickListener;
import de.cineaste.android.R;
import de.cineaste.android.activity.AboutActivity;
import de.cineaste.android.activity.MovieDetailActivity;
import de.cineaste.android.activity.SearchActivity;
import de.cineaste.android.adapter.MovieListAdapter;
import de.cineaste.android.controllFlow.BaseItemTouchHelperCallback;
import de.cineaste.android.controllFlow.WatchedlistItemTouchHelperCallback;
import de.cineaste.android.controllFlow.WatchlistItemTouchHelperCallback;
import de.cineaste.android.database.BaseDao;
import de.cineaste.android.database.ExportService;
import de.cineaste.android.database.ImportService;
import de.cineaste.android.database.MovieDbHelper;
import de.cineaste.android.entity.Movie;

import static android.app.ActivityOptions.makeSceneTransitionAnimation;

public class BaseMovieListFragment extends Fragment
        implements MovieClickListener, MovieListAdapter.DisplayMessage {

    private WatchState watchState;

    private RecyclerView watchlistRecyclerView;
    private LinearLayoutManager layoutManager;
    private MovieListAdapter movieListAdapter;
    private TextView emptyListTextView;

    private MovieDbHelper movieDbHelper;


    private WatchState getWatchState(String watchStateString) {
        if (watchStateString.equals(WatchState.WATCH_STATE.name()))
            return WatchState.WATCH_STATE;
        else
            return WatchState.WATCHED_STATE;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        watchState = getWatchState(args.getString(WatchState.WATCH_STATE_TYPE.name(), WatchState.WATCH_STATE.name()));
    }

    @Override
    public void onResume() {
        movieListAdapter.updateDataSet();

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View watchlistView = inflater.inflate(R.layout.fragment_movielist, container, false);

        movieDbHelper = MovieDbHelper.getInstance(getActivity());

        emptyListTextView = watchlistView.findViewById(R.id.info_text);

        watchlistRecyclerView = watchlistView.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());

        watchlistRecyclerView.setHasFixedSize(true);

        movieListAdapter = new MovieListAdapter(this, getActivity(), this, watchState);
        showMessageIfEmptyList();

        watchlistRecyclerView.setLayoutManager(layoutManager);
        watchlistRecyclerView.setAdapter(movieListAdapter);

        FloatingActionButton fab = watchlistView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                getActivity().startActivity(intent);
            }
        });

        initSwipe();

        return watchlistView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(WatchState.WATCH_STATE_TYPE.name(), watchState.name());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            String currentState = savedInstanceState.getString(WatchState.WATCH_STATE_TYPE.name(), WatchState.WATCH_STATE.name());
            this.watchState = getWatchState(currentState);
            if (watchState == WatchState.WATCH_STATE) {
                getActivity().setTitle(R.string.watchList);
            } else {
                getActivity().setTitle(R.string.watchedlist);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchViewMenuItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) searchViewMenuItem.getActionView();
        int searchImgId = android.support.v7.appcompat.R.id.search_button; // I used the explicit layout ID of searchView's ImageView
        ImageView v = mSearchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_filter);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.start_movie_night, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    ((MovieListAdapter) watchlistRecyclerView.getAdapter()).filter(newText);
                    return false;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.startMovieNight:
                MainActivity.startMovieNight(getFragmentManager());
                break;
            case R.id.exportMovies:
                exportMovies();
                break;
            case R.id.importMovies:
                importMovies();
                break;
            case R.id.about:
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                getActivity().startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportMovies() {
        List<Movie> movies = movieDbHelper.readAllMovies();
        ExportService.exportMovies(movies);
        Snackbar snackbar = Snackbar
                .make(watchlistRecyclerView, R.string.successfulExport, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void importMovies() {
        List<Movie> movies = ImportService.importMovies();
        int snackBarMessage;
        if (movies.size() != 0) {
            for (Movie current : movies) {
                movieDbHelper.createOrUpdate(current);
            }
            snackBarMessage = R.string.successfulImport;
        } else {
            snackBarMessage = R.string.unsuccessfulImport;
        }
        Snackbar snackbar = Snackbar
                .make(watchlistRecyclerView, snackBarMessage, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void showMessageIfEmptyList() {
        if (movieListAdapter.getDataSetSize() == 0) {
            watchlistRecyclerView.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
            emptyListTextView.setText(getEmptyListMessageByState());
        } else {
            watchlistRecyclerView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        }
    }

    private int getEmptyListMessageByState() {
        if (watchState == WatchState.WATCH_STATE) {
            return R.string.noMoviesOnWatchList;
        } else {
            return R.string.noMoviesOnWatchedList;
        }
    }

    @Override
    public void onMovieClickListener(long movieId, View[] views) {
        int state;
        if (watchState == WatchState.WATCH_STATE) {
            state = R.string.watchlistState;
        } else {
            state = R.string.watchedlistState;
        }

        Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
        intent.putExtra(BaseDao.MovieEntry._ID, movieId);
        intent.putExtra(getString(R.string.state), state);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = makeSceneTransitionAnimation(getActivity(),
                    Pair.create(views[0], "card"),
                    Pair.create(views[1], "poster")
            );
            getActivity().startActivity(intent, options.toBundle());
        } else {
            getActivity().startActivity(intent);
            // getActivity().overridePendingTransition( R.anim.fade_out, R.anim.fade_in );
        }
    }

    private void initSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(getCorrectCallBack());
        itemTouchHelper.attachToRecyclerView(watchlistRecyclerView);
    }

    private BaseItemTouchHelperCallback getCorrectCallBack() {
        if (watchState == WatchState.WATCH_STATE) {
            return new WatchlistItemTouchHelperCallback(layoutManager, movieListAdapter, watchlistRecyclerView, getResources());
        } else {
            return new WatchedlistItemTouchHelperCallback(layoutManager, movieListAdapter, watchlistRecyclerView, getResources());
        }
    }
}