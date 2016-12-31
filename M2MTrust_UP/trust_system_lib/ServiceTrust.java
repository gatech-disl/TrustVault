/**
 * Georgia Tech
 * 
 * 
 */



package trust_system_lib;




import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import sun.awt.image.ImageWatched.Link;

import core_lib.*;

/**
 * The EigenTM class conforms to the TrustAlg interface and implements the
 * EigenTrust algorithm as described by Hector Garcia-molina, et. al.
 */
public class ServiceTrust implements TrustAlg{
	
	// ************************* PROTECTED FIELDS ****************************
	
	
	/**
	 * Weighting constant making pre-trusted peers more powerful
	 */
	private final double ALPHA = 0.0;
	
	/**
	 * Acceptable error margin in convergence tests.
	 */
	private final double EPSILON = 0.001;
	/**
	 * The Network which this EigenTM is managing.
	 */
	protected Network nw;
	
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * Pre-trusted peer distribution and weighting vector
	 */
	private double[] pretrust;
	/*
	 *  the initial global trust for each peer
	 */
	private double [] inititrust;
	/**
	 * Matrix storing persistent normalized (pre-multiplication) values.
	 */
	private double[][] normalized;
	
	private double[][] adaptivenormalized;
	/**
	 * feedback credibility
	 */
	
	private double [][] similarityeigenfeedbk;
	private double simCre[][];  // using similarity to compute the feedback credibility
	/**
	 *  personal reputation 
	 */
	/**
	 * Scratch space vector for multiplication purposes.
	 */
	private double[] vectorA;
	
	private double [][] vectorAA;
	
	ArrayList <Double> normalizedrate[][];
	ArrayList<Double> avgrating[][];
	
	private double [][] similarity;
	
	private double [][] trustvector_similaroty;
	
	private double [][] personal_globalRep; // store the global trust value from the view point of each individual peer cik= sum cij*cjk.
	
	/*
	 * define the local trust matrix (normalmultsimi[][]) using normalied multiply similarity (Sikmilarity Credibility)
	 */
	private double normalmultsimi[][]; 
	/**
	 * Scratch space vector for multiplication purposes.
	 */
	private double[] vectorB;
	
	private double[][] vectorBB;

	private double [] globarep;
	
	private double [] adaptiveglobalrep;
	
