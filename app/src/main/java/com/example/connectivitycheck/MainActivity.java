package com.example.connectivitycheck;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView outputText;
    private TextView counterText;
    private ConnectivityManager connectivityManager;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = findViewById(R.id.outputText);
        counterText = findViewById(R.id.counterText);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        Button checkButton = findViewById(R.id.checkButton);
        checkButton.setOnClickListener(v -> checkInternetStatus());
    }

    private void checkInternetStatus() {
        StringBuilder sb = new StringBuilder();

        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

        if (capabilities != null) {
            sb.append("System APIs:\n");
            sb.append("***Validated***: ").append(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"True":"False").append("\n");
            sb.append("***Internet Access***: ").append(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)?"True":"False").append("\n\n");

            sb.append("1)   Transports: ").append("WIFI").append("\n");
            sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Connected":"-").append("\n\n");

            sb.append("2)   Transports: ").append("CELLULAR").append("\n");
            sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"Connected":"-").append("\n\n");

            sb.append("3)   Transports: ").append("VPN").append("\n");
            sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?"Connected":"-").append("\n\n");

            sb.append("4)   Transports: ").append("BLUETOOTH").append("\n");
            sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)?"Connected":"-").append("\n\n");

            sb.append("5)   Transports: ").append("ETHERNET").append("\n");
            sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)?"Connected":"-").append("\n\n");

            sb.append("6)   Transports: ").append("USB").append("\n");
            sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB)?"Connected":"-").append("\n\n");

            sb.append("7)   Transports: ").append("WIFI_AWARE").append("\n");
            sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)?"Connected":"-").append("\n\n");

            sb.append("8)   Transports: ").append("LOWPAN").append("\n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)?"Connected":"-").append("\n\n");
            } else {
                sb.append("\t\t\tStatus: Not supported (API < 27)\n\n");
            }

            sb.append("9)   Transports: ").append("SATELLITE").append("\n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_SATELLITE)?"Connected":"-").append("\n\n");
            } else {
                sb.append("\t\t\tStatus: Not supported (API < 30)\n\n");
            }

            sb.append("10)  Transports: ").append("THREAD").append("\n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                sb.append("\t\t\tStatus: ").append(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_THREAD)?"Connected":"-").append("\n\n");
            } else {
                sb.append("\t\t\tStatus: Not supported (API < 31)\n\n");
            }

            sb.append("\nManual checks:\n");

            // Run DNS + HTTP tests on background thread
            new Thread(() -> {
                boolean dnsWorks = isDNSWorking();
                boolean httpWorks = isHttpWorking();

                sb.append("DNS resolution \t(Google): ").append(dnsWorks ? "OK" : "FAILED").append("\n");
                sb.append("HTTP test (Browserstack): ").append(httpWorks ? "OK" : "FAILED").append("\n");

                mainHandler.post(() -> {
                    outputText.setText(sb.toString());
                    counterText.setText("");
                });

            }).start();
        }
        else {
            sb.append("\n\nNo network found!\n\n");
            outputText.setText(sb.toString());
        }

    }

    private boolean isDNSWorking() {
        try {
            InetAddress address = InetAddress.getByName("google.com");
            return address != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isHttpWorking() {
        try {
            URL url = new URL("https://browserstack.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.connect();
            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
}