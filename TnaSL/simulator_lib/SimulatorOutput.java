/**
 * Georgia Tech
 * DISL
 * 2016
 */

package simulator_lib;

import java.io.*;
import core_lib.*;
import trust_system_lib.*;

/**
 * The SimulatorOutput class assists the TraceSimulator driver program in
 * writing data to the output/statistics file.
 */
public class SimulatorOutput{
	
	// ************************** PRIVATE FIELDS *****************************

	/**
	 * Stream to the output/statistics file.
	 */
	private PrintWriter out;
	
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct a SimulatorOutput object.
	 * @param filename Path/filename of the output file to be written
	 */
	public SimulatorOutput(String filename){
		try{
			FileOutputStream fos = new FileOutputStream(filename);
			out = new PrintWriter(fos);
		} catch(FileNotFoundException e){
			System.out.println("\nError: Problems opening output trace file." +
					" Aborting.\n\n");
			System.exit(1);
		} // Open the PrintWriter on output file.
	}
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Write header to the output/statistics file, mostly global variable data.
	 * @param GLOBALS The Network parameterization object
	 * @param gen_seed Random seed used in trace generation
	 * @param TALG The trust algorithm being used for this run
	 * @param STRATEGY Malicious strategy being applied during this simulation
	 */
	public void printHeader(Globals GLOBALS, long gen_seed, TrustAlg TALG, 
			SimulatorMalicious.MAL_STRATEGY STRATEGY){
		out.print("\n----------- TRACE PARAMETERS ---------");
		out.printf("\n>Number of Peers:        %d", GLOBALS.NUM_USERS);
		out.printf("\n>Number of Files:        %d", GLOBALS.NUM_FILES);
		out.printf("\n>Number of Transactions: %d", GLOBALS.NUM_TRANS);
		out.printf("\n>Max. User Connections:  %d", GLOBALS.BAND_MAX);
		out.printf("\n>Bandwidth Period:       %d", GLOBALS.BAND_PER);
		out.printf("\n>Warm-up Transactions:   %d", GLOBALS.WARMUP);
		out.printf("\n>Zipf Constant:          %f", GLOBALS.ZIPF);
		out.printf("\n>Pre-Trusted Users:      %d", GLOBALS.PRE_TRUSTED);
		out.printf("\n>Good Behaving Users:    %d", GLOBALS.USR_GOOD);
		out.printf("\n>Purely Malicious Users: %d", GLOBALS.USR_PURE);
		out.printf("\n>Feedback Skewing Users: %d", GLOBALS.USR_FEED);
		out.printf("\n>Maligned Providers:     %d", GLOBALS.USR_PROV);	
		out.printf("\n>Disguised Malignants:   %d", GLOBALS.USR_DISG);
		out.printf("\n>Sybil Attackers:        %d", GLOBALS.USR_SYBL);
		out.printf("\n>Smart Trans Gen?:       %b", GLOBALS.SMART_GEN);
		out.printf("\n>Generator Rand Seed:    %d", gen_seed);
		out.printf("\n>Simulator Rand Seed:    %d\n\n", GLOBALS.RAND_SEED);
		out.printf("\n>Simulator F_PERC:    %f\n\n", GLOBALS.F_PERCENTAGE);
		out.printf("---------- SIMULATOR SPECIFIC --------");
		out.printf("\n>Simulator used:         %s", TALG.algName());
		out.printf("\n>Malicious strategy:     %s\n\n", print(STRATEGY));
	}
	
