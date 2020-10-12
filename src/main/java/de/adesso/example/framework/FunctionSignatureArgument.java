package de.adesso.example.framework;

import javax.validation.constraints.PositiveOrZero;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FunctionSignatureArgument extends Argument {

	/**
	 * position of the argument on the initial call. Counts from 0.
	 */
	private final int position;

	public FunctionSignatureArgument(final @NonNull Class<?> type, final @PositiveOrZero int position) {
		super(type);
		if (position < 0) {
			final String message = "position may not be negative";
			log.atError().log(message);
			throw new IndexOutOfBoundsException(message);
		}
		this.position = position;
	}

	@Override
	protected Object prepareArgument(final ApplicationProtocol<?> state, final Object[] args) {
		if (this.position >= args.length) {
			final String message = String.format(
					"too less arguments provided. Required position %d but argument length is %d",
					this.position, args.length);
			log.atError().log(message);
			throw new IndexOutOfBoundsException(message);
		}

		return args[this.position];
	}
}
