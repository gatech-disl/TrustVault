/**
 * Georgia Tech
 * DISL
 * 2016
 */

package core_lib;

/**
 * The Transaction class stores the parameters that characterize a transaction
 * between two users. Only access methods are provided.
 */
public class Transaction{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * Cycle when this Transaction will complete and data can be committed.
	 */
	private int commit_cycle;
	
	/**
	 * Numerical identifier for the file provider.
	 */
	private int sender;
	
	/**
	 * Numerical identifier for the file receiver.
	 */
	private int receiver;
	
	/**
	 * Numerical identifier for the file being exchanged.
	 */
	private int file_num;
	
	/**
	 * Validity of the file being exchanged.
	 */
	private boolean valid;
	
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct a Transaction object by providing all fields.
	 * @param commit_cycle Cycle when this transaction can commit
	 * @param sender File provider
	 * @param receiver File receiver
	 * @param file_num File being exchanged
	 * @param valid Validity of file being exchanged
	 */
	public Transaction(int commit_cycle, int sender, int receiver, 
			int file_num, boolean valid){
		this.commit_cycle = commit_cycle;
		this.sender = sender;
		this.receiver = receiver;
		this.file_num = file_num;
		this.valid = valid;
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Get the commit cycle of this Transaction.
	 * @return The commit cycle of this Transaction
	 */
	public int getCommit(){
		return (this.commit_cycle);
	}
	
	/**
	 * Get the identifier of the file provider
	 * @return The identifier of the file provider
	 */
	public int getSend(){
		return (this.sender);
	}
	
	/**
	 * Get the identifier of the file receiver
	 * @return The identifier of the file receiver
	 */
	public int getRecv(){
		return (this.receiver);
	}
	
	/**
	 * Get the identifier of the file being exchanged
	 * @return The identifier of the file being exchanged
	 */
	public int getFile(){
		return (this.file_num);
	}
	
	/**
	 * Get the validity of the file being exchanged
	 * @return The validity of the file being exchanged
	 */
	public boolean getValid(){
		return (this.valid);
	}
	
}
