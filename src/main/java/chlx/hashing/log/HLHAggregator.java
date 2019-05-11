package chlx.hashing.log;

import chlx.hashing.lsh.LSHFactory;
import chlx.hashing.minhash.MinHashSimplified64;
import net.jcip.annotations.ThreadSafe;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2019-01-18
 */
@ThreadSafe
public class HLHAggregator<E> {

	public static final int DEFAULT_LOG2M = 12;
	public static final int DEFAULT_REG_WIDTH = 4;
	public static final int DEFAULT_SIGNATURE_SIZE = 2 * 1024;
	public static final long DEFAULT_SEED = LSHFactory.DEDUPLICATION_LSH_SEED;

	public static final MinHashSimplified64 DEFAULT_MIN_HASH = new MinHashSimplified64(DEFAULT_SIGNATURE_SIZE, DEFAULT_SEED);

	private final Map<E, HybridLogHash> hashMap;
	private final MinHashSimplified64 minHash;
	private final int log2m;
	private final int regWidth;

	public HLHAggregator() {
		this(DEFAULT_MIN_HASH, DEFAULT_LOG2M, DEFAULT_REG_WIDTH);
	}

	public HLHAggregator(MinHashSimplified64 minHash, int log2m, int regWidth) {
		this.minHash = minHash;
		this.hashMap = new ConcurrentHashMap<>();
		this.log2m = log2m;
		this.regWidth = regWidth;
	}

	public MinHashSimplified64 getMinHash() {
		return minHash;
	}

	public void add(long h, Collection<E> keys) {
		long[] mhSig = minHash.signature(h);
		for (E key : keys) {
			hashMap.computeIfAbsent(key, k -> new HybridLogHash(minHash, log2m, regWidth)).add(h, mhSig);
		}
	}

	@SafeVarargs
	public final void add(long h, E... keys) {
		long[] mhSig = minHash.signature(h);
		for (E key : keys) {
			hashMap.computeIfAbsent(key, k -> new HybridLogHash(minHash, log2m, regWidth)).add(h, mhSig);
		}
	}

	public Map<E, HybridLogHash> getHashMap() {
		return Collections.unmodifiableMap(hashMap);
	}

}
