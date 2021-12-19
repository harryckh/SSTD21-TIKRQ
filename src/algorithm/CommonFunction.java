package algorithm;

import indoor_entitity.*;
import org.apache.commons.httpclient.methods.multipart.Part;
import utilities.Constant;
import utilities.DataGenConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * common functions
 * 
 * @author Tiantian Liu
 */

public class CommonFunction {

	// find shortest path
	public static String findShortestPath(Point sPoint, Point tPoint, Partition sPartition, Partition tPartition) {
		String result = "";

		if (sPoint.equals(tPoint)) {
			return sPoint.eDist(tPoint) + "\t";
		}

		if (sPartition.getmID() == tPartition.getmID()) {
			return sPoint.eDist(tPoint) + "\t";
		}

		ArrayList<Integer> sdoors = new ArrayList<Integer>();
//        sdoors = sPartition.getConnectivityTier().getP2DLeave();
		sdoors = sPartition.getmDoors();
		ArrayList<Integer> tdoors = new ArrayList<Integer>();
//        edoors = ePartition.getConnectivityTier().getP2DEnter();
		tdoors = tPartition.getmDoors();

		int size = IndoorSpace.iDoors.size() + 2;
		BinaryHeap<Double> H = new BinaryHeap<Double>(size);
		double[] dist = new double[size]; // stores the current shortest path distance from source ds to a door de
		ArrayList<ArrayList<Integer>> prev = new ArrayList<>();
		boolean[] visited = new boolean[size]; // mark door as visited

		for (int i = 0; i < size - 2; i++) {
			int doorID = IndoorSpace.iDoors.get(i).getmID();
			if (doorID != i)
				System.out.println("something wrong_Helper_d2dDist");
			dist[i] = Constant.large;

			// enheap
			H.insert(dist[i], doorID);

			prev.add(null);
		}

		int ps = size - 2;
		dist[ps] = 0;
		H.insert(dist[ps], ps); // start point
		prev.add(null);

		int pt = size - 1;
		dist[pt] = Constant.large;
		H.insert(dist[pt], pt); // end point
		prev.add(null);

//        System.out.println("Heap is ready");
		int h = 0;
		while (H.heapSize > 0) {
//            System.out.println(h++);
			String[] str = H.delete_min().split(",");
			int di = Integer.parseInt(str[1]);
//            System.out.println(di);
			double dist_di = Double.parseDouble(str[0]);
			if (dist_di == Constant.large) {
				return Double.toString(Constant.large) + "\t";

			}

//			System.out.println("dequeue <" + di + ", " + dist_di + ">");

			visited[di] = true;
//			System.out.println("d" + di + " is newly visited");
			if (di == pt) {
//				System.out.println("d2dDist_ di = " + di + " de = " + de);
				result += CommonFunction.getPath(prev, ps, pt);
				return dist_di + "\t" + result;
			}
			int flag = 0;

			for (int i = 0; i < tdoors.size(); i++) {
				if (di == tdoors.get(i)) {
					flag = 1;
					Door door = IndoorSpace.iDoors.get(di);

					double dist2 = CommonFunction.distv(door, tPoint);

					if ((dist[di] + dist2) < dist[pt]) {
						double oldDj = dist[pt];
						dist[pt] = dist[di] + dist2;
						H.updateNode(oldDj, pt, dist[pt], pt);
						prev.set(pt, new ArrayList<>(Arrays.asList(tPartition.getmID(), di)));
					}
				}
			}
			if (flag == 1) {
				continue;
			}

			if (di != ps) {
				Door door = IndoorSpace.iDoors.get(di);

				ArrayList<Integer> parts = new ArrayList<Integer>(); // list of leavable partitions
//                parts = door.getD2PLeave();
				parts = door.getmPartitions();

				int partSize = parts.size();
				for (int i = 0; i < partSize; i++) {
					ArrayList<Integer> doorTemp = new ArrayList<Integer>();
					int v = parts.get(i); // partition id
					if (prev.get(di).get(0) == v)
						continue;
					Partition partition = IndoorSpace.iPartitions.get(v);

					doorTemp = partition.getmDoors();
					ArrayList<Integer> doors = new ArrayList<Integer>(); // list of unvisited leavable doors
					// remove the visited doors
					int doorTempSize = doorTemp.size();
					for (int j = 0; j < doorTempSize; j++) {
						int index = doorTemp.get(j);
						if (DataGenConstant.exitDoors.contains(index))
							continue;
//					System.out.println("index = " + index + " " + !visited[index]);
						if (!visited[index]) {
							doors.add(index);
						}
					}
					for (int j = 0; j < doors.size(); j++) {
						int dj = doors.get(j);

						if (visited[dj])
							System.out.println("something wrong_Helper_d2dDist2");
//					System.out.println("for d" + di + " and d" + dj);

						double dist3 = partition.getdistMatrix().getDistance(di, dj);

						if ((dist[di] + dist3) < dist[dj]) {
							double oldDj = dist[dj];
							dist[dj] = dist[di] + dist3;
							H.updateNode(oldDj, dj, dist[dj], dj);
							prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
						}
					}

				}
			} else {
				for (int j = 0; j < sdoors.size(); j++) {
					int dj = sdoors.get(j);
//                    System.out.println("dj " + dj);
					if (DataGenConstant.exitDoors.contains(dj))
						continue;
					Door doorj = IndoorSpace.iDoors.get(dj);
					if (visited[dj])
						System.out.println("something wrong_Helper_d2dDist2");
//					System.out.println("for d" + di + " and d" + dj);
					int v = sPartition.getmID();
					double dist1 = CommonFunction.distv(sPoint, doorj);
//
					if ((dist[di] + dist1) < dist[dj]) {
						double oldDj = dist[dj];
						dist[dj] = dist[di] + dist1;
						H.updateNode(oldDj, dj, dist[dj], dj);
						prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
					}
				}

			}

		}

		return result;
	}

