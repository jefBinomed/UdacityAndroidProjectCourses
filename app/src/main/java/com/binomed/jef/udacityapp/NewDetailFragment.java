package com.binomed.jef.udacityapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.binomed.jef.udacityapp.data.NewsContract;
import com.binomed.jef.udacityapp.dummy.DummyContent;
import com.koushikdutta.ion.Ion;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A fragment representing a single New detail screen.
 * This fragment is either contained in a {@link NewListActivity}
 * in two-pane mode (on tablets) or a {@link NewDetailActivity}
 * on handsets.
 */
public class NewDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private static final String[] NEWS_COLUMNS = {
            NewsContract.NewsEntry.TABLE_NAME + "." + NewsContract.NewsEntry._ID,
            NewsContract.NewsEntry.COLUMN_DATETEXT,
            NewsContract.NewsEntry.COLUMN_URL,
            NewsContract.NewsEntry.COLUMN_PUBLISHER,
            NewsContract.NewsEntry.COLUMN_TITLE,
            NewsContract.NewsEntry.COLUMN_URL_IMAGE,
            NewsContract.NewsEntry.COLUMN_CONTENT
    };

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    private static final String NEWS_SHARE_HASHTAG = " #NewsApp";

    public static final String NEWS_KEY = "location";

    private ShareActionProvider mShareActionProvider;
    private String mTheme;
    private String mForecast;
    private String mUrlKey;

    private ImageView mIconView;
    private TextView mContentView;
    private TextView mDateView;
    private TextView mTitleView;
    private TextView mPublisherView;
    private TextView mLinkView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        /*if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.newsfragement, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_detail, container, false);

        // Show the dummy content as text in a TextView.

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUrlKey = arguments.getString(NEWS_KEY);
        }

        if (savedInstanceState != null) {
            mTheme = Utility.getPreferredTheme(getActivity());
        }

        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date);
        mContentView = (TextView) rootView.findViewById(R.id.detail_content);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_title);
        mPublisherView = (TextView) rootView.findViewById(R.id.detail_publisher);
        mLinkView = (TextView) rootView.findViewById(R.id.detail_link);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = NewsContract.NewsEntry.COLUMN_DATETEXT + " ASC";

        mTheme = Utility.getPreferredTheme(getActivity());
        // TODO corriger pour la requete
        Uri weatherForLocationUri = NewsContract.NewsEntry.buildNewsWithUrl(
                mTheme, mUrlKey);

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
    public static String getFriendlyDayString(Context context, String dateStr) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Date inputDate = NewsContract.getDateFromDb(dateStr);
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(inputDate);

    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Use weather art image

            Ion.with(mIconView)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_delete)
                    .load(data.getString(data.getColumnIndex(NewsContract.NewsEntry.COLUMN_URL_IMAGE)));


            // Read date from cursor and update views for day of week and date
            String date = data.getString(data.getColumnIndex(NewsContract.NewsEntry.COLUMN_DATETEXT));
             // Read date from cursor
            String dateString = data.getString(data.getColumnIndex(NewsContract.NewsEntry.COLUMN_DATETEXT));
            // Find TextView and set formatted date on it
            mDateView.setText(getFriendlyDayString(getActivity(), dateString));

            // Read description from cursor and update view
            String description = data.getString(data.getColumnIndex(
                    NewsContract.NewsEntry.COLUMN_CONTENT));
            mContentView.setText(description);

            // For accessibility, add a content description to the icon field
            mContentView.setContentDescription(description);


            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + NEWS_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
