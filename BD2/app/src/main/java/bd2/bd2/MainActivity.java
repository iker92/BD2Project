package bd2.bd2;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
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

       ArrayList<Polyline> polyLine =databaseAccess.queryComuniNearbyPolyLine();

        SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED , 8, SimpleMarkerSymbol.STYLE.CIRCLE);
        SimpleMarkerSymbol sms_poly = new SimpleMarkerSymbol(Color.BLUE , 4, SimpleMarkerSymbol.STYLE.CROSS);
        GraphicsLayer layer = new GraphicsLayer();
        GraphicsLayer layer_poly=new GraphicsLayer();
        SpatialReference input=SpatialReference.create(3003);
        SpatialReference output = SpatialReference.create(3857);
        Point p=new Point();
        //aggiungo i punti al layer
       for (int i = 0; i < points.size(); i++) {

            p.setXY(points.get(i).getX(),points.get(i).getY());

            Point webPoint = (Point)GeometryEngine.project(p,input, output);

            layer.addGraphic(new Graphic(webPoint, sms));

        }
       for (int i = 0; i <polyLine.size() ; i++) {

            layer_poly.addGraphic(new Graphic(polyLine.get(i),sms_poly));
        }

        mMapView.addLayers(new Layer[]{layer,layer_poly});

        try {
            databaseAccess.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("layer visibile?",String.valueOf(layer.isVisible()));
    }
}