	// find fastest path: point to point
	public static String findFastestPathP2P(Point sPoint, Point tPoint, Partition sPartition, Partition tPartition,
			double timeBudget) {
		String result = "";

		if (sPoint.equals(tPoint)) {
			return sPoint.eDist(tPoint) + "\t";
		}

		if (sPartition.getmID() == tPartition.getmID()) {
			return sPoint.eDist(tPoint) + "\t";
		}

		ArrayList<Integer> sdoors = new ArrayList<Integer>();
//        sdoors = sPartition.getConnectivityTier().getP2DLeave();
		sdoors = sPartition.getmDoors();
		ArrayList<Integer> tdoors = new ArrayList<Integer>();
//        edoors = ePartition.getConnectivityTier().getP2DEnter();
		tdoors = tPartition.getmDoors();

		int size = IndoorSpace.iDoors.size() + 2;
		BinaryHeap<Double> H = new BinaryHeap<Double>(size);
		double[] time = new double[size];
		ArrayList<ArrayList<Integer>> prev = new ArrayList<>();
		boolean[] visited = new boolean[size]; // mark door as visited

		for (int i = 0; i < size - 2; i++) {
			int doorID = IndoorSpace.iDoors.get(i).getmID();
			if (doorID != i)
				System.out.println("something wrong_Helper_d2dDist");
			time[i] = Constant.large;

			// enheap
			H.insert(time[i], doorID);

			prev.add(null);
		}

		int ps = size - 2;
		time[ps] = 0;
		H.insert(time[ps], ps); // start point
		prev.add(null);

		int pt = size - 1;
		time[pt] = Constant.large;
		H.insert(time[pt], pt); // end point
		prev.add(null);

//        System.out.println("Heap is ready");
		int h = 0;
		while (H.heapSize > 0) {
//            System.out.println(h++);
			String[] str = H.delete_min().split(",");
			int di = Integer.parseInt(str[1]);
//            System.out.println(di);
			double time_di = Double.parseDouble(str[0]);
			if (time_di == Constant.large || time_di > timeBudget) {
				return Double.toString(Constant.large) + "\t";

			}

//			System.out.println("dequeue <" + di + ", " + dist_di + ">");

			visited[di] = true;
//			System.out.println("d" + di + " is newly visited");
			if (di == pt) {
//				System.out.println("d2dDist_ di = " + di + " de = " + de);
				result += CommonFunction.getPath(prev, ps, pt);
				return time_di + "\t" + result;
			}
			int flag = 0;

			for (int i = 0; i < tdoors.size(); i++) {
				if (di == tdoors.get(i)) {
					flag = 1;
					Door door = IndoorSpace.iDoors.get(di);

//                    double time2 = CommonFunction.distv(door, tPoint) / DataGenConstant.traveling_speed + tPartition.getWaitTime();
					double time2 = CommonFunction.distv(door, tPoint) / DataGenConstant.traveling_speed;

					if ((time[di] + time2) < time[pt]) {
						double oldDj = time[pt];
						time[pt] = time[di] + time2;
						H.updateNode(oldDj, pt, time[pt], pt);
						prev.set(pt, new ArrayList<>(Arrays.asList(tPartition.getmID(), di)));
					}
				}
			}
			if (flag == 1) {
				continue;
			}

			if (di != ps) {
				Door door = IndoorSpace.iDoors.get(di);

				ArrayList<Integer> parts = new ArrayList<Integer>(); // list of leavable partitions
//                parts = door.getD2PLeave();
				parts = door.getmPartitions();

				int partSize = parts.size();
				for (int i = 0; i < partSize; i++) {
					ArrayList<Integer> doorTemp = new ArrayList<Integer>();
					int v = parts.get(i); // partition id
					if (prev.get(di).get(0) == v)
						continue;
					Partition partition = IndoorSpace.iPartitions.get(v);

					doorTemp = partition.getmDoors();
					ArrayList<Integer> doors = new ArrayList<Integer>(); // list of unvisited leavable doors
					// remove the visited doors
					int doorTempSize = doorTemp.size();
					for (int j = 0; j < doorTempSize; j++) {
						int index = doorTemp.get(j);
						if (DataGenConstant.exitDoors.contains(index))
							continue;
//					System.out.println("index = " + index + " " + !visited[index]);
						if (!visited[index]) {
							doors.add(index);
						}
					}
					for (int j = 0; j < doors.size(); j++) {
						int dj = doors.get(j);

						if (visited[dj])
							System.out.println("something wrong_Helper_d2dDist2");
//					System.out.println("for d" + di + " and d" + dj);

//                        double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed + partition.getWaitTime();
						double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed;

						if ((time[di] + time3) < time[dj]) {
							double oldDj = time[dj];
							time[dj] = time[di] + time3;
							H.updateNode(oldDj, dj, time[dj], dj);
							prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
						}
					}

				}
			} else {
				for (int j = 0; j < sdoors.size(); j++) {
					int dj = sdoors.get(j);
//                    System.out.println("dj " + dj);
					if (DataGenConstant.exitDoors.contains(dj))
						continue;
					Door doorj = IndoorSpace.iDoors.get(dj);
					if (visited[dj])
						System.out.println("something wrong_Helper_d2dDist2");
//					System.out.println("for d" + di + " and d" + dj);
					int v = sPartition.getmID();
					double time1 = CommonFunction.distv(sPoint, doorj) / DataGenConstant.traveling_speed;
//							+ sPartition.getWaitTime();
//
					if ((time[di] + time1) < time[dj]) {
						double oldDj = time[dj];
						time[dj] = time[di] + time1;
						H.updateNode(oldDj, dj, time[dj], dj);
						prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
					}
				}

			}

		}

		return result;
	}

