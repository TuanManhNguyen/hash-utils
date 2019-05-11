package chlx.hashing.shingles;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.NotNull;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public class CharacterBasedShingling implements Shingling {

	private final int k;

	public CharacterBasedShingling(int k) {
		if (k <= 0) {
			throw new IllegalArgumentException("k must be greater than 0");
		}
		this.k = k;
	}

	private int partiallyHash(String s, int start, int end) {
		int hash = 0;
		for (int i = start; i < end; ++i) {
			hash = hash * 31 + s.charAt(i);
		}
		return hash;
	}

	@NotNull
	@Override
	public IntOpenHashSet toShingleSet(@NotNull String s, int startIndex, int endIndex) {
		if (startIndex < 0) {
			throw new IllegalArgumentException("contentStart can't be smaller than 0");
		}
		IntOpenHashSet ret = new IntOpenHashSet();
		if (endIndex - startIndex < k) {
			ret.add(partiallyHash(s, startIndex, endIndex));
			return ret;
		}

		for (int i = 0; i <= endIndex - k; ++i) {
			ret.add(partiallyHash(s, i, i + k));
		}

		return ret;
	}

	@NotNull
	@Override
	public IntOpenHashSet toShingleSet(@NotNull String s) {
		return toShingleSet(s, 0, s.length());
	}

	@NotNull
	@Override
	public int[] toPositiveShingles(@NotNull String s) {
		return toPositiveShingles(s, 0, s.length());
	}

	@NotNull
	@Override
	public int[] toPositiveShingles(@NotNull String s, int startIndex, int endIndex) {
		if (startIndex < 0) {
			throw new IllegalArgumentException("contentStart can't be smaller than 0");
		}
		IntOpenHashSet ret = new IntOpenHashSet();
		if (endIndex - startIndex < k) {
			int hash = Math.abs(partiallyHash(s, startIndex, endIndex));
			if (hash > 0) {
				ret.add(hash);
			}
			return ret.toIntArray();
		}

		for (int i = 0; i <= endIndex - k; ++i) {
			int hash = Math.abs(partiallyHash(s, i, i + k));
			if (hash > 0) {
				ret.add(hash);
			}
		}

		return ret.toIntArray();
	}

	@Override
	public int getK() {
		return k;
	}

}
