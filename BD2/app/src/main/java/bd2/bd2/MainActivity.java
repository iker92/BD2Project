package bd2.bd2;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

//import com.esri.arcgisruntime.*;
//import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jsqlite.Exception;


public class MainActivity extends Activity{

    private ListView listView;
    MapView mMapView;
    boolean flag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        //TextView result=(TextView) findViewById(R.id.result);


        //this.listView = (ListView) findViewById(R.id.listView);
        DatabaseAccess databaseAccess = null;
        try {
            databaseAccess = DatabaseAccess.getInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  GraphicsLayer quotes=databaseAccess.queryTableSimple();
        ArrayList<Point> points = databaseAccess.queryComuniNearby();

        SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED , 8, SimpleMarkerSymbol.STYLE.CIRCLE);
        GraphicsLayer layer = new GraphicsLayer();
        SpatialReference webSR = SpatialReference.create(3857);

        //aggiungo i punti al layer
        for (int i = 0; i < points.size(); i++) {

            Point webPoint = GeometryEngine.project(points.get(i).getX(), points.get(i).getY(), webSR);
            layer.addGraphic(new Graphic(webPoint, sms));
        }


        //layer.setVisible(true);
        //flag=layer.isVisible();
       /* SpatialReference mSR = graphics[0].getSpatialReference();
        Point p1 = GeometryEngine.project(39.2305400, 0.0, mSR);
        Point p2 = GeometryEngine.project(-60.0, 50.0, mSR);
        Envelope mInitExtent = new Envelope(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        mMapView.setExtent(mInitExtent);*/
        //mMapView.addLayer(basemapTileLayer);
        //mMapView.setEnabled(true);
        mMapView.addLayer(layer);

        try {
            databaseAccess.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d("layer info: ",info);
        Log.d("layer visibile?",String.valueOf(flag));
      //result.setText(quotes);
    }
}