package bd2.bd2;

/**
 * Created by alex_ on 18/06/2016.
 */

        import android.content.Context;
        import jsqlite.*;
        import jsqlite.Exception;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;

        import com.esri.core.geometry.GeometryEngine;
        import com.esri.core.geometry.Point;
        import com.esri.core.geometry.Polyline;
        import com.esri.core.geometry.SpatialReference;
        import com.esri.core.map.Graphic;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.util.ArrayList;


public class DatabaseAccess {

    private static final String TAG ="GEODBH" ;
    private static final String TAG_SL = TAG + "_JSQLITE" ;
    private static final boolean ERROR =true ;
    private SQLiteOpenHelper openHelper;
    private Database database;
    ArrayList<Point> point_result=new ArrayList<>();
    private static DatabaseAccess instance;
    private static String DB_NAME = "dbProva.sqlite";
    private static String DB_PATH = "/data/data/bd2.bd2/databases";


    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    private DatabaseAccess(Context context) throws IOException {

        File cacheDatabase = new File(DB_PATH, DB_NAME);
        if (!cacheDatabase.getParentFile().exists()) {
            File dirDb = cacheDatabase.getParentFile();
            Log.i(TAG,"making directory: " + cacheDatabase.getParentFile());
            if (!dirDb.mkdir()) {
                throw new IOException(TAG_SL + "Could not create dirDb: " + dirDb.getAbsolutePath());
            }
        }

        InputStream inputStream = context.getAssets().open("databases/dbProva.sqlite");
        copyDatabase(inputStream, DB_PATH + File.separator + DB_NAME);
        database= new Database();
        
        try {
            database.open(cacheDatabase.getAbsolutePath(),
                    jsqlite.Constants.SQLITE_OPEN_READWRITE | jsqlite.Constants.SQLITE_OPEN_CREATE);
        } catch (jsqlite.Exception e) {
            Log.e(TAG_SL,e.getMessage());
        }
    }

    private void copyDatabase(InputStream inputStream, String dbFilename) throws IOException {
        OutputStream outputStream = new FileOutputStream(dbFilename);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer,0,length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
        Log.i(TAG,"Copied database to " + dbFilename);
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
     * prima query spaziale che rende dei punti che individuano
     * i paesi che toccano i bordi di Decimoputzu
     **/
    public ArrayList<Point> queryComuniNearby() {

        StringBuilder sb = new StringBuilder();
        sb.append("Query Comuni nearby...\n");

        String query = "SELECT Hex(ST_AsBinary(ST_Buffer(Geometry, 1.0))), ST_Srid(Geometry), ST_GeometryType(Geometry) from DBTComune" +
                " where NOME = 'SARROCH';";
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

        query = "SELECT  NOME , ASText(ST_centroid(Geometry)) from DBTComune where ST_Intersects( ST_GeomFromWKB(x'" + bufferGeom + "') ,Geometry);";

        try {
            sb.append("\tComuni nearby Decimoputzu: \n");
            Stmt stmt = database.prepare(query);

            while( stmt.step() ) {
                String name = stmt.column_string(0);
                String wkt = stmt.column_string(1);
                System.out.println(stmt.column_string(0));
                // mettere i punti trovati in un array per poi creare la polyline associata
                double x = Double.valueOf(wkt.substring(6,20));
                double y = Double.valueOf(wkt.substring(20,34));
               Point point = new Point(x,y);
                point_result.add(point);
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append(ERROR).append(e.getLocalizedMessage()).append("\n");
        }
        sb.append("Done...\n");

        return point_result;
    }


    public ArrayList<Polyline> queryComuniNearbyPolyLine() {

        Double  x,y;

        ArrayList<Polyline> polyline_result=new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("Query Comuni nearby...\n");

        String query = "SELECT Hex(ST_AsBinary(ST_Buffer(Geometry, 2.0))), ST_Srid(Geometry), ST_GeometryType(Geometry) from DBTComune" +
                " where NOME = 'SARROCH';";
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


        query = "SELECT  NOME , ASText(ST_ExteriorRing(Geometry)) from DBTComune where ST_Intersects( ST_GeomFromWKB(x'" + bufferGeom + "') ,Geometry);";


        try {
            sb.append("\tComuni nearby Decimoputzu: \n");
            Stmt stmt = database.prepare(query);

            while( stmt.step() ) {

                System.out.println(stmt.column_string(1));
                String name = stmt.column_string(0);
                if(stmt.column_string(1)!=null) {
                    String wkt = stmt.column_string(1);

                    // mettere i punti trovati in un array per poi creare la polyline associata
                    String pointStr = String.valueOf(wkt.subSequence(11, wkt.length()));
                    String[] split_comma = pointStr.split("\\s*,\\s*");
                    Polyline polyline = new Polyline();
                    for (int i = 0; i < split_comma.length; i++) {

                        String[] split = split_comma[i].split(" ");
                        String first = split[0];
                        String second = split[1];
                        if (second.endsWith(")")) {
                            second = second.substring(0, second.length() - 1);
                        }

                        x = Double.parseDouble(first);
                        y = Double.parseDouble(second);

                        Point point = new Point();
                        SpatialReference input = SpatialReference.create(3003);
                        SpatialReference output = SpatialReference.create(3857);
                        point.setXY(x, y);
                        Point webPoint = (Point) GeometryEngine.project(point, input, output);
                        if (i == 0) {
                            polyline.startPath(webPoint);
                        } else {
                            polyline.lineTo(webPoint);
                        }
                    }
                    //polyline.add(point_result);
                    polyline_result.add(polyline);
                }
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append(ERROR).append(e.getLocalizedMessage()).append("\n");
        }
        sb.append("Done...\n");

        return polyline_result;
    }

}