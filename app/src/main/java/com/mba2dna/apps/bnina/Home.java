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
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.appevents.AppEventsConstants;
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
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity {

    /* Views */
    LinearLayout searchLayout;
    EditText searchTxt;
    ProgressDialog pd;


    /* Variables */
    List<ParseObject> recipesArray;
    List<ParseObject> likesArray;


    // ON START() ----------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        if (!Configs.categoryStr.matches("")) {
            searchTxt.setText("");
            queryRecipes("");
        }
        Log.i("log-", "CATEGORY: " + Configs.categoryStr);


        if (ParseUser.getCurrentUser().getUsername() != null) {
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();

            // IMPORTANT: Replace "676980399521" with your own GCM Sender Id. -->
            installation.put("GCMSenderId", "676980399521");

            installation.put("userID", ParseUser.getCurrentUser().getObjectId());
            installation.put("username", ParseUser.getCurrentUser().getUsername());
            installation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.i("log-", "REGISTERED FOR PUSH NOTIFICATIONS!");
                }
            });
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // ON CREATE() ----------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // Hide ActionBar
        getSupportActionBar().hide();
        Window window = Home.this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(Home.this, R.color.colorPrimaryDark));

        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);
        pd.setIcon(R.drawable.logo);


        // Init views
        TextView titleTxt = (TextView) findViewById(R.id.hTitleTxt);
        titleTxt.setTypeface(Configs.typeWriter);
        searchTxt = (EditText) findViewById(R.id.hSearchTxt);
        searchLayout = (LinearLayout) findViewById(R.id.hSearchLayout);


        // Hide searchLayout
        hideSearchLayout();


        // Init TabBar buttons
        Button tab_one = (Button) findViewById(R.id.tab_two);
        Button tab_two = (Button) findViewById(R.id.tab_three);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Shopping.class));
            }
        });

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Account.class));
            }
        });


        // Call query for ALL bnina
        queryRecipes("");


        // MARK: - SEARCH RECIPES BY KEYWORDS --------------------------------------------------
        searchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    Configs.categoryStr = "";
                    queryRecipes(searchTxt.getText().toString().toLowerCase());
                    hideSearchLayout();
                    dismissKeyboard();

                    return true;
                }
                return false;
            }
        });


        // MARK: - CHOOSE CATEGORIES BUTTON ------------------------------------
        Button chooseCatButt = (Button) findViewById(R.id.hFilterButt);
        chooseCatButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, Categories.class));
            }
        });


        // MARK: - SEARCH BUTTON ------------------------------------
        Button sButt = (Button) findViewById(R.id.hSearchButt);
        sButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchLayout();
            }
        });


        // MARK: - CANCEL SEARCH BUTTON ------------------------------------
        Button csButt = (Button) findViewById(R.id.hCancelButt);
        csButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTxt.setText("");
                hideSearchLayout();
                dismissKeyboard();
            }
        });


        // MARK: - REFRESH BUTTON ------------------------------------
        Button rButt = (Button) findViewById(R.id.hRefreshButt);
        rButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTxt.setText("");
                Configs.categoryStr = "";
                hideSearchLayout();
                dismissKeyboard();

                // Recall query
                queryRecipes("");
            }
        });


        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }// end onCreate()


    // MARK: - QUERY RECIPES ------------------------------------------------------------------
    void queryRecipes(String searchText) {
        pd.setMessage("Please wait...");
        pd.show();


        ParseQuery query = new ParseQuery(Configs.RECIPES_CLASS_NAME);

        if (!searchText.matches("")) {
            List<String> keywords = new ArrayList<String>();
            String[] one = searchTxt.getText().toString().toLowerCase().split(" ");
            for (String keyw : one) {
                keywords.add(keyw);
            }
            Log.d("KEYWORDS", "\n" + keywords + "\n");
            query.whereContainedIn(Configs.RECIPES_KEYWORDS, keywords);
        }

        if (!Configs.categoryStr.matches("")) {
            query.whereEqualTo(Configs.RECIPES_CATEGORY, Configs.categoryStr);
        }

        query.whereEqualTo(Configs.RECIPES_IS_REPORTED, false);

        Log.i("log-", "SEARCH TEXT: " + searchText);

        query.orderByDescending(Configs.RECIPES_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    recipesArray = objects;
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
                            final ParseObject rObj = recipesArray.get(position);

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
                                    if (rObj.getNumber(Configs.RECIPES_LIKES) != null) {
                                        likesTxt.setText(Configs.roundThousandsIntoK(rObj.getNumber(Configs.RECIPES_LIKES)));
                                    } else {
                                        likesTxt.setText("0");
                                    }

                                    TextView commTxt = (TextView) finalCell.findViewById(R.id.crCommentsTxt);
                                    if (rObj.getNumber(Configs.RECIPES_COMMENTS) != null) {
                                        commTxt.setText(Configs.roundThousandsIntoK(rObj.getInt(Configs.RECIPES_COMMENTS)));
                                    } else {
                                        commTxt.setText(AppEventsConstants.EVENT_PARAM_VALUE_NO);
                                    }
                                    // Get Cover Image
                                    final ImageView coverImg = (ImageView) finalCell.findViewById(R.id.crRecipeImg);
                                    ParseFile fileObject = rObj.getParseFile(Configs.RECIPES_COVER);
                                    if (fileObject != null) {
                                        fileObject.getDataInBackground(new GetDataCallback() {
                                            public void done(byte[] data, ParseException error) {
                                                if (error == null) {
                                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                    if (bmp != null) {
                                                        coverImg.setImageBitmap(bmp);
                                                    }
                                                }
                                            }
                                        });
                                    }


                                    // MARK: - TAP COVER IMAGE TO SEE RECIPE'S DETAILS ----------------------
                                    coverImg.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Configs.categoryStr = "";

                                            ParseObject rObj = recipesArray.get(position);
                                            Intent i = new Intent(Home.this, RecipeDetails.class);
                                            Bundle extras = new Bundle();
                                            extras.putString("objectID", rObj.getObjectId());
                                            i.putExtras(extras);
                                            startActivity(i);
                                        }
                                    });


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
                                                    }
                                                }
                                            }
                                        });
                                    }

                                    // Get Fullname
                                    TextView fnTxt = (TextView) finalCell.findViewById(R.id.crFullnameTxt);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        fnTxt.setText(Html.fromHtml(userPointer.getString(Configs.USER_FULLNAME) + " is making <b><font color='#ed3851'>" + rObj.getString(Configs.RECIPES_TITLE) + "</font></b> in his house", 1));
                                    } else {
                                        fnTxt.setText(Html.fromHtml(userPointer.getString(Configs.USER_FULLNAME) + " is making <b><font color='#ed3851'>" + rObj.getString(Configs.RECIPES_TITLE) + "</font></b> in his house"));
                                    }


                                    // MARK: - TAP AVATAR -> GO TO USER'S PROFILE ------------------------------------
                                    avatarImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (!userPointer.getBoolean(Configs.USER_IS_REPORTED)) {
                                                Intent i = new Intent(Home.this, OtherUserProfile.class);
                                                Bundle extras = new Bundle();
                                                extras.putString("userID", userPointer.getObjectId());
                                                i.putExtras(extras);
                                                startActivity(i);
                                            } else {
                                                Configs.simpleAlert("This User has been reported!", Home.this);
                                            }
                                        }
                                    });


                                    // MARK: - LIKE BUTTON ------------------------------------
                                    Button likeButt = (Button) finalCell.findViewById(R.id.crLikeButt);
                                    likeButt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            final ParseUser currUser = ParseUser.getCurrentUser();

                                            // USER IS LOGGED IN
                                            if (currUser.getUsername() != null) {

                                                // Query Likes
                                                ParseQuery query = new ParseQuery(Configs.LIKES_CLASS_NAME);
                                                query.whereEqualTo(Configs.LIKES_LIKED_BY, currUser);
                                                query.whereEqualTo(Configs.LIKES_RECIPE_LIKED, rObj);
                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    public void done(List<ParseObject> objects, ParseException error) {
                                                        if (error == null) {
                                                            likesArray = objects;

                                                            ParseObject likesObj = new ParseObject(Configs.LIKES_CLASS_NAME);

                                                            if (likesArray.size() == 0) {
                                                                // LIKE RECIPE -----------
                                                                rObj.increment(Configs.RECIPES_LIKES, 1);
                                                                rObj.saveInBackground();
                                                                likesTxt.setText(Configs.roundThousandsIntoK(rObj.getInt(Configs.RECIPES_LIKES)));
                                                                rObj.saveInBackground();

                                                                likesObj.put(Configs.LIKES_LIKED_BY, currUser);
                                                                likesObj.put(Configs.LIKES_RECIPE_LIKED, rObj);
                                                                likesObj.saveInBackground(new SaveCallback() {
                                                                    @Override
                                                                    public void done(ParseException e) {
                                                                        if (e == null) {
                                                                            Configs.simpleAlert("You've liked this recipe and saved into your Account!", Home.this);

                                                                            // Send push notification
                                                                            final String pushMessage = currUser.getString(Configs.USER_FULLNAME) + " liked your recipe: " + rObj.getString(Configs.RECIPES_TITLE);

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
                                                                                        Configs.simpleAlert(e.getMessage(), Home.this);
                                                                                        pd.dismiss();
                                                                                    }
                                                                                }
                                                                            });

                                                                            // Save activity
                                                                            ParseObject actObj = new ParseObject(Configs.ACTIVITY_CLASS_NAME);
                                                                            actObj.put(Configs.ACTIVITY_CURRENT_USER, userPointer);
                                                                            actObj.put(Configs.ACTIVITY_OTHER_USER, currUser);
                                                                            actObj.put(Configs.ACTIVITY_TEXT, pushMessage);
                                                                            actObj.saveInBackground();
                                                                        }
                                                                    }
                                                                });


                                                                // UNLIKE RECIPE --------------------------
                                                            } else if (likesArray.size() > 0) {
                                                                rObj.increment(Configs.RECIPES_LIKES, -1);
                                                                int likeInt = (int) rObj.getNumber(Configs.RECIPES_LIKES);
                                                                likesTxt.setText(String.valueOf(likeInt));
                                                                rObj.saveInBackground();

                                                                likesObj = likesArray.get(0);
                                                                likesObj.deleteInBackground(new DeleteCallback() {
                                                                    @Override
                                                                    public void done(ParseException e) {
                                                                        if (e == null) {
                                                                            Configs.simpleAlert("You've unliked this recipe", Home.this);
                                                                        }
                                                                    }
                                                                });
                                                            }


                                                            // Error in query Likes
                                                        } else {
                                                            Configs.simpleAlert(error.getMessage(), Home.this);
                                                        }
                                                    }
                                                });


                                                // USER IS NOT LOGGED IN/REGISTERED
                                            } else {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
                                                alert.setMessage("You must login/sign up to like a recipe!")
                                                        .setTitle(R.string.app_name)
                                                        .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                startActivity(new Intent(Home.this, Login.class));
                                                            }
                                                        })
                                                        .setNegativeButton("Cancel", null)
                                                        .setIcon(R.drawable.logo);
                                                alert.create().show();
                                            }

                                        }
                                    }); // end likeButt


                                }
                            }); // end userPointer


                            return cell;
                        }

                        @Override
                        public int getCount() {
                            return recipesArray.size();
                        }

                        @Override
                        public Object getItem(int position) {
                            return recipesArray.get(position);
                        }

                        @Override
                        public long getItemId(int position) {
                            return position;
                        }
                    }


                    // Init GridView and set its cell's width
                    GridView aGrid = (GridView) findViewById(R.id.hRecipesGridView);
                    aGrid.setAdapter(new GridAdapter(Home.this, recipesArray));

                    // Set number of Columns accordingly to the device used
                    float scalefactor = getResources().getDisplayMetrics().density * 150; // 150 is the cell's width
                    int number = getWindowManager().getDefaultDisplay().getWidth();
                    int columns = (int) ((float) number / (float) scalefactor);
                    aGrid.setNumColumns(1);//columns


                    // error in query
                } else {
                    Configs.simpleAlert(error.getMessage(), Home.this);
                    pd.dismiss();
                }
            }
        });

    }


    // MARK: - HIDE/SHOW SEARCH LAYOUT ------------------------------------------------------
    void hideSearchLayout() {
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(searchLayout.getLayoutParams());
        marginParams.setMargins(0, -200, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        searchLayout.setLayoutParams(layoutParams);
    }

    void showSearchLayout() {
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(searchLayout.getLayoutParams());
        marginParams.setMargins(0, 100, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        searchLayout.setLayoutParams(layoutParams);
    }


    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);
    }

}//@end
