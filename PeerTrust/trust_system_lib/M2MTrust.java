/**
 * Georgia Tech
 * DISL
 * 2016
 */



package trust_system_lib;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import core_lib.*;

/**
 * The EigenTM class conforms to the TrustAlg interface and implements the
 * EigenTrust algorithm as described by Hector Garcia-molina, et. al.
 */
public class M2MTrust implements TrustAlg{
	
	public enum status{active, inactive};
	
	status [] user;
	status STATE;
	
	// ************************* PROTECTED FIELDS ****************************
	
	/**
	 * The Network which this EigenTM is managing.
	 */
	protected Network nw;
	
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * Weighting constant making pre-trusted peers more powerful
	 */
	private final double ALPHA = 0.1;
	private final double LT_ALPHA = 0.1;
	private final double balance_alpha=1.0;
	private final double simi_para=0.5; // when there is no common peers between two peers to calculate pairwise similarity.
	private int totl_timewindow;
	private int no_act_timewindow;
	
	
	/**
	 * Acceptable error margin in convergence tests.
	 */
	private final double EPSILON = 0.00001;
	
	/**
	 * Pre-trusted peer distribution and weighting vector
	 */
	private double[] pretrust;
	
	/*
	 *  the initial global trust for each peer
	 */
	private double [] inititrust;
	
	/**
	 * Scratch space vector for multiplication purposes.
	 */
	private double[] vectorA;
	//private double[][] LT_vectorA;
	private double [] globarep;
	/**
	 * Scratch space vector for multiplication purposes.
	 */
	private double[] vectorB;
	//private double[][] LT_vectorB;
	
	private double [] threshold;
	
	LinkedList <Integer> []currentactivelist;
	ArrayList normalizedrate[][];
	LinkedList<Double> []active_recall;
	LinkedList<Double> []active_precision;
	
	
//	double [] current_recall;
//	double [] current_precision;
	
	/**
	 * Matrix storing persistent normalized (pre-multiplication) values.
	 */
	private double[][] normalized;
	private double[][] unnormalized; // not normalized local trust used for activation 
	private double[][] LT_normalized;
	private double[][] M2MTrust_RLT_normalized; 
	LinkedList<Double> [] Rep;
	LinkedList<Double> [][]LT_Rep;
	LinkedList<Double> [][]LT_Rep_duringwindowtime;
	LinkedList<Double> []LT_Rep_Eigen;
	
	
	private double simCre[][];  // using similarity to compute the feedback credibility
	private double [][] weight;
	private double[][] weight_thre; 
//	private double [] standarddeviation; // each peer's standard deviation for computing Standardized Euclidean distance
		
	LinkedList<Integer> []linkedparent;
	LinkedList<Integer> []linkedchildren;
	//LinkedList<>
	
	LinkedList<Double> recallstore;
	LinkedList<Double> precisionstore;
	LinkedList<Double> influencestore;
	LinkedList<Double> [] PeerID;
	
	private double [][]similarity;
	private double [][]pretrust_LT;
	private int [][]simi_common; // used to judge whether two peers (p1,p2) have common interacted peers while computing similarity between p1 and p2 
	
//	private double [][] ini_trust;
	
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct an EigenTM object.
	 * @param nw Network which this EigenTM will be managing
	 */
	public M2MTrust(Network nw){
		this.nw = nw;
		pretrust = new double[nw.GLOBALS.NUM_USERS];
		globarep =new double [nw.GLOBALS.NUM_USERS];
		vectorA = new double[nw.GLOBALS.NUM_USERS];
	//	LT_vectorA= new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		vectorB = new double[nw.GLOBALS.NUM_USERS];
	//	LT_vectorB =new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		normalized = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		unnormalized = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		LT_normalized =new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		M2MTrust_RLT_normalized=new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		weight=new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		weight_thre=new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		similarity= new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		simi_common=new int[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		normalizedrate = new ArrayList[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		threshold =new double [nw.GLOBALS.NUM_USERS];
		user= new status[nw.GLOBALS.NUM_USERS];
		recallstore=new LinkedList<Double> ();
		precisionstore=new LinkedList<Double>();
		influencestore=new LinkedList<Double>();
		currentactivelist=new LinkedList[nw.GLOBALS.NUM_USERS];
		active_recall=new LinkedList[nw.GLOBALS.NUM_USERS];
		active_precision =new LinkedList[nw.GLOBALS.NUM_USERS];
		pretrust_LT=new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
//		ini_trust=new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		simCre =new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		inititrust= new double [nw.GLOBALS.NUM_USERS];
//		standarddeviation= new double [nw.GLOBALS.NUM_USERS];
		totl_timewindow=0;
		no_act_timewindow=0;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			user[i]=status.inactive;
	//	    threshold[i]=nw.GLOBALS.RAND.nextDouble()/(double)1.2 + (1-(double)1/(double)1.2);
//		do{
//			threshold[i]=nw.GLOBALS.RAND.nextDouble();
//	     }while (threshold[i]<=0.00);
		    threshold[i]=0.0 ;
//		    threshold[i]=nw.GLOBALS.RAND.nextDouble();
//			currentactivelist[i]=new LinkedList<Integer>();
			
		}
		PeerID = new LinkedList [nw.GLOBALS.NUM_USERS];
		linkedparent =new LinkedList[nw.GLOBALS.NUM_USERS];
		linkedchildren =new LinkedList[nw.GLOBALS.NUM_USERS];
//		current_recall=new double[nw.GLOBALS.NUM_USERS];
//		current_precision =new double [nw.GLOBALS.NUM_USERS];
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			linkedparent[i]=new LinkedList<Integer>();
			linkedchildren[i]=new LinkedList<Integer>();
			active_recall[i]=new LinkedList<Double>();
			active_precision[i]=new LinkedList<Double>();
			PeerID[i]=new LinkedList<Double>();
			currentactivelist[i] =new LinkedList<Integer>();
		}
		
		LT_Rep = new LinkedList[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		LT_Rep_duringwindowtime = new LinkedList[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				similarity[i][j]=0;
				simi_common[i][j]=0;
				simCre[i][j]=0;
				normalizedrate[i][j]=new ArrayList();
				LT_normalized[i][j]=0;
				M2MTrust_RLT_normalized[i][j]=0;
				LT_Rep[i][j]=new LinkedList<Double>();
				LT_Rep_duringwindowtime[i][j] =new LinkedList<Double>();
				if(nw.getUser(j).isPreTrusted()){
					pretrust_LT[i][j]=1/(double)nw.GLOBALS.PRE_TRUSTED;
				}
			}
		}
		
		Rep = new LinkedList [nw.GLOBALS.NUM_USERS]; 
		LT_Rep_Eigen = new LinkedList[nw.GLOBALS.NUM_USERS];
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			Rep[i]=new LinkedList<Double>();
			LT_Rep_Eigen[i] =new LinkedList<Double>();
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				weight[i][j]=0;
				weight_thre[i][j]=0;
			}
		}
				
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(nw.GLOBALS.PRE_TRUSTED > 0 && nw.getUser(i).isPreTrusted())
				pretrust[i] = (1.0 / nw.GLOBALS.PRE_TRUSTED);
			else if(nw.GLOBALS.PRE_TRUSTED > 0) // (and not pre-trusted)
				pretrust[i] = (0.0);
			else // (there are no pre-trusted users)
				pretrust[i] = (1.0 / nw.GLOBALS.NUM_USERS);
			
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
				 normalized[j][i] = pretrust[i]; 
			}
		} // Initialize pre-trusted vector, and persistent normalized values
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
	     	globarep[i]=pretrust[i];	
		    
		    }
	}
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Interfaced: Text name of this trust algorithm (spaces are okay).
	 */
	public String algName(){
		return "M2MTrust";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "M2MTrust";
	}
	public double[] showtrust(){
		/*
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
		System.out.print("\n"+i+".trust= "+vectorA[i]+" ");
		*/
		return globarep;

	}
	/**
	 * Interfaced: Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		normalizeVector(trans.getRecv());
	//	normalizeVector(trans.getSend());
	}
	
	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle){
		//setLinkedlist();
		calsimilarity(cycle);
		active_computetrust(user, 7, cycle);
	}
	private void active_computetrust(int user, int iter, int cycle){
		  setweight_linearthreshold(cycle);	  
		  		  
		  //----M2MTrust with uniform propagation (M2MTrust_UP)-----------
		 /*
		  trustMultiply_uniformPropagation( iter, cycle);  // compute M2MTrust with uniform propagation
		  */
		  //--------------end M2MTrust with uniform propagation------------
		  
		  
		  
		//----M2MTrust with controlled propagation (M2MTrust_RLT & M2MTrust)-----------
		  
		  activestatus ( cycle);			  
		  if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
		     shownormalized();
		  }
		  
		     //-----M2MTrust_RLT-------	
