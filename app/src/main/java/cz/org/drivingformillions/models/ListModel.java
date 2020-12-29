package cz.org.drivingformillions.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 12/6/2017.
 */

public class ListModel implements Serializable {

    public  int index;

    @SerializedName("id")
    public int id;

    @SerializedName("listname")
    public String listname;

    @SerializedName("updatedate")
    public String updateDate;

    @SerializedName("status")
    public String state;

    @SerializedName("properties")
    public ArrayList<PropertyModel> plist;

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public int getId() {
        return id;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public ArrayList<PropertyModel> getPlist() {
        return plist;
    }

    public String getListname() {
        return listname;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setListname(String listname) {
        this.listname = listname;
    }

    public void setPlist(ArrayList<PropertyModel> plist) {
        this.plist = plist;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isEmptyList(){
        if(this.plist == null) return true;
        else if(this.plist.size() == 0) return true;
        return false;
    }

}
