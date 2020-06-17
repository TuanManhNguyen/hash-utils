package chlx.hashing.log;

import chlx.hashing.minhash.ContinuableMinHash64;
import chlx.hashing.minhash.MinHashSimplified64;
import chlx.hashing.similarity.JaccardIndex;
import net.agkn.hll.HLL;
import net.jcip.annotations.ThreadSafe;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2019-01-16
 *
 * Using combination of MinHash and HyperLogLog
 * to estimate unique elements of union and intersection
 * between sets
 */
@ThreadSafe
public class HybridLogHash {

	private final HLL hll;
	private final ContinuableMinHash64 minHash64;

	private long[] sig;
	private long cardinality = 0L;
	private boolean addedAfterGet = true;

	public HybridLogHash(MinHashSimplified64 minHash, int log2m, int regWidth) {
		synchronized (this) {
			this.hll = new HLL(log2m, regWidth);
			this.minHash64 = new ContinuableMinHash64(minHash);
		}
	}

	private HybridLogHash(ContinuableMinHash64 mh64, HLL hll) {
		this.hll = hll;
		this.minHash64 = mh64;
	}

	public static HybridLogHash fromHash(MinHashSimplified64 minHash, long[] mhSig, byte[] hllBytes) {
		ContinuableMinHash64 minHash64 = new ContinuableMinHash64(minHash);
		minHash64.union(mhSig);
		return new HybridLogHash(minHash64, HLL.fromBytes(hllBytes));
	}

	private synchronized void tryUpdate() {
		if (addedAfterGet) {
			sig = minHash64.getSignature();
			cardinality = hll.cardinality();
			addedAfterGet = false;
		}
	}

	public long[] getSignature() {
		tryUpdate();
		return sig;
	}

	public byte[] getHllBytes() {
		return hll.toBytes();
	}

	public synchronized void add(long r) {
		hll.addRaw(r);
		minHash64.add(r);
		addedAfterGet = true;
	}

	public synchronized void add(long r, long[] precomputedMinHash) {
		hll.addRaw(r);
		minHash64.union(precomputedMinHash);
		addedAfterGet = true;
	}

	public void union(HybridLogHash that) {
		hll.union(that.hll);
		minHash64.union(that.minHash64);
	}

	public long countIntersect(HybridLogHash loggingHash) {
		double simIndex = JaccardIndex.indexMinHash(getSignature(), loggingHash.getSignature());
		return (long) (countUnion(loggingHash) * simIndex);
	}

	public long countUnion(HybridLogHash that) {
		try {
			HLL hll = this.hll.clone();
			hll.union(that.hll);
			return hll.cardinality();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public long cardinality() {
		tryUpdate();
		return cardinality;
	}

}
