package com.battre.triagesvc.service;

import com.battre.grpcifc.GrpcMethodInvoker;
import com.battre.stubs.services.BatteryTypeTierCount;
import com.battre.stubs.services.BatteryTypeTierPair;
import com.battre.stubs.services.GetRandomBatteryTypesRequest;
import com.battre.stubs.services.GetRandomBatteryTypesResponse;
import com.battre.stubs.services.ProcessIntakeBatteryOrderRequest;
import com.battre.stubs.services.ProcessIntakeBatteryOrderResponse;
import com.battre.triagesvc.enums.GenerateOrderStatusEnum;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TriageSvc {

  private static final Logger logger = Logger.getLogger(TriageSvc.class.getName());
  private final GrpcMethodInvoker grpcMethodInvoker;
  private final Random random;

  @Autowired
  public TriageSvc(GrpcMethodInvoker grpcMethodInvoker) {
    this.grpcMethodInvoker = grpcMethodInvoker;
    this.random = new Random();
  }

  public GenerateOrderStatusEnum generateIntakeBatteryOrder() {
    // Randomly decide # of battery types to include
    int numBatteryTypes = random.nextInt(3) + 1;

    List<BatteryTypeTierPair> batteryTypeTierInfo = queryRandomBatteryInfo(numBatteryTypes);
    logger.info("Battery info returned for " + batteryTypeTierInfo.size() + "/" + numBatteryTypes);

    if (!batteryTypeTierInfo.isEmpty()) {
      return processOrder(batteryTypeTierInfo);
    } else {
      return GenerateOrderStatusEnum.SPECSVC_GENBATTERIES_ERR;
    }
  }

  private GenerateOrderStatusEnum processOrder(List<BatteryTypeTierPair> batteryTypeTierInfo) {
    List<BatteryTypeTierCount> batteryTypeTierCountInfo =
        batteryTypeTierInfo.stream()
            .map(
                batteryTypeTierEntry ->
                    BatteryTypeTierCount.newBuilder()
                        .setBatteryType(batteryTypeTierEntry.getBatteryTypeId())
                        .setBatteryTier(batteryTypeTierEntry.getBatteryTierId())
                        .setBatteryCount(random.nextInt(2) + 1)
                        .build())
            .collect(Collectors.toList());

    ProcessIntakeBatteryOrderRequest request =
        ProcessIntakeBatteryOrderRequest.newBuilder()
            .addAllBatteries(batteryTypeTierCountInfo)
            .build();

    ProcessIntakeBatteryOrderResponse response =
        grpcMethodInvoker.invokeNonblock("opssvc", "processIntakeBatteryOrder", request);

    if (response == null) {
      return GenerateOrderStatusEnum.UNKNOWN_ERR;
    }

    return response.getSuccess()
        ? GenerateOrderStatusEnum.SUCCESS
        : GenerateOrderStatusEnum.fromStatusDescription(response.getStatus().toString());
  }

  public List<BatteryTypeTierPair> queryRandomBatteryInfo(int numBatteryTypes) {
    GetRandomBatteryTypesRequest request =
        GetRandomBatteryTypesRequest.newBuilder().setNumBatteryTypes(numBatteryTypes).build();

    GetRandomBatteryTypesResponse response =
        grpcMethodInvoker.invokeNonblock("specsvc", "getRandomBatteryTypes", request);

    return response.getBatteriesList();
  }
}
