package cz.org.drivingformillions.models;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 12/8/2017.
 */

public class AddressResultModel {
    @SerializedName("address_components")
    public List<ComponentModel> components;

    @SerializedName("formatted_address")
    public String formatAddress;

    @SerializedName("geometry")
    public GeoModel geometry;

    @SerializedName("place_id")
    public String placeId;

    @SerializedName("types")
    public String types;
}
