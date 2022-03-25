package com.vuttm.kafka.libeventsproducer.producer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vuttm.kafka.libeventsproducer.domain.LibraryEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventProducer {

	private final Logger log = LoggerFactory.getLogger(LibraryEventProducer.class);
	private static final String TOPIC_NAME = "library-events";
	
	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;
	
	@Autowired
	ObjectMapper objectMapper;
	
	/**
	 * Send library-events request as the synchronous
	 * @param libraryEvent
	 * @return
	 * @throws JsonProcessingException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public SendResult<Integer, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent)
			throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);
		SendResult<Integer, String> sendResult = null;
		try {
			sendResult = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException e) {
			log.error("InterruptedException | ExecutionException -"
					+ "Error sending message with the key: {} and the value: {}. The exception is {}",
					key, value, e.getMessage());
			throw e;
		}
		return sendResult;
	}
	
	public void sendLibraryEventByProducerRecord(LibraryEvent libraryEvent) throws JsonProcessingException {
		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);
		// Send message to the default topic
		ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value);
		
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}
		});
	}
	
	private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
		List<Header> recordHeaders = List.of(new RecordHeader("Event-source", "scanner".getBytes()));
		
		return new ProducerRecord<Integer, String>(TOPIC_NAME, null, key, value, recordHeaders);
	}
	
	/**
	 * Send library-events request as the asynchronous
	 * @param libraryEvent
	 * @throws JsonProcessingException
	 */
	public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);
		// Send message to the default topic
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send("library-events", key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}
		});
	}
	
	private void handleFailure(Integer key, String value, Throwable ex) {
		log.error("Error sending message with the key: {} and the value: {}. The exception is {}",
				key, value, ex.getMessage());
		try {
			throw ex;
		} catch (Throwable throwable) {
			log.error("Error Message on failure: {}", throwable.getMessage());
		}
	}
	
	private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
		log.info("Message sent successfully for the key: {} and the value: {} and the partition is {}",
				key, value, sendResult.getRecordMetadata().partition());
	}
}