//		  	  rebuiltlocaltrustmatrix_M2MTrust_RLT(cycle);
//			  if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
//				     showM2MTrust_RLT_normalized();
//				  }	
//			  LT_trustMultiply_M2MTrust_RLT( iter, cycle); // for each peer, calculate the others' reputation.			 
		     //-----end M2MTrust_RLT----
		  
			  
		      //---- M2MTrust-----------			  
			  rebuiltlocaltrustmatrix_Sim(cycle); 		  
			  if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
			     showLT_normalized();
			  }		   
			    LT_trustMultiply_M2MTrust( iter, cycle); // for each peer, calculate the others' reputation.			  
		      //---end M2MTrust 
		    
	       //-----------end M2MTrust controlled propagation---------------------
		     
		  
			initialweight();
			initialsimi_common();
			currentactivelistclear();
	  }
		
	  
	  protected double[] LT_trustMultiply_M2MTrust_RLT( int max_iters, int cycle){
			int currt_iter=0;

			cal_initialtrust(cycle);
			
			vectorA = singleMultiply_M2MTrust_RLT(inititrust);
			max_iters--;
			currt_iter++;
			//max_iters--;
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			    showreputation(currt_iter, vectorA);
				}
			
			do{ // Multiply until convergence or maximum iterations reached
				if(max_iters<=0){
					break;
				}
				
				vectorB = singleMultiply_M2MTrust_RLT(vectorA);
				currt_iter++;
				max_iters--;
				if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
					showreputation(currt_iter, vectorB);
						}
				if(max_iters<=0){
					break;
				}
				vectorA = singleMultiply_M2MTrust_RLT(vectorB);
				max_iters--;
				currt_iter++;
				if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				    showreputation(currt_iter, vectorA);
					}
				
			} while((max_iters > 0) && !hasConverged(vectorA, vectorB));
			
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				  System.out.print("\r\n the iteration number is :" + currt_iter);
				}
			
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
		    	showRepchange();
			}
			
			
			double sum=0;
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				sum+=vectorA[i];
			}
			
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				vectorA[i]= vectorA[i]/sum;
			}
			
			
			/*
			 *  the local trust rating (normalmultsimi) = normalized * similarity
			 */
			
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					nw.getUserRelation(i, j).setTrust(vectorA[j]);
				}
			}
			
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				globarep[j]=vectorA[j];	
		//		//System.out.println("globarep["+j+"]= "+globarep[j]);
			}
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 //   System.out.print("\n"+i+".trust= "+vectorA[i]+" "+ "percent= "+ vectorA[i]*100/sum+"%");
					//System.out.print(vectorA[i]/sum+" ");
				if(cycle>nw.GLOBALS.WARMUP)
				    PeerID[i].add(vectorA[i]);
				    
				}
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				   System.out.print("\r\nreputation change for each peers is described as: \r\n");
				for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				   //System.out.println("\n peer "+i+"'s reputation change process int the entire transaction process"+" ("+nw.GLOBALS.NUM_TRANS+"): ");
					for(int j=0;j<PeerID[i].size();j++){
						System.out.print(PeerID[i].get(j)+" ");
					}
				    //System.out.println();
				}
			//	//System.out.println("accout= "+accout);
			}
			
			if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
				showSimCre_normalized();
			  }
			
			if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){ 
				double sum1=0, mali_sum=0; 
				  int count=0;
				for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				   sum1+=vectorA[i];
				}
				//System.out.println("\rthe reputation values for each peer are list as follows: ");
				for (int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					if(sum1!=0){
				    	System.out.print(vectorA[j]/sum1+" ");
				    	if(nw.getUser(j).getModel()!=User.Behavior.USR_GOOD){
				    		mali_sum+=vectorA[j]/sum1;
				    		count++;
				    	}
					}else
						System.out.print("some thing must be wong!");
				}
				if(count>0){
				     //System.out.println("\r\nthe average reputation of malicious peers is: "+mali_sum/(double)count);	
				}else{
					 //System.out.println("\r\n no malicious peers!!");
				}
			}
			
			return vectorA.clone();
		}
	  
	  protected double[] LT_trustMultiply_M2MTrust( int max_iters, int cycle){
			int currt_iter=0;

			cal_initialtrust(cycle);
			
			vectorA = singleMultiply_M2MTrust(inititrust);
			max_iters--;
			currt_iter++;
			//max_iters--;
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			    showreputation(currt_iter, vectorA);
				}
			
			do{ // Multiply until convergence or maximum iterations reached
				if(max_iters<=0){
					break;
				}
				
				vectorB = singleMultiply_M2MTrust(vectorA);
				currt_iter++;
				max_iters--;
				if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
					showreputation(currt_iter, vectorB);
						}
				if(max_iters<=0){
					break;
				}
				vectorA = singleMultiply_M2MTrust(vectorB);
				max_iters--;
				currt_iter++;
				if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				    showreputation(currt_iter, vectorA);
					}
				
			} while((max_iters > 0) && !hasConverged(vectorA, vectorB));
			
			//if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				  System.out.print("\r\n the iteration count : " + currt_iter+"\r\n\r\n");
			//	}
			
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
		    	showRepchange();
			}
			
			
			double sum=0;
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				sum+=vectorA[i];
			}
			
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				vectorA[i]= vectorA[i]/sum;
			}
			
			
			/*
			 *  the local trust rating (normalmultsimi) = normalized * similarity
			 */
			
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					nw.getUserRelation(i, j).setTrust(vectorA[j]);
				}
			}
			
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				globarep[j]=vectorA[j];	
		//		//System.out.println("globarep["+j+"]= "+globarep[j]);
			}
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 //   System.out.print("\n"+i+".trust= "+vectorA[i]+" "+ "percent= "+ vectorA[i]*100/sum+"%");
					//System.out.print(vectorA[i]/sum+" ");
				if(cycle>nw.GLOBALS.WARMUP)
				    PeerID[i].add(vectorA[i]);
				    
				}
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				   System.out.print("\r\nreputation change for each peers is described as: \r\n");
				for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				   //System.out.println("\n peer "+i+"'s reputation change process int the entire transaction process"+" ("+nw.GLOBALS.NUM_TRANS+"): ");
					for(int j=0;j<PeerID[i].size();j++){
						System.out.print(PeerID[i].get(j)+" ");
					}
				    //System.out.println();
				}
			//	//System.out.println("accout= "+accout);
			}
			
			if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
				showSimCre_normalized();
			  }
			
			if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){ 
				double sum1=0, mali_sum=0; 
				  int count=0;
				for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				   sum1+=vectorA[i];
				}
				//System.out.println("\rthe reputation values for each peer are list as follows: ");
				for (int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					if(sum1!=0){
				    	System.out.print(vectorA[j]/sum1+" ");
				    	if(nw.getUser(j).getModel()!=User.Behavior.USR_GOOD){
				    		mali_sum+=vectorA[j]/sum1;
				    		count++;
				    	}
					}else
						System.out.print("some thing must be wong!");
				}
				if(count>0){
				     //System.out.println("\r\nthe average reputation of malicious peers is: "+mali_sum/(double)count);	
				}else{
					 //System.out.println("\r\n no malicious peers!!");
				}
			}
			
			return vectorA.clone();
		}
	  
	  
	  protected double[] trustMultiply_uniformPropagation( int max_iters, int cycle){
			int currt_iter=0;
			System.out.println("Uniform Propagation");
			cal_initialtrust(cycle);
			
			vectorA = singleMultiply_uniformPropagation(inititrust);
			max_iters--;
			currt_iter++;
			//max_iters--;
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			    showreputation(currt_iter, vectorA);
				}
			
			do{ // Multiply until convergence or maximum iterations reached
				if(max_iters<=0){
					break;
				}
				
				vectorB = singleMultiply_uniformPropagation(vectorA);
				currt_iter++;
				max_iters--;
				if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
					showreputation(currt_iter, vectorB);
						}
				if(max_iters<=0){
					break;
				}
				vectorA = singleMultiply_uniformPropagation(vectorB);
				max_iters--;
				currt_iter++;
				if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				    showreputation(currt_iter, vectorA);
					}
				
			} while((max_iters > 0) && !hasConverged(vectorA, vectorB));
			
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				  System.out.print("\r\n the iteration number is :" + currt_iter);
				}
			
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
		    	showRepchange();
			}
			
			
			double sum=0;
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				sum+=vectorA[i];
			}
			
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				vectorA[i]= vectorA[i]/sum;
			}
			
			
			/*
			 *  the local trust rating (normalmultsimi) = normalized * similarity
			 */
			
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					nw.getUserRelation(i, j).setTrust(vectorA[j]);
				}
			}
			
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				globarep[j]=vectorA[j];	
		//		//System.out.println("globarep["+j+"]= "+globarep[j]);
			}
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 //   System.out.print("\n"+i+".trust= "+vectorA[i]+" "+ "percent= "+ vectorA[i]*100/sum+"%");
					//System.out.print(vectorA[i]/sum+" ");
				if(cycle>nw.GLOBALS.WARMUP)
				    PeerID[i].add(vectorA[i]);
				    
				}
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				   System.out.print("\r\nreputation change for each peers is described as: \r\n");
				for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				   //System.out.println("\n peer "+i+"'s reputation change process int the entire transaction process"+" ("+nw.GLOBALS.NUM_TRANS+"): ");
					for(int j=0;j<PeerID[i].size();j++){
						System.out.print(PeerID[i].get(j)+" ");
					}
				    //System.out.println();
				}
			//	//System.out.println("accout= "+accout);
			}
			
			if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
				showSimCre_normalized();
			  }
			
			if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){ 
				double sum1=0, mali_sum=0; 
				  int count=0;
				for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				   sum1+=vectorA[i];
				}
				//System.out.println("\rthe reputation values for each peer are list as follows: ");
				for (int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					if(sum1!=0){
				    	System.out.print(vectorA[j]/sum1+" ");
				    	if(nw.getUser(j).getModel()!=User.Behavior.USR_GOOD){
				    		mali_sum+=vectorA[j]/sum1;
				    		count++;
				    	}
					}else
						System.out.print("some thing must be wong!");
				}
				if(count>0){
				     //System.out.println("\r\nthe average reputation of malicious peers is: "+mali_sum/(double)count);	
				}else{
					 //System.out.println("\r\n no malicious peers!!");
				}
			}
			
			return vectorA.clone();
		}

	
	/**
	 * Test if the difference between two vectors is below some threshold.
	 * @param vec1 The first vector for comparison
	 * @param vec2 The second vector for comparison
	 * @return TRUE if variance < EPSILON at every position. FALSE otherwise.
	 */
	protected boolean hasConverged(double[] vec1, double[] vec2){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(Math.abs(vec1[i]-vec2[i]) > this.EPSILON)
				return false;
		} // Compare vector elements, examining delta change
		return true;
	}
	
	protected boolean LT_hasConverged(int user, double[][] vec1, double[][] vec2){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(Math.abs(vec1[user][i]-vec2[user][i]) > this.EPSILON)
				return false;
		} // Compare vector elements, examining delta change
		return true;
	}
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * Normalize a single vector of the persistent matrix.
	 * @param new_vec The vector to be normalized
	 */
	private void normalizeVector(int new_vec){
		double fback_int, normalizer = 0, tmp=0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			fback_int = calcGlobalFBackInt(nw.getUserRelation(new_vec, i));
			normalizer += fback_int;
			normalized[new_vec][i] = fback_int;
			unnormalized[new_vec][i] = fback_int;
		} // Calculate normalizing sum in first pass
		
		
		if(normalizer == 0){
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[new_vec][i] = pretrust[i];
	//		    normalized[new_vec][i] = 0.0;
		} else{ // If a user trusts no one, default to the pre_trust vector
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			   normalized[new_vec][i] /= (normalizer*1.0);
//			   if(nw.getUserRelation(new_vec, i).getadaptivePos()>=nw.getUserRelation(new_vec, i).getadaptiveNeg()){
//				   tmp=((double)(nw.getUserRelation(new_vec, i).getadaptivePos()+1)/(double)(nw.getUserRelation(new_vec, i).getadaptivePos()+nw.getUserRelation(new_vec, i).getadaptiveNeg()+2)-0.5)*2;
//			   }else{
//				   tmp=-((double)(nw.getUserRelation(new_vec, i).getadaptiveNeg()+1)/(double)(nw.getUserRelation(new_vec, i).getadaptivePos()+nw.getUserRelation(new_vec, i).getadaptiveNeg()+2)-0.5)*2;
//			   }
//	     	   normalizedrate[new_vec][i].add(tmp); 
			   normalizedrate[new_vec][i].add(normalized[new_vec][i]);
			}
		} // Else, do the normalizing division in a second pass
		
		
		
		/*
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			//System.out.print("["+i+"]");
			for (int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print("["+i+"]["+j+"]= "+normalized[i][j]+" ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
		*/
	}
	
	/**
	 * Calculate a 'feedback integer' using global feedback data.
	 * @param rel Relation whose 'feedback integer' needs calculated
	 * @return The calculated 'feedback integer'
	 */
	private double calcGlobalFBackInt(Relation rel){
		double fback_int=0;
	
		 if(rel.getadaptivePos() + rel.getadaptiveNeg()>0){
		    fback_int = (double)(rel.getadaptivePos())/(double)(rel.getadaptivePos() + rel.getadaptiveNeg());
		}
		 if(rel.getadaptivePos()!=rel.getPos()||rel.getadaptiveNeg()!=rel.getNeg() ){
	//	   //System.out.println("getadaptivePos= "+rel.getadaptivePos()+ ", getPos= "+rel.getPos()+", getadaptiveNeg= "+rel.getadaptiveNeg()+ ", getNeg= "+rel.getNeg());
		 }
		   if(fback_int < 0)
			fback_int = 0;
		
//		if(rel.getadaptivePos() >= rel.getadaptiveNeg()){
//		         fback_int = ((double)(rel.getadaptivePos()+1)/(double)(rel.getadaptivePos() + rel.getadaptiveNeg()+2)-0.5)*2;
//		    //   fback_int = (double)(rel.getadaptivePos()+1)/(double)(rel.getadaptivePos() + rel.getadaptiveNeg()+2);
//		}
//		if(fback_int < 0)
//			fback_int = 0;
		
		return fback_int;
	}
			
	/**
	 * Perform a single multiplication iteration per EigenTrust specification.
	 * @param prev_vector Result of the last multiplication iteration
	 * @return A vector closer to converged global trust than that passed in
	 */
	private double[] singleMultiply_M2MTrust(double[] prev_vector){
		double[] lhs = vectorMatrixMult(prev_vector, LT_normalized);
		lhs = constantVectorMult((1-ALPHA), lhs);
		double[] rhs = constantVectorMult(ALPHA, pretrust);
		return (vectorAdd(lhs,rhs));	
	}
	
	private double[] singleMultiply_M2MTrust_RLT(double[] prev_vector){
		double[] lhs = vectorMatrixMult(prev_vector, M2MTrust_RLT_normalized);
		lhs = constantVectorMult((1-ALPHA), lhs);
		double[] rhs = constantVectorMult(ALPHA, pretrust);
		return (vectorAdd(lhs,rhs));	
	}
	
	/**
	 * Perform a single multiplication iteration per EigenTrust specification.
	 * @param prev_vector Result of the last multiplication iteration
	 * @return A vector closer to converged global trust than that passed in
	 */
	private double[] singleMultiply_uniformPropagation(double[] prev_vector){
		double[] lhs = vectorMatrixMult(prev_vector, weight_thre); 
		lhs = constantVectorMult((1-ALPHA), lhs);
		double[] rhs = constantVectorMult(ALPHA, pretrust);
		return (vectorAdd(lhs,rhs));	
	}
		// Linear algebra methods; nothing really unique going on here

	/**
	 * Linear Algebra: Vector-matrix multiplication.
	 * @param vector Vector to be multiplied
	 * @param matrix Matrix to be multiplied
	 * @return The product vector*matrix, per standard matrix multiply
	 */
	
	private double[] vectorMatrixMult(double[] vector, double[][] matrix){
		double[] dest = new double[nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[i] = 0.0;
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
				dest[i] += (matrix[j][i] * vector[j]);  
			} // Inner loop of matrix-vector multiplication
		} // Outer loop of matrix-vector multiplication
		return dest;
	}
	
	
	private double[][] LT_vectorMatrixMult(int user, double[][] vector, double[][] matrix){
		double[][] dest = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			//dest[i] = 0.0;
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
	//			dest[user][i] += ((vector[user][j]*simCre[user][j])*(matrix[j][i]*simCre[j][i]));
				dest[user][i] += ((vector[user][j])*(matrix[j][i]));
			} // Inner loop of matrix-vector multiplication
		} // Outer loop of matrix-vector multiplication
		 dest = normalizeprocess(user, dest); //normalize the reputation [user][i] for all peer i
		return dest;
	}
	
	private double[][] normalizeprocess(int user, double a[][]){
		//double [][] desta=new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		double sum=0;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			  sum+=a[user][i]; //*a[user][i];
		  }
		for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			if (sum!=0){
		    //	a[user][j]=a[user][j]/Math.sqrt(sum);
		    	a[user][j]=a[user][j]/sum;
			}
			else
				a[user][j]=0;
		}
		return a;
	}
	/**
	 * Linear Algebra: Constant-vector multiplication.
	 * @param constant Constant to be multiplied
	 * @param vector Vector to be multiplied
	 * @return The product constant*vector, per standard scalar multiply
	 */
	private double[] constantVectorMult(double constant, double[] vector){
		double[] dest = new double[nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[i] = vector[i] * constant;
		} // Just multiply every vector element by the constant
		return dest;
	}
	
	private double[][] LT_constantVectorMult(int user, double constant, double[][] vector){
		double[][] dest = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[user][i] = vector[user][i] * constant;
		} // Just multiply every vector element by the constant
		return dest;
	}
	
	/**
	 * Linear Algebra: Vector-vector addition.
	 * @param vector1 First vector to be added
	 * @param vector2 Second vector to be added
	 * @return The sum vector1+vector2, per standard vector addition
	 */
	private double[] vectorAdd(double[] vector1, double[] vector2){
		double[] dest = new double[nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[i] = vector1[i] + vector2[i];
		} // Just add the elements at corresponding positions
		return dest;
	}
	
	private double[][] LT_vectorAdd(int user, double[][] vector1, double[][] vector2){
		double[][] dest = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[user][i] = vector1[user][i] + vector2[user][i];
		} // Just add the elements at corresponding positions
		return dest;
	}
	
 private void showreputation(int iter, double []a){
		double sum=0;
        //System.out.println("\r\nall the peers' reputation values when the iteration round = "+ iter+" :");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.print(a[i]+" ");   //+", the weight ratio = "+a[i]*100/sum+"%");
			//double tmp=a[i];
			Rep[i].add(a[i]);
		}
		   //System.out.println("\r\n");
	}
 private void LT_showreputation_Eigen(int iter, double []a){
		double sum=0;
       //System.out.println("\r\nall the peers' reputation computed by LT_Eigen values when the iteration round = "+ iter+" :");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.print(a[i]+" ");   //+", the weight ratio = "+a[i]*100/sum+"%");
			//double tmp=a[i];
			LT_Rep_Eigen[i].add(a[i]);
		}
		   //System.out.println("\r\n");
	}
 
 private void showLT_reputation(int iter, int user, double [][]a){
        //System.out.println("\r\nall the peers' reputation values viewd by "+ user+" when the iteration round = "+ iter+" :");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.print(a[user][i]+" ");   //+", the weight ratio = "+a[i]*100/sum+"%");
			LT_Rep[user][i].add(a[user][i]);
		}
		   //System.out.println("\r\n");
	}
	
 private void showLT_last_iter_reputation(int user, double[][]a){
        //System.out.println("\r\nall the peers' reputation values vied by "+ user+" when the last iteration: ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.print(a[user][i]+" ");   
			LT_Rep[user][i].add(a[user][i]);
		}
		   //System.out.println("\r\n");
 }
	private void showRepchange(){
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			//System.out.println("\r\npeerID= "+i+"' reputation change during the iterations");
			for(int j=0;j<Rep[i].size();j++){
				System.out.print(Rep[i].get(j)+" ");
			}
			//System.out.println();
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			Rep[i].clear();
		}
	}
	private void LT_showRepchange_Eigen(){
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			//System.out.println("\r\npeerID= "+i+"' reputation change using LT_Eigen during the iterations");
			for(int j=0;j<LT_Rep_Eigen[i].size();j++){
				System.out.print(LT_Rep_Eigen[i].get(j)+" ");
			}
			//System.out.println();
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			LT_Rep_Eigen[i].clear();
		}
	}
	private void showLT_Repchange(){

			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					//System.out.println("\r\nreputation["+i+"]["+j+"]' reputation change: ");
					for(int k=0;k<LT_Rep[i][j].size();k++){
						System.out.print(LT_Rep[i][j].get(k)+" ");
					}
				}
			}
			
			//System.out.println();
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
    		   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				LT_Rep[i][j].clear();
    		   }
			}
			
	}
	
	private void showLT_Repchange_duringwindowtime(int user){

		
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				//System.out.println("\r\nreputation["+user+"]["+j+"]' reputation change during entire time window: ");
				for(int k=0;k<LT_Rep_duringwindowtime[user][j].size();k++){
					System.out.print(LT_Rep_duringwindowtime[user][j].get(k)+" ");
				}
			}
		
		
		//System.out.println();
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			   LT_Rep_duringwindowtime[i][j].clear();
		   }
		}
		
}
	
	public void activestatus( int cycle){
		int flag=1, tmp;
//		linkrelation();
		LinkedList<Integer> tmplist =new LinkedList<Integer>();
		double active_recl=0, active_precison=0;
        if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
        	//System.out.println("\rthe activation weight is: ");
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					 System.out.print("weight["+i+"]["+j+"]= "+weight[i][j]+" "); 
					 }
				 //System.out.println();
				   }
	        }
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			 if(weight[i][j] >= thresholdvalue(i,j, cycle)){
				    currentactivelist[i].add(j);
				}
			 }
		   }

		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			if( nw.getUser(i).getModel()==User.Behavior.USR_GOOD){
			  for(int j=0; j<nw.GLOBALS.NUM_USERS;j++){
			     if(currentactivelist[i].contains(j) && !tmplist.contains(j)){
				    tmplist.add(j);
			    }
			   }
			 }
		 }
		
		 
		 int tmp_p=-1;
		 if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){   	
			  int cnt1=0, cnt2=0;;
			  for(int i=0;i<tmplist.size();i++){
				  tmp_p= tmplist.get(i);
				    if(nw.getUser(tmp_p).getModel()==User.Behavior.USR_GOOD){  // good peers include pre-trusted peers
				       cnt1++;
				    } else{
				       cnt2++;
				    }
			  }
			 
			  System.out.println("\r\nthe activated nodes by good peers");
			  for(int i=0;i<tmplist.size();i++){
				  System.out.print(tmplist.get(i)+" ");
			  }
			  
			  System.out.println("\r\nthe fraction of good nodes activated by good peers is: "+ (double)cnt1/nw.GLOBALS.USR_GOOD+ " , nw.GLOBALS.USR_GOOD= "+nw.GLOBALS.USR_GOOD);
			  System.out.println("\rthe fraction of malicious nodes activated by good peers is: "+ (double)cnt2/(nw.GLOBALS.NUM_USERS-nw.GLOBALS.USR_GOOD));
	    } 
	
		   if(cycle>=nw.GLOBALS.WARMUP){
		    int noracc=0;
		    int currentactivelistsize;
	    	for(int i=0;i<tmplist.size();i++){
	    		 tmp_p= tmplist.get(i);
		    	if(nw.getUser(tmplist.get(i)).getModel()==User.Behavior.USR_GOOD){
			    	noracc++;
			  }
		   }
	    	
	    	
	    	if(tmplist.size()==0){
	    		currentactivelistsize=1;
	    		no_act_timewindow++;
	    	}
	    	else
	    		currentactivelistsize=tmplist.size();
	    	
	  //  	//System.out.println("nw.GLOBALS.PRE_TRUSTED+nw.GLOBALS.USR_GOOD= "+nw.GLOBALS.USR_GOOD);
	    	active_recl =(double)noracc/(double)(nw.GLOBALS.USR_GOOD);
	    	active_precison= (double)noracc/(double)(currentactivelistsize);  

	    	
	    	  totl_timewindow++;
	
	       if(noracc!=0){
				recallstore.add( active_recl);
				precisionstore.add(active_precison);
	      }
		}
		
		if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1 ){
			double avg_sum1=0, avg_sum2=0, avg_sum3=0 ;
	//		int cunt=0;
			//System.out.println("the recall change with time window: \n");
			  for(int i=0; i<recallstore.size();i++){
				System.out.print(recallstore.get(i)+" ");
				avg_sum1+=recallstore.get(i);
			  }
			  
			//System.out.println("\r\nthe precision change with time window: \n");
			  for(int i=0; i<precisionstore.size();i++){
				System.out.print(precisionstore.get(i)+" ");
				avg_sum2+=precisionstore.get(i);

//				}
			  }
	    
				//System.out.println("\r\naverage recall of time windows after finishing the transactions is: "+ avg_sum1/(double) recallstore.size()+", recallstore.size= "+recallstore.size());
				//System.out.println("average precision of time windows after finishing the transactions is: "+ avg_sum2/(double) precisionstore.size()+", precisionstore.size= "+precisionstore.size());
				//System.out.println("fraction of no-activation time window after finishing the transactions is: "+ (double)no_act_timewindow/totl_timewindow+",  number of no-activation time windows= "
				//                    +no_act_timewindow+", total number of time windows= "+totl_timewindow);
		}
		
		tmplist.clear();
	//	initialstatus(tmplist); // according to each peer, we all perform the activation behavior
	}
	
