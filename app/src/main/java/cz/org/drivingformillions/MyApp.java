package cz.org.drivingformillions;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import cz.org.drivingformillions.models.ListModel;
import cz.org.drivingformillions.models.PropertyModel;
import cz.org.drivingformillions.models.UserModel;

/**
 * Created by Administrator on 12/4/2017.
 */

public class MyApp extends Application {

    public static MyApp myApp = null;
    public static UserModel myProfile;
    public static ArrayList<ListModel> mylist;
    public static boolean editmode;
    public static boolean bReturn = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static MyApp getInstance(){
        if(myApp == null)
        {
            myApp = new MyApp();
            mylist = new ArrayList<>();
            editmode = true;
        }
        return myApp;
    }
}
