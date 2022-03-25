package com.vuttm.kafka.libeventsproducer.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import com.vuttm.kafka.libeventsproducer.domain.Book;
import com.vuttm.kafka.libeventsproducer.domain.LibraryEvent;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
//@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=spring.embedded.kafka.brokers",
//		"spring.kafka.admin.properties.bootstrap-servers=spring.embedded.kafka.brokers"})
public class LibraryEventsResourceTest {

	private final String LIBRARY_EVENT_URI = "/api/library";
	
	@Autowired
	TestRestTemplate restTemplate;
	
	@Test
	void createLibraryEvent() {
		// Can not use lombok in this Eclipse version
		Book book = new Book();
		book.setBookId(170984);
		book.setBookAuthor("Tran Tuan Minh Vu");
		book.setBookName("Apache Kafka - Practice");
		
		LibraryEvent libraryEvent = new LibraryEvent();
		libraryEvent.setBook(book);
		libraryEvent.setLibraryEventId(1);
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("content-type", MediaType.APPLICATION_JSON.toString());
		HttpEntity<LibraryEvent> request = new HttpEntity<LibraryEvent>(libraryEvent, headers);
		
		ResponseEntity<LibraryEvent> response = restTemplate.exchange(
				LIBRARY_EVENT_URI, HttpMethod.POST, request, LibraryEvent.class);
		
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}
	
	@Test
	void updateLibraryEvent() {
		// Can not use lombok in this Eclipse version
		Book book = new Book();
		book.setBookId(170984);
		book.setBookAuthor("Tran Tuan Minh Vu");
		book.setBookName("Apache Kafka - Practice");
		
		LibraryEvent libraryEvent = new LibraryEvent();
		libraryEvent.setBook(book);
		libraryEvent.setLibraryEventId(1);
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("content-type", MediaType.APPLICATION_JSON.toString());
		HttpEntity<LibraryEvent> request = new HttpEntity<LibraryEvent>(libraryEvent, headers);
		
		ResponseEntity<LibraryEvent> response = restTemplate.exchange(
				LIBRARY_EVENT_URI, HttpMethod.PUT, request, LibraryEvent.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
}
