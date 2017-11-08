package com.mba2dna.apps.bnina;

/*-----------------------------------

    - Recipes -

    created by FV iMAGINATION © 2017
    All Rights reserved

-----------------------------------*/


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class OtherUserProfile extends AppCompatActivity {

    /* Views */
    ImageView avatarImg;
    TextView titleTxt, fullnameTxt, aboutUserTxt, userRecipesTxt;
    ProgressDialog pd;


    /* Variables */
    ParseUser otherUserObj;
    List<ParseObject>otherUserRecipesArray;
    List<ParseObject>likesArray;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_user_profile);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init views
        avatarImg = (ImageView)findViewById(R.id.crAvatarImg);
        fullnameTxt = (TextView) findViewById(R.id.oupFullnameTxt);
        aboutUserTxt = (TextView) findViewById(R.id.oupAboutUserTxt);
        userRecipesTxt = (TextView) findViewById(R.id.oupUserRecipesTxt);
        titleTxt = (TextView) findViewById(R.id.oupTitleTxt);


        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);
        pd.setIcon(R.drawable.splashlogo);



        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("userID");
        otherUserObj = (ParseUser) ParseUser.createWithoutData(Configs.USER_CLASS_NAME, objectID);
        try { otherUserObj.fetchIfNeeded().getParseObject(Configs.USER_CLASS_NAME);

            // Call query
            showUserDetails();





            // MARK: - REPORT USER BUTTON ------------------------------------
            Button repButt = (Button)findViewById(R.id.oupReportButt);
            repButt.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {

                  final AlertDialog.Builder alert = new AlertDialog.Builder(OtherUserProfile.this);
                  final EditText editTxt = new EditText(OtherUserProfile.this);
                  editTxt.setHint("");

                  alert.setMessage("اشرح لنا بإيجاز سبب الإبلاغ عن هذا المستخدم")
                      .setView(editTxt)
                          .setTitle(R.string.app_name)
                              .setIcon(R.drawable.splashlogo)
                              .setNegativeButton("إلغاء", null)
                              .setPositiveButton("أوكي", new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which) {

                                  pd.setMessage("Reporting User...");
                                  pd.show();

                                  // Report user via Cloud Code
                                  HashMap<String, Object> params = new HashMap<String, Object>();
                                  params.put("userId", otherUserObj.getObjectId());
                                  params.put("reportMessage", editTxt.getText().toString());

                                  ParseCloud.callFunctionInBackground("reportUser", params, new FunctionCallback<ParseUser>() {
                                      public void done(ParseUser user, ParseException error) {
                                          if (error == null) {
                                              pd.dismiss();

                                              // Automatically report all thids User's bnina
                                              ParseQuery query = new ParseQuery(Configs.RECIPES_CLASS_NAME);
                                              query.whereEqualTo(Configs.RECIPES_USER_POINTER, otherUserObj);
                                              query.findInBackground(new FindCallback<ParseObject>() {
                                                  public void done(List<ParseObject> objects, ParseException error) {
                                                      if (error == null) {

                                                      for (int i = 0; i<objects.size(); i++) {
                                                          ParseObject rObj = objects.get(i);
                                                          rObj.put(Configs.RECIPES_IS_REPORTED, true);
                                                          rObj.put(Configs.RECIPES_REPORT_MESSAGE, "*Automatically reported after User report*");
                                                          rObj.saveInBackground();
                                                      }
                                              }}});


                                              AlertDialog.Builder alert = new AlertDialog.Builder(OtherUserProfile.this);
                                              alert.setMessage("شكرا على الإبلاغ " + otherUserObj.getString(Configs.USER_FULLNAME) + ". سنقوم التحقق من ذلك في غضون 24h.")
                                              .setTitle(R.string.app_name)
                                              .setPositiveButton("أوكي", new DialogInterface.OnClickListener() {
                                                  @Override
                                                  public void onClick(DialogInterface dialog, int which) {
                                                      finish();
                                              }})
                                              .setIcon(R.drawable.splashlogo);
                                              alert.create().show();


                          // Error in Cloud Code
                          } else {
                              Configs.simpleAlert(error.getMessage(), OtherUserProfile.this);
                              pd.dismiss();}
                      }});


                  }});
                  alert.create().show();
             }});


        } catch (ParseException e) { e.printStackTrace(); }




        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = (Button)findViewById(R.id.oupBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
         }});



        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()







    // MARK: - SHOW OTHER USER DETAILS --------------------------------------------------------
    void showUserDetails() {
        // Set title
        titleTxt.setText(otherUserObj.getString(Configs.USER_FULLNAME));
       // titleTxt.setTypeface(Configs.typeWriter);

        // Get avatar
        ParseFile fileObject = otherUserObj.getParseFile(Configs.USER_AVATAR);
        if (fileObject != null ) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            avatarImg.setImageBitmap(bmp);
        }}}});}

        if (otherUserObj.getString(Configs.USER_JOB) != null) { fullnameTxt.setText(otherUserObj.getString(Configs.USER_FULLNAME) + ", " + otherUserObj.getString(Configs.USER_JOB));
        } else { fullnameTxt.setText(otherUserObj.getString(Configs.USER_FULLNAME)); }

        if (otherUserObj.getString(Configs.USER_ABOUTME) != null) { aboutUserTxt.setText(otherUserObj.getString(Configs.USER_ABOUTME));
        } else { aboutUserTxt.setText("N/D"); }

        userRecipesTxt.setText(otherUserObj.getString(Configs.USER_FULLNAME) + " وصفة");


        // Call query
        queryUserRecipes();
    }






    // MARK: - QUERY USER RECIPES ---------------------------------------------------------------
    void queryUserRecipes() {
        pd.setMessage("يرجى الإنتضار...");
        pd.show();

        ParseQuery query = new ParseQuery(Configs.RECIPES_CLASS_NAME);
        query.whereEqualTo(Configs.RECIPES_USER_POINTER, otherUserObj);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    otherUserRecipesArray = objects;
                    pd.dismiss();


                    // CUSTOM GRID ADAPTER
                    class GridAdapter extends BaseAdapter {
                        private Context context;
                        public GridAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(final int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                cell = inflater.inflate(R.layout.cell_recipe, null);
                            }
                            final View finalCell = cell;

                            // Get Parse object
                            final ParseObject rObj = otherUserRecipesArray.get(position);

                            // Get userPointer
                            rObj.getParseObject(Configs.RECIPES_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(final ParseObject userPointer, ParseException e) {


                                    // Get Title
                                    TextView titleTxt = (TextView) finalCell.findViewById(R.id.crRecipeTitletxt);
                                    titleTxt.setText(rObj.getString(Configs.RECIPES_TITLE));

                                    // Get Category
                                    TextView catTxt = (TextView) finalCell.findViewById(R.id.crCategoryTxt);
                                    catTxt.setText(rObj.getString(Configs.RECIPES_CATEGORY));

                                    // Get Likes
                                    final TextView likesTxt = (TextView) finalCell.findViewById(R.id.crLikesTxt);
                                    if (rObj.getNumber(Configs.RECIPES_LIKES) != null) { likesTxt.setText(String.valueOf(rObj.getNumber(Configs.RECIPES_LIKES)));
                                    } else { likesTxt.setText("0"); }


                                    // Get Cover Image
                                    final ImageView coverImg = (ImageView) finalCell.findViewById(R.id.crRecipeImg);
                                    ParseFile fileObject = rObj.getParseFile(Configs.RECIPES_COVER);
                                    if(fileObject!= null) {
                                        fileObject.getDataInBackground(new GetDataCallback() {
                                            public void done(byte[] data, ParseException error) {
                                                if (error == null) {
                                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                    if (bmp != null) {
                                                        coverImg.setImageBitmap(bmp);
                                    }}}});}


                                    // MARK: - TAP COVER IMAGE TO SEE RECIPE'S DETAILS ------------------------------
                                    coverImg.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Configs.categoryStr = "";

                                            ParseObject rObj = otherUserRecipesArray.get(position);
                                            Intent i = new Intent(OtherUserProfile.this, RecipeDetails.class);
                                            Bundle extras = new Bundle();
                                            extras.putString("objectID", rObj.getObjectId());
                                            i.putExtras(extras);
                                            startActivity(i);
                                    }});




                                    // Get Avatar
                                    final ImageView avatarImage = (ImageView) finalCell.findViewById(R.id.crAvatarImg);
                                    ParseFile fileObject2 = userPointer.getParseFile(Configs.USER_AVATAR);
                                    if (fileObject2 != null ) {
                                        fileObject2.getDataInBackground(new GetDataCallback() {
                                            public void done(byte[] data, ParseException error) {
                                                if (error == null) {
                                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                    if (bmp != null) {
                                                        avatarImage.setImageBitmap(bmp);
                                                    }}}});}

                                    // Get Fullname
                                    TextView fnTxt = (TextView) finalCell.findViewById(R.id.crFullnameTxt);
                                    fnTxt.setText(userPointer.getString(Configs.USER_FULLNAME));






                                    // MARK: - LIKE BUTTON ---------------------------------------------
                                    Button likeButt = (Button) finalCell.findViewById(R.id.crLikeButt);
                                    likeButt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            final ParseUser currUser = ParseUser.getCurrentUser();

                                            // USER IS LOGGED IN
                                            if (currUser.getUsername() != null) {

                                                // Query Likes
                                                ParseQuery query = new ParseQuery(Configs.LIKES_CLASS_NAME);
                                                query.whereEqualTo(Configs.LIKES_LIKED_BY,currUser);
                                                query.whereEqualTo(Configs.LIKES_RECIPE_LIKED, rObj);
                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    public void done(List<ParseObject> objects, ParseException error) {
                                                        if (error == null) {
                                                            likesArray = objects;

                                                            ParseObject likesObj = new ParseObject(Configs.LIKES_CLASS_NAME);

                                                            if (likesArray.size() == 0) {
                                                                // LIKE RECIPE -----------
                                                                rObj.increment(Configs.RECIPES_LIKES, 1);
                                                                int likeInt = (int)rObj.getNumber(Configs.RECIPES_LIKES);
                                                                likesTxt.setText(Configs.roundThousandsIntoK(rObj.getInt(Configs.RECIPES_LIKES)));
                                                                rObj.saveInBackground();

                                                                likesObj.put(Configs.LIKES_LIKED_BY, currUser);
                                                                likesObj.put(Configs.LIKES_RECIPE_LIKED, rObj);
                                                                likesObj.saveInBackground(new SaveCallback() {
                                                                    @Override
                                                                    public void done(ParseException e) {
                                                                        if (e == null) {
                                                                            Configs.simpleAlert("لقد أحببت هذه الوصفة وحفظها في حسابك!", OtherUserProfile.this);

                                                                            // Send push notification
                                                                            final String pushMessage = currUser.getString(Configs.USER_FULLNAME) + " أحب وصفك: " + rObj.getString(Configs.RECIPES_TITLE);

                                                                            HashMap<String, Object> params = new HashMap<String, Object>();
                                                                            params.put("someKey", userPointer.getObjectId());
                                                                            params.put("data", pushMessage);

                                                                            ParseCloud.callFunctionInBackground("pushAndroid", params, new FunctionCallback<String>() {
                                                                                @Override
                                                                                public void done(String object, ParseException e) {
                                                                                    if (e == null) {

                                                                                        Log.i("log-", "PUSH SENT TO: " + userPointer.getString(Configs.USER_USERNAME)
                                                                                                + "\nMESSAGE: "
                                                                                                + pushMessage);

                                                                                    // Error in Cloud Code
                                                                                    } else {
                                                                                        Configs.simpleAlert(e.getMessage(), OtherUserProfile.this);
                                                                                        pd.dismiss();
                                                                            }}});


                                                                            // Save activity
                                                                            ParseObject actObj = new ParseObject(Configs.ACTIVITY_CLASS_NAME);
                                                                            actObj.put(Configs.ACTIVITY_CURRENT_USER, userPointer);
                                                                            actObj.put(Configs.ACTIVITY_OTHER_USER, currUser);
                                                                            actObj.put(Configs.ACTIVITY_TEXT, pushMessage);
                                                                            actObj.saveInBackground();
                                                                        }}});



                                                            // UNLIKE RECIPE --------------------------
                                                            } else if (likesArray.size() > 0) {
                                                                rObj.increment(Configs.RECIPES_LIKES, -1);
                                                                int likeInt = (int)rObj.getNumber(Configs.RECIPES_LIKES);
                                                                likesTxt.setText(String.valueOf(likeInt));
                                                                rObj.saveInBackground();

                                                                likesObj = likesArray.get(0);
                                                                likesObj.deleteInBackground(new DeleteCallback() {
                                                                    @Override
                                                                    public void done(ParseException e) {
                                                                        if (e == null) {
                                                                            Configs.simpleAlert("لقد ألغيت هذه الوصفة", OtherUserProfile.this);
                                                                        }}});
                                                            }


                                                        // Error in query Likes
                                                        } else {
                                                            Configs.simpleAlert(error.getMessage(), OtherUserProfile.this);
                                                    }}});




                                            // USER IS NOT LOGGED IN/REGISTERED
                                            } else {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(OtherUserProfile.this);
                                                alert.setMessage("يجب عليك تسجيل الدخول / التسجيل للإعجاب بوصفة!")
                                                        .setTitle(R.string.app_name)
                                                        .setPositiveButton("الدخول", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                startActivity(new Intent(OtherUserProfile.this, Login.class));
                                                            }})
                                                        .setNegativeButton("إلغاء", null)
                                                        .setIcon(R.drawable.splashlogo);
                                                alert.create().show();
                                            }

                                        }}); // end likeButt




                                }}); // end userPointer


                            return cell;
                        }

                        @Override public int getCount() { return otherUserRecipesArray.size(); }
                        @Override public Object getItem(int position) { return otherUserRecipesArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }


                    // Init GridView and set its cell's width
                    GridView aGrid = (GridView) findViewById(R.id.oupRecipesGridView);
                    aGrid.setAdapter(new GridAdapter(OtherUserProfile.this, otherUserRecipesArray));

                    // Set number of Columns accordingly to the device used
                    float scalefactor = getResources().getDisplayMetrics().density * 150; // 150 is the cell's width
                    int number = getWindowManager().getDefaultDisplay().getWidth();
                    int columns = (int) ((float) number / (float) scalefactor);
                    aGrid.setNumColumns(columns);



                // error in query
                } else {
                    Configs.simpleAlert(error.getMessage(), OtherUserProfile.this);
                    pd.dismiss();
        }}});
    }





}// @end