	// find fastest path: point to door
	public static String findFastestPathP2D(Point sPoint, Door tDoor, Partition sPartition) {
		String result = "";

		if (sPartition.getmDoors().contains(tDoor.getmID())) {
//        	System.out.println("6");
			int ps = IndoorSpace.iDoors.size() + 1 - 1;
			return sPoint.eDist(tDoor) + "\t" + ps + "\t" + tDoor.getmID();
		}

		ArrayList<Integer> sdoors = new ArrayList<Integer>();
//        sdoors = sPartition.getConnectivityTier().getP2DLeave();
		sdoors = sPartition.getmDoors();

		int size = IndoorSpace.iDoors.size() + 1;
		BinaryHeap<Double> H = new BinaryHeap<Double>(size);
		double[] time = new double[size];
		ArrayList<ArrayList<Integer>> prev = new ArrayList<>();
		boolean[] visited = new boolean[size]; // mark door as visited

		for (int i = 0; i < IndoorSpace.iDoors.size(); i++) {
			int doorID = IndoorSpace.iDoors.get(i).getmID();
			if (doorID != i)
				System.out.println("something wrong_Helper_d2dDist");
			time[i] = Constant.large;

			// enheap
			H.insert(time[i], doorID);
			prev.add(null);
		}

		int ps = size - 1;
		time[ps] = 0;
		H.insert(time[ps], ps); // start point
		prev.add(null);

//        System.out.println("Heap is ready");
		int h = 0;
		while (H.heapSize > 0) {
//            System.out.println(h++);
			String[] str = H.delete_min().split(",");
			int di = Integer.parseInt(str[1]);
//            System.out.println(di);
			double time_di = Double.parseDouble(str[0]);
			if (time_di == Constant.large) {
				return "no route";

			}

//			System.out.println("dequeue <" + di + ", " + time_di + ">");

			visited[di] = true;
//			System.out.println("d" + di + " is newly visited");
			if (di == tDoor.getmID()) {
//				System.out.println("d2dDist_ di = " + di );
				result += CommonFunction.getPath(prev, ps, di);
				return time_di + "\t" + result;
			}

			if (di != ps) {
				Door door = IndoorSpace.iDoors.get(di);

				ArrayList<Integer> parts = new ArrayList<Integer>(); // list of leavable partitions
//                parts = door.getD2PLeave();
				parts = door.getmPartitions();

				int partSize = parts.size();
				for (int i = 0; i < partSize; i++) {
					ArrayList<Integer> doorTemp = new ArrayList<Integer>();
					int v = parts.get(i); // partition id
					if (prev.get(di).get(0) == v)
						continue;
					Partition partition = IndoorSpace.iPartitions.get(v);

					doorTemp = partition.getmDoors();
					ArrayList<Integer> doors = new ArrayList<Integer>(); // list of unvisited leavable doors
					// remove the visited doors
					int doorTempSize = doorTemp.size();
					for (int j = 0; j < doorTempSize; j++) {
						int index = doorTemp.get(j);
						if (DataGenConstant.exitDoors.contains(index))
							continue;
//					System.out.println("index = " + index + " " + !visited[index]);
						if (!visited[index]) {
							doors.add(index);
						}
					}
					for (int j = 0; j < doors.size(); j++) {
						int dj = doors.get(j);

						if (visited[dj])
							System.out.println("something wrong_Helper_d2dDist2");
//					System.out.println("for d" + di + " and d" + dj);

//                        double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed + partition.getWaitTime();
						double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed;
						if ((time[di] + time3) < time[dj]) {
							double oldDj = time[dj];
							time[dj] = time[di] + time3;
							H.updateNode(oldDj, dj, time[dj], dj);
							prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
						}
					}

				}
			} else {
				for (int j = 0; j < sdoors.size(); j++) {
					int dj = sdoors.get(j);
//                    System.out.println("dj " + dj);
					if (DataGenConstant.exitDoors.contains(dj))
						continue;
					Door doorj = IndoorSpace.iDoors.get(dj);
					if (visited[dj])
						System.out.println("something wrong_Helper_d2dDist2");
//					System.out.println("for d" + di + " and d" + dj);
					int v = sPartition.getmID();
					double time1 = CommonFunction.distv(sPoint, doorj) / DataGenConstant.traveling_speed
							+ sPartition.getWaitTime();
//

					if ((time[di] + time1) < time[dj]) {
						double oldDj = time[dj];
						time[dj] = time[di] + time1;
						H.updateNode(oldDj, dj, time[dj], dj);
						prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
					}
				}

			}

		}

		return result;
	}

