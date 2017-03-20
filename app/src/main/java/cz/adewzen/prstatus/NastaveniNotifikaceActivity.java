package cz.adewzen.prstatus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class NastaveniNotifikaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nastaveni_notifikace);

        Intent intent = getIntent();
        final String parkIdString = intent.getStringExtra("cz.adewzen.prstatus.selectedParkId");

        Log.i("receiverd",parkIdString);
        Parkoviste parkoviste = MainActivity.parkoviste.get(parkIdString);

        setTitle(parkoviste.name + " - Notifikace");

        ((CheckBox)findViewById(R.id.checkBox_notifikace)).setChecked(parkoviste.notify);
        ((EditText)findViewById(R.id.editText)).setText(String.valueOf(parkoviste.limit));
        if (parkoviste.limitType.equals("procent")) {
            ((Spinner)findViewById(R.id.spinner)).setSelection(0);
        } else {
            ((Spinner)findViewById(R.id.spinner)).setSelection(1);
        }



        Spinner dropdown = (Spinner)findViewById(R.id.spinner);
        String[] items = new String[]{"procent", "míst"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        final Button button = (Button) findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("saving " , parkIdString);
                Parkoviste parkoviste = MainActivity.parkoviste.get(parkIdString);
                parkoviste.notify =   ((CheckBox)findViewById(R.id.checkBox_notifikace)).isChecked();
                Log.i("limit", ((EditText)findViewById(R.id.editText)).getText().toString());
                parkoviste.limit = Integer.parseInt(((EditText)findViewById(R.id.editText)).getText().toString());
                Log.i("limit", String.valueOf(parkoviste.limit));
                parkoviste.limitType = ((Spinner)findViewById(R.id.spinner)).getSelectedItem().toString();

                MainActivity.parkoviste.put(parkIdString,parkoviste);
                Log.i("saved " , parkIdString);

                Intent intent = new Intent(MainActivity.getAppContext(), ParkDetailActivity.class);

                intent.putExtra("cz.adewzen.prstatus.selectedParkId", parkIdString);

                startActivity(intent);
            }
        });


    }
}
