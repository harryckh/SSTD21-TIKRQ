package algorithm;

import indoor_entitity.Door;

import indoor_entitity.IndoorSpace;
import indoor_entitity.Partition;
import indoor_entitity.Point;
import utilities.Constant;
import utilities.DataGenConstant;
import wordProcess.CandidatePartitions;

import java.io.IOException;
import java.util.*;


/**
 * Algorithm: process TIKRQ
 * 
 */
public class AlgSSA {
	private HashMap<String, String> d2dPath;  //storing the fastest path from d1 to d2
	private HashSet<String> infeasibleParSets; //storing the par set that are infeasible

	public static double alpha = 0.5;

	private double curKCost;

	// SSA
	/**
	 * 
	 * @param sPoint
	 * @param tPoint
	 * @param QW
	 * @param costMax = max. time
	 * @param tau     = relevance threshold
	 * @param k       = number of results
	 * @return
	 * @throws IOException
	 */
	public ArrayList<String> tikrq(Point sPoint, Point tPoint, ArrayList<String> QW, //
			double costMax, double scorMin, int k) throws IOException {
		// initialize
		d2dPath = new HashMap<>();
		infeasibleParSets = new HashSet<>();
		cntFindFeasiblePath = 0;
		PriorityQueue<ParSet> result = new PriorityQueue<ParSet>(Comparator.reverseOrder());

		Partition sPartition = CommonFunction.locPartition(sPoint);
		Partition tPartition = CommonFunction.locPartition(tPoint);

		// Step 1. (Candidate Key Partitions Finding)
		// find all key partitions
		CandidatePartitions candidatePartitions = new CandidatePartitions(QW, DataGenConstant.threshold);

		ArrayList<ArrayList<String>> canPars_list = candidatePartitions.findAllCandPars3();
		// pruning 1
		for (int i = 0; i < canPars_list.size(); i++) {
			ArrayList<String> par = canPars_list.get(i);
			int parId = Integer.parseInt(par.get(0));
			double lowerBound = CommonFunction.calLowerBound(sPoint, tPoint, parId);
			if (lowerBound > costMax) {
				canPars_list.remove(par);
				i--;
			}
		}

		Collections.sort(canPars_list, new Comparator<ArrayList<String>>() {

			@Override
			public int compare(ArrayList<String> a, ArrayList<String> b) {
				// TODO Auto-generated method stub

				/// Need to handle \alpha here!
				Partition parA = IndoorSpace.iPartitions.get(Integer.parseInt(a.get(0)));
				Partition parB = IndoorSpace.iPartitions.get(Integer.parseInt(b.get(0)));
				double costA = alpha * (double) parA.getStaticCost() / DataGenConstant.SC_MAX
						+ (1.0 - alpha) * (1 - Double.parseDouble(a.get(1)));
				double costB = alpha * (double) parB.getStaticCost() / DataGenConstant.SC_MAX
						+ (1.0 - alpha) * (1 - Double.parseDouble(b.get(1)));

				if (costA > costB)
					return 1;
				else if (costA == costB)
					return 0;
				else
					return -1;
			}

		});

		double wTimeMax = calWTimeMax(sPoint, tPoint, sPartition, tPartition, costMax);

		curKCost = Constant.large;

		ArrayList<ArrayList<ArrayList<String>>> canPars_new = new ArrayList<>(QW.size());
		for (int i = 0; i < QW.size(); i++) {
			canPars_new.add(new ArrayList<>());
		}
		for (int i = 0; i < canPars_list.size(); i++) {
			ParSet parSet = new ParSet(QW.size());
			ArrayList<String> curPar = canPars_list.get(i);
			int pos = Integer.parseInt(curPar.get(2));
			parSet.setPar(curPar, pos);

			findKeyParsSets(canPars_new, parSet, 0, //
					wTimeMax, costMax, sPoint, tPoint, sPartition, tPartition, result, k, pos, canPars_list);
			canPars_new.get(pos).add(curPar);

		}
		ArrayList<String> r = new ArrayList<>();
		while (result.size() > 0) {
			ParSet parSet = result.poll();
			r.add(parSet.toString() + " " + parSet.path);
		}
		Collections.reverse(r);

		return r;
	}

