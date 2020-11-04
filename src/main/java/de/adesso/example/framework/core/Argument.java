package de.adesso.example.framework.core;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Optional;

import de.adesso.example.framework.ApplicationProtocol;
import de.adesso.example.framework.annotation.Required;
import de.adesso.example.framework.exception.RequiredParameterException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Getter(value = AccessLevel.PACKAGE)
@Log4j2
public abstract class Argument {

	/**
	 * type of the parameter of the implementing bean
	 */
	private final Class<?> type;

	/**
	 * Position of the parameter for the call of the bean implementing the method
	 * call. There are several procedures to create the argument list, this
	 * information will be set by the {@link MethodImplementation} during
	 * initialization. Since the implementing class might be a bean which is
	 */
	private int targetPosition;

	/**
	 * If the parameter is annotated as required, this flag will be set.
	 */
	private boolean required;

	/**
	 * If the parameter is required, it may be specified, that a collection should
	 * not be empty.
	 */
	private boolean requiredNotEmpty;

	private transient String parameterName;
	private transient BeanOperation beanOperation;

	public Argument(final Class<?> type) {
		this.type = type;
	}

	protected abstract Object prepareArgument(ApplicationProtocol<?> state, Object[] args);

	/**
	 * evaluate the parameters of the method and read the declared annotations. The
	 * annotations influence the behavior of execution. Every specific
	 * implementation of argument may overwrite this method and call super to set
	 * the field targetPosition.
	 *
	 * @param parameters
	 */
	void init(final BeanOperation beanOperation, final Parameter parameter, final int targetPosition) {
		this.targetPosition = targetPosition;
		this.parameterName = parameter.getName();
		this.beanOperation = beanOperation;

		this.checkRequiredAnnotation(parameter);
	}

	private void checkRequiredAnnotation(final Parameter parameter) {
		final Required annotation = parameter.getAnnotation(Required.class);
		this.required = annotation != null;
		if (this.required) {
			this.requiredNotEmpty = annotation.requireNotEmpty();
		}
	}

	protected void validateArgument(final Optional<?> result) {
		if (this.isRequired() && result.isEmpty()) {
			this.throwRequiredParameterException();
		}
	}

	protected void validateArgument(final ApplicationProtocol<?> result) {
		if (this.isRequired() && result == null) {
			this.throwRequiredParameterException();
		}
	}

	private void throwRequiredParameterException() {
		final String message = String.format("required parameter %s not found, will not call the bean %s",
				this.parameterName, this.beanOperation.getBeanType().getName());
		log.atInfo().log(message);
		throw new RequiredParameterException(message);
	}

	protected void validateArgumentCollection(final Collection<?> result) {
		if (this.isRequired() && result == null) {
			final String message = String.format("required parameter %s not found, will not call the bean %s%s",
					this.parameterName,
					this.beanOperation.getBeanType().getName(),
					this.beanOperation.getMethodIdentifier());
			log.atInfo().log(message);
			throw new RequiredParameterException(message);
		}
		if (this.requiredNotEmpty && result.isEmpty()) {
			final String message = String.format(
					"required collection %s in call %s::%s is empty, but should not be empty by annotation",
					this.parameterName,
					this.beanOperation.getBeanType().getName(),
					this.beanOperation.getMethodIdentifier());
			log.atInfo().log(message);
			throw new RequiredParameterException(message);
		}
	}

	protected Class<?> getBean() {
		return this.getBeanOperation().getBeanType();
	}

	protected String getBeanMethodIdentifier() {
		return this.getBeanOperation().getMethodIdentifier();
	}

	protected Class<?> getEmulatedInterface() {
		return this.getBeanOperation().getMethodImplementation().getDispatcher().getImplementationInterface();
	}

	protected Method getEmulatedInterfaceMethod() {
		return this.getBeanOperation().getMethodImplementation().getMethod();
	}

	protected Class<? extends Parameter> getTargetParameter() {
		return this.getBeanOperation().getMethod().getParameters()[this.getTargetPosition()].getClass();
	}
}