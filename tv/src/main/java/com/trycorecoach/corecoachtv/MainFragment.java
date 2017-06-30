/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.trycorecoach.corecoachtv;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.trycorecoach.corecoachtv.lists.Exercise;
import com.trycorecoach.corecoachtv.lists.Section;
import com.trycorecoach.corecoachtv.manager.DataManager;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.R.attr.fragment;
import static android.R.attr.y;
import static com.trycorecoach.corecoachtv.manager.DataManager.EXERCISE_KEY;
import static com.trycorecoach.corecoachtv.manager.DataManager.readExercisesFromSharedPreferences;

public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static int NUM_ROWS;
    private static int NUM_COLS;

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private Drawable backgroundDrawable;
    private BackgroundManager mBackgroundManager;

    private SharedPreferences sharedPreferences;

    // Keys for sections and exercises saved in sharedpreferences
    public final String SECTION_KEY = "SECTION_KEY";
    public final String EXERCISE_KEY = "EXERCISE_KEY";

    private ArrayList<Exercise> mainExercises;
    private ArrayList<Section> mainSections;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        mBackgroundManager.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.background));

        setupUIElements();

        loadRows();

        setOnItemViewClickedListener(new ItemViewClickedListener());

        new ParseSectionsAndExercisesTask().execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mBackgroundManager.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.background));
    }

    private void loadRows() {

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        int i;
        mainSections = new ArrayList<>();
        mainExercises = new ArrayList<>();
        ArrayList<Section> sectionsForCheck = DataManager.readSectionsFromSharedPreferences(getActivity());
        ArrayList<Exercise> exercisesForCheck = DataManager.readExercisesFromSharedPreferences(getActivity());
        if (sectionsForCheck != null) {
            mainSections = sectionsForCheck;
        }
        if (exercisesForCheck != null) {
            mainExercises = exercisesForCheck;
        }
        int y = 0;
        String[] sectionsArray = new String[100];
        for (Section section: mainSections) {
            sectionsArray[y] = (section.title);
            y++;
        }
        String[] exercisesArray = new String[100];
        for (Exercise exercise: mainExercises) {
            exercisesArray[y] = (exercise.title);
        }
        NUM_ROWS = y;
        for (i = 0; i < NUM_ROWS; i++) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            for (Exercise exercise: mainExercises) {
                if(exercise.sectionID == i+1) {
                    listRowAdapter.add(exercise);
                }
            }
            HeaderItem header = new HeaderItem(i, sectionsArray[i]);
            mRowsAdapter.add(new ListRow(header, listRowAdapter));
        }
        setAdapter(mRowsAdapter);
    }

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(getResources().getColor(R.color.fastlane_background));
    }

    private void setupEventListeners() {
//        setOnSearchClickedListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
//                        .show();
//            }
//        });

//        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

//    protected void updateBackground(String uri) {
//        int width = mMetrics.widthPixels;
//        int height = mMetrics.heightPixels;
//        Glide.with(getActivity())
//                .load(uri)
//                .centerCrop()
//                .error(mDefaultBackground)
//                .into(new SimpleTarget<GlideDrawable>(width, height) {
//                    @Override
//                    public void onResourceReady(GlideDrawable resource,
//                                                GlideAnimation<? super GlideDrawable>
//                                                        glideAnimation) {
//                        mBackgroundManager.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.background));
//                    }
//                });
//        mBackgroundTimer.cancel();
//    }

//    private void startBackgroundTimer() {
//        if (null != mBackgroundTimer) {
//            mBackgroundTimer.cancel();
//        }
//        mBackgroundTimer = new Timer();
//        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
//    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Exercise) {
                Exercise exercise = (Exercise) item;
                Log.d(TAG, "Item: " + item.toString());
//
                Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                intent.putExtra(DetailsActivity.EXERCISE, exercise);
                getActivity().startActivity(intent);

//                Log.d(TAG, "exercise: " + intent.getParcelableExtra(DetailsActivity.EXERCISE));

//                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        getActivity(),
//                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
//                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
//                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0) {
                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

//    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
//        @Override
//        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
//                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
//            if (item instanceof Movie) {
//                mBackgroundURI = ((Movie) item).getBackgroundImageURI();
//                startBackgroundTimer();
//            }
//
//        }
//    }

//    private class UpdateBackgroundTask extends TimerTask {
//
//        @Override
//        public void run() {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mBackgroundURI != null) {
//                        updateBackground(mBackgroundURI.toString());
//                    }
//                }
//            });
//
//        }
//    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.video_list_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

    // Parsing and saving exercises and sections into shared preferences
    class ParseSectionsAndExercisesTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        String result = null;

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL("http://162.243.11.219/api/v1/sections");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();

                result = "Success";

            } catch (Exception e) {

                result = null;

//                runOnUiThread(new Runnable() {
//                    public void run() {
//                    }
//                });

                e.printStackTrace();
            }

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = sharedPreferences.edit();

            try {
                JSONArray jsonArray = new JSONArray(resultJson);
                ArrayList<Section> sections = new ArrayList<Section>();
                ArrayList<Exercise> exercises = new ArrayList<Exercise>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject sectionJSON = jsonArray.getJSONObject(i);
                    Section newSection = new Section();

                    newSection.id = sectionJSON.getInt("id");
                    newSection.title = sectionJSON.getString("title");
                    newSection.position = sectionJSON.getInt("position");
                    sections.add(newSection);

                    JSONArray exercisesJsonArray = sectionJSON.getJSONArray("exercises");

                    for (int x = 0; x < exercisesJsonArray.length(); x++) {
                        JSONObject exerciseJSON = exercisesJsonArray.getJSONObject(x);
                        Exercise newExercise = new Exercise();

                        newExercise.id = exerciseJSON.getInt("id");
                        newExercise.sectionID = newSection.id;
                        newExercise.sectionPosition = newSection.position;
                        newExercise.title = exerciseJSON.getString("title");
                        newExercise.position = exerciseJSON.getInt("position");
                        newExercise.videoURL = exerciseJSON.getString("video");
                        newExercise.instructionsPhoneURL = exerciseJSON.getString("pdf");
                        newExercise.instructionsTabletURL = exerciseJSON.getString("ipad_pdf");
                        exercises.add(newExercise);
                    }
                }

                Gson gson = new Gson();
                editor.putString(SECTION_KEY, gson.toJson(sections));
                editor.putString(EXERCISE_KEY, gson.toJson(exercises));
                editor.apply();
                Log.d(TAG, "Sections: " + sections);
                Log.d(TAG, "Exercises: " + exercises);


                result = "Success";

            } catch (Exception e) {
                e.printStackTrace();
                result = null;
            }

            return result;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            ArrayList<Section> sectionsForCheck = DataManager.readSectionsFromSharedPreferences(getActivity());
            Log.d(TAG, "sectionsForCheck: " + sectionsForCheck);
            ArrayList<Exercise> exercisesForCheck = DataManager.readExercisesFromSharedPreferences(getActivity());
            Log.d(TAG, "exercisesForCheck: " + exercisesForCheck);
            Boolean sectionBool = false;
            Boolean exerciseBool = false;

            Log.d(TAG, "mainSectionsPRE: " + mainSections);
            Log.d(TAG, "mainExercisesPRE: " + mainExercises);


            if(mainSections != sectionsForCheck && sectionsForCheck != null) {
                mainSections = sectionsForCheck;
                sectionBool = true;
            }
            if (mainExercises != exercisesForCheck && exercisesForCheck != null) {
                mainExercises = exercisesForCheck;
                exerciseBool = true;
            }
            Log.d(TAG, "mainSectionsPOST: " + mainSections);
            Log.d(TAG, "mainExercisesPOST: " + mainExercises);
            if (sectionBool || exerciseBool) {
                loadRows();
            }

            if (result == null) {
                Toast.makeText(getActivity(), "Check Internet connection", Toast.LENGTH_LONG)
                        .show();
            }

        }
    }

}
