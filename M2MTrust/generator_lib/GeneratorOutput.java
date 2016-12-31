/**
 * Georgia Tech
 * DISL
 * 2016
 */

package generator_lib;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

import core_lib.*;

/**
 * The GeneratorOutput class assists the TraceGenerator driver program in 
 * writing data to the trace file
 */
public class GeneratorOutput{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * The Network parameterization object
	 */
	private final Globals GLOBALS;
	
	/**
	 * Output stream to the trace file
	 */
	private PrintWriter out;
	
	int queryfilenum[];
	double filepopular[];
	int querypeer_se[];
	int queryfile_se[];
	int account_peer=0;
	int account_file=0;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct a GeneratorOutput object
	 * @param output Filename/path of the trace file to be written
	 * @param GLOBALS The Network parameterization object
	 */
	public GeneratorOutput(String output, Globals GLOBALS){
		queryfilenum=new int[GLOBALS.NUM_FILES];
		filepopular=new double[GLOBALS.NUM_FILES];
		querypeer_se=new int[GLOBALS.NUM_TRANS+GLOBALS.WARMUP];
		queryfile_se=new int[GLOBALS.NUM_TRANS+GLOBALS.WARMUP];
		for (int i=0;i<GLOBALS.NUM_FILES;i++){
		queryfilenum[i]=0;
		filepopular[i]=0;
		
		}
		
		for(int j=0;j<GLOBALS.NUM_TRANS;j++){
			querypeer_se[j]=-1;
			queryfile_se[j]=-1;
		}
		
		try{
			FileOutputStream fos = new FileOutputStream(output);
			out = new PrintWriter(fos);
		} catch(FileNotFoundException e){
			System.out.println("\nError: Problems opening output trace file." +
					" Aborting.\n\n");
			System.exit(1);
		} // Open the PrintWriter on output file.
		this.GLOBALS = GLOBALS;
	}
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Write the header (mostly GLOBAL variables) data to the trace file.
	 */
	public void writeHeader(){
		out.printf("%d Users\n", GLOBALS.NUM_USERS);
		out.printf("%d Files\n", GLOBALS.NUM_FILES);
		out.printf("%d Transactions\n", GLOBALS.NUM_TRANS);
		out.printf("%d Maximum Connections\n", GLOBALS.BAND_MAX);
		out.printf("%d Cycle Length per Upload-Download\n", GLOBALS.BAND_PER);
		out.printf("%d Warm-up Transactions\n", GLOBALS.WARMUP);
		out.printf("%f Zipf constant\n", GLOBALS.ZIPF);
		out.printf("%d Pre-Trusted Users\n", GLOBALS.PRE_TRUSTED);
		out.printf("%d Well-Behaved (Good) Users\n", GLOBALS.USR_GOOD);
		out.printf("%d Purely Malicious Users\n", GLOBALS.USR_PURE);
		out.printf("%d Feedback Skewing Users\n", GLOBALS.USR_FEED);
		out.printf("%d Malignant Providing Users\n", GLOBALS.USR_PROV);
		out.printf("%d Disguised Malicous Users\n", GLOBALS.USR_DISG);
		out.printf("%d Sybil Attack Users\n", GLOBALS.USR_SYBL);
		out.printf("%b Intelligent Trans. Generation\n", GLOBALS.SMART_GEN);
		out.printf("%d Trace Generation Seed\n", GLOBALS.RAND_SEED);
		out.printf("%f F Percentage for Disguised Peers\n\n", GLOBALS.F_PERCENTAGE);
		System.out.print("\nHeader complete...\n");
	}
	
	/**
	 * Write User library data to the trace file
	 * @param nw The Network whose User data to write
	 */
	public void writeUsers(Network nw){
		for(int i=0; i < GLOBALS.NUM_USERS; i++){
			out.printf("(%f,", nw.getUser(i).getCleanup());
			out.printf("%f,", nw.getUser(i).getHonesty());
			out.printf("%d,", User.BehaviorToInt(nw.getUser(i).getModel()));
			out.printf("%b)\n", nw.getUser(i).isPreTrusted());
		} // Print all User initialization data to trace
		out.printf("\n"); // Line separator
		System.out.print("User initialization complete...\n");
	}
	
	/**
	 * Write file library data to the trace file
	 * @param nw The Network whose file data to write
	 */
	public void writeLibraries(Network nw){
		Iterator<FileCopy> iter;
		FileCopy copy;
		for(int i=0; i < GLOBALS.NUM_FILES; i++){
			iter = nw.getFileIterator(i);
			while(iter.hasNext()){
				copy = iter.next();
				out.printf("(%d,%d,%b)\n", copy.getOwner(), i, copy.getValid());
			} // Print trace entry for each file copy
		} // Output entries for all files in the Network
		out.print("\n"); // Blank separator
		System.out.print("Library initialization complete...\n");
	}
	
	/**
	 * Write a single transaction to the trace file
	 * @param trans The Transaction whose data to write
	 */
	public void writeTrans(Transaction trans){
		queryfilenum[trans.getFile()]+=1;
		querypeer_se[account_peer++]=trans.getRecv();
		queryfile_se[account_file++]=trans.getFile();
		out.printf("(%d,%d)\n", trans.getRecv(), trans.getFile());
	}
	
	public void writeTransagain(int i){
		out.printf("(%d,%d)\n", querypeer_se[i], queryfile_se[i]);
	}
	
     public void writelinkstructure (int i, LinkedList<Integer>[] a){
    	 out.printf("peer %d neighbors is: ", i);
    	 for (int j=0;j<a[i].size();j++){
    	 out.printf("%d, ", a[i].get(j));
    	 }
    	 
    	 out.printf("\n");
    	 
    	 if(i==GLOBALS.NUM_USERS-1)
    		 out.printf("\n");
     }
     
     public void writematrix(int i, int j, int[][] a){
    	 
    	 out.printf("%d,", a[i][j]);
    	 if (j==GLOBALS.NUM_USERS-1)
    		 out.printf("\n");
    	 if(i==GLOBALS.NUM_USERS-1 &&j==GLOBALS.NUM_USERS-1)
    		 out.printf("\n");
    	 
     }
     public void writefilepopular(){
    	 int sum=0;
       for (int i=0;i<GLOBALS.NUM_FILES;i++){
    	   sum+=queryfilenum[i];
    	  // System.out.println(i+".querynums= "+queryfilenum[i]);
       }
       
       for(int j=0;j<GLOBALS.NUM_FILES;j++){
          filepopular[j]= (double)queryfilenum[j]/ (double)sum;
       }
       for(int k=0;k<GLOBALS.NUM_FILES;k++){
          out.printf("%d.popular= %f\n", k, filepopular[k ]);
          
       }
     }
     
     public void generateemptyline(){
    	 out.printf("\n");
    	 
     }
	/**
	 * Shutdown (flush and close) the output stream
	 */
	public void shutdown(){
		out.flush();
		out.close();
	}
	
}
