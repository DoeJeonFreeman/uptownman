


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ComputePercentiles implements Serializable {
	static final long serialVersionUID = -8091216485095130416L; 

	private double quantile = 0.0;

	public ComputePercentiles() {
		this(50.0);
	}

	public ComputePercentiles(final double p) {
		setQuantile(p);
	}

	private void test( final double[] values, int start, int end ){
		if( start < 0 || start > values.length || end < start || end > values.length )
			throw new IllegalArgumentException("This is not a valid subrange");

	}
	
	public double evaluate(final double[] values, final double p) {
		test(values, 0, 0);
		return evaluate(values, 0, values.length, p);
	}

	public double evaluate( final double[] values, final int start, final int length) {
		return evaluate(values, start, length, quantile);
	}

	
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
			return values[begin]; 
		}

		// Sort array
		double[] sorted = new double[length];
		System.arraycopy(values, begin, sorted, 0, length);
		Arrays.sort(sorted);

		return evaluateSorted( sorted, p );
	}

	private double evaluateSorted( final double[] sorted, final double p ){
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

	public double evaluate( List<Double> list, int p ){
		if ((p > 100) || (p <= 0)) {
			throw new IllegalArgumentException("invalid quantile value: " + p);
		}

		if (list.size() == 0) {
			return Double.NaN;
		}
		if (list.size() == 1) {
			return list.get(0); 
		}

		double[] sorted = new double[list.size()];
		for( int i = 0; i < list.size(); i++ ){
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

	public double getQuantile() {
		return quantile;
	}

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
}