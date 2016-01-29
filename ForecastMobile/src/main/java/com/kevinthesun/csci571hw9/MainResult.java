package com.kevinthesun.csci571hw9;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookDialog;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.kevinthesun.csci571hw9.R;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class MainResult extends AppCompatActivity {
    private static CallbackManager callbackManager, shareCallbackManager;
    private ShareDialog shareDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize facebook

        setContentView(R.layout.activity_main_result);
        final Intent intent = getIntent();
        //Set title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ResultActivity");
        try {
            final JSONObject result = new JSONObject(intent.getStringExtra("result"));
            final JSONObject currently = result.getJSONObject("currently");
            final JSONObject daily = result.getJSONObject("daily");
            final double lat = result.getDouble("latitude"), lng = result.getDouble("longitude");

            //Render main result page
            //Get summary image
            String imgVal = currently.getString("icon"), summaryImg = "";
            switch (imgVal) {
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
            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
            ((ImageView) findViewById(R.id.SummaryImage)).setImageResource(imageResource);
            //Get summary text
            String summary = currently.getString("summary");
            summary += " in " + intent.getStringExtra("city") + ", " + intent.getStringExtra("state");
            ((TextView) findViewById(R.id.SummaryText)).setText(summary);
            //Get degree text
            double temperature = currently.getDouble("temperature");
            double tempMax = daily.getJSONArray("data").getJSONObject(0).getDouble("temperatureMax");
            double tempMin = daily.getJSONArray("data").getJSONObject(0).getDouble("temperatureMin");
            long roundTemp = Math.round(temperature);
            long roundTempMax = Math.round(tempMax);
            long roundTempMin = Math.round(tempMin);
            String degree = "";
            String degreeSign = "" + '\u00B0';
            if (intent.getStringExtra("degree").equals("Fahrenheit")) {
                degree = "" + '\u2109';
            } else {
                degree = "" + '\u2103';
            }
            ((TextView) findViewById(R.id.DegreeText)).setText(Html.fromHtml(String.valueOf(roundTemp) + "<small><small><small><small><sup>" + degree + "</sup></small></small></small></small>"));
            ((TextView) findViewById(R.id.DegreeDetailText)).setText(Html.fromHtml("L:" + String.valueOf(roundTempMin) + "<sup><small>"
                    + degreeSign + "</small></sup> " + "| H:" + String.valueOf(roundTempMax) + "<sup><small>" + degreeSign + "</small></sup>"));

            //Get the rest information
            String restText = "";
            Double precipIntensity = currently.getDouble("precipIntensity");
            if (intent.getStringExtra("degree").equals("Celsius")) {
                precipIntensity *= 0.0393700787;
            }
            String showPrecip = "";
            if (precipIntensity >= 0 && precipIntensity < 0.002) {
                showPrecip = "None";
            } else if (precipIntensity < 0.017) {
                showPrecip = "Very Light";
            } else if (precipIntensity < 0.1) {
                showPrecip = "Light";
            } else if (precipIntensity < 0.4) {
                showPrecip = "Moderate";
            } else {
                showPrecip = "Heavy";
            }
            restText += showPrecip + "\n";
            double chanceOfRain = currently.getDouble("precipProbability");
            restText += String.valueOf(Math.round(chanceOfRain * 100)) + "%\n";
            double windSpeed = currently.getDouble("windSpeed");
            String windSpeedUnit = "mph";
            if (intent.getStringExtra("degree").equals("Celsius")) {
                windSpeed *= 2.23693629;
                windSpeedUnit = "m/s";
            }
            restText += addDecimal(String.valueOf(round(windSpeed, 2))) + " " + windSpeedUnit + "\n";
            double dewPoint = currently.getDouble("dewPoint");
            restText += String.valueOf(Math.round(dewPoint)) + degree + "\n";
            double humidity = currently.getDouble("humidity");
            restText += String.valueOf(Math.round(humidity * 100)) + "%\n";
            double visibility = currently.getDouble("visibility");
            String visUnit = "mi";
            if (intent.getStringExtra("degree").equals("Celsius")) {
                visibility /= 0.621371192;
                visUnit = "km";
            }
            restText += addDecimal(String.valueOf(round(visibility, 2))) + " " + visUnit + "\n";
            long sunriseTimeStamp = daily.getJSONArray("data").getJSONObject(0).getLong("sunriseTime");
            long sunsetTimeStamp = daily.getJSONArray("data").getJSONObject(0).getLong("sunsetTime");
            Date date = new Date(sunriseTimeStamp * 1000);
            SimpleDateFormat showTime = new SimpleDateFormat("h:mm a");
            showTime.setTimeZone(TimeZone.getTimeZone(result.getString("timezone")));
            restText += showTime.format(date) + "\n";
            date = new Date(sunsetTimeStamp * 1000);
            restText += showTime.format(date);
            ((TextView) findViewById(R.id.DetailValueText)).setText(restText);

            //MORE DETAIL button event
            Button detailBtn = (Button) findViewById(R.id.DetailButton);
            View.OnClickListener detailBtnEvent = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent passIntent = new Intent(MainResult.this, DetailResult.class);
                    passIntent.putExtra("result", intent.getStringExtra("result"));
                    passIntent.putExtra("city", intent.getStringExtra("city"));
                    passIntent.putExtra("state", intent.getStringExtra("state"));
                    passIntent.putExtra("degree", intent.getStringExtra("degree"));
                    MainResult.this.startActivity(passIntent);
                }
            };
            detailBtn.setOnClickListener(detailBtnEvent);

            //VIEW MAP button event
            Button MapBtn = (Button) findViewById(R.id.MapButton);
            View.OnClickListener MapBtnEvent = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent passIntent = new Intent(MainResult.this, ShowMap.class);
                    passIntent.putExtra("lat", String.valueOf(lat));
                    passIntent.putExtra("lng", String.valueOf(lng));
                    MainResult.this.startActivity(passIntent);
                }
            };
            MapBtn.setOnClickListener(MapBtnEvent);
            //Facebook button event
            final ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Current Weather in " + intent.getStringExtra("city") + ", " + intent.getStringExtra("state"))
                    .setContentDescription(currently.getString("summary") + ", " + String.valueOf(roundTemp) + degree)
                    .setContentUrl(Uri.parse("http://forecast.io"))
                    .setImageUrl(Uri.parse("http://cs-server.usc.edu:45678/hw/hw8/images/" + summaryImg + ".png"))
                    .build();
            FacebookSdk.sdkInitialize(this.getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            shareDialog = new ShareDialog(this);
            LoginManager.getInstance().registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            Log.d("Success", "Login");
                            if (ShareDialog.canShow(ShareLinkContent.class)) {
                                shareDialog.show(linkContent);
                                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                                    @Override
                                    public void onSuccess(Sharer.Result result) {
                                        Toast.makeText(MainResult.this, "Facebook Post Successful", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onCancel() {
                                        Toast.makeText(MainResult.this, "Posted Cancelled", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(FacebookException e) {
                                        Toast.makeText(MainResult.this, "Post Failed Due to Error", Toast.LENGTH_LONG).show();
                                    }


                                });
                            }

                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(MainResult.this, "Login Cancel", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            Toast.makeText(MainResult.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
            ImageButton fbBtn = (ImageButton) findViewById(R.id.FacebookButton);
            fbBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LoginManager.getInstance().logInWithReadPermissions(MainResult.this, Arrays.asList("public_profile", "user_friends"));
                }
            });

        } catch (Exception e) {
            System.out.println("Error happens!");
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //Helper function
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static String addDecimal(String str) {
        int dotPos = -1;
        for(int i = 0; i < str.length(); ++i) {
            if(str.charAt(i) == '.') {
                dotPos = i;
                break;
            }
        }
        if(dotPos == -1) {
            return str + ".00";
        }
        else {
            if(dotPos == str.length() - 2) {
                return str + "0";
            }
            else {
                return str;
            }
        }
    }

}
