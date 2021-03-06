package com.catherine.materialdesignapp.jetpack.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.catherine.materialdesignapp.listeners.ProguardIgnored;

import java.util.List;

@Entity(tableName = "album_table", primaryKeys = {"artist", "title"})
public class Album implements ProguardIgnored, Comparable<Album> {
    private String image;

    @NonNull
    private String artist = "";

    @NonNull
    private String title = "";

    private String url;

    private String thumbnail_image;

    private List<String> songs;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail_image() {
        return thumbnail_image;
    }

    public void setThumbnail_image(String thumbnail_image) {
        this.thumbnail_image = thumbnail_image;
    }

    public List<String> getSongs() {
        return songs;
    }

    public void setSongs(List<String> songs) {
        this.songs = songs;
    }

    @Override
    public String toString() {
        return "{image = " + image + ", artist = " + artist + ", songs = " + songs + ", title = " + title + ", url = " + url + ", thumbnail_image = " + thumbnail_image + "}";
    }

    @Override
    public int compareTo(@NonNull Album o) {
        if (0 == artist.compareTo(o.artist) && 0 == title.compareTo(o.title))
            return 0;
        return -1;
    }
}