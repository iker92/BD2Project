package bd2.bd2;

/**
 * Created by alex_ on 18/06/2016.
 */

        import android.content.Context;
        import jsqlite.*;
        import jsqlite.Exception;
        import android.util.Log;
        import com.esri.core.geometry.GeometryEngine;
        import com.esri.core.geometry.Point;
        import com.esri.core.geometry.Polygon;
        import com.esri.core.geometry.Polyline;
        import com.esri.core.geometry.SpatialReference;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.lang.reflect.Array;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Objects;


public class DatabaseAccess {

    private static final String TAG = "GEODBH";
    private static final String TAG_SL = TAG + "_JSQLITE";
    private static final boolean ERROR = true;
    private Database database;
    private static DatabaseAccess instance;
    private static String DB_NAME = "dbProva.sqlite";
    private static String DB_PATH = "/data/data/bd2.bd2/databases";

    /**
     * Private constructor
     */
    private DatabaseAccess(Context context) throws IOException {

        File cacheDatabase = new File(DB_PATH, DB_NAME);
        if (!cacheDatabase.getParentFile().exists()) {
            File dirDb = cacheDatabase.getParentFile();
            Log.i(TAG, "making directory: " + cacheDatabase.getParentFile());
            if (!dirDb.mkdir()) {
                throw new IOException(TAG_SL + "Could not create dirDb: " + dirDb.getAbsolutePath());
            }
        }

        InputStream inputStream = context.getAssets().open("databases/dbProva.sqlite");
        copyDatabase(inputStream, DB_PATH + File.separator + DB_NAME);
        database = new Database();

        try {
            database.open(cacheDatabase.getAbsolutePath(),
                    jsqlite.Constants.SQLITE_OPEN_READWRITE | jsqlite.Constants.SQLITE_OPEN_CREATE);
        } catch (jsqlite.Exception e) {
            Log.e(TAG_SL, e.getMessage());
        }
    }

