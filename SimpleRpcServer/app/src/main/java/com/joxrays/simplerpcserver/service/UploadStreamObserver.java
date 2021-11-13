package com.joxrays.simplerpcserver.service;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.grpc.stub.StreamObserver;

public class UploadStreamObserver implements StreamObserver<Bytes> {
    private static final String TAG = "UploadStreamObserver";
    private final StreamObserver<RespFileInfo> responseObserver;
    private final File file;
    private FileOutputStream outputStream;
    private Exception exception;
    private long size;

    public UploadStreamObserver(StreamObserver<RespFileInfo> streamObserver) {
        this.responseObserver = streamObserver;
        file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
                "upload_file");
        Log.d(TAG, "file: " + file);
    }

    private void writeToFile(ByteBuffer buffer) throws IOException {
        Log.d(TAG, "write bytes: " + buffer.remaining());
        size += buffer.remaining();
        outputStream.write(buffer.array(), buffer.position(), buffer.limit());
    }

    @Override
    public void onNext(Bytes value) {
        ByteBuffer buffer = ByteBuffer.wrap(value.getValue().toByteArray());
        if (outputStream == null) {
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                outputStream = new FileOutputStream(file);
            } catch (Exception ex) {
                exception = ex;
                return;
            }
        }
        try {
            writeToFile(buffer);
        } catch (Exception ex) {
            exception = ex;
        }
    }

    @Override
    public void onError(Throwable t) {
    }

    @Override
    public void onCompleted() {
        if (exception != null) {
            exception.printStackTrace();
        }

        responseObserver.onNext(RespFileInfo.newBuilder()
                .setName(file.getName())
                .setSize(size)
                .build());

        responseObserver.onCompleted();

        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}