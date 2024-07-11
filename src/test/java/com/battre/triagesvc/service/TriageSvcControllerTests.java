package com.battre.triagesvc.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.battre.stubs.services.GenerateIntakeBatteryOrderRequest;
import com.battre.stubs.services.GenerateIntakeBatteryOrderResponse;
import com.battre.stubs.services.GenerateOrderStatus;
import com.battre.triagesvc.controller.TriageSvcController;
import com.battre.triagesvc.enums.GenerateOrderStatusEnum;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TriageSvcControllerTests {

  @Mock private TriageSvc triageSvc;

  @Mock
  private StreamObserver<GenerateIntakeBatteryOrderResponse>
      responseGenerateIntakeBatteryOrderResponse;

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
    when(triageSvc.generateIntakeBatteryOrder()).thenReturn(GenerateOrderStatusEnum.SUCCESS);
    GenerateIntakeBatteryOrderRequest request =
        GenerateIntakeBatteryOrderRequest.newBuilder().build();

    triageSvcController.generateIntakeBatteryOrder(
        request, responseGenerateIntakeBatteryOrderResponse);
    verify(triageSvc).generateIntakeBatteryOrder();
    verify(responseGenerateIntakeBatteryOrderResponse)
        .onNext(
            GenerateIntakeBatteryOrderResponse.newBuilder()
                .setSuccess(true)
                .setStatus(GenerateOrderStatus.SUCCESS)
                .build());
    verify(responseGenerateIntakeBatteryOrderResponse).onCompleted();
  }

  @Test
  void testCallSpecSvcRandBatteryFail() {
    when(triageSvc.generateIntakeBatteryOrder())
        .thenReturn(GenerateOrderStatusEnum.SPECSVC_GENBATTERIES_ERR);
    GenerateIntakeBatteryOrderRequest request =
        GenerateIntakeBatteryOrderRequest.newBuilder().build();

    triageSvcController.generateIntakeBatteryOrder(
        request, responseGenerateIntakeBatteryOrderResponse);
    verify(triageSvc).generateIntakeBatteryOrder();
    verify(responseGenerateIntakeBatteryOrderResponse)
        .onNext(
            GenerateIntakeBatteryOrderResponse.newBuilder()
                .setSuccess(false)
                .setStatus(GenerateOrderStatus.SPECSVC_GENBATTERIES_ERR)
                .build());
    verify(responseGenerateIntakeBatteryOrderResponse).onCompleted();
  }
}