    private void copyDatabase(InputStream inputStream, String dbFilename) throws IOException {
        OutputStream outputStream = new FileOutputStream(dbFilename);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
        Log.i(TAG, "Copied database to " + dbFilename);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DatabaseAccess getInstance(Context context) throws IOException {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }


    /**
     * Close the database connection.
     */
    public void close() throws Exception {
        if (database != null) {
            this.database.close();
        }
    }

    private ArrayList<Point> createPoint(ArrayList<String> punti) {
        ArrayList<Point> point_result = new ArrayList<>();

        for (int i = 0; i < punti.size(); i++) {


            String temp = punti.get(i).substring(6);
            String[] split = temp.split(" ");
            String first = split[0];

            String second = split[1];
            if (second.endsWith("))")) {
                second = second.substring(0, second.length() - 2);
            }
            if (second.endsWith(")")) {
                second = second.substring(0, second.length() - 1);
            }


            double x = Double.parseDouble(first);
            double y = Double.parseDouble(second);

            Point point = new Point();
            SpatialReference input = SpatialReference.create(3003);
            SpatialReference output = SpatialReference.create(3857);
            point.setXY(x, y);
            Point webPoint = (Point) GeometryEngine.project(point, input, output);

            point_result.add(webPoint);
        }
        return point_result;
    }

    private ArrayList<Polygon> createPolygon(ArrayList<String> poly) {
        ArrayList<Polygon> polyg=new ArrayList<>();
        for (int i = 0; i < poly.size(); i++) {

            Polygon polygon=new Polygon();
            String pointStr = poly.get(i).substring(9, poly.get(i).length());

            String[] split_comma = pointStr.split("\\s*,\\s*");


            for (int j = 0; j < split_comma.length; j++) {

                String[] split = split_comma[j].split(" ");
                String first = split[0];
                String second = split[1];

                if (second.endsWith("))")) {
                    second = second.substring(0, second.length() - 2);
                }
                if (second.endsWith(")")) {
                    second = second.substring(0, second.length() - 1);
                }

                // mettere i punti trovati in un array per poi creare la polyline associata
                double x = Double.parseDouble(first);
                double y = Double.parseDouble(second);
                Point point = new Point(x, y);
                SpatialReference input = SpatialReference.create(3003);
                SpatialReference output = SpatialReference.create(3857);
                Point webPoint = (Point) GeometryEngine.project(point, input, output);


                if (j == 0) {
                    polygon.startPath(webPoint);

                } else {
                    polygon.lineTo(webPoint);

                }

            }
            polyg.add(polygon);
        }
        return polyg;

    }

    private ArrayList<Polyline> createPolyline(ArrayList<String> multi_line) {

        ArrayList<Polyline> polyline=new ArrayList<>();
        for (int i = 0; i < multi_line.size(); i++) {
            Polyline poly2 = new Polyline();
            // mettere i punti trovati in un array per poi creare la polyline associata
            String pointStr = String.valueOf(multi_line.get(i).subSequence(11, multi_line.get(i).length()));

            String[] split_comma = pointStr.split("\\s*,\\s*");
            for (int j = 0; j < split_comma.length; j++) {

                String[] split = split_comma[j].split(" ");
                String first = split[0];
                String second = split[1];

                if (second.endsWith("))")) {
                    second = second.substring(0, second.length() - 2);
                }
                if (second.endsWith(")")) {
                    second = second.substring(0, second.length() - 1);
                }

                // mettere i punti trovati in un array per poi creare la polyline associata
                double x = Double.parseDouble(first);
                double y = Double.parseDouble(second);
                Point point = new Point(x, y);
                SpatialReference input = SpatialReference.create(3003);
                SpatialReference output = SpatialReference.create(3857);
                Point webPoint = (Point) GeometryEngine.project(point, input, output);
                if(j==0)
                {
                    poly2.startPath(webPoint);

                }
                else {
                    poly2.lineTo(webPoint);

                }
            }

            polyline.add(poly2);
        }
        return polyline;
    }

    /**
     * query spaziale che rende le strade passanti in
     * un determinato comune
     **/

    public Object [] queryComuneStrade(String name){

        Object [] array_final=new Object[2];
        array_final[0]=new ArrayList<Polygon>();
        array_final[1]=new ArrayList<Polyline>();
        ArrayList<Polygon> polygon_res=new ArrayList<>();
        ArrayList<String> poly=new ArrayList<>();
        ArrayList<String> multi_line=new ArrayList<>();
        ArrayList<Polyline> polyLine=new ArrayList<>();


        //String query = "SELECT ASText(fiumiTorrenti_ARC.nome, reteStradale.nome) from fiumiTorrenti_ARC JOIN reteStradale ON ST_Intersects(fiumiTorrenti_ARC.Geometry, reteStradale.Geometry);";
        String query = "SELECT ASText(GeometryN(reteStradale.Geometry,1)) from DBTComune JOIN reteStradale ON ((DBTComune.NOME = '"+name+"') AND (ST_Intersects(GeometryN(DBTComune.Geometry,1), GeometryN(reteStradale.Geometry,1)))) ;";
        String query_comune = "SELECT ASText(GeometryN(Geometry,1)) from DBTComune where nome = '"+name+"';";
        try {
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {
                String wkt = stmt.column_string(0);
                //fiume = stmt.column_string(1);
                //strada = stmt.column_string(1);
                multi_line.add(wkt);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        polyLine=createPolyline(multi_line);




        String wkt="";


        try{
            Stmt stmt2 = database.prepare(query_comune);
            while (stmt2.step()) {
                wkt = stmt2.column_string(0);
                poly.add(wkt);
                //fiume = stmt.column_string(1);
                //strada = stmt.column_string(1);

            }
            stmt2.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }




        // mettere i punti trovati in un array per poi creare la polyline associata
        polygon_res=createPolygon(poly);



        array_final[0] =polygon_res;
        array_final[1] = polyLine;



        return array_final;
    }


    /**
     * query spaziale che rende e strade che toccano più di un comune
     * che tocca un determinato parco naturale
     **/




public Object [] queryStradeComuniParco(String name){


    Object [] array_final=new Object[3];
    array_final[0]=new Polygon();
    array_final[1]=new ArrayList<Polyline>();
    array_final[2]=new ArrayList<Polygon>();
    Polyline poly2 = new Polyline();
    Polygon polygon_single=new Polygon();
    ArrayList<String> multi_line=new ArrayList<>();
    ArrayList<String> multi_polygon=new ArrayList<>();
    ArrayList<Polyline> polyLines=new ArrayList<>();
    ArrayList<Polygon> polygons=new ArrayList<>();


    //String query = "SELECT ASText(fiumiTorrenti_ARC.nome, reteStradale.nome) from fiumiTorrenti_ARC JOIN reteStradale ON ST_Intersects(fiumiTorrenti_ARC.Geometry, reteStradale.Geometry);";
    String query = "SELECT ASText(ST_GeometryN(reteStradale.Geometry,1)), ASText(ST_GeometryN(DBTComune.Geometry,1)) from " +
            "DBTComune,reteStradale, sistemaRegionaleParchi " +
            "where ST_GeometryN(DBTComune.Geometry,1) IN" +
            " ((SELECT ST_GeometryN(DBTComune.Geometry,1) FROM DBTComune comune JOIN sistemaRegionaleParchi parchi " +
            "ON " +
            "ST_Overlaps(ST_GeometryN(comune.Geometry,1),parchi.Geometry) WHERE parchi.nome='"+name+"')"+
            "AND "+
            "ST_GeometryN(reteStradale.Geometry,1) IN" +
           "(SELECT ST_GeometryN(reteStradale.Geometry,1) from DBTComune JOIN reteStradale " +
            "ON  " +
            "ST_Intersects(ST_GeometryN(DBTComune.Geometry,1), ST_GeometryN(reteStradale.Geometry,1)) " +
            "group by ST_GeometryN(DBTComune.Geometry,1) HAVING COUNT(ST_GeometryN(DBTComune.Geometry,1))>1));";


            String query_parco = "SELECT ASText(Geometry) from sistemaRegionaleParchi where nome = '"+name+"';";
    try {
        Stmt stmt = database.prepare(query);

        while (stmt.step()) {
            String wkt = stmt.column_string(0);
            String comune = stmt.column_string(1);
            //strada = stmt.column_string(1);
            multi_line.add(wkt);
            multi_polygon.add(comune);
        }
        stmt.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
            Polygon polygon1=new Polygon();

    for (int i = 0; i < multi_polygon.size(); i++) {

        // mettere i punti trovati in un array per poi creare la polyline associata
        String pointStr = String.valueOf(multi_line.get(i).subSequence(9, multi_line.get(i).length()));

        String[] split_comma = pointStr.split("\\s*,\\s*");
        for (int j = 0; j < split_comma.length; j++) {

            String[] split = split_comma[j].split(" ");
            String first = split[0];
            String second = split[1];

            if (second.endsWith("))")) {
                second = second.substring(0, second.length() - 2);
            }
            if (second.endsWith(")")) {
                second = second.substring(0, second.length() - 1);
            }

            // mettere i punti trovati in un array per poi creare la polyline associata
            double x = Double.parseDouble(first);
            double y = Double.parseDouble(second);
            Point point = new Point(x, y);

            SpatialReference input = SpatialReference.create(3003);
            SpatialReference output = SpatialReference.create(3857);
            Point webPoint = (Point) GeometryEngine.project(point, input, output);
            if(j==0)
            {
                polygon1.startPath(webPoint);

            }
            else {
                polygon1.lineTo(webPoint);

            }




        }

        polygons.add(polygon1);
    }


    for (int i = 0; i < multi_line.size(); i++) {

        // mettere i punti trovati in un array per poi creare la polyline associata
        String pointStr = String.valueOf(multi_line.get(i).subSequence(11, multi_line.get(i).length()));

        String[] split_comma = pointStr.split("\\s*,\\s*");
        for (int j = 0; j < split_comma.length; j++) {

            String[] split = split_comma[j].split(" ");
            String first = split[0];
            String second = split[1];

            if (second.endsWith("))")) {
                second = second.substring(0, second.length() - 2);
            }
            if (second.endsWith(")")) {
                second = second.substring(0, second.length() - 1);
            }

            // mettere i punti trovati in un array per poi creare la polyline associata
            double x = Double.parseDouble(first);
            double y = Double.parseDouble(second);
            Point point = new Point(x, y);
            SpatialReference input = SpatialReference.create(3003);
            SpatialReference output = SpatialReference.create(3857);
            Point webPoint = (Point) GeometryEngine.project(point, input, output);
            if(j==0)
            {
                poly2.startPath(webPoint);

            }
            else {
                poly2.lineTo(webPoint);

            }




        }

        polyLines.add(poly2);
    }




    String wkt="";


    try{
        Stmt stmt2 = database.prepare(query_parco);
        while (stmt2.step()) {
            wkt = stmt2.column_string(0);
            //fiume = stmt.column_string(1);
            //strada = stmt.column_string(1);

        }
        stmt2.close();
    }
    catch (Exception e)
    {
        e.printStackTrace();
    }




    // mettere i punti trovati in un array per poi creare la polyline associata
    String pointStr = wkt.substring(9,wkt.length());

    String[] split_comma = pointStr.split("\\s*,\\s*");

    for (int j = 0; j < split_comma.length; j++) {

        String[] split = split_comma[j].split(" ");
        String first = split[0];
        String second = split[1];

        if (second.endsWith("))")) {
            second = second.substring(0, second.length() - 2);
        }
        if (second.endsWith(")")) {
            second = second.substring(0, second.length() - 1);
        }

        // mettere i punti trovati in un array per poi creare la polyline associata
        double x = Double.parseDouble(first);
        double y = Double.parseDouble(second);
        Point point = new Point(x, y);
        SpatialReference input = SpatialReference.create(3003);
        SpatialReference output = SpatialReference.create(3857);
        Point webPoint = (Point) GeometryEngine.project(point, input, output);


        if(j==0)
        {
            polygon_single.startPath(webPoint);

        }
        else {
            polygon_single.lineTo(webPoint);

        }

    }




    array_final[0] =polygon_single;
    array_final[1] = polyLines;
    array_final[2]=polygons;






    return array_final;
}








    /**
     * query spaziale che rende i comuni che toccano
     * un determinato parco naturale
     **/

public ArrayList<Polygon> [] queryComunibyParchi(String name) {

   // String query = "SELECT ASText(ST_GeometryN(DBTComune.Geometry,1))  from DBTComune, sistemaRegionaleParchi where ST_Intersects( ST_GeometryN(DBTComune.Geometry,1) ,sistemaRegionaleParchi.Geometry) AND sistemaRegionaleParchi.nome='Parco Regionale Sulcis';";

    String query = "SELECT ASText(ST_GeometryN(comune.Geometry,1)) , ASText(parchi.Geometry) FROM DBTComune comune JOIN sistemaRegionaleParchi parchi on ST_Overlaps(ST_GeometryN(comune.Geometry,1),parchi.Geometry) WHERE parchi.nome='"+name+"';";
   // String query="SELECT ASText(Geometry) from sistemaRegionaleParchi where nome='Gennargentu e Golfo di Orosei';";

    ArrayList<String> multi_line = new ArrayList<>();
    ArrayList<String> multi_parco=new ArrayList<>();
    ArrayList<Polygon>[] polygon = new ArrayList[2];
    polygon[0]=new ArrayList<>();
    polygon[1]=new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    try {
        sb.append("\tComuni che toccano Gennargentu e Golfo di Orosei: \n");
        String parco="";
        Stmt stmt = database.prepare(query);

        while (stmt.step()) {
            String wkt = stmt.column_string(0);
            parco=stmt.column_string(1);
            multi_line.add(wkt);
        }
        multi_parco.add(parco);
        stmt.close();
    } catch (Exception e) {
        e.printStackTrace();
    }

        polygon[0]=createPolygon(multi_line);

        polygon[1]=createPolygon(multi_parco);


    return polygon;

}


    /**
     * prima query spaziale che rende dei punti che individuano
     * i paesi che toccano i bordi di Decimoputzu
     **/
    public ArrayList<Point> queryComuniNearbyCentroid(String name) {

        ArrayList<Point> point_result=new ArrayList<>();
        ArrayList<String> punti=new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("Query Comuni nearby...\n");

        String query = "SELECT Hex(ST_AsBinary(ST_Buffer(Geometry, 1.0))), ST_Srid(Geometry), ST_GeometryType(Geometry) from DBTComune" +
                " where NOME = '"+name+"';";
        sb.append("Execute query: ").append(query).append("\n");
        String bufferGeom = "";
        String bufferGeomShort = "";

        try {
            Stmt stmt = database.prepare(query);
            if (stmt.step()) {
                bufferGeom = stmt.column_string(0);
                String geomSrid = stmt.column_string(1);
                String geomType = stmt.column_string(2);
                sb.append("\tThe selected geometry is of type: ").append(geomType).append(" and of SRID: ").append(geomSrid).append("\n");
            }

            System.out.println(sb);
            bufferGeomShort = bufferGeom;
            if (bufferGeom.length() > 10)
                bufferGeomShort = bufferGeom.substring(0, 10) + "...";
            sb.append("\t"+name+" polygon buffer geometry in HEX: ").append(bufferGeomShort).append("\n");
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            sb.append(ERROR).append(e.getLocalizedMessage()).append("\n");
        }

        query = "SELECT  NOME , ASText(ST_centroid(ST_GeometryN(Geometry,1))) from DBTComune where ST_Intersects( ST_GeomFromWKB(x'" + bufferGeom + "') ,Geometry);";

        try {
            sb.append("\tComuni nearby "+name+": \n");
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {
                String name1 = stmt.column_string(0);
                String wkt = stmt.column_string(1);
                punti.add(wkt);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            sb.append(ERROR).append(e.getLocalizedMessage()).append("\n");
        }
        point_result=createPoint(punti);
        return point_result;
    }


    /**
     * seconda query spaziale che rende i poligoni che individuano
     * i paesi che toccano i bordi di Decimoputzu
     **/
    public ArrayList<Polygon> queryComuniNearbyPolygon(String name) {

        Double x, y;
        ArrayList<Polygon>polygon = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("Query Comuni nearby...\n");


        String query = "SELECT Hex(ST_AsBinary(ST_Buffer(Geometry, 1.0))), ST_Srid(Geometry), ST_GeometryType(Geometry) from DBTComune" +
                " where NOME = '"+name+"';";
        sb.append("Execute query: ").append(query).append("\n");
        String bufferGeom = "";
        String bufferGeomShort = "";

        try {
            Stmt stmt = database.prepare(query);
            if (stmt.step()) {
                bufferGeom = stmt.column_string(0);
                String geomSrid = stmt.column_string(1);
                String geomType = stmt.column_string(2);
                sb.append("\tThe selected geometry is of type: ").append(geomType).append(" and of SRID: ").append(geomSrid).append("\n");
            }

            System.out.println(sb);
            bufferGeomShort = bufferGeom;
            if (bufferGeom.length() > 10)
                bufferGeomShort = bufferGeom.substring(0, 10) + "...";
            sb.append("\tDecimoputzu polygon buffer geometry in HEX: ").append(bufferGeomShort).append("\n");
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            sb.append(ERROR).append(e.getLocalizedMessage()).append("\n");
        }

        query = "SELECT NOME, ASText(ST_GeometryN(Geometry,1)) from DBTComune where ST_Intersects( ST_GeomFromWKB(x'" + bufferGeom + "') ,Geometry);";
        ArrayList<String> multi_line = new ArrayList<>();

        try {
            sb.append("\tComuni nearby Decimoputzu: \n");
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {

                String name1 = stmt.column_string(0);
                String wkt = stmt.column_string(1);
                multi_line.add(wkt);
            }
            stmt.close();


        } catch (Exception e) {
            e.printStackTrace();
            sb.append(ERROR).append(e.getLocalizedMessage()).append("\n");
        }
        polygon=createPolygon(multi_line);

        return polygon;
    }

    public ArrayList<Polyline>[] queryStradeAttraversoFiumi(String name) {

        ArrayList<Polyline> array_final[] = new ArrayList[2];
        ArrayList<String> multi_line = new ArrayList<>();
        ArrayList<String> multi_line_fiumi = new ArrayList<>();
        array_final[0] = new ArrayList<>();
        array_final[1] = new ArrayList<>();

        String query = "SELECT ASText(fiumiTorrenti_ARC.Geometry) from fiumiTorrenti_ARC JOIN reteStradale ON ((fiumiTorrenti_ARC.nome = '"+name+"') AND (ST_Intersects(fiumiTorrenti_ARC.Geometry, reteStradale.Geometry))) ;";
        String query_fiume = "SELECT ASText(Geometry) from fiumiTorrenti_ARC where nome = '"+name+"';";

        try {
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {
                String wkt = stmt.column_string(0);
                //fiume = stmt.column_string(1);
                //strada = stmt.column_string(1);
                multi_line.add(wkt);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        array_final[0]=createPolyline(multi_line);




        try{
            Stmt stmt2 = database.prepare(query_fiume);
            while (stmt2.step()) {
                String wkt = stmt2.column_string(0);
                //fiume = stmt.column_string(1);
                //strada = stmt.column_string(1);
                multi_line_fiumi.add(wkt);
            }
            stmt2.close();
        }
            catch (Exception e) {
                e.printStackTrace();
            }

        array_final[1] = createPolyline(multi_line_fiumi);

        return array_final;
    }
}

