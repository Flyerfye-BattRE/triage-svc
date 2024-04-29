package com.battre.triagesvc.service;

import com.battre.stubs.services.GenerateIntakeBatteryOrderRequest;
import com.battre.stubs.services.GenerateIntakeBatteryOrderResponse;
import com.battre.stubs.services.TriageSvcGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.logging.Logger;

@GrpcService
public class TriageSvcImpl extends TriageSvcGrpc.TriageSvcImplBase {
    private static final Logger logger = Logger.getLogger(TriageSvcImpl.class.getName());
    private final TriageSvc triageSvc;

    @Autowired
    public TriageSvcImpl(TriageSvc triageSvc) {
        this.triageSvc = triageSvc;
    }

    @Override
    public void generateIntakeBatteryOrder(GenerateIntakeBatteryOrderRequest request, StreamObserver<GenerateIntakeBatteryOrderResponse> responseObserver) {
        logger.info("generateIntakeBatteryOrder() invoked");

        boolean status = triageSvc.generateIntakeBatteryOrder();
        GenerateIntakeBatteryOrderResponse response = GenerateIntakeBatteryOrderResponse.newBuilder()
                .setSuccess(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("generateIntakeBatteryOrder() finished");
    }
}
