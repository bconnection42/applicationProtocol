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

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import de.adesso.example.framework.ApplicationAppendix;
import de.adesso.example.framework.ApplicationProtocol;
import lombok.NonNull;

public class ArgumentSetFromAppendix extends Argument {

	private final Class<? extends ApplicationAppendix<?>> appendixClass;

	public ArgumentSetFromAppendix(@NonNull final Class<?> type,
			@NonNull final Class<? extends ApplicationAppendix<?>> appendixClass) {
		super(type);
		this.appendixClass = appendixClass;
	}

	@Override
	protected Set<?> prepareArgument(final ApplicationProtocol<?> state, final Object[] args) {
		final Collection<ApplicationAppendix<?>> result = state.getAllAppenixesOfTypeAsSet(this.appendixClass);

		this.validateArgumentCollection(result);

		return result.stream()
				.map(a -> a.getContent())
				.collect(Collectors.toSet());
	}
}