	// dynamic approach
	// find all key partition sets
	private void findKeyParsSets(ArrayList<ArrayList<ArrayList<String>>> canPars_all, ParSet parSet, int depth,
			double wTimeMax, double costMax, Point sPoint, Point tPoint, Partition sPartition, Partition tPartition,
			PriorityQueue<ParSet> result, int k, int pos, ArrayList<ArrayList<String>> canPars_list) {

		if (parSet.getwTime() > wTimeMax)
			return;

		// pruning 3
		if (depth == canPars_all.size()) {
			// base case

			if (parSet.calcTotalCost(depth) >= curKCost)
				return;
			// Step 3. (Feasible Route Finding)
			String feasiblePath = findFeasiblePath(sPoint, tPoint, sPartition, tPartition, parSet.getParSet(),
					parSet.getwTime(), costMax, canPars_list);

			if (feasiblePath != null && feasiblePath != "") {
				ParSet pSet = new ParSet(parSet);
				pSet.path = feasiblePath;
				result.add(pSet);

				if (result.size() > k) {
					result.poll();// remove max cost one
					curKCost = result.peek().calcTotalCost(canPars_all.size());
				}
				return;

			}
		} else {
			// recursive case
			if (depth == pos) {
				findKeyParsSets(canPars_all, parSet, depth + 1, wTimeMax, costMax, sPoint, tPoint, sPartition,
						tPartition, result, k, pos, canPars_list);
			} else {
				// ---------------
				// for each partition in this depth
				for (ArrayList<String> par : canPars_all.get(depth)) {
					parSet.setPar(par, depth);
					// pruning 2
					double costLB = parSet.calcTotalCostLB(canPars_all.size());
					if (costLB < curKCost) {
						findKeyParsSets(canPars_all, parSet, depth + 1, wTimeMax, costMax, sPoint, tPoint, sPartition,
								tPartition, result, k, pos, canPars_list);
					} else {
						parSet.removePar(depth);
						break;
					}
					parSet.removePar(depth);

				}
			}
		}
		return;
	}

	// calculate the max wait time
	private double calWTimeMax(Point ps, Point pt, Partition sPartition, Partition tPartition, double costMax) {
		double wTimeMax = 0;
		String dist_path = CommonFunction.findShortestPath(ps, pt, sPartition, tPartition);
		double dist = Double.parseDouble(dist_path.split("\t")[0]);
		double time = dist / DataGenConstant.traveling_speed;

//		System.out.println("s-to-t dist: "+dist + " time:" + time);

		wTimeMax = costMax - time;
		return wTimeMax;
	}

	int cntFindFeasiblePath = 0;

