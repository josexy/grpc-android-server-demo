package com.joxrays.simplerpcserver.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class FileServiceImpl extends FileServiceGrpc.FileServiceImplBase {

    @Override
    public StreamObserver<Bytes> uploadFile(StreamObserver<RespFileInfo> responseObserver) {
        return new UploadStreamObserver(responseObserver);
    }

    @Override
    public void downloadFile(ReqFilePath request, StreamObserver<Bytes> responseObserver) {
        File file = new File(request.getValue());
        if (!file.exists()) {
            responseObserver.onError(Status.INTERNAL.withDescription("file not found!").asRuntimeException());
            return;
        }
        InputStream in;
        try {
            in = new FileInputStream(file);
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
            return;
        }
        // 发送数据流给客户端
        StreamHandler handler = new StreamHandler(in);
        Exception ex = handler.handle(bytes -> {
            responseObserver.onNext(Bytes.newBuilder().setValue(bytes).build());
        });
        if (ex != null) ex.printStackTrace();

        responseObserver.onCompleted();
    }

    @Override
    public void sayHello(ReqHello request, StreamObserver<RespHello> responseObserver) {
        if (request.getValue().isEmpty()) {
            responseObserver.onError(Status.INTERNAL.withDescription("message is empty!").asRuntimeException());
            return;
        }
        responseObserver.onNext(RespHello.newBuilder()
                .setValue("reply: " + request.getValue())
                .build());
        responseObserver.onCompleted();
    }
}