	// find fastest path: point to all doors in @tDoor
	// the door is set to -1 if it is found
	// the corresponding result store the route
	public static void findFastestPathsP2D(Point sPoint, ArrayList<Integer> tDoorsID, Partition sPartition,
			HashMap<String, String> d2dPath, double timeBudget) {
//		ArrayList<String> result = new ArrayList<>();

		for (int i = 0; i < tDoorsID.size(); i++) {
			int tDoorID = tDoorsID.get(i);
			if (tDoorID != -1 && sPartition.getmDoors().contains(tDoorID)) {
//        	System.out.println("6");
				int ps = IndoorSpace.iDoors.size() + 1 - 1;

				d2dPath.put("-1" + "-" + tDoorID + "-" + sPartition.getmID(),
						sPoint.eDist(IndoorSpace.iDoors.get(tDoorID)) + "\t" + ps + "\t" + tDoorID);

//				result.set(i, sPoint.eDist(IndoorSpace.iDoors.get(tDoor)) + "\t" + ps + "\t" + tDoor);
				tDoorsID.set(i, -1);
			}
		}
		if (allMinusOne(tDoorsID))
			return;
		ArrayList<Integer> sdoors = new ArrayList<Integer>();
//        sdoors = sPartition.getConnectivityTier().getP2DLeave();
		sdoors = sPartition.getmDoors();

		int size = IndoorSpace.iDoors.size() + 1;
		BinaryHeap<Double> H = new BinaryHeap<Double>(size);
		double[] time = new double[size];
		ArrayList<ArrayList<Integer>> prev = new ArrayList<>();
		boolean[] visited = new boolean[size]; // mark door as visited

		for (int i = 0; i < IndoorSpace.iDoors.size(); i++) {
			int doorID = IndoorSpace.iDoors.get(i).getmID();
			if (doorID != i)
				System.out.println("something wrong_Helper_d2dDist");
			time[i] = Constant.large;

			// enheap
			H.insert(time[i], doorID);
			prev.add(null);
		}

		int ps = size - 1;
		time[ps] = 0;
		H.insert(time[ps], ps); // start point
		prev.add(null);

//        System.out.println("Heap is ready");
		int h = 0;
		while (H.heapSize > 0) {
//            System.out.println(h++);
			String[] str = H.delete_min().split(",");
			int di = Integer.parseInt(str[1]);
//            System.out.println(di);
			double time_di = Double.parseDouble(str[0]);
			if (time_di == Constant.large || time_di > timeBudget) {

//				return "no route";
				return;
			}

//			System.out.println("dequeue <" + di + ", " + time_di + ">");

			visited[di] = true;
//			System.out.println("d" + di + " is newly visited");
			int idi = tDoorsID.indexOf(di);
			if (idi != -1) {
//			if (di == tDoor.getmID()) {
//				System.out.println("d2dDist_ di = " + di );
				d2dPath.put("-1" + "-" + di + "-" + sPartition.getmID(),
						time_di + "\t" + CommonFunction.getPath(prev, ps, di));

//				result.set(idi, CommonFunction.getPath(prev, ps, di));
				tDoorsID.set(idi, -1);
//				return time_di + "\t" + result;
				if (allMinusOne(tDoorsID))
					return;
			}
			///// -------------------
			/// store all key partition that passes through for future
//			prev.get(di).get(0) 
//			int curParId= prev.get(di).get(0) ;

			///// -----------------
			if (di != ps) {
				Door door = IndoorSpace.iDoors.get(di);

				ArrayList<Integer> parts = new ArrayList<Integer>(); // list of leavable partitions
//                parts = door.getD2PLeave();
				parts = door.getmPartitions();

				int partSize = parts.size();
				for (int i = 0; i < partSize; i++) {
					ArrayList<Integer> doorTemp = new ArrayList<Integer>();
					int v = parts.get(i); // partition id
					if (prev.get(di).get(0) == v)
						continue;
					Partition partition = IndoorSpace.iPartitions.get(v);

					doorTemp = partition.getmDoors();
					ArrayList<Integer> doors = new ArrayList<Integer>(); // list of unvisited leavable doors
					// remove the visited doors
					int doorTempSize = doorTemp.size();
					for (int j = 0; j < doorTempSize; j++) {
						int index = doorTemp.get(j);
						if (DataGenConstant.exitDoors.contains(index))
							continue;
//					System.out.println("index = " + index + " " + !visited[index]);
						if (!visited[index]) {
							doors.add(index);
						}
					}
					for (int j = 0; j < doors.size(); j++) {
						int dj = doors.get(j);

						if (visited[dj])
							System.out.println("something wrong_Helper_d2dDist2");
//					System.out.println("for d" + di + " and d" + dj);

//                        double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed + partition.getWaitTime();
						double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed;
						if ((time[di] + time3) < time[dj]) {
							double oldDj = time[dj];
							time[dj] = time[di] + time3;
							H.updateNode(oldDj, dj, time[dj], dj);
							prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
						}
					}

				}
			} else {
				for (int j = 0; j < sdoors.size(); j++) {
					int dj = sdoors.get(j);
//                    System.out.println("dj " + dj);
					if (DataGenConstant.exitDoors.contains(dj))
						continue;
					Door doorj = IndoorSpace.iDoors.get(dj);
					if (visited[dj])
						System.out.println("something wrong_Helper_d2dDist2");
//					System.out.println("for d" + di + " and d" + dj);
					int v = sPartition.getmID();
					double time1 = CommonFunction.distv(sPoint, doorj) / DataGenConstant.traveling_speed
							+ sPartition.getWaitTime();
//

					if ((time[di] + time1) < time[dj]) {
						double oldDj = time[dj];
						time[dj] = time[di] + time1;
						H.updateNode(oldDj, dj, time[dj], dj);
						prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
					}
				}

			}

		}

		return;
	}

