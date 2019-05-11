package chlx.hashing.minhash;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public interface MinHash {

	int[] signature(int[] sortedShingles);

	long[][] getCoefficients();

	int[] signature(boolean[] vector);

	int getSignatureSize();

}
