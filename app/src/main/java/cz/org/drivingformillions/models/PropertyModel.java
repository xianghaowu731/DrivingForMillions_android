package cz.org.drivingformillions.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Administrator on 12/6/2017.
 */

public class PropertyModel implements Serializable {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("updatedate")
    public String updateDate;

    @SerializedName("lat")
    public String latitude;

    @SerializedName("lng")
    public String longitude;

    @SerializedName("address")
    public String address;

    @SerializedName("city")
    public String city;

    @SerializedName("country")
    public String state;

    @SerializedName("postal")
    public String zip;

    @SerializedName("owner_name")
    public String ownername;

    @SerializedName("email")
    public String owner_email;

    @SerializedName("phone")
    public String owner_phone;

    @SerializedName("secondary")
    public String secondary;

    @SerializedName("fav")
    public int fav;

    @SerializedName("tax_year")
    public String tax_year;

    @SerializedName("tax_amount")
    public String tax_amount;

    @SerializedName("tax_land")
    public String tax_land;

    @SerializedName("tax_improvement")
    public String tax_improvement;

    @SerializedName("tax_total")
    public String tax_total;

    @SerializedName("estimated_value")
    public String estimated_value;

    @SerializedName("estimated_low")
    public String estimated_low;

    @SerializedName("estimated_high")
    public String estimated_high;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}
