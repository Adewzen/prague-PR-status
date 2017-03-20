package cz.adewzen.prstatus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ParkDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_detail);

        Intent intent = getIntent();
        final String parkIdString = intent.getStringExtra("cz.adewzen.prstatus.selectedParkId");

        Log.i("receiverd",parkIdString);
        Parkoviste parkoviste = MainActivity.parkoviste.get(parkIdString);

        setTitle(parkoviste.name);

        TextView text = (TextView)findViewById(R.id.textView);
        text.setText("Obsazeno: " + parkoviste.obsazeno + " (" + Math.round(parkoviste.obsazeno*100/parkoviste.capacity) + "%)");
        text = (TextView)findViewById(R.id.textView4);
        text.setText("Volne: " + parkoviste.free + " (" + Math.round(parkoviste.free*100/parkoviste.capacity) + "%)");
        text = (TextView)findViewById(R.id.textView5);
        text.setText("Celkem: " + parkoviste.capacity);
        text = (TextView)findViewById(R.id.textView6);
        text.setText("Posledni aktualizace: " + parkoviste.lastUpdateDate);

        final Button button = (Button) findViewById(R.id.button_set_notification);


        Log.i("limit",String.valueOf(parkoviste.limit));
        if (parkoviste.notify) {
            ((TextView)findViewById(R.id.textView9)).setText("Notifikce nastavena (< " + String.valueOf(parkoviste.limit) + "  " + parkoviste.limitType + ")");
            button.setText("Upravit Notifikaci");
        } else {
            ((TextView)findViewById(R.id.textView9)).setText("Notifikce vypnuta");
            button.setText("Nastavit Notifikaci");
        }


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.getAppContext(), NastaveniNotifikaceActivity.class);

                intent.putExtra("cz.adewzen.prstatus.selectedParkId", parkIdString);

                startActivity(intent);
            }
        });

    }


}