private double thresholdvalue(int i,int j, int cycle){
	double threshold, threshold_value;
	   threshold = (double) 1/(1+Math.exp(simCre[i][j]));
	   /*
	    * map threshold to the interval [0,1]; the maximum value is 1/(1+Math.exp(0)) when similarity =0; 
	    * the minimum value is 1/(1+Math.exp(1)) when similarity is =1. 
	    */
	   threshold_value=(threshold-(double)1/(1+Math.exp(1.0)))/((double)1/(1+Math.exp(0))-(double)1/(1+Math.exp(1)));
//	   if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1)
//	     //System.out.println("threshold_value("+i+","+j+")="+threshold_value);
	 return threshold_value;
}

    
  
  private void showSimCre_normalized(){
		//System.out.println("\r\n");
		double sum;
		int[] cunt= new int[nw.GLOBALS.NUM_USERS];
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			cunt[i]=0;
		}
		int a, b=0;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			sum=0; a=0;
			//System.out.println("For peer "+i+" its rating matrix relationship is (after perform Linear threshold): ");
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			    sum+=LT_normalized[i][j];
				System.out.print(LT_normalized[i][j]+" ");
				if(LT_normalized[i][j]!=0){
					a++;
				}
			}
			//System.out.println(i+".Sim_-basedCred_LT_normalizedsum= "+sum);
			cunt[i]=a;
		}
		
		double summ=0;
		//System.out.println();
		//System.out.println("\rFor each peer, the rating percentage to other peers are: ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.print((double)cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
			summ+=(double)cunt[i]/(double)nw.GLOBALS.NUM_USERS;
			if((double)cunt[i]/(double)nw.GLOBALS.NUM_USERS!=0){
				b++;
			}
		}
		//System.out.println();
		//System.out.println("The average rating percentage from the viewpoint of entire network is: " +summ/ (double) b);
	}
  
  private void rebuiltlocaltrustmatrix_Sim(int cycle){
	  double sum, sumsim;
	  double [] weightsum = new double [nw.GLOBALS.NUM_USERS];
	  double [] Simsum = new double [nw.GLOBALS.NUM_USERS];
	  double [][] newsim =new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
	  for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  sum=0;
		  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(currentactivelist[i].contains(j)){
     				  sum+=weight[i][j];
			  }
		  }
		  weightsum[i]=sum;
	  }
	  
  
	  for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  if(weightsum[i]!=0){
		    for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(currentactivelist[i].contains(j)){
			         LT_normalized[i][j]=weight[i][j]/weightsum[i];
			  } 
			  else{
				      LT_normalized[i][j]=0;
			  }
		  }
	   }else{
		   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
		       LT_normalized[i][j]=0;
		   }
	   }
	  }
	  
	  
	  double [] normalizedsum1=new double [nw.GLOBALS.NUM_USERS];
	  double sum1;
	  for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  sum1=0;
		  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  sum1+=LT_normalized[i][j];
		  }
		  normalizedsum1[i]=sum1;
	  }
	  
	  for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  sum1=0;
		  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(normalizedsum1[i]!=0)
			  LT_normalized[i][j]=LT_normalized[i][j]/normalizedsum1[i];
			  else
			  LT_normalized[i][j]=0;  
		  }
	  }
		  
	  if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
	     //System.out.println();
	     for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
		    System.out.print("LT_normailizedsum["+i+"]= "+normalizedsum1[i]+"  ");
	     }
	  }
  }
  
  private void rebuiltlocaltrustmatrix_M2MTrust_RLT(int cycle){
	  double sum, sumsim;
	  double [] weightsum = new double [nw.GLOBALS.NUM_USERS];
	  double [] Simsum = new double [nw.GLOBALS.NUM_USERS];
	  double [][] newsim =new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
	  for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  sum=0;
		  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(currentactivelist[i].contains(j)){
     				  sum+=normalized[i][j];
			  }
		  }
		  weightsum[i]=sum;
	  }
	  
  
	  for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  if(weightsum[i]!=0){
		    for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(currentactivelist[i].contains(j)){
			         M2MTrust_RLT_normalized[i][j]=normalized[i][j]/weightsum[i];
			  } 
			  else{
				  M2MTrust_RLT_normalized[i][j]=0;
			  }
		  }
	   }else{
		   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			   M2MTrust_RLT_normalized[i][j]=0;
		   }
	   }
	  }
	  
	  
	  double [] normalizedsum1=new double [nw.GLOBALS.NUM_USERS];
	  double sum1;
	  for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  sum1=0;
		  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  sum1+=M2MTrust_RLT_normalized[i][j];
		  }
		  normalizedsum1[i]=sum1;
	  }
	  
	  for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  sum1=0;
		  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(normalizedsum1[i]!=0)
				  M2MTrust_RLT_normalized[i][j]=M2MTrust_RLT_normalized[i][j]/normalizedsum1[i];
			  else
				  M2MTrust_RLT_normalized[i][j]=0;  
		  }
	  }
		  
	  if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
	     //System.out.println();
	     for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
		    System.out.print("M2MTrust_RLT_normalized["+i+"]= "+normalizedsum1[i]+"  ");
	     }
	  }
  }
  
 private double sum_parent( int m, LinkedList<Integer> currntactivlist){
	 int tmp;
	 double sum=0;
	 for(int i=0;i<currntactivlist.size(); i++){
	     tmp = currntactivlist.get(i);
		 if(linkedparent[m].contains(tmp))
		 {
			 sum+=weight_thre[tmp][m];
		 }
	 }
	 
	 return sum;
 }
		
	boolean active(int m){
		if(user[m]==status.active)
			return true;
		else
		    return false;
	}
	
	private void setweight_linearthreshold( int cycle){
		double sum, sum1, sum2;
//		 balance_alpha=0.85;
		double []weightsum =new double[nw.GLOBALS.NUM_USERS];
		double []weightsum1 =new double[nw.GLOBALS.NUM_USERS];
		double []weightsum2 =new double[nw.GLOBALS.NUM_USERS];
		
//	   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//		   sum=0;
//		   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//			   //if(nw.getUserRelation(i, j).getPos()!=0 || nw.getUserRelation(i, j).getNeg()!=0){
//				   sum+=unnormalized[j][i];
//			   //}
//		   }
//		   weightsum[i]=sum;
//	   }
//	   
	   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//		   if(weightsum[i]!=0){
		   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//			    weight[i][j]=(1-balance_alpha) *normalized[j][i]/weightsum[i] + balance_alpha * simCre[i][j];    
//	    	    weight[i][j]=(1-balance_alpha) *unnormalized[i][j] + balance_alpha * simCre[i][j]; 
			   if(simi_common[i][j]==1 ){  //means that there are common interacted peer(s)
				   if(simCre[i][j]!=0)
	    	           weight[i][j]=normalized[i][j] * Math.exp(1-(double)1/simCre[i][j]);  //setting the weight between two peers.
				   else
					   weight[i][j]=0.0;
			   } else{ // meaning that there is no common interacted peer, the similarity is set as default (simi_para). 
				    weight[i][j]=normalized[i][j] * Math.exp(1-(double)1/simi_para); 
//				    if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1 && nw.getUser(i).getModel()!=User.Behavior.USR_GOOD){
//				    	//System.out.println("unnormalized["+i+"]["+j+"]= "+unnormalized[i][j]+", "+"normalized["+i+"]["+j+"]= "+normalized[i][j]);
//				    }
	    	     }
	    	    
		     }
