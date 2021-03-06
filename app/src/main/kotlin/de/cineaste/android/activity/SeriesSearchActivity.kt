package de.cineaste.android.activity

import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.View

import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type

import de.cineaste.android.R
import de.cineaste.android.adapter.series.SeriesSearchQueryAdapter
import de.cineaste.android.database.dao.BaseDao
import de.cineaste.android.database.dbHelper.SeriesDbHelper
import de.cineaste.android.entity.series.Series
import de.cineaste.android.network.NetworkClient
import de.cineaste.android.network.NetworkRequest
import de.cineaste.android.network.SeriesCallback
import de.cineaste.android.network.SeriesLoader

class SeriesSearchActivity : AbstractSearchActivity(), SeriesSearchQueryAdapter.OnSeriesStateChange {

    private lateinit var seriesQueryAdapter: SeriesSearchQueryAdapter

    override val layout: Int
        get() = R.layout.activity_series_search

    override val listAdapter: RecyclerView.Adapter<*>
        get() = seriesQueryAdapter

    override val listType: Type
        get() = object : TypeToken<List<Series>>() {

        }.type

    override fun getIntentForDetailActivity(itemId: Long): Intent {
        val intent = Intent(this, SeriesDetailActivity::class.java)
        intent.putExtra(BaseDao.SeriesEntry.ID, itemId)
        intent.putExtra(this.getString(R.string.state), R.string.searchState)
        return intent
    }

    override fun onSeriesStateChangeListener(series: Series, viewId: Int, index: Int) {
        val dbHelper = SeriesDbHelper.getInstance(this)
        val seriesCallback: SeriesCallback?
        when (viewId) {
            R.id.to_watchlist_button -> seriesCallback = object : SeriesCallback {
                override fun onFailure() {
                    runOnUiThread { seriesAddError(series, index) }
                }

                override fun onSuccess(series: Series) {
                    dbHelper.addToWatchList(series)
                }
            }
            R.id.history_button ->

                seriesCallback = object : SeriesCallback {
                    override fun onFailure() {
                        runOnUiThread { seriesAddError(series, index) }
                    }

                    override fun onSuccess(series: Series) {
                        dbHelper.addToHistory(series)

                    }
                }
            else -> seriesCallback = null
        }
        if (seriesCallback != null) {
            seriesQueryAdapter.removeSerie(index)

            SeriesLoader(this).loadCompleteSeries(series.id, seriesCallback)
        }
    }

    private fun seriesAddError(series: Series, index: Int) {
        val snackbar = Snackbar
                .make(recyclerView, R.string.could_not_add_movie, Snackbar.LENGTH_LONG)
        snackbar.show()
        seriesQueryAdapter.addSerie(series, index)
    }

    override fun initAdapter() {
        seriesQueryAdapter = SeriesSearchQueryAdapter(this, this)
    }

    override fun getSuggestions() {
        val client = NetworkClient(NetworkRequest(resources).popularSeries)
        client.sendRequest(networkCallback)
    }

    override fun searchRequest(searchQuery: String) {
        val client = NetworkClient(NetworkRequest(resources).searchSeries(searchQuery))
        client.sendRequest(networkCallback)
    }

    override fun getRunnable(json: String, listType: Type): Runnable {
        return Runnable {
            val series: List<Series> = gson.fromJson(json, listType)
            seriesQueryAdapter.addSeries(series)
            progressBar.visibility = View.GONE
        }
    }
}