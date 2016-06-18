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
        import android.util.Log;

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
    private SQLiteOpenHelper openHelper;
    private Database database;
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