package com.battre.triagesvc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.battre.stubs.services.SpecSvcGrpc;
import com.battre.stubs.services.SpecSvcResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

class TriageServiceTests {
    private static final Logger logger = Logger.getLogger(TriageServiceTests.class.getName());

    @Mock
    private SpecSvcGrpc.SpecSvcStub specSvcClient;

    private TriageService triageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        triageService = new TriageService(specSvcClient);
    }


    @Test
    void testQueryRandomBattery_Success() throws Exception {
        String expectedResponse = "Battery Type";

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                // Get the StreamObserver passed as an argument
                StreamObserver<SpecSvcResponse> observer = invocation.getArgument(1);

                // Create a response
                SpecSvcResponse response = SpecSvcResponse.newBuilder().setResponse(expectedResponse).build();

                // Pass the response to the StreamObserver
                observer.onNext(response);
                observer.onCompleted();

                return null;
            }
        }).when(specSvcClient).getRandomBatteryType(any(), any());

        String actualResponse = triageService.queryRandomBattery();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testQueryRandomBattery_Error() {
        CompletableFuture<SpecSvcResponse> responseFuture = new CompletableFuture<>();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                // Get the StreamObserver passed as an argument
                StreamObserver<SpecSvcResponse> observer = invocation.getArgument(1);

                // Simulate an error by calling onError on the observer
                observer.onError(new RuntimeException("Test Error"));

                // Complete the CompletableFuture with an exception
                responseFuture.completeExceptionally(new RuntimeException("Test Error"));

                return null;
            }
        }).when(specSvcClient).getRandomBatteryType(any(), any());

        logger.log(Level.INFO, "TriageSvc errors expected to follow as part of test");
        String actualResponse = triageService.queryRandomBattery();


        assertEquals(null, actualResponse);
    }

    @Test
    void testQueryRandomBattery_Timeout() {
        CompletableFuture<SpecSvcResponse> responseFuture = new CompletableFuture<>();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                // Get the StreamObserver passed as an argument
                StreamObserver<SpecSvcResponse> observer = invocation.getArgument(1);

                // No response will be sent, simulating timeout
                return null;
            }
        }).when(specSvcClient).getRandomBatteryType(any(), any());

        logger.log(Level.INFO, "TriageSvc errors expected to follow as part of test");
        String actualResponse = triageService.queryRandomBattery();

        assertEquals(null, actualResponse);
    }
}