//		   } 
	   }
	   
	   
	   
	   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		   sum2=0.0;
		   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			   if(j!=i)
			   sum2+=weight[i][j]; 
		   }
		       weightsum2[i]=sum2;
	   }

	   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			   if(weightsum2[i]>0.0){
				  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				   if(j!=i)
			         weight_thre[i][j]=weight[i][j]/weightsum2[i];
			   else
				      weight_thre[i][j]=0.0; 
//				   System.out.print("weight_thre["+i+"]["+j+"]= "+weight_thre[i][j]+" ");
				   }
				  }
			   else{
				   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				      weight_thre[i][j]=0.0;
//				      System.out.print("weight_thre["+i+"]["+j+"]= "+weight_thre[i][j]+" ");
				   } 
			   } 
//		   //System.out.println();
	   }
		   
       
	   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		   sum1=0;
		   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			   if(j!=i){
			   sum1+=weight_thre[j][i];
//			   System.out.print("weight_thre["+j+"]["+i+"]= "+weight_thre[j][i]+" ");
			   }
		   }
		   weightsum1[i]=sum1;
//		   //System.out.println();
	   }
	   
	   
	   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		   if(weightsum1[i]>1.0){
		   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			   if(j!=i){
			    weight_thre[j][i]=weight_thre[j][i]/weightsum1[i];
			   }
			   else
			    weight_thre[j][i]=0.0;
//			   System.out.print("weight_thre_rev["+j+"]["+i+"]= "+weight_thre[j][i]+" ");	
	//		   System.out.print("in_weight_sum>1.0");	
		    }
	     }
		  else{
			   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			      if(j!=i)
				   weight_thre[j][i]=weight_thre[j][i];
			      else
			       weight_thre[j][i]=0.0; 
//				   System.out.print("weight_thre_rev["+j+"]["+i+"]= "+weight_thre[j][i]+" ");	 
			   }
		   }
