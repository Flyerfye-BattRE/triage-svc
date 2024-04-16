package com.battre.triagesvc.service;

import com.battre.stubs.services.GenerateIntakeBatteryOrderRequest;
import com.battre.stubs.services.GenerateIntakeBatteryOrderResponse;
import com.battre.stubs.services.TriageSvcGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.logging.Logger;

@GrpcService
public class TriageServiceImpl extends TriageSvcGrpc.TriageSvcImplBase {
    private static final Logger logger = Logger.getLogger(TriageServiceImpl.class.getName());
    private final TriageService triageService;

    @Autowired
    public TriageServiceImpl(TriageService triageService) {
        this.triageService = triageService;
    }

    @Override
    public void generateIntakeBatteryOrder(GenerateIntakeBatteryOrderRequest request, StreamObserver<GenerateIntakeBatteryOrderResponse> responseObserver){
        logger.info("generateIntakeBatteryOrder() invoked");
        
        boolean status = triageService.generateIntakeBatteryOrder();
        GenerateIntakeBatteryOrderResponse response = GenerateIntakeBatteryOrderResponse.newBuilder()
                .setSuccess(status)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("generateIntakeBatteryOrder() finished");
    }
}
