package de.cineaste.android.fragment

import android.app.Activity
import android.app.ActivityOptions.makeSceneTransitionAnimation
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Pair
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import de.cineaste.android.R
import de.cineaste.android.activity.MovieNightActivity
import de.cineaste.android.adapter.BaseListAdapter
import de.cineaste.android.database.dbHelper.UserDbHelper
import de.cineaste.android.listener.ItemClickListener
import de.cineaste.android.util.CustomRecyclerView

abstract class BaseListFragment : Fragment(), ItemClickListener, BaseListAdapter.DisplayMessage {

    lateinit var watchState: WatchState
    lateinit var customRecyclerView: CustomRecyclerView
    internal lateinit var layoutManager: LinearLayoutManager
    private lateinit var emptyListTextView: TextView
    private lateinit var userDbHelper: UserDbHelper
    lateinit var progressbar: RelativeLayout
        private set
    protected abstract val subtitle: String
    protected abstract val layout: Int
    protected abstract val dataSetSize: Int
    protected abstract val emptyListMessageByState: Int
    protected abstract val correctCallBack: ItemTouchHelper.Callback

    val recyclerView: View
        get() = customRecyclerView

    abstract fun updateAdapter()
    protected abstract fun initAdapter(activity: Activity)
    protected abstract fun initRecyclerView()
    protected abstract fun initFab(activity: Activity, view: View)
    protected abstract fun filterOnQueryTextChange(newText: String)
    protected abstract fun reorderEntries(filterType: FilterType)
    protected abstract fun createIntent(itemId: Long, state: Int, activity: Activity): Intent

    protected enum class FilterType {
        ALPHABETICAL, RELEASE_DATE, RUNTIME
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        args?.let {
            watchState = getWatchState(
                    args.getString(WatchState.WATCH_STATE_TYPE.name,
                            WatchState.WATCH_STATE.name))
        }
    }

    private fun getWatchState(watchStateString: String): WatchState {
        return if (watchStateString == WatchState.WATCH_STATE.name)
            WatchState.WATCH_STATE
        else
            WatchState.WATCHED_STATE
    }

    override fun onResume() {
        updateAdapter()

        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity

        val watchlistView = initViews(inflater, container)

        if (activity != null) {
            initAdapter(activity)
            showMessageIfEmptyList()

            initRecyclerView()

            initFab(activity, watchlistView)

            initSwipe()

            if (watchState == WatchState.WATCH_STATE) {
                activity.setTitle(R.string.watchList)
            } else {
                activity.setTitle(R.string.history)
            }
            val actionBar = (activity as AppCompatActivity).supportActionBar
            actionBar?.subtitle = subtitle
        }


        return watchlistView
    }

    private fun initViews(inflater: LayoutInflater, container: ViewGroup?): View {
        val watchlistView = inflater.inflate(layout, container, false)

        progressbar = watchlistView.findViewById(R.id.progressBar)
        progressbar.visibility = View.GONE

        emptyListTextView = watchlistView.findViewById(R.id.info_text)

        customRecyclerView = watchlistView.findViewById(R.id.recycler_view)
        layoutManager = LinearLayoutManager(activity)

        customRecyclerView.setHasFixedSize(true)
        return watchlistView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(WatchState.WATCH_STATE_TYPE.name, watchState.name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (savedInstanceState != null) {
            val currentState = savedInstanceState.getString(WatchState.WATCH_STATE_TYPE.name, WatchState.WATCH_STATE.name)
            this.watchState = getWatchState(currentState)
        }

        val activity = activity
        activity?.let {
            userDbHelper = UserDbHelper.getInstance(activity)
        }

    }


    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.let {
            val searchViewMenuItem = menu.findItem(R.id.action_search)
            val mSearchView = searchViewMenuItem.actionView as SearchView
            val searchImgId = android.support.v7.appcompat.R.id.search_button // I used the explicit layout ID of searchView's ImageView
            val v = mSearchView.findViewById<ImageView>(searchImgId)
            v.setImageResource(R.drawable.ic_filter)
            super.onPrepareOptionsMenu(menu)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        val activity = activity
        activity?.let { activity ->
            val menuInflater = activity.menuInflater
            menuInflater.inflate(R.menu.start_movie_night, menu)

            val searchItem = menu?.findItem(R.id.action_search)

            searchItem?.let { searchItem ->
                val searchView = searchItem.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        filterOnQueryTextChange(newText)
                        return false
                    }
                })
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (item.itemId) {
                R.id.startMovieNight -> if (userDbHelper.user != null) {
                    startMovieNight()
                } else {
                    val fragmentManager = fragmentManager
                    if (fragmentManager != null) {
                        UserInputFragment().show(fragmentManager, "")
                    }
                }
                R.id.filterAlphabetical -> reorderLists(BaseListFragment.FilterType.ALPHABETICAL)
                R.id.filterReleaseDate -> reorderLists(BaseListFragment.FilterType.RELEASE_DATE)
                R.id.filterRunTime -> reorderLists(FilterType.RUNTIME)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun reorderLists(filterType: FilterType) {
        progressbar.visibility = View.VISIBLE
        customRecyclerView.enableScrolling(false)

        reorderEntries(filterType)

        progressbar.visibility = View.GONE
        customRecyclerView.enableScrolling(true)
    }

    private fun startMovieNight() {
        val activity = activity ?: return
        val intent = Intent(activity, MovieNightActivity::class.java)
        activity.startActivity(intent)
    }

    override fun showMessageIfEmptyList() {
        if (dataSetSize == 0) {
            customRecyclerView.visibility = View.GONE
            emptyListTextView.visibility = View.VISIBLE
            emptyListTextView.setText(emptyListMessageByState)
        } else {
            customRecyclerView.visibility = View.VISIBLE
            emptyListTextView.visibility = View.GONE
        }
    }

    override fun onItemClickListener(itemId: Long, views: Array<View>) {
        if (!customRecyclerView.isScrollingEnabled) {
            return
        }
        val state: Int = if (watchState == WatchState.WATCH_STATE) {
            R.string.watchlistState
        } else {
            R.string.historyState
        }
        val activity = activity ?: return
        val intent = createIntent(itemId, state, activity)

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = makeSceneTransitionAnimation(activity,
                    Pair.create(views[0], "card"),
                    Pair.create(views[1], "poster")
            )
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
            // getActivity().overridePendingTransition( R.anim.fade_out, R.anim.fade_in );
        }
    }

    private fun initSwipe() {
        val itemTouchHelper = ItemTouchHelper(correctCallBack)
        itemTouchHelper.attachToRecyclerView(customRecyclerView)
    }
}
