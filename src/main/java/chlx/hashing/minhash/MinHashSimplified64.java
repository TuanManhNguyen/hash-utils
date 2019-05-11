package chlx.hashing.minhash;

import java.security.InvalidParameterException;
import java.util.Random;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 *
 * Simplified version of {@link MinHashDictSized}
 * which use Integers for hashing functions which requires no modulus and casts
 */
public class MinHashSimplified64 implements MinHash64 {

	/**
	 * Signature size.
	 */
	private int signatureSize;
	/**
	 * Random a and b coefficients for the random hash functions.
	 */
	private long[][] hash_coefs;


	/**
	 * Initializes hash functions to compute MinHashDictSized signatures for sets built
	 * from a dictionary of dict_size elements.
	 *
	 * @param size the number of hash functions (and the size of resulting
	 *             signatures)
	 * @param seed random number generator seed. using the same value will
	 *             guarantee identical hashes across object instantiations
	 */
	public MinHashSimplified64(final int size, final long seed) {
		init(size, new Random(seed));
	}

	/**
	 * Initializes hash functions to compute MinHashDictSized signatures for sets built
	 * from a dictionary of dict_size elements.
	 *
	 * @param size the number of hash functions (and the size of resulting
	 *             signatures)
	 */
	public MinHashSimplified64(final int size) {
		init(size, new Random());
	}

	/**
	 * Computes the signature for this set. For example set = {0, 2, 3}
	 *
	 * @param longs
	 * @return the signature
	 */
	@Override
	public final long[] signature(final long[] longs) {
		long[] sig = new long[signatureSize];

		for (int i = 0; i < signatureSize; i++) {
			sig[i] = Long.MAX_VALUE;
		}

		for (final long r : longs) {
			// However, if c has 1 in row r, then for each i = 1, 2, . . . ,signatureSize
			// set SIG(i, c) to the smaller of the current value of
			// SIG(i, c) and hi(r)
			for (int i = 0; i < signatureSize; i++) {
				sig[i] = Math.min(
						sig[i],
						h(i, r));
			}
		}
		return sig;
	}

	@Override
	public long[][] getCoefficients() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Compute hash function coefficients using provided Random.
	 *
	 * @param size
	 * @param r
	 */
	private void init(final int size, final Random r) {
		if (size <= 0) {
			throw new InvalidParameterException(
					"Signature size should be positive");
		}

		this.signatureSize = size;

		// h = (a * x) + b
		// a and b should be randomly generated
		hash_coefs = new long[signatureSize][2];
		for (int i = 0; i < signatureSize; i++) {
			hash_coefs[i][0] = Math.abs(r.nextLong()); // a
			hash_coefs[i][1] = Math.abs(r.nextLong()); // b
		}
	}

	/**
	 * Computes hi(x) as (a_i * x + b_i) % dict_size.
	 *
	 * @param i
	 * @param x
	 * @return the hashed value of x, using ith hash function
	 */
	@Override
	public long h(final int i, final long x) {
		long ret = hash_coefs[i][0] * x + hash_coefs[i][1];
		return ret == Long.MIN_VALUE ? 0 : Math.abs(ret);
	}

	@Override
	public long[] signature(long l) {
		long[] ret = new long[signatureSize];
		for (int i = 0; i < signatureSize; i++) {
			ret[i] = h(i, l);
		}
		return ret;
	}

	@Override
	public long[] signature(boolean[] vector) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getSignatureSize() {
		return signatureSize;
	}

}
