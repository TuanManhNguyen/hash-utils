package chlx.hashing.shingles;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.NotNull;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public interface Shingling {

	/**
	 * @return integer shingle set of a string
	 */
	@NotNull
	IntOpenHashSet toShingleSet(@NotNull String s);

	/**
	 * @return integer shingle set of a sub string
	 */
	@NotNull
	IntOpenHashSet toShingleSet(@NotNull String s, int startIndex, int endIndex);

	/**
	 * @return sorted integer shingles of a sub string
	 * @implSpec Shingles that equal to '0' and 'Integer.MIN_VALUE' (0x80000000) will not be returned
	 */
	@NotNull
	int[] toPositiveShingles(@NotNull String s);

	/**
	 * @return sorted integer shingles of a sub string
	 * @implSpec Shingles that equal to '0' and 'Integer.MIN_VALUE' (0x80000000) will not be returned
	 */
	@NotNull
	int[] toPositiveShingles(@NotNull String s, int startIndex, int endIndex);

	int getK();

}
