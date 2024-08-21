/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.Chapter07.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter; 

/**
 * @author Michael Minella
 */
@Getter
@Setter
@XmlType(name = "transaction")
public class Transaction {

	private String accountNumber;
	private Date transactionDate;
	private Double amount;

	private DateFormat formatter =
			new SimpleDateFormat("MM/dd/yyyy"); 

	public String getDateString() {
		return this.formatter.format(this.transactionDate);
	}

	@Override
	public String toString() {
		return "Transaction{" +
				"accountNumber='" + accountNumber + '\'' +
				", transactionDate=" + transactionDate +
				", amount=" + amount +
				'}';
	}
}
