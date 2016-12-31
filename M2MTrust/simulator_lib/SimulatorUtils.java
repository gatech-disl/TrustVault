/**
 * Georgia Tech
 * DISL
 * 2016
 */

package simulator_lib;


import java.util.*;
import core_lib.*;
import simulator_lib.SimulatorMalicious.MAL_STRATEGY;
import trust_system_lib.TrustAlg;

/**
 * The SimulatorUtils class assists the TraceSimulator driver. Its work
 * most pertains to the queuing and commitment of transactions (actually 
 * transferring files and deciding feedbacks).
 */
public class SimulatorUtils{
	
	private List<Integer> collection;
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Simulate a transaction on a network.
	 * @param nw Network in which the transaction should be simulated
	 * @param cyc The current cycle
	 * @param trans The Transaction to be simulated
	 * @param mal Object coordinating malicious user activity
	 * @param ALG Trust algorithm instance managing Network 'nw'
	 */
	public void simTrans(Network nw, int cyc, Transaction trans, 
			SimulatorMalicious mal, TrustAlg ALG, Structure strct, MAL_STRATEGY strategy, SimulatorInput simulaInput){
		//System.out.println("simTrans (R) "+trans.getRecv()+" : (F)"+trans.getFile());
		transactionCommit(nw, cyc, ALG, strategy, strct);
		transactionQueue(nw, cyc, trans, mal, ALG, strct, strategy, simulaInput);
	}
	
	/**
	 * Commit all remaining transactions in a Network delay queue.
	 * @param nw The Network with outstanding (queued) transactions
	 * @param cycle Cycle from which to begin commitments 
	 * @param ALG Trust algorithm instance managing Network 'nw'
	 */
	public void commitRemaining(Network nw, int cycle, TrustAlg ALG, MAL_STRATEGY strategy, Structure strct){
		int inc = 0;
		while(nw.queueSize() != 0){
			transactionCommit(nw, (cycle+inc), ALG, strategy, strct);
			inc++;
		} // Commit all remaining transactions in delay queue	
	}
	
	// ************************** PRIVATE METHODS ****************************

	/**
	 * If ready (complete), commit the Transaction atop the delay queue.
	 * @param nw Network whose queued Transaction will commit (if ready)
	 * @param cycle The current cycle
	 * @param ALG Trust algorithm instance managing Network 'nw'
	 * @return The committed transaction, or NULL if none was committed
	 */
	private Transaction transactionCommit(Network nw, int cycle, TrustAlg ALG, MAL_STRATEGY strategy, Structure strct){
		Transaction cur_trans = nw.peekQueue();
		
	//	if (cycle<20)
	//	System.out.print("\n"+"cycle1= "+cycle+"  ");
		
		if((cur_trans != null) && (cur_trans.getCommit() == cycle)){
//			if(cycle<20){
//				System.out.print("cycle2= "+cycle+"  ");
//			System.out.print(cycle+"_sd="+cur_trans.getSend()+" "+cycle+"_rv= "+cur_trans.getRecv()+
//					" _file= "+cur_trans.getFile()+", "+" validity= "+ cur_trans.getValid()+", ");
//			}
			commitFile(nw, cur_trans);
			commitFBack(nw, cur_trans, strategy, strct);
			//System.out.println(cur_trans.getRecv()+" to "+cur_trans.getSend()+"::"+nw.getUserRelation(cur_trans.getRecv(), cur_trans.getSend()).getTrust());
			ALG.update(cur_trans);
		//	ALG.adaptiveupdate(cur_trans);
			nw.dequeueTrans();
		//	System.out.println("cur_trans.getRecv= "+cur_trans.getRecv());
			return cur_trans;
		} else {// If transaction at queue head is ready to be committed, do so
		//	System.out.println(" transaction at queue head is ready to be committed");
			return null;
			}
	}
	
