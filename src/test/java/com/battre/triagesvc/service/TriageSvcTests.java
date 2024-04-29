package com.battre.triagesvc.service;

import com.battre.stubs.services.BatteryTypeTierPair;
import com.battre.stubs.services.GetRandomBatteryTypesRequest;
import com.battre.stubs.services.GetRandomBatteryTypesResponse;
import com.battre.stubs.services.OpsSvcGrpc;
import com.battre.stubs.services.ProcessIntakeBatteryOrderRequest;
import com.battre.stubs.services.ProcessIntakeBatteryOrderResponse;
import com.battre.stubs.services.SpecSvcGrpc;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

class TriageSvcTests {
    private static final Logger logger = Logger.getLogger(TriageSvcTests.class.getName());

    @Mock
    private SpecSvcGrpc.SpecSvcStub specSvcClient;
    @Mock
    private OpsSvcGrpc.OpsSvcStub opsSvcClient;

    private TriageSvc triageSvc;

    private AutoCloseable closeable;

    public static void mockGetRandomBatteryTypes(SpecSvcGrpc.SpecSvcStub specSvcClient, GetRandomBatteryTypesResponse response) {
        doAnswer(invocation -> {
            StreamObserver<GetRandomBatteryTypesResponse> observer = invocation.getArgument(1);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(specSvcClient).getRandomBatteryTypes(any(GetRandomBatteryTypesRequest.class), any());
    }

    public static void mockProcessIntakeBatteryOrder(OpsSvcGrpc.OpsSvcStub opsSvcClient, ProcessIntakeBatteryOrderResponse response) {
        doAnswer(invocation -> {
            StreamObserver<ProcessIntakeBatteryOrderResponse> observer = invocation.getArgument(1);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(opsSvcClient).processIntakeBatteryOrder(any(ProcessIntakeBatteryOrderRequest.class), any());
    }

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
        triageSvc = new TriageSvc(specSvcClient, opsSvcClient);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testQueryRandomBattery_Success() {
        List<BatteryTypeTierPair> expectedBatteryTypes = new ArrayList<>();
        expectedBatteryTypes.add(BatteryTypeTierPair.newBuilder().setBatteryTypeId(5).setBatteryTierId(3).build());
        expectedBatteryTypes.add(BatteryTypeTierPair.newBuilder().setBatteryTypeId(2).setBatteryTierId(1).build());

        GetRandomBatteryTypesResponse expectedSpecSvcResponse =
                GetRandomBatteryTypesResponse.newBuilder().addAllBatteries(expectedBatteryTypes).build();
        mockGetRandomBatteryTypes(specSvcClient, expectedSpecSvcResponse);

        List<BatteryTypeTierPair> actualResponse = triageSvc.queryRandomBatteryInfo(2);

        assertEquals(expectedBatteryTypes, actualResponse);
    }

    @Test
    void testQueryRandomBattery_Error() {
        CompletableFuture<GetRandomBatteryTypesResponse> responseFuture = new CompletableFuture<>();

        doAnswer((Answer<Void>) invocation -> {
            StreamObserver<GetRandomBatteryTypesResponse> observer = invocation.getArgument(1);
            observer.onError(new RuntimeException("Test Error"));
            responseFuture.completeExceptionally(new RuntimeException("Test Error"));
            return null;
        }).when(specSvcClient).getRandomBatteryTypes(any(), any());

        logger.info("TriageSvc errors expected to follow as part of test");
        List<BatteryTypeTierPair> actualResponse = triageSvc.queryRandomBatteryInfo(2);


        assertNull(actualResponse);
    }

    @Test
    void testQueryRandomBattery_Timeout() {
        doAnswer((Answer<Void>) invocation -> {
            StreamObserver<GetRandomBatteryTypesResponse> observer = invocation.getArgument(1);
            // No response will be sent, simulating timeout
            return null;
        }).when(specSvcClient).getRandomBatteryTypes(any(), any());

        logger.info("TriageSvc errors expected to follow as part of test");
        List<BatteryTypeTierPair> actualResponse = triageSvc.queryRandomBatteryInfo(2);

        assertNull(actualResponse);
    }

    @Test
    void testGenerateIntakeBatteryOrder_Success() {
        List<BatteryTypeTierPair> expectedBatteryTypes = new ArrayList<>();
        expectedBatteryTypes.add(BatteryTypeTierPair.newBuilder().setBatteryTypeId(5).setBatteryTierId(3).build());
        expectedBatteryTypes.add(BatteryTypeTierPair.newBuilder().setBatteryTypeId(2).setBatteryTierId(1).build());

        GetRandomBatteryTypesResponse expectedSpecSvcResponse =
                GetRandomBatteryTypesResponse.newBuilder().addAllBatteries(expectedBatteryTypes).build();
        mockGetRandomBatteryTypes(specSvcClient, expectedSpecSvcResponse);

        ProcessIntakeBatteryOrderResponse expectedOpsSvcResponse =
                ProcessIntakeBatteryOrderResponse.newBuilder().setSuccess(true).build();
        mockProcessIntakeBatteryOrder(opsSvcClient, expectedOpsSvcResponse);

        boolean actualResponse = triageSvc.generateIntakeBatteryOrder();

        assertTrue(actualResponse);
    }

    @Test
    void testGenerateIntakeBatteryOrder_Fail() {
        List<BatteryTypeTierPair> expectedBatteryTypes = new ArrayList<>();
        expectedBatteryTypes.add(BatteryTypeTierPair.newBuilder().setBatteryTypeId(5).setBatteryTierId(3).build());
        expectedBatteryTypes.add(BatteryTypeTierPair.newBuilder().setBatteryTypeId(2).setBatteryTierId(1).build());

        GetRandomBatteryTypesResponse expectedSpecSvcResponse =
                GetRandomBatteryTypesResponse.newBuilder().addAllBatteries(expectedBatteryTypes).build();
        mockGetRandomBatteryTypes(specSvcClient, expectedSpecSvcResponse);

        ProcessIntakeBatteryOrderResponse expectedOpsSvcResponse =
                ProcessIntakeBatteryOrderResponse.newBuilder().setSuccess(false).build();
        mockProcessIntakeBatteryOrder(opsSvcClient, expectedOpsSvcResponse);

        boolean actualResponse = triageSvc.generateIntakeBatteryOrder();

        assertFalse(actualResponse);
    }
}