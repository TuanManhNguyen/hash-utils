package chlx.hashing.minhash;

import java.util.Arrays;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2019-01-15
 */
public class ContinuableMinHash64 {

	private final MinHashSimplified64 hash;
	private final int signatureSize;
	private final long[] sig;

	public ContinuableMinHash64(MinHashSimplified64 hash) {
		this.hash = hash;
		this.signatureSize = hash.getSignatureSize();
		this.sig = initSig(signatureSize);
	}

	public void add(long r) {
		for (int i = 0; i < signatureSize; i++) {
			sig[i] = Math.min(
					sig[i],
					hash.h(i, r));
		}
	}

	public long[] getSignature() {
		return Arrays.copyOf(sig, sig.length);
	}

	private static long[] initSig(int signatureSize) {
		long[] sig = new long[signatureSize];
		for (int i = 0; i < signatureSize; i++) {
			sig[i] = Long.MAX_VALUE;
		}
		return sig;
	}

	private static void union(long[] source, long[] target) {
		if (source.length != target.length) {
			throw new IllegalArgumentException("Sig sizes must be equal");
		}
		for (int i = 0; i < source.length; i++) {
			source[i] = Math.min(source[i], target[i]);
		}
	}

	public void union(ContinuableMinHash64 obj) {
		union(obj.sig);
	}

	public void union(long[] sig) {
		union(this.sig, sig);
	}

	public static long[] union(long[]... sigs) {
		if (sigs == null || sigs.length == 0) {
			throw new IllegalArgumentException("sigs contains no element");
		}
		if (sigs.length == 1) {
			return Arrays.copyOf(sigs[0], sigs[0].length);
		}

		long[] ret = initSig(sigs[0].length);
		for (long[] sig : sigs) {
			union(ret, sig);
		}
		return ret;
	}

}