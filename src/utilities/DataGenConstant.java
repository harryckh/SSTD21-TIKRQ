/**
 * 
 */
package utilities;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * <h>DataGenConstant</h>
 * Constant Values in Data Generating
 * @author feng zijin, Tiantian Liu
 *
 */
public class DataGenConstant {
	public static String dataset = "syn";
	
	// PARAMETERS FOR INDOOR SPACES
	/** dimensions of the floor */
	public static double floorRangeX = 1368;
	public static double floorRangeY = 1368;

	public static double zoomLevel = 0.6;

	/** numbers of the floor */
	public static int nFloor = 5;
//	public static int nFloor = 7;
	
	/** type of dataset */
	public static int dataType = 1; // 1 means regular dataset; 0 means less doors; 2 means more doors;

	/** type of division */
	public static int divisionType = 1; // 1 means regular division; 0 means no division for hallway; 2 means more hallway;

	/** length of stairway between two floors */
	public static double lenStairway = 20.0;
//	public static double lenStairway = 41.5;///==30second walking
	
	/** time of using elevator */
	public static double timeElevator = 40.0;//seconds 
	
	public static ArrayList<Integer> exitDoors = new ArrayList<>(Arrays.asList(210, 212, 213, 214));
	
	// ID COUNTERS FOR INDOOR ENTITIES
	/** the ID counter of Partitions */
	public static int mID_Par = 0;

	/** the ID counter of Doors */
	public static int mID_Door = 0;
	
	/** the ID counter of Floors */
	public static int mID_Floor = 0;

	// KEYWORDS	
	public static int mKeyworSize = 0;

	public static int tWordSize = 11797;
	public static int iWordSize = 1225;
	public static ArrayList<String> cWords = new ArrayList<>(Arrays.asList("shoes", "clothing", "cosmetics", "restaurant", "electronics", "bank", "accessories", "food", "bags", "travel", "others"));
//	public static ArrayList<String> cWords = new ArrayList<>(//
//			Arrays.asList("shoes","shoes2",//
//					"clothing","clothing2","clothing3","clothing4",//
//					"cosmetics","cosmetics2","cosmetics3","cosmetics4",//
//					"restaurant","restaurant2", //
//					"electronics","electronics2","electronics3","electronics4", //
//					"bank","bank2",//
//					"accessories","accessories2","accessories3",//
//					"food","food2","food3","food4","food5","food6","food7","food8",//
//					"bags", "travel","travel2",// 
//					"others","others2","others3","others4","others5","others6","others7","others8"));

//	public static double threshold = 0.04; // the threshold for the relevance
	public static double threshold = 0.1; // the threshold for the relevance
	
	public static double SC_MAX = 10;
	
	// traveling speed 83.34m/min. = 1.389m/s
	public static double traveling_speed = 1 * 83.34 / 60;
//	public static double traveling_speed = 1.4;

	public static void init(String dataName) {

		if (dataName.equals("hsm")) {
			dataset = "hsm";
			floorRangeX = 2100;
			floorRangeY = 2700;
			zoomLevel = 0.28;
			nFloor = 7;
			exitDoors = new ArrayList<>(Arrays.asList(1, 5, 9, 10, 233, 617, 619, 620, 622, 626));
		}

	}

}
