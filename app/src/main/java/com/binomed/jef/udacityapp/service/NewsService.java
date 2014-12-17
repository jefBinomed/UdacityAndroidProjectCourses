/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.binomed.jef.udacityapp.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.binomed.jef.udacityapp.data.NewsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Vector;


public class NewsService extends IntentService {
    private ArrayAdapter<String> mForecastAdapter;
    private Context mContext;
    public static final String THEME_QUERY_EXTRA = "tqe";
    private final String LOG_TAG = NewsService.class.getSimpleName();
    public NewsService() {
        super("News");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String themeQuery = intent.getStringExtra(THEME_QUERY_EXTRA);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String newsJsonStr = null;

        String format = "json";
        String version = "1.0";
        String size = "8";

        try {
            // Construct the URL for the Google News Api query
            // https://developers.google.com/news-search/v1/jsondevguide
            final String NEWS_BASE_URL =
                    "https://ajax.googleapis.com/ajax/services/search/news?";
            final String QUERY_PARAM = "q";
            final String VERSION_PARAM = "v";
            final String RSZ_PARAM = "rsz";


            Uri builtUri = Uri.parse(NEWS_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, themeQuery)
                    .appendQueryParameter(VERSION_PARAM, version)
                    .appendQueryParameter(RSZ_PARAM, size)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            newsJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.


        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String GNA_RESPONSE_DATA = "responseData";
        final String GNA_RESPONSE_STATUS = "responseStatus";
        final String GNA_RESULTS = "results";

        final String GNA_DATETIME = "publishedDate";
        final String GNA_CONTENT = "content";
        final String GNA_TITLE = "titleNoFormatting";
        final String GNA_PUBLISHER = "publisher";
        final String GNA_LANGUAGE = "language";
        final String GNA_URL = "unsecapeUrl";

        // All Images are children of the "temp" object.
        final String GNA_IMAGE = "image";

        final String GNA_IMAGE_URL = "url";
        final String GNA_THUMBNAIL_IMAGE_URL = "tbUrl";


        try {
            JSONObject newsJson = new JSONObject(newsJsonStr);
            JSONObject responseData = newsJson.getJSONObject(GNA_RESPONSE_DATA);
            int status = newsJson.getInt(GNA_RESPONSE_STATUS);
            if (status == 200){
                JSONArray newsArray = responseData.getJSONArray(GNA_RESULTS);


                // Insert the new weather information into the database
                Vector<ContentValues> cVVector = new Vector<ContentValues>(newsArray.length());

                for (int i = 0; i < newsArray.length(); i++) {
                    // These are the values that will be collected.


                    String dateTime;
                    String content;
                    String title;
                    String publisher;
                    String langage;
                    String url;

                    // Get the JSON object representing a news
                    JSONObject newsItem = newsArray.getJSONObject(i);

                    dateTime = newsItem.getString(GNA_DATETIME);
                    content = newsItem.getString(GNA_CONTENT);
                    title = newsItem.getString(GNA_TITLE);
                    publisher = newsItem.getString(GNA_PUBLISHER);
                    langage = newsItem.getString(GNA_LANGUAGE);
                    url = newsItem.getString(GNA_URL);


                    // List of image if present !
                    JSONObject imageObject =
                            newsItem.getJSONObject(GNA_IMAGE);

                    String urlImage = null;
                    String urlImageThumbnail = null;

                    if (imageObject != null){

                        urlImage = imageObject.getString(GNA_IMAGE_URL);
                        urlImageThumbnail = imageObject.getString(GNA_THUMBNAIL_IMAGE_URL);
                    }

                    ContentValues newsValues = new ContentValues();

                    newsValues.put(NewsContract.NewsEntry.COLUMN_DATETEXT, NewsContract.getDbDateString(NewsContract.getDateFromJson(dateTime)));
                    newsValues.put(NewsContract.NewsEntry.COLUMN_THEME, themeQuery);
                    newsValues.put(NewsContract.NewsEntry.COLUMN_TITLE, title);
                    newsValues.put(NewsContract.NewsEntry.COLUMN_CONTENT, content);
                    newsValues.put(NewsContract.NewsEntry.COLUMN_PUBLISHER, publisher);
                    newsValues.put(NewsContract.NewsEntry.COLUMN_LANGUAGE, langage);
                    newsValues.put(NewsContract.NewsEntry.COLUMN_URL, url);
                    newsValues.put(NewsContract.NewsEntry.COLUMN_URL_IMAGE, urlImage);
                    newsValues.put(NewsContract.NewsEntry.COLUMN_URL_IMAGE_THUMBNAIL, urlImageThumbnail);

                    cVVector.add(newsValues);
                }
                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    this.getContentResolver().bulkInsert(NewsContract.NewsEntry.CONTENT_URI,
                            cvArray);


                }
                Log.d(LOG_TAG, "News Service Complete. " + cVVector.size() + " Inserted");
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }




}
