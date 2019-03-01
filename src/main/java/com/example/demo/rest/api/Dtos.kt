package com.example.demo.rest.api.dtos

data class PackageIdentifier(
  val packageId: String
)

data class PackageRequest(
  val type: String,
  val description: String
)