	/**
	 * Transfer a file between two users.
	 * @param nw Network in which the transfer should take place
	 * @param trans Transaction detailing parties/parameters of transfer
	 */
	private void commitFile(Network nw, Transaction trans){
		int recv = trans.getRecv();
		//System.out.print("recv= "+recv+" ");
		double rand = nw.GLOBALS.RAND.nextDouble();
		if(!trans.getValid()){ 
			if(nw.getUser(recv).getModel()==User.Behavior.USR_GOOD){
		    	nw.STATS.NUM_INVAL_TRANS++;
		    	nw.getUser(trans.getSend()).incuploadcount();
//		    	System.out.println("send= "+trans.getSend());
			}
			//System.out.println("nw.STATS.NUM_INVAL_TRANS(1)"+nw.STATS.NUM_INVAL_TRANS);
			if(nw.getUser(recv).getModel() == User.Behavior.USR_GOOD)
				{
				nw.STATS.NUM_GOOD_FAIL++; //System.out.println("cut222");
				}
			//if(rand > nw.getUser(recv).getCleanup())
			//	nw.addFile(trans.getRecv(), trans.getFile(), trans.getValid());
		} else{ // If a bad file is received
			if(nw.getUser(recv).getModel()==User.Behavior.USR_GOOD){
			nw.STATS.NUM_VAL_TRANS++; //System.out.println("cut333");
			}
			//System.out.println("nw.STATS.NUM_VAL_TRANS(0)= "+nw.STATS.NUM_VAL_TRANS);
			if(nw.getUser(recv).getModel() == User.Behavior.USR_GOOD){
				nw.STATS.NUM_GOOD_SUCC++; //System.out.println("cut444");
				//nw.addFile(trans.getRecv(), trans.getFile(), trans.getValid());
			}
			//else if(rand > (1.0 - nw.getUser(recv).getCleanup()))
				//nw.addFile(trans.getRecv(), trans.getFile(), trans.getValid());
		} // Else if a good file is received
		return;
	}
	
