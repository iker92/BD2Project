package bd2.bd2;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
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

/**
 * Created by Crilly on 24/06/2016.
 */
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
    SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED, 4, SimpleMarkerSymbol.STYLE.CIRCLE);
    GraphicsLayer layer = new GraphicsLayer();
    Point p;
    ArrayList<Polygon> polygons;
    SimpleMarkerSymbol sms_poly = new SimpleMarkerSymbol(Color.GREEN, 4, SimpleMarkerSymbol.STYLE.CROSS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_layout);
        mMapView = (MapView) findViewById(R.id.map);

        try {
            database = DatabaseAccess.getInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**Trovo i centroidi**/
        points = database.queryComuniNearbyCentroid();

        //aggiungo i punti al layer di queryComuniNearByCentroid
        for (int i = 0; i < points.size(); i++) {

            p = points.get(i);
            layer.addGraphic(new Graphic(p, sms));
        }
        mMapView.addLayer(layer);

        /**Trovo i poligoni***/
        polygons = database.queryComuniNearbyPolygon();

        Graphic [] graphics=new Graphic[polygons.size()];

        //aggiungo i punti al layer di queryComuniNearByPolygon
        GraphicsLayer layer_poly=new GraphicsLayer();
        for (int i = 0; i <polygons.size() ; i++) {

            graphics[i]=new Graphic(polygons.get(i),sms_poly);
        }
        layer_poly.addGraphics(graphics);

        mMapView.addLayer(layer_poly);

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
}