	private double [] finalreputation;
	LinkedList<Double> [] Rep;
	LinkedList<Double> [] PeerID;
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct an EigenTM object.
	 * @param nw Network which this EigenTM will be managing
	 */
	public ServiceTrust(Network nw){
		this.nw = nw;
		
		pretrust = new double[nw.GLOBALS.NUM_USERS];
		globarep =new double [nw.GLOBALS.NUM_USERS];
		finalreputation =new double [nw.GLOBALS.NUM_USERS];
		adaptiveglobalrep =new double [nw.GLOBALS.NUM_USERS];
		normalized = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		vectorA = new double[nw.GLOBALS.NUM_USERS];
		vectorAA = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		vectorB = new double[nw.GLOBALS.NUM_USERS];
		vectorBB = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		adaptivenormalized =new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		similarityeigenfeedbk =new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
	    similarity= new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		normalizedrate = new ArrayList[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		avgrating=new ArrayList[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		personal_globalRep = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		trustvector_similaroty =new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		normalmultsimi =new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		simCre=new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		inititrust= new double [nw.GLOBALS.NUM_USERS];
		
		for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			for(int k=0;k<nw.GLOBALS.NUM_USERS;k++){
				similarityeigenfeedbk[j][k]=0;
				similarity[j][k]=0;
				trustvector_similaroty[j][k]=0;
				personal_globalRep[j][k]=0;
				normalizedrate[j][k]=new ArrayList();
				avgrating[j][k]=new ArrayList();
				simCre[j][k]=0;
			//	normalmultsimi[j][k]=0;
			}
		}
		Rep = new LinkedList [nw.GLOBALS.NUM_USERS];
		PeerID = new LinkedList [nw.GLOBALS.NUM_USERS];
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			Rep[i]=new LinkedList<Double>();
			PeerID[i]=new LinkedList<Double>();
		}
		
		for(int m=0;m<nw.GLOBALS.NUM_USERS;m++){
			finalreputation[m]=0;
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
		return "ServiceTrust_Eigen";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "ServiceTrust";
	}
	public double[] showtrust(){
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++)
		System.out.print("\n"+i+".trust= "+vectorA[i]+" ");
		return null;
	
	}
	/**
	 * Interfaced: Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		normalizeVector(trans.getRecv());
//		normalizeVector(trans.getSend());
		
		//normalizeVector(peer);
		//normalizeVector(trans.getSend());
	}
	
	
	public void adaptiveupdate(Transaction trans) {
		//adaptivenormalizeVector(peer);
		normalizeVector(trans.getRecv());
		normalizeVector(trans.getSend());
		
	}
	
	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle){
		//System.out.println("cycle= "+cycle);
	//	calpersonal_reputation(user, 1, cycle);
	//	calpersinal_reputation_similarity(cycle) ;
		calsimilarity(cycle);
	//	calfeedbkcredibility();
		calreputation(user, 100, cycle);
    	trustMultiply(user);
	}
	

	public void computeadptiveTrust(int user, int cycle) {
//		caladaptivereputation(user, 100);
    	//trustMultiply(user);
	}
	
	// ************************* PROTECTED METHODS ***************************
	
	/**
	 * Perform matrix multiply as a means of aggregating global trust data.
	 * @param user Identifier of user performing trust computation
	 * @param max_iters Maximum number of multiplications to perform
	 * @return The converged global trust vector
	 */
	protected void trustMultiply(int user){
		
   
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				nw.getUserRelation(i, j).setTrust(globarep[j]);
			}
		}
		
	}
	
	
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * Normalize a single vector of the persistent matrix.
	 * @param new_vec The vector to be normalized
	 */
	private void normalizeVector(int new_vec){
   
		double fback_int, normalizer = 0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			fback_int = calcGlobalFBackInt(nw.getUserRelation(new_vec, i), new_vec, i);
			normalizer += fback_int;
			normalized[new_vec][i] = fback_int;
		} // Calculate normalizing sum in first pass
		
		if(normalizer == 0){
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[new_vec][i] = pretrust[i];
		} else{ // If a user trusts no one, default to the pre_trust vector
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				normalized[new_vec][i] /= (normalizer*1.0);
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
	
	
	private void adaptivenormalizeVector(int new_vec){
		double fback_int, normalizer = 0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			fback_int = calcadaptiveGlobalFBackInt(nw.getUserRelation(new_vec, i));
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
	private double calcGlobalFBackInt(Relation rel, int receiver, int sender){
		double fback=0.0, avg_ReceivSend = 0.0 , avg_ReceivSend1=0.0, sum1=0.0, sum2=0.0, variation=0.0, sum_vari=0.0 ;
		double max_rating=0.0;
		
//		System.out.println("\r\nreceiver= "+receiver+", sender="+sender+ ", rel.getPos= "+nw.getUserRelation(receiver, sender).getPos()+", rel.getNeg= "+rel.getNeg());
		if(rel.getPos()+rel.getNeg()>0){
			avg_ReceivSend=(double)(rel.getPos()-rel.getNeg())/(double)(rel.getPos()+rel.getNeg());
//			avg_ReceivSend1=(double)(rel.getPos())/(double)(rel.getPos()+rel.getNeg());
		}else{
			avg_ReceivSend=avg_ReceivSend1=0.0;
		}
//		avgrating[receiver][sender].add(avg_ReceivSend1);
		avgrating[receiver][sender].add(avg_ReceivSend);
		
		variation = calvariation (receiver, sender);
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			sum_vari+=calvariation(i, sender);
		}
		
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			if(nw.getUserRelation(receiver, i).getPos()>0){
				max_rating=1.0;
				break;
			}else{
				max_rating=-1.0;
			}
		}		
		
		if(variation !=0){
			if(max_rating!=0 && sum_vari!=0){
				fback=(double)(variation*avg_ReceivSend*(rel.getPos()-rel.getNeg())) /(double) (max_rating *sum_vari);
			}else{
				fback=0.0;
			}
			
		}else{
			if(max_rating!=0){
				fback=(double)(avg_ReceivSend*(rel.getPos()-rel.getNeg())) / (double)max_rating ;
			}else{
				fback=0.0;
			}			
		}
		
		if(fback<0.0){
			fback=0.0;
		}
//		System.out.println("\r\nvariation= "+variation+", avg_ReceivSend= "+avg_ReceivSend+", max_rating= "+max_rating+", sum_vari= "+sum_vari);
//       System.out.println("the local trust computed by ServiceTrust s("+receiver+", "+sender+") is: "+fback);
		return fback;
	}
    
	private double calvariation (int a, int b){
		double sum1=0.0, sum2=0.0, vari=0.0, avgvalue;
		
		if(nw.getUserRelation(a, b).getPos()+nw.getUserRelation(a, b).getNeg()>0){
		    avgvalue=(double)(nw.getUserRelation(a, b).getPos()-nw.getUserRelation(a, b).getNeg())/(double)(nw.getUserRelation(a, b).getPos()+nw.getUserRelation(a, b).getNeg());

		}else{
			avgvalue=0.0;
		}
		
		for(int i=0; i<nw.getUserRelation(a, b).getPos();i++){
			   sum1+=(1.0-avgvalue)*(1.0-avgvalue);	 // based on the formula (8) in ServiceTrust paper, the tr(i,j)=1 in our setting because of the ratings are {1, -1}, but unlike {-1, 0, 1, 2, 3, 4, 5} in ServiceTrust 
			}
			for (int i=0; i<nw.getUserRelation(a, b).getNeg();i++){
				sum2+=(-1.0-avgvalue)*(-1.0-avgvalue);
			}
			if(nw.getUserRelation(a, b).getPos()+nw.getUserRelation(a, b).getNeg()>0){
				vari=(sum1+sum2)/(double)(nw.getUserRelation(a, b).getPos()+nw.getUserRelation(a, b).getNeg());
			}else{
				vari=0.0;
			}
			
			return vari;
			
	}
	private double 	calcadaptiveGlobalFBackInt(Relation rel){
		double fback_int=0;
		if(rel.getadaptivePos() + rel.getadaptiveNeg()>0){
		    fback_int = (double)(rel.getadaptivePos())/(double)(rel.getadaptivePos() + rel.getadaptiveNeg());
		}
		if(fback_int < 0)
			fback_int = 0;
		return fback_int;
	}
	
	public void calpersinal_reputation_similarity(int cycle){
		double sum;
		int num;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				sum=0;
				num=0;
				for(int k=0;k<nw.GLOBALS.NUM_USERS;k++){
					if((nw.getUserRelation(i, k).getPos()!=0 || nw.getUserRelation(i,k).getNeg()!=0) 
					&& ((nw.getUserRelation(j, k).getPos()!=0) || nw.getUserRelation(j, k).getNeg()!=0)){
					  sum += ((personal_globalRep[i][k]-personal_globalRep[j][k]))*((personal_globalRep[i][k]-personal_globalRep[j][k]));
					  num++;
					}
				}
				trustvector_similaroty[i][j]=1- Math.sqrt(sum/(double)nw.GLOBALS.NUM_USERS);
				if(num >= 1){
				  trustvector_similaroty[i][j]=1- Math.sqrt(sum/(double)num);
				}
			}
			
		}
		
