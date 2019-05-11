package chlx.hashing.lsh;

import chlx.hashing.minhash.MinHash;
import chlx.hashing.minhash.MinHashSimplified;
import chlx.hashing.similarity.JaccardIndex;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public abstract class AbstractLSHGrouper {

	private static final int GROUP_MIN_HASH_SIZE = 20;
	private static final int DUP_ELEMENT_MIN_APPEARANCE_32 = 4;
	private static final double DUP_GROUP_MIN_SIM_THRESHOLD = 0.5;
	private static final Logger LOG = LoggerFactory.getLogger(AbstractLSHGrouper.class);

	protected int dupElementMinAppearance = DUP_ELEMENT_MIN_APPEARANCE_32;

	/**
	 * put docId into presented buckets inorder to extract dupGroups later
	 * ... with hash validation
	 */
	public abstract boolean put(long docId, int[] lshHash);

	/**
	 * Converting Map<bucketHash, List<docIds> groupBuilder
	 * into a list of IdGroups, and a count map of IdPair
	 */
	protected abstract Pair<List<IdGroup>, Object2IntOpenHashMap<IdPair>> extractBigGroupsAndPairs();

	/**
	 * 1. Grouping: use grouping() to group bigGroups, and get countPairMap
	 * <p>
	 * 2. Combining:
	 * 2.1 from those bigGroups find all ids which appears more than dupElementMinAppearance times
	 * 2.2 do the same thing with pairCountMap
	 *
	 * @return lists of 'probably' duplicated ids stay in the same array
	 * @implNote We probably need to optimize the sorting part in 'finding candidates' if we want to
	 * extract duplicate groups from the whole index, because it would take
	 * GROUP_MIN_HASH_SIZE * n(log n) (while n is number of IdGroups)
	 */
	public List<long[]> extractDuplicateGroups() {
		LOG.debug("Filtering small bigGroups, grouping bigGroups and counting pairs");
		Pair<List<IdGroup>, Object2IntOpenHashMap<IdPair>> bigGroupsAndPairs = extractBigGroupsAndPairs();
		List<IdGroup> bigGroups = bigGroupsAndPairs.getValue0();
		Object2IntOpenHashMap<IdPair> pairCountMap = bigGroupsAndPairs.getValue1();

		// 1. Sort by each column of MinHash signature - from 0 to GROUP_MIN_HASH_SIZE
		// 2. Add IdGroups which has the same MinHash signature to other candidates
		// TODO: We might need to fasten this since there are too many groups if we do this with the entire index
		LOG.debug("Finished filtering with {} bigGroups, and {} unique pairs", bigGroups.size(), pairCountMap.size());
		LOG.debug("Finding candidates of {} bigGroups", bigGroups.size());
		for (int hashIndex = 0; hashIndex < GROUP_MIN_HASH_SIZE; hashIndex++) {
			LOG.debug("Finding candidates with hashIndex: {}/" + (GROUP_MIN_HASH_SIZE - 1), hashIndex);
			final int finalHashIndex = hashIndex;
			bigGroups.sort(Comparator.comparingInt(o -> o.minHash[finalHashIndex]));
			int lastSimilarIndex = 0;
			int lastHashValue = Integer.MIN_VALUE;
			for (int groupIndex = 0; groupIndex < bigGroups.size(); groupIndex++) {
				if (lastHashValue != bigGroups.get(groupIndex).minHash[hashIndex]) {
					for (int i = lastSimilarIndex; i < groupIndex; i++) {
						IdGroup iGroup = bigGroups.get(i);
						for (int j = lastSimilarIndex; j < groupIndex; j++) {
							if (i != j) {
								iGroup.putCandidate(bigGroups.get(j));
							}
						}
					}
					lastSimilarIndex = groupIndex;
				}
				lastHashValue = bigGroups.get(groupIndex).minHash[hashIndex];
			}
		}

		List<long[]> ret = new ArrayList<>();
		int compareWithPairsLength = (int) (2 / DUP_GROUP_MIN_SIM_THRESHOLD);
		double dupGroupMinSimToRemove = DUP_GROUP_MIN_SIM_THRESHOLD / 4;

		LOG.debug("Putting similar bigGroups together");
		HashSet<IdGroup> pollSet = new HashSet<>();
		pollSet.addAll(bigGroups);
		HashSet<IdGroup> removedGroups = new HashSet<>();
		HashSet<IdPair> removedPairs = new HashSet<>();

		for (IdGroup current : pollSet) {
			if (removedGroups.contains(current)) {
				continue;
			}
			removedGroups.add(current);

			if (current.candidates == null) {
				continue;
			}

			// Take all candidates and its candidates as well into nextGroups
			Queue<IdGroup> candidateQueue = new ArrayDeque<>();
			HashSet<IdGroup> nextGroups = new HashSet<>();
			Object2IntOpenHashMap<IdPair> nextPairs = null;
			candidateQueue.add(current);
			nextGroups.add(current);

			while (!candidateQueue.isEmpty()) {
				IdGroup next = candidateQueue.poll();

				Set<IdGroup> nextCandidates = next.getCandidates();
				if (nextCandidates != null) {
					for (IdGroup cd : nextCandidates) {
						if (!nextGroups.contains(cd)) {
							nextGroups.add(cd);
							candidateQueue.add(cd);
						}
					}
				}

				// With all groups which is not over 1/DUP_GROUP_MIN_SIM_THRESHOLD times larger than pair(2)
				// we also look for candidates in pairCountMap and all of them into nextPairs
				// but not the candidateQueue
				if (next.docIds.size() <= compareWithPairsLength) {
					if (nextPairs == null) {
						nextPairs = new Object2IntOpenHashMap<>();
					}
					for (int i1 = 0; i1 < next.docIds.size() - 1; i1++) {
						for (int i2 = i1 + 1; i2 < next.docIds.size(); i2++) {
							IdPair pair = new IdPair(Arrays.asList(next.docIds.get(i1), next.docIds.get(i2)));
							if (!removedPairs.contains(pair)) {
								Integer count = pairCountMap.get(pair);
								if (count != null) {
									nextPairs.put(pair, count);
								}
							}
						}
					}
				}
			}

			// 1. If nextGroups has more than 1 candidate,
			//      we will count number of appearance of docIds in it
			// 2. If docIds' count > dupElementMinAppearance
			//      then put into the matched id list as duplicated
			// 3. If IdGroups or IdPairs has more than (DUP_GROUP_MIN_SIM_THRESHOLD / 4) intersect with finalList
			//      then remove it in the corresponding set/map
			int pairSize = 0;
			if (nextPairs != null) {
				for (int count : nextPairs.values()) {
					pairSize += count;
				}
			}
			if (nextGroups.size() + pairSize >= dupElementMinAppearance) {
				Long2IntOpenHashMap map = new Long2IntOpenHashMap();
				nextGroups.forEach(idGroup -> idGroup.docIds.forEach(l -> map.addTo(l, 1)));
				if (nextPairs != null) {
					nextPairs.forEach((idPair, count) -> idPair.value.forEach(id -> map.addTo(id, count)));
				}
				List<Long> matchIds = new ArrayList<>();
				map.forEach((docId, count) -> {
					if (count > dupElementMinAppearance) {
						matchIds.add(docId);
					}
				});
				// If number of matched ids > 1 then add the sorted list into the returned list
				if (matchIds.size() > 1) {
					Collections.sort(matchIds);
					long[] toAdd = matchIds.stream().mapToLong(l -> l).toArray();
					ret.add(toAdd);
					for (IdGroup idGroup : nextGroups) {
						if (JaccardIndex.index(idGroup.docIds, matchIds) >= dupGroupMinSimToRemove) {
							removedGroups.add(idGroup);
						}
					}
					if (nextPairs != null) {
						nextPairs.forEach((idPair, count) -> {
							if (JaccardIndex.index(idPair.value, matchIds) >= dupGroupMinSimToRemove) {
								removedPairs.add(idPair);
							}
						});
					}
				}
			}
		}

		LOG.debug("Extracted {} dupGroups from big ones", ret.size());

		// For the remaining pairs after the process above,
		// If number of appearance of pairs >= dupElementMinAppearance
		// then add the pair into returning list
		pairCountMap.forEach((pair, count) -> {
			if (count >= dupElementMinAppearance && !removedPairs.contains(pair)) {
				ret.add(pair.value.stream().mapToLong(l -> l).toArray());
			}
		});

		LOG.debug("Extracted {} dupGroups from pairs", ret.size());

		return ret;
	}

	protected static class IdGroup {

		static final int MIN_HASH_SIZE = GROUP_MIN_HASH_SIZE;
		static final MinHash ID_MIN_HASH = new MinHashSimplified(MIN_HASH_SIZE, LSHFactory.DEDUPLICATION_LSH_SEED);

		// Bucket hash
		final long bucketHash;
		// Min hash signature of all doc IDs
		final int[] minHash;
		final List<Long> docIds;
		// Other IdGroups which we consider to be a like
		Set<IdGroup> candidates = null;

		/**
		 * @implNote Since we have no bucket that have more docs than Integer.MAX_VALUE,
		 * -> we have no docIds can exceed 4 bytes integer
		 * -> we can directly use int id for hash function
		 */
		IdGroup(long bucketHash, List<Long> docIds) {
			this.docIds = docIds;
			int[] intDocIds = new int[docIds.size()];
			for (int i = 0; i < docIds.size(); i++) {
				intDocIds[i] = docIds.get(i).intValue();
			}
			minHash = ID_MIN_HASH.signature(intDocIds);
			this.bucketHash = bucketHash;
		}

		void putCandidate(IdGroup idGroup) {
			if ((candidates == null || !candidates.contains(idGroup)) && JaccardIndex.index(idGroup.docIds, this.docIds) >= DUP_GROUP_MIN_SIM_THRESHOLD) {
				if (candidates == null) {
					candidates = new HashSet<>();
				}
				candidates.add(idGroup);
			}
		}

		/**
		 * @return Extract all candidate
		 */
		@Nullable
		Set<IdGroup> getCandidates() {
			return candidates;
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public int hashCode() {
			return Long.hashCode(bucketHash);
		}

	}

	protected class IdPair {

		final List<Long> value;

		IdPair(List<Long> value) {
			if (value.size() != 2) {
				throw new IllegalArgumentException("length can't be other than 2");
			}
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			IdPair that = (IdPair) obj;
			return this.value.equals(that.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

	}

}