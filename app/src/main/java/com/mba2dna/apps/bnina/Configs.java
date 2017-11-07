package com.mba2dna.apps.bnina;

/*-----------------------------------

    - Recipes -

    created by FV iMAGINATION © 2017
    All Rights reserved

-----------------------------------*/

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.text.DecimalFormat;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class Configs extends Application {


    // IMPORTANT: Reaplce the red strings below with your own Application ID and Client Key of your app on Parse.com
    public static String PARSE_APP_ID = "5LgyxLAC1O5wT5mWEwr6F5ceNPb2C92vyxZU7Ri3";
    public static String PARSE_CLIENT_KEY = "6mErIYXXfYesLNq7To6FRV1sEt0cQTQAMhCVPqKL";
    //-----------------------------------------------------------------------------


    // FOOD CATEGORIES ARRAY (editable)
    public static String[] categoriesArray = new String[] {
            "Appetizer",
            "Breakfast",
            "Healthy",
            "Holidays & Events",
            "Main Dish",
            "Seafood",
            "Vegetarian",
            "Salad",
            "Desserts",
            "Beverage",

            // You can add categories here...
            // IMPORTANT: Also remember to add the proper images into the 'drawable'  folder.
    };


    // Custom yellow color
    public static String yellow = "#f7fc8e";



    // Set fonts
    public static Typeface typeWriter;






    /************** DO NOT EDIT THE CODE BELOW *******************/
    public static String COMMENTS_CLASS_NAME = "Comments";
    public static String COMMENTS_COMMENT = "comment";
    public static String COMMENTS_RECIPE_POINTER = "recipePointer";
    public static String COMMENTS_USER_POINTER = "userPointer";
    public static String RECIPES_COMMENTS = "comments";
    public static String  USER_CLASS_NAME = "_User";
    public static String  USER_FULLNAME = "fullName";
    public static String  USER_AVATAR = "avatar";
    public static String  USER_USERNAME = "username";
    public static String  USER_EMAIL = "email";
    public static String  USER_JOB = "job";
    public static String  USER_ABOUTME = "aboutMe";
    public static String  USER_IS_REPORTED = "isReported";
    public static String  USER_REPORT_MESSAGE = "reportMessage";


    public static String  LIKES_CLASS_NAME = "Likes";
    public static String  LIKES_LIKED_BY = "likedBy";
    public static String  LIKES_RECIPE_LIKED = "recipeLiked";


    public static String  RECIPES_CLASS_NAME = "Recipes";
    public static String  RECIPES_COVER = "cover";
    public static String  RECIPES_TITLE = "title";
    public static String  RECIPES_TITLE_LOWERCASE = "titleLowercase";
    public static String  RECIPES_CATEGORY = "category";
    public static String  RECIPES_LIKES = "likes";
    public static String  RECIPES_ABOUT = "aboutRecipe";
    public static String  RECIPES_DIFFICULTY = "difficulty";
    public static String  RECIPES_COOKING = "cooking";
    public static String  RECIPES_BAKING = "baking";
    public static String  RECIPES_RESTING = "resting";
    public static String  RECIPES_YOUTUBE = "youtube";
    public static String  RECIPES_VIDEO_TITLE = "videoTitle";
    public static String  RECIPES_INGREDIENTS = "ingredients";
    public static String  RECIPES_PREPARATION = "preparation";
    public static String  RECIPES_USER_POINTER = "userPointer";
    public static String  RECIPES_IS_REPORTED = "isReported";
    public static String  RECIPES_REPORT_MESSAGE = "reportMessage";
    public static String  RECIPES_KEYWORDS = "keywords";
    public static String  RECIPES_CREATED_AT = "createdAt";


    public static String  ACTIVITY_CLASS_NAME = "Activity";
    public static String  ACTIVITY_CURRENT_USER = "currentUser";
    public static String  ACTIVITY_OTHER_USER = "otherUser";
    public static String  ACTIVITY_TEXT = "text";


    public static String categoryStr = "";
    public static String shoppingString;

    public static AlertDialog pd;

    boolean isParseInitialized = false;

    public void onCreate() {
        super.onCreate();

        if (isParseInitialized == false) {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(String.valueOf(PARSE_APP_ID))
                    .clientKey(String.valueOf(PARSE_CLIENT_KEY))
                    .server("https://parseapi.back4app.com")
                    .build()
            );
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
            ParseUser.enableAutomaticUser();
            isParseInitialized = true;
        }

        // Init Facebook Utils
        ParseFacebookUtils.initialize(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/DroidKufiRegular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        // Tnit font
        // (the font files are into app/src/main/assets/font folder)
        typeWriter = Typeface.createFromAsset(getAssets(),"font/AmericanTypewriter.ttc");


    }// end onCreate()



    // SIMPLE ALERT DIALOG --------------------------------------------------
    public static void simpleAlert(String mess, Context context){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setMessage(mess)
                .setTitle(R.string.app_name)
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.logo);
        alert.create().show();
    }




    // MARK: - SCALE BITMAP TO MAX SIZE
    public static Bitmap scaleBitmapToMaxSize(int maxSize, Bitmap bm) {
            int outWidth;
            int outHeight;
            int inWidth = bm.getWidth();
            int inHeight = bm.getHeight();
            if(inWidth > inHeight){
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, outWidth, outHeight, false);
            return resizedBitmap;
    }
    public static void showPD(String mess, Context ctx) {
        AlertDialog.Builder db = new AlertDialog.Builder(ctx);
        @SuppressLint("WrongConstant") View dialogView = ((LayoutInflater) ctx.getSystemService("layout_inflater")).inflate(R.layout.pd, null);
        ((TextView) dialogView.findViewById(R.id.pdMessTxt)).setText(mess);
        db.setView(dialogView);
        db.setCancelable(true);
        pd = db.create();
        pd.show();
    }

    public static void hidePD() {
        pd.dismiss();
    }
    public static String roundThousandsIntoK(Number number) {
        char[] suffix = new char[]{' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10((double) numValue));
        int base = value / 3;
        if (value < 3 || base >= suffix.length) {
            return new DecimalFormat("#,##0").format(numValue);
        }
        return new DecimalFormat("#0.0").format(((double) numValue) / Math.pow(10.0d, (double) (base * 3))) + suffix[base];
    }


}//@end
