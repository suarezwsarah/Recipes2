package com.mba2dna.apps.bnina;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.share.internal.ShareConstants;
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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Comments extends AppCompatActivity {
    EditText commentTxt;
    List<ParseObject> commentsArray;
    ParseObject recipeObj;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    class C07753 implements OnClickListener {

        class C07741 implements SaveCallback {

            class C07731 implements GetCallback<ParseObject> {

                class C07722 implements SaveCallback {
                    C07722() {
                    }

                    public void done(ParseException e) {
                        if (e == null) {
                            Comments.this.queryComments();
                        } else {
                            Toast.makeText(Comments.this.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                C07731() {
                }

                public void done(final ParseObject recipeUser, ParseException e) {
                    final String pushMessage = ParseUser.getCurrentUser().getString(Configs.USER_FULLNAME) + " commented your Recipe: " + Comments.this.recipeObj.getString(Configs.RECIPES_TITLE);
                    HashMap<String, Object> params = new HashMap();
                    params.put("someKey", recipeUser.getObjectId());
                    params.put(ShareConstants.WEB_DIALOG_PARAM_DATA, pushMessage);
                    ParseCloud.callFunctionInBackground("pushAndroid", params, new FunctionCallback<String>() {
                        public void done(String object, ParseException e) {
                            if (e == null) {
                                Log.i("log-", "PUSH SENT TO: " + recipeUser.getString(Configs.USER_USERNAME) + "\nMESSAGE: " + pushMessage);
                            } else {
                                Toast.makeText(Comments.this.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    ParseObject actObj = new ParseObject(Configs.ACTIVITY_CLASS_NAME);
                    actObj.put(Configs.ACTIVITY_CURRENT_USER, recipeUser);
                    actObj.put(Configs.ACTIVITY_OTHER_USER, ParseUser.getCurrentUser());
                    actObj.put(Configs.ACTIVITY_TEXT, ParseUser.getCurrentUser().getString(Configs.USER_FULLNAME) + " commented your Recipe: " + Comments.this.recipeObj.getString(Configs.RECIPES_TITLE));
                    actObj.saveInBackground(new C07722());
                }
            }

            C07741() {
            }

            public void done(ParseException e) {
                if (e == null) {
                    Configs.hidePD();
                    Comments.this.dismissKeyboard();
                    Comments.this.recipeObj.increment(Configs.RECIPES_COMMENTS, Integer.valueOf(1));
                    Comments.this.recipeObj.saveInBackground();
                    Comments.this.recipeObj.getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new C07731());
                    return;
                }
                Configs.hidePD();
                Configs.simpleAlert(e.getMessage(), Comments.this);
            }
        }

        C07753() {
        }

        public void onClick(View view) {
            Configs.showPD("Please wait...", Comments.this);
            ParseObject comObj = new ParseObject(Configs.COMMENTS_CLASS_NAME);
            comObj.put(Configs.COMMENTS_USER_POINTER, ParseUser.getCurrentUser());
            comObj.put(Configs.COMMENTS_RECIPE_POINTER, Comments.this.recipeObj);
            comObj.put(Configs.COMMENTS_COMMENT, Comments.this.commentTxt.getText().toString());
            comObj.saveInBackground(new C07741());
        }
    }

    class C07804 implements FindCallback<ParseObject> {

        class C07771 implements OnItemClickListener {

            class C07761 implements GetCallback<ParseObject> {
                C07761() {
                }

                public void done(ParseObject userPointer, ParseException e) {
                    Intent i = new Intent(Comments.this, OtherUserProfile.class);
                    Bundle extras = new Bundle();
                    extras.putString("userID", userPointer.getObjectId());
                    i.putExtras(extras);
                    Comments.this.startActivity(i);
                }
            }

            C07771() {
            }

            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ((ParseObject) Comments.this.commentsArray.get(position)).getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new C07761());
            }
        }

        class AnonymousClass1ListAdapter extends BaseAdapter {
            private Context context;

            public AnonymousClass1ListAdapter(Context context, List<ParseObject> list) {
                this.context = context;
            }

            @SuppressLint("WrongConstant")
            public View getView(int position, View cell, ViewGroup parent) {
                if (cell == null) {
                    cell = ((LayoutInflater) this.context.getSystemService("layout_inflater")).inflate(R.layout.cell_comment, null);
                }
                final View finalCell = cell;
                final ParseObject comObj = (ParseObject) Comments.this.commentsArray.get(position);
                comObj.getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject userPointer, ParseException e) {
                        ((TextView) finalCell.findViewById(R.id.ccFullnameTxt)).setText(userPointer.getString(Configs.USER_FULLNAME));
                        final ImageView anImage = (ImageView) finalCell.findViewById(R.id.ccAvatarImg);
                        ParseFile fileObject = userPointer.getParseFile(Configs.USER_AVATAR);
                        if (fileObject != null) {
                            fileObject.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, ParseException error) {
                                    if (error == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        if (bmp != null) {
                                            anImage.setImageBitmap(bmp);
                                        }
                                    }
                                }
                            });
                        }
                        ((TextView) finalCell.findViewById(R.id.ccDateTxt)).setText(new SimpleDateFormat("MMM dd yyyy, hh:mm a").format(comObj.getCreatedAt()));
                        ((TextView) finalCell.findViewById(R.id.ccCommentTxt)).setText(comObj.getString(Configs.COMMENTS_COMMENT));
                    }
                });
                return cell;
            }

            public int getCount() {
                return Comments.this.commentsArray.size();
            }

            public Object getItem(int position) {
                return Comments.this.commentsArray.get(position);
            }

            public long getItemId(int position) {
                return (long) position;
            }
        }

        C07804() {
        }

        public void done(List<ParseObject> objects, ParseException error) {
            if (error == null) {
                Comments.this.commentsArray = objects;
                Configs.hidePD();
                ListView aList = (ListView) Comments.this.findViewById(R.id.commCommentsListView);
                aList.setAdapter(new AnonymousClass1ListAdapter(Comments.this, Comments.this.commentsArray));
                aList.setOnItemClickListener(new C07771());
                return;
            }
            Configs.hidePD();
            Configs.simpleAlert(error.getMessage(), Comments.this);
        }
    }

    @SuppressLint("WrongConstant")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments);
        super.setRequestedOrientation(1);
        getSupportActionBar().hide();
        this.commentTxt = (EditText) findViewById(R.id.ccCommentEditText);
        this.recipeObj = ParseObject.createWithoutData(Configs.RECIPES_CLASS_NAME, getIntent().getExtras().getString("objectID"));
        try {
            this.recipeObj.fetchIfNeeded().getParseObject(Configs.RECIPES_CLASS_NAME);
            queryComments();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ((Button) findViewById(R.id.commRefreshButt)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                queryComments();
            }
        });
        ((Button) findViewById(R.id.commBackButt)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ((Button) findViewById(R.id.comSendCommButt)).setOnClickListener(new C07753());
    }

    void queryComments() {
        Configs.showPD("Please wait...", this);
        ParseQuery query = ParseQuery.getQuery(Configs.COMMENTS_CLASS_NAME);
        query.whereEqualTo(Configs.COMMENTS_RECIPE_POINTER, recipeObj);
        query.orderByDescending(Configs.RECIPES_CREATED_AT);
        query.findInBackground(new C07804());
    }

    @SuppressLint("WrongConstant")
    public void dismissKeyboard() {
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(commentTxt.getWindowToken(), 0);
       commentTxt.setText("");
    }
}

