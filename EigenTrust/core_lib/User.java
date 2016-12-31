/**
 * Georgia Tech
 * DISL
 * 2016
 */

package core_lib;

/**
 * The User class encapsulates a single user/peer/node in a Network. In
 * addition to describing behavioral properties, this class also manages
 * bandwidth, and tracks prior interactions with other Users.
 */
public class User{
	
	// ************************** PUBLIC FIELDS ******************************
	
	/**
	 * The Behavior enumeration details the initialization models to which a 
	 * User's behavior may conform. 
	 */
	public enum Behavior{USR_GOOD, USR_PURE, USR_FEED, USR_PROV, 
		USR_DISG, USR_SYBL, UNKNOWN}
	
	// ************************** PRIVATE FIELDS *****************************

	/**
	 * The Behavior model to which this User conforms.
	 */
	private Behavior model;
	
	/**
	 * Whether or not the user is pre-trusted
	 */
	private boolean pre_trusted;
	
	/**
	 * The probability this User will clean up an invalid file.
	 */
	private double pct_cleanup;
	
	/**
	 * The probability this User will provide honest feedback.
	 */
	private double pct_honest;
	
	/**
	 * The number of files owned by this User.
	 */
	private int num_files;
	
	/**
	 *  The number of upload files as provider;
	 */
	private int num_upload;
	/**
	 * Array storing Relations (reputations) of other User's in Network
	 */
	private Relation[] vector;
	
	/**
	 * Manager for upload bandwidth.
	 */
	private BWidthUnit ul_bwidth;
	
	/**
	 * Manager for download bandwidth.
	 */
	private BWidthUnit dl_bwidth;
	

	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Create a User, initializing some fields based on the Behavior argument.
	 * @param model Behavior to use for field generation
	 * @param pre_trusted Whether or not this user is pre-trusted 
	 * @param GLOBALS The Network parameterization object
	 */
	public User(Behavior model, boolean pre_trusted, Globals GLOBALS){
		this.model = model;
		this.pre_trusted = pre_trusted;
		this.num_files = 0;
		this.num_upload=0;
		
		if(model == Behavior.USR_GOOD){
			this.pct_cleanup = 1.0 ;//- (GLOBALS.RAND.nextDouble()/10);
			//this.pct_cleanup = 0.5 + (GLOBALS.RAND.nextDouble()/2);
			//this.pct_honest = 1.0 - (GLOBALS.RAND.nextDouble()/10);
			  this.pct_honest = 1.0 ;//- (GLOBALS.RAND.nextDouble()/5);
			//this.pct_honest = 1.0;
		} else if(model == Behavior.USR_PURE){
			//this.pct_cleanup = GLOBALS.RAND.nextDouble()/10;
			this.pct_cleanup = 0.0;
			this.pct_honest = 0.0;
		} else if(model == Behavior.USR_FEED){
			this.pct_cleanup = 1.0;  // - (GLOBALS.RAND.nextDouble()/10);
			this.pct_honest = 0.0;
		} else if(model == Behavior.USR_PROV){
			this.pct_cleanup = GLOBALS.RAND.nextDouble()/10;
			this.pct_honest = 1;
		} else if(model == Behavior.USR_DISG){
		    //this.pct_cleanup = 0.8 + (GLOBALS.RAND.nextDouble()/5);
			//this.pct_honest = 0.5 + (GLOBALS.RAND.nextDouble()/2);
			this.pct_cleanup = 1.0 ; // - (GLOBALS.RAND.nextDouble()/10);
			this.pct_honest = 0.0;
		} else if(model == Behavior.USR_SYBL){
			this.pct_cleanup = GLOBALS.RAND.nextDouble()/10;
			this.pct_honest = 0.0; // irrelevant
		} else{
			this.pct_cleanup = 0.0;
			this.pct_honest = 0.0;
		}
		
		this.vector = new Relation[GLOBALS.NUM_USERS];
		for(int i=0; i < GLOBALS.NUM_USERS; i++)
			vector[i] = new Relation();
		
		ul_bwidth = new BWidthUnit(GLOBALS);
		dl_bwidth = new BWidthUnit(GLOBALS);
	}
	
	/**
	 * Create a User by providing all relevant fields.
	 * @param model Behavior to which this User conforms
	 * @param cleanup Probability of cleaning up an invalid file
	 * @param honest Probability of providing an honest feedback
	 * @param pre_trusted Whether or not this user is pre-trusted
	 * @param GLOBALS The Network parameterization object
	 */
	public User(Behavior model, double cleanup, double honest, 
			boolean pre_trusted, Globals GLOBALS){
		this.model = model;
		this.pre_trusted = pre_trusted;
		this.num_files = 0;
		this.num_upload =0;
		this.pct_cleanup = cleanup;
		this.pct_honest = honest;
		
		this.vector = new Relation[GLOBALS.NUM_USERS];
		for(int i=0; i < GLOBALS.NUM_USERS; i++)
			vector[i] = new Relation();
		
		ul_bwidth = new BWidthUnit(GLOBALS);
		dl_bwidth = new BWidthUnit(GLOBALS);	
	}
	
