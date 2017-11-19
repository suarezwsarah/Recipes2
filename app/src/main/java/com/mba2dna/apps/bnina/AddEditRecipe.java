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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddEditRecipe extends AppCompatActivity {

    /* Views */
    private TextView topbarTitleTxt;
    private EditText titleTxt;
    private EditText storyTxt;
    private EditText cookingTxt;
    private EditText bakingTxt;
    private EditText restingTxt;
    private EditText youtubeTxt;
    private EditText videoTitleTxt;
    private EditText ingredientsTxt;
    private EditText preparationTxt;
    private ImageView coverImage;
    private ListView categoriesListView;
    private Button easyButt;
    private Button mediumButt;
    private Button hardButt;
    private Button deleteButt;
    private ProgressDialog pd;


    /* Variables */
    private ParseObject recipeObj;
    private List<ParseObject> likesArray;
    private String selectedCategory = "";
    private String difficultyStr = "";
    private final MarshMallowPermission mmp = new MarshMallowPermission(this);


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
        Window window = this.getWindow();
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);
        pd.setIcon(R.drawable.splashlogo);


        // Init views
        topbarTitleTxt = (TextView) findViewById(R.id.aerTopBarTitleTxt);
        // topbarTitleTxt.setTypeface(Configs.typeWriter);
        titleTxt = (EditText) findViewById(R.id.aerTitleTxt);
        storyTxt = (EditText) findViewById(R.id.aerStoryTxt);
        cookingTxt = (EditText) findViewById(R.id.aerCookingTxt);
        bakingTxt = (EditText) findViewById(R.id.aerBakingTxt);
        restingTxt = (EditText) findViewById(R.id.aerRestingTxt);
        youtubeTxt = (EditText) findViewById(R.id.aerYoutubeTxt);
        videoTitleTxt = (EditText) findViewById(R.id.aerVideoTitleTxt);
        ingredientsTxt = (EditText) findViewById(R.id.aerIngredientsTxt);
        preparationTxt = (EditText) findViewById(R.id.aerPreparationTxt);
        categoriesListView = (ListView) findViewById(R.id.aerCategoriesListView);

        easyButt = (Button) findViewById(R.id.aerEasyButt);
        mediumButt = (Button) findViewById(R.id.aerMediumButt);
        hardButt = (Button) findViewById(R.id.aerHardButt);
        deleteButt = (Button) findViewById(R.id.aerDeleteButt);

        coverImage = (ImageView) findViewById(R.id.aerCoverImage);


        // Init the Categories ListView
        initCategoriesListView();


        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();

        // EDIT A RECIPE ---------
        if (extras != null) {
            String objectID = extras.getString("objectID");
            recipeObj = ParseObject.createWithoutData(Configs.RECIPES_CLASS_NAME, objectID);
            try {
                recipeObj.fetchIfNeeded().getParseObject(Configs.RECIPES_CLASS_NAME);

                topbarTitleTxt.setText(" تغيير الوصفة");
                deleteButt.setVisibility(View.VISIBLE);

                // Call query
                showRecipeDetails();

            } catch (com.parse.ParseException e) {
                e.printStackTrace();
            }


            // THIS IS A NEW RECIPE -------
        } else {
            recipeObj = new ParseObject(Configs.RECIPES_CLASS_NAME);
            topbarTitleTxt.setText("إضافة وصفة");
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
            }
        });

        // MARK: - MEDIUM BUTTON ----------------------------------------------------
        mediumButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediumButt.setBackgroundResource(R.color.yellow_color);
                easyButt.setBackgroundResource(R.color.gray_color);
                hardButt.setBackgroundResource(R.color.gray_color);
                difficultyStr = mediumButt.getText().toString();
            }
        });

        // MARK: - HARD BUTTON ----------------------------------------------------
        hardButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hardButt.setBackgroundResource(R.color.yellow_color);
                mediumButt.setBackgroundResource(R.color.gray_color);
                easyButt.setBackgroundResource(R.color.gray_color);
                difficultyStr = hardButt.getText().toString();
            }
        });


        // MARK: - CHANGE THE COVER IMAGE ---------------------------------------------------
        coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddEditRecipe.this);
                alert.setTitle("إختر المصدر")
                        .setIcon(R.drawable.splashlogo)
                        .setItems(new CharSequence[]{
                                        "التقاط صورة",
                                        "اختر من الهاتف"},
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            // Open Camera
                                            case 0:
                                                if (!mmp.checkPermissionForCamera()) {
                                                    mmp.requestPermissionForCamera();
                                                } else if (!mmp.checkPermissionForReadExternalStorage()) {
                                                    mmp.requestPermissionForReadExternalStorage();
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
                                        }
                                    }
                                })
                        .setNegativeButton("إلغاء", null);
                alert.create().show();
            }
        });


        // MARK: - SUBMIT RECIPE BUTTON ----------------------------------------------------
        Button submitButt = (Button) findViewById(R.id.aerSubmitButt);
        submitButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser currUser = ParseUser.getCurrentUser();

                // YOU MUST FILL THE FIELDS TO SUBMIT THE RECIPE ----------------
                if (titleTxt.getText().toString().matches("") ||
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
                    Configs.simpleAlert("يجب ملء الحقول وإضافة صورة الغلاف!", AddEditRecipe.this);


                    // YOU CAN SAVE AND SUBMIT YOUR RECIPE ----------------
                } else {

                    pd.setMessage("يرجى الإنتضار...");
                    pd.show();
                    dismissKeyboard();

                    recipeObj.put(Configs.RECIPES_USER_POINTER, currUser);
                    recipeObj.put(Configs.RECIPES_TITLE, titleTxt.getText().toString());
                    recipeObj.put(Configs.RECIPES_TITLE_LOWERCASE, titleTxt.getText().toString().toLowerCase());
                    // Add keywords
                    List<String> keywords = new ArrayList<>();
                    String[] one = titleTxt.getText().toString().toLowerCase().split(" ");
                    Collections.addAll(keywords, one);
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
                                alert.setMessage("لقد أرسلت الوصفة بنجاح!")
                                        .setTitle(R.string.app_name)
                                        .setIcon(R.drawable.splashlogo)
                                        .setPositiveButton("أوكي", new DialogInterface.OnClickListener() {
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
                            }
                        }
                    });

                }

            }
        });


        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = (Button) findViewById(R.id.aerBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()


    // MARK: - SHOW RECIPE DETAILS ------------------------------------------------------------
    private void showRecipeDetails() {
        titleTxt.setText(recipeObj.getString(Configs.RECIPES_TITLE));
        storyTxt.setText(recipeObj.getString(Configs.RECIPES_ABOUT));

        if (recipeObj.getString(Configs.RECIPES_YOUTUBE) != null) {
            youtubeTxt.setText(recipeObj.getString(Configs.RECIPES_YOUTUBE));
        } else {
            youtubeTxt.setText("");
        }

        if (recipeObj.getString(Configs.RECIPES_VIDEO_TITLE) != null) {
            videoTitleTxt.setText(recipeObj.getString(Configs.RECIPES_VIDEO_TITLE));
        } else {
            videoTitleTxt.setText("");
        }

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
        if (difficultyStr.matches("سهل")) {
            easyButt.setBackgroundResource(R.color.yellow_color);
        } else if (difficultyStr.matches("متوسط")) {
            mediumButt.setBackgroundResource(R.color.yellow_color);
        } else if (difficultyStr.matches("صعب")) {
            hardButt.setBackgroundResource(R.color.yellow_color);
        }


        // Get image
        ParseFile fileObject = recipeObj.getParseFile(Configs.RECIPES_COVER);
        if (fileObject != null) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            coverImage.setImageBitmap(bmp);
                        }
                    }
                }
            });
        }


        // MARK: - DELETE RECIPE BUTTON ---------------------------------------------------
        Button deleteButt = (Button) findViewById(R.id.aerDeleteButt);
        deleteButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(AddEditRecipe.this);
                alert.setMessage("هل تريد بالتأكيد حذف هذه الوصفة؟")
                        .setTitle(R.string.app_name)
                        .setPositiveButton("حذف", new DialogInterface.OnClickListener() {
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
                                                for (int i = 0; i < likesArray.size(); i++) {
                                                    ParseObject likeObj = likesArray.get(i);
                                                    likeObj.deleteInBackground();
                                                }
                                            }

                                            // THEN, DELETE THE RECIPE
                                            recipeObj.deleteInBackground(new DeleteCallback() {
                                                @Override
                                                public void done(ParseException e) {

                                                    AlertDialog.Builder alert = new AlertDialog.Builder(AddEditRecipe.this);
                                                    alert.setMessage("تم حذف الوصفة!")
                                                            .setTitle(R.string.app_name)
                                                            .setPositiveButton("أوكي", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    finish();
                                                                }
                                                            })
                                                            .setIcon(R.drawable.splashlogo);
                                                    alert.create().show();
                                                }
                                            });


                                            // error in query
                                        } else {
                                            Configs.simpleAlert(e.getMessage(), AddEditRecipe.this);
                                        }
                                    }
                                });

                            }
                        })
                        .setNegativeButton("إلغاء", null)
                        .setIcon(R.drawable.splashlogo);
                alert.create().show();

            }
        });// end deleteButt

    }


    // INIT THE CATEGORIES LISTVIEW ---------------------------------------------------------------
    private void initCategoriesListView() {
        final List<String> catArray = new ArrayList<>(Arrays.asList(Configs.categoriesArray));

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
                    assert inflater != null;
                    cell = inflater.inflate(R.layout.cell_category2, null);
                }

                // Get name
                TextView nametxt = (TextView) cell.findViewById(R.id.ccat2NameTxt);
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

            @Override
            public int getCount() {
                return catArray.size();
            }

            @Override
            public Object getItem(int position) {
                return catArray.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }
        }


        // Init ListView and set its adapter
        categoriesListView.setAdapter(new ListAdapter(AddEditRecipe.this, catArray));
        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                selectedCategory = catArray.get(position);
                Log.i("log-", "SELECTED CATEGORY BY TAP ON LISTVIEW: " + selectedCategory);
            }
        });
    }


    // IMAGE HANDLING METHODS ------------------------------------------------------------------------
    private final int CAMERA = 0;
    private final int GALLERY = 1;
    private Uri imageURI;


    // OPEN CAMERA
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        imageURI = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        startActivityForResult(intent, CAMERA);
    }

    // OPEN GALLERY
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "حدد الصورة"), GALLERY);
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

                } catch (IOException e) {
                    e.printStackTrace();
                }


                // Image from Gallery
            } else if (requestCode == GALLERY) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            // Set image
            Bitmap scaledBm = Configs.scaleBitmapToMaxSize(500, bm);
            coverImage.setImageBitmap(scaledBm);
        }
    }
    //---------------------------------------------------------------------------------------------


    // MARK: - DISMISS KEYBOARD
    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
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
