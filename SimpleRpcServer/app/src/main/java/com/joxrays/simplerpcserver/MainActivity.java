package com.joxrays.simplerpcserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int PORT = 7777;

    private void startRpcWorkerService() {
        Data data = new Data.Builder().putInt("PORT", PORT).build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                RpcServerWorker.class,
                15, TimeUnit.MINUTES,
                15, TimeUnit.MINUTES)
                .setInputData(data)
                .build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(RpcServerWorker.TAG,
                        ExistingPeriodicWorkPolicy.REPLACE, request);

        Toast.makeText(this, "Start Rpc Server", Toast.LENGTH_SHORT).show();
    }

    private void stopRpcWorkerService() {
        WorkManager.getInstance(this).cancelUniqueWork(RpcServerWorker.TAG);

        Toast.makeText(this, "Stop Rpc Server", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_start_rpc_server).setOnClickListener(v -> {
            startRpcWorkerService();
        });

        findViewById(R.id.btn_stop_rpc_server).setOnClickListener(v -> {
            stopRpcWorkerService();
        });
    }
}