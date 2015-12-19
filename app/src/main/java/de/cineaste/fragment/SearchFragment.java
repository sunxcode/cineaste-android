package de.cineaste.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.cineaste.R;
import de.cineaste.adapter.SearchQueryAdapter;
import de.cineaste.entity.Movie;
import de.cineaste.network.TheMovieDb;

public class SearchFragment extends Fragment {

    private TheMovieDb mTheMovieDb = new TheMovieDb( getActivity() );
    private RecyclerView movieQueryRecyclerView;
    private RecyclerView.Adapter movieQueryAdapter;
    private RecyclerView.LayoutManager movieQueryLayoutMgr;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setHasOptionsMenu( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.fragment_search, container, false );

        movieQueryRecyclerView = (RecyclerView) view.findViewById( R.id.search_recycler_view );
        //movieQueryLayoutMgr =  new LinearLayoutManager(getActivity());
        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        movieQueryAdapter = new SearchQueryAdapter(getActivity(), new ArrayList<Movie>());
        movieQueryRecyclerView.setItemAnimator( new DefaultItemAnimator() );

        movieQueryRecyclerView.setLayoutManager(llm);
        movieQueryRecyclerView.setAdapter( movieQueryAdapter );

        return view;
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {

        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit( String query ) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange( String query ) {
                    if( !query.isEmpty() ) {
                        query = query.replace( " ", "+" );
                        mTheMovieDb.searchMoviesAsync( query, new TheMovieDb.OnSearchMoviesResultListener() {
                            @Override
                            public void onSearchMoviesResultListener( List<Movie> movies ) {
                                ((SearchQueryAdapter) movieQueryAdapter).mDataset = movies;
                                movieQueryAdapter.notifyDataSetChanged();
                            }
                        }, getResources().getString( R.string.language_tag ) );
                    }
                    return false;
                }
            } );
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.startMovieNight:
                break;
        }

        return super.onOptionsItemSelected( item );
    }
}
