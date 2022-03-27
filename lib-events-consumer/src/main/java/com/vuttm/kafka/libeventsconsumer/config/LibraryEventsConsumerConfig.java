package com.vuttm.kafka.libeventsconsumer.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.vuttm.kafka.libeventsconsumer.service.LibraryEventsService;

@Configuration
@EnableKafka
public class LibraryEventsConsumerConfig {

	private final Logger log = LoggerFactory.getLogger(LibraryEventsConsumerConfig.class);

	@Autowired
	LibraryEventsService libraryEventsService;

	@SuppressWarnings("deprecation")
	@Bean
	ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
			ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			ConsumerFactory<Object, Object> kafkaConsumerFactory) {
		ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		configurer.configure(factory, kafkaConsumerFactory);
		// 3 threats for 3 partitions
		factory.setConcurrency(3);
		// factory.getContainerProperties().setAckMode(AckMode.MANUAL);

		// Error handling
		factory.setErrorHandler(((thrownException, data) -> {
			log.error("The error has occurred {} an the record is {}", thrownException.getMessage(), data);
			// Do something
		}));
		factory.setRetryTemplate(retryTemplate());
		factory.setRecoveryCallback((context -> {
			if (context.getLastThrowable().getCause() instanceof RecoverableDataAccessException) {
				// invoke recovery logic
				log.info("Inside the recovery logic.");
				Arrays.asList(context.attributeNames()).forEach(attributeName -> {
					log.info("Attribute name: {} - value: {}", attributeName, context.getAttribute(attributeName));
				});
				@SuppressWarnings("unchecked")
				ConsumerRecord<Integer, String> record = (ConsumerRecord<Integer, String>) context
						.getAttribute("record");
				libraryEventsService.handleRecovery(record);
			} else {
				log.info("Inside the non recoverable logic");
				throw new RuntimeException(context.getLastThrowable().getMessage());
			}

			return null;
		}));

		return factory;
	}

	private RetryTemplate retryTemplate() {
		FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
		fixedBackOffPolicy.setBackOffPeriod(500); // milliseconds

		RetryTemplate template = new RetryTemplate();
		template.setRetryPolicy(createRetryPolicy());
		template.setBackOffPolicy(fixedBackOffPolicy);
		return template;
	}

	private RetryPolicy createRetryPolicy() {
		/*
		 * SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
		 * retryPolicy.setMaxAttempts(3);
		 */
		Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();
		exceptionMap.put(IllegalArgumentException.class, false);
		exceptionMap.put(RecoverableDataAccessException.class, true);
		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, exceptionMap, true);

		return retryPolicy;
	}

}
