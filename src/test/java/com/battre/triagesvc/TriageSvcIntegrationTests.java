package com.battre.triagesvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.battre.grpcifc.GrpcMethodInvoker;
import com.battre.grpcifc.GrpcTestMethodInvoker;
import com.battre.stubs.services.BatteryTypeTierPair;
import com.battre.stubs.services.GenerateIntakeBatteryOrderRequest;
import com.battre.stubs.services.GenerateIntakeBatteryOrderResponse;
import com.battre.stubs.services.GenerateOrderStatus;
import com.battre.stubs.services.GetRandomBatteryTypesRequest;
import com.battre.stubs.services.GetRandomBatteryTypesResponse;
import com.battre.stubs.services.ProcessIntakeBatteryOrderRequest;
import com.battre.stubs.services.ProcessIntakeBatteryOrderResponse;
import com.battre.stubs.services.ProcessOrderStatus;
import com.battre.triagesvc.controller.TriageSvcController;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = "grpc.server.port=9003")
@ExtendWith(MockitoExtension.class)
public class TriageSvcIntegrationTests {
  private static final Logger logger = Logger.getLogger(TriageSvcIntegrationTests.class.getName());

  @MockBean private GrpcMethodInvoker grpcMethodInvoker;
  @Autowired private TriageSvcController triageSvcController;
  private final GrpcTestMethodInvoker grpcTestMethodInvoker = new GrpcTestMethodInvoker();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGenerateIntakeBatteryOrder_Success() throws NoSuchMethodException {
    // Test
    GetRandomBatteryTypesResponse tryGetRandomBatteryTypesResponse =
        GetRandomBatteryTypesResponse.newBuilder()
            .addBatteries(
                BatteryTypeTierPair.newBuilder().setBatteryTypeId(4).setBatteryTierId(2).build())
            .build();
    when(grpcMethodInvoker.invokeNonblock(
            eq("specsvc"), eq("getRandomBatteryTypes"), any(GetRandomBatteryTypesRequest.class)))
        .thenReturn(tryGetRandomBatteryTypesResponse);

    ProcessIntakeBatteryOrderResponse tryProcessIntakeBatteryOrderResponse =
        ProcessIntakeBatteryOrderResponse.newBuilder()
            .setSuccess(true)
            .setStatus(ProcessOrderStatus.SUCCESS)
            .build();
    when(grpcMethodInvoker.invokeNonblock(
            eq("opssvc"),
            eq("processIntakeBatteryOrder"),
            any(ProcessIntakeBatteryOrderRequest.class)))
        .thenReturn(tryProcessIntakeBatteryOrderResponse);

    // Request
    GenerateIntakeBatteryOrderRequest request =
        GenerateIntakeBatteryOrderRequest.newBuilder().build();
    GenerateIntakeBatteryOrderResponse response =
        grpcTestMethodInvoker.invokeNonblock(
            triageSvcController, "generateIntakeBatteryOrder", request);
    assertTrue(response.getSuccess());
    assertEquals(response.getStatus(), GenerateOrderStatus.SUCCESS);

    // Verify
    verify(grpcMethodInvoker)
        .invokeNonblock(
            eq("specsvc"), eq("getRandomBatteryTypes"), any(GetRandomBatteryTypesRequest.class));
    verify(grpcMethodInvoker)
        .invokeNonblock(
            eq("opssvc"),
            eq("processIntakeBatteryOrder"),
            any(ProcessIntakeBatteryOrderRequest.class));
  }
}
