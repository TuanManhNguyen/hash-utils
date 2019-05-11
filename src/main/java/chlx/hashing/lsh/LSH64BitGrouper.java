package chlx.hashing.lsh;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public class LSH64BitGrouper extends AbstractLSHGrouper {

	public static final int DUP_ELEMENT_MIN_APPEARANCE_64 = 2;

	private static final int MINIMUM_APPEARANCE_FOR_NOISE_REDUCTION = 2;
	private static final Logger LOG = LoggerFactory.getLogger(LSH64BitGrouper.class);
	private static final LongArrayList NUL_LIST = new LongArrayList(0);

	private final int stage32bit;
	private final int stage64Bit;
	private final boolean enableNoiseReduction;
	private final LSHComputer.Signature64Converter signatureConverter;
	private final List<Long2ObjectOpenHashMap<LongArrayList>> groupBuilder;

	private long writtenGroupNum = 0;

	/**
	 * @param bucketHashesToExtract - List of bucket hashes which we will compare* its elements
	 *                              to get duplicate groups because we cannot put the whole thing
	 *                              into memory
	 */
	public LSH64BitGrouper(LSHComputer.Signature64Converter signatureConverter, Collection<Long>[] bucketHashesToExtract, boolean enableNoiseReduction) {
		this.signatureConverter = signatureConverter;
		this.stage32bit = signatureConverter.getReducedStages();
		this.stage64Bit = signatureConverter.getStage64Bit();
		this.enableNoiseReduction = enableNoiseReduction;
		this.groupBuilder = new ArrayList<>(stage64Bit);
		for (int stage = 0; stage < stage64Bit; stage++) {
			Long2ObjectOpenHashMap<LongArrayList> current = new Long2ObjectOpenHashMap<>();
			groupBuilder.add(current);
			for (Long hash : bucketHashesToExtract[stage]) {
				current.put(hash, NUL_LIST);
			}
		}

		this.dupElementMinAppearance = DUP_ELEMENT_MIN_APPEARANCE_64;
	}

	/**
	 * put docId into presented buckets inorder to extract dupGroups later
	 * ... with hash validation
	 */
	public boolean put(long docId, @Nullable int[] lshSignature) {
		return !(lshSignature == null || lshSignature.length < stage32bit) &&
				(enableNoiseReduction ? putWithNoiseReduction(docId, signatureConverter.to64BitSignature(lshSignature)) : putUnchecked(docId, signatureConverter.to64BitSignature(lshSignature)));
	}

	@Override
	protected Pair<List<IdGroup>, Object2IntOpenHashMap<IdPair>> extractBigGroupsAndPairs() {
		LOG.debug("Start extracting with {} groups from groupBuilder and {} written groups", groupBuilder.size(), writtenGroupNum);
		LOG.debug("Filtering small bigGroups, grouping bigGroups and counting pairs");
		List<IdGroup> bigGroups = new ArrayList<>();
		Object2IntOpenHashMap<IdPair> pairCountMap = new Object2IntOpenHashMap<>();

		// Converting Map<bucketHash, List<docIds> groupBuilder
		// into a list of IdGroups, and a count map of IdPair
		for (int stage = 0; stage < stage64Bit; stage++) {
			groupBuilder.get(stage).forEach((hash, docIdList) -> {
				if (docIdList.size() > 2) {
					bigGroups.add(new AbstractLSHGrouper.IdGroup(hash, docIdList));
				} else if (docIdList.size() == 2) {
					pairCountMap.addTo(new AbstractLSHGrouper.IdPair(docIdList), 1);
				}
			});
		}
		return new Pair<>(bigGroups, pairCountMap);
	}

	/**
	 * put docId into presented buckets inorder to extract dupGroups later
	 * ... with hash validation
	 */
	public boolean put(long docId, @Nullable long[] lsh64BitSignature) {
		return !(lsh64BitSignature == null || lsh64BitSignature.length != stage64Bit) &&
				(enableNoiseReduction ? putWithNoiseReduction(docId, lsh64BitSignature) : putUnchecked(docId, lsh64BitSignature));
	}

	/**
	 * try filtering doc with number of hash in
	 * groupBuilder < MINIMUM_APPEARANCE_FOR_NOISE_REDUCTION (3)
	 * before putting docId into presented buckets inorder to extract dupGroups later
	 */
	private boolean putWithNoiseReduction(long docId, @NotNull long[] lshSignature) {
		int count = 0;
		for (int stage = 0; stage < stage64Bit; ++stage) {
			if (groupBuilder.get(stage).containsKey(lshSignature[stage])) {
				++count;
			}
			if (count >= MINIMUM_APPEARANCE_FOR_NOISE_REDUCTION) {
				return putUnchecked(docId, lshSignature);
			}
		}
		return false;
	}


	/**
	 * put docId into presented buckets inorder to extract dupGroups later
	 */
	private boolean putUnchecked(long docId, @NotNull long[] lsh64BitSignature) {
		boolean ret = false;
		for (int stage = 0; stage < stage64Bit; ++stage) {
			LongArrayList docIdList = groupBuilder.get(stage).get(lsh64BitSignature[stage]);
			if (docIdList != null) {
				if (docIdList == NUL_LIST) {
					++writtenGroupNum;
					docIdList = new LongArrayList(2);
					groupBuilder.get(stage).put(lsh64BitSignature[stage], docIdList);
				}
				docIdList.add(docId);
				ret = true;
			}
		}
		return ret;
	}

}