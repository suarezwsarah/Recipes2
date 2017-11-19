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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;


import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Login extends AppCompatActivity {

        /* Views */
        private ProgressDialog pd;
        private EditText usernameTxt;
        private EditText passwordTxt;



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.login);
            getWindow().setBackgroundDrawableResource(R.drawable.back3);
            super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


            // Hide ActionBar
            getSupportActionBar().hide();

            Window window = Login.this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
            window.setStatusBarColor(ContextCompat.getColor(Login.this,R.color.colorPrimaryDark));


            // Init a ProgressDialog
            pd = new ProgressDialog(this);
            pd.setTitle(R.string.app_name);
            pd.setMessage("Logging in...");
            pd.setIndeterminate(false);

            // Init views
            usernameTxt = (EditText)findViewById(R.id.usernameTxt);
            passwordTxt = (EditText)findViewById(R.id.passwordTxt);




            // MARK: - FACEBOOK LOGIN BUTTON ------------------------------------------------------------------
            Button fbButt = (Button)findViewById(R.id.facebookButt);
            fbButt.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                List<String> permissions = Arrays.asList("public_profile", "email");
                pd.setMessage("يرجى الإنتضار...");
                pd.show();

                ParseFacebookUtils.logInWithReadPermissionsInBackground(Login.this, permissions, new LogInCallback() {
                     @Override
                    public void done(ParseUser user, ParseException e) {
                         if (user == null) {
                             Log.i("log-", "Uh oh. The user cancelled the Facebook login.");
                             pd.dismiss();

                         } else if (user.isNew()) {
                            getUserDetailsFromFB();

                         } else {
                            Log.i("log-", "RETURNING User logged in through Facebook!");
                            pd.dismiss();
                            startActivity(new Intent(Login.this, Home.class));
                }}});
            }});




            // This code generates a KeyHash that you'll have to copy from your Logcat console and paste it into Key Hashes field in the 'Settings' section of your Facebook Android App
            try {
                PackageInfo info = getPackageManager().getPackageInfo(
                    // IMPORTANT: REPLACE THE PACKAGE NAME BELOW WITH YOUR OWN ONE!
                    "com.mba2dna.apps.bnina",

                PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.i("log-", "keyhash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
            } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {
            }


            // MARK: - LOGIN BUTTON ------------------------------------------------------------------
            Button loginButt = (Button)findViewById(R.id.loginButt);
            loginButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pd.show();

                    ParseUser.logInInBackground(usernameTxt.getText().toString(), passwordTxt.getText().toString(),
                            new LogInCallback() {
                                public void done(ParseUser user, ParseException error) {
                                    if (user != null) {
                                        pd.dismiss();
                                        startActivity(new Intent(Login.this, Home.class));
                                    } else {
                                        pd.dismiss();
                                        Configs.simpleAlert(error.getMessage(), Login.this);
                    }}});
            }});





            // MARK: - SIGN UP BUTTON ------------------------------------------------------------------
            Button signupButt = (Button)findViewById(R.id.signUpButt);
            signupButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Login.this, SignUp.class));
            }});





            // MARK: - FORGOT PASSWORD BUTTON ------------------------------------------------------------------
            Button fpButt = (Button)findViewById(R.id.forgotPassButt);
            fpButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
                    alert.setTitle(R.string.app_name);
                    alert.setMessage("اكتب عنوان البريد الإلكتروني الصحيح الذي استخدمته للتسجيل في هذا التطبيق");

                    // Add an EditTxt
                    final EditText editTxt = new EditText (Login.this);
                    editTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    alert.setView(editTxt)
                            .setNegativeButton("إلغاء", null)
                            .setPositiveButton("أوكي", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    // Reset password
                                    ParseUser.requestPasswordResetInBackground(editTxt.getText().toString(), new RequestPasswordResetCallback() {
                                        public void done(ParseException error) {
                                            if (error == null) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                                builder.setMessage("لقد أرسلنا إليك رسالة إلكترونية لإعادة تعيين كلمة المرور.")
                                                        .setTitle(R.string.app_name)
                                                        .setPositiveButton("أوكي", null);
                                                AlertDialog dialog = builder.create();
                                                dialog.setIcon(R.drawable.splashlogo);
                                                dialog.show();

                                            } else {
                                                Configs.simpleAlert(error.getMessage(), Login.this);
                                    }}});

                            }});
                    alert.show();

            }});





            // MARK: - DISMISS BUTTON ---------------------------------------------------------------
            Button dismissButt = (Button)findViewById(R.id.loginDismissButt);
            dismissButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
            }});



        }// end onCreate()





    // MARK: - FACEBOOK GRAPH REQUEST --------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }


    private void getUserDetailsFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                String facebookID = "";
                String name = "";
                String email = "";
                String username = "";

                try{
                    name = object.getString("name");
                    email = object.getString("email");
                    facebookID = object.getString("id");

                    String[] one = name.toLowerCase().split(" ");
                    for (String word : one) { username += word; }
                    Log.i("log-", "USERNAME: " + username + "\n");
                    Log.i("log-", "email: " + email + "\n");
                    Log.i("log-", "name: " + name + "\n");

                } catch(JSONException e){ e.printStackTrace(); }


                // SAVE NEW USER IN YOUR PARSE DASHBOARD -> USER CLASS
                final String finalFacebookID = facebookID;
                final String finalUsername = username;
                final String finalEmail = email;
                final String finalName = name;

                final ParseUser currUser = ParseUser.getCurrentUser();
                currUser.put(Configs.USER_USERNAME, finalUsername);
                if (finalEmail != null) { currUser.put(Configs.USER_EMAIL, finalEmail);
                } else { currUser.put(Configs.USER_EMAIL, username + "@facebook.com"); }
                currUser.put(Configs.USER_FULLNAME, finalName);
                currUser.put(Configs.USER_IS_REPORTED, false);

                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.i("log-", "NEW USER signed up and logged in through Facebook...");


                        // Get and Save avatar from Facebook
                        new Timer().schedule(new TimerTask() {
                            @Override public void run() {
                                try {
                                    URL imageURL = new URL("https://graph.facebook.com/" + finalFacebookID + "/picture?type=large");
                                    Bitmap avatarBm = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    avatarBm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                                    currUser.put(Configs.USER_AVATAR, imageFile);
                                    currUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException error) {
                                            Log.i("log-", "... AND AVATAR SAVED!");
                                            pd.dismiss();

                                            startActivity(new Intent(Login.this, Home.class));
                                        }});
                                } catch (IOException error) { error.printStackTrace(); }

                            }}, 1000); // 1 second


                    }}); // end saveInBackground

            }}); // end graphRequest


        Bundle parameters = new Bundle();
        parameters.putString("fields", "email, name, picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }
    // END FACEBOOK GRAPH REQUEST --------------------------------------------------------------------




}//@end
