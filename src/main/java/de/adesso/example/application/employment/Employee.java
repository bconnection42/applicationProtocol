/**
 * The MIT License (MIT)
 *
 * Copyright © 2020 Matthias Brenner and Adesso SE
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.adesso.example.application.employment;

import java.io.Serializable;
import java.util.UUID;

import org.javamoney.moneta.Money;

import de.adesso.example.application.Standard;
import de.adesso.example.application.accounting.Customer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Employee implements Serializable {

	private static final long serialVersionUID = 800784862436922847L;

	private final String name;
	private final String firstName;
	private final int id;
	private final Customer employeeCustomer = new Customer(UUID.randomUUID());

	@Setter(value = AccessLevel.PACKAGE)
	private Money income;
	private final Money benefit = Money.of(0.0, Standard.EUROS);

	Employee(final String firstName, final String name, final int id) {
		this.firstName = firstName;
		this.name = name;
		this.id = id;
	}

	void registerBenefit(final EmployeeBenefit benefitRecord) {
		this.benefit.add(benefitRecord.getBenefit());
	}
}