	// find feasible path for a partition set
	private String findFeasiblePath(Point sPoint, Point tPoint, Partition sPartition, Partition tPartition,
			short[] parSet, double setWaitTime, double costMax, ArrayList<ArrayList<String>> canPars_list) {
		String result = "";
		cntFindFeasiblePath++;
		int ps = -1;
		int pt = -2;

		if (sPoint.equals(tPoint)) {
			return sPoint.eDist(tPoint) + "\t";
		}

		if (sPartition.getmID() == tPartition.getmID()) {
			return sPoint.eDist(tPoint) + "\t";
		}

		/// initialize parList as partition not visited yet
		ArrayList<Integer> parList = new ArrayList<>();
//		for (ArrayList<String> par : parSet) 
		for (int i = 0; i < parSet.length; i++) {
			int parId = parSet[i];
			if (!(parId == sPartition.getmID() || parId == tPartition.getmID()))
				if (parId != -1)
					parList.add(parId);
		}

		// initialize the priority queues
		MinHeap<Stamp> Q = new MinHeap<>("set");

		// initialize stamp
		Stamp s0 = new Stamp(sPartition.getmID(), setWaitTime, setWaitTime + "\t" + "-1", parList, new ArrayList<>());
		Q.insert(s0);

		while (Q.heapSize > 0) {
			Stamp si = Q.delete_min();

			int parId = si.getParId();
			Partition partition = IndoorSpace.iPartitions.get(parId);

			ArrayList<Integer> parList_notVisited = si.getParList();

			String curPath = si.getR();
			String[] curPathArr = curPath.split("\t");
			double curTimeCost = Double.parseDouble(curPathArr[0]);
			int dkId = Integer.parseInt(curPathArr[curPathArr.length - 1]);

			// if the size of the set is 0
			if (parList_notVisited.size() == 0) {
				String subPath;
				if (parId == sPartition.getmID()) {
					subPath = d2dPath.get(ps + "-" + pt + "-" + parId);
					if (subPath == null) {
						subPath = CommonFunction.findFastestPathP2P(sPoint, tPoint, sPartition, tPartition,
								costMax - curTimeCost);
						d2dPath.put(ps + "-" + pt + "-" + parId, subPath);
					}

				} else {
					subPath = d2dPath.get(dkId + "-" + pt + "-" + parId);

					if (subPath == null) {
						Door dk = IndoorSpace.iDoors.get(dkId);
						subPath = CommonFunction.findFastestPathD2P(dk, tPoint, partition, tPartition,
								costMax - curTimeCost);
						d2dPath.put(dkId + "-" + pt + "-" + parId, subPath);
					}
				}
				if (subPath.equals("no route"))
					continue;
				String[] subPathArr = subPath.split("\t");
				double timeCost_last = Double.parseDouble(subPathArr[0]);


				double timeCost = curTimeCost + timeCost_last;
				if (timeCost < costMax) {
					String finalPath = "";
					for (int i = 1; i < curPathArr.length; i++) {
						finalPath += curPathArr[i] + "\t";
					}
					for (int i = 2; i < subPathArr.length - 1; i++) {
						finalPath += subPathArr[i] + "\t";
					}
					finalPath += pt + "";
					result = timeCost + "\t" + finalPath;
					return result;
				}

			}

			// batch processing remaining door of remaining partitions!
			/// calculate the fastest path from dk to all doors of par_next
			ArrayList<Integer> remainingDoors = new ArrayList<>();

			// gather the remaining doors
			for (int parId_next : parList_notVisited) {
				// check infeasible hashmap
				ArrayList<Integer> tempNewList = new ArrayList<>(si.getParList_visited());
				tempNewList.add(parId_next);
				if (isInfeasible(tempNewList)) {
					continue;
				}
				Partition par_next = IndoorSpace.iPartitions.get(parId_next);
				ArrayList<Integer> doorList_next = par_next.getmDoors();

				/// --------------------
				// check d2d path hashmap
				for (Integer door_next : doorList_next) {
					if (parId == sPartition.getmID()) {
						if (!d2dPath.containsKey(ps + "-" + door_next + "-" + parId))
							remainingDoors.add(door_next);
					} else {
						if (!d2dPath.containsKey(dkId + "-" + door_next + "-" + parId))
							remainingDoors.add(door_next);
					}
				}
			}
			// --------------------------------------------------------------

			// perform the batch path finding
			if (parId == sPartition.getmID()) {
				CommonFunction.findFastestPathsP2D(sPoint, remainingDoors, sPartition, d2dPath, costMax - curTimeCost);
			} else {
//					System.out.println("---dkid:" + dkId + " " + partition.getmID()+ " ---  " + remainingDoors.toString());
				Door dk = IndoorSpace.iDoors.get(dkId);
				CommonFunction.findFastestPathsD2D(dk, remainingDoors, partition, d2dPath, costMax - curTimeCost);
			}

			// --------------------------------------------------------------
			/// for each not visited partition
			/// extend from current path and generate a new stamp
			for (int parId_next : parList_notVisited) {
				boolean isFeasible = false;
				// check infeasible hashmap
				ArrayList<Integer> tempNewList = new ArrayList<>(si.getParList_visited());
				tempNewList.add(parId_next);
				if (isInfeasible(tempNewList)) {
//					System.out.println("asas");
					continue;
				}
//				if (!isFeasible(si.getParList_visited()))
//					continue;

				Partition par_next = IndoorSpace.iPartitions.get(parId_next);
				ArrayList<Integer> doorList_next = par_next.getmDoors();

				/// -------------------
				/// for each door in the next target partition
				for (int doorId_next : doorList_next) {
//				for (int idn = 0; idn < doorList_next.size(); idn++) {
//					int doorId_next = doorList_next.get(idn);
					Door door_next = IndoorSpace.iDoors.get(doorId_next);
					String subPath = "";

					if (parId == sPartition.getmID()) {
						subPath = d2dPath.get(ps + "-" + doorId_next + "-" + parId);
					} else {
						subPath = d2dPath.get(dkId + "-" + doorId_next + "-" + parId);
					}
//					System.out.println("subPath:" + subPath);
					if (subPath == null || subPath.equals("no route")) {
//						System.out.println("no route");
						continue;
					}

					String[] subPathArr = subPath.split("\t");
					// the door before door_next can not be a door if par_next
					if (subPathArr.length > 4
							&& CommonFunction.findCommonPar(Integer.parseInt(subPathArr[subPathArr.length - 1]),
									Integer.parseInt(subPathArr[subPathArr.length - 2])) == parId_next) {
//						System.out.println("skipped subPathArr.length:"+ subPathArr.length);

						continue;
					}
					double timeCost_next = Double.parseDouble(subPathArr[0]);
					double timeCost_last = CommonFunction.calLowerBoundP2P(door_next, tPoint)
							/ DataGenConstant.traveling_speed;
//                    System.out.println("time_last: " + timeCost_last + "  " + costMax);
					double newTimeCostLB = curTimeCost + timeCost_next + timeCost_last;
					if (newTimeCostLB < costMax) {
						/// create a new stamp and add to Q
						String newPath = "";
						for (int i = 1; i < curPathArr.length; i++) {
							newPath += curPathArr[i] + "\t";
						}
						for (int i = 2; i < subPathArr.length; i++) {
							newPath += subPathArr[i] + "\t";
						}
						double newTimeCost = curTimeCost + timeCost_next;
						newPath = newTimeCost + "\t" + newPath;
						ArrayList<Integer> newParList_notVisited = new ArrayList<>(parList_notVisited);
						newParList_notVisited.remove(parList_notVisited.indexOf(parId_next));

						ArrayList<Integer> newParList_Visited = new ArrayList<>(tempNewList);

						Stamp stamp = new Stamp(parId_next, newTimeCost, newPath, newParList_notVisited,
								newParList_Visited);
						Q.insert(stamp);

//						System.out.println(" new stamp inserted: " + stamp.toString());
						isFeasible = true;
					}
				} /// end for each door in this partition

				/// maintain the global inFeasibleParSet if it is not feasible
				/// only need to maintain remaining >1 for future checking
				if (!isFeasible) {
					if (parList_notVisited.size() > 1) {
						si.addPar_visited(parId_next);
						String s = convert2String(si.getParList_visited());
						infeasibleParSets.add(s);
					}
//					System.out.println("Adding to inPS: " +s);
					break; // since this partial route must be infeasible
				}
			} // end for each unvisited partition
		} /// end while loop
		return result;
	}