	/**
	 * Commit feedback upon transaction completion.
	 * @param nw Network in which feedback should be made
	 * @param trans Transaction detailing parties/parameters of feedback
	 */
	private void commitFBack(Network nw, Transaction trans, MAL_STRATEGY strategy, Structure strct){
		
		int send = trans.getSend();
		int recv = trans.getRecv();
		//System.out.print("send= "+send+" ");
		
		boolean a = nw.getUser(send).getModel() == User.Behavior.USR_SYBL;
		boolean b = nw.getUser(recv).getModel() == User.Behavior.USR_SYBL;
		if(a || b){
			nw.STATS.NUM_FBACK_SYBL++;
			return;
		} // If sender or receiver is Sybil, no feedback is recorded.
	    nw.getUser(send).incuploadcount();
		//System.out.println("MAL STRATEGY:"+strategy);
		
//		if(nw.getUser(send).getModel()!=User.Behavior.USR_GOOD){
//			if(strategy==MAL_STRATEGY.COLLECTIVE){
//				if(nw.getUser(send).getModel()==User.Behavior.USR_PURE){
//						nw.getUserRelation(send, getmalipartner(send)).incGlobalPos();
//						nw.getUserRelation(send, getmalipartner(send)).incGlobaladaptivePos();
//						//System.out.println("\n send= "+send+", partner= "+getmalipartner(send));
//					
//			 }
//			} 
//			if(strategy==MAL_STRATEGY.DISGUISE){
//				 if(nw.getUser(send).getModel()==User.Behavior.USR_DISG){
//						 nw.getUserRelation(send, getmalipartner(send)).incGlobalPos();
//						 nw.getUserRelation(send, getmalipartner(send)).incGlobaladaptivePos();
//						// System.out.println("\n send= "+send+", partner= "+getmalipartner(send));
//			  }
//			}
//			if(strategy==MAL_STRATEGY.SPY){
//				if(nw.getUser(send).getModel()==User.Behavior.USR_DISG){
//				   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//					   if(nw.getUser(i).getModel()==User.Behavior.USR_PURE){
//						nw.getUserRelation(send, i).incGlobalPos();
//						nw.getUserRelation(send, i).incGlobaladaptivePos();
//						
//					}
//				}
//			  }
//			}
//		}	
			// Store accurate interaction history 
		if(trans.getValid())
			nw.getUserRelation(recv, send).incHonestPos();
		else
			nw.getUserRelation(recv, send).incHonestNeg();
		
		//System.out.print(": "+send+"-"+recv+"\n");
		
		double rand = nw.GLOBALS.RAND.nextDouble();
		if(rand > nw.getUser(recv).getHonesty()){
			//System.out.println("NOT HONEST");
			nw.STATS.NUM_FBACK_LIES++;
			if(trans.getValid()){
				//System.out.println("trans valid");
				if(strategy==MAL_STRATEGY.DISGUISE){
					if(getmalipartner(recv)==send){   // just give the partner positive rating for malicious peers
					       nw.getUserRelation(recv, send).incGlobalPos();
					       nw.getUserRelation(recv, send).incGlobaladaptivePos();
					    //   System.out.println("rating recv= "+ recv+" send= "+send+", positve-rating #"+nw.getUserRelation(recv, send).getadaptivePos());
					       }else{
					       nw.getUserRelation(recv, send).incGlobalNeg();
    					   nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
    					 //  System.out.println("--rating recv= "+ recv+" send= "+send);
					       }
				}
				if(strategy==MAL_STRATEGY.SPY){
					 if(nw.getUser(recv).getModel()==User.Behavior.USR_DISG){
						 if( nw.getUser(send).getModel()==User.Behavior.USR_PURE){
						     nw.getUserRelation(recv, send).incGlobalNeg();
					         nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
						 }else{
							 nw.getUserRelation(recv, send).incGlobalNeg();
				             nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
				         }
					 }else{
						  nw.getUserRelation(recv, send).incGlobalNeg();
						  nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
					 }
				 }
				
				if(strategy==MAL_STRATEGY.COLLECTIVE){
					  if(nw.getUser(recv).getModel()!=User.Behavior.USR_GOOD){
				    	if(getmalipartner(recv)==send){   // just give the partner positive rating for malicious peers
					   //     System.out.println("recv= "+recv);
				    		nw.getUserRelation(recv, send).incGlobalPos();
					       }else{
					    	    nw.getUserRelation(recv, send).incGlobalNeg();
					    	    nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
					       }
				    	}
				}
				else{
				     nw.getUserRelation(recv, send).incGlobalNeg();
				     nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
				     //System.out.println("in ELSE");
				}
				if(strategy==MAL_STRATEGY.ISOLATED){
		    	    nw.getUserRelation(recv, send).incGlobalNeg();
		    	    nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
		    	    //System.out.println("in next IF");
				}
		  	 }
			else{
				//System.out.println("trans invalid");
//				if(strategy==MAL_STRATEGY.COLLECTIVE){
//			    	if(getmalipartner(recv)==send){   // just give the partner positive rating for malicious peers
//				        nw.getUserRelation(recv, send).incGlobalPos();
//				       nw.getUserRelation(recv, send).incGlobaladaptivePos();
//				       }else{
//				    	   
//				    	    nw.getUserRelation(recv, send).incGlobalNeg();
//    				    	nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
//				       }
//			    	}
				if(strategy==MAL_STRATEGY.ISOLATED){
					//System.out.print("ISOLATED BEFORE pos:"+nw.getUserRelation(recv, send).getPos());
					System.out.println(" neg:"+nw.getUserRelation(recv, send).getNeg());
					
					if(nw.getUser(recv).getModel()==User.Behavior.USR_GOOD){
						nw.getUserRelation(recv, send).incGlobalPos();
						nw.getUserRelation(recv, send).incGlobaladaptivePos();
					}else{
		    	        nw.getUserRelation(recv, send).incGlobalNeg();
		    	        nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
					}
					//System.out.print("ISOLATED AFTER pos:"+nw.getUserRelation(recv, send).getPos());
					//System.out.println(" neg:"+nw.getUserRelation(recv, send).getNeg());
    	       }
				if(strategy==MAL_STRATEGY.COLLECTIVE){
				  if(nw.getUser(recv).getModel()!=User.Behavior.USR_GOOD){
			    	if(getmalipartner(recv)==send){   // just give the partner positive rating for malicious peers
				        nw.getUserRelation(recv, send).incGlobalPos();
				        nw.getUserRelation(recv, send).incGlobaladaptivePos();
				       }else{ 
				    	       nw.getUserRelation(recv, send).incGlobalNeg();
				    	       nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
				    	   }
				       }else{
				    	    nw.getUserRelation(recv, send).incGlobalPos();
				    	    nw.getUserRelation(recv, send).incGlobaladaptivePos();
				       }
			    	}
				if(strategy==MAL_STRATEGY.SPY){
					 if(nw.getUser(recv).getModel()==User.Behavior.USR_DISG){
						 if( nw.getUser(send).getModel()==User.Behavior.USR_PURE){
						     nw.getUserRelation(recv, send).incGlobalPos();
					         nw.getUserRelation(recv, send).incGlobaladaptivePos();
						 }else{
							 nw.getUserRelation(recv, send).incGlobalNeg();
				             nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
				         }
					 }else{
						  nw.getUserRelation(recv, send).incGlobalNeg();
						  nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
					 }
				 }
				
				
//				nw.getUserRelation(recv, send).incGlobalPos();
//				nw.getUserRelation(recv, send).incGlobaladaptivePos();
				//System.out.println("unhonesty_GlobalPos= +1");
			}
		} else{ // Some users will be dishonest in providing global-feedback
			//System.out.println("HONEST FEEDBACK");
			nw.STATS.NUM_FBACK_TRUE++;
			if(trans.getValid()){
				//System.out.println("BEFORE:"+nw.getUserRelation(recv, send).getPos()+" and "+nw.getUserRelation(recv, send).getNeg());
				nw.getUserRelation(recv, send).incGlobalPos();
				nw.getUserRelation(recv, send).incGlobaladaptivePos();
				//System.out.println("honesty_GlobalPos= +1");
				//System.out.println("AFTER:"+nw.getUserRelation(recv, send).getPos()+" and "+nw.getUserRelation(recv, send).getNeg());
			 }
			else{
				
				nw.getUserRelation(recv, send).incGlobalNeg();
				nw.getUserRelation(recv, send).incGlobaladaptiveNeg();
			    //System.out.println("-1 Rating from "+recv+" -- to "+send);
			}
		} // Whereas other users will provide truthful global-feedback
		nw.ALG.update(trans);

	}
	
	
	/**
	 * Begin a transaction between parties, queuing it for later commitment.
	 * @param nw  Network in which the transaction will take place 
	 * @param cycle The current cycle
	 * @param trans Transaction detailing parties/parameters of transaction
	 * @param mal Object coordinating malicious user activity
	 * @param ALG Trust algorithm instance managing Network 'nw'
	 */
	private void transactionQueue(Network nw, int cycle, Transaction trans, 
			SimulatorMalicious mal, TrustAlg ALG,  Structure strct, MAL_STRATEGY STRATEGY, SimulatorInput simulaInput){
		//System.out.println("transactionQueue");
		int recv = trans.getRecv();
	//	if(cycle<20)
	//	System.out.print(cycle+"_sed= "+trans.getSend()+" "+cycle+"_recv= "+recv+" ");
		int file = trans.getFile();
	//	if (cycle<20)
	//	System.out.print(cycle+"_file= "+file+" ");
		boolean cond1 = nw.hasFile(recv, file);
		boolean cond2 = nw.getUser(recv).BWidthAvailableDL(cycle);
	//	if(cycle<20)
		//System.out.print("cond1= "+cond1+" cond2= "+cond2+" ");
		if(!cond2 ){ //||cond1 ){
			nw.STATS.NUM_RECV_BLK_TR++;
			return;
		} // If receiving user already has file or no DL bandwidth, abort.
		
			// Setup the distributed malicious strategy, do trust computation
		//System.out.println("cycle@= "+cycle);
		
		/*if(cycle%100==0 && (cycle!=0)){
			//mal.computeTrust(recv, cycle, ALG);
			ALG.computeTrust(recv, cycle);
			 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					 nw.getUserRelation(i, j).reinitialposneg();
				 }
			 }
 
		}*/
		
			// Pick source user based on user model/availability
				
		User.Behavior model = nw.getUser(recv).getModel();
		SimulatorSource.Strategy strategy = SimulatorSource.pickStrategy(model);
		int send = SimulatorSource.pickSource(nw, cycle, recv, file, strategy, strct, STRATEGY, simulaInput);
		
		if(send == -1){
			if(nw.getUser(recv).getModel()==User.Behavior.USR_GOOD){		
			   nw.STATS.NUM_SEND_BLK_TR++;
		   }
			//System.out.println("return -1");
			return;
		} // If query unanswered or no sources have b-width, abort
		
		
			// Transaction is proceeding, consume bandwidth
		nw.getUser(recv).BWidthConsumeDL(cycle);
		nw.getUser(send).BWidthConsumeUL(cycle);
		
		boolean valid = nw.fileCopyValid(file, send);
		int commit = cycle + nw.GLOBALS.BAND_PER;
		Transaction t = new Transaction(commit, send, recv, file, valid);
		nw.enqueueTrans(t);
		//System.out.println("tx Enqueued!");
	}	

