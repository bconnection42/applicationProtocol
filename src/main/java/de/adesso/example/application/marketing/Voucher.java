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
package de.adesso.example.application.marketing;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Voucher implements Serializable {

	private static final long serialVersionUID = 6119269215970563757L;

	/** basic voucher type */
	private final VoucherType type;
	/** unique identifier of the voucher */
	private final String voucherId;
	/** How often can the voucher be used */
	private int maxApplications = 1;
	/** where is the voucher applicable */
	private final Set<VoucherApplication> applicableAt = new HashSet<>();
	/** used, when the system tries to use the vouchers */
	private transient int tryUse = 0;

	/** voucher compatibility with other vouchers */
	private final VoucherCompatibility compatibility;

	public Voucher(final String voucherId, final VoucherCompatibility compatibility, final VoucherType type) {
		this.voucherId = voucherId;
		this.type = type;
		this.compatibility = compatibility;
	}

	public Voucher(final String voucherId, final VoucherCompatibility compatibility, final VoucherType type,
			final int maxApplications) {
		this.voucherId = voucherId;
		this.maxApplications = maxApplications;
		this.type = type;
		this.compatibility = compatibility;
	}

	public Voucher(final String voucherId, final VoucherCompatibility compatibility, final VoucherType type,
			final VoucherApplication... applicableAt) {
		this.voucherId = voucherId;
		this.applicableAt.addAll(Set.of(applicableAt));
		this.type = type;
		this.compatibility = compatibility;
	}

	public Voucher(final String voucherId, final VoucherCompatibility compatibility, final VoucherType type,
			final int maxApplications,
			final VoucherApplication... applicableAt) {
		this.voucherId = voucherId;
		this.maxApplications = maxApplications;
		this.applicableAt.addAll(Set.of(applicableAt));
		this.type = type;
		this.compatibility = compatibility;
	}

	public void utilize() {
		if (this.maxApplications == 0) {
			throw VoucherNotUtilizableException.notUtilizable(this);
		}
		this.maxApplications--;
	}

	public void tryUtilize() {
		if (this.maxApplications - this.tryUse == 0) {
			throw VoucherNotUtilizableException.notUtilizable(this);
		}
		this.tryUse++;
	}

	public void resetTryUse() {
		this.tryUse = 0;
	}

	public boolean isUtilizable() {
		return this.maxApplications > 0;
	}

	public boolean isTryUtilizable() {
		return this.maxApplications - this.tryUse > 0;
	}

	public boolean isTopDog() {
		return this.compatibility == VoucherCompatibility.TOP_DOG;
	}

	public boolean isCompatible(final Voucher otherVoucher) {
		// top dogs are not compatible
		if (this.isOneOfCompatibility(otherVoucher, VoucherCompatibility.TOP_DOG)) {
			return false;
		}

		// standalone are compatible, if they have different type
		if (this.isOneOfCompatibility(otherVoucher, VoucherCompatibility.STAND_ALONE_WITHIN_TYPE)
				&& this.type == otherVoucher.type) {
			return false;
		}
		return true;
	}

	private boolean isOneOfCompatibility(final Voucher otherVoucher, final VoucherCompatibility compatibility) {
		return this.compatibility == compatibility || otherVoucher.compatibility == compatibility;
	}

	public StringBuilder toString(final StringBuilder sb, final int indent) {
		return this.identation(sb, indent).append(this.toString()).append("\n");
	}

	private StringBuilder identation(final StringBuilder sb, final int tabs) {
		for (int i = 0; i < tabs; i++) {
			sb.append('\t');
		}
		return sb;
	}
}
