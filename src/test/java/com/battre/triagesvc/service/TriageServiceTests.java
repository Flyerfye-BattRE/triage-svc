package com.battre.triagesvc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.battre.stubs.services.BatteryTypeTierPair;
import com.battre.stubs.services.GetRandomBatteryTypesResponse;
import com.battre.stubs.services.OpsSvcGrpc;
import com.battre.stubs.services.SpecSvcGrpc;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

class TriageServiceTests {
    private static final Logger logger = Logger.getLogger(TriageServiceTests.class.getName());

    @Mock
    private SpecSvcGrpc.SpecSvcStub specSvcClient;
    @Mock
    private OpsSvcGrpc.OpsSvcStub opsSvcClient;

    private TriageService triageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        triageService = new TriageService(specSvcClient, opsSvcClient);
    }

    @Test
    void testQueryRandomBattery_Success() {
        List<BatteryTypeTierPair> expectedBatteryTypes = new ArrayList<>();
        expectedBatteryTypes.add(BatteryTypeTierPair.newBuilder().setBatteryTypeId(5).setBatteryTierId(3).build());
        expectedBatteryTypes.add(BatteryTypeTierPair.newBuilder().setBatteryTypeId(2).setBatteryTierId(1).build());

        doAnswer((Answer<Void>) invocation -> {
            // Get the StreamObserver passed as an argument
            StreamObserver<GetRandomBatteryTypesResponse> observer = invocation.getArgument(1);

            // Create a response
            GetRandomBatteryTypesResponse response = GetRandomBatteryTypesResponse.newBuilder().addAllPairs(expectedBatteryTypes).build();

            // Pass the response to the StreamObserver
            observer.onNext(response);
            observer.onCompleted();

            return null;
        }).when(specSvcClient).getRandomBatteryTypes(any(), any());

        List<BatteryTypeTierPair> actualResponse = triageService.queryRandomBatteryInfo(2);

        assertEquals(expectedBatteryTypes, actualResponse);
    }

    @Test
    void testQueryRandomBattery_Error() {
        CompletableFuture<GetRandomBatteryTypesResponse> responseFuture = new CompletableFuture<>();

        doAnswer((Answer<Void>) invocation -> {
            // Get the StreamObserver passed as an argument
            StreamObserver<GetRandomBatteryTypesResponse> observer = invocation.getArgument(1);

            // Simulate an error by calling onError on the observer
            observer.onError(new RuntimeException("Test Error"));

            // Complete the CompletableFuture with an exception
            responseFuture.completeExceptionally(new RuntimeException("Test Error"));

            return null;
        }).when(specSvcClient).getRandomBatteryTypes(any(), any());

        logger.info("TriageSvc errors expected to follow as part of test");
        List<BatteryTypeTierPair> actualResponse = triageService.queryRandomBatteryInfo(2);


        assertEquals(null, actualResponse);
    }

    @Test
    void testQueryRandomBattery_Timeout() {
        doAnswer((Answer<Void>) invocation -> {
            // Get the StreamObserver passed as an argument
            StreamObserver<GetRandomBatteryTypesResponse> observer = invocation.getArgument(1);

            // No response will be sent, simulating timeout
            return null;
        }).when(specSvcClient).getRandomBatteryTypes(any(), any());

        logger.info("TriageSvc errors expected to follow as part of test");
        List<BatteryTypeTierPair> actualResponse = triageService.queryRandomBatteryInfo(2);

        assertEquals(null, actualResponse);
    }
}