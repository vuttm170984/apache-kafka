package com.vuttm.kafka.libeventsproducer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Book {

	private Integer bookId;
	private String bookName;
	private String bookAuthor;
}
