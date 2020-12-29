package cz.org.drivingformillions.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 12/4/2017.
 */

public class UserModel {
    @SerializedName("id")
    public int id;

    @SerializedName("username")
    public String username;

    @SerializedName("first_name")
    public String firstname;

    @SerializedName("last_name")
    public String lastname;

    @SerializedName("email")
    public String email;

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
