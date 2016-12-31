/**
 * Georgia Tech
 * DISL
 * 2016
 */

package core_lib;

/**
 * The Statistics class is simply a wrapper for several variables that
 * maintain statistics during a trace simulation. All fields have public 
 * access. This class minimizes lengthy and confusing parameter passing.
 */
public class Statistics {
	
	// ************************** PUBLIC FIELDS ******************************
	
	/**
	 * Transactions (completed) resulting in exchange of invalid file.
	 */
	public int NUM_INVAL_TRANS = 0;
	/**
	 * Transactions (completed) resulting in exchange of valid file.
	 */
	
	public int NUM_VAL_TRANS = 0;
	/**
	 * Incomplete transactions due to complications on receiving end.
	 */
	public int NUM_RECV_BLK_TR = 0;
	
	/**
	 * Incomplete transactions due to complications on source end.
	 */
	public int NUM_SEND_BLK_TR = 0;
	
	/**
	 * Honest feedbacks committed.
	 */
	public int NUM_FBACK_TRUE = 0;
	
	/**
	 * Dishonest feedbacks committed.
	 */
	public int NUM_FBACK_LIES = 0;
	
	/**
	 * Sybil feedbacks committed.
	 */
	public int NUM_FBACK_SYBL = 0;
	
	/**
	 * Transactions w/valid file exchange and 'good' receiver
	 */
	public int NUM_GOOD_SUCC = 0;
	
	/**
	 * Transactions w/invalid file exchange and 'good' receiver
	 */
	public int NUM_GOOD_FAIL = 0;
	
	/**
	 *  Number of authentic files provided by malicious peers
	 */
	public int NUM_validfile_maliciousprovide=0; 
	/**
	 *  Number of authentic files by malicious with good receriver
	 */
	public int Good_recv_NUM_validfile_maliciousprovide=0;
	
	public double minDirectDegree;
	public double maxDirectDegree;
	public double avgDirectDegree;
	public double countZeroTrustDegree;
		
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Reset all statistical fields (integer counters) back to zero.
	 */
	
	public void reset(){
		NUM_INVAL_TRANS = 0;
		NUM_VAL_TRANS = 0;
		NUM_RECV_BLK_TR = 0;
		NUM_SEND_BLK_TR = 0;
		NUM_FBACK_TRUE = 0;
		NUM_FBACK_LIES = 0;
		NUM_FBACK_SYBL = 0;
		NUM_GOOD_SUCC = 0;
		NUM_GOOD_FAIL = 0;
		NUM_validfile_maliciousprovide=0;
		Good_recv_NUM_validfile_maliciousprovide=0;
		
		return;
	}

}
