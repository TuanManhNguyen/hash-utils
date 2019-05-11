package chlx.hashing.similarity;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 *
 * Calculate the Jaccard Index between sets
 */
public class JaccardIndex {

	private static final int MIN_SIM_THRESHOLD_MIN_WORTHY_LENGTH = 20;

	/**
	 * Calculate similarity between 2 set
	 */
	public static double index(@NotNull Set <?> s1, @NotNull Set <?> s2) {
		if (s1.isEmpty() && s2.isEmpty()) {
			return 1.0;
		}
		int count = 0;
		for (Object i : s1) {
			if (s2.contains(i)) {
				count++;
			}
		}
		return (double) count / (s1.size() + s2.size() - count);
	}

	/**
	 * Calculate similarity between 2 sorted arrays
	 */
	public static double index(@NotNull Object[] s1, @NotNull Object[] s2) {
		if (s1.length == 0 && s2.length == 0) {
			return 1.0;
		}
		int count = 0;
		for (Object i : s1) {
			if (Arrays.binarySearch(s2, i) >= 0) {
				count++;
			}
		}
		return (double) count / (s1.length + s2.length - count);
	}

	/**
	 * Calculate similarity between 2 min-hashes
	 */
	public static double indexMinHash(@NotNull int[] minhash1, @NotNull int[] minhash2) {
		if (minhash1.length != minhash2.length) {
			throw new IllegalArgumentException("Min hash lengths must be the same");
		}
		int simCount = 0;
		for (int i = 0; i < minhash1.length; i++) {
			if (minhash1[i] == minhash2[i]) {
				++simCount;
			}
		}
		return (double) simCount / minhash1.length;
	}

	/**
	 * Calculate similarity between 2 min-hashes
	 */
	public static double indexMinHash(@NotNull long[] minhash1, @NotNull long[] minhash2) {
		if (minhash1.length != minhash2.length) {
			throw new IllegalArgumentException("Min hash lengths must be the same");
		}
		int simCount = 0;
		for (int i = 0; i < minhash1.length; i++) {
			if (minhash1[i] == minhash2[i]) {
				++simCount;
			}
		}
		return (double) simCount / minhash1.length;
	}


	/**
	 * Calculate similarity between 2 min-hashes (faster than normal index)
	 *
	 * @implSpec if similarity < minSimThreshold then return 0;
	 */
	public static double indexMinHashWithMinSimThreshold(double minSimThreshold, @NotNull int[] minhash1, @NotNull int[] minhash2) {
		if (minhash1.length != minhash2.length) {
			throw new IllegalArgumentException("Min hash lengths must be the same");
		}
		int diffCount = 0;
		int diffMaxThreshold = (int) (minSimThreshold * minhash1.length);
		for (int i = 0, j = 0; i < minhash1.length && j < minhash2.length;) {
			if (minhash1[i] == minhash2[j]) {
				++i;
				++j;
			} else {
				++diffCount;
				if (minhash1[i] > minhash2[j]) {
					++j;
				} else {
					++i;
				}
			}
			if (diffCount > diffMaxThreshold) {
				return 0;
			}
		}

		return (double) (minhash1.length - diffCount) / minhash1.length;
	}

	/**
	 * Calculate similarity between 2 sorted int arrays
	 */
	public static double index(@NotNull int[] s1, @NotNull int[] s2) {
		if (s1.length == 0 && s2.length == 0) {
			return 1.0;
		}
		int count = 0;

		int i1 = 0;
		int i2 = 0;
		while (i1 < s1.length && i2 < s2.length) {
			if (s1[i1] < s2[i2]) {
				++i1;
			} else if (s2[i2] < s1[i1]) {
				++i2;
			} else {
				++count;
				++i1;
				++i2;
			}
		}

		return (double) count / (s1.length + s2.length - count);
	}


	/**
	 * Calculate similarity between 2 sorted int list
	 */
	public static double index(@NotNull List<Long> s1, @NotNull List<Long> s2) {
		if (s1.size() == 0 && s2.size() == 0) {
			return 1.0;
		}
		int count = 0;

		int i1 = 0;
		int i2 = 0;
		while (i1 < s1.size() && i2 < s2.size()) {
			if (s1.get(i1) < s2.get(i2)) {
				++i1;
			} else if (s2.get(i2) < s1.get(i1)) {
				++i2;
			} else {
				++count;
				++i1;
				++i2;
			}
		}

		return (double) count / (s1.size() + s2.size() - count);
	}

