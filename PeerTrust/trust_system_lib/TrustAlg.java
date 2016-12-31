/**
 * Georgia Tech
 * DISL
 * 2016
 */


package trust_system_lib;

import core_lib.*;

/**
 * The TrustAlg interface specifies those methods which any class 
 * implementing a trust management system must include.
 */
public interface TrustAlg{
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * File extension placed on output files using this algorithm.
	 * @return File extension placed on output files using this algorithm
	 */
	public String fileExtension();
	
	/**
	 * Text name of this trust algorithm (spaces are okay)
	 * @return Text name of this trust algorithm (spaces are okay)
	 */
	public String algName();
	
	/**
	 * Given coordinates of a feedback commitment, update as needed.
	 * @param i Transaction detailing feedback commitment
	 */
	public void update(Transaction trans);
	
	/**
	 * Given coordinates of feedback commitment, update as needed.
	 * @param i
	 */
   public void adaptiveupdate(Transaction trans);
	/**
	 * Compute trust, exporting trust values to Network
	 * @param user Identifier of user performing trust computation
	 * @param cycle The current cycle
	 */
	public void computeTrust(int user, int cycle);
	
	public double[] showtrust();
	
	public void computeadptiveTrust(int user, int cycle);
	
	public void adaptivetimewindow();
	
	public void clearnormalizedrateList();

	public void getDirectTrusted();

}
