package com.trycorecoach.corecoachtv.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trycorecoach.corecoachtv.lists.Exercise;
import com.trycorecoach.corecoachtv.lists.Section;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class DataManager {

    // Keys for sections and exercises saved in sharedpreferences
    public static final String SECTION_KEY = "SECTION_KEY";
    public static final String EXERCISE_KEY = "EXERCISE_KEY";


    /**
     *     Getting sections from sharedpreferences
     *     @param context Context
     *     @return Sections retrieved from shared preferences
     */
    public static ArrayList<Section> readSectionsFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String fetchResult = sharedPreferences.getString(SECTION_KEY, "");
        ArrayList<Section> sections = parseStringToArrayListSection(fetchResult);
        if (sections != null) {
            Collections.sort(sections, new Comparator<Section>() {
                @Override
                public int compare(Section s1, Section s2) {
                    if (s1.position > s2.position) {
                        return 1;
                    } else if (s1.position == s2.position) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
        }
        return sections;
    }

    /**
     * Getting exercises from sharedpreferences
     * @param context Context
     * @return Exercises retrieved from shared preferences
     */
    public static ArrayList<Exercise> readExercisesFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String fetchResult = sharedPreferences.getString(EXERCISE_KEY, "");
        ArrayList<Exercise> exercises = parseStringToArrayListExercise(fetchResult);
        if(exercises != null) {
            Collections.sort(exercises, new Comparator<Exercise>() {
                @Override
                public int compare(Exercise e1, Exercise e2) {
                    if (e1.position > e2.position) {
                        return 1;
                    } else if (e1.position == e2.position) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
        }
        return exercises;
    }

    /**
     * Parse string to array list of sections
     * @param stringToParse input list in string type
     * @return output of sections list in arrayList type
     */
    private static ArrayList<Section> parseStringToArrayListSection(String stringToParse) {
        Type itemsListType = new TypeToken<List<Section>>(){}.getType();
        ArrayList<Section> contentRecords = new Gson().fromJson(stringToParse, itemsListType);
        return contentRecords;
    }

    /**
     * Parse string to array list of exercises
     * @param stringToParse input list in string type
     * @return output list if exercises in arrayList type
     */
    private static ArrayList<Exercise> parseStringToArrayListExercise(String stringToParse) {
        Type itemsListType = new TypeToken<List<Exercise>>(){}.getType();
        ArrayList<Exercise> contentRecords = new Gson().fromJson(stringToParse, itemsListType);
        return contentRecords;
    }

    /**
     * Building up URL for content
     * @param pathString second part of link
     * @return whole link for some resource
     */
    public static String buildURLforContent(String pathString) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("162.243.11.219")
                .appendEncodedPath(pathString);
        return builder.build().toString();
    }

}
