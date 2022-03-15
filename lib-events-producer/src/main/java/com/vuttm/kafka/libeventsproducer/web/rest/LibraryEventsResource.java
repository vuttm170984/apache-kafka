package com.vuttm.kafka.libeventsproducer.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.vuttm.kafka.libeventsproducer.domain.LibraryEvent;

@RestController
public class LibraryEventsResource {

	@PostMapping("/api/library")
	public ResponseEntity<LibraryEvent> saveLibraryEvent(@RequestBody LibraryEvent libraryEvent) {
		// invoke kafka producer
		libraryEvent.setLibraryEventId(1);
		return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
	}
}
