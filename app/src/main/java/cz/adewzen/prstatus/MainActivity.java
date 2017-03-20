package cz.adewzen.prstatus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog pDialog;
    private ListView lv;

    public static HashMap<String, Parkoviste>  parkoviste = new HashMap<String, Parkoviste>();
    //private static  ArrayList<HashMap<String,String> list = new ArrayList<Parkoviste>();
    ArrayList<HashMap<String, String>> list;

    private static GetData getData;

    private static Context context;

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        MainActivity.context = getApplicationContext();

        lv = (ListView) findViewById(R.id.list);

        new GetData().execute();

        Intent mServiceIntent = new Intent(this.getApplicationContext(), GetDataService.class);
        getAppContext().startActivity(mServiceIntent);



        load_settings(parkoviste);

        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
                HashMap<String, String> c = (HashMap<String, String>)parent.getAdapter().getItem(position);
                Log.i("clicked on: ", c.get("name") + " - " + c.get("parkid"));
                Intent intent = new Intent(MainActivity.getAppContext(), ParkDetailActivity.class);
                intent.putExtra("cz.adewzen.prstatus.selectedParkId", c.get("parkid"));
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();

        save_settings(parkoviste);
    }

    private void save_settings(HashMap<String, Parkoviste> parkoviste) {

        Context context = this.getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = sharedPref.edit();
        edit.clear();
        for (String parkId : parkoviste.keySet()) {
            Parkoviste  park = MainActivity.parkoviste.get(parkId);
            if (park.notify) {
                edit.putBoolean(park.parkId, park.notify);
                edit.putInt(park.parkId + "_limit", park.limit);
                edit.putString(park.parkId + "_limit_type", park.limitType);
            }

        }
    }

    private void load_settings(HashMap<String, Parkoviste> parkoviste) {
        Context context = this.getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        for (String parkId : parkoviste.keySet()) {
            Parkoviste  park = MainActivity.parkoviste.get(parkId);
            if (sharedPref.getBoolean(park.parkId, false)) {
                park.notify = true;
                park.limit = sharedPref.getInt(park.parkId + "_limit",10);
                park.limitType = sharedPref.getString(park.parkId + "_limit_type","procenta");
            };

        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        //new GetData().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.oAplikaci:
                intent = new Intent(this.getApplicationContext(), oAplikaci.class);

                startActivity(intent);
                return true;
            case R.id.Nastaveni:
                intent = new Intent(this.getApplicationContext(), NastaveniActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();


            String[] from = new String[] {"name", "obsazeni", "parkid"};
            int[] to = new int[] { R.id.name, R.id.obsazeni,R.id.parkid};

            // prepare the list of all records
            List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
            for (String parkId : parkoviste.keySet()) {
                Parkoviste  park = MainActivity.parkoviste.get(parkId);
                if (park.isPr) {
                    HashMap<String, String> map = new HashMap<String, String>();

                    map.put("name", park.name);
                    map.put("parkid", park.parkId);
                    map.put("obsazeni", Integer.toString(park.capacity - park.obsazeno) + "/" + Integer.toString(park.capacity));

                    fillMaps.add(map);
                }
            }

            // fill in the grid_item layout
            SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, fillMaps, R.layout.list_item, from, to);

            lv.setAdapter(adapter);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                URL url = new URL("https://www.tsk-praha.cz/tskexport/json/parkoviste");

                HttpURLConnection httpClient =  (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(httpClient.getInputStream());
                String response = convertStreamToString(in);

                JSONObject jsonObj = new JSONObject(response);

                JSONArray park_array = jsonObj.getJSONArray("results");

                //parkoviste.clear();

                for (int i = 0; i < park_array.length(); i++) {
                    JSONObject c = park_array.getJSONObject(i);
                    String parkId = c.getString("parkId");
                    Parkoviste park;
                    if ((park = MainActivity.parkoviste.get(parkId)) == null) {
                        park = new Parkoviste();
                    }
                    Log.i("parkoviste",c.getString("name"));
                    park.name=c.getString("name");
                    Log.i("totalNumOfPlaces","'"+c.getString("totalNumOfPlaces")+"'");
                    park.capacity=Integer.parseInt(c.getString("totalNumOfPlaces"));
                    Log.i("numOfTakenPlaces","'"+c.getString("numOfTakenPlaces")+"'");
                    park.obsazeno=Integer.parseInt(c.getString("numOfTakenPlaces"));
                    park.free=Integer.parseInt(c.getString("numOfFreePlaces"));
                    if (c.getString("pr").equals("true")) {
                        park.isPr = true;
                    }
                    park.lastUpdateDate = c.getString("lastUpdateDate");
                    park.lng = c.getString("lng");
                    park.lat = c.getString("lat");
                    park.parkId = c.getString("parkId");

                    parkoviste.put(park.parkId,park);
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);

        return true;
    }



    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