	// find fastest path: door to point
	public static String findFastestPathD2P(Door sDoor, Point tPoint, Partition sPartition, Partition tPartition,
			double timeBudget) {
		String result = "";

		if (tPartition!=null && tPartition.getmDoors().contains(sDoor.getmID())) {
			return sDoor.eDist(tPoint) + "\t";
		}

		ArrayList<Integer> tdoors = new ArrayList<Integer>();
//        edoors = ePartition.getConnectivityTier().getP2DEnter();
		if(tPartition!=null)
		tdoors = tPartition.getmDoors();

		int size = IndoorSpace.iDoors.size() + 1;
		BinaryHeap<Double> H = new BinaryHeap<Double>(size);
		double[] time = new double[size];
		ArrayList<ArrayList<Integer>> prev = new ArrayList<>();
		boolean[] visited = new boolean[size]; // mark door as visited

		for (int i = 0; i < IndoorSpace.iDoors.size(); i++) {
			int doorID = IndoorSpace.iDoors.get(i).getmID();
			if (doorID != i)
				System.out.println("something wrong_Helper_d2dDist");
			if (doorID == sDoor.getmID()) {
				time[doorID] = 0;
				H.insert(time[doorID], doorID); // start point
				prev.add(null);
			} else {
				time[i] = Constant.large;
				H.insert(time[i], doorID);
				prev.add(null);
			}
		}

		int pt = size - 1;
		time[pt] = Constant.large;
		H.insert(time[pt], pt); // end point
		prev.add(null);

//        System.out.println("Heap is ready");
		int h = 0;
		while (H.heapSize > 0) {
//            System.out.println(h++);
			String[] str = H.delete_min().split(",");
			int di = Integer.parseInt(str[1]);
//            System.out.println(di);
			double time_di = Double.parseDouble(str[0]);
			if (time_di == Constant.large || time_di > timeBudget) {
//				System.out.println(time_di + " "  + timeBudget);
				return "no route";

			}

//			System.out.println("dequeue <" + di + ", " + time_di + ">");

			visited[di] = true;
//			System.out.println("d" + di + " is newly visited");
			if (di == pt) {
//				System.out.println("d2dDist_ di = " + di + " de = " + de);
				result += CommonFunction.getPath(prev, sDoor.getmID(), pt);
				return time_di + "\t" + result;
			}
			int flag = 0;

			for (int i = 0; i < tdoors.size(); i++) {
				if (di == tdoors.get(i)) {
					flag = 1;
					Door door = IndoorSpace.iDoors.get(di);

//                    double time2 = CommonFunction.distv(door, tPoint) / DataGenConstant.traveling_speed + tPartition.getWaitTime();
					double time2 = CommonFunction.distv(door, tPoint) / DataGenConstant.traveling_speed;
					if ((time[di] + time2) < time[pt]) {
						double oldDj = time[pt];
						time[pt] = time[di] + time2;
						H.updateNode(oldDj, pt, time[pt], pt);
						prev.set(pt, new ArrayList<>(Arrays.asList(tPartition.getmID(), di)));
					}
				}
			}
			if (flag == 1) {
				continue;
			}

			Door door = IndoorSpace.iDoors.get(di);

			ArrayList<Integer> parts = new ArrayList<Integer>(); // list of leavable partitions
//                parts = door.getD2PLeave();
			parts = door.getmPartitions();

			int partSize = parts.size();
			for (int i = 0; i < partSize; i++) {
				ArrayList<Integer> doorTemp = new ArrayList<Integer>();
				int v = parts.get(i); // partition id
				if (di != sDoor.getmID() && prev.get(di).get(0) == v)
					continue;
//				if (di == sDoor.getmID() && v != sPartition.getmID())
//					continue;
				Partition partition = IndoorSpace.iPartitions.get(v);

				doorTemp = partition.getmDoors();
				ArrayList<Integer> doors = new ArrayList<Integer>(); // list of unvisited leavable doors
				// remove the visited doors
				int doorTempSize = doorTemp.size();
				for (int j = 0; j < doorTempSize; j++) {
					int index = doorTemp.get(j);
					if (DataGenConstant.exitDoors.contains(index))
						continue;
//					System.out.println("index = " + index + " " + !visited[index]);
					if (!visited[index]) {
						doors.add(index);
					}
				}
				for (int j = 0; j < doors.size(); j++) {
					int dj = doors.get(j);

					if (visited[dj])
						System.out.println("something wrong_Helper_d2dDist2");
//					System.out.println("for d" + di + " and d" + dj);

//                    double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed + partition.getWaitTime();
					double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed;

					// System.out.println(time3 + " " + partition.getdistMatrix().getDistance(di,
					// dj) + " " + partition.getWaitTime()) ;

					if ((time[di] + time3) < time[dj]) {
						double oldDj = time[dj];
						time[dj] = time[di] + time3;
						H.updateNode(oldDj, dj, time[dj], dj);
						prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
					}
				}

			}

		}

		return result;
	}

