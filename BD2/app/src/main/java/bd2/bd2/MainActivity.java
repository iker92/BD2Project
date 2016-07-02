package bd2.bd2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jsqlite.Exception;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener{

    Spinner spinner;
    DatabaseAccess databaseAccess = null;
    List<String> queryes = new ArrayList<>();
    TextView testo;
    static int position;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queryes.add("Scegli la query");
        queryes.add("Query1");
        queryes.add("Query2");
        queryes.add("Query3");
        queryes.add("Query4");
        queryes.add("Query5");
        queryes.add("Query6");

        try {
            databaseAccess = DatabaseAccess.getInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Button button = (Button) findViewById(R.id.button);

         final EditText editText = (EditText)findViewById(R.id.edittext);
        testo = (TextView) findViewById(R.id.testo);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = null;
                Context context = getApplicationContext();
                CharSequence text = "Nessun elemento selezionato!";
                int duration = Toast.LENGTH_SHORT;
                name = editText.getText().toString();
                switch(position)
                {
                    case 0:
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, Query1Activity.class);

                        intent.putExtra("name",name);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, Query2Activity.class);

                        intent.putExtra("name",name);
                        break;
                    case 3:
                        intent = new Intent(MainActivity.this, Query3Activity.class);

                        intent.putExtra("name",name);
                        break;
                    case 4:
                        intent = new Intent(MainActivity.this, Query4Activity.class);

                        intent.putExtra("name",name);
                        break;
                    case 5:
                        intent = new Intent(MainActivity.this, Query5Activity.class);
                        intent.putExtra("name",name);
                        break;
                    case 6:
                        intent=new Intent(MainActivity.this,Query6Activity.class);
                        intent.putExtra("name",name);
                }
                if(intent != null)
                {
                    startActivity(intent);
                }
            }
        });

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,queryes){

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v;

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

    /*protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.inputdialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        name=editText.getText().toString();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();


    }
*/
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

        position = adapterView.getSelectedItemPosition();


        switch(position) {
            case 1:

                testo.setText("Questa query prende in input il nome di un comune e restituisce i comuni confinanti");

                break;
            case 2:
                testo.setText("Questa query prende in input il nome di un parco e restituisce i comuni confinanti");

                break;
            case 3:

                testo.setText("Questa query prende in input il nome di un fiume e restituisce le intersezioni con le strade");

                break;
            case 4:

                testo.setText("Questa query prende in input il nome di un comune e restituisce le strade passanti per il comune");

                break;
            case 5:

                testo.setText("Questa query prende in input il nome di un parco e restituisce i comuni confinanti e le strade che passano per più di un comune confinante col parco");

                break;
            case 6:

                testo.setText("Questa query prende in input il nome di un parco e restituisce i comuni completamente dentro il parco");

                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {


    }

}