//	 if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
//		System.out.println();
//		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//			System.out.println("PeerId= "+i+"'s trustvectorsimilarity: \n");
//			for(int j=0; j<nw.GLOBALS.NUM_USERS;j++){
//				//System.out.print("trustvectorsimilarity["+i+"]["+j+"]= "+ trustvector_similaroty[i][j]+"  ");
//				System.out.print(trustvector_similaroty[i][j]+" ");
//			}
//			System.out.println("\r\n");
//		  }
//		}
	}
		// Linear algebra methods; nothing really unique going on here
	public void calsimilarity(int cycle){
		
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			  System.out.println("\r\nthe similarity matrix is:");
			}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
//			  System.out.println("peerID= "+i+"'s localtrustsimilarity is: \n");
//			}
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				similarity[i][j]=similaritycredibility(i,j);
				if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				    System.out.print("localtrust_similarity["+i+"]["+j+"]= "+similarity[i][j]+"  ");
//					System.out.print(+similarity[i][j]+" ");
				}
			}
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			   System.out.println();
			}
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				simCre[i][j]=similarity[i][j];
			}
		}

		double sum=0;
		double[] sum_similarity =new double [nw.GLOBALS.NUM_USERS];
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			sum=0;
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				sum+=similarity[i][j];
			}
			sum_similarity[i]=sum;
//			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
//			System.out.println("sum_similarity["+i+"]= "+sum_similarity[i]);
//			}
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			if(sum_similarity[i]>0){
			  for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				  simCre[i][j]=similarity[i][j]/sum_similarity[i];
//				  if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
//				   System.out.print("simCre["+i+"]["+j+"]= "+simCre[i][j]+" ");
//				  }
			   }
			}else{
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					simCre[i][j]=0;
				}
			}
