package bd2.bd2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.io.IOException;
import java.util.ArrayList;

public class Query9Activity extends Activity {


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
    SimpleMarkerSymbol sms_fiume = new SimpleMarkerSymbol(Color.GREEN, 4, SimpleMarkerSymbol.STYLE.CIRCLE);
    SimpleMarkerSymbol sms_comuni = new SimpleMarkerSymbol(Color.MAGENTA, 4, SimpleMarkerSymbol.STYLE.CROSS);
    SimpleMarkerSymbol sms_strade = new SimpleMarkerSymbol(Color.BLACK, 4, SimpleMarkerSymbol.STYLE.CROSS);

    Object array_final[] = new Object [2];
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
            name = name.toUpperCase();
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
        protected Object [] doInBackground(String... strings) {

            String name=strings[0];
            array_final=database.comuniFiumiContenuti(name);
            return array_final;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Query9Activity.this);
            pDialog.setMessage("Query in esecuzione...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(Object []aVoid) {
            super.onPostExecute(aVoid);
            if (pDialog.isShowing())
            {
                pDialog.dismiss();
            }

            ArrayList<Polyline> fiume = (ArrayList<Polyline>) array_final[0];
            ArrayList<Polygon> comuni = (ArrayList<Polygon>) array_final[1];
            ArrayList<Polyline> strade = (ArrayList<Polyline>) array_final[2];


            Graphic [] graphicFiume = new Graphic[fiume.size()];
            Graphic [] graphicComune = new Graphic[comuni.size()];
            Graphic [] graphicStrade = new Graphic[strade.size()];

            GraphicsLayer layer_fiume = new GraphicsLayer();
            GraphicsLayer layer_comuni = new GraphicsLayer();
            GraphicsLayer layer_strade = new GraphicsLayer();

            if(fiume.size()!=0) {
                for (int i = 0; i < fiume.size(); i++) {

                    graphicFiume[i] = new Graphic(fiume.get(i), sms_fiume);
                }
                layer_fiume.addGraphics(graphicFiume);
            }
            if(comuni.size()!=0) {
                for (int i = 0; i < comuni.size(); i++) {

                    graphicComune[i] = new Graphic(comuni.get(i), sms_comuni);
                }
                layer_comuni.addGraphics(graphicComune);
            }
            if(strade.size()!=0) {
                for (int i = 0; i < strade.size(); i++) {

                    graphicStrade[i] = new Graphic(strade.get(i), sms_strade);
                }
                layer_strade.addGraphics(graphicStrade);
            }

            mMapView.addLayer(layer_comuni);
            mMapView.addLayer(layer_fiume);
            mMapView.addLayer(layer_strade);
        }
    }
}


