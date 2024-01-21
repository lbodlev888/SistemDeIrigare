package com.example.sistemdeirigare;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.concurrent.atomic.AtomicBoolean;

public class server extends AppCompatActivity {
    private final String host;
    private final TextView result;
    server(String IPAddress, int storeResult, Context context)
    {
        this.host = IPAddress;
        result = ((Activity)context).findViewById(storeResult);
    }
    public boolean getData()
    {
        AtomicBoolean status = new AtomicBoolean(true);
        Thread t = new Thread(() -> {
            final StringBuilder builder = new StringBuilder();

            try {
                Document doc = Jsoup.connect(host).get();

                Element body =  doc.body();
                builder.append(body.text());

            } catch (Exception e) {
                builder.append("Error : ").append(e.getMessage()).append("\n");
                status.set(false);
            }
            runOnUiThread(() -> result.setText(builder.toString().replace(" $", "\n")));
        });
        t.start();
        try {
            t.join();
        }
        catch(InterruptedException e) {
            String msg = "Eroare: " + e.getMessage();
            result.setText(msg);
            status.set(false);
        }
        return status.get();
    }
    public void sendData(String key, String value)
    {
        Thread t = new Thread(() -> {
            final StringBuilder builder = new StringBuilder();

            try {
                Document doc = Jsoup.connect(host).data(key, value).get();

                Element body =  doc.body();
                builder.append(body.text());

            } catch (Exception e) {
                builder.append("Error : ").append(e.getMessage()).append("\n");
            }

            runOnUiThread(() -> result.setText(builder.toString().replace(" $", "\n")));
        });
        t.start();
        try {
            t.join();
        }
        catch(InterruptedException e) {
            String msg = "Eroare: " + e.getMessage();
            result.setText(msg);
        }
    }
}