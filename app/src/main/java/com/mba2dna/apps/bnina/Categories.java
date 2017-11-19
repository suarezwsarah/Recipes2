package com.mba2dna.apps.bnina;

/*-----------------------------------

    - Recipes -

    created by FV iMAGINATION © 2017
    All Rights reserved

-----------------------------------*/


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Categories extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        setupCategoriesListView();
        Window window = this.getWindow();
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        // MARK: - SELECT BUTTON ------------------------------------
        Button selButt = (Button)findViewById(R.id.catSelectButt);
        selButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {  finish(); }});


    }// end onCreate()


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    // MARK: - SETUP CATEGORIES LISTVIEW ---------------------------------------------------------------
    private void setupCategoriesListView() {
        final List<String>catArray = new ArrayList<>(Arrays.asList(Configs.categoriesArray));

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
                    cell = inflater.inflate(R.layout.cell_category, null);
                }

                // Get name
                TextView nametxt = (TextView)cell.findViewById(R.id.cCatName);
                nametxt.setText(catArray.get(position));


                // Get image
                ImageView catImg = (ImageView)cell.findViewById(R.id.cCatImage);
                String imageName = catArray.get(position).toLowerCase();

                // IMPORTANT: Set the correct png name for those Categories that have multiple words in their name
                if (imageName.matches("holidays & events")) {
                    imageName = "holidays_events";
                } else if (imageName.matches("main dish")) {
                    imageName = "main_dish";
                }

                int resID = getResources().getIdentifier(imageName , "drawable", getPackageName());
                catImg.setImageResource(resID);


                return cell;
            }

            @Override public int getCount() { return catArray.size(); }
            @Override public Object getItem(int position) { return catArray.get(position); }
            @Override public long getItemId(int position) { return position; }
        }


        // Init ListView and set its adapter
        ListView catListView = (ListView) findViewById(R.id.catCategoriesListView);
        catListView.setAdapter(new ListAdapter(Categories.this, catArray));
        catListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Configs.categoryStr = catArray.get(position);
                Log.i("log-", "SELECTED CATEGORY: " + Configs.categoryStr);
        }});

    }




}// @end
