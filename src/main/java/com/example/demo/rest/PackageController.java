package com.example.demo.rest;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.commands.CreatePackage;
import com.example.demo.api.commands.UpdatePackage;
import com.example.demo.api.queries.PackageRecordByIdQuery;
import com.example.demo.query.PackageRecord;
import com.example.demo.rest.api.dtos.PackageIdentifier;
import com.example.demo.rest.api.dtos.PackageRequest;

import reactor.core.publisher.Flux;

@RestController()
@RequestMapping(path = "/packages")
public class PackageController {

  private static final Logger LOG = LoggerFactory.getLogger(PackageController.class);

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;

  @Autowired
  public PackageController(CommandGateway commandGateway, QueryGateway queryGateway) {
    this.commandGateway = commandGateway;
    this.queryGateway = queryGateway;
  }


  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
  public CompletableFuture<PackageIdentifier> create(@RequestBody PackageRequest request) {
    Assert.notNull(request, "null request)");

    return commandGateway
        .send(new CreatePackage(UUID.randomUUID().toString(), request.getType(), request.getDescription()))
        .thenApply(id -> new PackageIdentifier(id.toString()));
  }


  @PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public CompletableFuture<Void> update(@PathVariable String id, @RequestBody PackageRequest request) {
    Assert.notNull(request, "null request)");

    UpdatePackage command = new UpdatePackage(id, request.getType(), request.getDescription());
    return commandGateway.send(command);
  }


  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PackageRecord> packageRecord(@PathVariable String id) {
    Assert.hasText(id, "null/blank id (package identifier)");

    return queryGateway.query(new PackageRecordByIdQuery(id), ResponseTypes.instanceOf(PackageRecord.class))
        .whenComplete(completePackageRecordByIdQuery(id));
  }

  @GetMapping(path = "/{id}/subscription", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<PackageRecord> packageRecordSubscription(@PathVariable String id)
      throws InterruptedException, ExecutionException {
    Assert.hasText(id, "null/blank id (package identifier)");

    return Flux.<PackageRecord> create(emitter -> {
      SubscriptionQueryResult<PackageRecord, PackageRecord> queryResult = subscribeToPackageRecord(queryGateway, id);
      // NOTE: it seems like the logging in `doOnError()` below should show up if an exception is thrown
      // somewhere along the way of trying to get the initial result....but to no avail here...
      queryResult.initialResult().doOnError(error -> LOG.warn("Initial result error...", error))
          .subscribe(emitter::next);
      queryResult.updates().doOnError(error -> LOG.warn("Updated result error...", error))
          .buffer(Duration.ofMillis(500)).map(prlist -> prlist.get(prlist.size() - 1)).doOnComplete(emitter::complete)
          .subscribe(emitter::next);
    });
  }


  static SubscriptionQueryResult<PackageRecord, PackageRecord> subscribeToPackageRecord(QueryGateway queryGateway,
      String pkgId) {
    SubscriptionQueryResult<PackageRecord, PackageRecord> queryResult =
        queryGateway.subscriptionQuery(new PackageRecordByIdQuery(pkgId), ResponseTypes.instanceOf(PackageRecord.class),
            ResponseTypes.instanceOf(PackageRecord.class));
    return queryResult;
  }

  static BiConsumer<? super PackageRecord, ? super Throwable> completePackageRecordByIdQuery(String packageId) {
    return (packageRecord, throwable) -> {
      // TODO: verify this is what we want...the runtime exception here may not be what we want...
      if (throwable != null) throw new RuntimeException(throwable);
      if (packageRecord == null) throw new NoSuchElementException("No value for packageId=" + packageId);
    };
  }
}
