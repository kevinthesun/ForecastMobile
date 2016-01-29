package com.kevinthesun.csci571hw9;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity {
    //Button and text field elements
    EditText street, city;
    Spinner state;
    RadioGroup degree;
    Button search, clear, about;
    TextView emtError, link;
    String passCity = "", passState = "", passDegree = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Locate view elements
        street = (EditText) findViewById(R.id.StreetInput);
        city = (EditText) findViewById(R.id.CityInput);
        state = (Spinner) findViewById(R.id.StateInput);
        degree = (RadioGroup) findViewById(R.id.DegreeRadio);
        search = (Button) findViewById(R.id.SearchButton);
        clear = (Button) findViewById(R.id.ClearButton);
        about = (Button) findViewById(R.id.AboutButton);
        emtError = (TextView) findViewById(R.id.EmtError);
        link = (TextView) findViewById(R.id.PowerLinkText);

        //Link to forecast.io
        link.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a style=\"text-decoration:none\" href='http://forecast.io'>FORECAST.IO</a>";
        link.setText(Html.fromHtml(text));
        this.removeUnderlines((Spannable) link.getText());

        //About button event
        View.OnClickListener aboutBtnEvent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AuthorScreen.class);
                MainActivity.this.startActivity(intent);
            }
        };
        about.setOnClickListener(aboutBtnEvent);

        //Search button event
        View.OnClickListener searchBtnEvent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Deal with empty input fields
                String error = "";
                boolean valid = true;
                if(street.getText().length() == 0) {
                    error += "Please enter a Street.\n";
                    valid = false;
                }
                if(city.getText().length() == 0) {
                    error += "Please enter a City.\n";
                    valid = false;
                }
                if(state.getSelectedItem().toString().equals("Select")) {
                    error += "Please select a state.";
                    valid = false;
                }
                if(!valid) {
                    emtError.setText(error);
                }
                else {
                    //Make http post request to AWS server
                    int selectedId = degree.getCheckedRadioButtonId();
                    passCity = city.getText().toString();
                    StateConvert converter = new StateConvert();
                    passState = converter.stateConvert(state.getSelectedItem().toString());
                    passDegree = ((RadioButton) findViewById(selectedId)).getText().toString();
                    try {
                        String url = "http://yaosamazonwebapplic-env.elasticbeanstalk.com/index.php?" + "street=" + URLEncoder.encode(street.getText().toString(), "UTF-8") + "&city="
                                + URLEncoder.encode(city.getText().toString(), "UTF-8") + "&state=" + URLEncoder.encode(passState, "UTF-8")
                                + "&degree=" + ((RadioButton) findViewById(selectedId)).getText().toString();
                        asyncTask newRequest = new asyncTask();
                        newRequest.execute(url);
                    }
                    catch (UnsupportedEncodingException e) {
                        throw new AssertionError("UTF-8 not supported");
                    }
                }
            }

        };
        search.setOnClickListener(searchBtnEvent);

        //Clear button event
        View.OnClickListener clearBtnEvent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Clear input field and return to default value
                street.setText("");
                city.setText("");
                String defaultVal = "Select";
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.us_states, android.R.layout.simple_spinner_item);
                int defaultPos = adapter.getPosition(defaultVal);
                state.setSelection(defaultPos);
                degree.check(R.id.DegreeButton1);

                //Clear error messages
                emtError.setText("");
            }
        };
        clear.setOnClickListener(clearBtnEvent);
    }

    //AsyncTask class
    public class asyncTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            //Send request to server
            URL url;
            HttpURLConnection urlConnection = null;
            JSONObject response = new JSONObject();

            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int responseCode = urlConnection.getResponseCode();

                if(responseCode == 200){
                    String responseString = readStream(urlConnection.getInputStream());
                    Log.v("asyncTask", responseString);
                    response = new JSONObject(responseString);
                }else{
                    Log.v("asyncTask", "Response code:"+ responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            return response;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            //Get result JSON data and render new activity screen
            Intent intent = new Intent(MainActivity.this, MainResult.class);
            intent.putExtra("result", result.toString());
            intent.putExtra("city", passCity);
            intent.putExtra("state", passState);
            intent.putExtra("degree", passDegree);
            MainActivity.this.startActivity(intent);
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response.toString();
        }
    }

    //Delete hyperlink
    public static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String p_Url) {
            super(p_Url);
        }

        public void updateDrawState(TextPaint p_DrawState) {
            super.updateDrawState(p_DrawState);
            p_DrawState.setUnderlineText(false);
        }
    }

    public static void removeUnderlines(Spannable p_Text) {
        URLSpan[] spans = p_Text.getSpans(0, p_Text.length(), URLSpan.class);

        for(URLSpan span:spans) {
            int start = p_Text.getSpanStart(span);
            int end = p_Text.getSpanEnd(span);
            p_Text.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            p_Text.setSpan(span, start, end, 0);
        }
    }
}
