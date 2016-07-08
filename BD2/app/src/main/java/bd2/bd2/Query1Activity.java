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
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import java.io.IOException;
import java.util.ArrayList;

public class Query1Activity extends Activity {

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
    ArrayList<Point> points;
    SimpleMarkerSymbol sms_punti = new SimpleMarkerSymbol(Color.RED, 4, SimpleMarkerSymbol.STYLE.CIRCLE);
    GraphicsLayer layer = new GraphicsLayer();
    Point p;
    ArrayList<Polygon> [] polygons;
    SimpleMarkerSymbol sms_comuni = new SimpleMarkerSymbol(Color.MAGENTA, 4, SimpleMarkerSymbol.STYLE.CROSS);
    SimpleMarkerSymbol sms_comuniVicini = new SimpleMarkerSymbol(Color.YELLOW, 4, SimpleMarkerSymbol.STYLE.CROSS);
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
            name=name.toUpperCase();
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

    private class BackgroundTask extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... strings) {

            String name = strings[0];
            points = database.queryComuniNearbyCentroid(name);
            polygons = database.queryComuniNearbyPolygon(name);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Query1Activity.this);
            pDialog.setMessage("Query in esecuzione...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            /**Trovo i centroidi**/

            ArrayList<Point> punti=points;

            //aggiungo i punti al layer di queryComuniNearByCentroid
            for (int i = 0; i < punti.size(); i++) {

                p = punti.get(i);
                layer.addGraphic(new Graphic(p, sms_punti));
            }
            mMapView.addLayer(layer);

            /**Trovo i poligoni***/

            ArrayList<Polygon> comune=polygons[0];
            ArrayList<Polygon> comuniVicini=polygons[1];
            GraphicsLayer layer_comune = new GraphicsLayer();
            GraphicsLayer layer_comuniVicini = new GraphicsLayer();
            if(comune.size()!=0) {

                Graphic graphicsComune = new Graphic(comune.get(0), sms_comuni);

                //aggiungo i punti al layer di queryComuniNearByPolygon

                layer_comune.addGraphic(graphicsComune);

            }
            if(comuniVicini.size()!=0) {
                Graphic[] graphicsComuniVicini = new Graphic[comuniVicini.size()];

                //aggiungo i punti al layer di queryComuniNearByPolygon
                for (int i = 0; i < polygons[1].size(); i++) {

                    graphicsComuniVicini[i] = new Graphic(comuniVicini.get(i), sms_comuniVicini);
                }
                layer_comuniVicini.addGraphics(graphicsComuniVicini);
            }
            mMapView.addLayer(layer_comuniVicini);
            mMapView.addLayer(layer_comune);
        }
    }

}
