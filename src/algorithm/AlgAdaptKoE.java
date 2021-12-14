package algorithm;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import indoor_entitity.Door;
import indoor_entitity.IndoorSpace;
import indoor_entitity.Partition;
import indoor_entitity.Point;
import utilities.Constant;
import utilities.DataGenConstant;
import wordProcess.CandidatePartitions;
import wordProcess.ReadWord;

public class AlgAdaptKoE {
	private HashMap<String, String> d2dPath;
	private HashSet<String> infeasibleParSets;

	public AlgAdaptKoE() {

	}

	private double curKCost;

	public ArrayList<String> KoE(Point sPoint, Point tPoint, ArrayList<String> QW, //
			double costMax, int k) throws IOException {

		int ps = -1;
		int pt = -2;

		d2dPath = new HashMap<String, String>();
		infeasibleParSets = new HashSet<>();

		PriorityQueue<ParSet> result = new PriorityQueue<ParSet>(Comparator.reverseOrder());

		Partition sPartition = CommonFunction.locPartition(sPoint);
		Partition tPartition = CommonFunction.locPartition(tPoint);

		// Step 1. (Candidate Key Partitions Finding)
		// find all key partitions
//		System.out.println("finding candidate partition... ");
		CandidatePartitions candidatePartitions = new CandidatePartitions(QW, DataGenConstant.threshold);

		ArrayList<ArrayList<ArrayList<String>>> canPars_all = candidatePartitions.findAllCandPars();

		double wTimeMax = calWTimeMax(sPoint, tPoint, sPartition, tPartition, costMax);
		curKCost = Constant.large;

		// ---
		// initialize the piority queues
		MinHeap<Stamp> Q = new MinHeap<>("set");

		// initialize stamp
		ParSet parSet0 = new ParSet(QW.size());

		double[] s0relScore = findRelScore(QW, canPars_all, sPartition.getmID());

		ArrayList<Integer> p = new ArrayList<>();// useless but for sorting purpose only
		for (int i = 0; i < QW.size(); i++) {
			if (s0relScore[i] != 0) {
				ArrayList<String> tmp = new ArrayList<>();
				tmp.add(sPartition.getmID() + "");
				tmp.add(s0relScore[i] + "");
				parSet0.setPar(tmp, i);
			}
			p.add(i);// some random thing, only size matter, equal to # uncovered key
		}
		Stamp s0 = new Stamp(sPartition.getmID(), 0, "0" + "\t" + "-1", p, null);
		s0.parSet = parSet0;

		Q.insert(s0);
		int h = 0;
		while (Q.heapSize > 0) {
			Stamp si = Q.delete_min();

			int parId = si.getParId();
			String curPath = si.getR();
			String[] curPathArr = curPath.split("\t");
			double curTimeCost = Double.parseDouble(curPathArr[0]);
			int dkId = Integer.parseInt(curPathArr[curPathArr.length - 1]);

			ParSet parSet = si.parSet;
			Partition partition = IndoorSpace.iPartitions.get(parId);

//			System.out.println((h++) + " " + si.toString());

			if (si.parList.size() == 0) {
				// connect to pt
				String subPath = "";
				if (d2dPath.get(dkId + "-" + pt + "-" + parId) != null) {
					subPath = d2dPath.get(dkId + "-" + pt + "-" + parId);
				} else {
					Door dk = IndoorSpace.iDoors.get(dkId);
					subPath = CommonFunction.findFastestPathD2P(dk, tPoint, partition, tPartition, costMax-curTimeCost);
					if (subPath != null && !subPath.equals("no route"))
					d2dPath.put(dkId + "-" + pt + "-" + parId, subPath);
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
					finalPath += "-2" + "";
					ParSet parSet2 = new ParSet(si.parSet);
					parSet2.path = timeCost + "\t" + finalPath;
					double totalCost = parSet2.calcTotalCost(QW.size());

					if (totalCost < curKCost && uniqueParSet(result, parSet2)) {

						result.add(parSet2);

						if (result.size() > k) {
							result.poll();// remove max cost one
							curKCost = result.peek().calcTotalCost(canPars_all.size());
//						System.out.println(result);
//						System.out.println("curKCost:" + curKCost);

						}
					}

					continue;
//					return result;
				}

			}

			// --------------------------------------------------

			// batch processing remaining door of remaining partitions!
			/// calculate the fastest path from dk to all doors of par_next
			ArrayList<Integer> remainingDoors = new ArrayList<>();

			// for each keyword that are not covered
			for (int i = 0; i < canPars_all.size(); i++) {
				if (parSet.getPar(i) != -1)
					continue;// already covered
				ArrayList<ArrayList<String>> candPars = canPars_all.get(i);
				for (ArrayList<String> candPar : candPars) {

					// --
//					next target partition
					int parId_next = Integer.parseInt(candPar.get(0));

					parSet.setPar(candPar, i);
					double costLB = parSet.calcTotalCostLB(QW.size());
					if (costLB >= curKCost) {
						parSet.removePar(i);
						continue;
					}
					parSet.removePar(i);

					if (isInfeasible(parSet.getParSet(), parId_next)) {
//						System.out.println("asas");
						continue;
					}

					Partition par_next = IndoorSpace.iPartitions.get(parId_next);
					ArrayList<Integer> doorList_next = par_next.getmDoors();
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
			}
			// --------------------------------------------------
			// perform the batch path finding
			if (parId == sPartition.getmID()) {
				CommonFunction.findFastestPathsP2D(sPoint, remainingDoors, sPartition, d2dPath, costMax-curTimeCost);
			} else {
//					System.out.println("---dkid:" + dkId + " " + partition.getmID()+ " ---  " + remainingDoors.toString());
				Door dk = IndoorSpace.iDoors.get(dkId);
				CommonFunction.findFastestPathsD2D(dk, remainingDoors, partition, d2dPath, costMax-curTimeCost);
			}
			// --------------------------------------------------
			// for each keyword that are not covered
			for (int i = 0; i < canPars_all.size(); i++) {
				if (parSet.getPar(i) != -1)
					continue;// already covered
				ArrayList<ArrayList<String>> candPars = canPars_all.get(i);
				for (ArrayList<String> candPar : candPars) {

					// --
//					next target partition
					int parId_next = Integer.parseInt(candPar.get(0));

					parSet.setPar(candPar, i);
					double costLB = parSet.calcTotalCostLB(QW.size());
					if (costLB >= curKCost) {
						parSet.removePar(i);
						continue;
					}
					parSet.removePar(i);

					if (isInfeasible(parSet.getParSet(), parId_next)) {
//						System.out.println("asas");
						continue;
					}
					boolean isFeasible = false;

					Partition par_next = IndoorSpace.iPartitions.get(parId_next);
					ArrayList<Integer> doorList_next = par_next.getmDoors();
					/// for each door in the next target partition
					for (int doorId_next : doorList_next) {
						Door door_next = IndoorSpace.iDoors.get(doorId_next);
						String subPath = "";
						if (parId == sPartition.getmID())
						subPath = d2dPath.get(ps + "-" + doorId_next + "-" + parId);
						
						else {
							subPath = d2dPath.get(dkId + "-" + doorId_next + "-" + parId);
						}

						// System.out.println("subPath:" + subPath);
						if (subPath==null|| subPath.equals("no route")) {
//							System.out.println("no route");
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
						double timeCostLB = curTimeCost + timeCost_next + timeCost_last + par_next.getWaitTime();
						if (timeCostLB < costMax) {
							/// create a new stamp and add to Q
							String newPath = "";
							for (int k1 = 1; k1 < curPathArr.length; k1++) {
								newPath += curPathArr[k1] + "\t";
							}
							for (int k1 = 2; k1 < subPathArr.length; k1++) {
								newPath += subPathArr[k1] + "\t";
							}
							double timeCost = curTimeCost + timeCost_next+ par_next.getWaitTime() ;
							newPath = timeCost + "\t" + newPath;
							// create new stamp
							Stamp sNew = new Stamp(si);
							sNew.parId = parId_next;
							sNew.cost = timeCost;
							sNew.R = newPath;
							sNew.parList.remove(0);
							sNew.parSet.setPar(candPar, i);
							sNew.parSet.path = newPath;

							Q.insert(sNew);
							isFeasible = true;
//						System.out.println(" new stamp inserted: " + sNew.toString());
						}
						// --

					}
					if (!isFeasible) {
						if (parSet.getParSet().length < QW.size()) {
							parSet.setPar(candPar, i);
							String s = convert2String(parSet.getParSet());
							infeasibleParSets.add(s);
						}
						break;
					}
				}
			}

		}

		// ---

		ArrayList<String> r = new ArrayList<>();
		while (result.size() > 0) {
			ParSet parSet1 = result.poll();
			r.add(parSet1.toString() + " " + parSet1.path);
		}
		Collections.reverse(r);

		return r;
	}

	/**
	 * 
	 * @param result
	 * @param parSet2
	 * @return true if parSet2 is not found in result
	 */
	private boolean uniqueParSet(PriorityQueue<ParSet> result, ParSet parSet2) {
		// TODO Auto-generated method stub

		Iterator<ParSet> it = result.iterator();

		while (it.hasNext()) {
//			ParSet parSet = it.next();
//			System.out.println(parSet2);
			if (it.next().equals(parSet2))
				return false;
		}
		return true;
	}


	// calculate the max wait time
	private double calWTimeMax(Point ps, Point pt, Partition sPartition, Partition tPartition, double costMax) {
		double wTimeMax = 0;
		String dist_path = CommonFunction.findShortestPath(ps, pt, sPartition, tPartition);
		double dist = Double.parseDouble(dist_path.split("\t")[0]);
		double time = dist / DataGenConstant.traveling_speed;
		wTimeMax = costMax - time;
		return wTimeMax;
	}

	/**
	 * 
	 * @param QW
	 * @param canPars_all
	 * @param parId
	 * @return
	 */
	private double[] findRelScore(ArrayList<String> QW, ArrayList<ArrayList<ArrayList<String>>> canPars_all,
			int parId) {

		double[] relScore = new double[QW.size()];

//			double score = -1;

		Partition par = IndoorSpace.iPartitions.get(parId);

		int iword = par.getIkeyword();
		ArrayList<Integer> twords = par.getTkeywords();
		// search for each query word
		for (int i = 0; i < QW.size(); i++) {
			String word = QW.get(i);
			if (DataGenConstant.cWords.contains(word)) {
				// c-word
				String category = par.getCategory();
				if (word.equals(category)) {
					relScore[i] = 1.0;
				}
			} else if (ReadWord.index_itword.get(word) != null) {
				// this query keyword is a i-word/t-word in par
				// we find the score from allCandPar

				for (ArrayList<String> p : canPars_all.get(i)) {
					if (Integer.parseInt(p.get(0)) == parId) {
						relScore[i] = Double.parseDouble(p.get(1));
//								score += Double.parseDouble(p.get(1));
					}

				}

			}
		}

		return relScore;
	}

	// check if the par set is infeasible
	private boolean isInfeasible(short[] pars, int mustInclude) {

		String s = "";
		return isInfeasible_sub(pars, mustInclude, s, 0);

//		return true;
	}

	private boolean isInfeasible_sub(short[] pars, int mustInclude, String s, int i) {
		// TODO Auto-generated method stub
		if (i == pars.length) {
			// base case
			if (s == "")
				return false;
			s += mustInclude + "-";
//			System.out.println("s:" + s);
			// this set can/cannot be found in infeasiblePS, thus is true/false
			return infeasibleParSets.contains(s);

		} else {
			// recursive case
			if (pars[i] == -1)
				return isInfeasible_sub(pars, mustInclude, s, i + 1);
			String s2 = s + pars[i] + "-";
//			isFeasible_sub(pars, s, i + 1); //not include [i]
//			isFeasible_sub(pars, s2, i + 1); //include [i]
			// we use || here since one combination found is enough
			return isInfeasible_sub(pars, mustInclude, s, i + 1) || isInfeasible_sub(pars, mustInclude, s2, i + 1);
		}
	}

	/// create a string ordered by par id, separated by "-"
	private String convert2String(short[] parList_visited) {
		// TODO Auto-generated method stub
		String s = "";
//			Collections.sort(parList_visited);

		for (short par : parList_visited) {
			s += par + "-";
		}

		return s;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testRun();
//		testUniqueParSet();
	}

	public static void testUniqueParSet() {
		PriorityQueue<ParSet> result = new PriorityQueue<ParSet>(Comparator.reverseOrder());
		ParSet p1 = new ParSet(2);
		p1.path = "-1\t200";
		result.add(p1);

		ParSet p2 = new ParSet(2);
		p2.path = "-1\t2200";

		AlgAdaptKoE algo_adapt = new AlgAdaptKoE();
		Boolean b = algo_adapt.uniqueParSet(result, p2);
		System.out.println(b);
	}

	private static void testRun() throws Exception {
		// TODO Auto-generated method stub
		Init.init();

		AlgAdaptKoE algo_adapt = new AlgAdaptKoE();

		ArrayList<String> result = new ArrayList<>();

		Runtime runtime1 = Runtime.getRuntime();
		runtime1.gc();
		long startMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long startTime1 = System.currentTimeMillis();
//
//		result = algo_adapt.KoE(new Point(1299.0, 1127.0, 1), //
//				new Point(113.0, 153.0, 2), //
//				new ArrayList<>(Arrays.asList("shoes", "other")), //
//				10000,  3);

//		result = algo_adapt.KoE(new Point(1299.0, 1127.0, 1), new Point(113.0, 153.0, 4), //
//				new ArrayList<>(Arrays.asList("3192", "3166", "-78")), //
//				5000, 7);
//		result = algo_adapt.KoE(new Point(1299.0, 1127.0, 1), new Point(113.0, 153.0, 4), //
//				new ArrayList<>(Arrays.asList("3192", "3166", "-78", "-47")), //
//				5000, 7);
		result = algo_adapt.KoE(new Point(238.0, 415.0, 4), new Point(1164.0, 354.0, 4), //
				new ArrayList<>(Arrays.asList("2871", "-701", "787", "bags", "-1195")), //
				2400,  7);
	
		long endTime1 = System.currentTimeMillis();
		long endMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long mem1 = (endMem1 - startMem1) / 1024 / 1024;
		long time1 = endTime1 - startTime1;

		System.out.println("result--------------");
		for (String path : result) {
			System.out.println(path);
		}

		System.out.println("time(ms): " + time1 + "; mem(kb): " + mem1);
	}

}