	// find fastest path: door to door
	public static String findFastestPathD2D(Door sDoor, Door tDoor, Partition sPartition) {
		String result = "";

		if (isInSamePar(sDoor, tDoor)) {
			return sDoor.eDist(tDoor) + "\t" + sDoor.getmID() + "\t" + tDoor.getmID();
		}

		int size = IndoorSpace.iDoors.size();
		BinaryHeap<Double> H = new BinaryHeap<Double>(size);
		double[] time = new double[size];
		ArrayList<ArrayList<Integer>> prev = new ArrayList<>();
		boolean[] visited = new boolean[size]; // mark door as visited

		/// initialize heap, enheap all doors
		for (int i = 0; i < IndoorSpace.iDoors.size(); i++) {
			int doorID = IndoorSpace.iDoors.get(i).getmID();
			if (doorID != i)
				System.out.println("something wrong_Helper_d2dDist");
			if (doorID == sDoor.getmID()) {
				time[doorID] = 0;
				H.insert(time[doorID], doorID); // start point
				prev.add(null);
			} else {
				time[i] = Constant.large;
				H.insert(time[i], doorID);
				prev.add(null);
			}
		}

//        System.out.println("Heap is ready");
//        int h = 0;
		while (H.heapSize > 0) {

			/// del min
			String[] str = H.delete_min().split(",");
			int di = Integer.parseInt(str[1]);
			double time_di = Double.parseDouble(str[0]);

//            System.out.println("h: " + (h++) + " di: " + di);
//			System.out.println("dequeue <" + di + ", " + dist_di + ">");

			if (time_di == Constant.large) {
				return "no route";

			}

			visited[di] = true;
//			System.out.println("d" + di + " is newly visited");
			if (di == tDoor.getmID()) {
				/// reached end point
//				System.out.println("d2dDist_ di = " + di + " de = " + de);
				result += CommonFunction.getPath(prev, sDoor.getmID(), di);
				return time_di + "\t" + result;
			}

			Door door = IndoorSpace.iDoors.get(di);

			ArrayList<Integer> parts = new ArrayList<Integer>(); // list of leavable partitions
//                parts = door.getD2PLeave();
			parts = door.getmPartitions();

			int partSize = parts.size();
			/// for each partition the door connected to
			for (int i = 0; i < partSize; i++) {
				ArrayList<Integer> doorTemp = new ArrayList<Integer>();
				int v = parts.get(i); // partition id
				if (di != sDoor.getmID() && prev.get(di).get(0) == v)
					continue;

//                if (di == sDoor.getmID() && v != sPartition.getmID()) continue;
				Partition partition = IndoorSpace.iPartitions.get(v);

				doorTemp = partition.getmDoors();
//                System.out.println( "  2 doorTemp:" + doorTemp.toString() );
				ArrayList<Integer> doors = new ArrayList<Integer>(); // list of unvisited leavable doors
				// remove the visited doors
				int doorTempSize = doorTemp.size();
				for (int j = 0; j < doorTempSize; j++) {
					int index = doorTemp.get(j);
					if (DataGenConstant.exitDoors.contains(index))
						continue;
//					System.out.println("index = " + index + " " + !visited[index]);
					if (!visited[index]) {
						doors.add(index);
					}
				}

				for (int j = 0; j < doors.size(); j++) {
					int dj = doors.get(j);

					if (visited[dj])
						System.out.println("something wrong_Helper_d2dDist2");

//                    double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed + partition.getWaitTime();
					double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed;
//                    System.out.println("for d" + di + " and d" + dj + " time3:" + time3 );
					if ((time[di] + time3) < time[dj]) {
						double oldDj = time[dj];
						time[dj] = time[di] + time3;
						H.updateNode(oldDj, dj, time[dj], dj);
						prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
					}
				}

			}

		}

		return result;
	}

