package bd2.bd2;

import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Alessio on 01/07/2016.
 */
public class Query6Activity extends Activity {

    MapView mMapView;
    private static final String KEY_MAPSTATE = "mapState";
    String mMapState = null;

    private static final String KEY_RESULT_TITLE = "title";
    String mResultTitle = null;

    private static final String KEY_RESULT_SNIPPET = "snippet";
    String mResultSnippet = null;

    private static final String KEY_RESULT_X = "locationX";
    double mResultX = Double.NaN;

    private static final String KEY_RESULT_Y = "locationY";
    double mResultY = Double.NaN;

    DatabaseAccess database = null;
    SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED, 4, SimpleMarkerSymbol.STYLE.CIRCLE);
    SimpleMarkerSymbol sms_poly = new SimpleMarkerSymbol(Color.GREEN, 4, SimpleMarkerSymbol.STYLE.CROSS);
    ArrayList<Polygon> polygons []=new ArrayList[2];
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.second_layout);
        mMapView = (MapView) findViewById(R.id.map);
        Intent intent = getIntent();
        String name="";
        if (intent != null) {
            name = intent.getStringExtra("name");
        }
        try {
            database = DatabaseAccess.getInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new BackgroundTask().execute(name);



        if (savedInstanceState != null) {
            mMapState = savedInstanceState.getString(KEY_MAPSTATE, null);
            mResultTitle = savedInstanceState.getString(KEY_RESULT_TITLE, null);
            mResultSnippet = savedInstanceState.getString(KEY_RESULT_SNIPPET, null);
            mResultX = savedInstanceState.getDouble(KEY_RESULT_X, Double.NaN);
            mResultY = savedInstanceState.getDouble(KEY_RESULT_Y, Double.NaN);

        }


        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onStatusChanged(Object source, OnStatusChangedListener.STATUS status) {

                if ((status == OnStatusChangedListener.STATUS.INITIALIZED) && (source instanceof MapView)) {

                    // When map is initialized, restore the map state (center and resolution)
                    // if one was saved.
                    if ((mMapState != null) && (!mMapState.isEmpty())) {
                        mMapView.restoreState(mMapState);
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMapView != null) {
            // Save map state
            mMapState = mMapView.retainState();

            // Call MapView.pause to suspend map rendering while the activity is
            // paused, which can save battery usage.
            mMapView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Call MapView.unpause to resume map rendering when the activity returns
        // to the foreground.
        if (mMapView != null) {
            mMapView.unpause();
        }
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current state of the map before the activity is destroyed.
        outState.putString(KEY_MAPSTATE, mMapState);
    }
    private class BackgroundTask extends AsyncTask<String,ArrayList<Polygon> [],ArrayList<Polygon> []> {
        @Override
        protected ArrayList<Polygon> [] doInBackground(String... strings) {

            String name = strings[0];

            polygons = database.queryComunibyParco(name);
            return polygons;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Query6Activity.this);
            pDialog.setMessage("Query in esecuzione...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<Polygon> [] aVoid) {
            super.onPostExecute(aVoid);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            GraphicsLayer layer_poly = new GraphicsLayer();
            if(polygons[0].size()!=0) {


                Graphic[] graphics = new Graphic[polygons[0].size()];


                for (int i = 0; i < polygons[0].size(); i++) {

                    graphics[i] = new Graphic(polygons[0].get(i), sms);
                }
                layer_poly.addGraphics(graphics);
            }

            if(polygons[1].size()!=0) {
                Graphic[] graphics1 = new Graphic[polygons[1].size()];


                for (int i = 0; i < polygons[1].size(); i++) {

                    graphics1[i] = new Graphic(polygons[1].get(i), sms_poly);
                }
                layer_poly.addGraphics(graphics1);
            }



            mMapView.addLayer(layer_poly);

        }
    }

}