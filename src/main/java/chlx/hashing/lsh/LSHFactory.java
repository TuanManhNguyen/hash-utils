package chlx.hashing.lsh;

import chlx.hashing.shingles.Shingling;
import chlx.hashing.shingles.SimpleWordBasedShingling;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public class LSHFactory {

	public static final int DEDUPLICATION_BUCKET_MIN_SIZE = 2;
	public static final long DEDUPLICATION_LSH_SEED = -8814109245394854757L;

	// Index deduplication setting constants
	private static final int DEDUPLICATION_LSH_STAGE = 10;
	// This also means we are going to use MinHashSimplified for our Min Hashing algorithm
	private static final int DEDUPLICATION_LSH_DICT_SIZE = Integer.MAX_VALUE;
	private static final int DEDUPLICATION_SHINGLING_K_VAL = 5;
	private static final double DEDUPLICATION_LSH_THRESHOLD = 0.95;

	private static final double NEWS_TOPIC_MODEL_LSH_THRESHOLD = 0.8;

	public static LSHComputer createLSHComputerForIndexDeduplication() {
		Shingling shingling = new SimpleWordBasedShingling(DEDUPLICATION_SHINGLING_K_VAL);
		return new LSHComputer(shingling, createLSHForIndexDeduplication());
	}

	private static LSH createLSHForIndexDeduplication() {
		return new LSHMinHash(
				DEDUPLICATION_LSH_STAGE,
				DEDUPLICATION_LSH_DICT_SIZE,
				DEDUPLICATION_LSH_SEED,
				DEDUPLICATION_LSH_THRESHOLD);
	}

	public static LSHComputer createLSHComputerForNewsTopicModel() {
		Shingling shingling = new SimpleWordBasedShingling(DEDUPLICATION_SHINGLING_K_VAL);
		return new LSHComputer(shingling, createLSHForNewsTopicModel());
	}

	private static LSH createLSHForNewsTopicModel() {
		return new LSHMinHash(
				DEDUPLICATION_LSH_STAGE,
				DEDUPLICATION_LSH_DICT_SIZE,
				DEDUPLICATION_LSH_SEED,
				NEWS_TOPIC_MODEL_LSH_THRESHOLD);
	}

}
