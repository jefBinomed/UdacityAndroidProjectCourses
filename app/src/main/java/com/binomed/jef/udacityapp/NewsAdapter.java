package com.binomed.jef.udacityapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.binomed.jef.udacityapp.data.NewsContract;
import com.koushikdutta.ion.Ion;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jef on 18/12/14.
 */
public class NewsAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 1;
    private static final int VIEW_TYPE_NEWS = 0;


    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView publisherView;
        public final TextView titleView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            publisherView = (TextView) view.findViewById(R.id.list_item_publisher_textview);
            titleView = (TextView) view.findViewById(R.id.list_item_title_textview);
        }
    }

    public NewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_NEWS: {
                layoutId = R.layout.list_item_news;
                break;
            }

        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_NEWS: {
                // Get weather icon
                Ion.with(viewHolder.iconView)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_delete)
                        .load(cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_URL_IMAGE_THUMBNAIL)));

                break;
            }

        }

        // Read date from cursor
        String dateString = cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_DATETEXT));
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(getFriendlyDayString(context, dateString));

        // Read weather forecast from cursor
        String title = cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_TITLE));
        // Find TextView and set weather forecast on it
        viewHolder.titleView.setText(title);

        // For accessibility, add a content description to the icon field
        viewHolder.iconView.setContentDescription(title);

        String publisher = cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_PUBLISHER));
        // Find TextView and set weather forecast on it
        viewHolder.publisherView.setText(publisher);

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
    public int getItemViewType(int position) {
        return VIEW_TYPE_NEWS;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
