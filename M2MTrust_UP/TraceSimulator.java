/**
 * Georgia Tech
 * DISL
 * 2016
 */



import java.io.*;
import core_lib.*;
import simulator_lib.SimulatorInput;
import simulator_lib.SimulatorOutput;
import simulator_lib.SimulatorUtils;
import simulator_lib.SimulatorMalicious;
import simulator_lib.SimulatorSource;
import trust_system_lib.*;

/**
 * The TraceSimulator class, when given a trace file and TM algorithm,
 * simulates the trace in that environment and outputs a statistical file.
 */
public class TraceSimulator{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * The TSYS enumeration lists the implemented TM algorithms.
	 */
	private enum TSYS{NONE, EIGEN, EigenTrustFade, ET_INC, TNA_SL, SIMILARITY, SIMILIRARITY_EIGEN, EIGENCREDIT,
		linearthreshold,SimiRevLTComWeight,linearthreshold_rev,linearthreshold_Eigen,
		SISwithReinfect, SIR,lt_feedbackcredibility_Eigen, M2MTrust,M2MTrust_up, serviceTrust, peerTrust};
	
	/**
	 * The path/filename of the input trace file.
	 */
	private static String FILE_NAME;
	
	
	/**
	 * Instance of a trust algorithm managing a simulation.
	 */
	private static TrustAlg TALG;
	
	/**
	 * Label describing the algorithm managing a simulation.
	 */
	private static TSYS TSYSTEM;
	
	/**
	 * Malicious strategy being applied during this simulation
	 */
	private static SimulatorMalicious.MAL_STRATEGY STRATEGY;
	/*
	 * define the number of inauthentic downloads
	 */
	private static int num_inauthticdwn=200;
	 /**
	  * define the start_time as a global variable 
	  */
	private static long start_time;

