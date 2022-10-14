package algorithm;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import indoor_entitity.Door;
import indoor_entitity.IndoorSpace;
import indoor_entitity.Partition;
import indoor_entitity.Point;
import utilities.Constant;
import utilities.DataGenConstant;
import wordProcess.CandidatePartitions;

public class AlgBaseline {

	private double curKCost;

	public ArrayList<String> baseline(Point sPoint, Point tPoint, ArrayList<String> QW, //
			double costMax, double scorMin, int k) throws IOException {
		return baseline(sPoint, tPoint, QW, costMax, scorMin, k, 0);
	}

	// SSA\P
	public ArrayList<String> baseline(Point sPoint, Point tPoint, ArrayList<String> QW, //
			double costMax, double scorMin, int k, int orderedQW) throws IOException {
		// initialize...
//		d2dPath = new HashMap<>();
//		infeasibleParSets = new HashSet<>();

		PriorityQueue<ParSet> result = new PriorityQueue<ParSet>(Comparator.reverseOrder());

		Partition sPartition = CommonFunction.locPartition(sPoint);
		Partition tPartition = null;
		if (tPoint != null)
			tPartition = CommonFunction.locPartition(tPoint);

		// Step 1. (Candidate Key Partitions Finding)
		// find all key partitions
//		System.out.println("finding candidate partition... ");
		CandidatePartitions candidatePartitions = new CandidatePartitions(QW, DataGenConstant.threshold);

		ArrayList<ArrayList<String>> canPars_list = candidatePartitions.findAllCandPars3();
		double wTimeMax = Constant.large;
//		System.out.println("wTimeMax: " + wTimeMax);

		curKCost = Constant.large;

		/// TODO:can be further optimize
		ArrayList<ArrayList<ArrayList<String>>> canPars_new = new ArrayList<>(QW.size());
		for (int i = 0; i < QW.size(); i++) {
			canPars_new.add(new ArrayList<>());
		}

		for (int i = 0; i < canPars_list.size(); i++) {
			ParSet parSet = new ParSet(QW.size());
			ArrayList<String> curPar = canPars_list.get(i);
			int pos = Integer.parseInt(curPar.get(2));
			parSet.setPar(curPar, pos);
//			System.out.println("----- i:"+ i+ " " + parSet.toString() + " -------");
			findKeyParsSets3(canPars_new, parSet, 0, //
					wTimeMax, costMax, sPoint, tPoint, sPartition, tPartition, result, k, pos, orderedQW);
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
	private void findKeyParsSets3(ArrayList<ArrayList<ArrayList<String>>> canPars_all, ParSet parSet, int depth,
			double wTimeMax, double costMax, Point sPoint, Point tPoint, Partition sPartition, Partition tPartition,
			PriorityQueue<ParSet> result, int k, int pos, int orderedQW) {

//		System.out.println("findKeyParsSets3 " + depth + " " + Arrays.toString(parSet.getParSet()));
		// --
//		if (parSet.getwTime() > wTimeMax)
//			return;

		// pruning 3
//        System.out.println("check wait time");
//        System.out.println("wTimeMax: " + wTimeMax + "; wTime: " + wTime);

		if (depth == canPars_all.size()) {
			// base case

			if (parSet.calcTotalCost(depth) >= curKCost)
				return;
			// Step 3. (Feasible Route Finding)

//	        System.out.println("findFeasiblePath--------");
			String feasiblePath = findFeasiblePath2(sPoint, tPoint, sPartition, tPartition, parSet.getParSet(), parSet.getwTime(), costMax,
					orderedQW);
			if (feasiblePath != null && feasiblePath != "") {
//				System.out.println("feasiblePath:" + parSet.toString() + " " + feasiblePath);
				ParSet pSet = new ParSet(parSet);
				pSet.path = feasiblePath;
				result.add(pSet);

				if (result.size() > k) {
					result.poll();// remove max cost one
					curKCost = result.peek().calcTotalCost(canPars_all.size());
					return;
				}

			}
		} else {

			if (depth == pos) {
				findKeyParsSets3(canPars_all, parSet, depth + 1, wTimeMax, costMax, sPoint, tPoint, sPartition,
						tPartition, result, k, pos, orderedQW);
			} else {
				// recursive case
				// for each partition in this depth
				for (ArrayList<String> par : canPars_all.get(depth)) {
//				int parId = Integer.parseInt(par.get(0));
//				Partition partition = IndoorSpace.iPartitions.get(parId);

					parSet.setPar(par, depth);
					// pruning 2
					double costLB = parSet.calcTotalCostLB(canPars_all.size());
//				double costLB2 = parSet.calcTotalCostLB2(canPars_all);
//System.out.println(costLB + " " + costLB2);
//					if (costLB < curKCost) {
					findKeyParsSets3(canPars_all, parSet, depth + 1, wTimeMax, costMax, sPoint, tPoint, sPartition,
							tPartition, result, k, pos, orderedQW);
//					} else {
////					System.out.println("costLB2<curKcost "+ costLB2 + " " + Arrays.toString( parSet.getParSet()));
//						parSet.removePar(depth);
//						break;
//					}
					parSet.removePar(depth);

				}
			}
		}
		return;
	}

	// find feasible path for a partition set
	private String findFeasiblePath2(Point sPoint, Point tPoint, Partition sPartition, Partition tPartition,
			short[] parSet, double setWaitTime, double costMax, int orderedQW) {
		String result = "";
//		cntFindFeasiblePath++;
		int ps = -1;
		int pt = -2;

		if (tPoint != null) {
			if (sPoint.equals(tPoint)) {
				return sPoint.eDist(tPoint) + "\t";
			}

			if (sPartition.getmID() == tPartition.getmID()) {
				return sPoint.eDist(tPoint) + "\t";
			}
		}
		// ------------

		/// initialize parList as partition not visited yet
		ArrayList<Integer> parList = new ArrayList<>();
//		for (ArrayList<String> par : parSet) 
		for (int i = 0; i < parSet.length; i++) {
			int parId = parSet[i];
			if (!(parId == sPartition.getmID() || (tPartition!=null && parId == tPartition.getmID())))
				if (parId != -1)
					parList.add(parId);
		}

		/// Infeasible par set check
//		if (isInfeasible(parList)) {
////			System.out.println("return by Infeasible ");
//			return "";
//		}
		// initialize the priority queues
		MinHeap<Stamp> Q = new MinHeap<>("set");

		// initialize stamp
//		Stamp s0 = new Stamp(sPartition.getmID(), 0, "0" + "\t" + "-1", parList, new ArrayList<>());
		Stamp s0 = new Stamp(sPartition.getmID(), 0, setWaitTime + "\t" + "-1", parList, new ArrayList<>());

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

//			System.out.println(si.toString());
//			System.out.println(curPath);

			// if the size of the set is 0
			if (parList_notVisited.size() == 0) {
				String[] subPathArr = null;
				double timeCost_last = 0;
				if (tPoint != null) {
					String subPath;
					if (parId == sPartition.getmID()) {
						subPath = CommonFunction.findFastestPathP2P(sPoint, tPoint, sPartition, tPartition,
								costMax - curTimeCost);

					} else {
						Door dk = IndoorSpace.iDoors.get(dkId);
						subPath = CommonFunction.findFastestPathD2P(dk, tPoint, partition, tPartition,
								costMax - curTimeCost);
					}
					if (subPath.equals("no route"))
						continue;
					subPathArr = subPath.split("\t");
					timeCost_last = Double.parseDouble(subPathArr[0]);
				}
//				p2pDist.put(parId+ "-" + pt, timeCost_last);

				double timeCost = curTimeCost + timeCost_last;
				if (timeCost < costMax) {
					String finalPath = "";
					for (int i = 1; i < curPathArr.length; i++) {
						finalPath += curPathArr[i] + "\t";
					}
					if (tPoint != null) {
						for (int i = 2; i < subPathArr.length - 1; i++) {
							finalPath += subPathArr[i] + "\t";
						}
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
				boolean isFeasible = false;
				// check infeasible hashmap
				ArrayList<Integer> tempNewList = new ArrayList<>(si.getParList_visited());
				tempNewList.add(parId_next);
//				if (isInfeasible(tempNewList)) {
////					System.out.println("asas");
//					continue;
//				}
				Partition par_next = IndoorSpace.iPartitions.get(parId_next);
				ArrayList<Integer> doorList_next = par_next.getmDoors();

				/// --------------------

				// check d2d path hashmap
				for (Integer door_next : doorList_next) {
//					if (parId == sPartition.getmID()) {
//						if (!d2dPath.containsKey(ps + "-" + door_next + "-" + parId))
//							remainingDoors.add(door_next);
//					} else {
//						if (!d2dPath.containsKey(dkId + "-" + door_next + "-" + parId))
					remainingDoors.add(door_next);
//					}
				}

				if (orderedQW == 1)
					break;
			}
			// --------------------------------------------------------------
			HashMap<String, String> d2dPath = new HashMap<>();
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
//				if (isInfeasible(tempNewList)) {
////					System.out.println("asas");
//					continue;
//				}
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
					// --
//					p2pDist.put(parId+ "-" + parId_next, timeCost_next);
					// --
//					double timeCost_last = CommonFunction.calLowerBoundP2P(door_next, tPoint)
//							/ DataGenConstant.traveling_speed;
//                    System.out.println("time_last: " + timeCost_last + "  " + costMax);
//					double timeCostLB = curTimeCost + timeCost_next + timeCost_last;
//					if (timeCostLB < costMax)
					{
						/// create a new stamp and add to Q
						String newPath = "";
						for (int i = 1; i < curPathArr.length; i++) {
							newPath += curPathArr[i] + "\t";
						}
						for (int i = 2; i < subPathArr.length; i++) {
							newPath += subPathArr[i] + "\t";
						}
						double timeCost = curTimeCost + timeCost_next;
						newPath = timeCost + "\t" + newPath;
						ArrayList<Integer> newParList_notVisited = new ArrayList<>(parList_notVisited);
						newParList_notVisited.remove(parList_notVisited.indexOf(parId_next));

						ArrayList<Integer> newParList_Visited = new ArrayList<>(tempNewList);

						Stamp stamp = new Stamp(parId_next, timeCost, newPath, newParList_notVisited,
								newParList_Visited);
						Q.insert(stamp);

//						System.out.println(" new stamp inserted: " + stamp.toString());
						isFeasible = true;
					}
				} /// end for each door in this partition

				/// maintain the global inFeasibleParSet if it is not feasible
				/// only need to maintain remaining >1 for future checking
//				if (!isFeasible) {
//					if (parList_notVisited.size() > 1) {
//						si.addPar_visited(parId_next);
//						String s = convert2String(si.getParList_visited());
//						infeasibleParSets.add(s);
//					}
////					System.out.println("Adding to inPS: " +s);
//					break; // since this partial route must be infeasible
//				}

				if (orderedQW == 1)
					break;
			} // end for each unvisited partition
		} /// end while loop
		return result;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testRun();
	}

	public static void testRun() throws Exception {
		Init.init();
		AlgBaseline algo = new AlgBaseline();

		ArrayList<String> result = new ArrayList<>();

		Runtime runtime1 = Runtime.getRuntime();
		runtime1.gc();
		long startMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long startTime1 = System.currentTimeMillis();

//		result = algo.baseline(new Point(1299.0, 1127.0, 1), new Point(113.0, 153.0, 2),//
//				new ArrayList<>(Arrays.asList("shoes", "china construction bank (asia)")), //
//				20000, 0.5, 3);
//		result = algo.baseline(new Point(1299.0, 1127.0, 1), new Point(113.0, 153.0, 4), //
//				new ArrayList<>(Arrays.asList("3192", "3166", "-78", "-47")), //
//				5000, 1, 7);
//		result = algo.baseline(new Point(521.0, 1066.0, 4), new Point(555.0, 753.0, 2), //
//				new ArrayList<>(Arrays.asList("restaurant", "-457", "-642", "-726", "bank")), //
//				5000, 1, 7);
//		result = algo.baseline(new Point(179.0, 142.0, 0), new Point(1310.0, 1203.0, 0), //
//		new ArrayList<>(Arrays.asList("-903", "-1039", "-749", "food", "83")), //
//		5000, 1, 7);
//		result = algo.baseline(new Point(1259.0, 1258.0, 3), new Point(611.0, 862.0, 3), //
//				new ArrayList<>(Arrays.asList("390", "-1059", "-318", "7560", "5587")), //
//				5000, 0.5, 7);
		result = algo.baseline(new Point(170.0, 621.0, 3), null, //
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
