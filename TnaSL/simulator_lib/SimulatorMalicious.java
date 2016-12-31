/**
 * Georgia Tech
 * DISL
 * 2016
 */

package simulator_lib;

import java.util.*;
import trust_system_lib.*;
import core_lib.*;

/**
 * The SimulatorMalicious class is used to coordinate malicious user behaviors,
 * especially with regards to coordinated activity and feedback switching.
 */
public class SimulatorMalicious{
	
	// ************************* PRIVATE FIELDS *****************************

	/**
	 * The MAL_STRATEGY enumeration lists malicious tactics that can be used
	 */
	public enum MAL_STRATEGY{NAIVE, ISOLATED, COLLECTIVE, DISGUISE, SPY}; 
	
	/**
	 * The malicious strategy being applied by this instance.
	 */
	private final MAL_STRATEGY strat;
	
	/**
	 * Network which this SimulatorMalicious object is being applied too.
	 */
	private final Network nw;
	
	/**
	 * List containing UserID's of those participating in malicious-collective
	 */
	private List<Integer> collective;
	/**
	 * List containing UserID's of those participating in malicious-disguise
	 */
	private List<Integer> disguise;
	
	/** List containing UserID's of those participating in malicious-disguise
	 */
	private List<Integer> spy;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct a SimulatorMalicious object.
	 * @param nw Network over which this Object should operate
	 * @param strat Malicious strategy being applied in this instance
	 */
	public SimulatorMalicious(Network nw, MAL_STRATEGY strat){
		this.nw = nw;
		this.strat = strat;
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Compute trust according to some algorithm, over a set of feedback data.
	 * Which data is set is used is set according to this object.
	 * @param recv Identifier of user performing trust computation
	 * @param cycle The current cycle in the simulator framework
	 * @param ALG Algorithm being brought to bear on interaction data
	 */
	public void computeTrust(int recv, int cycle, TrustAlg ALG){
		
		if(cycle == 0) // We have to get this init'ed before update called
			ALG.computeTrust(recv, cycle);
		
		if(this.strat == MAL_STRATEGY.NAIVE)
			ALG.computeTrust(recv, cycle);
		
		else if(this.strat == MAL_STRATEGY.ISOLATED){	
			if(nw.getUser(recv).getModel() != User.Behavior.USR_GOOD){
				this.setVecRelations(Relation.Copy.HONEST, recv, ALG, cycle);
				ALG.computeTrust(recv, cycle);
				this.setVecRelations(Relation.Copy.GLOBAL, recv, ALG, cycle);
			} else
				ALG.computeTrust(recv, cycle);		
		} 
		else if(this.strat == MAL_STRATEGY.COLLECTIVE){
			if(cycle == 0){
				collective = new ArrayList<Integer>();
				for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
					if(nw.getUser(i).getModel() != User.Behavior.USR_GOOD)
						collective.add(i);		
				} // Add all non-'good' users to a collective
			} else{
				if(nw.getUser(recv).getModel() != User.Behavior.USR_GOOD){
					int cur;
					for(int i=0; i < collective.size(); i++){
						cur = collective.get(i);
						this.setVecRelations(Relation.Copy.HONEST, cur, ALG, cycle);
					} // Get all malicious peers to share honest data
					ALG.computeTrust(recv, cycle);	
					for(int i=0; i < collective.size(); i++){
						cur = collective.get(i);
						this.setVecRelations(Relation.Copy.GLOBAL, cur, ALG, cycle);
					} // Switch all settings back before exit
				} else
					ALG.computeTrust(recv, cycle);
			} // Setup strategy the first time, apply thereafter
		} // Change data set according to malicious strategy
		
		else if (this.strat==MAL_STRATEGY.DISGUISE){
			if(cycle == 0){
				//System.out.println("cycle= "+cycle);
				disguise = new ArrayList<Integer>();
				for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
					if(nw.getUser(i).getModel() != User.Behavior.USR_GOOD)
						disguise.add(i);	
					
				} // Add all non-'good' users to a collective
				//System.out.println("disguise.size= "+disguise.size());
			} else{
				if(nw.getUser(recv).getModel() != User.Behavior.USR_GOOD){
					int cur;
					//System.out.println("disguise~~.size= "+disguise.size());
					for(int i=0; i < disguise.size(); i++){
						cur = disguise.get(i);
						this.setVecRelations(Relation.Copy.HONEST, cur, ALG, cycle);
					} // Get all malicious peers to share honest data
					ALG.computeTrust(recv, cycle);	
					for(int i=0; i < disguise.size(); i++){
						cur = disguise.get(i);
						this.setVecRelations(Relation.Copy.GLOBAL, cur, ALG, cycle);
					} // Switch all settings back before exit
				} else
					ALG.computeTrust(recv, cycle);
			} // Setup strategy the first time, apply thereafter
				
			}
		else if(this.strat==MAL_STRATEGY.SPY){
			if(cycle == 0){
				spy = new ArrayList<Integer>();
				for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
					if(nw.getUser(i).getModel() != User.Behavior.USR_GOOD)
						spy.add(i);		
				} // Add all non-'good' users to a collective
			} else{
				if(nw.getUser(recv).getModel() != User.Behavior.USR_GOOD){
					int cur;
					for(int i=0; i < spy.size(); i++){
						cur = spy.get(i);
						this.setVecRelations(Relation.Copy.HONEST, cur, ALG, cycle);
					} // Get all malicious peers to share honest data
					ALG.computeTrust(recv, cycle);	
					for(int i=0; i < spy.size(); i++){
						cur = spy.get(i);
						this.setVecRelations(Relation.Copy.GLOBAL, cur, ALG, cycle);
					} // Switch all settings back before exit
				} else
					ALG.computeTrust(recv, cycle);
			} // Setup strategy the first time, apply thereafter	
			
		}
		
		
	}
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * Set all relations to report a particular feedback type when queried.
	 * @param setting Feedback type which we want to be reported
	 */ /*
	private void setAllRelations(Relation.Copy setting, TrustAlg ALG){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			for(int j=0; j <nw.GLOBALS.NUM_USERS; j++){
				nw.getUserRelation(i, j).setHistory(setting);
				ALG.update(new Transaction(-1, i, j, -1, false));
			} // Appropriate TM structures must update at each change
		} // Set all relations to the desired feedback setting
	}*/
	
	/**
	 * Set all (user->x) relations in this Network to report a particular 
	 * feedback type when queried, where 'user' is a fixed User.
	 * @param setting Feedback type which we want to be reported
	 * @param vec User (numerical) vector whose relations are to be set 
	 */
	private void setVecRelations(Relation.Copy setting, int vec, TrustAlg ALG, int cycle){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			nw.getUserRelation(vec, i).setHistory(setting);
		//	if (cycle<10)
		//	System.out.print(cycle+ "_vec1= "+ vec+ "_vec2= "+i+" ");
		//	ALG.update(new Transaction(-1, i, vec, -1, false));
		} // Set all relations in some vector to a desired feedback setting
	} 

}
