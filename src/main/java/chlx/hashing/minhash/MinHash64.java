package chlx.hashing.minhash;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public interface MinHash64 {

	long[] signature(long[] sortedShingles);

	long[][] getCoefficients();

	long[] signature(boolean[] vector);

	int getSignatureSize();

	long h(final int i, final long x);

	long[] signature(long l);

}
