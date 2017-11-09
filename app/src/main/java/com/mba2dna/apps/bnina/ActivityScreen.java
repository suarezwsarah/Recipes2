package com.mba2dna.apps.bnina;

/*-----------------------------------

    - Recipes -

    created by FV iMAGINATION © 2017
    All Rights reserved

-----------------------------------*/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityScreen extends AppCompatActivity {

    /* Views */
    ProgressDialog pd;



    /* Variables */
    List<ParseObject> activityArray;



    // ON START() ----------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        // Call query
        queryActivity();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // ON CREATE() ----------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();
        Window window = this.getWindow();
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);
        pd.setIcon(R.drawable.splashlogo);

        // Init views
        TextView titleTxt = (TextView)findViewById(R.id.actTitleTxt);
       // titleTxt.setTypeface(Configs.typeWriter);



        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = (Button)findViewById(R.id.actBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {  finish();  }});


    }// end onCreate()





    // MARK: - QUERY ACTIVITY ---------------------------------------------------------------
    void queryActivity() {
        pd.setMessage("يرجى الإنتضار...");
        pd.show();

        ParseQuery query = new ParseQuery(Configs.ACTIVITY_CLASS_NAME);
        query.whereEqualTo(Configs.ACTIVITY_CURRENT_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    activityArray= objects;
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
                                cell = inflater.inflate(R.layout.cell_activity, null);
                            }
                            final View finalCell = cell;

                            // Get Parse object
                            final ParseObject actObj = activityArray.get(position);

                            // Get userPointer
                            actObj.getParseObject(Configs.ACTIVITY_OTHER_USER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {
                                    if (e == null) {

                                        // Get text
                                        TextView actTxt = (TextView) finalCell.findViewById(R.id.caActivityTxt);
                                        actTxt.setText(actObj.getString(Configs.ACTIVITY_TEXT));

                                        // Get date
                                        TextView dateTxt = (TextView) finalCell.findViewById(R.id.caDateTxt);
                                        Date aDate = actObj.getCreatedAt();
                                        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy | hh:mm a");
                                        dateTxt.setText(df.format(aDate));

                                        // Get Avatar
                                        final ImageView avImage = (ImageView) finalCell.findViewById(R.id.caAvatarImg);
                                        ParseFile fileObject = userPointer.getParseFile(Configs.USER_AVATAR);
                                        if (fileObject != null) {
                                            fileObject.getDataInBackground(new GetDataCallback() {
                                                public void done(byte[] data, ParseException error) {
                                                    if (error == null) {
                                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                        if (bmp != null) {
                                                            avImage.setImageBitmap(bmp);
                                        }}}});}


                            }}});// end userPointer


                            return cell;
                        }

                        @Override public int getCount() { return activityArray.size(); }
                        @Override public Object getItem(int position) { return activityArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Init ListView and set its Adapter
                    ListView aList = (ListView) findViewById(R.id.actListView);
                    aList.setAdapter(new ListAdapter(ActivityScreen.this, activityArray));
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            ParseObject actObj = activityArray.get(position);

                            // Get userPointer
                            actObj.getParseObject(Configs.ACTIVITY_OTHER_USER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {
                                    if (e == null) {

                                        if (!userPointer.getBoolean(Configs.USER_IS_REPORTED)) {

                                            Intent i = new Intent(ActivityScreen.this, OtherUserProfile.class);
                                            Bundle extras = new Bundle();
                                            extras.putString("userID", userPointer.getObjectId());
                                            i.putExtras(extras);
                                            startActivity(i);

                                        } else {
                                            Configs.simpleAlert("تم الإبلاغ عن هذا المستخدم!", ActivityScreen.this);
                                        }

                            }}});// end userPointer
                    }});


                // Error in query
                } else {
                    Configs.simpleAlert(error.getMessage(), ActivityScreen.this);
                    pd.dismiss();
        }}});


    }



}// @end
