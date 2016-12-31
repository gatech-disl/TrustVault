/**
 * Georgia Tech
 * DISL
 * 2016
 */
package simulator_lib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import core_lib. * ;
import simulator_lib.SimulatorMalicious.MAL_STRATEGY;
import simulator_lib.SimulatorInput;

/**
 * The SimulatorSource class assists the TraceSimulator driver program in
 * dynamic source selection based on user/file availability and trust values.
 */
public class SimulatorSource {

	// ************************** PUBLIC FIELDS ******************************

	/**
	 * The Strategy enumeration lists the source selection strategies.
	 */
	public enum Strategy {
		BEST,
		WORST,
		RAND
	};

	//	public static Structure st
	private static double f; // percentage of disguised malicious provide authentic files;

	//private static boolean randm= false;
	private static boolean randm;

	private static int hops = 8;

	private static boolean flage_good_peer_repu; // check weather all the good peers has positive reputation after accomplishing warming up, if so, no longer continue to perform more "warm up". 
	//	private static boolean flage_show_start_trans; // begin to count the failed/successful transactions;

	private static List < Integer > [] Malineighbor;
	private static boolean[] cntrl_Malsend_flage;

	SimulatorUtils simulatorU = new SimulatorUtils();

	// ************************** PUBLIC METHODS *****************************

	public SimulatorSource(Network nw) {
		f = nw.GLOBALS.F_PERCENTAGE;
		System.out.printf("F_PERC in SimSource = %f\n", f);
		flage_good_peer_repu = true;
		//		flage_show_start_trans=true;
		List < Integer > collection = new ArrayList < Integer > ();
		for (int i = 0; i < nw.GLOBALS.NUM_USERS; i++) {
			if (nw.getUser(i).getModel() != User.Behavior.USR_GOOD) {
				collection.add(i);
			}
		}

		Malineighbor = new List[collection.size()];
		for (int j = 0; j < collection.size(); j++) {
			Malineighbor[j] = new ArrayList < Integer > ();
		}

		for (int i = 0; i < collection.size(); i++) {
			if (i != collection.size() - 1) Malineighbor[i].add(collection.get(i + 1));
			else Malineighbor[i].add(0);
		}

		//		for(int j=0;j<collection.size();j++){
		//			for(int k=0;k<Malineighbor[j].size();k++){
		//				System.out.println(Malineighbor[j].get(k));
		//			}
		//		}

		cntrl_Malsend_flage = new boolean[collection.size()];
		for (int i = 0; i < collection.size(); i++) {
			cntrl_Malsend_flage[i] = false;
		}

	}

	/**
	 * Given a user Behavior, return the source Strategy they should follow.
	 * @param model The behavior model of some User
	 * @return The source Strategy that user should apply
	 */
	public void randmvalue(boolean bool) {
		randm = bool;
		System.out.println("randm= " + bool);

	}

	//	public void getMalipartner(Network nw){
	//		Malineighbor =new List[nw.GLOBALS.NUM_USERS];
	//		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
	//			Malineighbor[i].add(simulatorU.getmalipartner(i));
	//		}
	//	}

	public static Strategy pickStrategy(User.Behavior model) {
		if (model == User.Behavior.USR_GOOD) return Strategy.BEST;
		else if (model == User.Behavior.USR_PURE)
		//return Strategy.WORST;
		return Strategy.BEST;
		else if (model == User.Behavior.USR_FEED) return Strategy.RAND;
		else if (model == User.Behavior.USR_PROV) return Strategy.WORST;
		else if (model == User.Behavior.USR_DISG)
		//	return Strategy.RAND;
		return Strategy.BEST;
		else // if(model == User.Behavior.USR_SYBL)
		return Strategy.WORST;
	}

	/**
	 * Decide the source for a transaction, given current Network status.
	 * @param nw Network in which the transaction will take place
	 * @param cycle The current cycle
	 * @param recv Identifier of the user requesting the file
	 * @param file Identifier of the file being requested
	 * @param strategy Source selection Strategy being employed
	 * @return Identifier of source user, or -1 if no source exists
	 */
	public static int pickSource(Network nw, int cycle, int recv, int file, Strategy strategy, Structure strct, MAL_STRATEGY STRATEGY, SimulatorInput simulaInput) {
		if (strategy == Strategy.BEST) return (sourceBest(nw, cycle, recv, file, strct, STRATEGY, simulaInput));
		else if (strategy == Strategy.WORST) return (sourceWorst(nw, cycle, recv, file, strct, STRATEGY));
		else // if(strategy == Strategy.RAND)
		return (sourceRandom(nw, cycle, recv, file, strct, STRATEGY));
	}

	// ************************** PRIVATE METHODS ****************************

