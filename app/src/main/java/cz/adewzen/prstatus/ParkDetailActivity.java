package cz.adewzen.prstatus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ParkDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_detail);

        Intent intent = getIntent();
        String message = intent.getStringExtra("cz.adewzen.prstatus.selectedParkId");

        Log.i("receiverd",message);
        Parkoviste parkoviste = getParkovise(message);

        setTitle(parkoviste.name);

        TextView text = (TextView)findViewById(R.id.textView);
        text.setText("Obsazeno: " + parkoviste.obsazeno + " (" + Math.round(parkoviste.obsazeno*100/parkoviste.capacity) + "%)");
        text = (TextView)findViewById(R.id.textView4);
        text.setText("Volne: " + parkoviste.free + " (" + Math.round(parkoviste.free*100/parkoviste.capacity) + "%)");
        text = (TextView)findViewById(R.id.textView5);
        text.setText("Celkem: " + parkoviste.capacity);
        text = (TextView)findViewById(R.id.textView6);
        text.setText("Posledni aktualizace: " + parkoviste.lastUpdateDate);
    }

    protected Parkoviste getParkovise(String parkId) {
        for (Parkoviste park : MainActivity.parkoviste) {
            if (park.parkId.equals(parkId)) return park;
        }
        return null;
    }
}