//		   //System.out.println();
	   }
	   
	   if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
		   
		   //System.out.println("/r/nthe not normalzied local trust is: ");
		   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				   System.out.print("weight["+i+"]["+j+"]= "+ weight[i][j]+" ");
			   }
			       //System.out.println();
		   }
		   
		   //System.out.println("/r/nthe new generated local trust is: ");
		   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				   System.out.print("weight_thre["+i+"]["+j+"]= "+ weight_thre[i][j]+" ");
			   }
			       //System.out.println();
		   }
	   }
	   
	}
	
	
    private void initialweight(){
    	for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
    		for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
    			weight[i][j]=0;
    			weight_thre[i][j]=0;
    		}
    	}
    }
    
    private void initialsimi_common(){
    	for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
    		for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
    		  simi_common[i][j]=0;
    		}
    	}
    }
    private void initialstatus(LinkedList<Integer> tpm){
    	tpm.clear();
    }
    
	private void linkedrelationclear(){
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			linkedchildren[i].clear();
			linkedparent[i].clear();
		}
	}
	
	private void currentactivelistclear(){ 
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		currentactivelist[i].clear();
		}
	}
	private void linkrelation(){
		for (int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				if(weight_thre[i][j]!=0){
					if(!linkedchildren[i].contains(j))
				    	linkedchildren[i].add(j);
					if(!linkedparent[j].contains(i))
		    			linkedparent[j].add(i);
				}
			}
		}
	}
	
