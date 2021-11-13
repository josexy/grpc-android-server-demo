package com.joxrays.simplerpcserver;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.joxrays.simplerpcserver.service.FileServiceImpl;

import java.util.concurrent.TimeUnit;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

public class RpcServerWorker extends Worker {
    public static final String TAG = "RpcServerWorker";
    private Server server;
    private final int port;

    public RpcServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        port = workerParams.getInputData().getInt("PORT", 7777);
    }

    private void startRpcServer() {
        server = NettyServerBuilder.forPort(port)
                .addService(new FileServiceImpl())
                .build();

        Log.d(TAG, "start rpc server");
        try {
            server.start().awaitTermination();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void stopRpcServer() {
        Log.d(TAG, "stop rpc server");
        if (server != null) {
            try {
                server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStopped() {
        stopRpcServer();
        super.onStopped();
    }

    @NonNull
    @Override
    public Result doWork() {
        startRpcServer();
        return Result.success();
    }
}
