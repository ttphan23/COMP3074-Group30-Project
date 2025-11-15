package ca.gbc.comp3074.comp3074.data.remote.models;

import com.google.gson.annotations.SerializedName;

public class GameApiModel {

    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("rating")
    private double rating;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title != null ? title : "Unknown Game";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public double getRating() {
        return rating;
    }
}
