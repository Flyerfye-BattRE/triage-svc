package com.battre.triagesvc.service;

import com.battre.stubs.services.GenerateIntakeBatteryOrderRequest;
import com.battre.stubs.services.GenerateIntakeBatteryOrderResponse;
import com.battre.triagesvc.controller.TriageSvcController;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TriageSvcControllerTests {

    @Mock
    private TriageSvc triageSvc;

    @Mock
    private StreamObserver<GenerateIntakeBatteryOrderResponse> responseGenerateIntakeBatteryOrderResponse;

    private TriageSvcController triageSvcController;

    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
        triageSvcController = new TriageSvcController(triageSvc);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testCallSpecSvcRandBatterySuccess() {
        when(triageSvc.generateIntakeBatteryOrder()).thenReturn(true);
        GenerateIntakeBatteryOrderRequest request = GenerateIntakeBatteryOrderRequest.newBuilder().build();

        triageSvcController.generateIntakeBatteryOrder(request, responseGenerateIntakeBatteryOrderResponse);
        verify(triageSvc).generateIntakeBatteryOrder();
        verify(responseGenerateIntakeBatteryOrderResponse).onNext(GenerateIntakeBatteryOrderResponse.newBuilder().setSuccess(true).build());
        verify(responseGenerateIntakeBatteryOrderResponse).onCompleted();
    }

    @Test
    void testCallSpecSvcRandBatteryFail() {
        when(triageSvc.generateIntakeBatteryOrder()).thenReturn(false);
        GenerateIntakeBatteryOrderRequest request = GenerateIntakeBatteryOrderRequest.newBuilder().build();

        triageSvcController.generateIntakeBatteryOrder(request, responseGenerateIntakeBatteryOrderResponse);
        verify(triageSvc).generateIntakeBatteryOrder();
        verify(responseGenerateIntakeBatteryOrderResponse).onNext(GenerateIntakeBatteryOrderResponse.newBuilder().setSuccess(false).build());
        verify(responseGenerateIntakeBatteryOrderResponse).onCompleted();
    }
}
