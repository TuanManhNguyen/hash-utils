package chlx.hashing.shingles;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 *
 * k-shingling is the operation of transforming a string (or text document) into
 * a set of n-grams, which can be used to measure the similarity between two
 * strings or documents.
 */
public class SimpleWordBasedShingling implements Shingling {

	static final CharSet STOP_WORD_SYMBOL_SET;
	private static final char SPACE = ' ';

	static {
		char[] stopWordSymbols = new char[]{
				' ', '\t', '|', '!', '@', '#', '$', '.', '.',
				'%', '\\', '^', '*', ')', '(', '}', '{', '+',
				'=', ']', '[', '?', '/', '&', '\'', '\"', ',',
				'\n'
		};
		STOP_WORD_SYMBOL_SET = new CharOpenHashSet();
		for (char ch : stopWordSymbols) {
			STOP_WORD_SYMBOL_SET.add(ch);
		}
	}

	private final int nGram;
	private final int[] track;

	public SimpleWordBasedShingling(int nGram) {
		if (nGram <= 0) {
			throw new IllegalArgumentException("nGram must be greater than 0");
		}
		this.nGram = nGram;
		track = new int[nGram];
	}

	private int increaseHash(int hash, char ch) {
		return hash * 31 + ch;
	}

	/**
	 * Shingles that equal to '0' will not be returned
	 */
	@Override
	@NotNull
	public IntOpenHashSet toShingleSet(@NotNull String s) {
		return toShingleSet(s, 0, s.length());
	}

	/**
	 * Shingles that equal to '0' will not be returned
	 */
	@Override
	@NotNull
	public IntOpenHashSet toShingleSet(@NotNull String s, int startIndex, int endIndex) {
		if (startIndex < 0) {
			throw new IllegalArgumentException("contentStart can't be smaller than 0");
		}

		IntOpenHashSet ret = new IntOpenHashSet();
		Arrays.fill(track, 0);
		int current = 0;

		for (int charIndex = startIndex; charIndex < endIndex; ++charIndex) {
			char ch = s.charAt(charIndex);
			if (STOP_WORD_SYMBOL_SET.contains(ch)) {
				if (track[current] != 0) {
					current = (current + 1) % nGram;
					if (track[current] != 0) {
						ret.add(track[current]);
						track[current] = 0;
					}
					for (int i = 0; i < nGram; ++i) {
						track[i] = track[i] != 0 ? increaseHash(track[i], SPACE) : track[i];
					}
				}
			} else {
				track[current] = increaseHash(track[current], ch);
				int i = (current + 1) % nGram;
				while (i != current) {
					if (track[i] != 0) {
						track[i] = track[i] != 0 ? increaseHash(track[i], ch) : track[i];
					}
					i = (i + 1) % nGram;
				}
			}
		}

		if (track[current] != 0) {
			current = (current + 1) % nGram;
			if (track[current] != 0) {
				ret.add(track[current]);
			}
		}

		return ret;
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
		Arrays.fill(track, 0);
		int current = 0;

		for (int charIndex = startIndex; charIndex < endIndex; ++charIndex) {
			char ch = s.charAt(charIndex);
			if (STOP_WORD_SYMBOL_SET.contains(ch)) {
				if (track[current] != 0) {
					current = (current + 1) % nGram;
					if (track[current] != 0) {
						int hash = Math.abs(track[current]);
						if (hash > 0) {
							ret.add(hash);
						}
						track[current] = 0;
					}
					for (int i = 0; i < nGram; ++i) {
						track[i] = track[i] != 0 ? increaseHash(track[i], SPACE) : track[i];
					}
				}
			} else {
				track[current] = increaseHash(track[current], ch);
				int i = (current + 1) % nGram;
				while (i != current) {
					if (track[i] != 0) {
						track[i] = track[i] != 0 ? increaseHash(track[i], ch) : track[i];
					}
					i = (i + 1) % nGram;
				}
			}
		}

		if (track[current] != 0) {
			current = (current + 1) % nGram;
			if (track[current] != 0) {
				int hash = Math.abs(track[current]);
				if (hash > 0) {
					ret.add(hash);
				}
			}
		}

		return ret.toIntArray();
	}

	@Override
	public int getK() {
		return nGram;
	}

}
