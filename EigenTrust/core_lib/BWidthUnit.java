/**
 * Georgia Tech
 * DISL
 * 2016
 */

package core_lib;

import java.util.*;

/**
 * The BWidthUnit class helps manage bandwidth at the user level. It consists
 * of a queue of type integer. The number of entries in that queue represents
 * the number of occupied connections, saturating at GLOBALS.BAND_MAX. The
 * queue entry value indicates at which cycle that connection can be freed.
 */
public class BWidthUnit{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * Parameters of the Network in which this BWidthUnit resides.
	 */
	private Globals GLOBALS;
	
	/**
	 * Cycle number at which each connection can be freed.
	 */
	private Queue<Integer> queue;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct a BWidthUnit object.
	 * @param GLOBALS The Network parameterization object
	 */
	public BWidthUnit(Globals GLOBALS){
		this.GLOBALS = GLOBALS;
		queue = new LinkedList<Integer>();
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Query to see if this BWidthUnit has available bandwidth. 
	 * @param cycle The cycle at which this query is being made
	 * @return TRUE if bandwidth is available; FALSE otherwise
	 */
	public boolean available(int cycle){
		if(queue.peek() != null){
			if(queue.peek() <= cycle)
				queue.remove();			
		} // If a connection is atop the queue, see if it is ready for removal
		//if (cycle<10)
		//System.out.print(cycle+"_queue.size= "+queue.size()+" ");
		if(queue.size() < GLOBALS.BAND_MAX)
		    return true;
		else 
			return false;
	}
	
	/**
	 * Consume a connection for the next GLOBALS.BAND+PER cycles
	 * @param cycle The cycle at which this connection begins.
	 */
	public void consume(int cycle){
		queue.add(cycle + GLOBALS.BAND_PER);
		return;
	}
}