/**
 * Georgia Tech
 * DISL
 * 2016
 */

import generator_lib.GeneratorOutput;
import generator_lib.GeneratorUtils;

import java.io.FileNotFoundException;

import core_lib.Globals;
import core_lib.Network;
import core_lib.Structure;
import core_lib.User;

/**
 * TraceGenerator is a driver program that generates trace files embodying 
 * network runs that can be used in the evaluation of trust management systems.
 */
public class TraceGenerator{

	// ************************** PRIVATE FIELDS *****************************

	/**
	 * The filename/path of the *.trace file to be written
	 */	
	private static String OUTPUT;

	// ************************** PUBLIC METHODS *****************************	
	
	/**
	 * The main driver method.
	 * @param args See the enclosed README document for usage information
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException{
		
			// Parse the parameters into a Global object
		final Globals GLOBALS = parse_arguments(args);
		
			// Open object to print to trace, and write header
		GeneratorOutput Trace = new GeneratorOutput(OUTPUT, GLOBALS);
		Trace.writeHeader();
		
			// Initialize the network object with passed parameters
		Network nw = new Network(GLOBALS);
			
			// Do the User initializations and print them to trace
		GeneratorUtils Generator = new GeneratorUtils(nw, GLOBALS);
		Generator.generateUsers();
		Trace.writeUsers(nw);
		
	  // Then, create and output initial libraries
		Generator.generateInitLibs();
	    Generator.generateInitLibs_malicious();
		Generator.generateInitLibs_good();
		Generator.checkgoodpeer_new();
		Trace.writeLibraries(nw);
		
		
		int sum_file=0;
		int valfile[], invalfile[];
		valfile=new int[GLOBALS.NUM_USERS];
		invalfile=new int[GLOBALS.NUM_USERS];
		int sum_valfile, sum_invalfile;
		for(int i=0;i<GLOBALS.NUM_USERS;i++){
			sum_valfile=0;
            sum_invalfile=0;
			for(int j=0;j<GLOBALS.NUM_FILES;j++){
				if(nw.hasFile(i, j)){
					if(nw.fileCopyValid(j, i))
						sum_valfile++;
					else
						sum_invalfile++;
				}
			}		
			valfile[i]=sum_valfile;
			invalfile[i]=sum_invalfile;
			
		}
				System.out.print("\n");
		for (int i=0;i<GLOBALS.NUM_USERS;i++){
			
			  System.out.println(i+"'files number= "+nw.getUser(i).getNumFiles()+"  valid_file number= "+valfile[i]+" invalid file number= "+invalfile[i]);
			 
			  sum_file+=nw.getUser(i).getNumFiles();
			  
			}
		System.out.println("sum_file.copy= "+sum_file);
		
		int  goodp_valfile[];
		goodp_valfile= new int[GLOBALS.NUM_USERS];
		int goodp_sum_valfile;
		for(int i=0;i<GLOBALS.NUM_FILES ;i++){
            goodp_sum_valfile=0;
			for(int j=0;j<GLOBALS.NUM_USERS;j++){
				if(nw.hasFile(j, i)){
					if(nw.fileCopyValid(i, j)){
					    if(nw.getUser(j).getModel()==User.Behavior.USR_GOOD){
					    	goodp_sum_valfile++;
					      }
					    }
					  }
				}
			//goodp_valfile[i]=goodp_sum_valfile;
			
		}
		/*
		  System.out.println( "\n\rfile ID from 0 to "+ (GLOBALS.NUM_USERS-1) + "'s file number owned by good peers are: \n\r");
		  for(int i=0;i<GLOBALS.NUM_FILES;i++){
			  System.out.print(goodp_valfile[i]+"  ");
		  }
		  System.out.print("\r\n");
		  */
		  int sum_user_copy=0;
			int valid_file=0, invalid_file=0;
			
	    for(int i=0;i<GLOBALS.NUM_USERS;i++){
	    	for(int j=0;j<nw.GLOBALS.NUM_FILES;j++){
	    		if(nw.hasFile(i, j)){
	    		   if(nw.fileCopyValid(j, i))
	    		    	valid_file++;
	    		  else
	    			    invalid_file++;	
	    		}
	    	}
	    	
	    }
	    
	    for(int i=0;i<nw.GLOBALS.NUM_FILES;i++){
	    	System.out.println(i+".copy= "+nw.fileOwners(i));
			sum_user_copy+=nw.fileOwners(i);
	    }
	    System.out.println("sum_file= "+ sum_user_copy);  
	    System.out.println("valid file = "+ valid_file+" , invalid file= "+invalid_file+"\n");
	   

	    Structure strct= new Structure (GLOBALS);
		strct.Linkrelation(nw);
				
		for(int i=0;i<GLOBALS.NUM_USERS;i++){
			for(int j=0;j<GLOBALS.NUM_USERS;j++){
				Trace.writematrix(i, j, strct.map);
			}
			
		}
				
		
		
