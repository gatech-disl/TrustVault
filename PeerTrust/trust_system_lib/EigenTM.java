/**
 * Georgia Tech
 * DISL
 * 2016
 */


package trust_system_lib;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import core_lib.*;

/**
 * The EigenTM class conforms to the TrustAlg interface and implements the
 * EigenTrust algorithm as described by Hector Garcia-molina, et. al.
 */
public class EigenTM implements TrustAlg{
	
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
	
	/**
	 * Acceptable error margin in convergence tests.
	 */
	private final double EPSILON = 0.00001;
	
	/**
	 * Pre-trusted peer distribution and weighting vector
	 */
	private double[] pretrust;
	
	/**
	 * Scratch space vector for multiplication purposes.
	 */
	private double[] vectorA;
	
	/**
	 * Scratch space vector for multiplication purposes.
	 */
	private double[] vectorB;
	
	/**
	 * Matrix storing persistent normalized (pre-multiplication) values.
	 */
	private double[][] normalized;
	
	private double [][] adaptivenormalized;
	
	/**
	 * feedback credibility
	 */
	
	private double [][] feedbkcredit;
	
	/**
	 *  Global reputation
	 */
	
	private double [] globarep;
	
	private double [] adaptiveglobalrep;
	
	private double [] finalreputation;
	
	LinkedList<Double> [] PeerID;	
	LinkedList<Double> [] Rep;
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct an EigenTM object.
	 * @param nw Network which this EigenTM will be managing
	 */
	public EigenTM(Network nw){
		this.nw = nw;
		pretrust = new double[nw.GLOBALS.NUM_USERS];
		vectorA = new double[nw.GLOBALS.NUM_USERS];
		vectorB = new double[nw.GLOBALS.NUM_USERS];
		normalized = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		adaptivenormalized = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		feedbkcredit =new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		globarep =new double [nw.GLOBALS.NUM_USERS];
		adaptiveglobalrep =new double [nw.GLOBALS.NUM_USERS];
		finalreputation =new double [nw.GLOBALS.NUM_USERS];
		
		for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			for(int k=0;k<nw.GLOBALS.NUM_USERS;k++){
				feedbkcredit[j][k]=0;
				normalized[k][j] = 0;
				adaptivenormalized[k][j]=0;
			}
		}
		
		for(int m=0;m<nw.GLOBALS.NUM_USERS;m++){
			finalreputation[m]=0;
		}
		
