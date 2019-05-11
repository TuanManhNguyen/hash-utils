package chlx.hashing.lsh;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public class LSHBucketHashCounter {

	private final List<Int2IntOpenHashMap> counters;
	private final int stages;

	public LSHBucketHashCounter(LSHComputer lsh) {
		this.stages = lsh.lsh.getStages();
		this.counters = new ArrayList<>(stages);
		for (int i = 0; i < stages; i++) {
			counters.add(new Int2IntOpenHashMap());
		}
	}

	public boolean put(@Nullable int[] lshSignature) {
		if (lshSignature == null) {
			return false;
		}
		for (int stage = 0; stage < stages; stage++) {
			counters.get(stage).addTo(lshSignature[stage], 1);
		}
		return true;
	}

	public List<Long> extractLargeBucketHash(int bucketMinSize) {
		List<Long> ret = new ArrayList<>();
		for (int stage = 0; stage < stages; stage++) {
			final int finalStage = stage;
			counters.get(stage).forEach((bucketHash, count) -> {
				if (count >= bucketMinSize) {
					ret.add(LSHUtils.hashStageBucket(finalStage, bucketHash));
				}
			});
		}
		return ret;
	}

}