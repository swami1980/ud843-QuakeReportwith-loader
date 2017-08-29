/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.quakereportwithloader;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.view.View.GONE;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Location>> {
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=5&limit=10";

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    static String jsonResponse = "";
    static ArrayList<Location> curLoc = new ArrayList<Location>();

    public EarthquakeActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        /* check if there is internet connectivity */

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            TextView emptyText = (TextView) findViewById(R.id.text1);
            emptyText.setText("No Internet connection ");
        } else {
       /* Subclass to hold worker thread functionality */

/* forceload is the method which will trigger the load in background call back */
            Log.v("Process flow", "Loader initialized");
            getLoaderManager().initLoader(1, null, this);


        }
    }

    private void updateUi(ArrayList<Location> curLoc) {
        ListView earthquakeListView = (ListView) findViewById(R.id.list);
        TextView emptyText = (TextView) findViewById(R.id.text1);
        ProgressBar loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);
        if (curLoc.isEmpty()) {
           // earthquakeListView.setEmptyView(findViewById(R.id.text1));
            emptyText.setText("No earthquake data found");
            emptyText.setVisibility(View.VISIBLE);
            Log.v("Set", "Set text view visible");
        } else {
            Log.v("Response", jsonResponse);
             emptyText.setVisibility(GONE);
            loadingSpinner.setVisibility(GONE);
            // Create a new {@link ArrayAdapter} of earthquakes
            eqAdapter testadapter = new eqAdapter(
                    this, curLoc);

            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface
            earthquakeListView.setAdapter(testadapter);
        }
    }

    @Override
    public Loader<ArrayList<Location>> onCreateLoader(int i, Bundle bundle) {
        Log.v("Process flow","Loader instance created");
        return new EQAsyncTask(this) ;

    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Location>> loader, ArrayList<Location> locations) {
        Log.v("Process flow","Loading complete and Screen update process triggered");
        updateUi(locations);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }


    /*  Worker thread  ****************/
    private static class EQAsyncTask extends AsyncTaskLoader<ArrayList<Location>> {


       public EQAsyncTask(Context context) {
           super(context);

       }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            Log.v("Process flow","On start loading triggered from initloader");
            forceLoad();

        }


        @Override
       public ArrayList<Location> loadInBackground() {
           // Create URL object
            Log.v("Process flow","Background process trigggered from forceload method ");
           URL url = null;
           try {
               url = new URL(USGS_REQUEST_URL);
           } catch (MalformedURLException e) {
               e.printStackTrace();
           }

           // Perform HTTP request to the URL and receive a JSON response back

           try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
           } catch (IOException e) {
               // TODO Handle the IOException
           }

           curLoc = QueryUtils.extractJsonResponse(jsonResponse);
           // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
           return curLoc;
       }



       }
   }

