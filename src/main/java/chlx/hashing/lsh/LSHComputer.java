package chlx.hashing.lsh;

import chlx.hashing.shingles.Shingling;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public class LSHComputer {

	protected final Shingling shingling;
	protected final LSH lsh;
	//	 K value of Shingling
	private final int k;

	public LSHComputer(Shingling shingling, LSH lsh) {
		this.shingling = shingling;
		this.k = shingling.getK();
		this.lsh = lsh;
	}

	public int getStages() {
		return lsh.getStages();
	}

	@Nullable
	public int[] signature(@NotNull String s) {
		int[] sortedShingles = shingling.toPositiveShingles(s);
		if (sortedShingles.length > k) {
			return lsh.hashShingles(sortedShingles);
		} else {
			return null;
		}
	}

	@Nullable
	public int[] signature(@NotNull List<String> words) {
		if (words.size() < k) {
			return null;
		}

		IntOpenHashSet uniqueShingles = new IntOpenHashSet();
		words.forEach(word -> {
			int hash = Math.abs(word.hashCode());
			if (hash >= 0) {
				uniqueShingles.add(hash);
			}
		});

		if (uniqueShingles.size() < k) {
			return null;
		}

		int[] shingles = uniqueShingles.toIntArray();
		Arrays.sort(shingles);
		return lsh.hashShingles(shingles);
	}

	public Signature64Converter createSig64Converter() {
		return new Signature64Converter(getStages(), Signature64Converter.NO_DUP_ELEMENT_MIN_APPEARANCE);
	}

	public Signature64Converter createReducedSig64Converter(int reducedStages, int dupElementMinAppearance) {
		return new Signature64Converter(reducedStages, dupElementMinAppearance);
	}

	public class Signature64Converter {

		public static final int NO_DUP_ELEMENT_MIN_APPEARANCE = 0;

		private final boolean reduceSigSize;
		private final int dupElementMinAppearance;
		private final int reducedStages;
		private final int stage64Bit;

		private Signature64Converter(int reducedStages, int dupElementMinAppearance) {
			if (reducedStages > getStages()) {
				throw new IllegalArgumentException("reduced stage 32 size cannot be bigger than LSHComputer's stages");
			}

			// at least 2 and at most 4, just because I did not test all cases..
			dupElementMinAppearance = (dupElementMinAppearance <= 2 ||
					(dupElementMinAppearance > reducedStages / 2 && dupElementMinAppearance <= 4)) ?
					NO_DUP_ELEMENT_MIN_APPEARANCE : (dupElementMinAppearance > 4 ? 4 : dupElementMinAppearance);

			this.dupElementMinAppearance = dupElementMinAppearance;
			this.reduceSigSize = dupElementMinAppearance != NO_DUP_ELEMENT_MIN_APPEARANCE;
			this.reducedStages = reducedStages;
			this.stage64Bit = compute64BitStage(reducedStages, dupElementMinAppearance);

		}

		public int getStage64Bit() {
			return stage64Bit;
		}

		public int getReducedStages() {
			return reducedStages;
		}

		public int getNoReducedStage64Bit() {
			return compute64BitStage(getStages(), NO_DUP_ELEMENT_MIN_APPEARANCE);
		}

		// Combinatoric of (stage choose 2) - dupElementMinAppearance
		public int compute64BitStage(int reducedStage, int dupElementMinAppearance) {
			if (reducedStage < 0) {
				throw new IllegalArgumentException("stages cannot be smaller than 0");
			}
			int count = 0;
			for (int i = 0; i < reducedStage - 1; i++) {
				for (int j = i + 1; j < reducedStage; j++) {
					++count;
				}
			}
			return count - (dupElementMinAppearance >= 3 ? dupElementMinAppearance : 0);
		}

		public long[] to64BitSignature(@NotNull int[] lshSignature) {
			return reduceSigSize ? toReducedSigSizeSignature(lshSignature) : toFullSignature(lshSignature);
		}

		/**
		 * Why does this reduction work..?!
		 * ..I just hate explaining Maths (_ _!)
		 */
		private long[] toReducedSigSizeSignature(@NotNull int[] lshSignature) {
			long[] ret = new long[stage64Bit];
			int index = 0;
			for (int i = 0; i < reducedStages - dupElementMinAppearance; i++) {
				for (int j = i + 1; j < reducedStages; j++) {
					ret[index] = combineHashes(lshSignature[i], lshSignature[j]);
					++index;
				}
			}

			if (index < stage64Bit) {
				ret[index] = combineHashes(lshSignature[reducedStages - dupElementMinAppearance], lshSignature[reducedStages - dupElementMinAppearance + 1]);
				++index;
			}

			if (index < stage64Bit) {
				ret[index] = combineHashes(lshSignature[reducedStages - 2], lshSignature[reducedStages - 1]);
			}

			return ret;
		}


		// Simply combine paris of lsh signature's hashes together
		private long[] toFullSignature(@NotNull int[] lshSignature) {
			long[] ret = new long[stage64Bit];
			int index = 0;
			for (int i = 0; i < reducedStages - 1; i++) {
				for (int j = i + 1; j < reducedStages; j++) {
					ret[index] = combineHashes(lshSignature[i], lshSignature[j]);
					++index;
				}
			}
			return ret;
		}

		private long combineHashes(int int1, int int2) {
			return (((long) int1) << 32) | (int2 & 0xffffffffL);
		}

	}

}