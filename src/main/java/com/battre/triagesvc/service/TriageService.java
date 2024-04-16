package com.battre.triagesvc.service;

import com.battre.stubs.services.BatteryTypeTierCount;
import com.battre.stubs.services.BatteryTypeTierPair;
import com.battre.stubs.services.OpsSvcGrpc;
import com.battre.stubs.services.SpecSvcGrpc;
import com.battre.stubs.services.BatteryTypeTierCountRequest;
import com.battre.stubs.services.OpsSvcEmptyResponse;
import com.battre.stubs.services.GetRandomBatteryTypesRequest;
import com.battre.stubs.services.GetRandomBatteryTypesResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TriageService {

    private static final Logger logger = Logger.getLogger(TriageService.class.getName());

    @GrpcClient("specSvc")
    private SpecSvcGrpc.SpecSvcStub specSvcClient;
    @GrpcClient("opsSvc")
    private OpsSvcGrpc.OpsSvcStub opsSvcClient;

    @Autowired
    public TriageService(){}

    // Used for mocking spec svc client in tests
    public TriageService(SpecSvcGrpc.SpecSvcStub specSvcClient, OpsSvcGrpc.OpsSvcStub opsSvcClient){
        this.specSvcClient = specSvcClient;
        this.opsSvcClient = opsSvcClient;
    }

    public void generateIntakeBatteryOrder(){
        Random random = new Random();

        //Randomly decide # of battery types to include
        int numBatteryTypes = random.nextInt(3) + 1;

        List<BatteryTypeTierPair> batteryTypeTierInfo = queryRandomBatteryInfo(numBatteryTypes);

        List<BatteryTypeTierCount> batteryTypeTierCountInfo =
                batteryTypeTierInfo.stream()
                .map(batteryTypeTierEntry -> BatteryTypeTierCount.newBuilder()
                        .setBatteryType(batteryTypeTierEntry.getBatteryTypeId())
                        .setBatteryTier(batteryTypeTierEntry.getBatteryTierId())
                        .setBatteryCount(random.nextInt(2) + 1)
                        .build())
                .collect(Collectors.toList());

        BatteryTypeTierCountRequest.Builder batteryTypeCountRequestBuilder = BatteryTypeTierCountRequest.newBuilder();
        batteryTypeCountRequestBuilder.addAllBatteries(batteryTypeTierCountInfo);

        // Create a StreamObserver to handle the call asynchronously
        StreamObserver<OpsSvcEmptyResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(OpsSvcEmptyResponse response) {
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors
                logger.severe("processIntakeBatteryOrder() errored: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("processIntakeBatteryOrder() completed");
            }
        };

        opsSvcClient.processIntakeBatteryOrder(batteryTypeCountRequestBuilder.build(), responseObserver);
    }

    public List<BatteryTypeTierPair> queryRandomBatteryInfo(int numBatteryTypes) {
        GetRandomBatteryTypesRequest request = GetRandomBatteryTypesRequest
                .newBuilder()
                .setNumBatteryTypes(numBatteryTypes)
                .build();

        CompletableFuture<GetRandomBatteryTypesResponse> responseFuture = new CompletableFuture<>();

        // Create a StreamObserver to handle the response asynchronously
        StreamObserver<GetRandomBatteryTypesResponse> responseObserver = new StreamObserver<>() {

            @Override
            public void onNext(GetRandomBatteryTypesResponse response) {
                // Ensures only one response is returned in the stream
                responseFuture.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors
                logger.severe("getRandomBatteryTypes() errored: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // Handle the completion
                logger.info("getRandomBatteryTypes() completed");
            }
        };

        specSvcClient.getRandomBatteryTypes(request, responseObserver);

        List<BatteryTypeTierPair> batteryTypes = null;
        // Wait for the response or 1 sec handle timeout
        try {
            // Blocks until the response is available
            batteryTypes = responseFuture.get(5, TimeUnit.SECONDS).getPairsList();
            logger.info("getRandomBatteryTypes responseFuture response: " + batteryTypes);
        } catch (Exception e) {
            logger.severe("getRandomBatteryTypes responseFuture error: " + e.getMessage());
        }

        return batteryTypes;
    }
}