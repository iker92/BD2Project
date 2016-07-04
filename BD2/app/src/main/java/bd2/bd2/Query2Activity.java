package bd2.bd2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jsqlite.Exception;

/**
 * Created by Crilly on 24/06/2016.
 */
public class Query2Activity extends Activity {
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
    ArrayList<?>polygons []=new ArrayList[4];
    SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.BLUE, 4, SimpleMarkerSymbol.STYLE.CROSS);
    SimpleMarkerSymbol sms1 = new SimpleMarkerSymbol(Color.GREEN, 4, SimpleMarkerSymbol.STYLE.CROSS);
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
    private class BackgroundTask extends AsyncTask<String,ArrayList<?>[],ArrayList<?>[]> {
        @Override
        protected ArrayList<?>[] doInBackground(String... strings) {

            String name = strings[0];

                polygons = database.queryComunibyParchi(name);
            return polygons;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Query2Activity.this);
            pDialog.setMessage("Query in esecuzione...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<?>[] aVoid) {
            super.onPostExecute(aVoid);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            ArrayList<String> nome_parco=(ArrayList<String>)polygons[0];
            ArrayList<Polygon> parco=(ArrayList<Polygon>) polygons[1];
            ArrayList<String> nome_comune=(ArrayList<String>)polygons[2];
            ArrayList<Polygon> comuni=(ArrayList<Polygon>) polygons[3];

            GraphicsLayer layer_poly = new GraphicsLayer();
            GraphicsLayer layer_nomi=new GraphicsLayer();
            if(parco.size()!=0) {


                Graphic[] graphics_nome_parco = new Graphic[nome_parco.size()];
                Graphic [] graphics_parco=new Graphic[parco.size()];



                for (int i = 0; i < parco.size(); i++) {
                    TextSymbol txtSymbol = new TextSymbol(10,nome_parco.get(i), Color.BLACK);

                    graphics_nome_parco[i] = new Graphic(parco.get(i), txtSymbol);
                    graphics_parco[i]=new Graphic(parco.get(i),sms);
                }

                layer_poly.addGraphics(graphics_parco);
                layer_nomi.addGraphics(graphics_nome_parco);
            }

            if(comuni.size()!=0) {
                Graphic[] graphics_nomi_comuni = new Graphic[nome_comune.size()];
                Graphic[] graphics_comuni=new Graphic[comuni.size()];


                for (int i = 0; i < comuni.size(); i++) {
                    TextSymbol txtSymbol = new TextSymbol(10,nome_comune.get(i), Color.BLACK);

                    graphics_nomi_comuni[i] = new Graphic(comuni.get(i), txtSymbol);
                    graphics_comuni[i]=new Graphic(comuni.get(i),sms1);
                }

                layer_poly.addGraphics(graphics_comuni);
                layer_nomi.addGraphics(graphics_nomi_comuni);
            }



            mMapView.addLayer(layer_poly);
            mMapView.addLayer(layer_nomi);

        }
    }

}

