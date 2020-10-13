package de.adesso.example.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.log4j.Log4j2;

/**
 * This class describes how a method of a class should be called within the
 * framework. The requirements of the framework are that the result type is
 * {@link ApplicationProtocol}. There may be some parameters which can be given
 * from the call by {@link FunctionSignatureArgument} or from the list of
 * appendixes by the class {@link ArgumentFromAppendix}.
 *
 * @author Matthias
 *
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Log4j2
public class BeanOperation {

	// identifier of the operation
	@NonNull
	private final String methodIdentifier;
	// object which provides the requested method
	@NonNull
	private final ApplicationFrameworkInvokable implementation;
	// Operation list to be executed
	@NonNull
	private final Method method;
	// parameters
	@Singular
	private final List<Argument> arguments;

	/**
	 * @param proxy
	 * @param state
	 * @param args
	 * @return
	 */
	public ApplicationProtocol<?> execute(final ApplicationProtocol<?> state, final Object[] args) {

		final Object[] methodArguments = prepareArguments(state, args);
		ApplicationProtocol<?> result = null;
		try {
			result = (ApplicationProtocol<?>) this.method.invoke(this.implementation, methodArguments);

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			final String message = String.format("could not invoke configured target bean (%s::%s)",
					this.implementation.getClass().getName(), this.method.getName());
			log.atError().log(message);
			throw new ClassCastException(message);
		}

		return result;
	}

	private Object[] prepareArguments(final ApplicationProtocol<?> state, final Object[] args) {
		final Object[] result = this.arguments.stream()
				.map(a -> a.prepareArgument(state, args))
				.collect(Collectors.toList()).toArray();

		return result;
	}
}
