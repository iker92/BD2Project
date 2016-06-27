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
        import com.esri.core.geometry.SpatialReference;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.util.ArrayList;


public class DatabaseAccess {

    private static final String TAG = "GEODBH";
    private static final String TAG_SL = TAG + "_JSQLITE";
    private static final boolean ERROR = true;
    private Database database;
    ArrayList<Point> point_result = new ArrayList<>();
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
    Double x;
    Double y;
    try {
        sb.append("\tComuni che toccano Gennargentu e Golfo di Orosei: \n");
        Stmt stmt = database.prepare(query);

        while (stmt.step()) {
            String wkt = stmt.column_string(0);
            String parco=stmt.column_string(1);
            multi_parco.add(parco);
           /* String wkt = stmt.column_string(0);*/
            multi_line.add(wkt);
        }
        stmt.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
    for (int i = 0; i < multi_line.size(); i++) {

        //String temp= (multi_line.get(i));
        // String query_multi = "SELECT ST_AsText(ST_LineMerge(ST_SnapToGrid(ST_GeomFromText('"+ multi_line.get(i) + "'),0.1)));";





        // mettere i punti trovati in un array per poi creare la polyline associata
        String pointStr = String.valueOf(multi_line.get(i).subSequence(9,multi_line.get(i).length()));


        String[] split_comma = pointStr.split("\\s*,\\s*");

        Polygon polygon1 = new Polygon();
        for (int j = 0; j < split_comma.length; j++) {

            String[] split = split_comma[j].split(" ");
            String first = split[0];

            String second = split[1];
            if(second.endsWith("))")){
                second=second.substring(0,second.length() -2);
            }
            if(second.endsWith(")")){
                second=second.substring(0,second.length() -1);
            }


            x = Double.parseDouble(first);
            y = Double.parseDouble(second);

            Point point = new Point();
            SpatialReference input = SpatialReference.create(3003);
            SpatialReference output = SpatialReference.create(3857);
            point.setXY(x, y);
            Point webPoint = (Point) GeometryEngine.project(point, input, output);
            if (j == 0) {
                polygon1.startPath(webPoint);
            } else {
                polygon1.lineTo(webPoint);
            }

        }

        polygon[0].add(polygon1);

        //polyline.add(point_result);



    }

    for (int i = 0; i < multi_parco.size(); i++) {

        String temp= (multi_parco.get(i));
        // String query_multi = "SELECT ST_AsText(ST_LineMerge(ST_SnapToGrid(ST_GeomFromText('"+ multi_line.get(i) + "'),0.1)));";





        // mettere i punti trovati in un array per poi creare la polyline associata
        String pointStr = String.valueOf(multi_parco.get(i).subSequence(9,multi_parco.get(i).length()));


        String[] split_comma = pointStr.split("\\s*,\\s*");

        Polygon polygon1 = new Polygon();
        for (int j = 0; j < split_comma.length; j++) {

            String[] split = split_comma[j].split(" ");
            String first = split[0];

            String second = split[1];
            if(second.endsWith("))")){
                second=second.substring(0,second.length() -2);
            }
            if(second.endsWith(")")){
                second=second.substring(0,second.length() -1);
            }


            x = Double.parseDouble(first);
            y = Double.parseDouble(second);

            Point point = new Point();
            SpatialReference input = SpatialReference.create(3003);
            SpatialReference output = SpatialReference.create(3857);
            point.setXY(x, y);
            Point webPoint = (Point) GeometryEngine.project(point, input, output);
            if (j == 0) {
                polygon1.startPath(webPoint);
            } else {
                polygon1.lineTo(webPoint);
            }

        }

        polygon[1].add(polygon1);

        //polyline.add(point_result);



    }

    return polygon;

}


    /**
     * prima query spaziale che rende dei punti che individuano
     * i paesi che toccano i bordi di Decimoputzu
     **/
    public ArrayList<Point> queryComuniNearbyCentroid(String name) {

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
                String temp=wkt.substring(6);
                // mettere i punti trovati in un array per poi creare la polyline associata
                String[] split =temp.split(" ");
                String first = split[0];

                String second = split[1];
                if(second.endsWith("))")){
                    second=second.substring(0,second.length() -2);
                }
                if(second.endsWith(")")){
                    second=second.substring(0,second.length() -1);
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
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append(ERROR).append(e.getLocalizedMessage()).append("\n");
        }
        sb.append("Done...\n");

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

        for (int i = 0; i < multi_line.size(); i++) {

            // mettere i punti trovati in un array per poi creare la polyline associata
            String pointStr = String.valueOf(multi_line.get(i).subSequence(9,multi_line.get(i).length()));

            String[] split_comma = pointStr.split("\\s*,\\s*");

            Polygon polygon1 = new Polygon();
            for (int j = 0; j < split_comma.length; j++) {

                String[] split = split_comma[j].split(" ");
                String first = split[0];
                String second = split[1];

                if(second.endsWith("))")){
                    second=second.substring(0,second.length() -2);
                }
                if(second.endsWith(")")){
                    second=second.substring(0,second.length() -1);
                }

                x = Double.parseDouble(first);
                y = Double.parseDouble(second);

                Point point = new Point();
                SpatialReference input = SpatialReference.create(3003);
                SpatialReference output = SpatialReference.create(3857);
                point.setXY(x, y);
                Point webPoint = (Point) GeometryEngine.project(point, input, output);
                if (j == 0) {
                    polygon1.startPath(webPoint);
                } else {
                    polygon1.lineTo(webPoint);
                }
            }

            polygon.add(polygon1);

        }
        sb.append("Done...\n");

        return polygon;
    }
}

