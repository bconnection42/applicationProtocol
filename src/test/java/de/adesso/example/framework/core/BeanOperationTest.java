package de.adesso.example.framework.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import de.adesso.example.framework.ApplicationProtocol;

@RunWith(SpringRunner.class)
public class BeanOperationTest {

	@Mock
	private ApplicationContext contextMock;

	@Mock
	private BeanOperation beanOperationMock;

	@Mock
	private MethodImplementation methodImplMock;

	@Before
	public void setup() throws NoSuchMethodException, SecurityException {
		when(this.beanOperationMock.getMethodImplementation())
				.thenReturn(this.methodImplMock);
		final Method testMethod = TestImplementation.class.getMethod("testMethod", String.class, int.class);
		when(this.methodImplMock.getMethod())
				.thenReturn(testMethod);
	}

	@Test
	public void testBuilder() throws NoSuchMethodException, SecurityException {
		final String methodIdentifier = "testMethod";
		final TestImplementation implementation = new TestImplementation();
		final Method declaredMethod = TestImplementation.class.getDeclaredMethod(methodIdentifier, String.class,
				int.class);

		final BeanOperation operation = BeanOperation.builder()
				.implementation(implementation)
				.methodIdentifier(methodIdentifier)
				.argument(new ArgumentFromMethod(String.class, 0))
				.argument(new ArgumentFromMethod(int.class, 1))
				.build();
		operation.init(this.methodImplMock, this.contextMock);

		assertThat(operation)
				.isNotNull()
				.isInstanceOf(BeanOperation.class);
		assertThat(operation.getImplementation())
				.isNotNull()
				.isEqualTo(implementation);
		assertThat(operation.getMethod())
				.isNotNull()
				.isEqualTo(declaredMethod);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuilderMissingMethodIdentifier() throws NoSuchMethodException, SecurityException {
		final TestImplementation implementation = new TestImplementation();

		BeanOperation.builder()
				.implementation(implementation)
				.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuilderMissingImplementation() throws NoSuchMethodException, SecurityException {
		final String methodIdentifier = "testMethod";

		final BeanOperation operation = BeanOperation.builder()
				.methodIdentifier(methodIdentifier)
				.build();
		operation.init(this.methodImplMock, this.contextMock);
	}

	@Test
	public void testGetArguments() throws NoSuchMethodException, SecurityException {
		final String methodIdentifier = "testMethod";
		final TestImplementation implementation = new TestImplementation();

		final BeanOperation operation = BeanOperation.builder()
				.implementation(implementation)
				.methodIdentifier(methodIdentifier)
				.argument(new ArgumentFromMethod(String.class, 0))
				.argument(new ArgumentFromMethod(int.class, 1))
				.build();

		assertThat(operation)
				.isNotNull()
				.isInstanceOf(BeanOperation.class);
		final List<Argument> arguments = operation.getArguments();
		assertThat(arguments)
				.isNotNull()
				.hasSize(2);
		assertThat(arguments.get(0))
				.isNotNull()
				.isInstanceOf(ArgumentFromMethod.class);
		final Argument firstArgument = arguments.get(0);
		assertThat(firstArgument.getType())
				.isEqualTo(String.class);
		assertThat(arguments.get(1))
				.isNotNull()
				.isInstanceOf(ArgumentFromMethod.class);
		final Argument secondArgument = arguments.get(1);
		assertThat(secondArgument.getType())
				.isEqualTo(int.class);
	}

	@Test
	public void testExecute() throws NoSuchMethodException, SecurityException {
		final String methodIdentifier = "testMethod";
		final TestImplementation implementation = new TestImplementation();

		final BeanOperation operation = BeanOperation.builder()
				.implementation(implementation)
				.methodIdentifier(methodIdentifier)
				.argument(new ArgumentFromMethod(String.class, 0))
				.argument(new ArgumentFromMethod(int.class, 1))
				.build();
		operation.init(this.methodImplMock, this.contextMock);
		final String testString = "Das ist ein netter kleiner Teststring : ";
		final int testInt = 7;
		final Object[] args = { testString, testInt };
		final ApplicationProtocol<String> state = new ApplicationProtocol<>();
		final ApplicationProtocol<?> newState = operation.execute(state, args);

		assertThat(newState)
				.isNotNull();
		assertThat(newState.getResult())
				.isNotNull()
				.isInstanceOf(String.class)
				.isEqualTo(testString + testInt);
	}

	private class TestImplementation {

		@SuppressWarnings("unused")
		public ApplicationProtocol<String> testMethod(final String a, final int b) {
			final ApplicationProtocol<String> state = new ApplicationProtocol<>();
			state.setResult(a + b);

			return state;
		}
	}
}