//			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
//			 System.out.println();
//			}
		}
		
	}
	
	/**
	 *  compute the credibility of feedback! 
	 */
	private double similaritycredibility(int m, int n){ 
		double miltisum1[] =new double [nw.GLOBALS.NUM_USERS];
		double miltisum2[] =new double [nw.GLOBALS.NUM_USERS];
		double miltisum=0.0, pos_similarity=0.0, neg_similarity=0.0, similarity;
		double tmp1=0.0, tmp2=0.0;
		
		/*
		 * for positive similarity computation
		 */
		
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			miltisum1[i]=miltisum2[i]=0;
	}
		int cout=0, cunt1, cunt2;
		for (int i=0; i<nw.GLOBALS.NUM_USERS;i++){
		 tmp1=tmp2=0.0; cunt1=cunt2=0;
		   if((nw.getUserRelation(m, i).getPos()!=0) && (nw.getUserRelation(n, i).getPos()!=0)){
			             miltisum1[i]+= (double)(nw.getUserRelation(m, i).getPos())/(double)(nw.getUserRelation(m, i).getPos()+nw.getUserRelation(m, i).getNeg()) ;
				         miltisum2[i]+= (double)(nw.getUserRelation(n, i).getPos())/(double)(nw.getUserRelation(n, i).getPos()+nw.getUserRelation(n, i).getNeg()) ;
			 /*
			   for(int j=0;j<avgrating[m][i].size();j++){
				    //	  tmp1+= (double) normalizedrate[m][i].get(j);
				    	  if(avgrating[m][i].get(j)!=0){ 
		                	  cunt1++;
		                	  tmp1+= avgrating[m][i].get(j);
					    	  }
				    	 
				      }
				      for(int k=0;k<avgrating[n][i].size();k++){
				    //	  tmp2+= (double) normalizedrate[n][i].get(k);
				    	  if(avgrating[n][i].get(k)!=0){
				    		  cunt2++;
				    		  tmp2+= avgrating[n][i].get(k);
				        } 	
				    	  
				      }
				   //     miltisum += (normalized[m][i]-normalized[n][i])*(normalized[m][i]-normalized[n][i]);
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
				*/	    	         
			   cout++;      
		   }
		}
		
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			miltisum+=(miltisum1[i]-miltisum2[i])*(miltisum1[i]-miltisum2[i]);
	        }
		
		if(cout >= 1){
		//  similarity =1- Math.sqrt(miltisum/(double)cout);
			pos_similarity =1- Math.sqrt(miltisum/(double)(1*cout));
		}
		else
		    pos_similarity =0.0;
		
		if(pos_similarity<0){
			System.out.println("miltisum="+miltisum+", cout="+cout);
		}
		
		
		/*
		 * for negative similarity computation 
		 */
		
		double sum=0.0, tmp3=0.0, tmp4=0.0;
		int count2=0;
		for (int i=0;i<nw.GLOBALS.NUM_FILES;i++){
			 if((nw.getUserRelation(m, i).getNeg()!=0 || nw.getUserRelation(m, i).getPos()!=0)&& 
					 (nw.getUserRelation(n, i).getNeg()!=0 || nw.getUserRelation(n, i).getPos()!=0 )){
				      tmp3=tmp4=1.0;
					  if(nw.getUserRelation(m, i).getNeg()!=0 || nw.getUserRelation(n, i).getNeg()!=0){
						  tmp3= (double)(nw.getUserRelation(m, i).getPos()-nw.getUserRelation(m, i).getNeg())/(double)(nw.getUserRelation(m, i).getPos()+nw.getUserRelation(m, i).getNeg());
					      tmp4= (double)(nw.getUserRelation(n, i).getPos()-nw.getUserRelation(n, i).getNeg())/(double)(nw.getUserRelation(n, i).getPos()+nw.getUserRelation(n, i).getNeg());
					 
					      if(tmp3*tmp4<=0){
					    	  sum+=1.0;
					      }else{
					    	  sum+=0.0;
					      }
					      count2++;
				 }				
			 }
			
		}
		
		if(count2!=0){
			neg_similarity=1-sum/(double)count2;
		}else{
			neg_similarity=0.0;
		}
		
		double pos_weight=0.95, neg_weight=1.0-pos_weight;
		similarity= pos_weight*pos_similarity+neg_weight*neg_similarity;
		if(similarity<0)
		System.out.println("similarity("+m+", "+n+")="+similarity+", pos_similarity("+m+", "+n+")= "+pos_similarity+", neg_similarity("+m+", "+n+")= "+neg_similarity);
		 
		return similarity;
		
	}
	
	
	public double [] calreputation(int user, int max_iters, int cycle){
		   int currt_iter=0;
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				System.out.println(" Before initial trust for each of pair of peers, the normalized rating-matrix is: ");
				showrating_matrix(cycle, normalized);
			}

			inititrust=inital_localtrust(cycle);

			rebuiltmatrix(cycle, normalized); //// compute the local trust by adding similarity-based feedback credibility 

			vectorA = singleMultiply(inititrust, cycle);
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
				
				vectorB = singleMultiply(vectorA, cycle);
				currt_iter++;
				max_iters--;
				if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
					showreputation(currt_iter, vectorB);
						}
				if(max_iters<=0){
					break;
				}
				vectorA = singleMultiply(vectorB, cycle);
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
			
			
//			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
//				nw.getUserRelation(user, i).setTrust(vectorA[i]);
//				System.out.print("\n"+i+".trust= "+vectorA[i]+" "+ "percent= "+ vectorA[i]*100/sum+"%");
//			} // Import trust values back into Object form, duplicating vector
//			System.out.print("\n");
			
			/*
			 *  the local trust rating (normalmultsimi) = normalized * similarity
			 */
			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
				System.out.println(" After initial trust for each of pair of peers, the normalized rating-matrix is: ");
				showrating_matrix(cycle, normalized);
			}
			
			for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					nw.getUserRelation(i, j).setTrust(vectorA[j]);
				}
			}
			
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				globarep[j]=vectorA[j];	
		//		System.out.println("globarep["+j+"]= "+globarep[j]);
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
				   System.out.println("\n peer "+i+"'s reputation change process int the entire transaction process"+" ("+nw.GLOBALS.NUM_TRANS+"): ");
					for(int j=0;j<PeerID[i].size();j++){
						System.out.print(PeerID[i].get(j)+" ");
					}
				    System.out.println();
				}
			//	System.out.println("accout= "+accout);
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
				System.out.println("\rthe reputation values for each peer are list as follows: ");
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
				 System.out.println("\r\nthe average reputation of malicious peers is: "+mali_sum/(double)count+", the number of malicious nodes is: "+count);	
			}
			
			return vectorA.clone();
		}
	
	
