package com.vuttm.kafka.libeventsconsumer.repository;

import org.springframework.data.repository.CrudRepository;

import com.vuttm.kafka.libeventsconsumer.domain.LibraryEvent;

public interface LibraryEventsRepository extends CrudRepository<LibraryEvent,Integer> {
}
