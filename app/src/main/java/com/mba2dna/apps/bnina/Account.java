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
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Account extends AppCompatActivity {

    /* Views */
    ProgressDialog pd;
    RelativeLayout noUserLayout, mainLayout;
    ImageView avatarImage;
    TextView titleTxt, fullNameTxt, aboutMeTxt;

    RadioButton myRecipesRB, likedRecipesRB;

    ListView myRecipesListView;
    GridView likedRecipesGridView;



    /* Variables */
    List<ParseObject> myRecipesArray;
    List<ParseObject>likedRecipesArray;




    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // ON START() ---------------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        if (ParseUser.getCurrentUser().getUsername() == null) {
            noUserLayout.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.INVISIBLE);

        } else {
            noUserLayout.setVisibility(View.INVISIBLE);
            mainLayout.setVisibility(View.VISIBLE);

            // Call queries
            queryMyRecipes();
            showUserDetails();
        }
    }





    // ON CREATE() ---------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init TabBar buttons
        Button tab_one = (Button)findViewById(R.id.tab_one);
        Button tab_two = (Button)findViewById(R.id.tab_two);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, Home.class));
            }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, Shopping.class));
            }});


        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);
        pd.setIcon(R.drawable.logo);


        // Init views
        noUserLayout = (RelativeLayout)findViewById(R.id.accNoUserLayout);
        mainLayout = (RelativeLayout)findViewById(R.id.accMainLayout);
        titleTxt = (TextView)findViewById(R.id.accTitleTxt);
       // titleTxt.setTypeface(Configs.typeWriter);
        fullNameTxt = (TextView)findViewById(R.id.accFullnametxt);
        aboutMeTxt = (TextView)findViewById(R.id.accAboutMeTxt);
        titleTxt = (TextView)findViewById(R.id.accTitleTxt);
        avatarImage = (ImageView) findViewById(R.id.epAvatarImg);
        myRecipesRB = (RadioButton) findViewById(R.id.accMyRecipesRB);
        likedRecipesRB = (RadioButton) findViewById(R.id.accLikedRecipesRB);
        myRecipesListView = (ListView)findViewById(R.id.accMyRecipesListView);
        likedRecipesGridView = (GridView) findViewById(R.id.accLikedRecipesGridView);




        // MARK: - RADIO BUTTONS ----------------------------------------------------
        myRecipesRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryMyRecipes();
                myRecipesRB.setChecked(true);
                likedRecipesRB.setChecked(false);
            }});

        likedRecipesRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryLikedRecipes();
                myRecipesRB.setChecked(false);
                likedRecipesRB.setChecked(true);
        }});




        // MARK: - EDIT PROFILE BUTTON ------------------------------------
        Button epButt = (Button)findViewById(R.id.accEditProfileButt);
        epButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(Account.this, EditProfile.class));
        }});




        // MARK: - ACTIVITY BUTTON ------------------------------------
        Button actButt = (Button)findViewById(R.id.accActivityButty);
        actButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Account.this, ActivityScreen.class));
        }});




        // MARK: - LOGOUT BUTTON ------------------------------------
        Button loButt = (Button)findViewById(R.id.accLogoutButt);

        // Disable Logout button in case you're not logged in
        if (ParseUser.getCurrentUser().getUsername() == null) { loButt.setEnabled(false); }

        loButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              AlertDialog.Builder alert = new AlertDialog.Builder(Account.this);
              alert.setMessage("Are you sure you want to logout?")
                      .setTitle(R.string.app_name)
                      .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {
                              pd.setMessage("Logging out...");
                              pd.show();

                              ParseUser.logOutInBackground(new LogOutCallback() {
                                  @Override
                                  public void done(ParseException e) {
                                      pd.dismiss();

                                      noUserLayout.setVisibility(View.VISIBLE);
                                      mainLayout.setVisibility(View.INVISIBLE);
                                  }});
                          }})
                      .setNegativeButton("Cancel", null)
                      .setIcon(R.drawable.logo);
              alert.create().show();
         }});




        // MARK: - ADD RECIPE BUTTON ------------------------------------
        Button arButt = (Button)findViewById(R.id.accAddRecipeButt);
        arButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              if (ParseUser.getCurrentUser().getUsername() != null) {
                  startActivity(new Intent(Account.this, AddEditRecipe.class));
              } else {
                  Configs.simpleAlert("You must login/sign up to add a Recipe!", Account.this);
              }
        }});




        // MARK: - LOGIN BUTTON ------------------------------------
        Button loginButt = (Button)findViewById(R.id.accLoginButt);
        loginButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(Account.this, Login.class));
         }});




        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()








    // MARK: - SHOW USER DETAILS -----------------------------------------------------------------
    void showUserDetails() {
        ParseUser currUser = ParseUser.getCurrentUser();

        // Get Avatar
        ParseFile fileObject = currUser.getParseFile(Configs.USER_AVATAR);
        if (fileObject != null ) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            avatarImage.setImageBitmap(bmp);
        }}}});}


        if (currUser.getString(Configs.USER_JOB) != null) { fullNameTxt.setText(currUser.getString(Configs.USER_FULLNAME) + ", " + currUser.getString(Configs.USER_JOB));
        } else { fullNameTxt.setText(currUser.getString(Configs.USER_FULLNAME)); }

        if (currUser.getString(Configs.USER_ABOUTME) != null) { aboutMeTxt.setText(currUser.getString(Configs.USER_ABOUTME));
        } else { aboutMeTxt.setText("N/D"); }
    }





    // MARK: - QUERY MY RECIPES ------------------------------------------------------------------
    void queryMyRecipes() {
        likedRecipesGridView.setVisibility(View.INVISIBLE);
        myRecipesListView.setVisibility(View.VISIBLE);
        myRecipesRB.setChecked(true);
        likedRecipesRB.setChecked(false);


        pd.setMessage("Please wait...");
        pd.show();

        ParseUser currUser = ParseUser.getCurrentUser();

        ParseQuery query = new ParseQuery(Configs.RECIPES_CLASS_NAME);
        query.whereEqualTo(Configs.RECIPES_USER_POINTER, currUser);
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    myRecipesArray = objects;
                    pd.dismiss();


                    // CUSTOM LIST ADAPTER
                    class ListAdapter extends BaseAdapter {
                        private Context context;
                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }

                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                cell = inflater.inflate(R.layout.cell_my_recipe, null);
                            }

                            // Get Parse object
                            ParseObject rObj = myRecipesArray.get(position);

                            // Get name
                            TextView nameTxt = (TextView) cell.findViewById(R.id.cmrRecNameTxt);
                            nameTxt.setText(rObj.getString(Configs.RECIPES_TITLE));

                            // Get Cover Image
                            final ImageView anImage = (ImageView) cell.findViewById(R.id.cmrRecipeImg);
                            ParseFile fileObject = rObj.getParseFile(Configs.RECIPES_COVER);
                            if (fileObject != null) {
                            fileObject.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, ParseException error) {
                                    if (error == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        if (bmp != null) {
                                            anImage.setImageBitmap(bmp);
                            }}}});}


                            return cell;
                        }

                        @Override public int getCount() { return myRecipesArray.size(); }
                        @Override public Object getItem(int position) { return myRecipesArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Init ListView and set its Adapter
                    myRecipesListView.setAdapter(new ListAdapter(Account.this, myRecipesArray));
                    myRecipesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            ParseObject rObj = myRecipesArray.get(position);
                            Intent i = new Intent(Account.this, AddEditRecipe.class);
                            Bundle extras = new Bundle();
                            extras.putString("objectID", rObj.getObjectId());
                            i.putExtras(extras);
                            startActivity(i);
                    }});


                // Error in query
                } else {
                    Configs.simpleAlert(error.getMessage(), Account.this);
                    pd.dismiss();
        }}});

    }






    // MARK: - QUERY LIKED RECIPES ----------------------------------------------------------------
    void queryLikedRecipes() {
        likedRecipesGridView.setVisibility(View.VISIBLE);
        myRecipesListView.setVisibility(View.INVISIBLE);

        pd.setMessage("Please wait...");
        pd.show();

        ParseUser currUser = ParseUser.getCurrentUser();

        ParseQuery query = ParseQuery.getQuery(Configs.LIKES_CLASS_NAME);
        query.whereEqualTo(Configs.LIKES_LIKED_BY, currUser);
        query.include(Configs.RECIPES_CLASS_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    likedRecipesArray= objects;
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
                            final ParseObject likesObj = likedRecipesArray.get(position);

                            // Get recipePointer
                            likesObj.getParseObject(Configs.LIKES_RECIPE_LIKED).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(final ParseObject recipePointer, ParseException e) {


                                    // THIS RECIPE HAS NOT BEEN REPORTED
                                    if (!recipePointer.getBoolean(Configs.RECIPES_IS_REPORTED)) {

                                        // Get userPointer
                                        recipePointer.getParseObject(Configs.RECIPES_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                            public void done(final ParseObject userPointer, ParseException e) {
                                                if (e == null) {

                                                    // Get Title
                                                    TextView titleTxt = (TextView) finalCell.findViewById(R.id.crRecipeTitletxt);
                                                    titleTxt.setText(recipePointer.getString(Configs.RECIPES_TITLE));

                                                    // Get Category
                                                    TextView catTxt = (TextView) finalCell.findViewById(R.id.crCategoryTxt);
                                                    catTxt.setText(recipePointer.getString(Configs.RECIPES_CATEGORY));

                                                    // Get Likes
                                                    final TextView likesTxt = (TextView) finalCell.findViewById(R.id.crLikesTxt);
                                                    if (recipePointer.getNumber(Configs.RECIPES_LIKES) != null) {
                                                        likesTxt.setText(String.valueOf(recipePointer.getNumber(Configs.RECIPES_LIKES)));
                                                    } else {
                                                        likesTxt.setText("0");
                                                    }


                                                    // Get Cover Image
                                                    final ImageView coverImg = (ImageView) finalCell.findViewById(R.id.crRecipeImg);
                                                    ParseFile fileObject = recipePointer.getParseFile(Configs.RECIPES_COVER);
                                                    if (fileObject != null) {
                                                        fileObject.getDataInBackground(new GetDataCallback() {
                                                            public void done(byte[] data, ParseException error) {
                                                                if (error == null) {
                                                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                                    if (bmp != null) {
                                                                        coverImg.setImageBitmap(bmp);
                                                    }}}
                                                        });}


                                                    // MARK: - TAP COVER IMAGE TO SEE RECIPE'S DETAILS ----------------------
                                                    coverImg.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Configs.categoryStr = "";

                                                            Intent i = new Intent(Account.this, RecipeDetails.class);
                                                            Bundle extras = new Bundle();
                                                            extras.putString("objectID", recipePointer.getObjectId());
                                                            i.putExtras(extras);
                                                            startActivity(i);
                                                    }});


                                                    // Get Avatar
                                                    final ImageView avatarImage = (ImageView) finalCell.findViewById(R.id.crAvatarImg);
                                                    ParseFile fileObject2 = userPointer.getParseFile(Configs.USER_AVATAR);
                                                    if (fileObject2 != null) {
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


                                                    // MARK: - TAP AVATAR -> GO TO USER'S PROFILE ------------------------------------
                                                    avatarImage.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            if (!userPointer.getBoolean(Configs.USER_IS_REPORTED)) {
                                                                Intent i = new Intent(Account.this, OtherUserProfile.class);
                                                                Bundle extras = new Bundle();
                                                                extras.putString("userID", userPointer.getObjectId());
                                                                i.putExtras(extras);
                                                                startActivity(i);
                                                            } else {
                                                                Configs.simpleAlert("This User has been reported!", Account.this);
                                                    }}});




                                                    // MARK: - UNLIKE BUTTON ------------------------------------
                                                    Button unlikeButt = (Button) finalCell.findViewById(R.id.crLikeButt);
                                                    unlikeButt.setBackgroundResource(R.drawable.liked_butt);
                                                    unlikeButt.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {

                                                            final ParseUser currUser = ParseUser.getCurrentUser();

                                                            recipePointer.increment(Configs.RECIPES_LIKES, -1);
                                                            int likeInt = (int)recipePointer.getNumber(Configs.RECIPES_LIKES);
                                                            likesTxt.setText(Configs.roundThousandsIntoK(recipePointer.getInt(Configs.RECIPES_LIKES)));
                                                            recipePointer.saveInBackground();

                                                            likesObj.deleteInBackground(new DeleteCallback() {
                                                                @Override
                                                                public void done(ParseException e) {
                                                                    if (e == null) {
                                                                        Configs.simpleAlert("You've unliked this recipe", Account.this);
                                                                        likedRecipesArray.remove(position);
                                                                        likedRecipesGridView.invalidateViews();
                                                                        likedRecipesGridView.refreshDrawableState();
                                                            }}});

                                                    }});// end unlikeButt

                                            }}});// end userPointer




                                    // MARK: - THIS RECIPE HAS BEEN REPORTED!
                                    } else {
                                        TextView titleTxt = (TextView) finalCell.findViewById(R.id.crRecipeTitletxt);
                                        titleTxt.setText("REPORTED!");
                                        TextView catTxt = (TextView) finalCell.findViewById(R.id.crCategoryTxt);
                                        catTxt.setText("");
                                        TextView fnTxt = (TextView) finalCell.findViewById(R.id.crFullnameTxt);
                                        fnTxt.setText("");
                                        TextView likesTxt = (TextView) finalCell.findViewById(R.id.crLikesTxt);
                                        likesTxt.setText("N/A");
                                        ImageView coverImg = (ImageView) finalCell.findViewById(R.id.crRecipeImg);
                                        coverImg.setImageBitmap(null);
                                        coverImg.setBackgroundColor(Color.parseColor("#555555"));
                                        ImageView avatarImg = (ImageView) finalCell.findViewById(R.id.crAvatarImg);
                                        avatarImg.setImageResource(R.drawable.logo);
                                        avatarImg.setEnabled(false);
                                        Button unlikeButt = (Button) finalCell.findViewById(R.id.crLikeButt);
                                        unlikeButt.setEnabled(false);
                                    }


                                }}); // end recipePointer


                            return cell;
                        }

                        @Override public int getCount() { return likedRecipesArray.size(); }
                        @Override public Object getItem(int position) { return likedRecipesArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }


                    // Init GridView and set its cell's width
                    GridView aGrid = (GridView) findViewById(R.id.accLikedRecipesGridView);
                    aGrid.setAdapter(new GridAdapter(Account.this, likedRecipesArray));

                    // Set number of Columns accordingly to the device used
                    float scalefactor = getResources().getDisplayMetrics().density * 150; // 150 is the cell's width
                    int number = getWindowManager().getDefaultDisplay().getWidth();
                    int columns = (int) ((float) number / (float) scalefactor);
                    aGrid.setNumColumns(columns);



                // error in query
                } else {
                    Configs.simpleAlert(error.getMessage(), Account.this);
                    pd.dismiss();
        }}});

    }






}// @end
