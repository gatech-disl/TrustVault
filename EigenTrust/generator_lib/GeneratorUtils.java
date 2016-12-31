/**
 * Georgia Tech
 * DISL
 * 2016
 */

package generator_lib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import core_lib.*;

/**
 * The GeneratorUtils class assists the TraceGenerator driver program with 
 * the generation of users, file libraries, and transactions.
 */
public class GeneratorUtils{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * The Network for which the generations are taking place.
	 */
	private Network nw;
	
	/**
	 * The Network parameterization object.
	 */
	private final Globals GLOBALS;
	
	/**
	 * Sum of all file Zipf frequencies.
	 */
	private double ZIPF_SUM;
	
	private LinkedList<Integer>[] query_file;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct a GeneratorUtils object.
	 * @param network The Network on which the utilities will operate
	 * @param GLOBALS The Network parameterization object
	 */
	public GeneratorUtils(Network network, Globals GLOBALS){
		this.nw = network;
		this.GLOBALS = GLOBALS;
		this.ZIPF_SUM = 0.0;
		
	  query_file= new LinkedList [GLOBALS.NUM_FILES];
		for (int i=0;i<GLOBALS.NUM_FILES;i++){
			//System.out.println(GLOBALS.NUM_FILES);
			query_file[i]=new LinkedList<Integer>();
		}
		
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Generate/populate the User library.
	 */
	public void generateUsers(){
		int total = 0;
		total += genUserType(User.Behavior.USR_PURE, GLOBALS.USR_PURE, total);
		total += genUserType(User.Behavior.USR_FEED, GLOBALS.USR_FEED, total);
		total += genUserType(User.Behavior.USR_PROV, GLOBALS.USR_PROV, total);
		total += genUserType(User.Behavior.USR_DISG, GLOBALS.USR_DISG, total);
		total += genUserType(User.Behavior.USR_SYBL, GLOBALS.USR_SYBL, total);
		genUserType(User.Behavior.USR_GOOD, GLOBALS.USR_GOOD, total);
	}
	
	/**
	 * Generate/populate the initial file library.
	 */
	public void generateInitLibs(){
		boolean valid;
		double usr_cleanup;
		int filenum_contrl=0;
		for(int i=0; i < GLOBALS.NUM_USERS; i++){
			for(int j=0; j < GLOBALS.NUM_FILES; j++){
				if(GLOBALS.RAND.nextDouble() <= getZipf(j)){
					usr_cleanup = nw.getUser(i).getCleanup();					
					valid = (GLOBALS.RAND.nextDouble() < usr_cleanup);
					// System.out.println("rand= "+GLOBALS.RAND.nextDouble()+" valid= "+valid+" usr_cleanup= "+usr_cleanup);
					if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD && num_filegoodpeerhas(j)<filenum_contrl)
					  nw.addFile(i, j, valid); 
					//else //if(nw.getUser(i).getModel()!=User.Behavior.USR_GOOD)
					//  nw.addFile(i, j, false);
				} // Add file to library based on parameter thresholds				
			} // Each user can (probabilistically) own any file
		} // Initialize libraries for all users
		for(int i=0; i < GLOBALS.NUM_FILES; i++){ 
			this.ZIPF_SUM += getZipf(i);
		} // Calculate total ZIPF weight for all files
	}
	
