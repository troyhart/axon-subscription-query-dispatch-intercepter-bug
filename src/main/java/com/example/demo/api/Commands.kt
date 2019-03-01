package com.example.demo.api.commands

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.springframework.util.Assert
import org.springframework.util.StringUtils


interface PackageCommand {
  val packageId: String
  fun validate()
}

data class CreatePackage(
  @TargetAggregateIdentifier
  override val packageId: String,
  val type: String,
  val description: String

) : PackageCommand {

  override fun validate() {
    Assert.hasText(packageId, "packageId is null/blank")
    Assert.hasText(type, "type is null/blank")
    Assert.hasText(description, "description is null/blank")
  }
}


data class UpdatePackage(
  @TargetAggregateIdentifier
  override val packageId: String,
  val type: String?,
  val description: String?

) : PackageCommand {

  override fun validate() {
    Assert.hasText(packageId, "packageId is null/blank")
    Assert.isTrue(
      StringUtils.hasText(type) || StringUtils.hasText(description),
      "One or the other (or both) of type and description must be specified"
    )
  }
}
