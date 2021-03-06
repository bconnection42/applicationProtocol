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
package de.adesso.example.framework.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import de.adesso.example.framework.annotation.Emulated;
import de.adesso.example.framework.annotation.Implementation;
import de.adesso.example.framework.core.BeanOperation.BeanOperationBuilder;
import de.adesso.example.framework.core.MethodImplementation.MethodImplementationBuilder;
import de.adesso.example.framework.exception.BuilderException;
import de.adesso.example.framework.exception.MissingAnnotationException;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * This class creates the factory for a proxy implementing an application
 * framework supported interface. It is not possible to provide this class as
 * Spring service, because it has to be instantiated early during the
 * initialization process which makes things a little more complicated. The
 * class cannot trust on injection of dependencies. All has to be provided
 * during instantiation.
 *
 * @author Matthias
 *
 */
@Log4j2
public class ApplicationProxyFactory implements FactoryBean<Object>, ApplicationContextAware {

	private final Class<Object> emulatedInterface;
	private Object generatedEmulation;
	private final ArgumentFactory argumentFactory;
	private ApplicationContext applicationContext;

	public ApplicationProxyFactory(final ArgumentFactory argumentFactory,
			final Class<Object> emulatedInterface) {
		Assert.notNull(emulatedInterface, "the emulated interface may not be null");
		this.argumentFactory = argumentFactory;
		this.emulatedInterface = emulatedInterface;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		applicationContext.getClassLoader();
	}

	public Object getObject() throws Exception {
		if (this.generatedEmulation == null) {
			this.generatedEmulation = this.emulateInterface(this.emulatedInterface);
			if (this.generatedEmulation instanceof ApplicationContextAware) {
				final ApplicationContextAware aca = (ApplicationContextAware) this.generatedEmulation;
				aca.setApplicationContext(this.applicationContext);
			}
			if (this.generatedEmulation instanceof InitializingBean) {
				final InitializingBean ib = (InitializingBean) this.generatedEmulation;
				ib.afterPropertiesSet();
			}
		}
		return this.generatedEmulation;
	}

	public Class<Object> getObjectType() {
		return this.emulatedInterface;
	}

	private Object emulateInterface(@NonNull final Class<Object> interfaceType) {
		this.validateClassAnnotations(interfaceType);

		// initialize the factory to build the proxy
		final DaisyChainDispatcherFactory factory = new DaisyChainDispatcherFactory(this.applicationContext)
				.emulationInterface(interfaceType);

		// add the methods of the interface to be emulated
		this.processAllMethods(interfaceType).stream()
				.forEach(m -> factory.implementation(m));

		return factory.build();
	}

	private <T> Collection<MethodImplementation> processAllMethods(final Class<T> interfaceType) {
		final List<MethodImplementation> implementations = new ArrayList<>();
		for (final Method m : interfaceType.getMethods()) {
			final MethodImplementation methodEmulation = this.buildMethodEmulation(m);
			methodEmulation.method(m);
			implementations.add(methodEmulation);
		}
		return implementations;
	}

	@SuppressWarnings("unchecked")
	private MethodImplementation buildMethodEmulation(final Method interfaceMethod) {
		Assert.notNull(interfaceMethod, "method is mandatory argument");

		final String methodName = interfaceMethod.getName();
		final MethodImplementationBuilder miBuilder = MethodImplementation.builder()
				.methodIdentifier(methodName);

		final Implementation[] implDef = interfaceMethod.getAnnotationsByType(Implementation.class);
		for (final Implementation implClass : implDef) {
			final BeanOperationBuilder operationBuilder = BeanOperation.builder();
			if (implClass.bean().isInterface()) {
				operationBuilder.anInterface(implClass.bean());
			} else {
				operationBuilder.beanType((Class<Object>) implClass.bean());
			}
			final String implMethod = implClass.method().isEmpty() ? methodName : implClass.method();
			operationBuilder.methodIdentifier(implMethod);
			final Method beanMethod = this.extractCorrespondingBeanMethod(implMethod, implClass.bean());
			operationBuilder.method(beanMethod);

			// found appropriate method, now instrument it
			final List<Argument> argumentList = ParameterPosition.buildParameterList(beanMethod).stream()
					.map(pp -> this.argumentFactory.createArgument(interfaceMethod, beanMethod, pp))
					.collect(Collectors.toList());
			operationBuilder.arguments(argumentList);
			miBuilder.beanOperation(operationBuilder.build());
		}
		return miBuilder.build();
	}

	private Method extractCorrespondingBeanMethod(@NonNull final String methodName, @NonNull final Class<?> implClass) {
		Method beanMethod = null;
		for (final Method implMethod : implClass.getMethods()) {
			if (methodName.equals(implMethod.getName())) {
				beanMethod = implMethod;
				return beanMethod;
			}
		}
		// nothing found, basic requirement: the implementation bean provides at least
		// one method with the same identifier
		throw BuilderException.methodNotFound(implClass, methodName);
	}

	/**
	 * call will fail, if the given interface is not annotated probably.
	 *
	 * @throws MissingAnnotationException if the annotation @Emulated is missing.
	 */
	private <T> void validateClassAnnotations(final Class<T> interfaceType) {
		final Emulated emulationAnnotation = interfaceType.getAnnotation(Emulated.class);
		if (emulationAnnotation == null) {
			final String message = "the interface has to be annotated as @Emulated";
			log.atError().log(message);
			throw new MissingAnnotationException(message);
		}
	}
}
