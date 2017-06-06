package com.moutaigua.ultimatetictactoe;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.models.Country;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by mou on 3/11/17.
 */

public class ActivityLeaderboard extends Activity {

    private TextView title;
    private TextView tableColRank;
    private TextView tableColUsername;
    private TextView tableColRate;
    private TextView tableColCountry;
    private ListView list;
    private ArrayAdapter adapter;
    private ArrayList<User> top20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Typeface titleFont = Typeface.createFromAsset(getAssets(),"fonts/Patchwork_Stitchlings.ttf");
        Typeface dispFont = Typeface.createFromAsset(getAssets(),"fonts/Sansation_Light.ttf");

        title = (TextView) findViewById(R.id.activity_leaderboard_txtview_title);
        title.setTypeface(titleFont);
        tableColRank = (TextView) findViewById(R.id.activity_leaderboard_txtview_table_col_rank);
        tableColRank.setTypeface(dispFont);
        tableColUsername = (TextView) findViewById(R.id.activity_leaderboard_txtview_table_col_username);
        tableColUsername.setTypeface(dispFont);
        tableColRate = (TextView) findViewById(R.id.activity_leaderboard_txtview_table_col_rate);
        tableColRate.setTypeface(dispFont);
        tableColCountry = (TextView) findViewById(R.id.activity_leaderboard_txtview_table_col_country);
        tableColCountry.setTypeface(dispFont);

        top20 = rankingListInit();
        list = (ListView) findViewById(R.id.activity_leaderboard_listview_ranking);
        adapter = new LeaderboardAdapter(this, top20);
        list.setAdapter(adapter);

        //data loading
        FirebaseDatabaseHelper.getInstance().getTop20Users(new FirebaseDatabaseHelper.UserCallback() {
            @Override
            public void onComplete(ArrayList<User> usersList) {
                top20.clear();
                top20.addAll(usersList);
                adapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SoundHelper.getInstance(getApplicationContext()).playButtonClick();
    }


    private ArrayList<User> rankingListInit(){
        ArrayList<User> list = new ArrayList<>();
        User loadingTag = new User();
        loadingTag.setUsername("Loading");
        list.add(loadingTag);
        return list;
    }



    private class LeaderboardAdapter extends ArrayAdapter {

        private Context ctxt;
        private ArrayList<User> data;


        public LeaderboardAdapter(@NonNull Context context, ArrayList<User> users) {
            super(context, 0);
            this.ctxt = context;
            this.data = users;
        }


        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public User getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            // data is in an ascendant order
            Typeface dispFont = Typeface.createFromAsset(getAssets(),"fonts/Sansation_Light.ttf");
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View eachRow = inflater.inflate(R.layout.leaderboard_ranking_list_each, viewGroup, false);
            TextView txtIndex = (TextView) eachRow.findViewById(R.id.ranking_list_each_txtview_index);
            txtIndex.setTypeface(dispFont);
            txtIndex.setText((i+1) + ".");
            TextView txtUsername = (TextView) eachRow.findViewById(R.id.ranking_list_each_txtview_username);
            txtUsername.setTypeface(dispFont);
            txtUsername.setText(data.get(data.size()-1-i).getUsername());
            TextView txtRate = (TextView) eachRow.findViewById(R.id.ranking_list_each_txtview_rate);
            txtRate.setTypeface(dispFont);
            txtRate.setText(data.get(data.size()-1-i).getWinningPercent());

            CountryPicker picker = CountryPicker.newInstance("Select Country");
            Country country = picker.getCountryByName(ctxt, data.get(data.size()-1-i).getCountry());
            String flagFileName = "flag_" + country.getCode().toLowerCase(Locale.ENGLISH);
            int resId = getResources().getIdentifier(flagFileName, "raw", getPackageName());
            InputStream inputStream = getResources().openRawResource(resId);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ImageView imgFlag = (ImageView) eachRow.findViewById(R.id.ranking_list_each_imgview_flag);
            imgFlag.setImageBitmap(bitmap);
            return eachRow;
        }

    }



}
