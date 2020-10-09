package de.adesso.example;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import de.adesso.example.application.Article;
import de.adesso.example.application.BasePriceCalculator;
import de.adesso.example.application.EmployeeDiscountCalculator;
import de.adesso.example.application.PriceCalculatorInterface;
import de.adesso.example.application.VoucherDiscountCalculator;
import de.adesso.example.application.employment.Employee;
import de.adesso.example.application.employment.Employment;
import de.adesso.example.application.marketing.Marketing;
import de.adesso.example.application.marketing.Voucher;
import de.adesso.example.framework.ArgumentFromAppendix;
import de.adesso.example.framework.BeanOperation;
import de.adesso.example.framework.DaisyChainDispatcherFactory;
import de.adesso.example.framework.FunctionSignatureArgument;
import de.adesso.example.framework.MethodImplementation;

@Configuration
public class ApplicationConfig {

	@Autowired
	private BasePriceCalculator basePriceCalculator;

	@Autowired
	private EmployeeDiscountCalculator employeeDiscountCalculator;
	
	@Autowired
	private VoucherDiscountCalculator voucherDiscountCalculator;

	@Bean
	@Scope(scopeName = "singelton")
	PriceCalculatorInterface priceCalculator() {
		return new DaisyChainDispatcherFactory()
				.implementationInterface(PriceCalculatorInterface.class)
				.operation(MethodImplementation.builder()
						.methodIdentifier("calculatePrice")
						.returnValueType(BigDecimal.class)
						// first call BasePriceCalculator
						.beanOperation(BeanOperation.builder()
								.implementation(basePriceCalculator)
								.methodIdentifier("calculatePrice")
								.argument(new FunctionSignatureArgument (Article.class, 0))
								.build())
						// second call EmployeeDiscountCalculator
						.beanOperation(BeanOperation.builder()
								.implementation(employeeDiscountCalculator)
								.methodIdentifier("calculatePrice")
								.argument(new FunctionSignatureArgument (Article.class, 0))
								.argument(new ArgumentFromAppendix(Employee.class, Employment.employeeAppendixId))
								.build())
						// third call VoucherDiscountCalculator
						.beanOperation(BeanOperation.builder()
								.implementation(voucherDiscountCalculator)
								.methodIdentifier("calculatePrice")
								.argument(new FunctionSignatureArgument (Article.class, 0))
								.argument(new ArgumentFromAppendix(Voucher.class, Marketing.voucherAppendixId))
								.build())
						.build())
				.build();
	}
}
