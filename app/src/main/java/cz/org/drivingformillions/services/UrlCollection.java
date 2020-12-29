package cz.org.drivingformillions.services;

/**
 * Created by Administrator on 12/4/2017.
 */

public class UrlCollection {
    public static String server_url = "http://drivingformillions.com/DrivingForMillions/api/";
    public static String signup_url = "users/sign_up";
    public static String login_url = "users/login";
    public static String login_fb_url = "users/login_fb";
    public static String forgotpassword_url = "users/forgotpassword";
    public static String changepass_url = "users/changepassword";
    public static String updateprofile_url = "users/updateprofile";
    public static String getuserinfo_url = "users/getuserbyid";
    public static String createlist_url = "products/createList";
    public static String setFavorite_url = "products/setFavorite";
    public static String getLists_url = "products/getProducts";
    public static String updatelist_url = "products/updateList";
    public static String getFavorites_url = "products/getFavorites";
    public static String deletelist_url = "products/removeProducts";
    public static String getAddressFromLocation_url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=true";
    public static String getStreetView_url = "https://maps.googleapis.com/maps/api/streetview?size=300x300&location=%f,%f";
    public static String getOwnerInfo_url = "https://search.onboard-apis.com/propertyapi/v1.0.0/property/detailowner?address1=%s&address2=%s";
    public static String getOwnerInfo2_url = "https://estated.com/api/property?token=%s&address=%s&city=%s&state=%s&zipcode=%s";
}
