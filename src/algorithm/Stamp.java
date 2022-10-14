package algorithm;

/**
 *
 */

import java.util.ArrayList;

import utilities.Constant;
import utilities.Functions;

/**
 * @author Tiantian
 *
 */
public class Stamp {
	public int parId;

	public String R; // routes: timecost + "\t" + "ps" + "\t" + "d0" +....
	
	public double cost; // time cost

	public ArrayList<Integer> parList; // not visited partition list;

	public ArrayList<Integer> parList_visited = new ArrayList<>(); // visited partition list;

	public ParSet parSet;

	/**
	 * Constructor
	 *
	 */
	public Stamp(int parId, double cost, String R, ArrayList<Integer> parList, ArrayList<Integer> parList_visited) {
		this.parId = parId;
		this.cost = cost;
		this.R = R;
		this.parList = parList;
		this.parList_visited = parList_visited;
	}

	/**
	 * Constructor
	 *
	 */
	public Stamp(Stamp stamp) {
		this.parId = stamp.parId;
		this.cost = stamp.cost;
		this.R = stamp.R;

		this.parList = new ArrayList<>(stamp.parList);

		if (stamp.parSet != null)
			this.parSet = new ParSet(stamp.parSet);
	}

	/**
	 * set parId
	 *
	 * @param parId
	 */
	public void setParId(int parId) {
		this.parId = parId;
	}

	public int getParId() {
		return this.parId;
	}

	public void setR(String R) {
		this.R = R;
	}

	public String getR() {
		return this.R;
	}

//	public void setCost(double cost) {
//		this.cost = cost;
//	}
//
//	public double getCost() {
//		return this.cost;
//	}

	public void setParList(ArrayList<Integer> parList) {
		this.parList = parList;
	}

	public ArrayList<Integer> getParList() {
		return this.parList;
	}

	public void setParList_visited(ArrayList<Integer> parList_visited) {
		this.parList_visited = parList_visited;
	}

	public void addPar_visited(int par_visited) {
		this.parList_visited.add(par_visited);
	}

	public ArrayList<Integer> getParList_visited() {
		return this.parList_visited;
	}

	public boolean equals(Stamp another) {
		if (this.parId != another.parId)
			return false;
		if (this.cost != another.cost)
			return false;
		if (!this.R.equals(another.R))
			return false;
		if (!this.parList.equals(another.parList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String string=
		 "Stamp [parId=" + parId + ", cost=" + cost + ", parList=" + parList + ", parList_visited="
				+ parList_visited + "]";
		
		if(parSet!=null)
			string += ", parSet:" + parSet.toString();
		string += ", R=" + R;
		
		return string;
	}

}