	public void printuploadnum(Network nw){
		out.printf(">Number of uploading files as provider: ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 out.printf(i+".uploadnum= "+ nw.getUser(i).getNumuploads());
		     out.printf("\n");
		}
		int sum=0;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
			sum+=nw.getUser(i).getNumuploads();
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			out.printf(i+".uploadpercentage= "+ (double)nw.getUser(i).getNumuploads()/(double)sum);
		     out.printf("\n");
		}
		out.printf("\n\n");
	}
	
	
				
	/**
	 * Write final statistics to the output/statics file.
	 * @param GLOBALS The Network parameterization object
	 * @param STATS The statistical variable wrapper object
	 */
	public void printStatistics(Globals GLOBALS, Statistics STATS){
		
		int INCMPLETE_TRANS = (STATS.NUM_RECV_BLK_TR + STATS.NUM_SEND_BLK_TR);
		int COMPLETED_TRANS = (GLOBALS.NUM_TRANS - INCMPLETE_TRANS);
//		int NUM_VALID_TRANS = (COMPLETED_TRANS - STATS.NUM_INVAL_TRANS);
		int TOTAL_GOOD_TRANS = (STATS.NUM_GOOD_SUCC + STATS.NUM_GOOD_FAIL);
		
		out.printf("-------- TRANSACTION OVERVIEW --------\n");
		out.printf(">Transacts Attempted:    %d\n", GLOBALS.NUM_TRANS);
		out.printf(">Transacts Completed:    %d\n", COMPLETED_TRANS);
		out.printf(">Transacts Incomplete:   %d\n", INCMPLETE_TRANS);
		//out.printf(">Transacts Incomplete due to receiving end:    %d\n", STATS.NUM_RECV_BLK_TR);
		//out.printf(">Transacts Incomplete due to source end:    %d\n\n", STATS.NUM_SEND_BLK_TR);

		out.printf("-------- INCOMPLETE TRANS SUM --------\n");
		out.printf(">Transacts Incomplete:   %d\n", INCMPLETE_TRANS);
		out.printf(">Reception Declined:     %d\n", STATS.NUM_RECV_BLK_TR);
		out.printf(">No Eligible Senders:    %d\n\n", STATS.NUM_SEND_BLK_TR);
		
		out.printf("--------- COMPLETE TRANS SUM ---------\n");
		out.printf(">Transacts Completed:    %d\n", COMPLETED_TRANS);
		//out.printf(">Valid Transactions:     %d\n", NUM_VALID_TRANS);
		out.printf(">valid Transactions:     %d\n", STATS.NUM_VAL_TRANS);
		out.printf(">Invalid Transactions:   %d\n\n", STATS.NUM_INVAL_TRANS);
	
		
		out.printf("--------- FEEDBACK OVERVIEW ----------\n");
		out.printf(">Feedbacks Committed:    %d\n", COMPLETED_TRANS);
		out.printf(">Truthful Feedbacks:     %d\n", STATS.NUM_FBACK_TRUE);
		out.printf(">Dishonest Feedbacks:    %d\n", STATS.NUM_FBACK_LIES);
		out.printf(">Sybil-User Feedbacks:   %d\n\n", STATS.NUM_FBACK_SYBL);
		
		out.printf("--------- EVALUATION METRIC ----------\n");
		out.printf(">Good User Transacts:    %d\n", TOTAL_GOOD_TRANS);
		out.printf(">Good User Successes:    %d\n", STATS.NUM_GOOD_SUCC);
		out.printf(">Good User Failures:     %d\n\n", STATS.NUM_GOOD_FAIL);
		
		out.printf("--------- FILES PROVIDED BY MALICIOUS PEERS  ----------\n");
		out.printf(">Authentic Files by Malicious peers:     %d\n", STATS.NUM_validfile_maliciousprovide);
		out.printf(">Authentic Files by Malicious peers with Good receiver:     %d\n\n", STATS.Good_recv_NUM_validfile_maliciousprovide);
		
		//out.printf(">Fraction of inauthentic downloads (exclude No Eligible Senders): %f\n", (double)(STATS.NUM_INVAL_TRANS)/(double)(STATS.NUM_INVAL_TRANS+STATS.NUM_VAL_TRANS));
		out.printf(">Fraction of inauthentic downloads: %f\n", (double)(STATS.NUM_INVAL_TRANS+STATS.NUM_SEND_BLK_TR)/(double)(STATS.NUM_INVAL_TRANS+STATS.NUM_SEND_BLK_TR+STATS.NUM_VAL_TRANS));
	    out.printf("\n\n");
	}
	public void writeruntime(double runtime){
		out.printf(">Simulation runtime: %f secs\n\n", runtime);
	}
	public void printTrustDegree(Statistics STATS){
		System.out.println("--------- DIRECT TRUST DEGREES  ----------");
		System.out.println(">Minimum Trust Degree:\t"+STATS.minDirectDegree);
		System.out.println(">Maximum Trust Degree:\t"+STATS.maxDirectDegree);
		System.out.println(">Average Trust Degree:\t"+STATS.avgDirectDegree);
		System.out.println("> # Zero Trust Degree:\t"+STATS.countZeroTrustDegree);
		
		out.println("--------- DIRECT TRUST DEGREES  ----------");
		out.println(">Minimum Trust Degree:\t"+STATS.minDirectDegree);
		out.println(">Maximum Trust Degree:\t"+STATS.maxDirectDegree);
		out.println(">Average Trust Degree:\t"+STATS.avgDirectDegree);
		out.println("> # Zero Trust Degree:\t"+STATS.countZeroTrustDegree+"\n");
	}
	public void printFinalTrust(Network nw){
		double[] trusts = nw.ALG.showtrust();
		if(trusts==null)
			return;
		for(int i=0;i<trusts.length;++i)
			out.println(i+".trustVal= " + trusts[i]);
		out.println();
	}
	/**
	 * Shutdown (flush and close) the output stream.
	 */
	public void shutdown(){
		out.flush();
		out.close();
	}
	
	// ************************* PRIVATE METHODS *****************************
	
	/**
	 * Return a string representation of a MAL_STRATEGY enumeration element.
	 * @param STRATEGY MAL_STRATEGY enumeration element
	 * @return A string representation of a MAL_STRATEGY enumeration element
	 */
	private String print(SimulatorMalicious.MAL_STRATEGY STRATEGY){
		if(STRATEGY == SimulatorMalicious.MAL_STRATEGY.ISOLATED)
			return("Isolated");
		else if(STRATEGY == SimulatorMalicious.MAL_STRATEGY.NAIVE)
			return("Naive");
		else if (STRATEGY==SimulatorMalicious.MAL_STRATEGY.COLLECTIVE)
			return("Collective");
		else if (STRATEGY==SimulatorMalicious.MAL_STRATEGY.DISGUISE)
			return ("Disguise");
		else
			return ("SPY");
	}	
	
}
