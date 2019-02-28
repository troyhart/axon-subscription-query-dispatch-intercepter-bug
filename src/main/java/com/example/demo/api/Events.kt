package com.example.demo.api

interface PackageEvent {
  val packageId: String
}

data class PackageCreated(
  override val packageId: String,
  val type: String,
  val description: String

) : PackageEvent

data class PackageUpdated(
  override val packageId: String,
  val type: String,
  val description: String

) : PackageEvent