public void calsimilarity(int cycle){
	
	   
	if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
		  //System.out.println("\r\nthe similarity matrix is: ");
		}	
	
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
//			  //System.out.println("peerID= "+i+"'s localtrustsimilarity is: \n");
//			}			   
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				similarity[i][j]=similaritycredibility(i,j);
				if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
//				    System.out.print("localtrust_similarity["+i+"]["+j+"]= "+similarity[i][j]+"  ");
			    	System.out.print(+similarity[i][j]+" ");
				}
			}
			
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			   //System.out.println();
			}
		}
		
		double sumSimCre[] =new double [nw.GLOBALS.NUM_USERS];
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				sumSimCre[i]+=similarity[i][j];
			}
		}
		
//		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//		  if(sumSimCre[i]!=0){
//			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//				simCre[i][j]=similarity[i][j]/sumSimCre[i];
//			}
//		  }else{
//			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//				simCre[i][j]=0.0;
//				}
//			}
//		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				simCre[i][j]=similarity[i][j];
			}
		}

		
//		double sum=0;
//		double[] sum_similarity =new double [nw.GLOBALS.NUM_USERS];
//		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//			sum=0;
//			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//				sum+=similarity[i][j];
//			}
//			sum_similarity[i]=sum;
//			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1)
//			//System.out.println("sum_similarity["+i+"]= "+sum_similarity[i]);
//			
//		}
//		
//		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//			if(sum_similarity[i]>0){
//			  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//				  simCre[i][j]=similarity[i][j]/sum_similarity[i];
//				  if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1)
//				   System.out.print("simCre["+i+"]["+j+"]= "+simCre[i][j]+" ");
//			   }
//			}else{
//				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//					simCre[i][j]=0;
//				}
//			}
//			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1)
//			 //System.out.println();
//		}
		
		
	}

	
	/**
	 *  compute the credibility of feedback! 
	 */
	private double similaritycredibility(int m, int n){
     
		double miltisum1[] =new double [nw.GLOBALS.NUM_USERS];
		double miltisum2[] =new double [nw.GLOBALS.NUM_USERS];
		double miltisum=0, similarity=0;
		double tmp1=0, tmp2=0, tmp3;
		
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
				miltisum1[i]=miltisum2[i]=0;
		}
					
		int cunt1, cunt2, cout=0, cunt3=0;
		for (int i=0; i<nw.GLOBALS.NUM_USERS;i++){
		 tmp1=tmp2=0; cunt1=cunt2=0;
		   if((nw.getUserRelation(m, i).getPos()!=0 ) 
				   && (nw.getUserRelation(n, i).getPos()!=0)){
			      for(int j=0;j<normalizedrate[m][i].size();j++){
                    if((Double)normalizedrate[m][i].get(j)!=0){ 
                	  cunt1++;
                	  tmp1+= (Double) normalizedrate[m][i].get(j);
			    	  }
//                    if((double)normalizedrate[m][i].get(j)<0){
//                    	//System.out.println("<0");
//                    }
			      }
			      for(int k=0;k<normalizedrate[n][i].size();k++){
			    	  if((Double)normalizedrate[n][i].get(k)!=0){
			    		  cunt2++;
			    		  tmp2+= (Double) normalizedrate[n][i].get(k);
			        } 	  
			      }
			     // if(nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS==150699)
			   //   //System.out.println("normalizedrate["+m+"]["+i+"].size()= "+normalizedrate[m][i].size()+"  cunt1= "+cunt1);
			      
			      if(cunt1!=0 ){
			        miltisum1[i] = tmp1/(double)cunt1;
			       }else{
			    	   miltisum1[i]=0; 
			       }
			       if(cunt2!=0){
			    	   miltisum2[i] = tmp2/(double)cunt2; 
			    	   }else{
			    	   miltisum2[i] = 0;
			    	   }
			       
			      cout++;      
		   }
		}
	
		
		/*
		 *  Standardized (Weighted) Euclidean distance
		 */
		
		double [] temp1_standdevi = new double [nw.GLOBALS.NUM_USERS];
		double [] normal_standdevi = new double [nw.GLOBALS.NUM_USERS];
		double sum_standdevi=0.0;
		int cout2=0;
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){  
			if(miltisum1[i]!=0 || miltisum2[i]!=0)
			  temp1_standdevi[i] = Com_standarddeviation(miltisum1[i],miltisum2[i]);  // calculate the standard deviation of peer i rated by peer m and peer n
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 sum_standdevi+=temp1_standdevi[i];
			}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			if(sum_standdevi!=0)
		    	temp1_standdevi[i]/=sum_standdevi;
			else
				temp1_standdevi[i]=0;
			}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			if(miltisum1[i]!=0 || miltisum2[i]!=0){
		     	miltisum+=temp1_standdevi[i]*(miltisum1[i]-miltisum2[i])*(miltisum1[i]-miltisum2[i]); // standarddeviation(i): computer peer i's standard deviation for Standardized Euclidean distance
			}
		}
		
