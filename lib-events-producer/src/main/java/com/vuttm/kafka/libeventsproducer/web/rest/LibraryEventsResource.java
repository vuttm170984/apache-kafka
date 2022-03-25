package com.vuttm.kafka.libeventsproducer.web.rest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vuttm.kafka.libeventsproducer.domain.LibraryEvent;
import com.vuttm.kafka.libeventsproducer.domain.LibraryEventType;
import com.vuttm.kafka.libeventsproducer.producer.LibraryEventProducer;

@RestController
public class LibraryEventsResource {

	private final Logger log = LoggerFactory.getLogger(LibraryEventsResource.class);
	
	private static final String BAD_REQUEST_MSG = "Invalid request!";
	
	@Autowired
	private LibraryEventProducer libraryEventProducer;
	
	@PostMapping("/api/library")
	public ResponseEntity<LibraryEvent> createLibraryEvent(@RequestBody LibraryEvent libraryEvent)
			throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
		libraryEvent.setLibraryEventType(LibraryEventType.NEW);
		
		// invoke Apache-Kafka producer
		log.info("Before sendLibraryEvent");
		//libraryEvent.setLibraryEventId(1);
		// This is used to show the asynchronous code is separately handled in different threads.
		//libraryEventProducer.sendLibraryEvent(libraryEvent);
		
		// Synchronous request
		//SendResult<Integer, String> sendResult = libraryEventProducer.sendLibraryEventSynchronous(libraryEvent);
		// log.info("After sendLibraryEvent with partition: {}", sendResult.getRecordMetadata().partition());
		
		// Send message using ProducerRecord
		libraryEventProducer.sendLibraryEventByProducerRecord(libraryEvent);
		return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
	}
	
	@PutMapping("/api/library")
	public ResponseEntity<?> updateLibraryEvent(@RequestBody LibraryEvent libraryEvent)
			throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
		if (null == libraryEvent
				|| null == libraryEvent.getLibraryEventId()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BAD_REQUEST_MSG);
		}
		
		libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
		libraryEventProducer.sendLibraryEventByProducerRecord(libraryEvent);
		return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
	}
}
