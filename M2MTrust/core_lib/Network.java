/**
 * Georgia Tech
 * DISL
 * 2016
 */

package core_lib;

import java.util.*;

import trust_system_lib.TrustAlg;

/**
 * The Network class is a high-level object managing data and behaviors 
 * specific to users, files, and bandwidth 
 */
public class Network{
	
	// ************************** PUBLIC FIELDS ******************************

	/**
	 * Parameterization of this Network.
	 */
	public Globals GLOBALS;
	
	/**
	 * Statistics pertaining to happenings on this Network.
	 */
	public Statistics STATS = new Statistics();
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * Array containing Users in the Network; a User library.
	 */
	private User[] users;
	
	
	// Trial
	
	public TrustAlg ALG;
	/**
	 * Array acting as a file library for the Network.
	 */
	private LinkedList<FileCopy>[] files;
	
	/**
	 * Queue storing transactions in progress, yet to commit data.
	 */
	private Queue<Transaction> delay_queue;
	
	//private int [] maliciarry;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct a Network object.
	 * @param GLOBALS The Network parameterization object
	 */
	@SuppressWarnings("unchecked")
	public Network(Globals GLOBALS){
		this.GLOBALS = GLOBALS;
		users = new User[GLOBALS.NUM_USERS];		
		files = (LinkedList<FileCopy>[])new LinkedList[GLOBALS.NUM_FILES];
		for(int i=0; i < GLOBALS.NUM_FILES; i++)
			files[i] = new LinkedList<FileCopy>();
		this.delay_queue = new LinkedList<Transaction>();
		//maliciarry =new int[GLOBALS.NUM_USERS-GLOBALS.USR_GOOD];
	}
	
	// ************************** PUBLIC METHODS *****************************
	
		// ----------------------- USER METHODS ------------------------------
	
	/**
	 * Access method to a User in the User library.
	 * @param user_num Numerical identifier of the User to return
	 * @return User with identifier 'user_num'
	 */
	public User getUser(int user_num){
		return (this.users[user_num]);
	}
	public void setAlg(TrustAlg alg){
		System.out.println("TrustAlg for nw is set");
		this.ALG=alg;
	}
	/**
	 * Modifier to place a User into the User library.
	 * @param user_num Numerical identifier of parameter 'user'
	 * @param user The User object to be placed into the User library
	 */
	public void setUser(int user_num, User user){
		this.users[user_num] = user;
	}	
	
	/**
	 * Access method to a Relation object between two library Users.
	 * @param source Numerical identifier of a User
	 * @param dest Numerical identifier of a second User
	 * @return Relation between 'source' and 'dest'; from former's perspective
	 */
	public Relation getUserRelation(int source, int dest){
		return (this.users[source].getRelation(dest));
	}
	
		// ----------------------- FILE METHODS ------------------------------
	
	/**
	 * Fetch an iterator over all copies of a particular file.
	 * @param file_num Numerical file identifier
	 * @return Iterator over all FileCopies of file 'file'
	 */
	public Iterator<FileCopy> getFileIterator(int file_num){
		return (this.files[file_num].iterator());
	}	

	/**
	 * The number of owners (e.g. FileCopies) of a particular file.
	 * @param file_num Numerical file identifier
	 * @return Number of FileCopies that exist for file 'file'
	 */
	public int fileOwners(int file_num){
		return (this.files[file_num].size());
	}
	
	/**
	 * Seek the validity of a FileCopy in the file library.
	 * @param file_num Numerical file identifier
	 * @param user_num Numerical user identifier
	 * @return TRUE if 'file_num' owned by 'user_num' is valid. Else, FALSE.
	 */
	public boolean fileCopyValid(int file_num, int user_num){
		for(int i=0; i < files[file_num].size(); i++){
			if(files[file_num].get(i).getOwner() == user_num){
				//System.out.print(" "+user_num+"<-"+file_num+".");
				//System.out.print( files[file_num].get(i).getValid()+"\n");
				return files[file_num].get(i).getValid();
				}
		} // Search FileCopy list until match is found
		return false; // Uncalled		
	}
	
	/**
	 * calculate how many good peers has the file.
	 */
	
	//public int numfilegoodpeerhas (int file_num, int User){
		
	//}
	
	/**
	 * Determine if a user possesses a certain file.
	 * @param user_num Numerical user identifier
	 * @param file_num Numerical file identifier
	 * @return TRUE if 'user_num" owns a copy of 'file_num'. Else, FALSE.
	 */
	public boolean hasFile(int user_num, int file_num){
		for(int i=0; i < files[file_num].size(); i++){
			if(files[file_num].get(i).getOwner() == user_num)
				return true;
		} // Search the file's owner list exhaustively
		return false;
	}
	
	/**
	 * Determine the number of files available in this Network.
	 * @return Number of files with at least one owner
	 */
	public int availableFiles(){
		int available = 0;
		for(int i=0; i < GLOBALS.NUM_FILES; i++){
			if(files[i].size() > 0)
				available++;
		} // Count all files with at least one owner
		return available;
	}
	
	/**
	 * Add a file/owner/validity entry to the libraries.
	 * @param user_num Numerical user identifier of file owner
	 * @param file_num Numerical file identifier of file to be added
	 * @param valid Validity of the file being added
	 */
	public void addFile(int user_num, int file_num, boolean valid){
		this.files[file_num].add(new FileCopy(user_num, valid));
		this.users[user_num].incFileCount();
	}

		// ---------------------- QUEUE METHODS ------------------------------
	
	/**
	 * Enqueue a transaction onto tail of the delay queue.
	 * @param trans The transaction to be enqueued
	 */
	public void enqueueTrans(Transaction trans){
		this.delay_queue.add(trans);
	}
	
	/**
	 * Dequeue transaction at the head of the delay queue.
	 * @return Transaction at head of the delay queue
	 */
	public Transaction dequeueTrans(){
		return (this.delay_queue.remove());
	}
	
	/**
	 * Return, but not remove transaction on head of delay queue.
	 * @return Transaction at head of the delay queue
	 */
	public Transaction peekQueue(){
		return (this.delay_queue.peek());
	}
	
	/**
	 * The number of transactions currently in the delay queue.
	 * @return Number of transactions currently in the delay queue.
	 */
	public int queueSize(){
		return (this.delay_queue.size());
	}
	
	
}
