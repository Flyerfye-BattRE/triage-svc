package com.battre.triagesvc.service;

import com.battre.grpcifc.GrpcMethodInvoker;
import com.battre.stubs.services.BatteryTypeTierCount;
import com.battre.stubs.services.BatteryTypeTierPair;
import com.battre.stubs.services.GetRandomBatteryTypesRequest;
import com.battre.stubs.services.GetRandomBatteryTypesResponse;
import com.battre.stubs.services.ProcessIntakeBatteryOrderRequest;
import com.battre.stubs.services.ProcessIntakeBatteryOrderResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class TriageSvc {

    private static final Logger logger = Logger.getLogger(TriageSvc.class.getName());
    private final GrpcMethodInvoker grpcMethodInvoker;
    private final Random random;

    @Autowired
    public TriageSvc(GrpcMethodInvoker grpcMethodInvoker) {
        this.grpcMethodInvoker = grpcMethodInvoker;
        this.random = new Random();
    }

    public boolean generateIntakeBatteryOrder() {
        //Randomly decide # of battery types to include
        int numBatteryTypes = random.nextInt(3) + 1;

        List<BatteryTypeTierPair> batteryTypeTierInfo = queryRandomBatteryInfo(numBatteryTypes);

        if (!batteryTypeTierInfo.isEmpty()) {
            return processOrder(batteryTypeTierInfo);
        } else {
            return false;
        }
    }

    private boolean processOrder(List<BatteryTypeTierPair> batteryTypeTierInfo) {
        List<BatteryTypeTierCount> batteryTypeTierCountInfo =
                batteryTypeTierInfo.stream()
                        .map(batteryTypeTierEntry -> BatteryTypeTierCount.newBuilder()
                                .setBatteryType(batteryTypeTierEntry.getBatteryTypeId())
                                .setBatteryTier(batteryTypeTierEntry.getBatteryTierId())
                                .setBatteryCount(random.nextInt(2) + 1)
                                .build())
                        .collect(Collectors.toList());

        ProcessIntakeBatteryOrderRequest request = ProcessIntakeBatteryOrderRequest.newBuilder()
                .addAllBatteries(batteryTypeTierCountInfo)
                .build();

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

        grpcMethodInvoker.callMethod(
                "opssvc",
                "processIntakeBatteryOrder",
                request,
                responseObserver
        );

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
        GetRandomBatteryTypesRequest request = GetRandomBatteryTypesRequest.newBuilder()
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

        grpcMethodInvoker.callMethod("specsvc", "getRandomBatteryTypes", request, responseObserver);

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