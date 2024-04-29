package com.battre.triagesvc.service;

import com.battre.stubs.services.GenerateIntakeBatteryOrderRequest;
import com.battre.stubs.services.GenerateIntakeBatteryOrderResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TriageSvcImplTests {

    @Mock
    private TriageSvc triageSvc;

    @Mock
    private StreamObserver<GenerateIntakeBatteryOrderResponse> responseGenerateIntakeBatteryOrderResponse;

    private TriageSvcImpl triageSvcImpl;

    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
        triageSvcImpl = new TriageSvcImpl(triageSvc);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testCallSpecSvcRandBatterySuccess() {
        when(triageSvc.generateIntakeBatteryOrder()).thenReturn(true);
        GenerateIntakeBatteryOrderRequest request = GenerateIntakeBatteryOrderRequest.newBuilder().build();

        triageSvcImpl.generateIntakeBatteryOrder(request, responseGenerateIntakeBatteryOrderResponse);
        verify(triageSvc).generateIntakeBatteryOrder();
        verify(responseGenerateIntakeBatteryOrderResponse).onNext(GenerateIntakeBatteryOrderResponse.newBuilder().setSuccess(true).build());
        verify(responseGenerateIntakeBatteryOrderResponse).onCompleted();
    }

    @Test
    void testCallSpecSvcRandBatteryFail() {
        when(triageSvc.generateIntakeBatteryOrder()).thenReturn(false);
        GenerateIntakeBatteryOrderRequest request = GenerateIntakeBatteryOrderRequest.newBuilder().build();

        triageSvcImpl.generateIntakeBatteryOrder(request, responseGenerateIntakeBatteryOrderResponse);
        verify(triageSvc).generateIntakeBatteryOrder();
        verify(responseGenerateIntakeBatteryOrderResponse).onNext(GenerateIntakeBatteryOrderResponse.newBuilder().setSuccess(false).build());
        verify(responseGenerateIntakeBatteryOrderResponse).onCompleted();
    }
}