	// find fastest path: door to door
	public static void findFastestPathsD2D(Door sDoor, ArrayList<Integer> tDoorsID, Partition sPartition,
			HashMap<String, String> d2dPath, double timeBudget) {

		for (int i = 0; i < tDoorsID.size(); i++) {
			int tDoorID = tDoorsID.get(i);
			if (tDoorID != -1) {
				Door tDoor = IndoorSpace.iDoors.get(tDoorID);
				if (isInSamePar(sDoor, tDoor)) {
//					System.out.println(sDoor.getmID() + "-" + tDoorID + "-" + sPartition.getmID());
//System.out.println(sDoor.eDist(tDoor) + "\t" + sDoor.getmID() + "\t" + tDoor.getmID());
					d2dPath.put(sDoor.getmID() + "-" + tDoorID + "-" + sPartition.getmID(),
							sDoor.eDist(tDoor) + "\t" + sDoor.getmID() + "\t" + tDoor.getmID());

//					result.set(i, sDoor.eDist(tDoor) + "\t" + sDoor.getmID() + "\t" + tDoor.getmID());
					tDoorsID.set(i, -1);

				}
			}
		}
		if (allMinusOne(tDoorsID))
			return;
		int size = IndoorSpace.iDoors.size();
		BinaryHeap<Double> H = new BinaryHeap<Double>(size);
		double[] time = new double[size];
		ArrayList<ArrayList<Integer>> prev = new ArrayList<>();
		boolean[] visited = new boolean[size]; // mark door as visited

		/// initialize heap, enheap all doors
		for (int i = 0; i < IndoorSpace.iDoors.size(); i++) {
			int doorID = IndoorSpace.iDoors.get(i).getmID();
			if (doorID != i)
				System.out.println("something wrong_Helper_d2dDist");
			if (doorID == sDoor.getmID()) {
				time[doorID] = 0;
				H.insert(time[doorID], doorID); // start point
				prev.add(null);
			} else {
				time[i] = Constant.large;
				H.insert(time[i], doorID);
				prev.add(null);
			}
		}

//        System.out.println("Heap is ready");
//        int h = 0;
		while (H.heapSize > 0) {

			/// del min
			String[] str = H.delete_min().split(",");
			int di = Integer.parseInt(str[1]);
			double time_di = Double.parseDouble(str[0]);

//            System.out.println("h: " + (h++) + " di: " + di);
//			System.out.println("dequeue <" + di + ", " + dist_di + ">");

			if (time_di == Constant.large || time_di > timeBudget) {
//				return "no route";
				return;
			}

			visited[di] = true;
//			System.out.println("d" + di + " is newly visited");
//			if (di == tDoor.getmID()) {
//				/// reached end point
////				System.out.println("d2dDist_ di = " + di + " de = " + de);
//				result += CommonFunction.getPath(prev, sDoor.getmID(), di);
//				return time_di + "\t" + result;
//			}
			int idi = tDoorsID.indexOf(di);
			if (idi != -1) {
//			if (di == tDoor.getmID()) {
//				System.out.println("d2dDist_ di = " + di  + " " + idi + " " +sDoor.getmID());
//				System.out.println(sDoor.getmID() + "-" + di + "-" + sPartition.getmID());

				d2dPath.put(sDoor.getmID() + "-" + di + "-" + sPartition.getmID(),
						time_di + "\t" + CommonFunction.getPath(prev, sDoor.getmID(), di));

//				result.set(idi, CommonFunction.getPath(prev, sDoor.getmID(), di));
				tDoorsID.set(idi, -1);
//				return time_di + "\t" + result;
				if (allMinusOne(tDoorsID))
					return;
			}

			// ---
//			for(ArrayList<String> par: canPars_list) {
//				int parId= Integer.parseInt(par.get(0));
//				if(di != sDoor.getmID() &&parId==prev.get(di).get(0)) {
//					//it is a key par
////					System.out.println(sDoor.getmID() +" " + parId + " "+ di);
//					d2dPath.put(sDoor.getmID() + "-" + di + "-" + sPartition.getmID(),
//							time_di + "\t" + CommonFunction.getPath(prev, sDoor.getmID(), di));
//
//				}
//			}
			// ---
			Door door = IndoorSpace.iDoors.get(di);

			ArrayList<Integer> parts = new ArrayList<Integer>(); // list of leavable partitions
//                parts = door.getD2PLeave();
			parts = door.getmPartitions();

			int partSize = parts.size();
			/// for each partition the door connected to
			for (int i = 0; i < partSize; i++) {
				ArrayList<Integer> doorTemp = new ArrayList<Integer>();
				int v = parts.get(i); // partition id
				if (di != sDoor.getmID() && prev.get(di).get(0) == v)
					continue;

//                if (di == sDoor.getmID() && v != sPartition.getmID()) continue;
				Partition partition = IndoorSpace.iPartitions.get(v);

				doorTemp = partition.getmDoors();
//                System.out.println( "  2 doorTemp:" + doorTemp.toString() );
				ArrayList<Integer> doors = new ArrayList<Integer>(); // list of unvisited leavable doors
				// remove the visited doors
				int doorTempSize = doorTemp.size();
				for (int j = 0; j < doorTempSize; j++) {
					int index = doorTemp.get(j);
					if (DataGenConstant.exitDoors.contains(index))
						continue;
//					System.out.println("index = " + index + " " + !visited[index]);
					if (!visited[index]) {
						doors.add(index);
					}
				}

				for (int j = 0; j < doors.size(); j++) {
					int dj = doors.get(j);

					if (visited[dj])
						System.out.println("something wrong_Helper_d2dDist2");

//                    double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed + partition.getWaitTime();
					double time3 = partition.getdistMatrix().getDistance(di, dj) / DataGenConstant.traveling_speed;
//                    System.out.println("for d" + di + " and d" + dj + " time3:" + time3 );
					if ((time[di] + time3) < time[dj]) {
						double oldDj = time[dj];
						time[dj] = time[di] + time3;
						H.updateNode(oldDj, dj, time[dj], dj);
						prev.set(dj, new ArrayList<>(Arrays.asList(v, di)));
					}
				}

			}

		}

		return;
	}

	private static boolean allMinusOne(ArrayList<Integer> tDoorsID) {
		// TODO Auto-generated method stub
		for (Integer id : tDoorsID) {
			if (id != -1)
				return false;
		}
		return true;
	}

	/**
	 * find the common door of two partitions
	 * 
	 * @param parId1
	 * @param parId2
	 * @return
	 */
	public static int findCommonDoor(int parId1, int parId2) {
		int result = -1;
		Partition par1 = IndoorSpace.iPartitions.get(parId1);
		Partition par2 = IndoorSpace.iPartitions.get(parId2);
		ArrayList<Integer> doors1 = par1.getmDoors();
		ArrayList<Integer> doors2 = par2.getmDoors();
		for (int i = 0; i < doors1.size(); i++) {
			for (int j = 0; j < doors2.size(); j++) {
				if (doors1.get(i) == doors2.get(j)) {
					return doors1.get(i);
				}
			}
		}
		return result;
	}