	/**
	 * Calculate similarity between 2 sorted long arrays
	 */
	public static double index(@NotNull long[] s1, @NotNull long[] s2) {
		if (s1.length == 0 && s2.length == 0) {
			throw new RuntimeException("Both inputs set are empty");
		}
		int count = 0;

		int i1 = 0;
		int i2 = 0;
		while (i1 < s1.length && i2 < s2.length) {
			if (s1[i1] < s2[i2]) {
				++i1;
			} else if (s2[i2] < s1[i1]) {
				++i2;
			} else {
				++count;
				++i1;
				++i2;
			}
		}

		return (double) count / (s1.length + s2.length - count);
	}

	/**
	 * @implSpec Calculate similarity between 2 sorted long arrays with unique element
	 * if similarity < minSimThreshold then return 0;
	 * this has been optimized for {@link chlx.hashing.lsh.LSHComputer}
	 */
	public static double indexWithMinThreshold(double minSimThreshold, @NotNull long[] s1, @NotNull long[] s2) {
		if (s1.length == 0) {
			if (s2.length == 0) {
				return 1;
			}
			return 0;
		} else if (s2.length == 0) {
			return 0;
		}

		int maxDif1 = (int) (s1.length * (1 - minSimThreshold));
		int maxDif2 = (int) (s2.length * (1 - minSimThreshold));

		if (s1[maxDif1] > s2[s2.length - 1] || s2[maxDif2] > s1[s1.length - 1]) {
			return 0;
		}

		if (s1.length < MIN_SIM_THRESHOLD_MIN_WORTHY_LENGTH || s2.length < MIN_SIM_THRESHOLD_MIN_WORTHY_LENGTH) {
			double sim = index(s1, s2);
			return sim < minSimThreshold ? 0 : sim;
		}

		int count = 0;

		int i1 = 0;
		int i2 = 0;
		int dif1 = 0;
		int dif2 = 0;
		while (i1 < s1.length && i2 < s2.length) {
			if (s1[i1] < s2[i2]) {
				++i1;
				++dif1;
				if (dif1 > maxDif1) {
					return 0;
				}
			} else if (s2[i2] < s1[i1]) {
				++i2;
				++dif2;
				if (dif2 > maxDif2) {
					return 0;
				}
			} else {
				++count;
				++i1;
				++i2;
			}
		}

		return (double) count / (s1.length + s2.length - count);
	}

	/**
	 * Calculate similarity between 2 sorted int arrays (faster than normal index)
	 *
	 * @implSpec if similarity < minSimThreshold then return 0;
	 */
	public static double indexWithMinThreshold(double minSimThreshold, @NotNull int[] s1, @NotNull int[] s2) {
		if (s1.length < MIN_SIM_THRESHOLD_MIN_WORTHY_LENGTH || s2.length < MIN_SIM_THRESHOLD_MIN_WORTHY_LENGTH) {
			double sim = index(s1, s2);
			return sim < minSimThreshold ? 0 : sim;
		}

		if (s1.length == 0) {
			if (s2.length == 0) {
				return 1;
			}
			return 0;
		} else if (s2.length == 0) {
			return 0;
		}

		int count = 0;
		int maxDif1 = (int) (s1.length * (1 - minSimThreshold));
		int maxDif2 = (int) (s2.length * (1 - minSimThreshold));

		int i1 = 0;
		int i2 = 0;
		int dif1 = 0;
		int dif2 = 0;
		while (i1 < s1.length && i2 < s2.length) {
			if (s1[i1] < s2[i2]) {
				++i1;
				++dif1;
				if (dif1 > maxDif1) {
					return 0;
				}
			} else if (s2[i2] < s1[i1]) {
				++i2;
				++dif2;
				if (dif2 > maxDif2) {
					return 0;
				}
			} else {
				++count;
				++i1;
				++i2;
			}
		}

		return (double) count / (s1.length + s2.length - count);
	}

	public static long countIntersect(long count1, long count2, double simIndex) {
		return (long) ((count1 + count2) * simIndex / (1 + simIndex));
	}

	public static long countUnion(long count1, long count2, double simIndex) {
		return (long) ((count1 + count2) / (1 + simIndex));
	}

}
