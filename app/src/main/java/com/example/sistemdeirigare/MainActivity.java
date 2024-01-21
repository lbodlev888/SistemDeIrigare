package com.example.sistemdeirigare;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    server serverConnection;
    boolean isConnected = false, pompIsOn = false, autoIsOn = false;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textGetIPAddress = findViewById(R.id.host);
        db = getBaseContext().openOrCreateDatabase("hosts.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS data (id INTEGER PRIMARY KEY AUTOINCREMENT, ip TEXT)");
        db.execSQL("INSERT OR IGNORE INTO data VALUES (0, 'http://192.168.1.1')");
        Cursor query = db.rawQuery("SELECT * FROM data", null);
        while (query.moveToNext())
            textGetIPAddress.setText(query.getString(1));
        query.close();
        db.close();

        Button connect = findViewById(R.id.connect);
        Button changeStatusPomp = findViewById(R.id.changeStatusPomp);
        Button changeStatusAuto = findViewById(R.id.changeStatusAuto);

        connect.setOnClickListener(v -> {
            if(!isConnected) {
                serverConnection = new server(textGetIPAddress.getText().toString(), R.id.result, this);
                if(serverConnection.getData()) {
                    connect.setText(getResources().getString(R.string.textBtnDisconnect));
                    textGetIPAddress.setEnabled(false);
                    isConnected = true;
                    Toast.makeText(this, "Conectat cu succes", Toast.LENGTH_SHORT).show();
                    db = getBaseContext().openOrCreateDatabase("hosts.db", MODE_PRIVATE, null);
                    db.execSQL("UPDATE data SET ip='" + textGetIPAddress.getText().toString() + "' WHERE id=0");
                    db.close();
                }
                else Toast.makeText(this, "Nu s-a putut conecta", Toast.LENGTH_SHORT).show();
            }
            else {
                connect.setText(getResources().getString(R.string.textBtnConnect));
                textGetIPAddress.setEnabled(true);
                isConnected = false;
                serverConnection = null;
                Toast.makeText(this, "Deconectat", Toast.LENGTH_SHORT).show();
            }
        });

        changeStatusPomp.setOnClickListener(v -> {
            if(serverConnection == null) {
                Toast.makeText(this, "Nu sunteti conectat", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!pompIsOn) {
                serverConnection.sendData("pomp", "yes");
                changeStatusPomp.setText(getResources().getString(R.string.changePompAutoToOff));
                TextView txt = findViewById(R.id.checkStatusPomp);
                txt.setText(getResources().getString(R.string.pompIsOn));
                pompIsOn = true;
            }
            else {
                serverConnection.sendData("pomp", "no");
                changeStatusPomp.setText(getResources().getString(R.string.changePompAutoToOn));
                TextView txt = findViewById(R.id.checkStatusPomp);
                txt.setText(R.string.pompIsOff);
                pompIsOn = false;
            }
        });

        changeStatusAuto.setOnClickListener(v -> {
            if(serverConnection == null) {
                Toast.makeText(this, "Nu sunteti conectat", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!autoIsOn) {
                serverConnection.sendData("auto", "yes");
                changeStatusAuto.setText(getResources().getString(R.string.changePompAutoToOff));
                TextView txt = findViewById(R.id.checkStatusAuto);
                txt.setText(getResources().getString(R.string.autoIsOn));
                autoIsOn = true;
            }
            else {
                serverConnection.sendData("auto", "no");
                changeStatusAuto.setText(getResources().getString(R.string.changePompAutoToOn));
                TextView txt = findViewById(R.id.checkStatusAuto);
                txt.setText(getResources().getString(R.string.autoIsOff));
                autoIsOn = false;
            }
        });
    }
}