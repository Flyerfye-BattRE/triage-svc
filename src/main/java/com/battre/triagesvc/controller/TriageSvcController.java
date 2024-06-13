package com.battre.triagesvc.controller;

import com.battre.stubs.services.GenerateIntakeBatteryOrderRequest;
import com.battre.stubs.services.GenerateIntakeBatteryOrderResponse;
import com.battre.stubs.services.TriageSvcGrpc;
import com.battre.triagesvc.enums.GenerateOrderStatusEnum;
import com.battre.triagesvc.service.TriageSvc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.logging.Logger;

@GrpcService
public class TriageSvcController extends TriageSvcGrpc.TriageSvcImplBase {
    private static final Logger logger = Logger.getLogger(TriageSvcController.class.getName());
    private final TriageSvc triageSvc;

    @Autowired
    public TriageSvcController(TriageSvc triageSvc) {
        this.triageSvc = triageSvc;
    }

    @Override
    public void generateIntakeBatteryOrder(GenerateIntakeBatteryOrderRequest request, StreamObserver<GenerateIntakeBatteryOrderResponse> responseObserver) {
        logger.info("generateIntakeBatteryOrder() invoked");

        GenerateOrderStatusEnum status = triageSvc.generateIntakeBatteryOrder();
        GenerateIntakeBatteryOrderResponse response = GenerateIntakeBatteryOrderResponse.newBuilder()
                .setSuccess(status == GenerateOrderStatusEnum.SUCCESS ? true : false)
                .setStatus(status.getgrpcStatus())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("generateIntakeBatteryOrder() finished");
    }
}