//	public void calpersonal_reputation(int user, int max_iters, int cycle){
//	   for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//		   personal_globalreputation_cal(i ,max_iters, cycle);
//	   }
//	   
//	 //  System.out.println("cut point!!");
//	}
	
	private void showSimCre_normalized(){
		System.out.println("\r\n");
		double sum;
		int[] cunt= new int[nw.GLOBALS.NUM_USERS];
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			cunt[i]=0;
		}
		int a, b=0;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			sum=0; a=0;
			System.out.println("For peer "+i+" its rating matrix relationship is: ");
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
			    sum+=normalized[i][j];
				System.out.print(normalized[i][j]+" ");
				if(normalized[i][j]!=0){
					a++;
				}
			}
			System.out.println(i+".Sim-basedCred_normalizedsum= "+sum);
			cunt[i]=a;
		}
		
		double summ=0;
		System.out.println();
		System.out.println("\rFor each peer, the rating percentage to other peers are: ");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.print((double)cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
			summ+=(double)cunt[i]/(double)nw.GLOBALS.NUM_USERS;
			if((double)cunt[i]/(double)nw.GLOBALS.NUM_USERS!=0){
				b++;
			}
		}
		System.out.println();
		System.out.println("The average rating percentage from the viewpoint of entire network is: " +summ/ (double) b);
	}
	
	/*
	 * show the matrix of rating between a pair of peers 
	 */
	
	private void showrating_matrix(int cycle, double[][] matrix){
	    int[] out_cunt= new int[nw.GLOBALS.NUM_USERS];
	    int[] in_cunt= new int [nw.GLOBALS.NUM_USERS];
		int a, c, d=0, b=0; double sumb=0, sumd=0;
		 DecimalFormat df = new DecimalFormat( "0.0000");
		 
		System.out.println("\rpeers' ratings are list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
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
				System.out.println(";");
			}
			
			System.out.println("\rpeers' out_ratings matrix (graph) (0-1-inf) for computing diameter is list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
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
				
				System.out.println(";");
			}
			
			System.out.println("\r\npeers' in_ratings matrix (graph) (0-1-inf) for computing diameter is list according to the order like (0-"+(nw.GLOBALS.NUM_USERS-1)+")");
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
				
				System.out.println(";");
			}
			
			double max_out_cunt=Double.MIN_VALUE;
		//	System.out.println("max_out_cunt= "+max_out_cunt);
			double min_out_cunt= Double.MAX_VALUE;
		//	System.out.println("min_out_cunt= "+min_out_cunt);
			int max_out_degree= 0, min_out_degree=0, max_in_degree=0, min_in_degree=0;
			double avg_out_degree=0.0, avg_in_degree=0.0;
			System.out.println("\rFor each peer, the out_rating percentage is from 0 to "+ (nw.GLOBALS.NUM_USERS-1));
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		   if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){  // only refer to the good peers
			 System.out.print((double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS+" ");
			 sumb+=(double)out_cunt[i]/(double)nw.GLOBALS.NUM_USERS;
			 if(out_cunt[i]!=0){
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
		 
		 
		 double max_in_cunt=Double.MIN_VALUE;
		 double min_in_cunt=Double.MAX_VALUE;
			System.out.println("\r\nFor each peer, the in_rating percentage is from 0 to "+ (nw.GLOBALS.NUM_USERS-1));
			 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			  if(nw.getUser(i).getModel()==User.Behavior.USR_GOOD || nw.getUser(i).isPreTrusted()){  // only refer to the good peers
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
			 
			 System.out.println("sum_out= "+sum_out+", cnt_out="+cnt_out+"; sum_in= "+sum_in+", cnt_in"+cnt_in);
			 avg_out_degree= (double)sum_out/cnt_out;
			 avg_in_degree= (double)sum_in/cnt_in;	
			 
		 System.out.println();
		 System.out.println("\r\n\rthe maximum outgoing degree is: "+ max_out_degree+ ", the maximum out_rating percentage is: " + max_out_cunt);
		 System.out.println("\n\rthe minimum outgoing degreeis: "+min_out_degree+", the minimum out_rating percentage is: " + min_out_cunt);
		 System.out.println("\n\rthe average outgoing degree is: "+avg_out_degree+", the average out_rating percentage is: " + (double)sumb/(double)b);
		 
		 System.out.println("\r\n\rthe maximum ingoing degree is: "+ max_in_degree+", the maximum in_rating percentage is: " + max_in_cunt);
		 System.out.println("\n\rthe minimum ingoing degree is: "+min_in_degree+", the minimum in_rating percentage is: " + min_in_cunt);
		 System.out.println("\n\rthe average ingoing degree is :"+ avg_in_degree+", the average in_rating percentage is: " + (double)sumd/(double)d);
		 
	}
	
private void showreputation(int iter, double []a){
		
		double sum=0;
	//	System.out.println("\r\nthe "+ iter+" times' repuatation values are: \n");
	    /*
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			sum+=a[i];
		}
		*/
     //   System.out.println("\r\nall the peers' reputation values when the iteration round = "+ iter+" :");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
	//		System.out.print(a[i]+" ");   //+", the weight ratio = "+a[i]*100/sum+"%");
			//double tmp=a[i];
			Rep[i].add(a[i]);
		}
		//   System.out.println("\r\n");
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

//  private void personal_globalreputation_cal(int user, int max_iters, int cycle){
//		int cur_iter=0;
//		double [][] badpeernormalized_init =new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
//	
//		vectorAA = personal_globalRep_singleMultiply(user, normalized);
////		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
////	     	showlocaltrusst(user);
////		  }
//	    cur_iter++;
//		max_iters--;
//		
////		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
////			//System.out.println("cut point!!");
////			showperson_GlobalRep(cur_iter, user, vectorAA);
////			}
//		do{ // Multiply until convergence or maximum iterations reached
//			if(max_iters<=0){
//				break;
//			}
//			vectorBB = personal_globalRep_singleMultiply(user, vectorAA);
//			max_iters--;
//			cur_iter++;
////			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
////			  showperson_GlobalRep(cur_iter, user, vectorBB);
////			  }
//			if(max_iters<=0){
//				break;
//				}
//			vectorAA = personal_globalRep_singleMultiply(user, vectorBB);
//			max_iters--;
//			cur_iter++;
////			if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
////				showperson_GlobalRep(cur_iter, user, vectorAA);
////				}
//		} while((max_iters > 0) && !personal_globalReep_hasConverged(user, vectorAA, vectorBB));
//		
////		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
////			System.out.println("\r\niteration rounds ="+ cur_iter);
////		}
//		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
//			personal_globalRep[user][i]=vectorAA[user][i];
//			
//		}
//		
//		
//	}

//	public double [] caladaptivereputation(int user, int max_iters){
//		vectorA = adaptivesingleMultiply(pretrust);
//		max_iters--;
//		do{ // Multiply until convergence or maximum iterations reached
//			vectorB = adaptivesingleMultiply(vectorA);
//			vectorA = adaptivesingleMultiply(vectorB);
//			max_iters -= 2;
//		} while((max_iters > 0) && !hasConverged(vectorA, vectorB));
//		
//		/*
//		double sum=0;
//		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
//			sum+=vectorA[i];
//		}
//		*/
//		
//		//for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
//			//nw.getUserRelation(user, i).setTrust(vectorA[i]);
//		//	System.out.print("\n"+i+".trust= "+vectorA[i]+" "+ "percent= "+ vectorA[i]*100/sum+"%");
//		//} // Import trust values back into Object form, duplicating vector
//		//System.out.print("\n");
//	/*	
//		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
//			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//				nw.getUserRelation(i, j).setTrust(vectorA[j]);
//			}
//		}
//		*/	
//		for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
//			adaptiveglobalrep[j]=vectorA[j];	
//			
//		}
//		
//		return vectorA.clone();
//	}

/*
	private double[] adaptivesingleMultiply(double[] prev_vector){
		double[] lhs = vectorMatrixMult(prev_vector);
		lhs = constantVectorMult((1-ALPHA), lhs);
		double[] rhs = constantVectorMult(ALPHA, pretrust);
		return (vectorAdd(lhs,rhs));	
	}
	*/
	/**
	 * Perform a single multiplication iteration per EigenTrust specification.
	 * @param prev_vector Result of the last multiplication iteration
	 * @return A vector closer to converged global trust than that passed in
	 */
	private double[] singleMultiply(double[] prev_vector, int cycl){
		double[] lhs = vectorMatrixMult(prev_vector, cycl);
		lhs = constantVectorMult((1-ALPHA), lhs);
		double[] rhs = constantVectorMult(ALPHA, pretrust);
		return (vectorAdd(lhs,rhs));	
	}
	
	
	private double[][] personal_globalRep_singleMultiply(int user, double[][] prev_vector){
		double[][] lhs = person_gloabRep_vectorMatrixMult(user, prev_vector, normalized);
		//lhs = constantVectorMult((1-ALPHA), lhs);
		//double[] rhs = constantVectorMult(ALPHA, pretrust);
	    //return (vectorAdd(lhs,rhs));	
		return lhs;
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
	
	protected boolean personal_globalReep_hasConverged(int user, double[][] vec1, double[][] vec2){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(Math.abs(vec1[user][i]-vec2[user][i]) > this.EPSILON)
				return false;
		} // Compare vector elements, examining delta change
		return true;
	}
	
	public void calfeedbkcredibility(){
		List<Integer> []user =new LinkedList[nw.GLOBALS.NUM_USERS];
		double sumsimilarity;
		double []sumsimieigen= new double[nw.GLOBALS.NUM_USERS];
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			user[i]=new LinkedList();
		}
		
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			sumsimilarity=0;
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
		//		if(nw.getUserRelation(i, j).getPos()!=0){
		//			user[i].add(j);
					sumsimilarity+=similarity[i][j];
	//			}
			}
			  sumsimieigen[i]=sumsimilarity;
		}
		
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			if(sumsimieigen[i]!=0){
			for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				  similarityeigenfeedbk[i][j]= similarity[i][j]/sumsimieigen[i];
			  }
			}else{
				for(int j=0;j<user[i].size();j++){
					similarityeigenfeedbk[i][j]= 0;
				  }
			}
				
		}
		
