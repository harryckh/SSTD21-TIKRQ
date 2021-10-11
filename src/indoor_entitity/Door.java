/**
 * 
 */
package indoor_entitity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import utilities.DataGenConstant;

/**
 * <h1>Door</h1>
 * to describe a door 
 * 
 * @author feng zijin, Tiantian Liu
 *
 */
public class Door extends Point implements Comparable<Object>{
	private int mID;		// the door ID
	
	private ArrayList<Integer> mPartitions = new ArrayList<Integer>();	// the accessible partitions
	
	private ArrayList<Integer> mFloors = new ArrayList<Integer>();	// the accessible floors
	
	private ArrayList<Direction> mDirections = new ArrayList<Direction>();		// the direction of the door

	/**
	 * Constructor
	 * 
	 * @param x
	 * @param y
	 * @param mType
	 */
	public Door(double x, double y, int mType) {
		super(x, y, DataGenConstant.mID_Floor, mType);
		this.mID = DataGenConstant.mID_Door++;
	}
	
	public Door(double x, double y, int mType, double oTime, double cTime) {
		super(x, y, DataGenConstant.mID_Floor, mType);
		this.mID = DataGenConstant.mID_Door++;
	}

	public Door(double x, double y) {
		super(x, y);
		this.mID = DataGenConstant.mID_Door++;
	}
	
	/**
	 * Constructor
	 * 
	 * @param another is a point
	 */
	public Door(Point another) {
		super(another);
		this.setmType(another.getmType());
	}
	
	/**
	 * add a accessible partition for this door
	 * 
	 * @param parID
	 */
	public void addPar(int parID) {
		if (this.mPartitions != null) {
			if (!this.mPartitions.contains(parID)) {
				this.mPartitions.add(parID);
			}
		}
	}
	
	/**
	 * add a accessible floor for this door
	 * 
	 * @param floorID
	 */
	public void addFloor(int floorID) {
		if (!this.mFloors.contains(floorID)) {
			this.mFloors.add(floorID);
		}
	}

	/**
	 * @return the mID
	 */
	public int getmID() {
		return mID;
	}

	/**
	 * @param mID
	 *            the mID to set
	 */
	public void setmID(int mID) {
		this.mID = mID;
	}

	/**
	 * @return the mPartitions
	 */
	public ArrayList<Integer> getmPartitions() {
		return mPartitions;
	}
	
	/**
	 * @param mPartitions
	 *            the mPartitions to set
	 */
	public void setmPartitions(ArrayList<Integer> mPartitions) {
		this.mPartitions = mPartitions;
	}

	/**
	 * @return the mFloors
	 */
	public ArrayList<Integer> getmFloors() {
		return mFloors;
	}

	/**
	 * @param mFloors
	 *            the mFloors to set
	 */
	public void setmFloors(ArrayList<Integer> mFloors) {
		this.mFloors = mFloors;
	}
	
	/**
	 * @param direction
	 *            the direction to add
	 */
	public void addDirection(Direction direction) {
		this.mDirections.add(direction);
	}
	
	/**
	 * @return the mDirections
	 */
	public ArrayList<Direction> getmDirections() {
		return mDirections;
	}
	
	/**
	 * @param mDirections
	 *            the mDirections to set
	 */
	public void setmDirections(ArrayList<Direction> mDirections) {
		this.mDirections = mDirections;
	}
	
	/**
	 * @return a list of partition id that one can leave through the door
	 */
	public ArrayList<Integer> getD2PLeave(){
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for (int i = 0; i < this.mDirections.size(); i ++) {
			if (!result.contains(this.mDirections.get(i).getsID())) result.add(this.mDirections.get(i).getsID());
		}
		
		return result;
	}
	
	/**
	 * @return a list of partition id that one can enter through the door
	 */
	public ArrayList<Integer> getD2PEnter(){
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for (int i = 0; i < this.mDirections.size(); i ++) {
			if (!result.contains(this.mDirections.get(i).geteID())) result.add(this.mDirections.get(i).geteID());
		}
		
		return result;
	}


	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		Door otherdoor = (Door) arg0;
		return this.mID > otherdoor.mID ? 1 : (this.mID == otherdoor.mID ? 0
				: -1);
	}
	
	/**
	 * toString
	 * 
	 * @return mID+x+y+mFloor+mPars
	 */
	public String toString() {
		String outputString = this.getmID() + "\t" + this.getX() + "\t"
				+ this.getY() + "\t" + this.getmFloor() + "\t";

		if (this.mPartitions != null) {
			Iterator<Integer> itr = this.mPartitions.iterator();

			while (itr.hasNext()) {
				outputString = outputString + itr.next() + "\t";
			}
		}

		return outputString;
	}
}