		for(int i=0; i < (GLOBALS.NUM_TRANS + GLOBALS.WARMUP); i++){
			if(GLOBALS.SMART_GEN)
				Trace.writeTrans(Generator.genTransactionSmart(i));
			else
				Trace.writeTrans(Generator.genTransactionNaive());
		} // Generate and print transactions, per mode parameter
		
		Generator.printqueryfile(nw);
		Trace.generateemptyline();
		for(int i=0; i < (GLOBALS.NUM_TRANS + GLOBALS.WARMUP); i++){
			if(GLOBALS.SMART_GEN)
				Trace.writeTransagain(i);
			else
				Trace.writeTransagain(i);
		}
		
		Trace.generateemptyline();
		Trace.writefilepopular();
		Trace.generateemptyline();
		
		
		for (int i=0;i<GLOBALS.NUM_USERS;i++){
			Trace.writelinkstructure(i, strct.user_link);
		}
			
//		for (int i=0;i<GLOBALS.NUM_USERS;i++){
//			Trace.writelinkmatrix(i, strct.user_link);
//		}
		
//		strct.cal_reachratio();
		
		System.out.print("Transaction generation complete...\n");
		System.out.printf("Done! Output written to %s\n\n", OUTPUT);
		
	
	
		Trace.shutdown();
		return;
	}
	
	// ************************** PRIVATE METHODS ****************************

	/**
	 * Parse the command-line arguments provided to the main() method.
	 * @param args See the enclosed README document for usage information
	 * @return A Globals object wrapping parameter variables
	 */
	private static Globals parse_arguments(String[] args){
	//	 System.out.println("args.length= "+args.length);
		if(args.length % 2 == 1){
			System.out.print("\nInvalid # of arguments. Aborting.\n\n");
			System.exit(1);
		} // Check the number of arguments
		
			// Set default in case some arguments aren't provided
		int NUM_USERS = 103 ;
		int NUM_FILES = 20 ;
		int NUM_TRANS = 1500;																																																																	 ;
		double ZIPF = 0.4;   // old is 0.4, 1.3 when using large network size in spy model
		int PRE_TRUSTED = 10 ;
		int USR_FEED = 0; //LEAVE AS ZERO
		int USR_PURE = 40 ; //TYPE A , B
		int USR_PROV = 0; //LEAVE AS ZERO
		int USR_DISG = 0; // TYPE C, D
		int USR_SYBL = 0;
		int BAND_MAX = 7;
		int BAND_PER = 1;
		double F_PERCENTAGE = 0.4;
		int WARMUP = 500;
		boolean SMART_GEN = true;
		OUTPUT = "trace_0.trace";
		
	//	System.out.print("args.length="+args.length);
		for(int i=1; i < args.length; i+=2){
			if(args[i-1].equalsIgnoreCase("-users"))
				NUM_USERS = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-files"))
				NUM_FILES = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-trans"))
				NUM_TRANS = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-zipf"))
				ZIPF = Double.parseDouble(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:pre_trusted"))
				PRE_TRUSTED = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:purely"))
				USR_PURE = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:feedback"))
				USR_FEED = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:provider"))
				USR_PROV = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:disguise"))
				USR_DISG = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-f"))
				F_PERCENTAGE = Double.parseDouble(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:sybil"))
				USR_SYBL = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-band:max_conn"))
				BAND_MAX = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-band:period"))
				BAND_PER = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-mode:warmup"))
				WARMUP = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-mode:smartgen"))
				SMART_GEN = Boolean.parseBoolean(args[i]);
			else if(args[i-1].equalsIgnoreCase("-output"))
				OUTPUT = args[i];
			else{
				System.out.print("\nInvalid argument(s). Aborting.\n\n");
				System.exit(1);
			} // Catch any unsupported arguments		
		} // Parse all arguments
		
		int USR_GOOD = (NUM_USERS-USR_PURE-USR_FEED-USR_PROV-USR_DISG-USR_SYBL);
		if(USR_GOOD < 0){
			System.out.print("\nError: Number of malicious users > total " +
					"users. Aborting.\n\n");
			System.exit(1);
		} // Make sure user counts are legal
		
		if(PRE_TRUSTED > USR_GOOD){
			System.out.print("\nError: Number of pre-trusted users > good " +
					"users .Aborting.\n\n");
			System.exit(1);
		} // Make sure pre-trusted count is legal 
		
		if(!OUTPUT.endsWith(".trace")){
			System.out.print("\nError: Output file doesn't end in *.trace " +
					"extension. Aborting.\n\n");
			System.exit(1);
		} // Make sure output file extension is well formed
		
		return(new Globals(NUM_USERS, NUM_FILES, NUM_TRANS, ZIPF, 
				PRE_TRUSTED, USR_GOOD, USR_PURE, USR_FEED, USR_PROV, USR_DISG, 
				USR_SYBL, BAND_MAX, BAND_PER, WARMUP, SMART_GEN, F_PERCENTAGE));
	}
}