//		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
//			user[i].clear();
//		}
	}
	
	private void rebuiltmatrix(int cycle, double[][] matrix){
//		double localtrust[][]=new double [nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		double localsum[]=new double[nw.GLOBALS.NUM_USERS];
		double sum;
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			sum=0;
			for(int j=0;j<nw.GLOBALS.NUM_USERS; j++){
				sum+=matrix[i][j]*simCre[i][j];
			}
			localsum[i]=sum;
		}
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			if(localsum[i]>0){
			for(int j=0;j<nw.GLOBALS.NUM_USERS; j++){
	//			localtrust[i][j]=matrix[i][j]*similarityeigenfeedbk[i][j]/localsum[i];
				normalmultsimi[i][j]= (matrix[i][j]*simCre[i][j])/localsum[i];
			}
			}else{
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
	//			   localtrust[i][j]=0; 
				   normalmultsimi[i][j]=0;
				}
			}			
		}
				
		if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			System.out.println("\r\n the local trust value computed by initial local trust multiply normalzied similarity is as follows: ");
			for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
				for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
					System.out.print("normalmultsimi["+i+"]["+j+"]="+normalmultsimi[i][j]+" ");
				}
				System.out.println();
			}
		}

	}
	
	private double[] vectorMatrixMult(double[] vector, int cycle){
		
		double[] dest = new double[nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[i] = 0.0;
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
//				if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
//					System.out.print("normalmultsimi["+j+"]["+i+"]= "+normalmultsimi[j][i]+ ", vector["+j+"]= "+vector[j]+", simCre["+j+"]["+i+"]="+simCre[j][i]+", normalized["+j+"]["+i+"]= "+normalized[j][i]+"; "); 
//				}				
		     	dest[i] += (normalmultsimi[j][i]* vector[j]); 
			//	dest[i] += (matrix[j][i] * vector[j]);
			} // Inner loop of matrix-vector multiplication
//			if(cycle==nw.GLOBALS.NUM_TRANS+nw.GLOBALS.WARMUP-1){
//				System.out.println("\r\ndest["+i+"]= "+dest[i]+"\r\n");
//			}
			
		} // Outer loop of matrix-vector multiplication
		dest=normalization(dest);
		return dest;
	}
	
	
	private double[][] person_gloabRep_vectorMatrixMult(int user, double[][] vector, double[][] matrix){
		//System.out.println("user= "+user);
		double[][] dest = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
	
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[user][i] = 0.0;
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
		    // 	dest[i] += (matrix[j][i] *similarityeigenfeedbk[j][i]* vector[j]);
				dest[user][i] += (matrix[j][i] * vector[user][j]);
			} // Inner loop of matrix-vector multiplication
		} // Outer loop of matrix-vector multiplication
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
	
	public void clearnormalizedrateList(){
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			for(int j=0; j<nw.GLOBALS.NUM_USERS;j++){
				normalizedrate[i][j].clear();
			}
		}
	}


	public void adaptivetimewindow() {
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
	
	private void showperson_GlobalRep(int cur_iter, int user, double [][]a){
	
		System.out.println("\r\nthe "+cur_iter+"th iteration rounds, from "+user+"'s vew point, the global trust vector is: \n");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
		//System.out.print("Rep["+i+"]= "+a[user][i]+" ");
		System.out.print(a[user][i]+" ");
		}
		System.out.println();
		
	}
	
	private void showlocaltrusst(int user){
		System.out.println("\r\npeer " +user+"' local trust:");
		for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			System.out.print("the localtrust["+user+"]["+i+"]= "+ normalized[user][i]+" ");
		}
		System.out.println();
	}
	public int getmalipartner(int k){
		ArrayList<Integer> collection = new ArrayList<Integer>();
		for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			if(nw.getUser(i).getModel()!=User.Behavior.USR_GOOD){
				collection.add(i);
			}
		}
		
		int m;
		if(k!=collection.size()-1)
	     	m=collection.get(k+1);
		else
			m=collection.get(0);
		return m;
	}
	
	private double[] inital_localtrust( int cycle){
		double sum_ini; 
		double[] a=new double [nw.GLOBALS.NUM_USERS];
        
		/*
		 * each node i's initial reputation is calculated by normalized[i][j]*normalzied[j][i]
		 */
		/*
		 for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			 for(int j=0;j<nw.GLOBALS.NUM_USERS;j++){
				 a[i]+=normalized[j][i]*normalized[i][j];		
			 }
		 }
		 
	
		 double sum=0.0;
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 sum+=a[i];
			 }
		 
		 for(int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			 a[i]/=sum;
		 }
		 if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			 System.out.println("the initial global trsut values are: ");
			 for (int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 System.out.print(a[i]+" ");
			 }
			 System.out.println();
		 }
		 return a;
		 */
		
		 /*
		  * only the pretrusted nodes have nonzero reputation 
		  */
		
		 int PretrustedNodeNum=0;
		 for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			 if(nw.getUser(i).isPreTrusted()){
				 PretrustedNodeNum++;
			 }
		 }
		 for(int i=0; i<nw.GLOBALS.NUM_USERS;i++){
			 if(nw.getUser(i).isPreTrusted()){
				 a[i]=(double)1/PretrustedNodeNum;
			 }
		 }		 

		 if(cycle==nw.GLOBALS.WARMUP+nw.GLOBALS.NUM_TRANS-1){
			 System.out.println("the initial global trsut values are (the number of pre-trusted nodes is "+PretrustedNodeNum+"): ");
			 for (int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				 System.out.print(a[i]+" ");
			 }
			 System.out.println();
		 }
		 return a;
	  }
	 
		private double[] normalization( double a[]){
			  double sum=0;
			for (int i=0;i<nw.GLOBALS.NUM_USERS;i++){
			    sum+=a[i];
			}
			
			for (int i=0;i<nw.GLOBALS.NUM_USERS;i++){
				if(sum!=0)
			        a[i]/=sum;
				else
					a[i]=0;
			}
			
			return a;
		}
		@Override
		public void getDirectTrusted() {
			// TODO Auto-generated method stub
			
		}
}
