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

import java.util.List;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Michael Minella
 */
@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer {

	@Id
	private Long id;

	@Column(name = "firstName")
	private String firstName;
	@Column(name = "middleInitial")
	private String middleInitial;
	@Column(name = "lastName")
	private String lastName;
	private String addressNumber;
	private String street;
	private String address;
	private String city;
	private String state;
	private String zipCode;

    @XmlElementWrapper(name = "transactions")
	@XmlElement(name = "transaction")
    @Transient
	private List<Transaction> transactions;

	public Customer() {
	}

	public Customer(String firstName, String middleName, String lastName, String addressNumber, String street, String city, String state, String zipCode) {
		this.firstName = firstName;
		this.middleInitial = middleName;
		this.lastName = lastName;
		this.addressNumber = addressNumber;
		this.street = street;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
	}

  

	@Override
	public String toString() {
		return "Customer{" +
				"id=" + id +
				", firstName='" + firstName + '\'' +
				", middleInitial='" + middleInitial + '\'' +
				", lastName='" + lastName + '\'' +
				", address='" + address + '\'' +
				", city='" + city + '\'' +
				", state='" + state + '\'' +
				", zipCode='" + zipCode + '\'' +
				'}';
	}

//	@Override
//	public String toString() {
//		StringBuilder output = new StringBuilder();
//
//		output.append(firstName);
//		output.append(" ");
//		output.append(middleInitial);
//		output.append(". ");
//		output.append(lastName);
//
//		if(transactions != null&& transactions.size() > 0) {
//			output.append(" has ");
//			output.append(transactions.size());
//			output.append(" transactions.");
//		} else {
//			output.append(" has no transactions.");
//		}
//
//		return output.toString();
//	}

	//	@Override
//	public String toString() {
//		return "Customer{" +
//				"firstName='" + firstName + '\'' +
//				", middleInitial='" + middleInitial + '\'' +
//				", lastName='" + lastName + '\'' +
//				", address='" + address + '\'' +
////				", addressNumber='" + addressNumber + '\'' +
////				", street='" + street + '\'' +
//				", city='" + city + '\'' +
//				", state='" + state + '\'' +
//				", zipCode='" + zipCode + '\'' +
//				'}';
//	}
}
