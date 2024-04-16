package com.battre.triagesvc.service;

import com.battre.stubs.services.GenerateIntakeBatteryOrderRequest;
import com.battre.stubs.services.GenerateIntakeBatteryOrderResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

class TriageServiceImplTests {

    @Mock
    private TriageService triageService;

    @Mock
    private StreamObserver<GenerateIntakeBatteryOrderResponse> responseObserver;

    private TriageServiceImpl triageServiceImpl;

    @Test
    void testCallSpecSvcRandBattery() {
        MockitoAnnotations.openMocks(this);
        triageServiceImpl = new TriageServiceImpl(triageService);

        GenerateIntakeBatteryOrderRequest request = GenerateIntakeBatteryOrderRequest.newBuilder().build();

        triageServiceImpl.generateIntakeBatteryOrder(request, responseObserver);
        verify(triageService).generateIntakeBatteryOrder();
    }
}
