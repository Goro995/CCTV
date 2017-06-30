package com.trycorecoach.corecoachtv.lists;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * List of exercise objects
 */
public class Exercise implements Serializable {

    public int id;
    public int sectionID;
    public String title = "";
    public String videoURL = "";
    public String instructionsPhoneURL = "";
    public String instructionsTabletURL = "";
    public int position;
    public int sectionPosition;
    public Exercise() {}

    private int mData;

    @Override
    public String toString() {
        return this.title;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel out, int flags) {
//        out.writeInt(mData);
//    }

    public static final Parcelable.Creator<Exercise> CREATOR = new Parcelable.Creator<Exercise>() {
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    private Exercise(Parcel in) {
        mData = in.readInt();
    }

}