package com.example.demo.rest.api;

import org.springframework.util.Assert;

public class PackageIdentifierDto {
  private String packageId;

  public PackageIdentifierDto(String packageId) {
    Assert.hasText(packageId, "null/blank packageId");

    this.packageId = packageId;
  }

  public String getPackageId() {
    return packageId;
  }

  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }
}
