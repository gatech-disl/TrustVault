/**
 * Georgia Tech
 * DISL
 * 2016
 */

package trust_system_lib;

/**
 * The Opinion class implements Subjective Logic opinions.
 */
public class Opinion implements Comparable<Opinion>{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * Belief field.
	 */
	public double b;
	
	/**
	 * Disbelief field.
	 */
	public double d;
	
	/**
	 * Uncertainty field.
	 */
	public double u;
	
	/**
	 * Base-rate field. For a-priori notions of trust.
	 */
	private double a;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct an Opinion by passing all field values.
	 * @param b Belief
	 * @param d Disbelief 
	 * @param u Uncertainty 
	 * @param a Base-rate
	 */
	public Opinion(double b, double d, double u, double a){
		this.b = b;
		this.d = d;
		this.u = u;
		this.a = a;
		return;
	}
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Update an Opinion using feedback data.
	 * @param pos_fbacks Number of positive feedbacks
	 * @param neg_fbacks Number of negative feedbacks
	 */
	public void edit(int pos_fbacks, int neg_fbacks){
		//System.out.println("pos,neg:"+pos_fbacks+":"+neg_fbacks);
		//System.out.println("in edit:"+this.b+":"+this.d+":"+this.u);
		this.b = ((double)1.0*pos_fbacks / (double)(pos_fbacks + neg_fbacks + 2.0));
		this.d = ((double)1.0*neg_fbacks / (double)(pos_fbacks + neg_fbacks + 2.0));
		this.u = ((double)1.0 - this.b -this.d);
		//System.out.println("out edit:"+this.b+":"+this.d+":"+this.u);
		//this.u = (2.0 / (pos_fbacks + neg_fbacks + 2.0));
		return;
	}
	
	/**
	 * Calculate the expected value of this Opinion. 
	 * @return The expected value of this Opinion
	 */
	public double expectedValue(){
		return (this.b + (this.a * this.u));
	}
	
	/**
	 * The discount operator used in the analysis of transitive chains.
	 * @param that Second Opinion in 'this -> that' chain. Order matters!
	 * @return The discounted Opinion.
	 */
	public Opinion discount(Opinion that){
		double belief = this.b * that.b;
		double disbelief = this.b * that.d;
		double uncertainty = (1.0 - belief - disbelief);
		//double uncertainty = this.d + this.u + (this.b * that.u);
		double alpha = that.a;
		return (new Opinion(belief, disbelief, uncertainty, alpha));
	}
	
	/**
	 * The consensus operator used in the 'fusion' of two Opinions.
	 * @param that The second Opinion being averaged with 'this' one.
	 * @return The fused Opinion.
	 */
	public Opinion consensus(Opinion that){
		double belief, disbelief, uncertainty;
		if((this.u == 0.0) && (that.u == 0.0)){
			belief = ((this.b + that.b) / 2.0);
			disbelief = (1.0 - belief);
			//disbelief = ((this.d + that.d) / 2.0);
			uncertainty = 0.0;
		} else{
			double denom = ((this.u + that.u) - (this.u * that.u));
			belief = (((this.b * that.u) + (that.b * this.u)) / denom);
			disbelief = (((this.d * that.u) + (that.d * this.u)) / denom);
			uncertainty = (1.0 - belief - disbelief);
			//uncertainty = ((this.u * that.u) / denom);
		} // Math differs based on uncertainty values
		double alpha = this.a;
		return (new Opinion(belief, disbelief, uncertainty, alpha));
	}
	
	/**
	 * Compare this Opinion to another Opinion object. Comparisons are made on
	 * the basis of confidence=(1-uncertainty). Ties are broken by belief.
	 * @param that The other Opinion being compared to 'this' one
	 * @return 1 if this > that, 0 if this == that, -1 otherwise
	 */
	public int compareTo(Opinion that){
		if(this.u < that.u)
			return (1);
		else if(this.u > that.u)
			return (-1);
		else{
			if(this.b > that.b)
				return (1);
			else if(this.b < that.b)
				return (-1);
			else
				return (0);
		} // Compare first by confidence, ties broken by belief.
	}
	
	/**
	 * Create a duplicate of this Opinion object.
	 * @return a duplicate of this Opinion object
	 */
	public Opinion clone(){
		return new Opinion(this.b, this.d, this.u, this.a);
	}
	
}
