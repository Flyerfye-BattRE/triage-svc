package com.battre.triagesvc.service;

import com.battre.stubs.services.TriageSvcEmptyRequest;
import com.battre.stubs.services.TriageSvcEmptyResponse;
import com.battre.stubs.services.TriageSvcGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.logging.Level;
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
    public void callSpecSvcRandBattery(TriageSvcEmptyRequest request, StreamObserver<TriageSvcEmptyResponse> responseObserver){
        logger.log(Level.INFO, "callSpecSvcRandBattery invoked");

        Context ctx = Context.current().fork();
        ctx.run(() -> {
            // Can start asynchronous work here that will not
            // be cancelled when method returns
            triageService.queryRandomBattery();
        });

        TriageSvcEmptyResponse myResponse = TriageSvcEmptyResponse.newBuilder()
                .build();

        responseObserver.onNext(myResponse);
        responseObserver.onCompleted();

        logger.log(Level.INFO, "callSpecSvcRandBattery finished");
    }
}