		PeerID =new LinkedList [nw.GLOBALS.NUM_USERS];
		Rep =new LinkedList [nw.GLOBALS.NUM_USERS];
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			Rep[i]=new LinkedList<Double>();
			PeerID[i]=new LinkedList<Double>();
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
				adaptivenormalized[j][i]=pretrust[i];
			}
		} // Initialize pre-trusted vector, and persistent normalized values
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
	     	globarep[i]=pretrust[i];	
		    adaptiveglobalrep[i]=pretrust[i];
		    }
	}
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Interfaced: Text name of this trust algorithm (spaces are okay).
	 */
	public String algName(){
		return "EigenTrust";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "EigenTrust";
	}
	public double[] showtrust(){
		//for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
		//System.out.print("\n"+i+".trust= "+vectorA[i]+" ");
		return globarep;
		/*
		double nrz = 0.0;
		double [] ret = new double[nw.GLOBALS.NUM_USERS];
		double[] unnormalized = globarep;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;++i){
			nrz+=unnormalized[i];
		}
		if (nrz ==0)
			return ret;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;++i){
			ret[i] = unnormalized[i]/nrz;
		}
		return ret;*/
	}
	/**
	 * Interfaced: Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		//normalizeVector(peer);
		normalizeVector(trans.getRecv());
		normalizeVector(trans.getSend());
	}
	
	public void adaptiveupdate (Transaction trans){
		//adaptivenormalizeVector(peer);
		normalizeVector(trans.getRecv());
		normalizeVector(trans.getSend());
	}
	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle){
	//	calfeedbkcredibility();
		
		int iter=7;
		int NodeID =0;
		trustMultiply(user, iter, cycle);
//		Num_nodes_propagated(NodeID, iter);
	}
	public void computeadptiveTrust(int user, int cycle){
		//calfeedbkcredibility();
	//	adaptivetrustMultiply(user, 100);
	}
	
	// ************************* PROTECTED METHODS ***************************
	
	/**
	 * Perform matrix multiply as a means of aggregating global trust data.
	 * @param user Identifier of user performing trust computation
	 * @param max_iters Maximum number of multiplications to perform
	 * @return The converged global trust vector
	 */
	protected double[] trustMultiply(int user, int max_iters, int cycle){
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
		//System.out.println("Before initial trust for each of pair of peers, the normalized rating-matrix is: ");
		showrating_matrix(cycle, normalized);
	}
		int currt_iter=0;
		vectorA = singleMultiply(pretrust);
		
		max_iters--;
		currt_iter++;
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			
		    showreputation(currt_iter, vectorA);
			}
		
		do{ // Multiply until convergence or maximum iterations reached
			if(max_iters<=0){
				break;
			}
			
			vectorB = singleMultiply(vectorA);
			currt_iter++;
			max_iters--;
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				showreputation(currt_iter, vectorB);
					}
			if(max_iters<=0){
				break;
			}
			vectorA = singleMultiply(vectorB);
			max_iters--;
			currt_iter++;
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			    showreputation(currt_iter, vectorA);
				}
		//	max_iters -= 2;
//		} while(max_iters > 0);
		} while((max_iters > 0) && !hasConverged(vectorA, vectorB));
		
		//if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			  System.out.println("\r\n the iteration count to convergence : " + currt_iter+"\r\n");
		//	}
		
		
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
	    	showRepchange();
		}
		
		//System.out.println("\r\nwhen cycle= "+ cycle+" all the peers's reputation values are (summation = 1.0): ");
		double sum_vector=0.0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			sum_vector+=vectorA[i];
		         }
		
		    for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			    
			    System.out.print(vectorA[i]/sum_vector +" ");
		           } // Import trust values back into Object form, duplicating vector
		        System.out.print("\n");
		    
		
		
			
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				nw.getUserRelation(i, j).setTrust(vectorA[j]);
			}
		}
		
		for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			globarep[j]=vectorA[j];	
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
		
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
		    //System.out.println("\r\nwhen the amount of transaction is: "+ (cycle-nw.GLOBALS.WARMUP+1));
	    	double sum=0;
		    for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			  sum+=vectorA[i];
		         }
		    //System.out.println("all the peers's reputation values are (summation = 1.0): ");
		    for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			    
			    System.out.print(vectorA[i]/sum +" ");
		           } // Import trust values back into Object form, duplicating vector
		        System.out.print("\n");
		    }
		
		return vectorA.clone();
	}
	/*
	 * count the number of nodes propagated and amount of trust propagation implemented starting from a special node
	 */
private void Num_nodes_propagated(int m, int iter){
	int propagatingNum=0;
    int hop=1; 
	int Int_temp, Int_temp1;
	LinkedList<Integer> [] Node_propagted= new LinkedList [iter];
	for(int i=0; i<iter; i++){
		Node_propagted[i]=new LinkedList();
	}
	Vector<Integer> TotalNumNode= new Vector();
		
	for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
	  if(normalized [m][i]!=0){
		  Node_propagted[hop].add(i);
		  propagatingNum++;
	  }
	}	
	 	 
	while(hop<iter){
		for(int i=0; i<Node_propagted[hop].size();i++){
			Int_temp=Node_propagted[hop].get(i);
			hop++;
//			NumNodePropCom(Int_temp, hop);
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				if(normalized[Int_temp][j]!=0){
					Node_propagted[hop].add(i);
					propagatingNum++;
				}
			}
		}
	}
	
	for(int i=0; i<iter;i++){
		for(int j=0;j<Node_propagted[i].size();j++){
			Int_temp1=Node_propagted[i].get(j);
			if(!TotalNumNode.contains(Int_temp1)){
				TotalNumNode.add(Int_temp1);
			}
		}
	}
	//System.out.println("the Number of nodes to whcih trust is propoased from "+m+"is: "+TotalNumNode.size()+ ", the number of trust propagation is: "+propagatingNum);
}

