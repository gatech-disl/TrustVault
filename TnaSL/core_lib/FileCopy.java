/**
 * Georgia Tech
 * DISL
 * 2016
 */

package core_lib;

/**
 * The FileCopy class stores a unique instance of a file. A single 'file' does
 * not exist on a network - instead there are multiple FileCopy objects for
 * each 'file' - each with a distinct owner and validity.
 */
public class FileCopy{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * The owner of this FileCopy.
	 */
	private int owner;
	
	/**
	 * The validity of this FileCopy.
	 */
	private boolean valid;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct a FileCopy object.
	 * @param owner The user (number) who owns this FileCopy
	 * @param valid The validity of this FileCopy
	 */
	public FileCopy(int owner, boolean valid){
		this.owner = owner;
		this.valid = valid;
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Access method to the 'owner' field.
	 * @return The user who owns this FileCopy
	 */
	public int getOwner(){
		return this.owner;
	}
	
	/**
	 * Access method to the 'valid' field.
	 * @return The validity of the this FileCopy
	 */
	public boolean getValid(){
		return this.valid;
	}

}