	// ************************** PUBLIC METHODS *****************************
	
		// ----------------- BEHAVIOR RELATED (STATIC) -----------------------
	
	/**
	 * Retrieve the integer identifier assigned to a Behavior model.
	 * @param behavior The behavior model whose identifier is desired
	 * @return The unique integer identifier of that model
	 */
	public static int BehaviorToInt(Behavior behavior){
		// Though inelegant, this method allows us to treat this Java 
		// enumeration like one from C, permitting trace file compatibility.
		if(behavior == Behavior.USR_GOOD) return 0;
		else if(behavior == Behavior.USR_PURE) return 1;
		else if(behavior == Behavior.USR_FEED) return 2;
		else if(behavior == Behavior.USR_PROV) return 3;
		else if(behavior == Behavior.USR_DISG) return 4;
		else if(behavior == Behavior.USR_SYBL) return 5;
		else return 6; // (behavior == Behavior.UNKNOWN) 
	}
	
	/**
	 * Given an integer, return the Behavior associated with that identifier.
	 * @param behavior_int The integer identifier of the desired Behavior
	 * @return The desired Behavior type
	 */
	public static Behavior IntToBehavior(int behavior_int){
		if(behavior_int == 0) return Behavior.USR_GOOD;
		else if(behavior_int == 1) return Behavior.USR_PURE;
		else if(behavior_int == 2) return Behavior.USR_FEED;
		else if(behavior_int == 3) return Behavior.USR_PROV;
		else if(behavior_int == 4) return Behavior.USR_DISG;
		else if(behavior_int == 5) return Behavior.USR_SYBL;
		else return Behavior.UNKNOWN; // if(behavior_int == 6) 
	}
		
		// ---------------- FIELD ACCESS/MODIFY METHODS ----------------------

	/**
	 * Access method to a global Relation between this user and another.
	 * @param user_num Numerical identifier of other User
	 * @return Relation describing this User's relationship with 'user_num'
	 */
	public Relation getRelation(int user_num){
		return (this.vector[user_num]);
	}
	
	/**
	 * Access method to the Behavior model of this User.
	 * @return Behavior model of this User
	 */
	public Behavior getModel(){
		return (this.model);
	}
	
	/**
	 * Access method to the pre_trusted field of this User
	 * @return TRUE if this User is pre-trusted, FALSE otherwise.
	 */
	public boolean isPreTrusted(){
		return (this.pre_trusted);
	}
	
	/**
	 * Access method to the cleanup field of this User.
	 * @return Cleanup rate of this User
	 */
	public double getCleanup(){
		return (this.pct_cleanup);
	}
	
	/**
	 * Access method to the honesty field of this User.
	 * @return Honesty rate of this User
	 */
	public double getHonesty(){
		return (this.pct_honest);
	}
	
	/**
	 * Access method to the num_files field of this User.
	 * @return The number of files this User possesses 
	 */
	public double getNumFiles(){
		return (this.num_files);
	}
	
	/***
     *Access method to the num_uploads.
	 * @return The number of upload files as provider 
	 */
	public double getNumuploads(){
		return (this.num_upload);
	}
	public void resetUploads(){
		this.num_upload=0;
	}
	/**
	 * Increase the file possession counter by one.
	 */
	public void incFileCount(){
		this.num_files++;
	}
	
	/**
	 * Increase the number to upload files (excluding the status return -1)
	 */
	public void incuploadcount(){
		this. num_upload++;
	}
		// ------------------- USER BANDWIDTH METHODS ------------------------
	
	/**
	 * Check the availability of upload bandwidth.
	 * @param cycle The cycle when this availability query is being made
	 * @return TRUE if upload bandwidth is available. FALSE otherwise.
	 */
	public boolean BWidthAvailableUL(int cycle){
		return this.ul_bwidth.available(cycle);
	}
	
	/**
	 * Consume bandwidth from the upload BWidthUnit.
	 * @param cycle The cycle when this consumption is to begin
	 */
	public void BWidthConsumeUL(int cycle){
		this.ul_bwidth.consume(cycle);
	}
	
	/**
	 * Check the availability of download bandwidth.
	 * @param cycle The cycle when this availability query is being made
	 * @return TRUE if download bandwidth is available. FALSE otherwise
	 */
	public boolean BWidthAvailableDL(int cycle){
		return this.dl_bwidth.available(cycle);
	}
	
	/**
	 * Consume bandwidth from the download BWidthUnit.
	 * @param cycle The cycle when this consumption is to begin
	 */
	public void BWidthConsumeDL(int cycle){
		this.dl_bwidth.consume(cycle);
	}
	             	
}
	