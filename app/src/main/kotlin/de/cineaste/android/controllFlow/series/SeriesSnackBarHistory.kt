package de.cineaste.android.controllFlow.series

import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import de.cineaste.android.R
import de.cineaste.android.adapter.series.SeriesListAdapter
import de.cineaste.android.controllFlow.BaseSnackBar

class SeriesSnackBarHistory internal constructor(linearLayoutManager: LinearLayoutManager, view: View, private val adapter: SeriesListAdapter) : BaseSnackBar(linearLayoutManager, view) {

    override fun getSnackBarLeftSwipe(position: Int) {
        val seriesToBeDeleted = adapter.getItem(position)
        adapter.removeItem(position)

        val currentSeason = seriesToBeDeleted.currentNumberOfSeason
        val currentEpisode = seriesToBeDeleted.currentNumberOfEpisode

        val mySnackBar = Snackbar.make(view,
                R.string.series_deleted, Snackbar.LENGTH_LONG)
        mySnackBar.setAction(R.string.undo) {
            //do nothing
        }
        mySnackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                when (event) {
                    Snackbar.Callback.DISMISS_EVENT_ACTION -> {
                        adapter.addDeletedItemToHistoryAgain(seriesToBeDeleted, position, currentSeason, currentEpisode)
                        val fist = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                        if (fist >= position) {
                            linearLayoutManager.scrollToPosition(position)
                        }
                    }
                }
            }
        })
        mySnackBar.show()
    }

    override fun getSnackBarRightSwipe(position: Int, message: Int) {
        val seriesToBeUpdated = adapter.getItem(position)

        val currentSeason = seriesToBeUpdated.currentNumberOfSeason
        val currentEpisode = seriesToBeUpdated.currentNumberOfEpisode

        adapter.moveToWatchList(seriesToBeUpdated)
        val mySnackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        mySnackBar.setAction(R.string.undo) {
            //do nothing
        }
        mySnackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                when (event) {
                    Snackbar.Callback.DISMISS_EVENT_ACTION -> {
                        adapter.moveBackToHistory(seriesToBeUpdated, position, currentSeason, currentEpisode)

                        val first = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                        if (first >= position) {
                            linearLayoutManager.scrollToPosition(position)
                        }
                    }
                }
            }

        })
        mySnackBar.show()
    }

}
