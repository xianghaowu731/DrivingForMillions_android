package cz.org.drivingformillions.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 12/8/2017.
 */

public class ComponentModel {
    @SerializedName("long_name")
    public String longName;

    @SerializedName("short_name")
    public String  shortName;

    @SerializedName("types")
    public String types;

}
