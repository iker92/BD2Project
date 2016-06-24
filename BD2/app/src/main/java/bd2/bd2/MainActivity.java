package bd2.bd2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;

import jsqlite.Exception;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener{
Spinner spinner;
    DatabaseAccess databaseAccess = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            databaseAccess = DatabaseAccess.getInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.nomi_query, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            databaseAccess.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        Intent intent = null;
        int position=adapterView.getSelectedItemPosition();
        switch(position)
        {
            case 0:
                intent = new Intent(MainActivity.this, Query1Activity.class);


                break;
            case 1:
                intent = new Intent(MainActivity.this, Query2Activity.class);


                break;
        }
        if(intent != null)
        {
            startActivity(intent);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {


    }
}