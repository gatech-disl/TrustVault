/**
 * Georgia Tech
 * DISL
 * 2016
 */


package trust_system_lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Scanner;

import core_lib.*;
import core_lib.User.Behavior;

/**
 * The TnaSlTM class conforms to the TrustAlg interface and implements the
 * 'Trust Network Analysis with Subjective Logic' approach of Josang et. al.
 */
public class TnaSlTM implements TrustAlg{
	
	// ************************** PRIVATE FIELDS *****************************

	/**
	 * The Network which this EigenTM is managing.
	 */
	private Network nw;
	
	/**
	 * Matrix storing persistent Opinion objects for each relation.
	 */
	private Opinion[][] op_stor;
	
	/**
	 * Matrix storing max-confidence Opinions seen during multiplication.
	 */
	private Opinion[][] max_matrix;
	
	/**
	 * Scratch space matrix for multiplication purposes.
	 */
	private Opinion[][] matrixA;
	
	/**
	 * Scratch space matrix for multiplication purposes.
	 */
	private Opinion[][] matrixB;
	
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct a TnaSlTM object.
	 * @param nw Network which this EtIncTM will be managing
	 */
	public TnaSlTM(Network nw){
		this.nw = nw;
		op_stor = new Opinion[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		matrixA = new Opinion[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		matrixB = new Opinion[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		max_matrix = new Opinion[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
				if(nw.getUser(j).isPreTrusted())
					op_stor[i][j] = new Opinion(0.0, 0.0, 1.0, 1.0);
				else
					op_stor[i][j] = new Opinion(0.0, 0.0, 1.0, 0.0);
			} // Initialize persistent Opinion values
		} // Do so for every relation in the network
	}

	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Interfaced: Text name of this trust algorithm (spaces are okay).
	 */
	public String algName(){
		return "TNA-SL";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "tnasl";
	}
	
	/**
	 * Interfaced: Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		
		int new_row = trans.getRecv();
		int new_vec = trans.getSend();
		
		int pos_fbacks = nw.getUserRelation(new_row, new_vec).getPos();
		int neg_fbacks = nw.getUserRelation(new_row, new_vec).getNeg();
		op_stor[new_row][new_vec].edit(pos_fbacks, neg_fbacks);	
		//System.out.print(op_stor[new_row][new_vec].expectedValue());
		//System.out.print(" b ::> "+ op_stor[new_row][new_vec].b);
		//System.out.print(" d ::> "+ op_stor[new_row][new_vec].d);
		//System.out.println(" u ::> "+ op_stor[new_row][new_vec].u);
	}
	
	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle){
		System.out.println("TNA-SL ComputeTrust");
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++)
				max_matrix[i][j] = op_stor[i][j];
		} // Initialize the 'max value' matrix for this run
		int maxIterCount=800;
		matrixA = subj_square(op_stor);
		while(matrixA != null){
			matrixB = subj_square(matrixA);
			if(matrixB == null)
				break;
			matrixA = subj_square(matrixB);
			--maxIterCount;
			if(maxIterCount<1){
				System.out.println("maxIterCount break");
				break;
			}
		} // Multiply until 'max_matrix' is saturated
		
		for(user=0; user < nw.GLOBALS.NUM_USERS; user++){
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				Relation rel = nw.getUserRelation(user, i);
				rel.setTrust(max_matrix[user][i].expectedValue());
			}
		} // Export Opinion expected values's as trust value
		System.out.println("\r\n Iteration Number is : "+(800-maxIterCount)+"\r\n\r\n");
		/*
		PrintWriter pw;
		try {
			pw = new PrintWriter(new File("trustvals_tnasl.txt"));
		pw.println();
		for(int i=0;i<nw.GLOBALS.NUM_USERS;++i){
			for (int j=0;j<nw.GLOBALS.NUM_USERS;++j){
				pw.printf("%.5f ",nw.getUserRelation(i, j).getTrust());
			}
			pw.println();
		}
		pw.flush();
		pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//System.out.println();
		//System.out.println("trust vals in cycle"+cycle+":");
		//for(int i=0; i < nw.GLOBALS.NUM_USERS; i++,System.out.println()){
		//	for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
		//		System.out.print(max_matrix[i][j].expectedValue()+" ");
		//	}
		//}
		//(new Scanner(System.in)).nextLine();
		
	}
	
	// ************************** PRIVATE METHODS ****************************

	/**
	 * Complete a single multiplication per the TNA-SL specification.
	 * @param source The Opinion matrix which is to be squared
	 * @return Resulting matrix if multiply improved trust, NULL otherwise
	 */
	private Opinion[][] subj_square(Opinion[][] source){
		Opinion op_holder; Opinion[][] dest;
		dest = new Opinion[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
				for(int k=0; k < nw.GLOBALS.NUM_USERS; k++){
					if(k==0)
						dest[i][j] = source[i][0].discount(source[0][j]);
					else{
						op_holder = source[i][k].discount(source[k][j]);
						dest[i][j] = dest[i][j].consensus(op_holder);
					} // Don't consensus first discount; it isn't monotonic
				} // The inner loop of matrix multiply
			} // Discount and then consensus Opinions
		}  // Perform 'multiply' over all user vectors and rows
		
		boolean max_modified = false;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
				if(dest[i][j].compareTo(max_matrix[i][j]) == 1){
					max_matrix[i][j] = dest[i][j].clone();
					max_modified = true;
				} // Flag flips if a copy to 'max_value' is made
			} // Update the 'max_value' matrix at each position
		} // Iterate over all matrix positions	

		if(max_modified)
			return dest;
		else 
			return null;
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

	@Override
	public double[] showtrust() {
		double[] ret = new double[nw.GLOBALS.NUM_USERS];
		for(int i=0;i<nw.GLOBALS.NUM_USERS;++i){
			int r=0;
			for(int j=0;j<nw.GLOBALS.NUM_USERS;++j){
				if(nw.getUser(j).getModel()==User.Behavior.USR_GOOD){
					ret[i]+=max_matrix[j][i].expectedValue();
					++r;
				}
			}
			ret[i]/=r;
		}
		return ret;
	}
}
