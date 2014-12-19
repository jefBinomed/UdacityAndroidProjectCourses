package com.binomed.jef.udacityapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.binomed.jef.udacityapp.data.NewsContract;
import com.binomed.jef.udacityapp.service.NewsService;

import java.util.Date;

/**
 * A list fragment representing a list of News. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link NewDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class NewListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] NEWS_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            NewsContract.NewsEntry.TABLE_NAME + "." + NewsContract.NewsEntry._ID,
            NewsContract.NewsEntry.COLUMN_DATETEXT,
            NewsContract.NewsEntry.COLUMN_URL_ID,
            NewsContract.NewsEntry.COLUMN_URL_IMAGE_THUMBNAIL,
            NewsContract.NewsEntry.COLUMN_PUBLISHER,
            NewsContract.NewsEntry.COLUMN_TITLE
    };

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";


    private static final int NEWS_LOADER = 0;

    private NewsAdapter mNewsAdapter;

    private String mTheme;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: replace with a real list adapter.
        mNewsAdapter = new NewsAdapter(getActivity(), null, 0);
        setListAdapter(mNewsAdapter);

    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.


        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(NEWS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }


    @Override
    public void onResume() {
        super.onResume();

        Cursor cursor = getActivity().getContentResolver().query(NewsContract.NewsEntry.CONTENT_URI, null, null,null, null);
        Log.d(getTag(), "Nb news : "+cursor.getCount());

        // TODO faire quelque chose
        if (mTheme != null && !mTheme.equals(Utility.getPreferredTheme(getActivity()))) {
            getLoaderManager().restartLoader(NEWS_LOADER, null, this);
        }else{
            //

        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        Cursor cursor = mNewsAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            mCallbacks.onItemSelected(cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_URL_ID)));
        }
        mActivatedPosition = position;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = NewsContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = NewsContract.NewsEntry.COLUMN_DATETEXT + " ASC";

        mTheme = Utility.getPreferredTheme(getActivity());
        Uri weatherForLocationUri = NewsContract.NewsEntry.buildNewsWithTheme(mTheme);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                NEWS_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() ==0){
            updateNews();
        }else {
            mNewsAdapter.swapCursor(data);
            if (mActivatedPosition != ListView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                getListView().smoothScrollToPosition(mActivatedPosition);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNewsAdapter.swapCursor(null);
    }

    private void updateNews() {
        Intent alarmIntent = new Intent(getActivity(), NewsService.AlarmReceiver.class);
        alarmIntent.putExtra(NewsService.THEME_QUERY_EXTRA, Utility.getPreferredTheme(getActivity()));

        //Wrap in a pending intent which only fires once.
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);//getBroadcast(context, 0, i, 0);

        AlarmManager am=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

        //Set the AlarmManager to wake up the system.
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
    }
}
