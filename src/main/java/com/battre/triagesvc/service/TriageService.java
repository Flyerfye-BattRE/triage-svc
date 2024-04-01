package com.battre.triagesvc.service;


import com.battre.stubs.services.SpecSvcEmptyRequest;
import com.battre.stubs.services.SpecSvcResponse;
import com.battre.stubs.services.SpecSvcGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TriageService {

    private static final Logger logger = Logger.getLogger(TriageService.class.getName());

    @GrpcClient("specSvc")
    private SpecSvcGrpc.SpecSvcStub specSvcClient;

    @Autowired
    public TriageService(){}

    // Used for mocking spec svc client in tests
    public TriageService(SpecSvcGrpc.SpecSvcStub specSvcClient){
        this.specSvcClient = specSvcClient;
    }

    public String queryRandomBattery() {
        String batteryResponse = null;
        SpecSvcEmptyRequest request = SpecSvcEmptyRequest.newBuilder()
                .build();

        CompletableFuture<SpecSvcResponse> responseFuture = new CompletableFuture<>();

        // Create a StreamObserver to handle the response asynchronously
        StreamObserver<SpecSvcResponse> responseObserver = new StreamObserver<SpecSvcResponse>() {
            private SpecSvcResponse randomBatteryResponse;

            @Override
            public void onNext(SpecSvcResponse response) {
                // Ensures only one response is returned in the stream
                if (randomBatteryResponse == null) {
                    randomBatteryResponse = response;
                    responseFuture.complete(randomBatteryResponse);
                } else {
                    // If more than one response is received, handle the error
                    onError(new RuntimeException("More than one response received"));
                }
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors
                logger.log(Level.SEVERE, "TriageSvc errored: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // Handle the completion
                if (randomBatteryResponse == null) {
                    // Handle case where no response is received
                    logger.log(Level.SEVERE, "No response received");
                }
            }
        };

        specSvcClient.getRandomBatteryType(request, responseObserver);

        // Wait for the response or 1 sec handle timeout
        try {
            // Blocks until the response is available
            batteryResponse = responseFuture.get(1, TimeUnit.SECONDS).getResponse();
            logger.log(Level.INFO, "TriageSvc sees battery response: " + batteryResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error: " + e.getMessage());
        }

        return batteryResponse;
    }
}