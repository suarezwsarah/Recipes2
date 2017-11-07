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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EditProfile extends AppCompatActivity {

    /* Views */
    ProgressDialog pd;
    EditText fullnameTxt, occupationTxt, aboutTxt;
    TextView emailTxt;



    /* Variables */
    MarshMallowPermission mmp = new MarshMallowPermission(this);


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);
        pd.setIcon(R.drawable.logo);


        // Init views
        TextView titleTxt = (TextView)findViewById(R.id.epTitleTxt);
        titleTxt.setTypeface(Configs.typeWriter);
        fullnameTxt = (EditText) findViewById(R.id.epFullnameTxt);
        occupationTxt = (EditText) findViewById(R.id.epOccupationTxt);
        aboutTxt = (EditText) findViewById(R.id.epAboutTxt);
        emailTxt = (TextView)findViewById(R.id.epEmailTxt);


        // Call query
        showUserDetails();



        // MARK: - BACK BUTTON ------------------------------------
        Button myButt = (Button)findViewById(R.id.epBackButt);
        myButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) { finish(); }});


    }// end onCreate()






    // MARK: - SHOW USER DETAILS ---------------------------------------------------------------
    void showUserDetails() {
        ParseUser currUser = ParseUser.getCurrentUser();

        fullnameTxt.setText(currUser.getString(Configs.USER_FULLNAME));
        if (currUser.getString(Configs.USER_JOB) != null) { occupationTxt.setText(currUser.getString(Configs.USER_JOB));
        } else { occupationTxt.setText(""); }
        if (currUser.getString(Configs.USER_ABOUTME) != null) { aboutTxt.setText(currUser.getString(Configs.USER_ABOUTME));
        } else { aboutTxt.setText(""); }
        emailTxt.setText(currUser.getString(Configs.USER_EMAIL));

        // Get avatar image
        final ImageView avImage = (ImageView)findViewById(R.id.epAvatarImg);
        ParseFile fileObject = currUser.getParseFile(Configs.USER_AVATAR);
        if (fileObject != null ) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            avImage.setImageBitmap(bmp);
        }}}});}



        // CHANGE AVATAR ------------------------------------------------------------
        avImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        AlertDialog.Builder alert  = new AlertDialog.Builder(EditProfile.this);
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




        // MARK: - UPDATE PROFILE BUTTON ------------------------------------
        Button upButt = (Button)findViewById(R.id.epUpdateProfileButt);
        upButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
               ParseUser currUser = ParseUser.getCurrentUser();
              pd.setMessage("Please wait...");
              pd.show();

              if (!fullnameTxt.getText().toString().matches("")) {
                  currUser.put(Configs.USER_FULLNAME, fullnameTxt.getText().toString());

                  if (!occupationTxt.getText().toString().matches("")) {
                      currUser.put(Configs.USER_JOB, occupationTxt.getText().toString());
                  } else{ currUser.put(Configs.USER_JOB, ""); }
                  if (!aboutTxt.getText().toString().matches("")) {
                      currUser.put(Configs.USER_ABOUTME, aboutTxt.getText().toString());
                  } else{ currUser.put(Configs.USER_ABOUTME, ""); }

                  // Save image
                  ImageView avImage = (ImageView) findViewById(R.id.epAvatarImg);
                  Bitmap bitmap = ((BitmapDrawable) avImage.getDrawable()).getBitmap();
                  ByteArrayOutputStream stream = new ByteArrayOutputStream();
                  bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                  byte[] byteArray = stream.toByteArray();
                  ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                  currUser.put(Configs.USER_AVATAR, imageFile);

                  // Saving block
                  currUser.saveInBackground(new SaveCallback() {
                      @Override
                      public void done(ParseException error) {
                          if (error == null) {
                              pd.dismiss();

                              Configs.simpleAlert("Your profile has been updated!", EditProfile.this);

                          // error
                          } else {
                              Configs.simpleAlert(error.getMessage(), EditProfile.this);
                              pd.dismiss();
                  }}});



              // FULLNAME IS REQUIRED!
              } else {
                  Configs.simpleAlert("You full name is required!", EditProfile.this);
              }

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
                    bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
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
            ImageView img = (ImageView)findViewById(R.id.epAvatarImg);
            img.setImageBitmap(bm);
        }
    }
    //---------------------------------------------------------------------------------------------






    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(fullnameTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(occupationTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(aboutTxt.getWindowToken(), 0);
    }


}// @end