private void showreputation(int iter, double []a){
		
		double sum=0;
		//System.out.println("\r\nthe "+ iter+" times' repuatation values are: \n");
	    /*
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			sum+=a[i];
		}
		*/
       //System.out.println("\r\nall the peers' reputation values when the iteration round = "+ iter+" :");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.print(a[i]+" ");   //+", the weight ratio = "+a[i]*100/sum+"%");
			//double tmp=a[i];
			Rep[i].add(a[i]);
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

	protected double[] adaptivetrustMultiply(int user, int max_iters){
		vectorA = adaptivesingleMultiply(pretrust);
		max_iters--;
		do{ // Multiply until convergence or maximum iterations reached
			vectorB = adaptivesingleMultiply(vectorA);
			vectorA = adaptivesingleMultiply(vectorB);
			max_iters -= 2;
		} while((max_iters > 0) && !hasConverged(vectorA, vectorB));
		
		/*
		double sum=0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			sum+=vectorA[i];
		}
		*/
		
		//for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			//nw.getUserRelation(user, i).setTrust(vectorA[i]);
		//	System.out.print("\n"+i+".trust= "+vectorA[i]+" "+ "percent= "+ vectorA[i]*100/sum+"%");
		//} // Import trust values back into Object form, duplicating vector
		//System.out.print("\n");
	/*	
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				nw.getUserRelation(i, j).setTrust(vectorA[j]);
			}
		}
		*/	
		for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			adaptiveglobalrep[j]=vectorA[j];	
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
	
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * Normalize a single vector of the persistent matrix.
	 * @param new_vec The vector to be normalized
	 */
	private void normalizeVector(int new_vec){
		double fback_int, normalizer = 0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			fback_int = calcGlobalFBackInt(nw.getUserRelation(new_vec, i));
			normalizer += fback_int;
			normalized[new_vec][i] = fback_int;
		} // Calculate normalizing sum in first pass
		
		if(normalizer == 0){
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[new_vec][i] = pretrust[i];
		} else{ // If a user trusts no one, default to the pre_trust vector
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[new_vec][i] /= (normalizer*1.0);
		} // Else, do the normalizing division in a second pass
		/*
		System.out.print("\n");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			//System.out.print("["+i+"]");
			for (int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print("normalized["+i+"]["+j+"]= "+normalized[i][j]+" ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
		*/
	}
	
	private void adaptivenormalizeVector(int new_vec){
		double fback_int, normalizer = 0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			fback_int = adaptivecalcGlobalFBackInt(nw.getUserRelation(new_vec, i));
			normalizer += fback_int;
			adaptivenormalized[new_vec][i] = fback_int;
		} // Calculate normalizing sum in first pass
		
		if(normalizer == 0){
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				adaptivenormalized[new_vec][i] = pretrust[i];
		} else{ // If a user trusts no one, default to the pre_trust vector
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				adaptivenormalized[new_vec][i] /= (normalizer*1.0);
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
		 fback_int = rel.getPos() - rel.getNeg();
//		 //System.out.println("fback_int= "+fback_int);
//		if(rel.getPos() + rel.getNeg()>0){
//		     fback_int = (double)rel.getPos()/(double)(rel.getPos() + rel.getNeg());
//		  }
		if(fback_int < 0.0)
			fback_int = 0.0;
		return fback_int;
	}
	private double adaptivecalcGlobalFBackInt(Relation rel){
		double fback_int=0;
		if(rel.getadaptivePos() + rel.getadaptiveNeg()>0){
//		    fback_int= (double)(rel.getadaptivePos()-rel.getadaptiveNeg())/(double)(rel.getadaptivePos() + rel.getadaptiveNeg());
		    fback_int= (double)rel.getadaptivePos()/(double)(rel.getadaptivePos() + rel.getadaptiveNeg());
		}
		if(fback_int < 0)
			fback_int = 0;
		return fback_int;
	}
	/**
	 * Perform a single multiplication iteration per EigenTrust specification.
	 * @param prev_vector Result of the last multiplication iteration
	 * @return A vector closer to converged global trust than that passed in
	 */
	private double[] singleMultiply(double[] prev_vector){
		double[] lhs = vectorMatrixMult(prev_vector, normalized);
		lhs = constantVectorMult((1-ALPHA), lhs);
		double[] rhs = constantVectorMult(ALPHA, pretrust);
		return (vectorAdd(lhs,rhs));	
	}
	
	private double[] adaptivesingleMultiply(double[] prev_vector){
		double[] lhs = vectorMatrixMult(prev_vector, adaptivenormalized);
		lhs = constantVectorMult((1-ALPHA), lhs);
		double[] rhs = constantVectorMult(ALPHA, pretrust);
		return (vectorAdd(lhs,rhs));	
	}
	
		
		// Linear algebra methods; nothing really unique going on here

	/**
	 *  compute the credibility of feedback! 
	 */
	private void calfeedbkcredibility(){
		double trustsum[] =new double [nw.GLOBALS.NUM_USERS];
		List<Integer> user[];
		user= new LinkedList[nw.GLOBALS.NUM_USERS];
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			user[i]=new LinkedList();
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			trustsum[i]=0;
		//	//System.out.println("globarep["+i+"]= "+globarep[i]);
		}
		
		double trusum;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			trusum=0;
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			if(nw.getUserRelation(j, i).getNeg()!=0 || nw.getUserRelation(j, i).getPos()!=0){
				trusum+=globarep[j];
				user[i].add(j);
			}
			}
			trustsum[i]=trusum;
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			if(trustsum[i]!=0){
			 for(int j=0;j<user[i].size();j++){  
				   feedbkcredit[user[i].get(j)][i]=globarep[user[i].get(j)]/trustsum[i];
			     }
			}else{
				for(int j=0;j<user[i].size();j++){  
					   feedbkcredit[user[i].get(j)][i]=0;
				     }
			}
		}
		
		/*
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				System.out.print("feedbkcredit["+i+"]["+j+"]= "+feedbkcredit[i][j]+"  ");
			}
			//System.out.println();
		}
		*/
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			user[i].clear();
		}
	}
	
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
	/*
	 * show the matrix of rating between a pair of peers 
	 */
	
	private void showrating_matrix(int cycle, double[][] matrix){
	    int[] out_cunt= new int[nw.GLOBALS.NUM_USERS];
	    int[] in_cunt= new int [nw.GLOBALS.NUM_USERS];
		int a, c, d=0, b=0; double sumb=0, sumd=0;
		 DecimalFormat df = new DecimalFormat( "0.0000");
		 
		//System.out.println("\rpeers' ratings are list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
			{   a=0; c=0;
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					System.out.print(df.format(matrix[i][j])+" "); // peer i's rating to j (i gives rating to j)
					if(matrix[i][j]!=0){
					//	System.out.print(1+" ");
						a++;
					}
					if(matrix[j][i]!=0){
						c++;
					}
//					else{
//						System.out.print(0+" ");
//					}
						
				}
				out_cunt[i]=a;
				in_cunt[i]=c;
				//System.out.println(";");
			}
			
			//System.out.println("\rpeers' out_ratings matrix (graph) (0-1-inf) for computing diameter is list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
			{  
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				  if(i!=j){
					 if(matrix[i][j]!=0){
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
					 if(matrix[j][i]!=0){
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
			
			double max_out_cunt=Double.MIN_VALUE;
		//	//System.out.println("max_out_cunt= "+max_out_cunt);
			double min_out_cunt= Double.MAX_VALUE;
		//	//System.out.println("min_out_cunt= "+min_out_cunt);
			int max_out_degree= 0, min_out_degree=0, max_in_degree=0, min_in_degree=0;
			double avg_out_degree=0.0, avg_in_degree=0.0;
			//System.out.println("\rFor each peer, the out_rating percentage is from 0 to "+ (nw.GLOBALS.NUM_USERS-1));
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){  // only refer to the good peers
			 System.out.print((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
			 sumb+=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
			 if(out_cunt[i]!=0){
				 if((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS>max_out_cunt){
					 max_out_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
				     max_out_degree= out_cunt[i]; 
				 }
//				 if((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS<min_out_cunt ){
//					 min_out_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
//				     min_out_degree=out_cunt[i];
//				   }
				 if((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS<min_out_cunt && nw.getUser(i).getModel()==User.Behavior.USR_GOOD){
					 min_out_cunt=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
				     min_out_degree=out_cunt[i];
				   }
				 
				 b++;
			 }	
			 }
		 }
		 
		 
		 double max_in_cunt=0;
		 double min_in_cunt=1;
			//System.out.println("\r\nFor each peer, the in_rating percentage is from 0 to "+ (nw.GLOBALS.NUM_USERS-1));
			 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			   if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){ 
				 System.out.print((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
				 sumd+=(double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
				 if(in_cunt[i]!=0){
					 if((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS>max_in_cunt){
						 max_in_cunt=(double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
					     max_in_degree=in_cunt[i];
					 }
					 if((double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS<min_in_cunt){
						 min_in_cunt=(double)in_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
					     min_in_degree=in_cunt[i];
					 }
					 d++;
				 }	 
			   }
			 }
		
			 int sum_out=0, sum_in=0, cnt_out=0, cnt_in=0;
			 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			   if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){
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
			 
			 //System.out.println("\r\nsum_out= "+sum_out+", cnt_out="+cnt_out+"; sum_in= "+sum_in+", cnt_in= "+cnt_in);
			 avg_out_degree= (double)sum_out/nw.GLOBALS.NUM_USERS;
			 avg_in_degree= (double)sum_in/nw.GLOBALS.NUM_USERS;		 
			 
			 
		 System.err.println();
		 System.err.println("\r\n\rthe maximum outgoing degree is: "+ max_out_degree+ ", the maximum out_rating percentage is: " + max_out_cunt);
		 System.err.println("\n\rthe minimum outgoing degreeis: "+min_out_degree+", the minimum out_rating percentage is: " + min_out_cunt);
		 System.err.println("\n\rthe average outgoing degree is: "+avg_out_degree+", the average out_rating percentage is: " + (double)sumb/(double)b);
		 
		 System.err.println("\r\n\rthe maximum ingoing degree is: "+ max_in_degree+", the maximum in_rating percentage is: " + max_in_cunt);
		 System.err.println("\n\rthe minimum ingoing degree is: "+min_in_degree+", the minimum in_rating percentage is: " + min_in_cunt);
		 System.err.println("\n\rthe average ingoing degree is :"+ avg_in_degree+", the average in_rating percentage is: " + (double)sumd/(double)d);

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
	
	public void adaptivetimewindow(){
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			if(adaptiveglobalrep[i]<globarep[i]){
				  finalreputation[i]=adaptiveglobalrep[i];
			}
				else{
					finalreputation[i]=globarep[i];
			}
		}
		
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				nw.getUserRelation(i, j).setTrust(finalreputation[j]);
			}
		}
		
	}
	public void clearnormalizedrateList(){
		
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