//		//System.out.println("distance=" + miltisum);
		
		if(cout >= 1){
		  similarity =1- Math.sqrt(miltisum);
		  simi_common[m][n]=1;    // meaning these two peers m and n have at least one common interacted peer. 
		  if(similarity>1.0){
			  //System.out.println("similarity= "+ similarity);
		  }
		}
		else
		  similarity =0;
		
		  return similarity;

		/*
		 * consine similarity, which is the two vectors' relation and not suitable for feedback similarity  
		 * 
		 */
		/*
		double vec_1 =0.0, vec_2=0.0, avg_1=0.0, avg_2=0.0, sum_miltisum1=0,sum_miltisum2=0; 
		
//		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//			sum_miltisum1 += miltisum1[i];
//			sum_miltisum2 += miltisum2[i];
//		}
//		
//		if(cout>0){
//			avg_1= sum_miltisum1/cout++;
//			avg_2= sum_miltisum2/cout++;
//		}
//		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			if(miltisum1[i]!=0 && miltisum2[i]!=0){
				miltisum += (miltisum1[i]*miltisum2[i]);
				vec_1 += miltisum1[i]*miltisum1[i];
				vec_2 += miltisum2[i]*miltisum2[i];	
				
//				miltisum += (miltisum1[i]-avg_1)*(miltisum2[i]-avg_2);
//				vec_1 += (miltisum1[i]-avg_1)*(miltisum1[i]-avg_1);
//				vec_2 += (miltisum2[i]-avg_2)*(miltisum2[i]-avg_2);	
	        }
		}
			
		if(cout >= 1 && vec_1!=0 && vec_2!=0){
		  similarity =miltisum/(Math.sqrt(vec_1)*Math.sqrt(vec_2));
		}
		else
		  similarity =0;
		
	*/	
	}
	
	
	private double Com_standarddeviation(double avg_normalizem, double avg_normalizen){
		    double sum=0.0, standdeviation=0.0;		
		    double avg=0.0;
		    
		        avg = (avg_normalizem + avg_normalizen)/(double)2; // mean value
		    	sum+=(avg_normalizem - avg)*(avg_normalizem-avg) + (avg_normalizen-avg)*(avg_normalizen-avg);
		        standdeviation= Math.sqrt(sum/(double)2);
    
		   return standdeviation;
	}
	
	private void shownormalized(){
		//System.out.println("\r\n");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print("normalized["+i+"]["+j+"]= "+normalized[i][j]+" ");
			}
			//System.out.println();
		}
	}
	private void showLT_normalized(){
		//System.out.println("\r\n");
		double sum[] =new double[nw.GLOBALS.NUM_USERS];
		int[] out_cunt= new int[nw.GLOBALS.NUM_USERS];
		int[] in_cunt=new int[nw.GLOBALS.NUM_USERS];
		
		//System.out.println("For each peer, the rating matrix relationship is (before perform Linear threshold): ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print(normalized[i][j]+" ");
				}
			//System.out.println(" ;");
		}
		
		//System.out.println("For each peer, the rating matrix relationship is (after perform Linear threshold): ");
		int a,c, b=0,d=0;
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 a=0; c=0;
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print(LT_normalized[i][j]+" ");
				if(LT_normalized[i][j]!=0){
					a++;
				}
				if(LT_normalized[j][i]!=0){
					c++;
				}
			}
			//System.out.println(" ;");
			out_cunt[i]=a;
			in_cunt[i]=c;
		}
		
		//System.out.println("\r\npeers' out_ratings matrix (graph) (0-1-inf) for computing diameter is list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
		{  
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(i!=j){
				 if(LT_normalized[i][j]!=0){
					System.out.print(1+" ");
				   }
				 else{
					System.out.print("inf"+" ");
				     }
				  }
			  else 
					System.out.print(0+" ");
			}
			
			//System.out.println(";");
		}
		
		//System.out.println("\r\npeers' in_ratings matrix (graph) (0-1-inf) for computing diameter is list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
		{  
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(i!=j){
				 if(LT_normalized[j][i]!=0){
					System.out.print(1+" ");
				   }
				 else{
					System.out.print("inf"+" ");
				     }
				  }
			  else 
					System.out.print(0+" ");
			}
			
			//System.out.println(";");
		}
		
			
		double summ=0;
		double max_out_cunt=Double.MIN_VALUE;
		double min_out_cunt= Double.MAX_VALUE;
		int max_out_degree= 0, min_out_degree=0, max_in_degree=0, min_in_degree=0;
		double avg_out_degree=0.0, avg_in_degree=0.0;
		
		//System.out.println("\r\nFor each peer, the rating percentage to other peers are: ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){  // only refer to the good peers
			System.out.print((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
			summ+=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
			if((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS!=0){
				if((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS>max_out_cunt){
					max_out_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
				    max_out_degree= out_cunt[i];
				}
				if((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS<min_out_cunt){
					min_out_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
					min_out_degree=out_cunt[i];
				}
				b++;
			}
			 }
		}
		
		double sumd=0;
		double max_in_cunt=Double.MIN_VALUE;
		double min_in_cunt= Double.MAX_VALUE;
		//System.out.println("\r\nFor each peer, the rating percentage to other peers are: ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){  // only refer to the good peers
			System.out.print((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
			sumd+=(double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
			if((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS!=0){
				if((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS>max_in_cunt){
					max_in_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
					max_in_degree=in_cunt[i];
				}
				if((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS<min_in_cunt){
					min_in_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
					min_in_degree=in_cunt[i];
				}
				d++;
			}
			 }
		}
		
		 double sum_out=0, sum_in=0, cnt_out=0, cnt_in=0;
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){  // only refer to the good peers
			 if(out_cunt[i]!=0){
				 sum_out+=out_cunt[i];
				 cnt_out++;
			 }
			 if(in_cunt[i]!=0){
				 sum_in+=in_cunt[i];
				 cnt_in++;
			 }
		  }
		 }
		 
		 //System.out.println("sum_out= "+sum_out+", cnt_out="+cnt_out+"; sum_in= "+sum_in+", cnt_in"+cnt_in);
		 avg_out_degree= (double)sum_out/cnt_out;
		 avg_in_degree= (double)sum_in/cnt_in;	
		 
		 //System.out.println();
		 //System.out.println("\r\n\rthe maximum outgoing degree is: "+ max_out_degree+ ", the maximum out_rating percentage is: " + max_out_cunt);
		 //System.out.println("\n\rthe minimum outgoing degreeis: "+min_out_degree+", the minimum out_rating percentage is: " + min_out_cunt);
		 //System.out.println("\n\rthe average outgoing degree is: "+avg_out_degree+", the average out_rating percentage is: " + (double)summ/(double)b);
		 
		 //System.out.println("\r\n\rthe maximum ingoing degree is: "+ max_in_degree+", the maximum in_rating percentage is: " + max_in_cunt);
		 //System.out.println("\n\rthe minimum ingoing degree is: "+min_in_degree+", the minimum in_rating percentage is: " + min_in_cunt);
		 //System.out.println("\n\rthe average ingoing degree is :"+ avg_in_degree+", the average in_rating percentage is: " + (double)sumd/(double)d);
		 
		 
//		//System.out.println();
//		//System.out.println("The average out_rating percentage from the viewpoint of entire network is: " +summ/ (double) b);
	}
	
	private void showM2MTrust_RLT_normalized(){
		//System.out.println("\r\n");
		double sum[] =new double[nw.GLOBALS.NUM_USERS];
		int[] out_cunt= new int[nw.GLOBALS.NUM_USERS];
		int[] in_cunt=new int[nw.GLOBALS.NUM_USERS];
		
		//System.out.println("For each peer, the rating matrix relationship is (before perform SIR controlled propagation): ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print(normalized[i][j]+" ");
				}
			//System.out.println(" ;");
		}
		
		//System.out.println("For each peer, the rating matrix relationship is (after perform SIR controlled propagation): ");
		int a,c, b=0,d=0;
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 a=0; c=0;
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print(M2MTrust_RLT_normalized[i][j]+" ");
				if(M2MTrust_RLT_normalized[i][j]!=0){
					a++;
				}
				if(M2MTrust_RLT_normalized[j][i]!=0){
					c++;
				}
			}
			//System.out.println(" ;");
			out_cunt[i]=a;
			in_cunt[i]=c;
		}
		
		//System.out.println("\r\npeers' out_ratings matrix (graph) (0-1-inf) for computing diameter is list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
		{  
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(i!=j){
				 if(M2MTrust_RLT_normalized[i][j]!=0){
					System.out.print(1+" ");
				   }
				 else{
					System.out.print("inf"+" ");
				     }
				  }
			  else 
					System.out.print(0+" ");
			}
			
			//System.out.println(";");
		}
		
		//System.out.println("\r\npeers' in_ratings matrix (graph) (0-1-inf) for computing diameter is list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
		{  
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			  if(i!=j){
				 if(M2MTrust_RLT_normalized[j][i]!=0){
					System.out.print(1+" ");
				   }
				 else{
					System.out.print("inf"+" ");
				     }
				  }
			  else 
					System.out.print(0+" ");
			}
			
			//System.out.println(";");
		}
		
			
		double summ=0;
		double max_out_cunt=Double.MIN_VALUE;
		double min_out_cunt= Double.MAX_VALUE;
		int max_out_degree= 0, min_out_degree=0, max_in_degree=0, min_in_degree=0;
		double avg_out_degree=0.0, avg_in_degree=0.0;
		
		//System.out.println("\r\nFor each peer, the rating percentage to other peers are: ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){  // only refer to the good peers
			System.out.print((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
			summ+=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
			if((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS!=0){
				if((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS>max_out_cunt){
					max_out_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
				    max_out_degree= out_cunt[i];
				}
				if((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS<min_out_cunt){
					min_out_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
					min_out_degree=out_cunt[i];
				}
				b++;
			}
			 }
		}
		
		double sumd=0;
		double max_in_cunt=Double.MIN_VALUE;
		double min_in_cunt= Double.MAX_VALUE;
		//System.out.println("\r\nFor each peer, the rating percentage to other peers are: ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){  // only refer to the good peers
			System.out.print((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
			sumd+=(double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
			if((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS!=0){
				if((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS>max_in_cunt){
					max_in_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
					max_in_degree=in_cunt[i];
				}
				if((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS<min_in_cunt){
					min_in_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
					min_in_degree=in_cunt[i];
				}
				d++;
			}
			 }
		}
		
		 double sum_out=0, sum_in=0, cnt_out=0, cnt_in=0;
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		  if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){  // only refer to the good peers
			 if(out_cunt[i]!=0){
				 sum_out+=out_cunt[i];
				 cnt_out++;
			 }
			 if(in_cunt[i]!=0){
				 sum_in+=in_cunt[i];
				 cnt_in++;
			 }
		  }
		 }
		 
		 //System.out.println("sum_out= "+sum_out+", cnt_out="+cnt_out+"; sum_in= "+sum_in+", cnt_in"+cnt_in);
		 avg_out_degree= (double)sum_out/cnt_out;
		 avg_in_degree= (double)sum_in/cnt_in;	
		 
		 //System.out.println();
		 //System.out.println("\r\n\rthe maximum outgoing degree is: "+ max_out_degree+ ", the maximum out_rating percentage is: " + max_out_cunt);
		 //System.out.println("\n\rthe minimum outgoing degreeis: "+min_out_degree+", the minimum out_rating percentage is: " + min_out_cunt);
		 //System.out.println("\n\rthe average outgoing degree is: "+avg_out_degree+", the average out_rating percentage is: " + (double)summ/(double)b);
		 
		 //System.out.println("\r\n\rthe maximum ingoing degree is: "+ max_in_degree+", the maximum in_rating percentage is: " + max_in_cunt);
		 //System.out.println("\n\rthe minimum ingoing degree is: "+min_in_degree+", the minimum in_rating percentage is: " + min_in_cunt);
		 //System.out.println("\n\rthe average ingoing degree is :"+ avg_in_degree+", the average in_rating percentage is: " + (double)sumd/(double)d);
		 
		 
//		//System.out.println();
//		//System.out.println("The average out_rating percentage from the viewpoint of entire network is: " +summ/ (double) b);
	}
	 private void inital_localtrust(){
		  
//		  //System.out.println("before initialization (ask friends' rating), the local trust ratings are: ");
//		  for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
//			  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//				  System.out.print(LT_normalized[i][j]+" ");
//			  }
//			  //System.out.println();
//		  }
		 double tmp_LT_normalized[][]=new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		 
		 for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			//	 if(LT_normalized[i][j]==0){
				 for(int k=0;k<nw.GLOBALS.NUM_USERS;k++){	 
						 if(weight[i][k]!=0 && LT_normalized[k][j]!=0){
					      tmp_LT_normalized[i][j]+=weight[i][k]*LT_normalized[k][j]; 
						 }
					 }
			//	 }
			 }
		 }
		 
		 
		 double sum;
		 double[] sum_LT_normalized =new double[nw.GLOBALS.NUM_USERS];
		 
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 sum=0;
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				 if(LT_normalized[i][j]==0)
				   sum+=tmp_LT_normalized[i][j];
				 else
				   sum+=LT_normalized[i][j]; 
			 }
			 sum_LT_normalized[i]=sum;
		 }
			 
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 if(sum_LT_normalized[i]>0){
			   for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				   if(LT_normalized[i][j]==0)
				       LT_normalized[i][j]=tmp_LT_normalized[i][j]/sum_LT_normalized[i];
				   else
					   LT_normalized[i][j]=LT_normalized[i][j]/sum_LT_normalized[i]; 
			    }
			 }else{
				 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					 LT_normalized[i][j]=0;
				 }
			 }
		 }
		 
