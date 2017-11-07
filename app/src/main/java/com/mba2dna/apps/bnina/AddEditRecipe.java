package com.mba2dna.apps.bnina;

/*-----------------------------------

    - Recipes -

    created by FV iMAGINATION © 2017
    All Rights reserved

-----------------------------------*/

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddEditRecipe extends AppCompatActivity {

    /* Views */
    TextView topbarTitleTxt;
    EditText titleTxt, storyTxt, cookingTxt, bakingTxt, restingTxt, youtubeTxt, videoTitleTxt, ingredientsTxt, preparationTxt;
    ImageView coverImage;
    ListView categoriesListView;
    Button easyButt, mediumButt, hardButt, deleteButt;
    ProgressDialog pd;



    /* Variables */
    ParseObject recipeObj;
    List<ParseObject>likesArray;
    String selectedCategory = "";
    String difficultyStr = "";
    MarshMallowPermission mmp = new MarshMallowPermission(this);


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_recipe);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);
        pd.setIcon(R.drawable.logo);



        // Init views
        topbarTitleTxt = (TextView)findViewById(R.id.aerTopBarTitleTxt);
        topbarTitleTxt.setTypeface(Configs.typeWriter);
        titleTxt = (EditText)findViewById(R.id.aerTitleTxt);
        storyTxt = (EditText)findViewById(R.id.aerStoryTxt);
        cookingTxt = (EditText)findViewById(R.id.aerCookingTxt);
        bakingTxt = (EditText)findViewById(R.id.aerBakingTxt);
        restingTxt = (EditText)findViewById(R.id.aerRestingTxt);
        youtubeTxt = (EditText)findViewById(R.id.aerYoutubeTxt);
        videoTitleTxt = (EditText)findViewById(R.id.aerVideoTitleTxt);
        ingredientsTxt = (EditText)findViewById(R.id.aerIngredientsTxt);
        preparationTxt = (EditText)findViewById(R.id.aerPreparationTxt);
        categoriesListView = (ListView)findViewById(R.id.aerCategoriesListView);

        easyButt = (Button)findViewById(R.id.aerEasyButt);
        mediumButt = (Button)findViewById(R.id.aerMediumButt);
        hardButt = (Button)findViewById(R.id.aerHardButt);
        deleteButt = (Button)findViewById(R.id.aerDeleteButt);

        coverImage = (ImageView)findViewById(R.id.aerCoverImage);


        // Init the Categories ListView
        initCategoriesListView();


        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();

        // EDIT A RECIPE ---------
        if (extras != null) {
            String objectID = extras.getString("objectID");
            recipeObj = ParseObject.createWithoutData(Configs.RECIPES_CLASS_NAME, objectID);
            try { recipeObj.fetchIfNeeded().getParseObject(Configs.RECIPES_CLASS_NAME);

                topbarTitleTxt.setText("EDIT RECIPE");
                deleteButt.setVisibility(View.VISIBLE);

                // Call query
                showRecipeDetails();

            } catch (com.parse.ParseException e) { e.printStackTrace(); }


        // THIS IS A NEW RECIPE -------
        } else {
            recipeObj = new ParseObject(Configs.RECIPES_CLASS_NAME);
            topbarTitleTxt.setText("ADD RECIPE");
            deleteButt.setVisibility(View.INVISIBLE);
        }






        // MARK: - EASY BUTTON ------------------------------------------------------
        easyButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easyButt.setBackgroundResource(R.color.yellow_color);
                mediumButt.setBackgroundResource(R.color.gray_color);
                hardButt.setBackgroundResource(R.color.gray_color);
                difficultyStr = easyButt.getText().toString();
        }});

        // MARK: - MEDIUM BUTTON ----------------------------------------------------
        mediumButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediumButt.setBackgroundResource(R.color.yellow_color);
                easyButt.setBackgroundResource(R.color.gray_color);
                hardButt.setBackgroundResource(R.color.gray_color);
                difficultyStr = mediumButt.getText().toString();
        }});

        // MARK: - HARD BUTTON ----------------------------------------------------
        hardButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hardButt.setBackgroundResource(R.color.yellow_color);
                mediumButt.setBackgroundResource(R.color.gray_color);
                easyButt.setBackgroundResource(R.color.gray_color);
                difficultyStr = hardButt.getText().toString();
        }});







        // MARK: - CHANGE THE COVER IMAGE ---------------------------------------------------
        coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert  = new AlertDialog.Builder(AddEditRecipe.this);
                alert.setTitle("SELECT SOURCE")
                        .setIcon(R.drawable.logo)
                        .setItems(new CharSequence[] {
                                        "Take a picture",
                                        "Pick from Gallery" },
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            // Open Camera
                                            case 0:
                                                if (!mmp.checkPermissionForCamera()) {
                                                    mmp.requestPermissionForCamera();
                                                } else {
                                                    openCamera();
                                                }
                                                break;

                                            // Open Gallery
                                            case 1:
                                                if (!mmp.checkPermissionForReadExternalStorage()) {
                                                    mmp.requestPermissionForReadExternalStorage();
                                                } else {
                                                    openGallery();
                                                }
                                                break;
                                }}})
                        .setNegativeButton("Cancel", null);
                alert.create().show();
        }});








        // MARK: - SUBMIT RECIPE BUTTON ----------------------------------------------------
        Button submitButt = (Button)findViewById(R.id.aerSubmitButt);
        submitButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              ParseUser currUser = ParseUser.getCurrentUser();

              // YOU MUST FILL THE FIELDS TO SUBMIT THE RECIPE ----------------
              if (    titleTxt.getText().toString().matches("") ||
                      selectedCategory.matches("") ||
                      storyTxt.getText().toString().matches("") ||
                      difficultyStr.matches("") ||
                      cookingTxt.getText().toString().matches("") ||
                      bakingTxt.getText().toString().matches("") ||
                      restingTxt.getText().toString().matches("") ||
                      ingredientsTxt.getText().toString().matches("") ||
                      preparationTxt.getText().toString().matches("") ||
                      coverImage.getDrawable() == null
                  ) {
                    Configs.simpleAlert("You must fill the fields and add a cover image!", AddEditRecipe.this);



              // YOU CAN SAVE AND SUBMIT YOUR RECIPE ----------------
              } else {

                  pd.setMessage("Please wait...");
                  pd.show();
                  dismissKeyboard();

                  recipeObj.put(Configs.RECIPES_USER_POINTER, currUser);
                  recipeObj.put(Configs.RECIPES_TITLE, titleTxt.getText().toString());
                  recipeObj.put(Configs.RECIPES_TITLE_LOWERCASE, titleTxt.getText().toString().toLowerCase());
                  // Add keywords
                  List<String> keywords = new ArrayList<String>();
                  String[] one = titleTxt.getText().toString().toLowerCase().split(" ");
                  for (String keyw : one) {
                      keywords.add(keyw);
                  }
                  recipeObj.put(Configs.RECIPES_KEYWORDS, keywords);

                  recipeObj.put(Configs.RECIPES_CATEGORY, selectedCategory);
                  recipeObj.put(Configs.RECIPES_ABOUT, storyTxt.getText().toString());
                  recipeObj.put(Configs.RECIPES_DIFFICULTY, difficultyStr);
                  recipeObj.put(Configs.RECIPES_COOKING, cookingTxt.getText().toString());
                  recipeObj.put(Configs.RECIPES_BAKING, bakingTxt.getText().toString());
                  recipeObj.put(Configs.RECIPES_RESTING, restingTxt.getText().toString());
                  if (!youtubeTxt.getText().toString().matches("")) {
                      recipeObj.put(Configs.RECIPES_YOUTUBE, youtubeTxt.getText().toString());
                  } else {
                      recipeObj.put(Configs.RECIPES_YOUTUBE, "");
                  }
                  if (!videoTitleTxt.getText().toString().matches("")) {
                      recipeObj.put(Configs.RECIPES_VIDEO_TITLE, videoTitleTxt.getText().toString());
                  } else {
                      recipeObj.put(Configs.RECIPES_VIDEO_TITLE, "");
                  }
                  recipeObj.put(Configs.RECIPES_INGREDIENTS, ingredientsTxt.getText().toString());
                  recipeObj.put(Configs.RECIPES_PREPARATION, preparationTxt.getText().toString());
                  recipeObj.put(Configs.RECIPES_IS_REPORTED, false);

                  // Save image
                  Bitmap bitmap = ((BitmapDrawable) coverImage.getDrawable()).getBitmap();
                  ByteArrayOutputStream stream = new ByteArrayOutputStream();
                  bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                  byte[] byteArray = stream.toByteArray();
                  ParseFile imageFile = new ParseFile("cover.jpg", byteArray);
                  recipeObj.put(Configs.RECIPES_COVER, imageFile);


                  // Saving block
                  recipeObj.saveInBackground(new SaveCallback() {
                      @Override
                      public void done(ParseException error) {
                          if (error == null) {
                              pd.dismiss();

                              AlertDialog.Builder alert = new AlertDialog.Builder(AddEditRecipe.this);
                              alert.setMessage("You've successfully submitted your recipe!")
                                      .setTitle(R.string.app_name)
                                      .setIcon(R.drawable.logo)
                                      .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                          @Override
                                          public void onClick(DialogInterface dialog, int which) {
                                              finish();
                                          }
                                      });
                              alert.create().show();

                          // error on saving
                          } else {
                              Configs.simpleAlert(error.getMessage(), AddEditRecipe.this);
                              pd.dismiss();
                  }}});

              }

         }});




        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = (Button)findViewById(R.id.aerBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {  finish(); }});



        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()








    // MARK: - SHOW RECIPE DETAILS ------------------------------------------------------------
    void showRecipeDetails() {
        titleTxt.setText(recipeObj.getString(Configs.RECIPES_TITLE));
        storyTxt.setText(recipeObj.getString(Configs.RECIPES_ABOUT));

        if (recipeObj.getString(Configs.RECIPES_YOUTUBE) != null) { youtubeTxt.setText(recipeObj.getString(Configs.RECIPES_YOUTUBE));
        } else { youtubeTxt.setText(""); }

        if (recipeObj.getString(Configs.RECIPES_VIDEO_TITLE) != null) { videoTitleTxt.setText(recipeObj.getString(Configs.RECIPES_VIDEO_TITLE));
        } else { videoTitleTxt.setText(""); }

        cookingTxt.setText(recipeObj.getString(Configs.RECIPES_COOKING));
        bakingTxt.setText(recipeObj.getString(Configs.RECIPES_BAKING));
        restingTxt.setText(recipeObj.getString(Configs.RECIPES_RESTING));
        ingredientsTxt.setText(recipeObj.getString(Configs.RECIPES_INGREDIENTS));
        preparationTxt.setText(recipeObj.getString(Configs.RECIPES_PREPARATION));


        // Get category
        selectedCategory = recipeObj.getString(Configs.RECIPES_CATEGORY);
        Log.i("log-", "CATEGORY (EDIT RECIPE): " + selectedCategory);


        // Set difficulty button
        difficultyStr = recipeObj.getString(Configs.RECIPES_DIFFICULTY);
        if (difficultyStr.matches("easy")) {
            easyButt.setBackgroundResource(R.color.yellow_color);
        } else if (difficultyStr.matches("medium")) {
            mediumButt.setBackgroundResource(R.color.yellow_color);
        } else if (difficultyStr.matches("hard")) {
            hardButt.setBackgroundResource(R.color.yellow_color);
        }


        // Get image
        ParseFile fileObject = recipeObj.getParseFile(Configs.RECIPES_COVER);
        if (fileObject != null ) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            coverImage.setImageBitmap(bmp);
        }}}});}







        // MARK: - DELETE RECIPE BUTTON ---------------------------------------------------
        Button deleteButt = (Button)findViewById(R.id.aerDeleteButt);
        deleteButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

              AlertDialog.Builder alert = new AlertDialog.Builder(AddEditRecipe.this);
              alert.setMessage("Are you sure you want to delete this recipe?")
                  .setTitle(R.string.app_name)
                  .setPositiveButton("delete", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {

                          // FIRST, DELETE ALL LIKES
                          ParseQuery query = new ParseQuery(Configs.LIKES_CLASS_NAME);
                          query.whereEqualTo(Configs.LIKES_RECIPE_LIKED, recipeObj);
                          query.findInBackground(new FindCallback<ParseObject>() {
                              @Override
                              public void done(List<ParseObject> objects, ParseException e) {
                                  if (e == null) {

                                      likesArray = objects;
                                      if (likesArray.size() > 0) {
                                          for (int i = 0; i<likesArray.size(); i++) {
                                              ParseObject likeObj = likesArray.get(i);
                                              likeObj.deleteInBackground();
                                          }
                                      }

                                      // THEN, DELETE THE RECIPE
                                      recipeObj.deleteInBackground(new DeleteCallback() {
                                          @Override
                                          public void done(ParseException e) {

                                              AlertDialog.Builder alert = new AlertDialog.Builder(AddEditRecipe.this);
                                              alert.setMessage("Your recipe has been deleted!")
                                                      .setTitle(R.string.app_name)
                                                      .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                          @Override
                                                          public void onClick(DialogInterface dialog, int which) {
                                                              finish();
                                                          }})
                                                      .setIcon(R.drawable.logo);
                                              alert.create().show();
                                      }});


                                  // error in query
                                  } else {
                                      Configs.simpleAlert(e.getMessage(), AddEditRecipe.this);
                          }}});

                  }})
                  .setNegativeButton("Cancel", null)
                  .setIcon(R.drawable.logo);
              alert.create().show();

         }});// end deleteButt

    }









    // INIT THE CATEGORIES LISTVIEW ---------------------------------------------------------------
    void initCategoriesListView() {
        final List<String>catArray = new ArrayList<String>(Arrays.asList(Configs.categoriesArray));

        class ListAdapter extends BaseAdapter {
            private Context context;

            public ListAdapter(Context context, List<String> categories) {
                super();
                this.context = context;
            }

            // CONFIGURE CELL
            @Override
            public View getView(int position, View cell, ViewGroup parent) {
                if (cell == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    cell = inflater.inflate(R.layout.cell_category2, null);
                }

                // Get name
                TextView nametxt = (TextView)cell.findViewById(R.id.ccat2NameTxt);
                nametxt.setText(catArray.get(position));

                // Set category by highlighting the cell
                if (!selectedCategory.matches("")) {
                    String catName = catArray.get(position);
                    if (selectedCategory.matches(catName)) {
                        cell.setBackgroundResource(R.color.dark_white);
                    }
                }

                return cell;
            }

            @Override public int getCount() { return catArray.size(); }
            @Override public Object getItem(int position) { return catArray.get(position); }
            @Override public long getItemId(int position) { return position; }
        }


        // Init ListView and set its adapter
        categoriesListView.setAdapter(new ListAdapter(AddEditRecipe.this, catArray));
        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                selectedCategory = catArray.get(position);
                Log.i("log-", "SELECTED CATEGORY BY TAP ON LISTVIEW: " + selectedCategory);
        }});
    }









    // IMAGE HANDLING METHODS ------------------------------------------------------------------------
    int CAMERA = 0;
    int GALLERY = 1;
    Uri imageURI;


    // OPEN CAMERA
    public void openCamera() {
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        imageURI = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        startActivityForResult(intent, CAMERA);
    }

    // OPEN GALLERY
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY);
    }



    // IMAGE PICKED DELEGATE -----------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bitmap bm = null;

            // Image from Camera
            if (requestCode == CAMERA) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageURI);

                    String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
                    Cursor cur = managedQuery(imageURI, orientationColumn, null, null, null);
                    int orientation = -1;
                    if (cur != null && cur.moveToFirst()) {
                        orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
                    }
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);

                }  catch (IOException e) { e.printStackTrace(); }


            // Image from Gallery
            } else if (requestCode == GALLERY) {
                try { bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) { e.printStackTrace(); }
            }


            // Set image
            Bitmap scaledBm = Configs.scaleBitmapToMaxSize(500, bm);
            coverImage.setImageBitmap(scaledBm);
        }
    }
    //---------------------------------------------------------------------------------------------







    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(titleTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(storyTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(cookingTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(bakingTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(restingTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(youtubeTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(videoTitleTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(ingredientsTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(preparationTxt.getWindowToken(), 0);
    }


}//@end
