package de.adesso.example.application.accounting;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class CustomerOrganization extends Customer {

	CustomerOrganization(final UUID customerId, final String name) {
		super(customerId);
		this.name = name;
	}

	private final String name;
}