	/// create a string ordered by par id, separated by "-"
	private String convert2String(ArrayList<Integer> parList_visited) {
		// TODO Auto-generated method stub
		String s = "";
//		Collections.sort(parList_visited);

		for (Integer par : parList_visited) {
			s += par + "-";
		}

		return s;
	}

	// check if the par set is infeasible
	private boolean isInfeasible(ArrayList<Integer> pars) {
		String s = "";
		return isInfeasible_sub(pars, s, 0);

	}

	private boolean isInfeasible_sub(ArrayList<Integer> pars, String s, int i) {
		// TODO Auto-generated method stub
		if (i == pars.size()) {
			// base case
//			System.out.println("s:" + s );
			if (s == "")
				return false;

			// this set can/cannot be found in infeasiblePS, thus is true/false
			return infeasibleParSets.contains(s);

		} else {
			// recursive case
			String s2 = s + pars.get(i) + "-";
//			isFeasible_sub(pars, s, i + 1); //not include [i]
//			isFeasible_sub(pars, s2, i + 1); //include [i]
			// we use || here since one combination found is enough
			return isInfeasible_sub(pars, s, i + 1) || isInfeasible_sub(pars, s2, i + 1);
		}
	}

	/**
	 * calculate the indoor distance between two doors
	 *
	 * @param ds door id ds
	 * @param de door id de
	 * @return distance
	 */
	public static String d2dDist(int ds, int de) {
		// System.out.println("ds = " + IndoorSpace.iDoors.get(ds).toString() + " de = "
		// + IndoorSpace.iDoors.get(de).toString());
		String result = "";

		if (ds == de)
			return 0 + "\t";

		int size = IndoorSpace.iDoors.size();
		BinaryHeap<Double> H = new BinaryHeap<Double>(size);
		double[] dist = new double[size]; // stores the current shortest path distance
		// from source ds to a door de
//		PrevPair[] prev = new PrevPair[size]; // stores the corresponding previous partition
		// and door pair (v,di) through which the algorithm
		// visits the current door de.
		boolean[] visited = new boolean[size]; // mark door as visited

		for (int i = 0; i < size; i++) {
			int doorID = IndoorSpace.iDoors.get(i).getmID();
			if (doorID != i)
				System.out.println("something wrong_Helper_d2dDist");
			if (doorID != ds)
				dist[i] = Constant.large;
			else
				dist[i] = 0;

			// enheap
			H.insert(dist[i], doorID);

//			PrevPair pair = null;
//			prev[doorID] = pair;
		}

		while (H.heapSize > 0) {
			String[] str = H.delete_min().split(",");
			int di = Integer.parseInt(str[1]);
			double dist_di = Double.parseDouble(str[0]);

			// System.out.println("dequeue <" + di + ", " + dist_di + ">");

			if (di == de) {
				// System.out.println("d2dDist_ di = " + di + " de = " + de);
//				result += getPath(prev, ds, de);
				return result = dist_di + "\t" + result;
			}

			visited[di] = true;
			// System.out.println("d" + di + " is newly visited");

			Door door = IndoorSpace.iDoors.get(di);
			ArrayList<Integer> parts = new ArrayList<Integer>(); // list of leavable partitions
			parts = door.getD2PLeave();

			int partSize = parts.size();

			for (int i = 0; i < partSize; i++) {
				ArrayList<Integer> doorTemp = new ArrayList<Integer>();
				int v = parts.get(i); // partition id
				Partition partition = IndoorSpace.iPartitions.get(v);
				doorTemp = partition.getmDoors();

				// remove the visited doors
				ArrayList<Integer> doors = new ArrayList<Integer>(); // list of unvisited leavable doors
				int doorTempSize = doorTemp.size();
				for (int j = 0; j < doorTempSize; j++) {
					int index = doorTemp.get(j);
					// System.out.println("index = " + index + " " + !visited[index]);
					if (!visited[index]) {
						doors.add(index);
					}
				}

				int doorSize = doors.size();
				// System.out.println("doorSize = " + doorSize + ": " +
				// Functions.printIntegerList(doors));

				for (int j = 0; j < doorSize; j++) {
					int dj = doors.get(j);
					if (visited[dj])
						System.out.println("something wrong_Helper_d2dDist2");
					// System.out.println("for d" + di + " and d" + dj);

					double fd2d = partition.getdistMatrix().getDistance(di, dj);
					;
					if (fd2d == -1) {
						int fid1 = IndoorSpace.iPartitions.get(IndoorSpace.iDoors.get(di).getmPartitions().get(0))
								.getmFloor();
						int fid2 = IndoorSpace.iPartitions.get(IndoorSpace.iDoors.get(dj).getmPartitions().get(0))
								.getmFloor();
						fd2d = DataGenConstant.lenStairway * (Math.abs(fid1 - fid2));
						// System.out.println("fid1 = " + fid1 + " fid2 = " + fid2 + " fd2d = " + fd2d);
					}

					if ((dist[di] + fd2d) < dist[dj]) {
						double oldDj = dist[dj];
						dist[dj] = dist[di] + fd2d;
						H.updateNode(oldDj, dj, dist[dj], dj);
//						prev[dj] = new PrevPair(v, di);
//						prev[dj].toString();
					}
				}
			}
		}
		return result;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testRun();
	}

