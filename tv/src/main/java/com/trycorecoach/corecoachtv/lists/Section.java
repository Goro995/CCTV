package com.trycorecoach.corecoachtv.lists;

/**
 * List of section objects
 */
public class Section {
    public int id;
    public String title = "";
    public int position;
    public Section() {}

    @Override
    public String toString() {
        return this.title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

}