	// ************************** PUBLIC METHODS *****************************

	
	/**
	 * The main driver method.
	 * @param args  See the README document for usage information
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		start_time = System.currentTimeMillis();
		parse_arguments(args);
	
			// Open the input file, and read off global variables
		SimulatorInput Trace = new SimulatorInput(FILE_NAME);
		Globals GLOBALS = Trace.parseGlobals();
		
			// Create the network and add static trace data to it
		Network nw = new Network(GLOBALS);
		Structure strct= new Structure (GLOBALS);
		Trace.parseUsers(nw);
		Trace.parseLibraries(nw);
		System.out.print("\nTrace file parsed and static initialization " +
				"complete...\n");
		
		  System.out.print("\n");
		  Trace.parsematrix(strct, nw);
	//	  System.out.print("\n");
	//	  strct.showmatrix();
		  Trace.accountfilenumber(nw);
		//strct.Linkrelation(nw);
			// Create and parameterize object coordinating malicious behavior
		  
		SimulatorMalicious mal = new SimulatorMalicious(nw, STRATEGY);
		
			// Set and construct the TM managing the Network
		if(TSYSTEM == TSYS.EIGEN)
			TALG = new EigenTM(nw);
		else if(TSYSTEM==TSYS.M2MTrust) 
			TALG = new M2MTrust(nw);    // each peer propagates its trust to those neighbors that are activated by this peer
		else if(TSYSTEM==TSYS.M2MTrust_up) 
			TALG = new M2MTrust_UP(nw);    // each peer propagates its trust to those neighbors that are activated by this peer
		else if(TSYSTEM==TSYS.peerTrust) 
			TALG = new PeerTrust(nw);    // each peer propagates its trust to those neighbors that are activated by this peer
		else if(TSYSTEM==TSYS.TNA_SL) 
			TALG = new TnaSlTM(nw);   // 
		else if(TSYSTEM==TSYS.NONE) 
			TALG = new NoneTM(nw);
		
		System.out.printf("F_PERCENTAGE VALUE = %f\n",GLOBALS.F_PERCENTAGE);
			// Perform the warm-up transactions
		System.out.printf("Beginning warm-up phase... (%d transactions)\n", 
				GLOBALS.WARMUP);
		nw.setAlg(TALG);
		
		SimulatorUtils Simulate = new SimulatorUtils();	
		Simulate.maliciarray(nw);
	//	Simulate.showcollection();
		 SimulatorSource simsource= new SimulatorSource(nw);
		// simsource.SimulatorSourceInitial(nw);
		 if (TSYSTEM==TSYS.NONE)
			 simsource.randmvalue(true);
		 else
			 simsource.randmvalue(false);
		 
		    FILE_NAME = FILE_NAME.substring(0, FILE_NAME.lastIndexOf('.')+1);
			FILE_NAME = FILE_NAME.concat(TALG.fileExtension());
			System.out.print("fileextension= "+TALG.fileExtension());
			SimulatorOutput Output = new SimulatorOutput(FILE_NAME);
		for(int i=0; i < GLOBALS.WARMUP; i++){
			Simulate.simTrans(nw, i, Trace.parseNextTransaction(), mal, TALG, strct, STRATEGY, Trace);	
			if(((i+1) % 50 == 0) && (i != 0)){
				//  Simulate.showNegPos(nw);
			    //	Simulate.callocaltrust(nw, TALG);
				    Simulate.calreputation(nw, 0, i,TALG);
				  //  Simulate.caladaptivelocaltrust(nw, TALG);
				  //  Simulate.caladaptivereputation(nw, 0,i,TALG);
			     //	Simulate.setnegpos(nw);
				    }
			if(((i+1) % 50 == 0) && (i != 0)){
				System.out.printf("Warm-up transactions completed (starting from i=0): %d...\n", (i+1));
				System.out.flush();
			} // Periodic status updates during warm-up phase
		} // Parse and dynamically perform transactions
		System.out.print("Warm-up phase complete...\n");		
		nw.STATS.reset();  // Reset statistics and perform actual transactions
		for (int i=0;i<nw.GLOBALS.NUM_USERS;++i)
			nw.getUser(i).resetUploads();
		System.out.printf("Beginning simulation phase... (%d transactions)\n", 	GLOBALS.NUM_TRANS);
		
		for(int i=GLOBALS.WARMUP; i < (GLOBALS.WARMUP+GLOBALS.NUM_TRANS); i++){
			Simulate.simTrans(nw, i, Trace.parseNextTransaction(), mal, TALG, strct, STRATEGY, Trace);
			if(((((i+1)-GLOBALS.WARMUP) % 50 == 0) && (i != 0))||(i==GLOBALS.WARMUP+GLOBALS.NUM_TRANS-1)){
				//  Simulate.showNegPos(nw);
			    //	Simulate.callocaltrust(nw, TALG);
				    Simulate.calreputation(nw, 0, i,TALG);
				   // Simulate.caladaptivelocaltrust(nw, TALG);
				   // Simulate.caladaptivereputation(nw, 0,i,TALG);    
			//	TALG.adaptivetimewindow();  // evoke the adaptive time windows		
			//	Simulate.setnegpos(nw, TALG);
			}
			if((((i+1)-GLOBALS.WARMUP) % 50 == 0) && (i != 0)){
				System.out.printf("Transactions completed so far: %d...\n", 
						(i-GLOBALS.WARMUP+1));
				System.out.flush();
			} // Periodic status updates during simulation phase
		} // Parse and dynamically perform transactions
		System.out.println("\r\nSimulation phase complete"+"("+GLOBALS.WARMUP+"(Warm up)"+"+"+GLOBALS.NUM_TRANS+"(Transactions))!");
		Simulate.commitRemaining(nw, GLOBALS.WARMUP + GLOBALS.NUM_TRANS, TALG, STRATEGY, strct);
		TALG.getDirectTrusted();
		Output.printTrustDegree(nw.STATS);
			// Set extension on output file; open; print header and stats
//		FILE_NAME = FILE_NAME.substring(0, FILE_NAME.lastIndexOf('.')+1);
//		FILE_NAME = FILE_NAME.concat(TALG.fileExtension());
//		System.out.print("fileextension= "+TALG.fileExtension());
//		SimulatorOutput Output = new SimulatorOutput(FILE_NAME);
		Output.printHeader(GLOBALS, Trace.getGenSeed(), TALG, STRATEGY);
		Output.printStatistics(nw.GLOBALS, nw.STATS);
		Output.printFinalTrust(nw);
		Output.printuploadnum(nw);
			
		
			// Calculate runtime and print final notes to terminal
		long stop_time = System.currentTimeMillis();
		double run_time = ((stop_time - start_time) / 1000.0); 
		System.out.printf("Run complete! Data written to %s\r\n", FILE_NAME);
		System.out.printf("Simulation runtime: %f secs\r\n", run_time);
		
		Output.writeruntime(run_time);
			// Cleanup and exit
		Trace.shutdown();
		Output.shutdown();
		/*STARTT
		try{
			PrintWriter __pw = new PrintWriter(new File(FILE_NAME+".trustNetworkDensity"));
			int cur = 0;
			double total = 0;
			double min=999999,max=-1,avg;
			for(int i=0;i<GLOBALS.NUM_USERS;++i){
				cur=0;
				for (int j=0;j<GLOBALS.NUM_USERS;++j){
					if(i!=j)
						if(nw.getUserRelation(i, j).getTrust()>0.000001)
							cur++;
				}
				if(cur<min)
					min=cur;
				if(cur>max)
					max=cur;
				total+=cur;
			}
			avg = total/GLOBALS.NUM_USERS;  pp
			__pw.println("TOTAL:"+total+"MIN:"+min+"\tMAX:"+max+"\tAVG:"+avg);
			__pw.flush();
			__pw.close();
			//(new Scanner(System.in)).nextInt();
		}
		catch(Exception e){e.printStackTrace();}
		/*ENDD*/
		return;
	}
	
	// ************************** PRIVATE METHODS ****************************	
	/*
	 * set the number of inauthentic downloads 
	 */
	
	private static void setinauthenticdwn(Network nw, SimulatorOutput Output, Globals GLOBALS,SimulatorInput Trace){
	    if((nw.STATS.NUM_INVAL_TRANS+nw.STATS.NUM_SEND_BLK_TR) >= num_inauthticdwn){
    	Output.printHeader(GLOBALS, Trace.getGenSeed(), TALG, STRATEGY);
		Output.printStatistics(nw.GLOBALS, nw.STATS);
		Output.printuploadnum(nw);
		long stop_time = System.currentTimeMillis();
		double run_time = ((stop_time - start_time) / 1000.0); 
		System.out.printf("Run complete! Data written to %s\n", FILE_NAME);
		System.out.printf("Simulation runtime: %f secs\n\n", run_time);
		
		Output.writeruntime(run_time);
		System.exit(1);
   }
	}
	/**
	 * Parse the command-line arguments provided to the main() method.
	 * @param args See the enclosed README document for usage information
	 */
	private static void parse_arguments(String[] args){
		//System.out.print("args.length=" + args.length);
		
		if(args.length != 6){
			System.out.print("\nInvalid # of arguments. Aborting.\n\n");
			System.exit(1);
		} // Check the number of arguments
		
		for(int i=1; i < args.length; i+=2){
			if(args[i-1].equalsIgnoreCase("-input"))
				FILE_NAME = args[i];
			else if(args[i-1].equalsIgnoreCase("-tm")){
				if(args[i].equalsIgnoreCase("eigen"))
					TSYSTEM = TSYS.EIGEN;
				else if(args[i].equalsIgnoreCase("EigenFade"))
					TSYSTEM = TSYS.EigenTrustFade;
				else if(args[i].equalsIgnoreCase("similarity"))
					TSYSTEM = TSYS.SIMILARITY;
				else if(args[i].equalsIgnoreCase("similarity_eigen"))
					TSYSTEM = TSYS.SIMILIRARITY_EIGEN;
				else if(args[i].equalsIgnoreCase("eigencredit"))
					TSYSTEM = TSYS.EIGENCREDIT;
				else if(args[i].equalsIgnoreCase("linearthreshold"))
					TSYSTEM = TSYS.linearthreshold;
				else if(args[i].equalsIgnoreCase("linearthreshold_rev"))
					TSYSTEM = TSYS.linearthreshold_rev;
				else if(args[i].equalsIgnoreCase("linearthreshold_Eigen"))
					TSYSTEM = TSYS.linearthreshold_Eigen;
				else if(args[i].equalsIgnoreCase("lt_feedbackcredibility_Eigen"))
					TSYSTEM = TSYS.lt_feedbackcredibility_Eigen;
				else if(args[i].equalsIgnoreCase("simiRevltComWeight"))
					TSYSTEM = TSYS.SimiRevLTComWeight;
				else if(args[i].equalsIgnoreCase("m2mtrust"))
					TSYSTEM = TSYS.M2MTrust;
				else if(args[i].equalsIgnoreCase("m2mtrust_up"))
					TSYSTEM = TSYS.M2MTrust_up;
				else if(args[i].equalsIgnoreCase("peertrust"))
					TSYSTEM = TSYS.peerTrust;
				else if(args[i].equalsIgnoreCase("servicetrust"))
					TSYSTEM = TSYS.serviceTrust;
				else if(args[i].equalsIgnoreCase("SISReinfected"))
					TSYSTEM = TSYS.SISwithReinfect;
				else if(args[i].equalsIgnoreCase("SIRTrust"))
					TSYSTEM = TSYS.SIR;
				else if(args[i].equalsIgnoreCase("eigentrust"))
					TSYSTEM = TSYS.EIGEN;
				else if(args[i].equalsIgnoreCase("et_inc"))
					TSYSTEM = TSYS.ET_INC;
				else if(args[i].equalsIgnoreCase("etinc"))
					TSYSTEM = TSYS.ET_INC;
				else if(args[i].equalsIgnoreCase("tna_sl"))
					TSYSTEM = TSYS.TNA_SL;
				else if(args[i].equalsIgnoreCase("tnasl"))
					TSYSTEM = TSYS.TNA_SL;
				else if (args[i].equalsIgnoreCase("none"))
					TSYSTEM = TSYS.NONE;
				else{
					System.out.print("\n malicious type input wrong.\n\n");
					System.exit(1);
				}
					
			} else if(args[i-1].equalsIgnoreCase("-strategy")){
				if(args[i].equalsIgnoreCase("isolated"))
					STRATEGY = SimulatorMalicious.MAL_STRATEGY.ISOLATED;
				else if(args[i].equalsIgnoreCase("collective"))
					STRATEGY = SimulatorMalicious.MAL_STRATEGY.COLLECTIVE;
				else if(args[i].equalsIgnoreCase("disguise"))
					STRATEGY = SimulatorMalicious.MAL_STRATEGY.DISGUISE;
				else if(args[i].equalsIgnoreCase("spy"))
					STRATEGY = SimulatorMalicious.MAL_STRATEGY.SPY;
				else // if(args[i].equalsIgnoreCase("naive"))
					STRATEGY = SimulatorMalicious.MAL_STRATEGY.NAIVE;
			} else{ 
				System.out.print("\nRequired argument missing. Aborting.\n\n");
				System.exit(1);
			} // Neither '-input' or '-trust_sys' should be omitted
		} // Parse arguments. Check for required flags.
	}
	
}