	public void generateInitLibs_malicious(){
		boolean valid;
		double user_cleanup;
		double percent_DISG=0.55 ;  // percentage of owned files by peer: disguise: 0.55 ; spy: 1.0
		double percent_PURE=1.0 ;   // unchanged 
		//double percent_USR_FEED=0.8;
		for(int i=0;i<GLOBALS.NUM_USERS;i++){
			if(nw.getUser(i).getModel()==User.Behavior.USR_DISG){
				for(int j=0;j<GLOBALS.NUM_FILES*percent_DISG;j++){
					user_cleanup = nw.getUser(i).getCleanup();
					valid = (GLOBALS.RAND.nextDouble() < user_cleanup);
					if(!nw.hasFile(i, j))
						nw.addFile(i, j, valid);
				}			}		}
		for(int i=0;i<GLOBALS.NUM_USERS;i++){
			if(nw.getUser(i).getModel()==User.Behavior.USR_PURE){
				for(int j=0;j<GLOBALS.NUM_FILES*percent_PURE;j++){
					user_cleanup = nw.getUser(i).getCleanup();
					valid = (GLOBALS.RAND.nextDouble() < user_cleanup);
					if(!nw.hasFile(i, j))
						nw.addFile(i, j, valid);
				}	} }			} 
	/**
	 * generate a certain number of files possessed by good peers. 
	 */
	public void checkgoodpeer_new(){
		int contrl,flag, file_num;
		int max= 3 ;  // isolated/collective/disguise: 3(0) when the number of system peers is 73(0); spy: 2(0) in when the number of system peers is 103(0)
		boolean valid=true;
		for(int i=0;i<GLOBALS.NUM_USERS;i++){
			if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD){ 
				flag=0;
			   // contrl=1+nw.GLOBALS.RAND.nextInt(max);
			     contrl=max;
			// System.out.println("randmax="+contrl);
			for(int j=0;j<GLOBALS.NUM_FILES;j++){
				if(nw.hasFile(i, j) && nw.fileCopyValid(j,i))
				    flag++;	
			}
			
			while (flag<contrl){
				
					for(int k=0;k<nw.GLOBALS.NUM_FILES && flag<contrl;k++){
						if(GLOBALS.RAND.nextDouble() <= getZipf(k)){					
							valid = (GLOBALS.RAND.nextDouble() < nw.getUser(i).getCleanup());
							if(!nw.hasFile(i, k)){
							     nw.addFile(i, k, valid); 
							     flag++;       	 
							 }	
						} 		
					}
				}
			}
		}
	}
	
	/**
	 * guarantee each file is possessed by peers.
	 */
	public void generateInitLibs_good(){
		ArrayList<Integer> pretrust= new ArrayList<Integer>();
		boolean valid=true;
		boolean flag;
		//int filenum_contrl=2;
		int ranuser;
	    	for(int i=0;i<GLOBALS.NUM_FILES;i++){
	    	   flag=false;
	    	   //System.out.println("i= "+i);
	    		for(int j=0;j<GLOBALS.NUM_USERS;j++){
	    			if(nw.getUser(j).getModel()==User.Behavior.USR_GOOD && !nw.getUser(j).isPreTrusted()){
	    			//System.out.print("j= "+j+"  ");
					if(nw.hasFile(j, i) && (nw.fileCopyValid(i, j))){
						flag=true;		
						break;	
					}	
				 }
	    		}
	    		if (flag==false){
	    			//double randm=0;
						boolean flag2=false;
						for(int k=0;k<GLOBALS.NUM_USERS;k++){
							if (nw.getUser(k).getModel()==User.Behavior.USR_GOOD && !nw.getUser(k).isPreTrusted()  && nw.getUser(k).getNumFiles()==0 ){
							// randm=1;
							// if(nw.GLOBALS.RAND.nextDouble()<randm){
							     nw.addFile(k, i, valid);
							     flag2=true;
							     break;
							// }
						   }	
							if(nw.getUser(k).isPreTrusted() && nw.getUser(k).getNumFiles()==0){
								nw.addFile(k, i, valid);
								flag2=false;
								break;
							}
						}
						
						if(flag2==false){
						   do{
					            ranuser=nw.GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);  
						     } while(nw.getUser(ranuser).getModel()!=User.Behavior.USR_GOOD || nw.hasFile(ranuser, i) || nw.getUser(ranuser).isPreTrusted());
						      nw.addFile(ranuser, i, valid);
						}
	    		       }
					}
	    	/*
	    	 * define number of files belonged to good peers
	    	 */
	    	int num_file=2;
	    	int randmuser;
	    	for(int i=0;i<nw.GLOBALS.NUM_FILES;i++){
	    		while(num_filegoodpeerhas(i)<num_file){
	    			do{
	    				randmuser=nw.GLOBALS.RAND.nextInt(nw.GLOBALS.NUM_USERS);
	    			}while(nw.getUser(randmuser).getModel()!=User.Behavior.USR_GOOD || nw.hasFile(randmuser, i));
	    			nw.addFile(randmuser, i, valid);
	    		}
	    	}
	    	
	    	
	    		//System.out.println();
	    	}
	
	/**
	 * 
	 * @param file_num
	 * @return
	 */
	
	
	public int num_filegoodpeerhas(int file_num){
		int acc=0;
			for(int j=0;j<GLOBALS.NUM_USERS;j++){
			 if(nw.getUser(j).getModel()==User.Behavior.USR_GOOD){
				 if(nw.fileCopyValid(file_num, j)){
				   acc++;	 
				 }
			}		
			}
		return acc;
	}
	
	/**
	 * 'Intelligently' generate a transaction.
	 * @return A Transaction object containing the data generated
	 */
	public Transaction genTransactionSmart(int i){
		int recv, file_num;
	
	   if(i<GLOBALS.NUM_FILES){
		   do{
				recv = GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
				}while (!nw.getUser(recv).isPreTrusted());
		   
	    	nw.addFile(recv, i, true);
	    	return (new Transaction(-1, -1, recv, i, false));
	    }
	    else{ 
	    	boolean cond3, cond4=false ;
			do{ // A receiver shouldn't already have a "full" library
				recv = GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
			//	cond3 = nw.getUser(recv).getModel()!=User.Behavior.USR_GOOD;
				cond3=GLOBALS.RAND.nextDouble()>nw.getUser(recv).getCleanup();
			
		      if(nw.getUser(recv).getModel()!=User.Behavior.USR_GOOD){
				   // cond4=GLOBALS.RAND.nextDouble()>nw.getUser(recv).getCleanup();
				      cond4=GLOBALS.RAND.nextDouble()> 0.1 ; // issue queries based on the probability 0.1
				}
		        
			//	else
				//	cond3=GLOBALS.RAND.nextDouble()>nw.getUser(recv).getCleanup();
				
			} while(cond3||cond4); // || nw.getUser(recv).getNumFiles() == nw.availableFiles());		
			
		boolean cond1, cond2;
		do{ // Receiver must not already have file, and it must be available
			file_num = inverseZipf(GLOBALS.RAND.nextDouble() * this.ZIPF_SUM);
		    //cond1 = nw.hasFile(recv, file_num);
			cond2 = nw.fileOwners(file_num) == 0;		 
		} while(cond2); // || cond1);
		nw.addFile(recv, file_num, true);
		query_file[file_num].add(recv);
		System.out.println("recv= "+ recv+", file_num= "+ file_num+ ", true");
		return (new Transaction(-1, -1, recv, file_num, false));
	  }
	}
	
	/**
	 * 'Naively' generate a transaction.
	 * @return A Transaction object containing the data generated
	 */
	public Transaction genTransactionNaive(){
		int recv = GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
		int file_num = GLOBALS.RAND.nextInt(GLOBALS.NUM_FILES);
		return (new Transaction(-1, -1, recv, file_num, false));
	}
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * Generate all users of a specified type, and place in library
	 * @param model The Behavior model of the generated users
	 * @param quantity The number of users of this type to generate
	 * @param prev Identifier where first such user will be placed in User lib.
	 * @return The number of users generated
	 */
	private int genUserType(User.Behavior model, int quantity, int prev){
		for(int i=0; i < quantity; i++){
			if((model == User.Behavior.USR_GOOD) && (i < GLOBALS.PRE_TRUSTED))
				nw.setUser(prev+i, new User(model, true, GLOBALS));
			else
				nw.setUser(prev+i, new User(model, false, GLOBALS));
		} // Initialize the specified number of Users, per model
		return quantity;
	}
	
	// *************************** ZIPF PRIVATES *****************************
	
	/**
	 * Return the Zipf frequency of a given file. 
	 * @param rank File number whose frequency is desired
	 * @return Fraction describing frequency rate on [0..1]
	 */
	private double getZipf(int rank){
		return ((1.0) / (Math.pow((rank + 2.0), GLOBALS.ZIPF)));
	}	
	
	/**
	 * Given an number on [0..ZIPF_SUM], map that value to a file identifier.
	 * This is performed such that if a number is randomly selected on that
	 * interval, the probability of a file identifier being returned corresponds
	 * to its Zipf frequency as calculated by the getZipf() method. I am 
	 * dissatisfied with the efficiency of this calculation.
	 * @param weight Number on the [0..ZIPF_SUM] interval
	 * @return Corresponding file identifier, per probability weighting
	 */
	private int inverseZipf(double weight){
		double total = 0.0;
		for(int i=0; i < GLOBALS.NUM_FILES; i++){
			total += getZipf(i);
			if(total > weight)
				return i;
		} // Iteratively sum frequencies
		return (GLOBALS.NUM_FILES - 1);
	}
	
	
	public void printqueryfile(Network nw){
		for (int i=0;i<GLOBALS.NUM_FILES;i++)
			System.out.println(i+".querytimes= "+ query_file[i].size());
	}
	
	
}
