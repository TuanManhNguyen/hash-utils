package chlx.hashing.lsh;

/**
 * Implementation of Locality Sensitive Hashing (LSH) principle, as described in
 * Leskovec, Rajaraman & Ullman (2014), "Mining of Massive Datasets",
 * Cambridge University Press.
 *
 * @implSpec This has been modified to a no-bucket used version
 */
public abstract class LSH {

	private static final long LARGE_PRIME = 433494437;

	private final int stages;

	/**
	 * Instantiates a LSH instance with s stages (or bands) in a space with n dimensions.
	 *
	 * @param stages stages
	 */
	public LSH(final int stages) {
		this.stages = stages;
	}

	/**
	 * Hash a signature.
	 * The signature is divided in s stages (or bands)
	 *
	 * @param signature
	 * @return An vector of s integers (between 0 and b-1)
	 */
	public final int[] hashSignature(final int[] signature) {

		// Create an accumulator for each stage
		int[] hash = new int[stages];

		// Number of rows per stage
		int rows = signature.length / stages;

		for (int i = 0; i < signature.length; i++) {
			int stage = Math.min(i / rows, stages - 1);
			hash[stage] = (int)
					((hash[stage] + (long) signature[i] * LARGE_PRIME));

		}

		return hash;
	}

	public abstract int[] hashShingles(final int[] sortedShingles);

	public abstract int[] signature(final int[] sortedShingles);

	public int getStages() {
		return stages;
	}

}
