package com.battre.triagesvc.service;

import com.battre.stubs.services.TriageSvcEmptyRequest;
import com.battre.stubs.services.TriageSvcEmptyResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.verify;

class TriageServiceImplTests {

    @Mock
    private TriageService triageService;

    @Mock
    private StreamObserver<TriageSvcEmptyResponse> responseObserver;

    private TriageServiceImpl triageServiceImpl;

    @Test
    void testCallSpecSvcRandBattery() {
        MockitoAnnotations.openMocks(this);
        triageServiceImpl = new TriageServiceImpl(triageService);

        TriageSvcEmptyRequest request = TriageSvcEmptyRequest.newBuilder().build();

        triageServiceImpl.callSpecSvcRandBattery(request, responseObserver);
        verify(triageService).queryRandomBattery();
    }
}
