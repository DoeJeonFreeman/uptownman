package trunk.medm.mos.ensemble;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Provides percentile computation.
 * <p>
 * There are several commonly used methods for estimating percentiles (a.k.a. 
 * quantiles) based on sample data.  For large samples, the different methods 
 * agree closely, but when sample sizes are small, different methods will give
 * significantly different results.  The algorithm implemented here works as follows:
 * <ol>
 * <li>Let <code>n</code> be the length of the (sorted) array and 
 * <code>0 < p <= 100</code> be the desired percentile.</li>
 * <li>If <code> n = 1 </code> return the unique array element (regardless of 
 * the value of <code>p</code>); otherwise </li>
 * <li>Compute the estimated percentile position  
 * <code> pos = p * (n + 1) / 100</code> and the difference, <code>d</code>
 * between <code>pos</code> and <code>floor(pos)</code> (i.e. the fractional
 * part of <code>pos</code>).  If <code>pos >= n</code> return the largest
 * element in the array; otherwise</li>
 * <li>Let <code>lower</code> be the element in position 
 * <code>floor(pos)</code> in the array and let <code>upper</code> be the
 * next element in the array.  Return <code>lower + d * (upper - lower)</code>
 * </li>
 * </ol>
 * <p>
 * To compute percentiles, the data must be (totally) ordered.  Input arrays
 * are copied and then sorted using  {@link java.util.Arrays#sort(double[])}.
 * The ordering used by <code>Arrays.sort(double[]</code> is the one determined
 * by {@link java.lang.Double#compareTo(Double)}.  This ordering makes 
 * <code>Double.NaN</code> larger than any other value (including 
 * <code>Double.POSITIVE_INFINITY</code>).  Therefore, for example, the median
 * (50th percentile) of  
 * <code>{0, 1, 2, 3, 4, Double.NaN}</code> evaluates to <code>2.5.</code>  
 * <p>
 * Since percentile estimation usually involves interpolation between array 
 * elements, arrays containing  <code>NaN</code> or infinite values will often
 * result in <code>NaN<code> or infinite values returned.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If 
 * multiple threads access an instance of this class concurrently, and at least
 * one of the threads invokes the <code>increment()</code> or 
 * <code>clear()</code> method, it must be synchronized externally.
 * <p>
 * This code is taken from Jakarta Commons - since we only need this routine,
 * there's not much point in taking the entire library. It's unlikely that maths
 * would change...
 * <p>
 * (C) Apache Software Foundation 2003-2004.
 */
public class ComputePercentiles implements Serializable 
{
	/** Serializable version identifier */
	static final long serialVersionUID = -8091216485095130416L; 

	/** Determines what percentile is computed when evaluate() is activated 
	 * with no quantile argument */
	private double quantile = 0.0;

	/**
	 * Constructs a Percentile with a default quantile
	 * value of 50.0.
	 */
	public ComputePercentiles() {
		this(50.0);
	}

	/**
	 * Constructs a Percentile with the specific quantile value.
	 * @param p the quantile
	 * @throws IllegalArgumentException  if p is not greater than 0 and less
	 * than or equal to 100
	 */
	public ComputePercentiles(final double p) {
		setQuantile(p);
	}

	private void test( final double[] values, int start, int end )
	{
		if( start < 0 || start > values.length || end < start || end > values.length )
			throw new IllegalArgumentException("This is not a valid subrange");

	}
	/**
	 * Returns an estimate of the <code>p</code>th percentile of the values
	 * in the <code>values</code> array.
	 * <p>
	 * Calls to this method do not modify the internal <code>quantile</code>
	 * state of this statistic.
	 * <p>
	 * <ul>
	 * <li>Returns <code>Double.NaN</code> if <code>values</code> has length 
	 * <code>0</code></li>
	 * <li>Returns (for any value of <code>p</code>) <code>values[0]</code>
	 *  if <code>values</code> has length <code>1</code></li>
	 * <li>Throws <code>IllegalArgumentException</code> if <code>values</code>
	 *  is null </li>
	 * </ul>
	 * <p>
	 * See {@link Percentile} for a description of the percentile estimation
	 * algorithm used.
	 * 
	 * @param values input array of values
	 * @param p the percentile value to compute
	 * @return the result of the evaluation or Double.NaN if the array is empty
	 * @throws IllegalArgumentException if <code>values</code> is null
	 */
	public double evaluate(final double[] values, final double p) {
		test(values, 0, 0);
		return evaluate(values, 0, values.length, p);
	}

	/**
	 * Returns an estimate of the <code>quantile</code>th percentile of the
	 * designated values in the <code>values</code> array.  The quantile
	 * estimated is determined by the <code>quantile</code> property.
	 * <p>
	 * <ul>
	 * <li>Returns <code>Double.NaN</code> if <code>length = 0</code></li>
	 * <li>Returns (for any value of <code>quantile</code>) 
	 * <code>values[begin]</code> if <code>length = 1 </code></li>
	 * <li>Throws <code>IllegalArgumentException</code> if <code>values</code>
	 * is null,  or <code>start</code> or <code>length</code> 
	 * is invalid</li>
	 * </ul>
	 * <p>
	 * See {@link Percentile} for a description of the percentile estimation
	 * algorithm used.
	 * 
	 * @param values the input array
	 * @param start index of the first array element to include
	 * @param length the number of elements to include
	 * @return the percentile value
	 * @throws IllegalArgumentException if the parameters are not valid
	 * 
	 */
	public double evaluate( final double[] values, final int start, final int length) {
		return evaluate(values, start, length, quantile);
	}

	/**
	 * Returns an estimate of the <code>p</code>th percentile of the values
	 * in the <code>values</code> array, starting with the element in (0-based)
	 * position <code>begin</code> in the array and including <code>length</code>
	 * values.
	 * <p>
	 * Calls to this method do not modify the internal <code>quantile</code>
	 * state of this statistic.
	 * <p>
	 * <ul>
	 * <li>Returns <code>Double.NaN</code> if <code>length = 0</code></li>
	 * <li>Returns (for any value of <code>p</code>) <code>values[begin]</code>
	 *  if <code>length = 1 </code></li>
	 * <li>Throws <code>IllegalArgumentException</code> if <code>values</code>
	 *  is null , <code>begin</code> or <code>length</code> is invalid, or 
	 * <code>p</code> is not a valid quantile value</li>
	 * </ul>
	 * <p>
	 * See {@link Percentile} for a description of the percentile estimation
	 * algorithm used.
	 * 
	 * @param values array of input values
	 * @param p  the percentile to compute
	 * @param begin  the first (0-based) element to include in the computation
	 * @param length  the number of array elements to include
	 * @return  the percentile value
	 * @throws IllegalArgumentException if the parameters are not valid or the
	 * input array is null
	 */
	public double evaluate(final double[] values, final int begin, 
			final int length, final double p) {

		test(values, begin, length);

		if ((p > 100) || (p <= 0)) {
			throw new IllegalArgumentException("invalid quantile value: " + p);
		}
		if (length == 0) {
			return Double.NaN;
		}
		if (length == 1) {
			return values[begin]; // always return single value for n = 1
		}

		// Sort array
		double[] sorted = new double[length];
		System.arraycopy(values, begin, sorted, 0, length);
		Arrays.sort(sorted);

		return evaluateSorted( sorted, p );
	}

	private double evaluateSorted( final double[] sorted, final double p )
	{
		double n = sorted.length;
		double pos = p * (n + 1) / 100;
		double fpos = Math.floor(pos);
		int intPos = (int) fpos;
		double dif = pos - fpos;

		if (pos < 1) {
			return sorted[0];
		}
		if (pos >= n) {
			return sorted[sorted.length - 1];
		}
		double lower = sorted[intPos - 1];
		double upper = sorted[intPos];
		return lower + dif * (upper - lower);
	}

	public double evaluate( List<Double> list, int p )
	{
		if ((p > 100) || (p <= 0)) {
			throw new IllegalArgumentException("invalid quantile value: " + p);
		}

		if (list.size() == 0) {
			return Double.NaN;
		}
		if (list.size() == 1) {
			return list.get(0); // always return single value for n = 1
		}

		// Sort array.  We avoid a third copy here by just creating the
		// list directly.
		double[] sorted = new double[list.size()];
		for( int i = 0; i < list.size(); i++ )
		{
			sorted[i] = list.get(i);
		}
		Arrays.sort(sorted);

		return evaluateSorted( sorted, p );    
	}

	public double getMinMaxVal(List<Double> list, String flag){
		double[] sorted = new double[list.size()];
		for( int i = 0; i < list.size(); i++ ){
			sorted[i] = list.get(i);
		}
		Arrays.sort(sorted);
		double val = 0;
		if(flag.toUpperCase().equals("MIN")) val = sorted[0];
		else if(flag.toUpperCase().equals("MAX"))val = sorted[sorted.length-1];
		return val;
	}

	/**
	 * Returns the value of the quantile field (determines what percentile is
	 * computed when evaluate() is called with no quantile argument).
	 * 
	 * @return quantile
	 */
	public double getQuantile() {
		return quantile;
	}

	/**
	 * Sets the value of the quantile field (determines what percentile is 
	 * computed when evaluate() is called with no quantile argument).
	 * 
	 * @param p a value between 0 < p <= 100 
	 * @throws IllegalArgumentException  if p is not greater than 0 and less
	 * than or equal to 100
	 */
	public void setQuantile(final double p) {
		if (p <= 0 || p > 100) {
			throw new IllegalArgumentException("Illegal quantile value: " + p);
		}
		quantile = p;
	}

	public double getPTYPercentage(List<Double> list, int memberCount, String flag){
		double currNum=0;
		double counter=0;
		if(flag.equals("rain")){
			currNum = 1; 
		}else if(flag.equals("sleet")){
			currNum = 2; 
		}else if(flag.equals("snow")){
			currNum = 3; 
		}	
		for(double val : list){
			if(currNum==val){
				counter++;
			}
		}
		return (counter/memberCount)*100;
	}

	public double getCLDPercentage(List<Double> list, int memberCount, String flag){
		double valFrom = 0;
		double valTo = 0;
		double counter=0;
		if(flag.equals("clear")){
			valFrom = 0; valTo = 2.5;
		}else if(flag.equals("scattered")){
			valFrom = 2.5; valTo = 5;
		}else if(flag.equals("broken")){
			valFrom = 5; valTo = 7.5;
		}else if(flag.equals("overcast")){
			valFrom = 7.5; valTo = 10.1;
		}	
		for (double val : list){
			if(valFrom<=val && val<valTo){
				counter++;
			}
		}
		return (counter/memberCount)*100;
	}


	public static void main(String[] args){
		ComputePercentiles com = new ComputePercentiles(); //default median 50%ile
		List<Double> values = new Vector<Double>();
		//    	for(int i=10; i>1; i--){
		//    		values.add(new Double(i));
		//    	}
		values.add(new Double(25));
		values.add(new Double(36));
		values.add(new Double(27));
		values.add(new Double(20));
		values.add(new Double(80));
		values.add(new Double(29));
		values.add(new Double(100));
		values.add(new Double(31));
		values.add(new Double(150));
		values.add(new Double(88));
		values.add(new Double(99));

		System.out.println("[MIN]"+com.getMinMaxVal(values,"MIN"));
		System.out.println("[10%]"+com.evaluate(values,10));
		System.out.println("[25%]"+com.evaluate(values,25));
		System.out.println("[median]"+com.evaluate(values,50));
		System.out.println("[75%]"+com.evaluate(values,75));
		System.out.println("[90%]"+com.evaluate(values,90));
		System.out.println("[MAX]"+com.getMinMaxVal(values,"MAX"));
	}

}