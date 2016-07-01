package bd2.bd2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Crilly on 25/06/2016.
 */
public class Query3Activity extends Activity {
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
    ArrayList<Polyline> array_final[] = new ArrayList[2];
    SimpleMarkerSymbol sms_poly = new SimpleMarkerSymbol(Color.GREEN, 4, SimpleMarkerSymbol.STYLE.CROSS);
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
            // and get whatever type user account id is
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.second_layout);
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
    private class BackgroundTask extends AsyncTask<String,Object [],Object []> {
        @Override
        protected Object[] doInBackground(String... strings) {

            String name = strings[0];
            array_final = database.queryStradeAttraversoFiumi(name);
            return array_final;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Query3Activity.this);
            pDialog.setMessage("Query in esecuzione...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(Object[] aVoid) {
            super.onPostExecute(aVoid);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            Graphic [] graphics=new Graphic[array_final[0].size()];
            Graphic [] graphics1=new Graphic[array_final[1].size()];

            //aggiungo i punti al layer di queryComuniNearByPolygon
            GraphicsLayer layer_intersezioni=new GraphicsLayer();
            GraphicsLayer layer_fiume=new GraphicsLayer();
            for (int i = 0; i <array_final[0].size() ; i++) {

                graphics[i]=new Graphic(array_final[0].get(i),sms);

            }
            layer_intersezioni.addGraphics(graphics);
            for (int i = 0; i <array_final[1].size() ; i++) {

                graphics1[i]=new Graphic(array_final[1].get(i),sms_poly);
            }
            layer_fiume.addGraphics(graphics1);

            mMapView.addLayer(layer_fiume);
            mMapView.addLayer(layer_intersezioni);
        }
    }

}
