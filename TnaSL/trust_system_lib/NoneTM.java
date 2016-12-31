/**
 * Georgia Tech
 * DISL
 * 2016
 */


package trust_system_lib;

import java.util.LinkedList;

import core_lib.*;

/**
 * The NoneTM class conforms to the TrustAlg interface and simulates the lack
 * of a TM system. Essentially, random source selection is used.
 */
public class NoneTM implements TrustAlg{
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
	private final double EPSILON = 0.001;
	
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
	LinkedList<Double> [] Rep;
	
	LinkedList<Double> [] PeerID;	
	int flag_cycle=-1; // judge whether the cycle is overlapping 
	
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct a NoneTm object.
	 * @param nw Network which this EtIncTM will be managing
	 */
	public NoneTM(Network nw){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
				nw.getUserRelation(j, i).setTrust(0.0);
			} // Initialize all trust to an identical value 
		} // Do so for every relation in the network
		
		this.nw = nw;
		pretrust = new double[nw.GLOBALS.NUM_USERS];
		vectorA = new double[nw.GLOBALS.NUM_USERS];
		vectorB = new double[nw.GLOBALS.NUM_USERS];
		normalized = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		
		Rep = new LinkedList [nw.GLOBALS.NUM_USERS];
		PeerID =new LinkedList [nw.GLOBALS.NUM_USERS];
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
			
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++)
				normalized[j][i] = pretrust[i];
		} // Initialize pre-trusted vector, and persistent normalized values
		
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Interfaced: Text name of this trust algorithm (spaces are okay).
	 */
	public String algName(){
		return "None";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "none";
	}
	
	/**
	 * Interfaced: Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		// Do nothing, let values persist
//		normalizeVector(trans.getRecv());
//		normalizeVector(trans.getSend());
	}

	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle){
		// Do nothing, let values persist
//		trustMultiply(user, 1, cycle);
	}
	
	protected double[] trustMultiply(int user, int max_iters, int cycle){
		int currt_iter=0;
		
//		if(max_iters==1){
//			inital_localtrust();
//		}
//		
		vectorA = singleMultiply(pretrust);
//		int total_num=max_iters;
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
		//	showreputation(currt_iter, vectorB);
			if(max_iters<=0){
				break;
			}
			vectorA = singleMultiply(vectorB);
			max_iters--;
			currt_iter++;
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			    showreputation(currt_iter, vectorA);
				}
		//	showreputation(currt_iter, vectorA);
		} while((max_iters > 0) && !hasConverged(vectorA, vectorB));
		
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
		  System.out.print("\r\n the iteration number is :" + currt_iter);
		}
		
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
	    	showRepchange();
		}
		
//		double sum=0;
//		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
//			sum+=vectorA[i];
//		}
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
		    System.out.println("\r\nwhen the amount of transaction is: "+ (cycle-nw.GLOBALS.WARMUP+1));
	    	double sum=0;
		    for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			  sum+=vectorA[i];
		         }
		    System.out.println("all the peers's reputation values are (summation = 1.0): ");
		    for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			    
			    System.out.print(vectorA[i]/sum +" ");
		           } // Import trust values back into Object form, duplicating vector
		        System.out.print("\n");
		    }
		
		
		if(cycle%100==0 && cycle!=flag_cycle){
			//System.out.println("cycle= "+cycle);
			//   System.out.print("\r\nat the"+ cycle+"th cycle computation, the wight of reputation each peers is described as: \r\n");
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 //   System.out.print("\n"+i+".trust= "+vectorA[i]+" "+ "percent= "+ vectorA[i]*100/sum+"%");
				//System.out.print(vectorA[i]/sum+" ");
			    PeerID[i].add(vectorA[i]);
			    
			}
			flag_cycle=cycle;
			}
	
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			   System.out.print("\r\nreputation change for each peers is described as: \r\n");
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			   System.out.println("\n peer "+i+"'s reputation change process int the entire transaction process"+" ("+nw.GLOBALS.NUM_TRANS+"): ");
				for(int j=0;j<PeerID[i].size();j++){
					System.out.print(PeerID[i].get(j)+" ");
				}
			    System.out.println();
			}
		//	System.out.println("accout= "+accout);
		}
		
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			showrating_matrix(cycle, normalized);
		}
		
		
//		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
//			nw.getUserRelation(user, i).setTrust(vectorA[i]);
//			
//	     	
//		} // Import trust values back into Object form, duplicating vector
//		//System.out.print("\n");
		return vectorA.clone();
	}
	
	
	private void showrating_matrix(int cycle, double[][] matrix){
	    int[] cunt= new int[nw.GLOBALS.NUM_USERS];
		 int a, b=0; double sum=0;
		System.out.println("\rpeers' ratings are list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
			{   a=0;
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					System.out.print(matrix[i][j]+" "); // peer i's rating to j (i gives rating to j)
					if(matrix[i][j]!=0){
						a++;
					}	
				}
				cunt[i]=a;
				System.out.println();
			}
			 
			System.out.println("\rFor each peer, the rating percentage is from 0 to "+ (nw.GLOBALS.NUM_USERS-1));
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 System.out.print((double)cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
			 sum+=(double)cunt[i]/(double)nw.GLOBALS.NUM_USERS;
			 if(cunt[i]!=0){
				 b++;
			 }	 
		 }
		 System.out.println();
		 System.out.println("\n\rthe average rating percentage is: " + (double)sum/(double)nw.GLOBALS.NUM_USERS);
		 
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
	
	private void showreputation(int iter, double []a){
		
		double sum=0;
	//	System.out.println("\r\nthe "+ iter+" times' repuatation values are: \n");
	    /*
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			sum+=a[i];
		}
		*/
        System.out.println("\r\nall the peers' reputation values when the iteration round = "+ iter+" :");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.print(a[i]+" ");   //+", the weight ratio = "+a[i]*100/sum+"%");
			//double tmp=a[i];
			Rep[i].add(a[i]);
		}
		   System.out.println("\r\n");
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
	
	private void showRepchange(){
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.println("\r\npeerID= "+i+"' reputation change during the iterations");
			for(int j=0;j<Rep[i].size();j++){
				System.out.print(Rep[i].get(j)+" ");
			}
			System.out.println();
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			Rep[i].clear();
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
		dest=normlization(dest);
		return dest;
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
	
	/**
	 * Normalize a single vector of the persistent matrix.
	 * @param new_vec The vector to be normalized
	 */
	private void normalizeVector(int new_vec){
		int fback_int, normalizer = 0;
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
	private int calcGlobalFBackInt(Relation rel){
		int fback_int = rel.getPos() - rel.getNeg();
		if(fback_int < 0)
			fback_int = 0;
		return fback_int;
	}
	
	private double[] normlization(double[] a){
		double sum=0;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			sum+=a[i];
		}
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			a[i]/=sum;
		}
		return a;
	}
	
	 private void inital_localtrust(){
		  
//		  System.out.println("before initialization (ask friends' rating), the local trust ratings are: ");
//		  for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
//			  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//				  System.out.print(normalized[i][j]+" ");
//			  }
//			  System.out.println();
//		  }
		  double tmp_normalized[][]=new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		  
		 for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				 if(normalized[i][j]==0){
				 for(int k=0;k<nw.GLOBALS.NUM_USERS;k++){	 
						 if(normalized[i][k]!=0 && normalized[k][j]!=0){
					       tmp_normalized[i][j]+=normalized[i][k]*normalized[k][j]; 
						 }
					 }
				 }
			 }
		 }
 
		 double sum;
		 double[] sum_normalized =new double[nw.GLOBALS.NUM_USERS];
		 
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 sum=0;
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				 if(normalized[i][j]==0)
				 sum+=tmp_normalized[i][j];
				 else
				 sum+=normalized[i][j];
			 }
			 sum_normalized[i]=sum;
		//	 System.out.println("sum_normalized["+i+"]= "+sum_normalized[i]);
		 }
			 
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			// System.out.println("sum_normalized["+i+"]= "+sum_normalized[i]);
			 if(sum_normalized[i]!=0.0){
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				 if(normalized[i][j]==0)
				    normalized[i][j]=tmp_normalized[i][j]/sum_normalized[i];
				 else
					normalized[i][j]=normalized[i][j]/sum_normalized[i];
			//	 System.out.println("normalized["+i+"]["+j+"]= "+normalized[i][j]);
			 }
			 }else{
				 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					 normalized[i][j]=0.0;
				 } 
			 }
		 }
		 
