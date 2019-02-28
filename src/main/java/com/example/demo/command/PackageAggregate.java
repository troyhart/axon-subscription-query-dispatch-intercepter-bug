package com.example.demo.command;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.annotation.MetaDataValue;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.util.StringUtils;

import com.example.demo.api.CreatePackage;
import com.example.demo.api.PackageCreated;
import com.example.demo.api.PackageUpdated;
import com.example.demo.api.UpdatePackage;

@Aggregate
public class PackageAggregate {

  @AggregateIdentifier
  private String packageId;

  private String type;
  private String description;

  public PackageAggregate() {
  }

  @CommandHandler
  PackageAggregate(CreatePackage command, @MetaDataValue("USER_INFO") Object userInfo) {
    command.validate();

    AggregateLifecycle.apply(new PackageCreated(command.getPackageId(), command.getType(), command.getDescription()));
  }

  @CommandHandler
  void handle(UpdatePackage command, @MetaDataValue("USER_INFO") Object userInfo) {
    command.validate();

    String type = getType(command);
    String description = getDescription(command);

    if (!this.type.equals(type) || !this.description.equals(description)) {
      AggregateLifecycle.apply(new PackageUpdated(command.getPackageId(), type, description));
    }
  }

  private String getType(UpdatePackage command) {
    return StringUtils.hasText(command.getType()) ? command.getType() : type;
  }

  private String getDescription(UpdatePackage command) {
    return StringUtils.hasText(command.getDescription()) ? command.getDescription() : description;
  }

  @EventSourcingHandler
  public void on(PackageUpdated event) {
    this.packageId = event.getPackageId();
    this.type = event.getType();
    this.description = event.getDescription();
  }

  @EventSourcingHandler
  public void on(PackageCreated event) {
    this.packageId = event.getPackageId();
    this.type = event.getType();
    this.description = event.getDescription();
  }
}
