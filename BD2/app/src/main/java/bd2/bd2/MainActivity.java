package bd2.bd2;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import java.io.IOException;
import java.util.ArrayList;
import jsqlite.Exception;


public class MainActivity extends Activity{

    MapView mMapView;
    boolean flag=false;

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

        ArrayList<Point> points = databaseAccess.queryComuniNearby();

        SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED , 8, SimpleMarkerSymbol.STYLE.CIRCLE);
        GraphicsLayer layer = new GraphicsLayer();
        SpatialReference webSR = SpatialReference.create(3857);

        //aggiungo i punti al layer
        for (int i = 0; i < points.size(); i++) {

            Point webPoint = GeometryEngine.project(points.get(i).getX(), points.get(i).getY(), webSR);
            layer.addGraphic(new Graphic(webPoint, sms));
        }

        mMapView.addLayer(layer);

        try {
            databaseAccess.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("layer visibile?",String.valueOf(flag));
    }
}