/**
 * Georgia Tech
 * DISL
 * 2016
 */

package core_lib;

import java.util.Random;

/**
 * The Globals class provides a wrapper for common Network parameters. All
 * fields are given public access. This class was created to minimize 
 * constructor size for objects throughout the simulator framework.
 */
public class Globals{
	
	// ************************** PUBLIC FIELDS ******************************
		
	/**
	 * Number of users/peers/nodes in a Network.
	 */
	public final int NUM_USERS;
	
	/**
	 * Number of files (not copies) in a Network.
	 */
	public final int NUM_FILES;
	
	/**
	 * Number of transactions to be simulated.
	 */
	public final int NUM_TRANS;
	
	/**
	 * Zipf constant controlling file popularity and demand
	 */
	public final double ZIPF;
	
	/**
	 * Number of pre-trusted users, a subset of 'good' users
	 */
	public final int PRE_TRUSTED;
	
	/**
	 * Number of 'well-behaved'/'good' users
	 */
	public final int USR_GOOD;
	
	/**
	 * Number of 'purely malicious' users
	 */
	public final int USR_PURE;
	
	/**
	 * Number of 'feedback malicious' users
	 */
	public final int USR_FEED;
	
	/**
	 * Number of 'malicious provider' users
	 */
	public final int USR_PROV;
	
	/**
	 * Number of 'disguised malicious' users
	 */
	public final int USR_DISG;
	
	/**
	 * Number of 'Sybil attack' users 
	 */
	public final int USR_SYBL;
	
	/**
	 * Number of maximum simultaneous upload/download connections per user.
	 */
	public final int BAND_MAX;
	
	/**
	 * Number of time units (cycles) each upload/download requires
	 */
	public final int BAND_PER;
	
	/**
	 * Number of warm-up transactions before statistical tabulation begins
	 */
	public final int WARMUP;
	
	/**
	 * Whether or not intelligent transaction generation should be used.
	 */
	public final boolean SMART_GEN;
	
	/**
	 * Seed that initialized the 'this.RAND' Random object herein
	 */
	public final long RAND_SEED;
	
	/**
	 * A Random object seeded by this.RAND_SEED. 
	 */
	public final Random RAND;
	
	public final Double F_PERCENTAGE;
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct a Globals object by providing all field values.
	 * @param NUM_USERS Number of users in Network
	 * @param NUM_FILES Number of files in Network
	 * @param NUM_TRANS Number of transactions to simulate
	 * @param ZIPF Zipf constant controlling file popularity
	 * @param PRE_TRUSTED Number of pre-trusted users, a subset of 'good' users
	 * @param USR_GOOD Number of 'good' users
	 * @param USR_PURE Number of 'purely malicious' users
	 * @param USR_FEED Number of 'feedback malicious' users
	 * @param USR_PROV Number of 'malicious provider' users
	 * @param USR_DISG Number of 'disguised malicious' users
	 * @param USR_SYBL Number of 'Sybil attack' users
	 * @param BAND_MAX Maximum number of connections per user
	 * @param BAND_PER Number of time units (cycles) a transaction requires
	 * @param WARMUP Number of warm-up instructions to simulate
	 * @param SMART_GEN Use intelligent transaction generation?
	 */
	public Globals(int NUM_USERS, int NUM_FILES, int NUM_TRANS, double ZIPF, 
			int PRE_TRUSTED, int USR_GOOD, int USR_PURE, int USR_FEED, 
			int USR_PROV, int USR_DISG, int USR_SYBL, int BAND_MAX, 
			int BAND_PER, int WARMUP, boolean SMART_GEN,double F_PERCENTAGE){
		
		this.NUM_USERS = NUM_USERS;
		this.NUM_TRANS = NUM_TRANS;
		this.NUM_FILES = NUM_FILES;
		this.ZIPF = ZIPF;
		this.F_PERCENTAGE = F_PERCENTAGE;
		this.PRE_TRUSTED = PRE_TRUSTED;
		this.USR_GOOD = USR_GOOD;
		this.USR_PURE = USR_PURE;
		this.USR_FEED = USR_FEED;
		this.USR_PROV = USR_PROV;
		this.USR_DISG = USR_DISG;
		this.USR_SYBL = USR_SYBL;
		
		this.BAND_MAX = BAND_MAX;
		this.BAND_PER = BAND_PER;
		
		this.WARMUP = WARMUP;
		this.SMART_GEN = SMART_GEN;
		
		this.RAND_SEED = System.currentTimeMillis();
		this.RAND = new Random(RAND_SEED);
	}
	
}
