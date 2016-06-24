package bd2.bd2;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import java.io.IOException;
import java.util.ArrayList;
import jsqlite.Exception;


public class MainActivity extends Activity {

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

    boolean flag = false;
    SimpleMarkerSymbol sms_poly = new SimpleMarkerSymbol(Color.BLUE, 4, SimpleMarkerSymbol.STYLE.CROSS);
    SpatialReference input = SpatialReference.create(3003);
    SpatialReference output = SpatialReference.create(3857);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);

        DatabaseAccess databaseAccess = null;
        try {
            databaseAccess = DatabaseAccess.getInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Point> points = databaseAccess.queryComuniNearbyCentroid();

        ArrayList<Polygon> polygons = databaseAccess.queryComuniNearbyPolygon();

        SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED, 8, SimpleMarkerSymbol.STYLE.CIRCLE);

        GraphicsLayer layer = new GraphicsLayer();


        Point p = new Point();
        //aggiungo i punti al layer
        for (int i = 0; i < points.size(); i++) {

            p.setXY(points.get(i).getX(), points.get(i).getY());

            Point webPoint = (Point) GeometryEngine.project(p, input, output);

            layer.addGraphic(new Graphic(webPoint, sms));

        }

        mMapView.addLayer(layer);

        Graphic [] graphics=new Graphic[polygons.size()];

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

            // Too early to set map state here, as the map is not initialized;
            // at this point restoreState would be ignored.
        }


        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onStatusChanged(Object source, STATUS status) {

                if ((status == STATUS.INITIALIZED) && (source instanceof MapView )) {

                    // When map is initialized, restore the map state (center and resolution)
                    // if one was saved.
                    if ((mMapState != null) && (!mMapState.isEmpty())) {
                        mMapView.restoreState(mMapState);
                    }


                }

            }
        });


        try {
            databaseAccess.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("layer visibile?", String.valueOf(layer.isVisible()));




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