package algorithm;

import java.util.ArrayList;
import java.util.Arrays;

import indoor_entitity.IndoorSpace;
import indoor_entitity.Partition;
import utilities.DataGenConstant;

/**
 * 
 * @author harry
 *
 *         A set of partition
 */
public class ParSet implements Comparable<ParSet> {

	private short[] parSet; // -1 = not filled yet
	private double[] rel;
	private double SC = 0;
	private double relevance = 0;
	private double wTime = 0;
	private double totalCost = 0;
	private double alpha = AlgSSA.alpha;
	public String path = ""; // store the resulting feasible path

	public ParSet(int size) {
		// TODO Auto-generated constructor stub
		parSet = new short[size];
		rel = new double[size];

		for (int i = 0; i < size; i++) {
			parSet[i] = -1;
			rel[i] = 0;
		}
	}


	public ParSet(ParSet another) {

		parSet = new short[another.parSet.length];
		rel = new double[another.parSet.length];

		for (int i = 0; i < parSet.length; i++)
			parSet[i] = another.parSet[i];

		for (int i = 0; i < parSet.length; i++)
			rel[i] = another.rel[i];

		SC = another.SC;
		wTime = another.wTime;
		relevance = another.relevance;
		totalCost = calcTotalCost(another.parSet.length);

	}

	public short[] getParSet() {
		return parSet;
	}

	public void setPar(ArrayList<String> par, int i) {
		short parId = Short.parseShort(par.get(0));
		parSet[i] = parId;
		rel[i] = Double.parseDouble(par.get(1));
		// add SC, wTime and rele
		Partition partition = IndoorSpace.iPartitions.get(parId);
		SC += partition.getStaticCost();
		wTime += partition.getWaitTime();

		relevance += rel[i];

	}

	public void removePar(int i) {
		// TODO Auto-generated method stub
		int parId = parSet[i];
		Partition partition = IndoorSpace.iPartitions.get(parId);
		SC -= partition.getStaticCost();
		wTime -= partition.getWaitTime();
		relevance -= rel[i];

		parSet[i] = -1;

	}

	public int getPar(int i) {
		return parSet[i];
	}

	public double getSC() {
		return SC;
	}

	public double getRelevance() {
		return relevance;
	}

	public double getwTime() {
		return wTime;
	}

	public void setwTime(double wTime) {
		this.wTime = wTime;
	}


	public double calcTotalCost(int querySize) {
		
		if (parSet.length != querySize) {
			System.out.println("something wrong in calc total score!");
			return -1;
		}

		totalCost = alpha * (SC / (DataGenConstant.SC_MAX * querySize)) + (1.0 - alpha) * (1 - (relevance / querySize));

		return totalCost;
	}

	// assuming textual rel of each remaining key is 1
	public double calcTotalCostLB(int querySize) {
		if (parSet.length != querySize) {
			System.out.println("something wrong in calc total score!");
			return -1;
		}

		double relUB = 0;

		for (int i = 0; i < querySize; i++) {
			if (parSet[i] == -1)
				relUB += 1;
			else
				relUB += rel[i];
		}
		return alpha * (SC / (DataGenConstant.SC_MAX * querySize)) + (1.0 - alpha) * (1 - (relUB / querySize));
//		return a * (SC / (10 * querySize)) + 0;
	}

	// assuming textual rel of each remaining key is 1
	public double calcTotalCostLB2(ArrayList<ArrayList<ArrayList<String>>> canPars_all) {
		double a = AlgSSA.alpha;
		int querySize = canPars_all.size();
		if (parSet.length != querySize) {
			System.out.println("something wrong in calc total score!");
			return -1;
		}

		double relUB = relevance;		
		double SCLB = SC;

		
		for (int i = 0; i < querySize; i++) {
			if (parSet[i] == -1) {
				ArrayList<String> a0 = canPars_all.get(i).get(0);
				Partition p = IndoorSpace.iPartitions.get(Integer.parseInt(a0.get(0)));
				SCLB +=(double) p.getStaticCost();
				relUB += Double.parseDouble(a0.get(1));
			}
		}
//		System.out.println(SC + " " + SCLB + " " + relevance + " "+ relUB);
		return a * (SCLB / (DataGenConstant.SC_MAX * querySize)) + (1.0 - a) * (1 - (relUB / querySize));
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		ParSet b = new ParSet(this);
		return b;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String s = "";
		for (int i = 0; i < parSet.length; i++)
			s += parSet[i] + ", ";
		s += " SC:" + SC + " wTime:" + wTime + " rel:" + relevance + " totalCost:" + totalCost;
		return s;
	}

	@Override
	/**
	 * sorting
	 */
	public int compareTo(ParSet anotherParSet) {
		// TODO Auto-generated method stub
		if (anotherParSet == null)
			return 1;

		if (totalCost > anotherParSet.totalCost)
			return 1;
		else if (totalCost == anotherParSet.totalCost)
			return 0;
		else
			return -1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParSet other = (ParSet) obj;
		if (!Arrays.equals(parSet, other.parSet))
			return false;

		return true;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalScore(double totalScore) {
		this.totalCost = totalScore;
	}

}