	public void maliciarray(Network nw){
		collection =new ArrayList<Integer>();
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			if(nw.getUser(i).getModel()!=User.Behavior.USR_GOOD){
				collection.add(i);
			}
		}
	}
	
	public void showcollection(){
		for(int i=0;i<collection.size();i++){
			System.out.println("collection("+i+")= "+collection.get(i)+"  "+i+".partner= "+getmalipartner(i));
		}
	}
	
	public int getmalipartner(int k){
	
		int m;
		if(k!=collection.size()-1)
	     	m=collection.get(k+1);
		else
			m=collection.get(0);
		return m;
	}
	
	public void callocaltrust(Network nw, TrustAlg ALG){
		/*for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			ALG.update(i);	
		}*/
	}
	
	public void caladaptivelocaltrust(Network nw, TrustAlg ALG){
		/*for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			ALG.adaptiveupdate(i);	
		}*/
	}
	
	public void calreputation(Network nw, int m, int cycle, TrustAlg ALG){
		//System.out.println("CalReputation");
		  ALG.computeTrust(m, cycle);	 
	}
	
	public void caladaptivereputation(Network nw, int m, int cycle, TrustAlg ALG){
		
		  ALG.computeadptiveTrust(m, cycle);	 
	}
	
	public void setnegpos(Network nw, TrustAlg ALG){
		
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				 nw.getUserRelation(i, j).reinitialposneg();
				 nw.getUserRelation(i, j).reinitialadaptivefeedbk();
			 }
		 }
		
		ALG.clearnormalizedrateList();
	}
	
	public void setadaptivenegpos(Network nw, TrustAlg ALG){
		
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				 nw.getUserRelation(i, j).reinitialadaptivefeedbk();
			 }
		 }
		
		ALG.clearnormalizedrateList();
		
	}
	
	public void showadaptiveNegPos(Network nw){
		//System.out.println(" adaptive time windows:");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print("nw.getUserRelation("+i+","+ j+").getadaptiveNeg= "+nw.getUserRelation(i, j).getadaptiveNeg() +" ; "+
						"nw.getUserRelation("+i+","+ j+").getadaptivePos= "+nw.getUserRelation(i, j).getadaptivePos());
			 }
		 }
	}
	
	public void showNegPos(Network nw){
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print("nw.getUserRelation("+i+","+ j+").getNeg= "+nw.getUserRelation(i, j).getNeg() +" ; "+
						"nw.getUserRelation("+i+","+ j+").getPos= "+nw.getUserRelation(i, j).getPos());
			 }
		 }
	}
}