	public static void testRun() throws Exception {
		Init.init();
		AlgSSA algo = new AlgSSA();

		ArrayList<String> result = new ArrayList<>();

		Runtime runtime1 = Runtime.getRuntime();
		runtime1.gc();
		long startMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long startTime1 = System.currentTimeMillis();

//		result = algo.tikrq(new Point(1299.0, 1127.0, 1), new Point(113.0, 153.0, 2),//
//				new ArrayList<>(Arrays.asList("shoes", "china construction bank (asia)")), //
//				20000, 0.5, 3);
//		result =  algo.tikrq(new Point(1299.0, 1127.0, 1), new Point(113.0, 153.0, 4), //
//				new ArrayList<>(Arrays.asList("3192", "3166", "-78", "-47")), //
//				5000, 1, 7);
//		result = algo.tikrq(new Point(521.0, 1066.0, 4), new Point(555.0, 753.0, 2), //
//				new ArrayList<>(Arrays.asList("restaurant", "-457", "-642", "-726", "bank")), //
//				5000, 1, 7);
//		result =  algo.tikrq(new Point(179.0, 142.0, 0), new Point(1310.0, 1203.0, 0), //
//		new ArrayList<>(Arrays.asList("-903", "-1039", "-749", "food", "83")), //
//		5000, 1, 7);
//		result =  algo.tikrq(new Point(1259.0, 1258.0, 3), new Point(611.0, 862.0, 3), //
//				new ArrayList<>(Arrays.asList("390", "-1059", "-318", "7560", "5587")), //
//				5000, 0.5, 7);
		result = algo.tikrq(new Point(170.0, 621.0, 3), new Point(1074.0, 889.0, 4), //
				new ArrayList<>(Arrays.asList("-1053", "-131", "-574", "-811")), //
				5000, 0.5, 7);

		System.out.println("result--------------");
		for (String path : result) {
			System.out.println(path);
		}

		long endTime1 = System.currentTimeMillis();
		long endMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long mem1 = (endMem1 - startMem1) / 1024 / 1024;
		long time1 = endTime1 - startTime1;

		System.out.println("time(ms): " + time1 + "; mem(mb): " + mem1);

	}

}
