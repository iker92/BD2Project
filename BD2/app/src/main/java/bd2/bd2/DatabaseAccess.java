package bd2.bd2;

/**
 * Created by alex_ on 18/06/2016.
 */

        import android.content.Context;
        import android.content.res.AssetManager;
        import android.database.Cursor;
        import jsqlite.*;
        import jsqlite.Exception;

        import android.database.sqlite.SQLiteOpenHelper;
        import android.graphics.Color;
        import android.util.Log;

        import com.esri.android.map.GraphicsLayer;
        import com.esri.core.geometry.Geometry;
        import com.esri.core.geometry.GeometryEngine;
        import com.esri.core.geometry.Point;
        import com.esri.core.geometry.Polygon;
        import com.esri.core.geometry.SpatialReference;
        import com.esri.core.map.Graphic;
        import com.esri.core.symbol.SimpleMarkerSymbol;
        import com.esri.core.symbol.Symbol;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.util.ArrayList;
        import java.util.List;

public class DatabaseAccess {

    private static final String TAG ="GEODBH" ;
    private static final String TAG_SL = TAG + "_JSQLITE" ;
    private static final boolean ERROR =true ;
    private SQLiteOpenHelper openHelper;
    private Database database;
    Graphic [] graphics1;
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
        /*this.openHelper = new DatabaseOpenHelper(context);
        database = new Database();*/
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
     * Read all quotes from the database.
     *
     * @return a List of quotes
     */

    public String queryTableSimple() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("query comuni table..");

        String query = "select NOME from DBTComune order by NOME";
        stringBuilder.append("Execute query: ").append(query).append("\n");

        try {
            Stmt stmt = database.prepare(query);
            int index = 0;
            while (stmt.step()) {
                String result = stmt.column_string(0);
                stringBuilder.append("\t").append(result).append("\n");
                if (index++ > 10) break;
            }
            stringBuilder.append("\t...");
            stmt.close();
        } catch (jsqlite.Exception e) {
            Log.e(TAG_SL,e.getMessage());
        }

        stringBuilder.append("done\n");

        return stringBuilder.toString();
    }

    public void changeReference() throws Exception {

        String query="ALTER TABLE "+ database.getFilename()+".DBTComune " +
                "ALTER COLUMN Geometry TYPE geometry(MULTIPOLYGON, 3003) USING ST_Transform(Geometry, 3003);";
 Stmt stmt=database.prepare(query);

    }

    public ArrayList<Point> queryComuniNearby() {



        StringBuilder sb = new StringBuilder();
        //sb.append(SEP);
        sb.append("Query Comuni nearby...\n");

        String query = "SELECT Hex(ST_AsBinary(ST_Buffer(Geometry, 1.0))), ST_Srid(Geometry), ST_GeometryType(Geometry) from DBTComune" +
                " where NOME = 'DECIMOPUTZU';";
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
/*  String query = "select * from districts where within("
        + "ST_Transform(GeomFromText('"
                + gpsPoint + "', " + GPS_SRID
                + "), " + SOURCE_DATA_SRID + "),districts.Geometry);";*/
        query = "SELECT  NOME , ASText(ST_centroid(Geometry)) from DBTComune where ST_Intersects( ST_GeomFromWKB(x'" + bufferGeom + "') ,Geometry);";
        // just for print
        String tmpQuery = "SELECT NOME from DBTComune where ST_Intersects( ST_GeomFromWKB(x'" + bufferGeomShort + "') , Geometry );";
        sb.append("Execute query: ").append(tmpQuery).append("\n");
        try {
            sb.append("\tComuni nearby Decimoputzu: \n");
            Stmt stmt = database.prepare(query);

            ArrayList<Graphic> graphics = new ArrayList<>();
            while( stmt.step() ) {
                String name = stmt.column_string(0);
                String wkt = stmt.column_string(1);

                // mettere i punti trovati in un array per poi creare la polyline associata

               double x = Double.valueOf(wkt.substring(6,20));
                double y = Double.valueOf(wkt.substring(20,34));
                String query_points = "SELECT AsText(Transform(MakePoint(" + x + ", " + y + ", 3003), 4326));";
                database.prepare(query_points);
                double x_2 = 0.0;
                double y_2 = 0.0;
                try {
                    Stmt stmt_2 = database.prepare(query_points);
                    while (stmt_2.step()) {
                        String pointStr = stmt_2.column_string(0);
                        String first = (String) pointStr.substring(6);
                        String[] split = first.split(" ");
                        first = split[0];
                        String second = split[1];
                        if(second.endsWith(")"))
                        {
                            second = second.substring(0,second.length()-1);
                        }

                        x_2 = Double.parseDouble(first);
                        y_2 = Double.parseDouble(second);

                        Point point = new Point(x_2, y_2);
                        point_result.add(point);

                        System.out.println("X: " + point.getX() + " Y: " + point.getY());
                        sb.append("\t\t").append(name).append(" - with centroid in ").append(wkt).append("\n");
                    }
                    stmt_2.close();
                }catch (Exception e) {
                    e.printStackTrace();
                    sb.append(ERROR).append(e.getLocalizedMessage()).append("\n");
                }
            }
            /*graphics1=new Graphic[graphics.size()];
            for (int i = 0; i <graphics1.length ; i++) {
                graphics1[i]=graphics.get(i);
            }*/
            stmt.close();

            //layer.addGraphics(graphics1);

        } catch (Exception e) {
            e.printStackTrace();
            sb.append(ERROR).append(e.getLocalizedMessage()).append("\n");
        }
        sb.append("Done...\n");

        return point_result;
    }

  /*  public List<String> firstQuery() {
        List<String> list = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT NOME FROM DBTComune WHERE NOME= 'DECIMOPUTZU'", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }*/
}