	public static boolean isInSamePar(Door door1, Door door2) {
		boolean result = false;
		ArrayList<Integer> pars1 = door1.getmPartitions();
		ArrayList<Integer> pars2 = door2.getmPartitions();
		for (int par1 : pars1) {
			if (pars2.contains(par1)) {
				return true;
			}
		}
		return result;
	}

	public static int findCommonPar(int doorId1, int doorId2) {

		int par = -1;
		Door door1 = IndoorSpace.iDoors.get(doorId1);
		Door door2 = IndoorSpace.iDoors.get(doorId2);
		ArrayList<Integer> pars1 = door1.getmPartitions();
		ArrayList<Integer> pars2 = door2.getmPartitions();
		for (int par1 : pars1) {
			if (pars2.contains(par1)) {
				return par1;
			}
		}
		return par;
	}

	public static double calLowerBound(Point sPoint, Point tPoint, int parId) {
		double result = 0;
		double minDist = Constant.large;

		Partition par = IndoorSpace.iPartitions.get(parId);
		ArrayList<Integer> doors = par.getmDoors();

		if (doors.size() == 1) {
			Door door = IndoorSpace.iDoors.get(0);
			double dist1 = calLowerBoundP2P(sPoint, door);
			double dist2 = calLowerBoundP2P(door, tPoint);
			result = (dist1 + dist2) / DataGenConstant.traveling_speed + par.getWaitTime();

			return result;
		}

		for (int doorId1 : doors) {
			Door door1 = IndoorSpace.iDoors.get(doorId1);
			double dist1 = calLowerBoundP2P(sPoint, door1);
			for (int doorId2 : doors) {
				if (doorId1 == doorId2)
					continue;
				Door door2 = IndoorSpace.iDoors.get(doorId2);
				double dist2 = par.getdistMatrix().getDistance(doorId1, doorId2);
				double dist3 = calLowerBoundP2P(door2, tPoint);
				double dist = dist1 + dist2 + dist3;
				if (dist < minDist) {
					minDist = dist;
				}
			}
		}
		result = minDist / DataGenConstant.traveling_speed + par.getWaitTime();
		return result;
	}

	public static double calLowerBoundP2P(Point point1, Point point2) {

		if (point1 == null || point2 == null)
			return 0;

		double result = 0;
		double minDist = Constant.large;
		int floorId1 = point1.getmFloor();
		int floorId2 = point2.getmFloor();
		if (floorId1 == floorId2) {
			result = point1.eDist(point2);
			return result;
		}
		List<Integer> existDoors1 = IndoorSpace.iFloors.get(floorId1).getmDoors();
		List<Integer> existDoors2 = IndoorSpace.iFloors.get(floorId2).getmDoors();
		double floorDist = Math.abs(floorId1 - floorId2) * DataGenConstant.lenStairway;
		for (int doorId1 : existDoors1) {
			Door door1 = IndoorSpace.iDoors.get(doorId1);
			double dist1 = point1.eDist(door1);
			for (int doorId2 : existDoors2) {
				Door door2 = IndoorSpace.iDoors.get(doorId2);
				double dist2 = door2.eDist(point2);
				double dist = dist1 + floorDist + dist2;
				if (dist < minDist) {
					minDist = dist;
					result = minDist;
				}
			}
		}
		return result;
	}

	/**
	 * calculate dist v
	 *
	 * @param p1
	 * @param p2
	 * @return distance
	 */
	public static double distv(Point p1, Point p2) {
		return p1.eDist(p2);
	}

	/**
	 * locate a partition according to location string
	 *
	 * @param point
	 * @return partition
	 */
	public static Partition locPartition(Point point) {
		int partitionId = -1;

		int floor = point.getmFloor();
		ArrayList<Integer> pars = IndoorSpace.iFloors.get(floor).getmPartitions();
		for (int i = 0; i < pars.size(); i++) {
			Partition par = IndoorSpace.iPartitions.get(pars.get(i));
			if (point.getX() >= par.getX1() && point.getX() <= par.getX2() && point.getY() >= par.getY1()
					&& point.getY() <= par.getY2()) {
				partitionId = par.getmID();
				return IndoorSpace.iPartitions.get(partitionId);
			}
		}

		if (partitionId == -1)
			return null;
		return IndoorSpace.iPartitions.get(partitionId);
	}

	/**
	 * @param prev
	 * @param ds
	 * @param de
	 * @return a string path
	 */
	public static String getPath(ArrayList<ArrayList<Integer>> prev, int ds, int de) {
		String result = de + "";

//		System.out.println("ds = " + ds + " de = " + de + " " + prev[de].par + " " + prev[de].door);
		int currp = prev.get(de).get(0);
		int currd = prev.get(de).get(1);

		while (currd != ds) {
			result = currd + "\t" + result;
//			System.out.println("current: " + currp + ", " + currd + " next: " + prev[currd].toString());
			currp = prev.get(currd).get(0);
			currd = prev.get(currd).get(1);

		}

		result = currd + "\t" + result;

		return result;
	}

	public static void main(String arg[]) throws IOException {

	}

}