	/**
	 * Choose the best (most trusted) available source for a transaction.
	 * @param nw Network in which transaction will take place
	 * @param cycle The current cycle
	 * @param recv Identifier of the user requesting the file
	 * @param file Identifier of the file being requested
	 * @return Identifier of source user, or -1 if no source exists
	 */
	public static int sourceBest(Network nw, int cycle, int recv, int file, Structure strct, MAL_STRATEGY STRATEGY, SimulatorInput simulaInput) {
		//	double max_trust = Double.NEGATIVE_INFINITY;
		int pos_sources = 0;
		int[] scratch_vector = new int[nw.GLOBALS.NUM_USERS];
		double[] send_trust = new double[nw.GLOBALS.NUM_USERS];
		//double f = 0.0 ; // percentage of disguised malicious provide authentic files;

		boolean cond1,
		cond2;
		/*for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			cond1 = nw.hasFile(i, file);
			cond2 = nw.getUser(i).BWidthAvailableUL(cycle);
			if(!cond1 || !cond2)
				scratch_vector[i] = Double.NEGATIVE_INFINITY;
			*/
		if (popular(nw, file, 0.05)) {
			// System.out.println("filepopularity = [0, 0.05]");
			for (int i = 0; i < nw.GLOBALS.NUM_USERS; i++) {
				cond1 = nw.hasFile(i, file);
				cond2 = nw.getUser(i).BWidthAvailableUL(cycle);
				if (!cond1 || !cond2) send_trust[i] = Double.NEGATIVE_INFINITY;

				else if (recv != i) { // && strct.pathlength(recv,i, nw)<hops){
					scratch_vector[pos_sources++] = i;
					send_trust[i] = nw.getUserRelation(recv, i).getTrust();
					//pos_sources = 1;
					//max_trust = nw.getUserRelation(recv, i).getTrust();
					//scratch_vector[i] = max_trust;
				}
				else send_trust[i] = nw.getUserRelation(recv, i).getTrust();
			}
		}
		if (popular(nw, file, 1) && !(popular(nw, file, 0.05))) {
			// System.out.println("filepopularity = [0.05, 1]");
			for (int i = 0; i < nw.GLOBALS.NUM_USERS; i++) {
				cond1 = nw.hasFile(i, file);
				cond2 = nw.getUser(i).BWidthAvailableUL(cycle);
				if (!cond1 || !cond2) send_trust[i] = Double.NEGATIVE_INFINITY;
				else if (! (nw.getUser(i).isPreTrusted()) && recv != i) { //  && strct.pathlength(recv,i, nw)<hops ){
					scratch_vector[pos_sources++] = i;
					send_trust[i] = nw.getUserRelation(recv, i).getTrust();
				}
				else send_trust[i] = nw.getUserRelation(recv, i).getTrust();
			}
		}
		/* 
			if(popular(nw, file, 1)&&!(popular(nw, file, 0.2))&&!(popular(nw, file, 0.05))){
				for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
					cond1 = nw.hasFile(i, file);
					cond2 = nw.getUser(i).BWidthAvailableUL(cycle);
					if(!cond1 ||! cond2)
						send_trust[i] = Double.NEGATIVE_INFINITY;	
					
					else if((!nw.getUser(i).isPreTrusted())&& (nw.getUser(i).getModel()==User.Behavior.USR_GOOD)			
						&&(strct.pathlength(recv,i, nw)<hops)){
						//System.out.println("hops= "+hops);
					    scratch_vector[pos_sources++]=i;
					   // send_trust[i]=nw.getUserRelation(recv, i).getTrust();
				}															
			}
	       }*/

		//} else if((nw.getUserRelation(recv, i).getTrust() == max_trust)&&(strct.pathlength(recv,i)<8)){
		//pos_sources++;
		//scratch_vector[i] = max_trust;
		//} else
		//	scratch_vector[i] = Double.NEGATIVE_INFINITY;
		//	} // Count quantity of peers with maximum trust value

		if (pos_sources == 0) return - 1; // If no sources available, report that fact	
		//  System.out.println("pos_sources= "+pos_sources+" "+"scrathc_vector. length= "+scratch_vector.length);
		// System.out.println("scrathc_vector. length= "+scratch_vector.length);

		/*int source_num = (int)((nw.GLOBALS.RAND.nextDouble()*pos_sources)+1.0);
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(scratch_vector[i] == max_trust)
				source_num--;
			if(source_num == 0)
				return i; // Break loop once we have randomly selected provider 
		} // Iterate over all peers
		return -1
		*/

		/*
		for(int i=0;i<scratch_vector.length;i++){
		 System.out.print("scratch_vector["+i+"]= "+scratch_vector[i]+" ");	
		}
		
		System.out.println("cycle(SimulatorSource) = "+ cycle);
		System.out.println("pos_sources= "+pos_sources);
		System.out.println("recver= "+recv+" file= "+file);
		System.out.println("file= "+file);		
	    System.out.print("response peers are: ");
		*/

		int[] scratch_new = new int[pos_sources];
		for (int i = 0; i < pos_sources; i++) {
			scratch_new[i] = scratch_vector[i];
			//  System.out.print(" scratch_new["+i+"]= "+ scratch_new[i]+" "+ " distance ="+ strct.pathlength(recv, scratch_new[i], nw)+ " scratch_new["+i+"].trust= "+nw.getUserRelation(recv, scratch_new[i]).getTrust());

		}

		if (nw.getUser(recv).getModel() != User.Behavior.USR_GOOD && cntrl_Malsend_flage[recv] == false && cycle > nw.GLOBALS.WARMUP) {
			if (STRATEGY == MAL_STRATEGY.COLLECTIVE) {
				for (int i = 0; i < pos_sources; i++) {
					if (Malineighbor[recv].contains(scratch_new[i])) {
						cntrl_Malsend_flage[recv] = true;
						return scratch_new[i];
					}

				}
			}
			if (STRATEGY == MAL_STRATEGY.DISGUISE) {
				for (int i = 0; i < pos_sources; i++) {
					if (Malineighbor[recv].contains(scratch_new[i])) {
						cntrl_Malsend_flage[recv] = true;
						//		System.out.println("recv= "+ recv+", Malipartner= " +scratch_new[i]);
						return scratch_new[i];
					}

				}
			}
			if (STRATEGY == MAL_STRATEGY.SPY) {
				if (nw.getUser(recv).getModel() == User.Behavior.USR_DISG) {
					for (int i = 0; i < pos_sources; i++) {
						if (nw.getUser(scratch_new[i]).getModel() == User.Behavior.USR_PURE) {
							cntrl_Malsend_flage[recv] = true;
							return scratch_new[i];
						}
					}
				}
			}
		}

		//  System.out.println(" number of response peers= "+scratch_new.length);

		if (randm == true) {
			double randm;
			int sour; //=nw.GLOBALS.RAND.nextInt(scratch_new.length);
			int source = nw.GLOBALS.RAND.nextInt(pos_sources);
			int sender = scratch_new[source];

			boolean flag = false;
			/*
				 if(nw.getUser(sender).getModel()==User.Behavior.USR_GOOD){
	    			 randm=nw.GLOBALS.RAND.nextDouble();
	    			 if(randm<0.05){
	    				 flag=true;
	    			 }
			     }
			  */
			if (STRATEGY == MAL_STRATEGY.DISGUISE) {
				if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
					randm = nw.GLOBALS.RAND.nextDouble();
					if (randm >= f) {
						flag = true;
					}
				}
			}
			/*
	    		 if(STRATEGY==MAL_STRATEGY.SPY){
	    			 if(nw.getUser(sender).getModel()==User.Behavior.USR_DISG){  				
	    				 if( simulaInput.filepopularacc[file]<0.05){
	    				     flag=true;
	    				 } 
	    			 }
	    		 }
               */
			while ((!nw.fileCopyValid(file, sender) || flag) && scratch_new.length > 1) {
				flag = false;
				if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) {
					nw.STATS.NUM_INVAL_TRANS++;
					nw.STATS.NUM_GOOD_FAIL++;
					//   System.out.println("cut@@@@"); 
				}
				//   System.out.println("randm_sender= "+sender);
				//   System.out.println("invalid file_randm!" + "nw.STATS.NUM_INVAL_TRANS(0)= "+nw.STATS.NUM_INVAL_TRANS); 
				scratch_new = delete(sender, scratch_new);
				nw.getUser(sender).incuploadcount();

				sour = nw.GLOBALS.RAND.nextInt(scratch_new.length);
				sender = scratch_new[sour];
				/*
				    	   if(nw.getUser(sender).getModel()==User.Behavior.USR_GOOD){
				    			 randm=nw.GLOBALS.RAND.nextDouble();
				    			 if(randm<0.05){
				    				 flag=true;
				    			 }
						     }
						  */
				if (STRATEGY == MAL_STRATEGY.DISGUISE) {
					if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
						randm = nw.GLOBALS.RAND.nextDouble();
						if (randm >= f) {
							flag = true;
						}
					}
				}
			}
			if (STRATEGY == MAL_STRATEGY.ISOLATED) {
				if (nw.getUser(sender).getModel() != User.Behavior.USR_GOOD && nw.fileCopyValid(file, sender)) {
					System.out.println("cut point 2!");
					return - 1;
					//nw.STATS.NUM_validfile_maliciousprovide++;
					//if (nw.getUser(recv).getModel()==User.Behavior.USR_GOOD)
					//    nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
				}
				if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
					if (nw.GLOBALS.RAND.nextDouble() < 0.05) return - 1;
					else return sender;
				}
			}

			if (STRATEGY == MAL_STRATEGY.COLLECTIVE) {
				if (nw.getUser(sender).getModel() != User.Behavior.USR_GOOD && nw.fileCopyValid(file, sender)) {
					nw.STATS.NUM_validfile_maliciousprovide++;
					if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
				}

				if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
					if (nw.GLOBALS.RAND.nextDouble() < 0.05) return - 1;
					else return sender;
				}

			}

			if (STRATEGY == MAL_STRATEGY.DISGUISE) {
				if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
					if (flag == true) {
						return - 1;
					}
					else {
						//  if(nw.GLOBALS.RAND.nextDouble()<f){
						if (nw.fileCopyValid(file, sender)) {
							nw.STATS.NUM_validfile_maliciousprovide++;
							if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
						}
						else {
							//System.out.println("disguise test 222!!");
							return - 1;
						}
					}
				}

				if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
					if (nw.GLOBALS.RAND.nextDouble() < 0.05) {
						return - 1;
					}
					else {
						return sender;
					}
					//	if(nw.GLOBALS.RAND.nextDouble()<0.05)
					//	return -1;
				}

			}
			if (STRATEGY == MAL_STRATEGY.SPY) {
				if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
					// System.out.println("filepopular= "+simulaInput.filepopularacc[file]);
					if (simulaInput.filepopularacc[file] >= 0.05) {
						if (nw.fileCopyValid(file, sender)) {
							nw.STATS.NUM_validfile_maliciousprovide++;
							if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
							// }
							//System.out.println("SPY_STATS_2");
						}
						else {
							System.out.println("SPY test2!!!!");
							return - 1;
						}
					} else {
						return - 1;
					}
				}

				if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
					// if(flag1==true){
					if (nw.GLOBALS.RAND.nextDouble() < 0.05) {
						return - 1;
					}
					else {
						return sender;
					}
					//	if(nw.GLOBALS.RAND.nextDouble()<0.05)
					//	return -1;
				}
			}
			//System.out.println("random_sender= "+scratch_vector[source]);

			return sender;

		}

		else if (randm == false) {
			double randm1;
			boolean flag1;
			int sender = scratch_new[0];
			//double tmp0=nw.GLOBALS.RAND.nextDouble();
			int tmp = 0;
			int temp;
			int randm2 = 1 + nw.GLOBALS.RAND.nextInt(10);

			/*
	    	 * response peers with reputation=0.0 can be selected via probability [0%-10%]
	    	 */

			if (nw.GLOBALS.RAND.nextDouble() <= (double) randm2 / (double) 100) {
				//System.out.println("\nwhen cycle= " + cycle + " recv:" + recv + ", sender selected 0%-10% random");
				//if(nw.GLOBALS.RAND.nextDouble()<0.1){
				//  System.out.println("(double)randm2/(double)100= "+(double)randm2/(double)100);
				for (int i = 0; i < pos_sources; i++) {
					if (nw.getUserRelation(recv, scratch_new[i]).getTrust() == 0) {
						tmp++;
					}
				}
				if (tmp > 0) {
					do {
						temp = nw.GLOBALS.RAND.nextInt(pos_sources);
					} while ( nw . getUserRelation ( recv , scratch_new [ temp ]).getTrust() != 0);

					sender = scratch_new[temp];
					if (STRATEGY == MAL_STRATEGY.ISOLATED) {
						// System.out.println("cut point 1!");
						if (nw.getUser(sender).getModel() != User.Behavior.USR_GOOD && nw.fileCopyValid(file, sender)) {
							//nw.STATS.NUM_INVAL_TRANS++;
							//continue;
							return - 1;
							// nw.STATS.NUM_validfile_maliciousprovide++;
							//if(nw.getUser(recv).getModel()==User.Behavior.USR_GOOD)
							//  nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
						}

						if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
							//	randm1=1+nw.GLOBALS.RAND.nextInt(5);
							//	System.out.println("nw.GLOBALS.RAND.nextInt(5)= "+nw.GLOBALS.RAND.nextInt(5));
							if (nw.GLOBALS.RAND.nextDouble() < 0.05) return - 1;
						}
					}
					if (STRATEGY == MAL_STRATEGY.COLLECTIVE) {
						if (nw.getUser(sender).getModel() != User.Behavior.USR_GOOD && nw.fileCopyValid(file, sender)) {
							nw.STATS.NUM_validfile_maliciousprovide++;
							if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
						}
						if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
							if (nw.GLOBALS.RAND.nextDouble() < 0.05) return - 1;
						}
					}
					if (STRATEGY == MAL_STRATEGY.DISGUISE) {
						if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
							// double tmp=nw.GLOBALS.RAND.nextDouble();
							if (nw.GLOBALS.RAND.nextDouble() < f) {
								if (nw.fileCopyValid(file, sender)) {
									nw.STATS.NUM_validfile_maliciousprovide++;
									if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
								}
								// System.out.println("DISGUISE_STATS_1");
							}
							else {
								//  System.out.println("randm.-1");
								return - 1;
							}
						}

						if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
							if (nw.GLOBALS.RAND.nextDouble() < 0.05) return - 1;
						}

					}
					if (STRATEGY == MAL_STRATEGY.SPY) {
						if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
							if (simulaInput.filepopularacc[file] >= 0.05) {
								if (nw.fileCopyValid(file, sender)) {
									nw.STATS.NUM_validfile_maliciousprovide++;
									if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
									//System.out.println("SPY_STATS_1");
								}
								else {
									return - 1;
								}
							} else {
								return - 1;
							}
						}

						if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
							if (nw.GLOBALS.RAND.nextDouble() < 0.05) return - 1;
						}

					}
					System.out.println("randm_sender= " + sender);
					return sender;
				}
				//	System.out.println("select new peer as sender!");	 
				// }
			}

			//////////////////response peers with reputation=0.0 can be selected via probability [0%-10%]-End/////////////////  	

			// counting the failed/succussful transaction after warming up    	    	  
			flag1 = false;
			sender = maxelement(nw, scratch_new, recv);
			//System.out.println("\nwhen cycle= " + cycle + " recv:" + recv + ", sender: " + sender + ".reputation= " + nw.getUserRelation(recv, sender).getTrust());
			/*
		    		if(nw.getUser(sender).getModel()==User.Behavior.USR_GOOD){
		    			 randm1=nw.GLOBALS.RAND.nextDouble();
		    			 if(randm1<0.05){
		    				 flag1=true;
		    				 System.out.println("randm1<0.05");
		    			 }
				     }
				  */

			if (STRATEGY == MAL_STRATEGY.DISGUISE) {
				if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
					randm1 = nw.GLOBALS.RAND.nextDouble();
					if (randm1 >= f) {
						flag1 = true;
						// System.out.println("randm1>=f");
					}
				}
			}

			if (STRATEGY == MAL_STRATEGY.SPY) {
				if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
					//	 System.out.println("filepopular1= "+simulaInput.filepopularacc[file]);
					if (simulaInput.filepopularacc[file] < 0.05) {
						//   System.out.println("filepopular2= "+simulaInput.filepopularacc[file]);
						flag1 = true;
					}
				}
			}

			while ((flag1 || !nw.fileCopyValid(file, sender)) && scratch_new.length > 1) {
				flag1 = false;
				if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) {
					nw.STATS.NUM_INVAL_TRANS++;
					nw.STATS.NUM_GOOD_FAIL++;
				}
				Transaction ttx = new Transaction(cycle, sender, recv, file, nw.fileCopyValid(file, sender));
				nw.getUser(sender).incuploadcount();

				//System.out.print("sender_while= " + sender);
				//System.out.println(" @Invalid File!" + "nw.STATS.NUM_INVAL_TRANS(0)= " + nw.STATS.NUM_INVAL_TRANS);
				invalidfile_feedback(nw, sender, recv, STRATEGY);
				nw.ALG.update(ttx);
				scratch_new = delete(sender, scratch_new);
				sender = maxelement(nw, scratch_new, recv);
				//System.out.println("maxelement= " + sender + ".reputation=" + nw.getUserRelation(recv, sender).getTrust());
				/*
		    	   if(nw.getUser(sender).getModel()==User.Behavior.USR_GOOD){
		    			 randm1=nw.GLOBALS.RAND.nextDouble();
		    			 if(randm1<0.05){
		    				 flag1=true;
		    				 System.out.println("randm1<0.05");
		    			 }
				     }
					*/

				if (STRATEGY == MAL_STRATEGY.DISGUISE) {
					if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
						randm1 = nw.GLOBALS.RAND.nextDouble();
						if (randm1 >= f) {
							flag1 = true;
						}
					}
				}

				if (STRATEGY == MAL_STRATEGY.SPY) {
					if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
						//System.out.println("filepopular1= "+simulaInput.filepopularacc[file]);
						if (simulaInput.filepopularacc[file] < 0.05) {
							//System.out.println("filepopular2= "+simulaInput.filepopularacc[file]);
							flag1 = true;
						}
					}
				}

			}

			//System.out.println("final_sender= " + sender + ", flag1= " + flag1);
			if (STRATEGY == MAL_STRATEGY.ISOLATED) {
				if (nw.getUser(sender).getModel() != User.Behavior.USR_GOOD) {
					if (nw.fileCopyValid(file, sender)) {
						return - 1;
					} else {
						//  System.out.println("sender!!= "+ sender);  
						return sender;
					}
				}

				if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
					if (nw.GLOBALS.RAND.nextDouble() < 0.05) {
						return - 1;
					}
					else {
						//	 System.out.println("sender!!= "+ sender);
						return sender;
					}
				}

			}
			if (STRATEGY == MAL_STRATEGY.COLLECTIVE) {
				if (nw.getUser(sender).getModel() != User.Behavior.USR_GOOD) {
					if (nw.fileCopyValid(file, sender)) {
						nw.STATS.NUM_validfile_maliciousprovide++;
						if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
					}
				}
				if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
					if (nw.GLOBALS.RAND.nextDouble() < 0.05) {
						return - 1;
					}
					else {
						return sender;
					}
				}
			}
			if (STRATEGY == MAL_STRATEGY.DISGUISE) {
				//System.out.println("in disguise if after final sender!!");
				if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
					if (flag1 == true) {
						return - 1;
					}
					else {
						if (nw.fileCopyValid(file, sender)) {
							nw.STATS.NUM_validfile_maliciousprovide++;
							if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
						}
						else {
							return - 1;
						}
					}
				}

				if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
					if (nw.GLOBALS.RAND.nextDouble() < 0.05) {
						return - 1;
					}
					else {
						return sender;
					}
				}

			}
			if (STRATEGY == MAL_STRATEGY.SPY) {
				if (nw.getUser(sender).getModel() == User.Behavior.USR_DISG) {
					if (simulaInput.filepopularacc[file] >= 0.05) {
						if (nw.fileCopyValid(file, sender)) {
							nw.STATS.NUM_validfile_maliciousprovide++;
							if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
							// }
							//System.out.println("SPY_STATS_2");
						}
						else {
							// System.out.println("SPY test2!!!!");
							return - 1;
						}
					}
					else {
						return - 1;
					}
				}

				if (nw.getUser(sender).getModel() == User.Behavior.USR_GOOD) {
					// if(flag1==true){
					if (nw.GLOBALS.RAND.nextDouble() < 0.05) {
						return - 1;
					}
					else {
						return sender;
					}
					//	if(nw.GLOBALS.RAND.nextDouble()<0.05)
					//	return -1;
				}

			}
			//System.out.println("returning sender!");
			//System.out.println("sender= "+sender);
			return sender;
		}
		else {
			System.out.println("UNUSED USED!!!");
			return - 1; // Unused
		}
	}

	/**
	 * Choose the worse (least trusted) available source for a transaction.
	 * @param nw Network in which transaction will take place
	 * @param cycle The current cycle
	 * @param recv Identifier of the user requesting the file
	 * @param file Identifier of the file being requested
	 * @return Identifier of source user, or -1 if no source exists
	 */
	private static int sourceWorst(Network nw, int cycle, int recv, int file, Structure strct, MAL_STRATEGY STRATEGY) {
		double min_trust = Double.POSITIVE_INFINITY;
		int pos_sources = 0;
		double[] scratch_vector = new double[nw.GLOBALS.NUM_USERS];
		double[] send_trust = new double[nw.GLOBALS.NUM_USERS];

		boolean cond1,
		cond2;
		for (int i = 0; i < nw.GLOBALS.NUM_USERS; i++) {
			cond1 = nw.hasFile(i, file);
			cond2 = nw.getUser(i).BWidthAvailableUL(cycle);
			if (!cond1 || !cond2) send_trust[i] = Double.POSITIVE_INFINITY;
			//scratch_vector[i] = Double.POSITIVE_INFINITY;
			else if ((nw.getUserRelation(recv, i).getTrust() < min_trust) && (strct.pathlength(recv, i, nw) < hops)) {

				pos_sources = 0;
				min_trust = nw.getUserRelation(recv, i).getTrust();
				send_trust[i] = min_trust;
				scratch_vector[pos_sources++] = i;
			} else if ((nw.getUserRelation(recv, i).getTrust() == min_trust) && (strct.pathlength(recv, i, nw) < hops)) {
				//pos_sources++;
				scratch_vector[pos_sources] = i;
				send_trust[i] = min_trust;
			} else send_trust[i] = Double.POSITIVE_INFINITY;
		} // Count quantity of peers with maximum trust value

		if (pos_sources == 0) return - 1; // If no sources available, report that fact

		int source_num = nw.GLOBALS.RAND.nextInt(pos_sources);
		//for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
		//	if(scratch_vector[i] == min_trust)
		//	source_num--;
		//	if(source_num == 0){
		if (STRATEGY == MAL_STRATEGY.ISOLATED) {
			System.out.println("cut point 3!");
			if (nw.getUser(source_num).getModel() != User.Behavior.USR_GOOD && nw.fileCopyValid(file, source_num)) {
				return - 1;
			}

			if (nw.getUser(source_num).getModel() == User.Behavior.USR_GOOD) {
				if (nw.GLOBALS.RAND.nextDouble() <= 0.05) return - 1;
			}

		}
		if (STRATEGY == MAL_STRATEGY.COLLECTIVE) {
			if (nw.getUser(source_num).getModel() != User.Behavior.USR_GOOD && nw.fileCopyValid(file, source_num)) {
				nw.STATS.NUM_validfile_maliciousprovide++;
				if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
			}
			if (nw.getUser(source_num).getModel() == User.Behavior.USR_GOOD) {
				if (nw.GLOBALS.RAND.nextDouble() <= 0.05) return - 1;
			}
		}
		if (STRATEGY == MAL_STRATEGY.DISGUISE) {
			if (nw.getUser(source_num).getModel() == User.Behavior.USR_DISG) {
				if (nw.GLOBALS.RAND.nextDouble() <= f) {
					if (nw.fileCopyValid(file, source_num)) {
						System.out.println(" DISGUISE 2");
						nw.STATS.NUM_validfile_maliciousprovide++;
						if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
					}
				}
				else return - 1;

			}
			if (nw.getUser(source_num).getModel() == User.Behavior.USR_GOOD) {
				if (nw.GLOBALS.RAND.nextDouble() <= 0.05) return - 1;
			}
		}
		if (STRATEGY == MAL_STRATEGY.SPY) {
			if (nw.getUser(source_num).getModel() == User.Behavior.USR_DISG && popular(nw, file, 0.05)) {
				if (nw.fileCopyValid(file, source_num)) {
					// if(nw.fileCopyValid(file, i)){
					nw.STATS.NUM_validfile_maliciousprovide++;
					if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
					// }
					//System.out.println("SPY_STATS_2");
				}
				else return - 1;
			}
			if (nw.getUser(source_num).getModel() == User.Behavior.USR_GOOD) {
				if (nw.GLOBALS.RAND.nextDouble() <= 0.05) return - 1;
			}
		}

		return source_num; // Break loop once we have randomly selected provider 
		//}
		//} // Iterate over all peers
		//return -1; // Unused
	}

	/**
	 * Choose a random available source for a transaction.
	 * @param nw Network in which transaction will take place
	 * @param cycle The current cycle
	 * @param recv Identifier of the user requesting the file
	 * @param file Identifier of the file being requested
	 * @return Identifier of source user, or -1 if no source exists
	 */
	private static int sourceRandom(Network nw, int cycle, int recv, int file, Structure strct, MAL_STRATEGY STRATEGY) {
		int pos_sources = 0;
		double[] scratch_vector = new double[nw.GLOBALS.NUM_USERS];

		boolean cond1,
		cond2,
		cond3;
		for (int i = 0; i < nw.GLOBALS.NUM_USERS; i++) {
			cond1 = nw.hasFile(i, file);
			cond2 = nw.getUser(i).BWidthAvailableUL(cycle);
			cond3 = strct.pathlength(recv, i, nw) < hops;
			if (cond1 && cond2 && cond3) {
				pos_sources++;
				scratch_vector[i] = Double.POSITIVE_INFINITY;
			} else scratch_vector[i] = Double.NEGATIVE_INFINITY;
		} // Count quantity of potential sources

		if (pos_sources == 0) return - 1; // If no sources exist, this should be noted

		int source_num = (int)((nw.GLOBALS.RAND.nextDouble() * pos_sources) + 1.0);
		for (int i = 0; i < nw.GLOBALS.NUM_USERS; i++) {
			if (scratch_vector[i] == Double.POSITIVE_INFINITY) source_num--;
			if (source_num == 0) {
				if (STRATEGY == MAL_STRATEGY.ISOLATED) {
					if (nw.getUser(i).getModel() != User.Behavior.USR_GOOD && nw.fileCopyValid(file, i)) return - 1;
				}
				if (STRATEGY == MAL_STRATEGY.COLLECTIVE) {
					if (nw.getUser(i).getModel() != User.Behavior.USR_GOOD && nw.fileCopyValid(file, i)) {
						nw.STATS.NUM_validfile_maliciousprovide++;
						if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
					}
				}
				if (STRATEGY == MAL_STRATEGY.DISGUISE) {
					if (nw.getUser(i).getModel() == User.Behavior.USR_DISG) {
						if (nw.GLOBALS.RAND.nextDouble() <= f) {
							if (nw.fileCopyValid(file, i)) {
								System.out.println(" DISGUISE 3");
								nw.STATS.NUM_validfile_maliciousprovide++;
								if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
							}
						}
						else return - 1;

					}

				}
				if (STRATEGY == MAL_STRATEGY.SPY) {
					if (nw.getUser(i).getModel() == User.Behavior.USR_DISG && popular(nw, file, 0.05)) {
						if (nw.fileCopyValid(file, i)) {
							// if(nw.fileCopyValid(file, i)){
							nw.STATS.NUM_validfile_maliciousprovide++;
							if (nw.getUser(recv).getModel() == User.Behavior.USR_GOOD) nw.STATS.Good_recv_NUM_validfile_maliciousprovide++;
							//}
							//System.out.println("SPY_STATS_2");
						}
						else return - 1;
					}
				}
				return i; // Break loop once we have randomly selected provider 
			}
		} // Iterate over all peers
		return - 1; // Unused	
	}

	private static boolean popular(Network nw, int file, double top_percent) {
		for (int i = 0; i < nw.GLOBALS.NUM_FILES * top_percent; i++) {
			if (file == i) {
				//System.out.println("popularity= "+i);
				return true;
			}
		}
		return false;
	}

	private static int maxelement(Network nw, int[] a, int recv) {
		/*
		System.out.println("the response list is: ");
		for (int i = 0; i < a.length; i++) {
			System.out.print(a[i] + " ");
		}
		System.out.println();
		*/
		int random = -1,
		element = -1;
		random = nw.GLOBALS.RAND.nextInt(a.length); //select another response peer randomly
		element = a[random];
		LinkedList < Integer > lst = new LinkedList < Integer > ();
		double sum_trust = 0;
		//double[] prob=new double[a.length];
		double[] trust = new double[a.length];

		/* 
		 * probabilistic selection
		 */

		for (int i = 0; i < a.length; i++) {
			sum_trust += nw.getUserRelation(recv, a[i]).getTrust();
		}

		for (int j = 0; j < a.length; j++) {
			if (sum_trust > 0) trust[j] = nw.getUserRelation(recv, a[j]).getTrust() / sum_trust;
		}

		double t;
		for (int i = 1; i <= a.length - 1; i++) {
			for (int j = 0; j < a.length - i; j++) {
				if (trust[j] > trust[j + 1]) {
					t = trust[j];
					trust[j] = trust[j + 1];
					trust[j + 1] = t;
				}
			}
		}

		//for(int i=0;i<trust.length;i++)
		//	System.out.print("trust["+i+"]= "+trust[i]+" ");
		//System.out.print("\n");

		//	for(int i=0;i<a.length;i++)
		//	System.out.print("a["+i+"]= "+a[i]+" ");
		//System.out.print("\n");

		//
		//  sorting based on the increasing order
		//

		if (sum_trust > 0.0) {
			for (int i = 0; i < trust.length; i++) {
				for (int j = 0; j < a.length; j++) {
					if (nw.getUserRelation(recv, a[j]).getTrust() / sum_trust == trust[i]) {
						if (!lst.contains(a[j])) lst.add(a[j]);
						else continue;
					}
				}
			}
		} else {
			for (int j = 0; j < a.length; j++) {
				lst.add(a[j]);
			}
		}
		double t1 = 0.0,
		t2 = 0.0;
		double tmp = nw.GLOBALS.RAND.nextDouble();

		for (int k = 0; k < trust.length; k++) {
			if (k == 0) {
				t1 = 0.0;
				t2 = trust[0];
			} else {
				t1 = t2;
				t2 = t1 + trust[k];
			}
			if (tmp > t1 && tmp <= t2) {
				element = (int) lst.get(k);
				break;
			}

		}

		lst.clear();
		//		System.out.println("element= "+element);

		return element;

		/*
		 * deterministic selection with highest reputation
		 */
		/*	
		double max_trust=nw.getUserRelation(recv, a[0]).getTrust();
		for(int i=a.length-1;i>0;i--){
			if(nw.getUserRelation(recv, a[i]).getTrust()>max_trust){
				max_trust=nw.getUserRelation(recv, a[i]).getTrust();
				element=a[i];
			}
			
		}
		return element;
	*/

	}
	/*
	private static int maxelement_warmup(Network nw, int[] a, int recv) {
		int element = a[nw.GLOBALS.RAND.nextInt(a.length)];
		double trustvalue = 0;
		//double max_trust=nw.getUserRelation(recv, a[0]).getTrust();
		for (int i = a.length - 1; i >= 0; i--) {
			if (nw.getUserRelation(recv, a[i]).getTrust() == trustvalue && (nw.getUser(a[i]).getModel() == User.Behavior.USR_GOOD)) {
				element = a[i];
			}

		}
		//System.out.println("max_trust= "+max_trust);
		return element;

	}*/

	private static boolean good_peer_positive_reputation(Network nw, int recv, int cycl) {
		// if(check_good_peer){
		for (int i = 0; i < nw.GLOBALS.NUM_USERS; i++) {
			if (nw.getUser(i).getModel() == User.Behavior.USR_GOOD) {
				if (nw.getUserRelation(recv, i).getTrust() == 0.0) {
					//					  System.out.println(i+".reputation= "+nw.getUserRelation(recv, i).getTrust()+", return false, cycle= "+ cycl);
					flage_good_peer_repu = true;
					//					System.out.println("flage_good_peer_repu 1= "+flage_good_peer_repu);
					return false;
				}
			}
		}
		//	 }
		flage_good_peer_repu = false;
		//		System.out.println("flage_good_peer_repu 2= "+flage_good_peer_repu);
		System.out.println("\r\nBegin to count the failed/successful transactions when the cycle= " + cycl + ", and the warm up transaction number=" + nw.GLOBALS.WARMUP + ", the set transaction number=" + nw.GLOBALS.NUM_TRANS);
		return true;
	}

	private static int[] delete(int sender, int[] a) {
		int[] b = new int[a.length - 1];
		int m = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == sender) {
				//System.out.println("delete node "+sender);
				continue;
			}
			b[m++] = a[i];
		}
		a = b;
		return a;
	}

	/**
	 * 
	 */
	private static void invalidfile_feedback(Network nw, int send, int recv, MAL_STRATEGY strategy) {

		boolean a = nw.getUser(send).getModel() == User.Behavior.USR_SYBL;
		boolean b = nw.getUser(recv).getModel() == User.Behavior.USR_SYBL;
		if (a || b) {
			nw.STATS.NUM_FBACK_SYBL++;
			return;
		} // If sender or receiver is Sybil, no feedback is recorded.

		//System.out.print("ISOLATED BEFORE pos:" + nw.getUserRelation(recv, send).getPos());
		//System.out.println(" neg:" + nw.getUserRelation(recv, send).getNeg());
		double rand = nw.GLOBALS.RAND.nextDouble();
		if (rand > nw.getUser(recv).getHonesty()) {
			nw.STATS.NUM_FBACK_LIES++;

			if (strategy == MAL_STRATEGY.ISOLATED) {
				if (nw.getUser(recv).getModel() != User.Behavior.USR_GOOD) {
					nw.getUserRelation(recv, send).incGlobalNeg();
					nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
				} else {
					nw.getUserRelation(recv, send).incGlobalPos();
					nw.getUserRelation(recv, send).incGlobaladaptivePos();
				}
			}

			if (strategy == MAL_STRATEGY.COLLECTIVE) {
				if (nw.getUser(recv).getModel() != User.Behavior.USR_GOOD) {
					if (Malineighbor[recv].contains(send)) { // just give the partner positive rating for malicious peers
						nw.getUserRelation(recv, send).incGlobalPos();
						nw.getUserRelation(recv, send).incGlobaladaptivePos();
					} else {
						nw.getUserRelation(recv, send).incGlobalNeg();
						nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
					}
				} else {
					nw.getUserRelation(recv, send).incGlobalPos();
					nw.getUserRelation(recv, send).incGlobaladaptivePos();
				}
			}
			if (strategy == MAL_STRATEGY.DISGUISE) {
				if (Malineighbor[recv].contains(send)) { // just give the partner positive rating for malicious peers
					nw.getUserRelation(recv, send).incGlobalPos();
					nw.getUserRelation(recv, send).incGlobaladaptivePos();
					//      System.out.println("rating recv= "+ recv+" send= "+send+", positve-rating #"+nw.getUserRelation(recv, send).getadaptivePos());
				} else {
					nw.getUserRelation(recv, send).incGlobalNeg();
					nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
					//  System.out.println("--rating recv= "+ recv+" send= "+send);
				}
			}
			if (strategy == MAL_STRATEGY.SPY) {
				if (nw.getUser(recv).getModel() == User.Behavior.USR_DISG) {
					if (nw.getUser(send).getModel() == User.Behavior.USR_PURE) {
						nw.getUserRelation(recv, send).incGlobalPos();
						nw.getUserRelation(recv, send).incGlobaladaptivePos();
					} else {
						nw.getUserRelation(recv, send).incGlobalNeg();
						nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
					}
				} else {
					nw.getUserRelation(recv, send).incGlobalNeg();
					nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
				}
			}

		} else { // Some users will be dishonest in providing global-feedback
			nw.STATS.NUM_FBACK_TRUE++;
			nw.getUserRelation(recv, send).incGlobalNeg();
			nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
		} // Whereas other users will provide truthful global-feedback

		//System.out.print("ISOLATED AFTER pos:" + nw.getUserRelation(recv, send).getPos());
		//System.out.println(" neg:" + nw.getUserRelation(recv, send).getNeg());
	}
}