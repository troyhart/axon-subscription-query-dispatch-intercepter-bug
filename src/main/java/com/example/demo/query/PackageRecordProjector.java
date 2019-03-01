package com.example.demo.query;

import java.time.Instant;
import java.util.Optional;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.messaging.annotation.MetaDataValue;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.example.demo.api.events.PackageCreated;
import com.example.demo.api.events.PackageUpdated;
import com.example.demo.api.queries.PackageRecordByIdQuery;


@Component
public class PackageRecordProjector {

  private PackageRecordRepository packageRecordRepository;
  private QueryUpdateEmitter queryUpdateEmitter;

  private static final Logger LOGGER = LoggerFactory.getLogger(PackageRecordProjector.class);

  @Autowired
  public PackageRecordProjector(PackageRecordRepository packageRecordRepository,
      QueryUpdateEmitter queryUpdateEmitter) {
    this.packageRecordRepository = packageRecordRepository;
    this.queryUpdateEmitter = queryUpdateEmitter;
  }

  @QueryHandler
  public PackageRecord handle(PackageRecordByIdQuery query, @MetaDataValue("USER_INFO") Object userInfo) {
    assertUserCanQuery(query, userInfo);
    Optional<PackageRecord> record = packageRecordRepository.findById(query.getId());
    return record.isPresent() ? record.get() : null;
  }

  @EventHandler
  public void on(PackageCreated event, @SequenceNumber long aggregateVersion, @Timestamp Instant occurrenceInstant,
      @MetaDataValue("USER_INFO") Object userInfo) {
    handleDebugEvent(event, userInfo);
    // @formatter:off
    PackageRecord packageRecord = new PackageRecord()
        .setPackageId(event.getPackageId())
        .setType(event.getType())
        .setDescription(event.getDescription())
        .setCreatedBy(userInfo).setLastUpdatedBy(userInfo)
        .setCreatedInstant(occurrenceInstant).setLastUpdatedInstant(occurrenceInstant);
    // @formatter:on
    save(packageRecord, userInfo, occurrenceInstant, aggregateVersion);
  }

  @EventHandler
  public void on(PackageUpdated event, @SequenceNumber long aggregateVersion, @Timestamp Instant occurrenceInstant,
      @MetaDataValue("USER_INFO") Object userInfo) {
    handleDebugEvent(event, userInfo);
    Optional<PackageRecord> packageRecord = packageRecordRepository.findById(event.getPackageId());
    if (packageRecord.isPresent()) {
      // @formatter:off
      packageRecord.get()
          .setType(event.getType())
          .setDescription(event.getDescription())
          .setLastUpdatedBy(userInfo)
          .setLastUpdatedInstant(occurrenceInstant);
      // @formatter:on
      save(packageRecord.get(), userInfo, occurrenceInstant, aggregateVersion);
    }
    else {
      LOGGER.error("Can not locate pacakge to update!");
      return;
    }
  }

  private void save(PackageRecord packageRecord, Object userInfo, Instant occurrenceInstant, long aggregateVersion) {
    packageRecordRepository.save(packageRecord);
    LOGGER.trace("emitting update: {}", packageRecord);
    queryUpdateEmitter.emit(PackageRecordByIdQuery.class, query -> query.getId().equals(packageRecord.getPackageId()),
        packageRecord);
    LOGGER.debug("PackageRecord has been saved and the new record has been emitted!");
  }

  private void assertUserCanQuery(Object query, Object userInfo) {
    Assert.notNull(userInfo, "null userInfo");
    Assert.notNull(query, "null query");
    LOGGER.debug("The user can execute the given query.........\n>>> {}\n>>> {}", userInfo, query);
  }

  private void handleDebugEvent(Object event, Object userInfo) {
    LOGGER.debug("Handling event.........\n>>> {}\n>>> {}", userInfo, event);
  }
}