//		 double aftersum;
//		 System.out.println("after initialization (ask friends' rating), the local trust ratings are: ");
//		 for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
//			   aftersum=0;
//			  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//				  System.out.print(normalized[i][j]+" ");
//				  aftersum+=normalized[i][j];
//			  }
//			  System.out.println(i+".aftersum= "+aftersum);
//		  }
		 
	  }

	@Override
	public void adaptiveupdate(Transaction trans) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeadptiveTrust(int user, int cycle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void adaptivetimewindow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearnormalizedrateList() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void getDirectTrusted() {
		double minn=Integer.MAX_VALUE, maxx=0, avg=0;
		double cur;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;++i){
			cur=0;
			for(int j=0;j<nw.GLOBALS.NUM_USERS;++j){
				cur+= nw.getUserRelation(i, j).getPos()+nw.getUserRelation(i, j).getNeg()>0? 1:0;
			}
			minn = Math.min(minn,cur);
			maxx = Math.max(maxx,cur);
			avg+=cur;
		}
		avg/=nw.GLOBALS.NUM_USERS;
		nw.STATS.minDirectDegree=minn;
		nw.STATS.maxDirectDegree=maxx;
		nw.STATS.avgDirectDegree=avg;
	}

	@Override
	public double[] showtrust() {
		// TODO Auto-generated method stub
		return null;
	}
}