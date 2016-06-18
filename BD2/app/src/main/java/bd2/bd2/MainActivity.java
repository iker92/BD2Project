package bd2.bd2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jsqlite.Exception;


public class MainActivity extends Activity{

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView result=(TextView) findViewById(R.id.result);

        this.listView = (ListView) findViewById(R.id.listView);
        DatabaseAccess databaseAccess = null;
        try {
            databaseAccess = DatabaseAccess.getInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String quotes=databaseAccess.queryTableSimple();
        try {
            databaseAccess.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

      result.setText(quotes);
    }
}
//crilly