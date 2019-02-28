package com.example.demo.query;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class PackageRecord {

  @Id
  private String packageId;

  private String type;
  private String description;
  private Instant createdInstant;
  private Object createdBy;
  private Instant lastUpdatedInstant;
  private Object lastUpdatedBy;

  public String getPackageId() {
    return packageId;
  }

  PackageRecord setPackageId(String packageId) {
    this.packageId = packageId;
    return this;
  }

  public String getType() {
    return type;
  }

  PackageRecord setType(String type) {
    this.type = type;
    return this;
  }

  public String getDescription() {
    return description;
  }

  PackageRecord setDescription(String description) {
    this.description = description;
    return this;
  }

  public Object getCreatedBy() {
    return createdBy;
  }

  PackageRecord setCreatedBy(Object createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public Instant getCreatedInstant() {
    return createdInstant;
  }

  PackageRecord setCreatedInstant(Instant createdInstant) {
    this.createdInstant = createdInstant;
    return this;
  }

  public Object getLastUpdatedBy() {
    return lastUpdatedBy;
  }

  PackageRecord setLastUpdatedBy(Object lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
    return this;
  }

  public Instant getLastUpdatedInstant() {
    return lastUpdatedInstant;
  }

  PackageRecord setLastUpdatedInstant(Instant lastUpdatedInstant) {
    this.lastUpdatedInstant = lastUpdatedInstant;
    return this;
  }
}
