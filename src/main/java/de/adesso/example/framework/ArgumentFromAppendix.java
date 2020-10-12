package de.adesso.example.framework;

import java.util.UUID;

import lombok.NonNull;

public class ArgumentFromAppendix extends Argument {
	private final UUID requiredAttachment;

	public ArgumentFromAppendix(@NonNull final Class<?> type, @NonNull final UUID requiredAttachment) {
		super(type);
		this.requiredAttachment = requiredAttachment;
	}

	@Override
	protected Object prepareArgument(final ApplicationProtocol<?> state, final Object[] args) {
		final Object result = state.getAppendixOfType(this.requiredAttachment);

		return result;
	}
}
