package chlx.hashing.lsh;

/**
 * Author: (づ｡◕‿‿◕｡)づ*
 * Creation date: 2017-06
 */
public class LSHUtils {

	public static long hashStageBucket(int stage, int bucket) {
		return (((long) stage) << 32) | (bucket & 0xffffffffL);
	}

	public static int hashToStage(long hash) {
		return (int) (hash >> 32);
	}

	public static int hashToBucket(long hash) {
		return (int) hash;
	}

}
