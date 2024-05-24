package com.battre.triagesvc.service;

import com.battre.grpcifc.GrpcMethodInvoker;
import com.battre.stubs.services.BatteryTypeTierPair;
import com.battre.stubs.services.GetRandomBatteryTypesRequest;
import com.battre.stubs.services.GetRandomBatteryTypesResponse;
import com.battre.stubs.services.ProcessIntakeBatteryOrderRequest;
import com.battre.stubs.services.ProcessIntakeBatteryOrderResponse;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

class TriageSvcTests {
    private static final Logger logger = Logger.getLogger(TriageSvcTests.class.getName());

    @Mock
    private GrpcMethodInvoker grpcMethodInvoker;

    private TriageSvc triageSvc;

    private AutoCloseable closeable;

    public void mockGetRandomBatteryTypes(GetRandomBatteryTypesResponse response) {
        doAnswer(invocation -> {
            StreamObserver<GetRandomBatteryTypesResponse> observer = invocation.getArgument(3);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(grpcMethodInvoker).callMethod(
                eq("specsvc"),
                eq("getRandomBatteryTypes"),
                any(GetRandomBatteryTypesRequest.class),
                any(StreamObserver.class)
        );
    }

    public void mockProcessIntakeBatteryOrder(ProcessIntakeBatteryOrderResponse response) {
        doAnswer(invocation -> {
            StreamObserver<ProcessIntakeBatteryOrderResponse> observer = invocation.getArgument(3);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(grpcMethodInvoker).callMethod(
                eq("opssvc"),
                eq("processIntakeBatteryOrder"),
                any(ProcessIntakeBatteryOrderRequest.class),
                any(StreamObserver.class)
        );
    }

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
        triageSvc = new TriageSvc(grpcMethodInvoker);
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
        mockGetRandomBatteryTypes(expectedSpecSvcResponse);

        List<BatteryTypeTierPair> actualResponse = triageSvc.queryRandomBatteryInfo(2);

        assertEquals(expectedBatteryTypes, actualResponse);
    }

    @Test
    void testQueryRandomBattery_Error() {
        CompletableFuture<GetRandomBatteryTypesResponse> responseFuture = new CompletableFuture<>();

        doAnswer((Answer<Void>) invocation -> {
            StreamObserver<GetRandomBatteryTypesResponse> observer = invocation.getArgument(3);
            observer.onError(new RuntimeException("Test Error"));
            responseFuture.completeExceptionally(new RuntimeException("Test Error"));
            return null;
        }).when(grpcMethodInvoker).callMethod(
                eq("specsvc"),
                eq("getRandomBatteryTypes"),
                any(),
                any()
        );

        logger.info("TriageSvc errors expected to follow as part of test");
        List<BatteryTypeTierPair> actualResponse = triageSvc.queryRandomBatteryInfo(2);

        assertNull(actualResponse);
    }

    @Test
    void testQueryRandomBattery_Timeout() {
        doAnswer((Answer<Void>) invocation -> {
            StreamObserver<GetRandomBatteryTypesResponse> observer = invocation.getArgument(3);
            // No response will be sent, simulating timeout
            return null;
        }).when(grpcMethodInvoker).callMethod(
                eq("specsvc"),
                eq("getRandomBatteryTypes"),
                any(),
                any()
        );

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
        mockGetRandomBatteryTypes(expectedSpecSvcResponse);

        ProcessIntakeBatteryOrderResponse expectedOpsSvcResponse =
                ProcessIntakeBatteryOrderResponse.newBuilder().setSuccess(true).build();
        mockProcessIntakeBatteryOrder(expectedOpsSvcResponse);

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
        mockGetRandomBatteryTypes(expectedSpecSvcResponse);

        ProcessIntakeBatteryOrderResponse expectedOpsSvcResponse =
                ProcessIntakeBatteryOrderResponse.newBuilder().setSuccess(false).build();
        mockProcessIntakeBatteryOrder(expectedOpsSvcResponse);

        boolean actualResponse = triageSvc.generateIntakeBatteryOrder();

        assertFalse(actualResponse);
    }
}