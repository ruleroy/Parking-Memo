package com.vannakittikun.parkingmemo;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Rule on 12/8/2017.
 */

public class GetGeoLocation extends AsyncTask<String, Void, String>{
    private String server_response = null;
    public GetGeoLocationResponse getGeoLocationResponse = null;

    public GetGeoLocation(GetGeoLocationResponse response){
        this.getGeoLocationResponse = response;
    }

    @Override
    protected String doInBackground(String... strings) {
        URL url;
        HttpURLConnection urlConnection = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            url = new URL(strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();

            int responseCode = urlConnection.getResponseCode();
            Log.i("RESPONSE", Integer.toString(responseCode));

            if(responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = urlConnection.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);

                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    stringBuilder.append(current);
                }
            } else {
                Log.e("ERROR", "BAD CONNECTION");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection !=null){
                urlConnection.disconnect();
            }
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //server_response = getCurrentLocationViaJSON(jsonObject);

        JSONObject jsonObj = jsonObject;
        Log.i("JSON string =>", jsonObj.toString());

        String currentLocation = "testing";
        String street_address = null;
        String postal_code = null;

        try {
            String status = jsonObj.getString("status").toString();
            Log.i("status", status);

            if(status.equalsIgnoreCase("OK")){
                JSONArray results = jsonObj.getJSONArray("results");
                int i = 0;
                Log.i("i", i+ "," + results.length() ); //TODO delete this
                do{

                    JSONObject r = results.getJSONObject(i);
                    JSONArray typesArray = r.getJSONArray("types");
                    String types = typesArray.getString(0);

                    if(types.equalsIgnoreCase("street_address")){
                        street_address = r.getString("formatted_address").split(",")[0];
                        Log.i("street_address", street_address);
                    }else if(types.equalsIgnoreCase("postal_code")){
                        postal_code = r.getString("formatted_address");
                        Log.i("postal_code", postal_code);
                    }

                    if(street_address!=null && postal_code!=null){
                        currentLocation = street_address + ", " + postal_code;
                        Log.i("Current Location =>", currentLocation); //Delete this
                        i = results.length();
                    }

                    i++;
                }while(i<results.length());

                Log.i("JSON Geo Locatoin =>", currentLocation);
                server_response = currentLocation;
            }

        } catch (JSONException e) {
            Log.e("testing","Failed to load JSON");
            e.printStackTrace();
        }

        return server_response;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.e("Response", "" + server_response);

        if (s != null) {
            getGeoLocationResponse.onTaskDone(s);
        } else {
            getGeoLocationResponse.onError();
        }
    }

    public String getCurrentLocationViaJSON(JSONObject obj) {

        JSONObject jsonObj = obj;
        Log.i("JSON string =>", jsonObj.toString());

        String currentLocation = "testing";
        String street_address = null;
        String postal_code = null;

        try {
            String status = jsonObj.getString("status").toString();
            Log.i("status", status);

            if(status.equalsIgnoreCase("OK")){
                JSONArray results = jsonObj.getJSONArray("results");
                int i = 0;
                Log.i("i", i+ "," + results.length() ); //TODO delete this
                do{

                    JSONObject r = results.getJSONObject(i);
                    JSONArray typesArray = r.getJSONArray("types");
                    String types = typesArray.getString(0);

                    if(types.equalsIgnoreCase("street_address")){
                        street_address = r.getString("formatted_address").split(",")[0];
                        Log.i("street_address", street_address);
                    }else if(types.equalsIgnoreCase("postal_code")){
                        postal_code = r.getString("formatted_address");
                        Log.i("postal_code", postal_code);
                    }

                    if(street_address!=null && postal_code!=null){
                        currentLocation = street_address + ", " + postal_code;
                        Log.i("Current Location =>", currentLocation); //Delete this
                        i = results.length();
                    }

                    i++;
                }while(i<results.length());

                Log.i("JSON Geo Locatoin =>", currentLocation);
                return currentLocation;
            }

        } catch (JSONException e) {
            Log.e("testing","Failed to load JSON");
            e.printStackTrace();
        }
        return null;
    }
}
