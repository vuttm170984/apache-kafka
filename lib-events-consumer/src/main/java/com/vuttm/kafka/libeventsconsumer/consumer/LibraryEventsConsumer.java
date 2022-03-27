package com.vuttm.kafka.libeventsconsumer.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vuttm.kafka.libeventsconsumer.service.LibraryEventsService;

@Component
public class LibraryEventsConsumer {

	private final Logger log = LoggerFactory.getLogger(LibraryEventsConsumer.class);
	
	@Autowired
    private LibraryEventsService libraryEventsService;
	
	@KafkaListener(topics = {"library-events"})
	public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
		log.info("Consummer record: {}", consumerRecord);
		libraryEventsService.processLibraryEvent(consumerRecord);
	}
}
