package com.example.manhuntapp2;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * RestfulUserSubmitTask performs GET request which adds a user to serve DB
 * parses XML response and displays a list of current users
 *
 * @author Devin Grant-Miles
 */
public class RestfulUserSubmitTask extends AsyncTask<String, Void, String>
{
    private TextView userView;
    private String userName;

    public RestfulUserSubmitTask(TextView userView, String userName) {
        this.userView = userView;
        this.userName = userName;
    }

    //send GET request and parse XML response
    protected String doInBackground(String... params)
    {
        if (params.length == 0) {
            return "No URL provided";
        }
        try {
            URL url = new URL(params[0]);//users DB URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(3000); // 3000ms
            conn.setConnectTimeout(3000); // 3000ms
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader
                        (new InputStreamReader(conn.getInputStream()));
                StringBuilder xmlResponse = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    xmlResponse.append(line);
                    line = br.readLine();
                }
                br.close();
                conn.disconnect();
                if (xmlResponse.length() == 0)
                    return "No results found";

                StringBuilder userList = new StringBuilder();
                int userIndex = xmlResponse.indexOf("<users>");
                while (userIndex >= 0)
                {
                    int userStartIndex
                            = xmlResponse.indexOf("<userName>", userIndex) + 10;
                    int userEndIndex
                            = xmlResponse.indexOf("</", userStartIndex);
                    String user = (userEndIndex > userStartIndex) ?
                            xmlResponse.substring(userStartIndex,
                                    userEndIndex) : "No user name";
                    userList.append("User: ").append(user)
                            .append("\n");

                    int latStartIndex
                            = xmlResponse.indexOf("<lat>", userIndex) + 5;
                    int latEndIndex
                            = xmlResponse.indexOf("</", latStartIndex);
                    String lat = (latEndIndex > latStartIndex) ?
                            xmlResponse.substring(latStartIndex,
                                    latEndIndex) : "No lat";
                    userList.append("Lat: ").append(lat)
                            .append("\n");

                    int lngStartIndex
                            = xmlResponse.indexOf("<lng>", userIndex) + 5;
                    int lngEndIndex
                            = xmlResponse.indexOf("</", lngStartIndex);
                    String lng = (lngEndIndex > lngStartIndex) ?
                            xmlResponse.substring(lngStartIndex,
                                    lngEndIndex) : "No lng";
                    userList.append("Lng: ").append(lng)
                            .append("\n");

                    userIndex = xmlResponse.indexOf("<users>", userIndex + 1);
                }
                return userList.toString();
            } else
                System.out.println("HTTP Response code " + responseCode);
        } catch (MalformedURLException e) {
            Log.e("RestfulSearchLookupTask", "Malformed URL: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("RestfulSearchLookupTask", "IOException: " + e);
            e.printStackTrace();
        }
        return "Error during HTTP request to url " + params[0];
    }

    //Takes XML response and creates a clickable link out of all the returned user names
    protected void onPostExecute(String workerResult) {

        int userIndex = workerResult.indexOf("User");
        SpannableString ss = new SpannableString(workerResult);

        while (userIndex >= 0) {
            int userStartIndex
                    = workerResult.indexOf("User: ", userIndex) + 6;
            int userEndIndex
                    = workerResult.indexOf("\n", userStartIndex);
            final String user = workerResult.substring(userStartIndex,
                    userEndIndex);

            int latStartIndex
                    = workerResult.indexOf("Lat: ", userIndex) + 5;
            int latEndIndex
                    = workerResult.indexOf("\n", latStartIndex);
            final String lat = workerResult.substring(latStartIndex,
                    latEndIndex);

            int lngStartIndex
                    = workerResult.indexOf("Lng: ", userIndex) + 5;
            int lngEndIndex
                    = workerResult.indexOf("\n", lngStartIndex);
            final String lng = workerResult.substring(lngStartIndex,
                    lngEndIndex);

            //OnClick method starts an intent and Activity that takes the username +
            // friend they want to locate along with lat and long
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(widget.getContext(), MapsActivity.class);
                    intent.putExtra("userName", userName);
                    intent.putExtra("user", user);
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);

                    widget.getContext().startActivity(intent);
                }
            };

            //set clickable span
            ss.setSpan(clickableSpan, userStartIndex, userEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            userIndex = workerResult.indexOf("User", userIndex + 1);
        }

        //set the text displayed in the view
        userView.setText(ss);
        userView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}