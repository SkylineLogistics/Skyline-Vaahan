package online.skylinelogistics.vaahan;

public class config {

    //URLS
    public static final String LOGIN_URL = "https://api.skylinelogistics.online/vaahan/android/v1/login.php";
    //public static final String STATUS_URL = "https://api.skylinelogistics.online/vaahan/android/v1/status.php";
    public static final String STATUS_URL = "https://api.skylinelogistics.online/vaahan/android/v1/test_status.php";
    public static final String VEHICLE_DETAIL = "https://api.skylinelogistics.online/vaahan/android/v1/vehicle_detail.php";
    public static final String VEHICLE_LIST = "https://api.skylinelogistics.online/vaahan/android/v1/vehicle_list.php";

    //Keys for email and password as defined in our $_POST['key'] in login.php
    public static final String KEY_VNO = "vno";
    public static final String KEY_PASSWORD = "password";

    //If server response is equal to this that means login is successful
    public static final String LOGIN_SUCCESS = "success";

    //Keys for Sharedpreferences
    //This would be the name of our shared preferences
    public static final String SHARED_PREF_NAME = "SkylineVaahan";

    //This would be used to store the email of current logged in user
    public static final String VEHICLE_SHARED_PREF = "vno";

    //This would be used to store the stage of trip
    public static final String TRIP_STAGE = "TripStage";

    //We will use this to store the boolean in sharedpreference to track user is loggedin or not
    public static final String LOGGEDIN_SHARED_PREF = "loggedin";

    //This Would be The Successful Server Response After Status Update
    public static final String STATUS_SUCCESS = "success";

    public static final String RESPONSE_PREVIOUS_STEP = "step_last";

    public static final String LOGOUT_SUCCESS = "success";

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";


}
