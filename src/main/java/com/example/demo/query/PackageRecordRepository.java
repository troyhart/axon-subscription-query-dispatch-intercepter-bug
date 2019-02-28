package com.example.demo.query;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageRecordRepository extends MongoRepository<PackageRecord, String> {
}
