package bd2.bd2;

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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class DatabaseAccess {

    private static final String TAG = "GEODBH";
    private static final String TAG_SL = TAG + "_JSQLITE";
    private static final boolean ERROR = true;
    private Database database;
    private static DatabaseAccess instance;
    private static String DB_NAME = "DBFinal.sqlite";
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

        InputStream inputStream = context.getAssets().open("databases/DBFinal.sqlite");
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
        ExecutorService es = Executors.newCachedThreadPool();
        ArrayList<GetSinglePoint> coll = new ArrayList<>();
        Log.d("drawPoint", "Inizio disegno punto");

        for (int i = 0; i < punti.size(); i++) {

            Log.d("we", "Punto " + i);
            coll.add(new GetSinglePoint(punti.get(i), i));

        }
        List<Future<Point>> results = new LinkedList<>();
        try {
            results = es.invokeAll(coll);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Future<Point> fut : results) {
            try {
                point_result.add(fut.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

        return point_result;
    }
    public ArrayList<Polygon> createPolygon(ArrayList<String> poly)  {

        Log.d("drawPoly", "Inizio disegno poligono");
        ArrayList<Polygon> polyg=new ArrayList<>();

        ExecutorService es = Executors.newCachedThreadPool();
        ArrayList<GetSinglePolygon> coll = new ArrayList<>();

        for (int i = 0; i < poly.size(); i++) {

            Log.d("we", "Polygono " + i);
            coll.add(new GetSinglePolygon(poly.get(i), i));

        }
        List<Future<Polygon>> results = new LinkedList<>();
        try {
            results = es.invokeAll(coll);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Future<Polygon> fut : results) {
            try {
                polyg.add(fut.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

        return polyg;
    }

    private ArrayList<Polyline> createPolyline(ArrayList<String> multi_line) {

        ArrayList<Polyline> polyline=new ArrayList<>();
        ExecutorService es = Executors.newCachedThreadPool();
        ArrayList<GetSinglePolyline> coll = new ArrayList<>();
        Log.d("drawPoly", "Inizio disegno polyline");

        for (int i = 0; i < multi_line.size(); i++) {

            Log.d("we", "PolyLine " + i);
            coll.add(new GetSinglePolyline(multi_line.get(i), i));

        }
        List<Future<Polyline>> results = new LinkedList<>();
        try {
            results = es.invokeAll(coll);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Future<Polyline> fut : results) {
            try {
                polyline.add(fut.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

        return polyline;
    }

    /**
     * query spaziale che rende le strade che passano nei comuni
     * che toccano un determinato parco naturale
     **/

    public Object[] queryStradeComuniParco(String name) {

        Object[] array_final = new Object[3];
        array_final[0] = new Polygon();
        array_final[1] = new ArrayList<Polyline>();
        array_final[2] = new ArrayList<Polygon>();
        ArrayList<Polyline> strade_final = new ArrayList<>();
        ArrayList<Polygon> parchio = new ArrayList<>();
        ArrayList<Polygon> comuni = new ArrayList<>();
        ArrayList<String> nomi_comuni = new ArrayList<>();
        ArrayList<String> strade = new ArrayList<>();
        ArrayList<String> parchi=new ArrayList<>();
        String parco = "";

        String intersezione = "SELECT ASText(comune.Geometry) , ASText(parchi.Geometry) FROM DBTComune comune JOIN sistemaRegionaleParchi parchi on ST_Intersects(comune.Geometry ,parchi.Geometry) WHERE parchi.nome='"+name+"' " +
                "AND comune.ROWID IN " +
                "(SELECT pkid " +
                "FROM idx_DBTComune_geometry WHERE xmin <= MbrMaxX(parchi.Geometry) AND " +
                "ymin <= MbrMaxY(parchi.Geometry) AND " +
                "xmax >= MbrMinX(parchi.Geometry) AND " +
                "ymax >= MbrMinY(parchi.Geometry)) " +
                "GROUP BY comune.PK_UID;";


        try {
            Stmt stmt = database.prepare(intersezione);

            while (stmt.step()) {
                String comune = stmt.column_string(0);
                if(comune!=null) {
                    nomi_comuni.add(comune);
                }

                parco = stmt.column_string(1);
            }
            stmt.close();
            parchi.add(parco);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(nomi_comuni.size()>=1) {
            for (int i = 0; i < nomi_comuni.size(); i++) {

                String query_strade = "SELECT ASText(reteStradale.Geometry) " +
                        "from DBTComune JOIN reteStradale " +
                        "ON ST_Intersects(ST_GeomFromText('" + nomi_comuni.get(i) + "'), reteStradale.Geometry) " +
                        "AND DBTComune.ROWID IN " +
                        "(SELECT pkid " +
                        "FROM idx_DBTComune_geometry WHERE xmin <= MbrMaxX(reteStradale.Geometry) AND " +
                        "ymin <= MbrMaxY(reteStradale.Geometry) AND " +
                        "xmax >= MbrMinX(reteStradale.Geometry) AND " +
                        "ymax >= MbrMinY(reteStradale.Geometry))" +
                        "GROUP BY reteStradale.PK_UID;";
                try {
                    Stmt stmt = database.prepare(query_strade);
                    while (stmt.step()) {
                        String strada = stmt.column_string(0);
                        if (strada != null) {
                            strade.add(strada);
                        }

                    }
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(strade.size()!=0){
            strade_final = createPolyline(strade);
        }
        if(nomi_comuni.size()!=0){
            comuni = createPolygon(nomi_comuni);
        }
        if(parchi.size()!=0){
            parchio=createPolygon(parchi);
        }


        array_final[1] = strade_final;
        array_final[2] = comuni;
        array_final[0] = parchio;

        return array_final;
    }


    /**
     * query spaziale che rende le strade passanti in
     * un determinato comune
     **/

    public Object [] queryComuneStrade(String name){

        Object[] array_final = new Object[2];
        array_final[0] = new ArrayList<Polygon>();
        array_final[1] = new ArrayList<Polyline>();
        ArrayList<Polygon> comune_res = new ArrayList<>();
        ArrayList<String> comuni_nomi = new ArrayList<>();
        ArrayList<String> strade_nomi = new ArrayList<>();
        ArrayList<Polyline> strade = new ArrayList<>();

        String query = "SELECT ASText(reteStradale.Geometry) from DBTComune,reteStradale WHERE " +
                "DBTComune.NOME = '"+name+"' AND ST_Intersects(DbtComune.Geometry, reteStradale.Geometry) " +
                "AND DBTComune.ROWID IN" +
                " (SELECT pkid"+
                " FROM idx_DBTComune_geometry WHERE xmin <= MbrMaxX(reteStradale.Geometry) AND" +
                "     ymin <= MbrMaxY(reteStradale.Geometry) AND" +
                "     xmax >= MbrMinX(reteStradale.Geometry) AND" +
                "     ymax >= MbrMinY(reteStradale.Geometry))"+
                " GROUP BY reteStradale.PK_UID;";

        String query_comune = "SELECT ASText(Geometry) from DBTComune where nome = '"+name+"';";

        try {
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {
                String strada = stmt.column_string(0);
                if(strada!=null) {
                    strade_nomi.add(strada);
                }
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(strade_nomi.size()!=0) {
            strade = createPolyline(strade_nomi);
        }

        String comune="";

        try{
            Stmt stmt2 = database.prepare(query_comune);
            while (stmt2.step()) {
                comune = stmt2.column_string(0);
                comuni_nomi.add(comune);
            }
            stmt2.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        comune_res=createPolygon(comuni_nomi);

        array_final[0] =comune_res;
        array_final[1] = strade;

        return array_final;
    }


    /**
     * query spaziale che rende i comuni che toccano
     * un determinato parco naturale
     **/

    public ArrayList<Polygon> [] queryComunibyParchi(String name) {

        ArrayList<String> multi_line = new ArrayList<>();
        ArrayList<String> multi_parco=new ArrayList<>();
        ArrayList<Polygon>[] polygon = new ArrayList[2];
        polygon[0]=new ArrayList<>();
        polygon[1]=new ArrayList<>();

        String query = "SELECT ASText(comune.Geometry) , ASText(parchi.Geometry) FROM DBTComune comune JOIN sistemaRegionaleParchi parchi on ST_Intersects(comune.Geometry,parchi.Geometry) WHERE parchi.nome='"+name+"' " +
                "AND comune.ROWID IN " +
                "(SELECT pkid " +
                "FROM idx_DBTComune_geometry WHERE xmin <= MbrMaxX(parchi.Geometry) AND " +
                "ymin <= MbrMaxY(parchi.Geometry) AND " +
                "xmax >= MbrMinX(parchi.Geometry) AND " +
                "ymax >= MbrMinY(parchi.Geometry)) " +
                "GROUP BY comune.PK_UID;";

        try {
            String parco="";
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {
                String wkt = stmt.column_string(0);
                parco=stmt.column_string(1);
                if(wkt!=null) {
                    multi_line.add(wkt);
                }
            }
            if(parco!="") {
                multi_parco.add(parco);
                polygon[1]=createPolygon(multi_parco);
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(multi_parco.size()==0){
            String query_parco = "SELECT ASText(parchi.Geometry) FROM sistemaRegionaleParchi parchi WHERE parchi.nome='"+name+"';";

            try {
                String parco = "";
                Stmt stmt = database.prepare(query_parco);

                while (stmt.step()) {
                    parco = stmt.column_string(0);
                }
                stmt.close();

                multi_parco.add(parco);
                polygon[1] = createPolygon(multi_parco);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (multi_line.size()!=0)
        {
            polygon[0]=createPolygon(multi_line);

        }


        return polygon;
    }


    /**
     * prima query spaziale che rende dei punti che individuano
     * i paesi che toccano i bordi di un determinato comune
     **/
    public ArrayList<Point> queryComuniNearbyCentroid(String name) {

        ArrayList<Point> point_result = new ArrayList<>();
        ArrayList<String> punti = new ArrayList<>();
        ArrayList<String> comuni_res = new ArrayList<>();
        String query = "SELECT Hex(ST_AsBinary(ST_Buffer(Geometry, 1.0))) from DBTComune" +
                " where NOME = '" + name + "';";
        String bufferGeom = "";

        try {
            Stmt stmt = database.prepare(query);
            while (stmt.step()) {
                bufferGeom = stmt.column_string(0);
                comuni_res.add(bufferGeom);
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < comuni_res.size(); i++) {

        query = "SELECT  NOME , ASText(ST_centroid(Geometry)) from DBTComune where ST_Intersects( ST_GeomFromWKB(x'" + comuni_res.get(i) + "') ,Geometry);";

        try {
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {
                String name1 = stmt.column_string(0);
                String wkt = stmt.column_string(1);
                if (wkt != null) {
                    punti.add(wkt);
                }
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        if(punti.size()!=0){
            point_result=createPoint(punti);
        }

        return point_result;
    }


    /**
     * seconda query spaziale che rende i poligoni che individuano
     * i paesi che toccano i bordi di un determinato comune
     **/
    public ArrayList<Polygon> [] queryComuniNearbyPolygon(String name) {

        ArrayList<Polygon> polygon = new ArrayList<>();
        ArrayList<Polygon> comune_poly = new ArrayList<>();
        ArrayList<String> comuni_res = new ArrayList<>();
        ArrayList<Polygon>[] totalPolygon = new ArrayList[2];
        totalPolygon[0] = new ArrayList<>();
        totalPolygon[1] = new ArrayList<>();

        String query = "SELECT Hex(ST_AsBinary(ST_Buffer(Geometry, 1.0))) from DBTComune" +
                " where NOME = '" + name + "';";
        String bufferGeom = "";

        try {
            Stmt stmt = database.prepare(query);
            while (stmt.step()) {
                bufferGeom = stmt.column_string(0);
                comuni_res.add(bufferGeom);
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> multi_line = new ArrayList<>();
        ArrayList<String> comune = new ArrayList<>();
        for (int i = 0; i < comuni_res.size(); i++) {


        query = "SELECT NOME, ASText(Geometry) from DBTComune where ST_Intersects( ST_GeomFromWKB(x'" + comuni_res.get(i) + "') ,Geometry)" +
                "AND DBTComune.ROWID IN" +
                " (SELECT pkid" +
                " FROM idx_DBTComune_geometry WHERE xmin <= MbrMaxX(ST_GeomFromWKB(x'" + comuni_res.get(i) + "')) AND" +
                "     ymin <= MbrMaxY(ST_GeomFromWKB(x'" + comuni_res.get(i) + "')) AND" +
                "     xmax >= MbrMinX(ST_GeomFromWKB(x'" + comuni_res.get(i) + "')) AND" +
                "     ymax >= MbrMinY(ST_GeomFromWKB(x'" + comuni_res.get(i) + "')))" +
                " GROUP BY DBTComune.PK_UID;";


        try {
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {

                String name1 = stmt.column_string(0);
                String wkt = stmt.column_string(1);
                if (wkt != null) {
                    if (name.equals(name1)) {
                        comune.add(wkt);
                    } else {
                        multi_line.add(wkt);
                    }
                }
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        if (multi_line.size()!=0){
            polygon=createPolygon(multi_line);
        }
        if(comune.size()!=0){
            comune_poly=createPolygon(comune);
        }
        totalPolygon[0]=comune_poly;
        totalPolygon[1]=polygon;

        return totalPolygon;
    }


    /**
     * query spaziale che rende  le strade che attraversano un determinato fiume
     **/

    public ArrayList<Polyline>[] queryStradeAttraversoFiumi(String name) {

        ArrayList<Polyline> array_final[] = new ArrayList[2];
        ArrayList<String> strade_nomi = new ArrayList<>();
        ArrayList<String> multi_line_fiumi = new ArrayList<>();
        array_final[0] = new ArrayList<>();
        array_final[1] = new ArrayList<>();

        String query = "SELECT ASText(reteStradale.Geometry) from fiumiTorrenti_ARC JOIN reteStradale ON ((fiumiTorrenti_ARC.nome = '"+name+"') AND (ST_Intersects(fiumiTorrenti_ARC.Geometry, reteStradale.Geometry))) " +
                "AND reteStradale.ROWID IN" +
                " (SELECT pkid"+
                " FROM idx_reteStradale_geometry WHERE xmin <= MbrMaxX(fiumiTorrenti_ARC.Geometry) AND" +
                "     ymin <= MbrMaxY(fiumiTorrenti_ARC.Geometry) AND" +
                "     xmax >= MbrMinX(fiumiTorrenti_ARC.Geometry) AND" +
                "     ymax >= MbrMinY(fiumiTorrenti_ARC.Geometry))"+
                " GROUP BY reteStradale.PK_UID;";

        String query_fiume = "SELECT ASText(Geometry) from fiumiTorrenti_ARC where nome = '"+name+"';";

        try {
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {
                String strada = stmt.column_string(0);
                if(strada!=null) {
                    strade_nomi.add(strada);
                }
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(strade_nomi.size()!=0) {
            array_final[0] = createPolyline(strade_nomi);
        }

        try{
            Stmt stmt2 = database.prepare(query_fiume);
            while (stmt2.step()) {
                String fiume = stmt2.column_string(0);
                if(fiume!=null) {
                    multi_line_fiumi.add(fiume);
                }

            }
            stmt2.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(multi_line_fiumi.size()!=0) {
            array_final[1] = createPolyline(multi_line_fiumi);
        }

        return array_final;
    }


    /**Query spaziale che rende tutti i comuni contenuti in un parco**/

    public ArrayList<Polygon>[] queryComunibyParco(String name){

        ArrayList<Polygon> [] array_final=new ArrayList[2];
        array_final[0]=new ArrayList<Polygon>();
        array_final[1]=new ArrayList<Polygon>();
        ArrayList<String> multi_line = new ArrayList<>();
        ArrayList<String> multi_parco=new ArrayList<>();

        String query = "SELECT ASText(comune.Geometry) , ASText(parchi.Geometry) FROM DBTComune comune JOIN sistemaRegionaleParchi parchi on ST_Within(comune.Geometry,parchi.Geometry) WHERE parchi.nome='"+name+"' " +
                "AND comune.ROWID IN " +
                "(SELECT pkid " +
                "FROM idx_DBTComune_geometry WHERE xmin <= MbrMaxX(parchi.Geometry) AND " +
                "ymin <= MbrMaxY(parchi.Geometry) AND " +
                "xmax >= MbrMinX(parchi.Geometry) AND " +
                "ymax >= MbrMinY(parchi.Geometry)) " +
                "GROUP BY comune.PK_UID;";

        try {
            String parco="";
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {
                String comune = stmt.column_string(0);
                parco=stmt.column_string(1);
                if(comune!=null) {
                    multi_line.add(comune);
                }
            }
            if(parco!="") {
                multi_parco.add(parco);
                array_final[1]=createPolygon(multi_parco);
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(multi_parco.size()==0){
            String query_parco = "SELECT ASText(parchi.Geometry) FROM sistemaRegionaleParchi parchi WHERE parchi.nome='"+name+"';";

            try {
                String parco = "";
                Stmt stmt = database.prepare(query_parco);

                while (stmt.step()) {
                    parco = stmt.column_string(0);
                }
                stmt.close();

                multi_parco.add(parco);
                array_final[1] = createPolygon(multi_parco);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (multi_line.size()!=0)
        {
            array_final[0]=createPolygon(multi_line);

        }

        return array_final;
    }

    /**Query spaziale che rende tutti i comuni che toccano un determinato fiume**/

    public Object[] comuniViciniFiume(String name){

        Object[] finale = new Object[2];
        ArrayList<String> fiume_stringa = new ArrayList<>();
        ArrayList<String> comuni_stringa = new ArrayList<>();
        finale[0] = new ArrayList<Polyline>();
        finale[1] = new ArrayList<Polygon>();

        String query = "SELECT ASText(DBTComune.Geometry) from fiumiTorrenti_ARC JOIN DBTComune ON " +
                "((fiumiTorrenti_ARC.nome = '"+name+"') AND (ST_Crosses(fiumiTorrenti_ARC.Geometry, DBTComune.Geometry))) " +
                "AND DBTComune.ROWID IN" +
                " (SELECT pkid"+
                " FROM idx_DBTComune_geometry WHERE xmin <= MbrMaxX(fiumiTorrenti_ARC.Geometry) AND" +
                "     ymin <= MbrMaxY(fiumiTorrenti_ARC.Geometry) AND" +
                "     xmax >= MbrMinX(fiumiTorrenti_ARC.Geometry) AND" +
                "     ymax >= MbrMinY(fiumiTorrenti_ARC.Geometry))"+
                " GROUP BY DBTComune.PK_UID;";

        String query_fiume = "SELECT ASText(Geometry) from fiumiTorrenti_ARC where nome = '"+name+"';";

        try {
            Stmt stmt = database.prepare(query_fiume);

            while (stmt.step()) {
                String fiume = stmt.column_string(0);
                if(fiume!=null) {
                    fiume_stringa.add(fiume);
                }
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Stmt stmt2 = database.prepare(query);

            while (stmt2.step()) {
                String comune = stmt2.column_string(0);
                if(comune!=null) {
                    comuni_stringa.add(comune);
                }
            }
            stmt2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(fiume_stringa.size()!=0) {
            finale[0] = createPolyline(fiume_stringa);
        }
        if(comuni_stringa.size()!=0) {
            finale[1] = createPolygon(comuni_stringa);
        }

        return finale;
    }

    /**
     * query spaziale che rende le strade passanti all'interno di un comune e i parchi intersecati dal comune
     **/


    public  Object[] queryComuneParchiOverlapsStradeContains(String name) {

        Object[] array_final = new Object[3];
        array_final[0] = new ArrayList<Polygon>();
        array_final[1]=new ArrayList<Polygon>();
        array_final[2]=new ArrayList<Polyline>();
        ArrayList<Polygon> parchi_res = new ArrayList<>();
        ArrayList<String> multi_line=new ArrayList<>();
        ArrayList<String> parchi = new ArrayList<>();
        ArrayList<String> comune=new ArrayList<>();
        ArrayList<Polyline> strade=new ArrayList<>();

        String query = "SELECT ASText(parchi.Geometry) from " +
                "DBTComune comune ,sistemaRegionaleParchi parchi" +
                " WHERE " +
                "comune.NOME = '"+name+"' AND ST_Overlaps(comune.Geometry, parchi.Geometry) " +
                "AND parchi.ROWID IN "  +
                "(SELECT pkid " +
                "FROM idx_sistemaRegionaleParchi_geometry WHERE xmin <= MbrMaxX(comune.Geometry) AND " +
                "ymin <= MbrMaxY(comune.Geometry) AND " +
                "xmax >= MbrMinX(comune.Geometry) AND " +
                "ymax >= MbrMinY(comune.Geometry)) " +
                "GROUP BY parchi.PK_UID ;";

        String query_comune = "SELECT ASText(comune.Geometry), ASText(strade.Geometry) from DBTComune comune ,reteStradale strade where " +
                "comune.NOME = '"+name+"' " +
                "AND ST_Contains(comune.Geometry,strade.Geometry) " +
                "AND strade.ROWID IN " +
                "(SELECT pkid " +
                "FROM idx_reteStradale_geometry WHERE xmin <= MbrMaxX(comune.Geometry) AND " +
                "ymin <= MbrMaxY(comune.Geometry) AND " +
                "xmax >= MbrMinX(comune.Geometry) AND " +
                "ymax >= MbrMinY(comune.Geometry)) " +
                "GROUP BY strade.PK_UID;";

        try {
            Stmt stmt = database.prepare(query);

            while (stmt.step()) {

                String parco=stmt.column_string(0);
                if(parco!=null) {
                    parchi.add(parco);
                }
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



        String wkt="";

        try{
            Stmt stmt2 = database.prepare(query_comune);
            int i=0;
            while (stmt2.step()) {
                wkt = stmt2.column_string(0);
                String strada=stmt2.column_string(1);

                if(!wkt.equals("")){
                    comune.add(wkt);
                }
                if(strada!=null) {
                    multi_line.add(strada);
                }

            }

            stmt2.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if(comune.size()==0) {
            String query_comune_alone = "SELECT ASText(comuni.Geometry) FROM DBTComune comuni WHERE comuni.NOME='" + name + "';";

            try {
                Stmt stmt = database.prepare(query_comune_alone);

                while (stmt.step()) {
                    wkt = stmt.column_string(0);
                }
                stmt.close();

                comune.add(wkt);
                array_final[1] = createPolygon(comune);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        strade = createPolyline(multi_line);

        parchi_res=createPolygon(parchi);

        array_final[0] = parchi_res;
        array_final[1]=createPolygon(comune);
        array_final[2]=strade;

        return array_final;
    }

    /**Query spaziale che prende in input un paese e restituisce i fiumi e le strade
     * completamente contenuti all'interno **/

    public Object[] comuniFiumiContenuti(String name) {

        Object[] finale = new Object[3];
        ArrayList<String> fiume_stringa = new ArrayList<>();
        ArrayList<String> comuni_stringa = new ArrayList<>();
        ArrayList<String> strade_stringa = new ArrayList<>();
        finale[0] = new ArrayList<Polyline>();
        finale[1] = new ArrayList<Polyline>();
        finale[2] = new ArrayList<Polygon>();
        String strade="";
        String fiumi="";

        String query_comune = "SELECT ASText(DBTComune.Geometry) from DBTComune where nome = '"+name+"'";

        String query = "SELECT ASText(fiumi.Geometry), ASText(strade.Geometry)" +
                " FROM DBTComune comuni, reteStradale strade, fiumiTorrenti_ARC fiumi " +
                "where (ST_Contains(comuni.Geometry, fiumi.Geometry) and ST_Contains(comuni.Geometry, strade.Geometry)) " +
                "AND comuni.nome='" + name + "' " +
                "AND strade.ROWID IN " +
                "(SELECT pkid " +
                "FROM idx_reteStradale_geometry WHERE xmin <= MbrMaxX(comuni.Geometry) AND " +
                "ymin <= MbrMaxY(comuni.Geometry) AND " +
                "xmax >= MbrMinX(comuni.Geometry) AND " +
                "ymax >= MbrMinY(comuni.Geometry)) " +
                "AND fiumi.ROWID IN "  +
                "(SELECT pkid " +
                "FROM idx_fiumiTorrenti_ARC_geometry WHERE xmin <= MbrMaxX(comuni.Geometry) AND " +
                "ymin <= MbrMaxY(comuni.Geometry) AND " +
                "xmax >= MbrMinX(comuni.Geometry) AND " +
                "ymax >= MbrMinY(comuni.Geometry)) " +
                "GROUP BY strade.PK_UID, fiumi.PK_UID;";

        try {
            Stmt stmt = database.prepare(query);
            while (stmt.step()) {
                strade = stmt.column_string(1);
                fiumi = stmt.column_string(0);
                if(!strade.equals("")) {
                    strade_stringa.add(strade);
                }
                if(!fiumi.equals("")) {
                    fiume_stringa.add(fiumi);
                }

            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Stmt stmt = database.prepare(query_comune);
            while (stmt.step()) {
                String wkt = stmt.column_string(0);
                comuni_stringa.add(wkt);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(fiume_stringa.size()!=0){
            finale[0] = createPolyline(fiume_stringa);
        }
        if(comuni_stringa.size()!=0){
            finale[1] = createPolygon(comuni_stringa);
        }
        if(strade_stringa.size()!=0){
            finale[2] = createPolyline(strade_stringa);
        }

        return finale;
    }


    /**
     * query spaziale che rende, dato un parco, i fiumi completamenete contenui dentro al parco, le strade che attraversano
     * il parco e i comuni attraversati dalle strade trovate
     **/

    public Object [] queryAll(String name){
        Object [] array_final=new Object[4];
        ArrayList<String> strade_stringa = new ArrayList<>();
        ArrayList<String> comuni_stringa = new ArrayList<>();
        ArrayList<String> fiumi_stringa = new ArrayList<>();
        ArrayList<String> parchi_stringa = new ArrayList<>();
        array_final[0] = new ArrayList<Polyline>();
        array_final[1]=new ArrayList<Polyline>();
        array_final[2] = new ArrayList<Polygon>();
        array_final[3]=new ArrayList<Polygon>();
        String strada="";
        String fiume="";


        String query_strade = "SELECT ASText(parchi.Geometry), ASText(strade.Geometry)from " +
                " sistemaRegionaleParchi parchi, reteStradale strade " +
                " WHERE " +
                "parchi.nome = '"+name+"' AND ST_Crosses(strade.Geometry, parchi.Geometry) " +
                "AND " +
                "strade.ROWID IN "  +
                "(SELECT pkid " +
                "FROM idx_reteStradale_geometry WHERE xmin <= MbrMaxX(parchi.Geometry) AND " +
                "ymin <= MbrMaxY(parchi.Geometry) AND " +
                "xmax >= MbrMinX(parchi.Geometry) AND " +
                "ymax >= MbrMinY(parchi.Geometry)) " +
                "GROUP BY strade.PK_UID";

        String parco="";

        try {
            Stmt stmt = database.prepare(query_strade);
            while (stmt.step()) {
                strada = stmt.column_string(1);
                parco=stmt.column_string(0);
                if(!strada.equals("")) {
                    strade_stringa.add(strada);
                }

            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String query_fiume = "SELECT ASText(fiumi.Geometry)from " +
                " sistemaRegionaleParchi parchi, fiumiTorrenti_ARC fiumi " +
                " WHERE " +
                "parchi.nome = '"+name+"' AND ST_Contains(parchi.Geometry, fiumi.Geometry) " +
                "AND " +
                "fiumi.ROWID IN "  +
                "(SELECT pkid " +
                "FROM idx_fiumiTorrenti_ARC_geometry WHERE xmin <= MbrMaxX(parchi.Geometry) AND " +
                "ymin <= MbrMaxY(parchi.Geometry) AND " +
                "xmax >= MbrMinX(parchi.Geometry) AND " +
                "ymax >= MbrMinY(parchi.Geometry)) " +
                "GROUP BY fiumi.PK_UID";

        try {
            Stmt stmt = database.prepare(query_fiume);
            while (stmt.step()) {
                fiume = stmt.column_string(0);
                if(!fiume.equals("")) {
                    fiumi_stringa.add(fiume);
                }

            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!parco.equals("")){
            parchi_stringa.add(parco);
        }

        if(parchi_stringa.size()==0) {
            String query_parco_alone = "SELECT ASText(parchi.Geometry) FROM sistemaRegionaleParchi parchi WHERE parchi.nome='" + name + "';";

            try {
                StringBuilder sb = new StringBuilder();

                Stmt stmt = database.prepare(query_parco_alone);

                while (stmt.step()) {
                    parco = stmt.column_string(0);
                }
                stmt.close();

                parchi_stringa.add(parco);
                array_final[3] = createPolygon(parchi_stringa);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        for (int i = 0; i <strade_stringa.size() ; i++) {


            String queryComuniStrade = " SELECT ASText(comune.Geometry) from " +
                    "DBTComune comune, reteStradale strade "+
                    "WHERE " +
                    "ST_Intersects(ST_GeomFromText('" + strade_stringa.get(i) + "'), comune.Geometry) " +
                    "AND " +
                    "comune.ROWID IN " +
                    "(SELECT pkid " +
                    "FROM idx_DBTComune_geometry WHERE xmin <= MbrMaxX(strade.Geometry) AND " +
                    "ymin <= MbrMaxY(strade.Geometry) AND " +
                    "xmax >= MbrMinX(strade.Geometry) AND " +
                    "ymax >= MbrMinY(strade.Geometry)) " +
                    "GROUP BY comune.PK_UID";

            try {
                Stmt stmt2 = database.prepare(queryComuniStrade);
                while (stmt2.step()) {
                    String comune = stmt2.column_string(0);
                    if(comuni_stringa.contains(comune)){

                    }
                    else{
                        comuni_stringa.add(comune);
                    }

                }
                stmt2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if(fiumi_stringa.size()!=0){
            array_final[1] = createPolyline(fiumi_stringa);
        }
        if(comuni_stringa.size()!=0){
            array_final[2] = createPolygon(comuni_stringa);
        }
        if(strade_stringa.size()!=0){
            array_final[0] = createPolyline(strade_stringa);
        }
        if(parchi_stringa.size()!=0){
            array_final[3] = createPolygon(parchi_stringa);
        }

        return array_final;
    }


    class GetSinglePolygon implements Callable<Polygon> {

        private int id;
        private Polygon polygon;
        private String passedString;

        public GetSinglePolygon(String passedString, int id) {
            this.passedString = passedString;
            this.id = id;
            polygon = new Polygon();
        }

        @Override
        public Polygon call() throws java.lang.Exception {
            Log.d("THREADS", "Thread " + id + " started.");
            SpatialReference input = SpatialReference.create(3003);
            SpatialReference output = SpatialReference.create(3857);
            String pointStr = passedString.substring(9, passedString.length());
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

                // mettere i punti trovati in un array per poi creare il polygon associato
                double x = Double.parseDouble(first);
                double y = Double.parseDouble(second);
                Point point = new Point(x, y);


                if (j == 0) {
                    polygon.startPath(point);
                } else {
                    polygon.lineTo(point);
                }
            }
            Polygon webPolygon = (Polygon) GeometryEngine.project(polygon, input, output);
            Log.d("THREADS", "Thread " + id + " returning.");
            return webPolygon;
        }
    }

    class GetSinglePolyline implements Callable<Polyline> {

        private int id;
        private Polyline polyline;
        private String passedString;

        public GetSinglePolyline(String passedString, int id) {
            this.passedString = passedString;
            this.id = id;
            polyline = new Polyline();
        }

        @Override
        public Polyline call() throws java.lang.Exception {
            Log.d("THREADS", "Thread " + id + " started.");
            SpatialReference input = SpatialReference.create(3003);
            SpatialReference output = SpatialReference.create(3857);
            String pointStr = passedString.substring(11, passedString.length());
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


                if (j == 0) {
                    polyline.startPath(point);
                } else {
                    polyline.lineTo(point);
                }
            }
            Polyline webPolyline = (Polyline) GeometryEngine.project(polyline, input, output);
            Log.d("THREADS", "Thread " + id + " returning.");
            return webPolyline;
        }
    }


    private class GetSinglePoint implements Callable<Point>{
        private int id;
        private Point point;
        private String passedString;

        public GetSinglePoint(String passedString, int id) {
            this.passedString = passedString;
            this.id = id;
            point = new Point();
        }


        @Override
        public Point call() throws java.lang.Exception {
            Log.d("THREADS", "Thread " + id + " started.");
            SpatialReference input = SpatialReference.create(3003);
            SpatialReference output = SpatialReference.create(3857);
            String temp = passedString.substring(6);
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

            point.setXY(x, y);
            Point webPoint = (Point) GeometryEngine.project(point, input, output);
            Log.d("THREADS", "Thread " + id + " returning.");
            return webPoint;
        }
    }
}