//		 //System.out.println("after initialization (ask friends' rating), the local trust ratings are: ");
//		 for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
//			  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//				  System.out.print(LT_normalized[i][j]+" ");
//			  }
//			  //System.out.println();
//		  }
		 
	  }
	 private void cal_initialtrust(int cycle){
		 double sum_ini;
		 
		 for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				 inititrust[i]+=normalized[j][i]*normalized[i][j];		
			 }
		 }
		 
	
		 double sum=0.0;
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 sum+=inititrust[i];
			 }
		 
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 inititrust[i]/=sum;
		 }
		 
		 
		 if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			 //System.out.println("the initial global trsut values are: ");
			 for (int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 System.out.print(inititrust[i]+" ");
			 }
			 //System.out.println();
		 }
	 }
	 
	public void adaptiveupdate(Transaction trans) {
		// TODO Auto-generated method stub
		
	}

	public void computeadptiveTrust(int user, int cycle) {
		// TODO Auto-generated method stub
		
	}

	public void adaptivetimewindow() {
		// TODO Auto-generated method stub
		
	}

	public void clearnormalizedrateList() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getDirectTrusted() {
		System.out.println("Direct Tx Trusts:");
		double minn=Integer.MAX_VALUE, maxx=0, avg=0;
		double cur;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;++i){
			cur=0;
			for(int j=0;j<nw.GLOBALS.NUM_USERS;++j){
				cur+= nw.getUserRelation(i, j).getPos()+nw.getUserRelation(i, j).getNeg()>0? 1:0;
				System.out.println(""+i+" -> "+j);
			}
			if(cur ==0)
				nw.STATS.countZeroTrustDegree++;
			minn = Math.min(minn,cur);
			maxx = Math.max(maxx,cur);
			avg+=cur;
		}
		avg/=nw.GLOBALS.NUM_USERS;
		nw.STATS.minDirectDegree=minn;
		nw.STATS.maxDirectDegree=maxx;
		nw.STATS.avgDirectDegree=avg;
		System.out.println("Direct Tx Trusts END");
	}
}
