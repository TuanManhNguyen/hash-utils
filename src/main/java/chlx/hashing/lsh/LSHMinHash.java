/*
 * The MIT License
 *
 * Copyright 2015 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package chlx.hashing.lsh;

import chlx.hashing.minhash.MinHash;
import chlx.hashing.minhash.MinHashDictSized;
import chlx.hashing.minhash.MinHashSimplified;

/**
 * @author Thibault Debatty
 * @implNote this has been modified to fit to our needs
 */
public class LSHMinHash extends LSH {

	private final MinHash mh;

	/**
	 * Instantiates a LSH instance that internally uses MinHash,
	 * with stages (or bands) for sets out of a
	 * dictionary of dictionarySize elements.
	 *
	 * @param stages         stages
	 * @param dictionarySize dictionary size
	 */
	public LSHMinHash(final int stages, final long dictionarySize, final double threshold) {
		super(stages);
		int signature_size = computeSignatureSize(stages, threshold);
		if (dictionarySize != Integer.MAX_VALUE) {
			this.mh = new MinHashDictSized(signature_size, dictionarySize);
		} else {
			this.mh = new MinHashSimplified(signature_size);
		}
	}

	/**
	 * Instantiates a LSH instance that internally uses MinHash,
	 * with stages (or bands) and b buckets (per stage), for sets out of a
	 * dictionary of dictionarySize elements.
	 *
	 * @param stages         stages
	 * @param dictionarySize dictionary size
	 * @param seed           random number generator seed. using the same value will
	 *                       guarantee identical hashes across object instantiations
	 */
	public LSHMinHash(final int stages, final int dictionarySize, final long seed) {
		super(stages);
		int signatureSize = computeSignatureSize(stages, dictionarySize);
		if (dictionarySize != Integer.MAX_VALUE) {
			this.mh = new MinHashDictSized(signatureSize, dictionarySize, seed);
		} else {
			this.mh = new MinHashSimplified(signatureSize, seed);
		}
	}

	/**
	 * Instantiates a LSH instance that internally uses MinHash,
	 * with stages (or bands) for sets out of a
	 * dictionary of dictionarySize elements.
	 *
	 * @param stages         stages
	 * @param dictionarySize dictionary size
	 * @param threshold      minimum similarity threshold
	 * @param seed           random number generator seed. using the same value will
	 *                       guarantee identical hashes across object instantiations
	 */
	public LSHMinHash(final int stages, final int dictionarySize, final long seed, final double threshold) {
		super(stages);
		int signatureSize = computeSignatureSize(stages, threshold);
		if (dictionarySize != Integer.MAX_VALUE) {
			this.mh = new MinHashDictSized(signatureSize, dictionarySize, seed);
		} else {
			this.mh = new MinHashSimplified(signatureSize, seed);
		}
	}

	/**
	 * Compute the size of the signature according to "Mining of Massive
	 * Datasets" p88.
	 * It can be shown that, using MinHash, the probability that the
	 * signatures of 2 sets with Jaccard similarity s agree in all the
	 * rows of at least one stage (band), and therefore become a candidate
	 * pair, is 1−(1−s^R)^b
	 * where R = signature_size / b (number of rows in a stage/band)
	 * Thus, the curve that shows the probability that 2 items fall in the
	 * same bucket for at least one of the stages, as a function of their
	 * Jaccard index similarity, has a S shape.
	 * The threshold (the value of similarity at which the probability of
	 * becoming a candidate is 1/2) is a function of the number of stages
	 * (s, or bands b in the book) and the signature size:
	 * threshold ≃ (1/s)^(1/R)
	 * Hence the signature size can be computed as:
	 * R = ln(1/s) / ln(threshold)
	 * signature_size = R * b
	 */
	private int computeSignatureSize(final int stages, final double threshold) {
		if (threshold == 1.0) {
			return 20;
		}
		int rowsPerStage = (int) Math.ceil(Math.log(1.0 / stages) / Math.log(threshold));
		return rowsPerStage * stages;
	}

	@Override
	public int[] hashShingles(final int[] sortedShingles) {
		return hashSignature(this.mh.signature(sortedShingles));
	}

	@Override
	public int[] signature(final int[] sortedShingles) {
		return this.mh.signature(sortedShingles);
	}

	/**
	 * Get the coefficients used by internal hashing functions.
	 *
	 * @return min hash coefficients
	 */
	public final long[][] getCoefficients() {
		return mh.getCoefficients();
	}


}
