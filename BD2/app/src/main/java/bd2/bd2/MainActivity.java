package bd2.bd2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jsqlite.Exception;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener{
Spinner spinner;
    DatabaseAccess databaseAccess = null;
    List<String> queryes=new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queryes.add("Scegli la query");
        queryes.add("Query1");
        queryes.add("Query2");
        queryes.add("Query3");

        try {
            databaseAccess = DatabaseAccess.getInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Query1Activity.class);
                startActivity(i);
            }
        });
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,queryes){

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = null;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                }
                else {
                    // Pass convertView as null to prevent reuse of special case views
                    v = super.getDropDownView(position, null, parent);
                }

                // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };

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
            case 1:
                intent = new Intent(MainActivity.this, Query1Activity.class);


                break;
            case 2:
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