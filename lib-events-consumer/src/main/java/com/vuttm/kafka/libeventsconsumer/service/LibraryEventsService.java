package com.vuttm.kafka.libeventsconsumer.service;

import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vuttm.kafka.libeventsconsumer.domain.LibraryEvent;
import com.vuttm.kafka.libeventsconsumer.repository.LibraryEventsRepository;

@Service
public class LibraryEventsService {

	private final Logger log = LoggerFactory.getLogger(LibraryEventsService.class);

	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	private LibraryEventsRepository libraryEventsRepository;

	public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
		LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
		log.info("libraryEvent : {} ", libraryEvent);

		if (libraryEvent.getLibraryEventId() != null && libraryEvent.getLibraryEventId() == 000) {
			throw new RecoverableDataAccessException("Temporary Network Issue");
		}

		switch (libraryEvent.getLibraryEventType()) {
		case NEW:
			save(libraryEvent);
			break;
		case UPDATE:
			validate(libraryEvent);
			save(libraryEvent);
			break;
		default:
			log.info("Invalid Library Event Type");
		}

	}

	private void validate(LibraryEvent libraryEvent) {
		if (null == libraryEvent.getLibraryEventId()) {
			throw new IllegalArgumentException("Library Event Id is missing");
		}

		Optional<LibraryEvent> libraryEventOptional = libraryEventsRepository
				.findById(libraryEvent.getLibraryEventId());
		if (!libraryEventOptional.isPresent()) {
			throw new IllegalArgumentException("Not a valid library Event");
		}
		log.info("Validation is successful for the library Event : {} ", libraryEventOptional.get());
	}

	private void save(LibraryEvent libraryEvent) {
		libraryEvent.getBook().setLibraryEvent(libraryEvent);
		libraryEventsRepository.save(libraryEvent);
		log.info("Successfully Persisted the libary Event {} ", libraryEvent);
	}
	
	public void handleRecovery(ConsumerRecord<Integer, String> record) {
		if (null == record) {
			return;
		}
		Integer key = record.key();
		String value = record.value();
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate
				.sendDefault(record.key(), record.value());
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
			@Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
		});
	}
	
	private void handleFailure(Integer key, String value, Throwable ex) {
		log.error("Error: {}", ex.getMessage());
		try {
			throw ex;
		} catch (Throwable e) {
			log.error("Error in onFailure: {}", e.getMessage());
		}
	}
	
	private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
		log.info("Message sent successfully with key: {}, value: {}, partition: {}", key, value,
				result.getRecordMetadata().partition());
	}
}
