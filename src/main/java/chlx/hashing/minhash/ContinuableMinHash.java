package chlx.hashing.minhash;

import java.util.Arrays;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2019-01-15
 */
public class ContinuableMinHash {

	private final MinHashSimplified minHashSimplified;
	private final int signatureSize;
	private final int[] sig;

	public ContinuableMinHash(MinHashSimplified minHashSimplified) {
		this.minHashSimplified = minHashSimplified;
		this.signatureSize = minHashSimplified.getSignatureSize();
		sig = new int[signatureSize];
		for (int i = 0; i < signatureSize; i++) {
			sig[i] = Integer.MAX_VALUE;
		}
	}

	public void add(int r) {
		for (int i = 0; i < signatureSize; i++) {
			sig[i] = Math.min(
					sig[i],
					minHashSimplified.h(i, r));
		}
	}

	public int[] getSignature() {
		return Arrays.copyOf(sig, sig.length);
	}

}
