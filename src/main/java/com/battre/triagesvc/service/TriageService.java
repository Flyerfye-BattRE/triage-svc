package com.battre.triagesvc.service;

import com.battre.stubs.services.BatteryTypeTierCount;
import com.battre.stubs.services.BatteryTypeTierPair;
import com.battre.stubs.services.OpsSvcGrpc;
import com.battre.stubs.services.SpecSvcGrpc;
import com.battre.stubs.services.ProcessIntakeBatteryOrderRequest;
import com.battre.stubs.services.ProcessIntakeBatteryOrderResponse;
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

    private Random random;

    @GrpcClient("specSvc")
    private SpecSvcGrpc.SpecSvcStub specSvcClient;
    @GrpcClient("opsSvc")
    private OpsSvcGrpc.OpsSvcStub opsSvcClient;

    @Autowired
    public TriageService(){
        this.random = new Random();
    }

    // Used for mocking spec svc client in tests
    public TriageService(SpecSvcGrpc.SpecSvcStub specSvcClient, OpsSvcGrpc.OpsSvcStub opsSvcClient){
        this.specSvcClient = specSvcClient;
        this.opsSvcClient = opsSvcClient;
        this.random = new Random();
    }

    public boolean generateIntakeBatteryOrder(){
        //Randomly decide # of battery types to include
        int numBatteryTypes = random.nextInt(3) + 1;

        List<BatteryTypeTierPair> batteryTypeTierInfo = queryRandomBatteryInfo(numBatteryTypes);

        return processOrder(batteryTypeTierInfo);
    }

    public boolean processOrder(List<BatteryTypeTierPair> batteryTypeTierInfo) {
        List<BatteryTypeTierCount> batteryTypeTierCountInfo =
                batteryTypeTierInfo.stream()
                        .map(batteryTypeTierEntry -> BatteryTypeTierCount.newBuilder()
                                .setBatteryType(batteryTypeTierEntry.getBatteryTypeId())
                                .setBatteryTier(batteryTypeTierEntry.getBatteryTierId())
                                .setBatteryCount(random.nextInt(2) + 1)
                                .build())
                        .collect(Collectors.toList());

        ProcessIntakeBatteryOrderRequest.Builder processIntakeBatteryOrderRequestBuilder = ProcessIntakeBatteryOrderRequest.newBuilder();
        processIntakeBatteryOrderRequestBuilder.addAllBatteries(batteryTypeTierCountInfo);


        CompletableFuture<ProcessIntakeBatteryOrderResponse> responseFuture = new CompletableFuture<>();
        StreamObserver<ProcessIntakeBatteryOrderResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ProcessIntakeBatteryOrderResponse response) {
                responseFuture.complete(response);
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

        opsSvcClient.processIntakeBatteryOrder(processIntakeBatteryOrderRequestBuilder.build(), responseObserver);

        boolean result = false;
        // Wait for the response or 1 sec handle timeout
        try {
            // Blocks until the response is available
            result = responseFuture.get(5, TimeUnit.SECONDS).getSuccess();
            logger.info("processIntakeBatteryOrder() responseFuture response: " + result);
        } catch (Exception e) {
            logger.severe("processIntakeBatteryOrder() responseFuture error: " + e.getMessage());
        }

        return result;
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
            batteryTypes = responseFuture.get(5, TimeUnit.SECONDS).getBatteriesList();
            logger.info("getRandomBatteryTypes() responseFuture response: " + batteryTypes);
        } catch (Exception e) {
            logger.severe("getRandomBatteryTypes() responseFuture error: " + e.getMessage());
        }

        return batteryTypes;
    }
}