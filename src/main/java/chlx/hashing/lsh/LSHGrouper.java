package chlx.hashing.lsh;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public class LSHGrouper extends AbstractLSHGrouper {

	private static final Logger LOG = LoggerFactory.getLogger(LSHGrouper.class);

	private final int stages;
	private final Map<Long, List<Long>> groupBuilder;

	/**
	 * @param lsh                   - the exact lsh instance used to compute lsh signatures
	 * @param bucketHashesToExtract - List of bucket hashes which we will compare* its elements
	 *                              to get duplicate groups because we cannot put the whole thing
	 *                              into memory
	 */
	public LSHGrouper(LSHComputer lsh, List<Long> bucketHashesToExtract) {
		this.stages = lsh.lsh.getStages();
		this.groupBuilder = new Long2ObjectOpenHashMap<>();
		bucketHashesToExtract.forEach(hash -> groupBuilder.put(hash, new ArrayList<>()));
	}

	/**
	 * put docId into presented buckets inorder to extract dupGroups later
	 * ... with hash validation
	 */
	public boolean put(long docId, int[] lshHash) {
		return !(lshHash == null || lshHash.length != stages) && putUnchecked(docId, lshHash);
	}

	@Override
	protected Pair<List<IdGroup>, Object2IntOpenHashMap<IdPair>> extractBigGroupsAndPairs() {
		LOG.debug("Filtering small bigGroups, grouping bigGroups and counting pairs");
		List<IdGroup> bigGroups = new ArrayList<>();
		Object2IntOpenHashMap<IdPair> pairCountMap = new Object2IntOpenHashMap<>();

		// Converting Map<bucketHash, List<docIds> groupBuilder
		// into a list of IdGroups, and a count map of IdPair
		groupBuilder.forEach((hash, docIdList) -> {
			if (docIdList.size() > 2) {
				bigGroups.add(new AbstractLSHGrouper.IdGroup(hash, docIdList));
			} else if (docIdList.size() == 2) {
				pairCountMap.addTo(new AbstractLSHGrouper.IdPair(docIdList), 1);
			}
		});
		return new Pair<>(bigGroups, pairCountMap);
	}

	/**
	 * put docId into presented buckets inorder to extract dupGroups later
	 */
	private boolean putUnchecked(long docId, @NotNull int[] hash) {
		boolean ret = false;
		for (int stage = 0; stage < stages; ++stage) {
			if (groupBuilder.computeIfPresent(LSHUtils.hashStageBucket(stage, hash[stage]), (bucket, docIdList) -> {
				docIdList.add(docId);
				return docIdList;
			}) != null) {
				ret = true;
			}
		}
		return ret;
	}

}