package com.kevinthesun.csci571hw9;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.kevinthesun.csci571hw9.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DetailResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_result);
        //Set title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DetailsActivity");
        //Set tabhost
        final TabHost host = (TabHost) findViewById(R.id.tabHost);
        host.setup();
        //NEXT 24 HOURS tab
        TabHost.TabSpec spec = host.newTabSpec("NEXT 24 HOURS");
        spec.setContent(R.id.Next24Hours);
        spec.setIndicator("NEXT 24 HOURS");
        host.addTab(spec);
        //NEXT 7 DAYS
        spec = host.newTabSpec("NEXT 7 DAYS");
        spec.setContent(R.id.Next7Days);
        spec.setIndicator("NEXT 7 DAYS");
        host.addTab(spec);
        //Set tab style
        host.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.tab_bg);
        host.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.tab_bg1);
        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if(host.getCurrentTab() == 0) {
                    if(findViewById(R.id.ExpBtnRow) != null) {
                        ((RelativeLayout) findViewById(R.id.ExpBtnRow)).setVisibility(View.VISIBLE);
                    }
                }
                else {
                    if(findViewById(R.id.ExpBtnRow) != null) {
                        ((RelativeLayout) findViewById(R.id.ExpBtnRow)).setVisibility(View.GONE);
                    }
                }
                for (int i = 0; i < host.getTabWidget().getChildCount(); i++) {
                    host.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_bg1);
                }
                int tab = host.getCurrentTab();
                host.getTabWidget().getChildAt(tab).setBackgroundResource(R.drawable.tab_bg);
            }
        });
        //Render NEXT 24 HOURS
        final Intent intent = getIntent();
        try {
            final JSONObject result = new JSONObject(intent.getStringExtra("result"));
            JSONObject currently = result.getJSONObject("currently");
            JSONObject daily = result.getJSONObject("daily");
            final JSONObject hourly = result.getJSONObject("hourly");
            //Render title text
            String city = intent.getStringExtra("city"), state = intent.getStringExtra("state"), degreeUnit = intent.getStringExtra("degree");
            String title = "More Details for " + city + ", " + state;
            ((TextView) findViewById(R.id.DetailTitleText)).setText(title);
            String degree = "";
            if (degreeUnit.equals("Fahrenheit")) {
                degree = "" + '\u2109';
            } else {
                degree = "" + '\u2103';
            }
            ((TextView) findViewById(R.id.TemperatureHead)).setText("Temp" + "(" + degree + ")");
            //Render next 12 hours' information
            for(int i = 0; i < 12; ++i) {
                addRow(hourly.getJSONArray("data").getJSONObject(i), result, i);
            }
            //Add expanding button
            ImageButton expBtn = (ImageButton) findViewById(R.id.ExpBtn);
            View.OnClickListener expBtnEvent = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    RelativeLayout view = ((RelativeLayout) findViewById(R.id.ExpBtnRow));
                    ((ViewGroup) view.getParent()).removeView(view);
                    for(int i = 12; i < 24; ++i) {
                        try {
                            addRow(hourly.getJSONArray("data").getJSONObject(i), result, i);
                        }
                        catch(Exception e) {
                            System.out.println("Error happens!");
                        }
                    }
                }
            };
            expBtn.setOnClickListener(expBtnEvent);
            //Add next week information
            for(int i = 1; i <= 7; ++i) {
                JSONObject data = daily.getJSONArray("data").getJSONObject(i);
                String finalTime, temp, imgLoc = findImg(data.getString("icon"));
                long time = data.getLong("time");
                Date date = new Date(time * 1000);
                SimpleDateFormat showTime = new SimpleDateFormat("EEEE, MMMM dd");
                showTime.setTimeZone(TimeZone.getTimeZone(result.getString("timezone")));
                finalTime = showTime.format(date);
                temp = "Min: " + String.valueOf(Math.round(data.getDouble("temperatureMin"))) + degree + " | " +
                        "Max: " + String.valueOf(Math.round(data.getDouble("temperatureMax"))) + degree;
                String uri = "textView" + String.valueOf(i + 1);
                int textId = getResources().getIdentifier(uri, "id", getPackageName());
                uri = "imageView" + String.valueOf(i + 1);
                int imageId = getResources().getIdentifier(uri, "id", getPackageName());
                TextView text = (TextView) findViewById(textId);
                ImageView image = (ImageView) findViewById(imageId);
                text.setText(Html.fromHtml("<b>" + finalTime + "</b><br><br>" + temp + "<br>"));
                int imageResource = getResources().getIdentifier(imgLoc, null, getPackageName());
                image.setImageResource(imageResource);
            }
        }
        catch(Exception e) {
            System.out.println("Error happens!");
        }

    }
    private String findImg(String icon) {
        String summaryImg = "";
        switch (icon) {
            case "clear-day":
                summaryImg = "clear";
                break;
            case "clear-night":
                summaryImg = "clear_night";
                break;
            case "rain":
                summaryImg = "rain";
                break;
            case "snow":
                summaryImg = "snow";
                break;
            case "sleet":
                summaryImg = "sleet";
                break;
            case "wind":
                summaryImg = "wind";
                break;
            case "fog":
                summaryImg = "fog";
                break;
            case "cloudy":
                summaryImg = "cloudy";
                break;
            case "partly-cloudy-day":
                summaryImg = "cloud_day";
                break;
            case "partly-cloudy-night":
                summaryImg = "cloud_night";
                break;
        }
        String uri = "@drawable/" + summaryImg;
        return uri;
    }

    private void addRow(JSONObject data, JSONObject result, int i) {
        String finalTime, imgLoc, temp;
        try {
            long time = data.getLong("time");
            Date date = new Date(time * 1000);
            SimpleDateFormat showTime = new SimpleDateFormat("hh:mm a");
            showTime.setTimeZone(TimeZone.getTimeZone(result.getString("timezone")));
            finalTime = showTime.format(date);
        }
        catch (JSONException e) {
            finalTime = "N/A";
        }
        try {
            imgLoc = findImg(data.getString("icon"));
        }
        catch (JSONException e) {
            imgLoc = "";

        }
        try {
            temp = String.valueOf(Math.round(data.getDouble("temperature")));
        }
        catch (JSONException e) {
            temp = "N/A";
        }
        //Add a row
        TableLayout table = (TableLayout) findViewById(R.id.Next24Hours);
        TableRow tr = new TableRow(this);
        if(i % 2 == 0) {
            tr.setBackgroundColor(Color.GRAY);
        }
        tr.setPadding(0, 0, 0, 10);
        TableRow.LayoutParams para = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        para.setMargins(0, 0, 0, 5);
        tr.setLayoutParams(para);
        TextView time = new TextView(this);
        time.setText(finalTime);
        time.setTextSize(20);
        time.setTypeface(null, Typeface.BOLD);
        time.setGravity(Gravity.CENTER_HORIZONTAL);
        time.setLayoutParams(new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 1f));
        tr.addView(time);
        ImageView summary = new ImageView(this);
        int imageResource = getResources().getIdentifier(imgLoc, null, getPackageName());
        summary.setImageResource(imageResource);
        summary.setLayoutParams(new TableRow.LayoutParams(0,
                TableRow.LayoutParams.MATCH_PARENT, 1f));
        tr.addView(summary);
        TextView temperature = new TextView(this);
        temperature.setText(temp);
        temperature.setTextSize(20);
        temperature.setLayoutParams(new TableRow.LayoutParams(0,
                TableLayout.LayoutParams.WRAP_CONTENT, 1f));
        temperature.setGravity(Gravity.CENTER_HORIZONTAL);
        tr.addView(temperature);
        table.addView(tr, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));


    }

}
