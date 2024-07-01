package com.battre.triagesvc.enums;

import com.battre.stubs.services.GenerateOrderStatus;

public enum GenerateOrderStatusEnum {
  UNKNOWN_ERR(0, "UNKNOWN_ERR", GenerateOrderStatus.UNKNOWN_ERR),
  SUCCESS(1, "SUCCESS", GenerateOrderStatus.SUCCESS),
  OPSSVC_CREATE_RECORD_ERR(
      2, "OPSSVC_CREATE_RECORD_ERR", GenerateOrderStatus.OPSSVC_CREATE_RECORD_ERR),
  STORAGESVC_STORE_BATTERIES_ERR(
      3, "STORAGESVC_STORE_BATTERIES_ERR", GenerateOrderStatus.STORAGESVC_STORE_BATTERIES_ERR),
  LABSVC_BACKLOG_ERR(4, "LABSVC_BACKLOG_ERR", GenerateOrderStatus.LABSVC_BACKLOG_ERR),
  SPECSVC_GENBATTERIES_ERR(
      5, "SPECSVC_GENBATTERIES_ERR", GenerateOrderStatus.SPECSVC_GENBATTERIES_ERR);

  private final int statusCode;
  private final String statusDescription;
  private final GenerateOrderStatus grpcStatus;

  GenerateOrderStatusEnum(
      int statusCode, String statusDescription, GenerateOrderStatus grpcStatus) {
    this.statusCode = statusCode;
    this.statusDescription = statusDescription;
    this.grpcStatus = grpcStatus;
  }

  public static GenerateOrderStatusEnum fromStatusCode(int statusCode) {
    for (GenerateOrderStatusEnum status : values()) {
      if (status.statusCode == statusCode) {
        return status;
      }
    }
    return UNKNOWN_ERR;
  }

  public static GenerateOrderStatusEnum fromStatusDescription(String statusDescription) {
    for (GenerateOrderStatusEnum status : values()) {
      if (status.statusDescription.equals(statusDescription)) {
        return status;
      }
    }
    return UNKNOWN_ERR;
  }

  public static GenerateOrderStatusEnum fromGrpcStatus(GenerateOrderStatus grpcStatus) {
    for (GenerateOrderStatusEnum status : values()) {
      if (status.grpcStatus.equals(grpcStatus)) {
        return status;
      }
    }
    return UNKNOWN_ERR;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

  public String getStatusDescription() {
    return this.statusDescription;
  }

  public GenerateOrderStatus getgrpcStatus() {
    return this.grpcStatus;
  }
}
