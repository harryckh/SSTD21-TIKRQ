package algorithm;

import java.io.IOException;
import java.util.*;

import indoor_entitity.IndoorSpace;
import indoor_entitity.Partition;
import indoor_entitity.Point;
import utilities.Constant;
import utilities.DataGenConstant;
import wordProcess.CandidatePartitions;

/**
 * 
 * @author harry
 *
 *
 *         Extension on Session-based TIKRQ
 */
public class AlgExtension {

	// we model the situation that the parameter is updated after query execution
	// here costMax is updated
	// we assured that costMaxNew < costMax
	public ArrayList<String> resultUpdateCaseC(AlgSSA algSSA, Point sPoint, Point tPoint, ArrayList<String> QW, //
			double costMax, double scorMin, int k, double costMaxNew)
			throws IOException {
		PriorityQueue<ParSet> resultPrev = algSSA.getResult();
//		System.out.println("size: " + resultPrev.size());
		ArrayList<ArrayList<String>> canPars_list = algSSA.getCanPars_list();
		Partition sPartition = CommonFunction.locPartition(sPoint);
		Partition tPartition = CommonFunction.locPartition(tPoint);


		if (costMaxNew >= costMax)
			return null;

		PriorityQueue<ParSet> resultNew = new PriorityQueue<ParSet>(Comparator.reverseOrder());

		/* Phase 1. check resultPrev */
		checkResultValidityCaseC(algSSA, sPoint, tPoint, sPartition, tPartition, resultPrev, costMaxNew, resultNew);

		if (resultNew.size() < k) {

			/* Phase 2. add new result if needed */
			addNewResult(algSSA, sPoint, tPoint, sPartition, tPartition, costMaxNew, k, QW.size(), canPars_list, resultNew, "caseC");

		}
		// convert each result to string and return
		ArrayList<String> r = new ArrayList<>();
		while (resultNew.size() > 0) {
			ParSet parSet = resultNew.poll();
			r.add(parSet.toString() + " " + parSet.getTimeCost() + " " + parSet.getParSetPath());
//			System.out.println(parSet.toString());
		}
		Collections.reverse(r);

		return r;
	}

	private void checkResultValidityCaseC(AlgSSA algSSA, Point sPoint, Point tPoint, Partition sPartition,
										  Partition tPartition, PriorityQueue<ParSet> resultPrev, double costMaxNew,
			PriorityQueue<ParSet> resultNew) {

		while (resultPrev.size() > 0) {
			ParSet parSet = resultPrev.poll();
			double timecost = parSet.getTimeCost();
//			System.out.println("timecost: " + timecost);
			if (timecost < costMaxNew) {
				resultNew.add(parSet);
				short[] parSetArr = parSet.getParSet();
				String parSetString = "";
				for (int i = 0; i < parSetArr.length; i++) {
					parSetString += parSetArr[i];
				}
				algSSA.inResultParSet.add(parSetString);
//				System.out.println("parsetString: " + parSetString);
			}
			else {
				String feasiblePath = algSSA.findFeasiblePath(sPoint, tPoint, sPartition, tPartition, parSet.getParSet(),
						parSet.getwTime(), costMaxNew, algSSA.canPars_list,0);

				if (feasiblePath != null && feasiblePath != "") {
					ParSet pSet = new ParSet(parSet);
					double timeCost = Double.parseDouble(feasiblePath.split(" ")[0]);
					String path = feasiblePath.split(" ")[1];
					pSet.setTimeCost(timeCost);
					pSet.setParSetPath(path);
					resultNew.add(pSet);
				}
			}
//			System.out.println("resultNew size: " + resultNew.size());
		}

	}

	private void addNewResult(AlgSSA algSSA, Point sPoint, Point tPoint, Partition sPartition, Partition tPartition,
							  double costMax, int k, int QWSize, ArrayList<ArrayList<String>> canPars_list,
			PriorityQueue<ParSet> resultNew, String caseType) {

		ArrayList<ArrayList<ArrayList<String>>> canPars_new = new ArrayList<>(QWSize);
		for (int i = 0; i < QWSize; i++) {
			canPars_new.add(new ArrayList<>());
		}
		double wTimeMax = algSSA.calWTimeMax(sPoint, tPoint, sPartition, tPartition, costMax);
		algSSA.curKCost = Constant.large;
		int startNum = 0;

//		System.out.println("startNum " + startNum);
//		System.out.println("canPars_list size: " + canPars_list.size());
//		System.out.println("resultNew size: " + resultNew.size());
		for (int i = startNum; i < canPars_list.size(); i++) {
			ParSet parSet = new ParSet(QWSize);
			ArrayList<String> curPar = canPars_list.get(i);
			int pos = Integer.parseInt(curPar.get(2));
			parSet.setPar(curPar, pos);
			algSSA.findKeyParsSets(canPars_new, parSet, 0, //
					wTimeMax, costMax, sPoint, tPoint, sPartition, tPartition, resultNew, k, pos, canPars_list,0);
			canPars_new.get(pos).add(curPar);
//			System.out.println("remain: " + (i + 1));
//			if (resultNew.size() >= k) break;

		}

	}

	// we model the situation that the parameter is updated after query execution
	// here QW is updated
	// we assured that QWNew = QW\w
	public ArrayList<String> resultUpdateCaseF(AlgSSA algSSA, Point sPoint, Point tPoint, ArrayList<String> QW, //
											   double costMax, double scorMin, int k, ArrayList<String> QWNew)
			throws IOException {
		PriorityQueue<ParSet> resultPrev = algSSA.getResult();
		ArrayList<ArrayList<String>> canPars_list = algSSA.getCanPars_list();
		Partition sPartition = CommonFunction.locPartition(sPoint);
		Partition tPartition = CommonFunction.locPartition(tPoint);


		if (QWNew.size() != QW.size() -1)
			return null;

		int pos = -1;
		for (int i = 0; i < QWNew.size(); i++) {
			String word = QWNew.get(i);
			for (int j = 0; j < QW.size(); j++) {
				String wordNew = QW.get(j);
				if (!wordNew.equals(word)) {
					pos = j;
				}
			}
		}
//		System.out.println("pos: " + pos);

		// update canPars_list
		ArrayList<ArrayList<String>> canPars_listNew = new ArrayList<>();
		for (int i = 0; i < canPars_list.size(); i++) {
			ArrayList<String> curPar = canPars_list.get(i);
			int position = Integer.parseInt(curPar.get(2));
			if (position == pos) continue;
			if (position > pos) {
				curPar.set(2, (position - 1) + "");
			}
			canPars_listNew.add(curPar);
		}

		PriorityQueue<ParSet> resultNew = new PriorityQueue<ParSet>(Comparator.reverseOrder());

		/* Phase 1. check resultPrev */
		checkResultValidityCaseF(algSSA, resultPrev, pos, QWNew.size(), resultNew);

		if (resultNew.size() < k + resultNew.size()) {

			/* Phase 2. add new result if needed */
			addNewResult(algSSA, sPoint, tPoint, sPartition, tPartition, costMax, k, QWNew.size(), canPars_listNew, resultNew, "caseF");

		}
		// convert each result to string and return
		ArrayList<String> r = new ArrayList<>();
		while (resultNew.size() > 0) {
			ParSet parSet = resultNew.poll();
			if (resultNew.size() <= k) {
				r.add(parSet.toString() + " " + parSet.getTimeCost() + " " + parSet.getParSetPath());
			}
		}
		Collections.reverse(r);

		return r;
	}

	
	private void checkResultValidityCaseF(AlgSSA algSSA, PriorityQueue<ParSet> resultPrev, int pos, int QWNewSize,
										  PriorityQueue<ParSet> resultNew) {

		while (resultPrev.size() > 0) {
			ParSet parSet = new ParSet(QWNewSize);
			ParSet parSetPrev = resultPrev.poll();
			short[] parSetPrevArr = parSetPrev.getParSet();
			double[] relPrev = parSetPrev.getRel();
			int num = 0;
			for (int i = 0; i < parSetPrevArr.length; i++) {
				if (i == pos) continue;
				short par = parSetPrevArr[i];
				double rel = relPrev[i];
				ArrayList<String> parArr = new ArrayList<>();
				parArr.add(par + "");
				parArr.add(rel + "");
				parArr.add(num + "");
				parSet.setPar(parArr, num);
				num++;
			}
			parSet.setTotalScore(parSet.calcTotalCost(QWNewSize));
			parSet.setParSetPath(parSetPrev.getParSetPath());
			parSet.setTimeCost(parSetPrev.getTimeCost());

			short[] parSetArr = parSet.getParSet();
			String parSetString = "";
			for (int i = 0; i < parSetArr.length; i++) {
				parSetString += parSetArr[i];
			}
			if (algSSA.inResultParSet.contains(parSetString)) continue;
			algSSA.inResultParSet.add(parSetString);
			resultNew.add(parSet);
		}

//		System.out.println("resultNew size: " + resultNew.size());

	}
	
	
	// we model the situation that the parameter is updated after query execution
		// here sPoint OR tPoint is updated
		public ArrayList<String> resultUpdateCaseDE(AlgSSA algSSA, Point sPointNew, Point tPointNew, ArrayList<String> QW, //
												   double costMax, double scorMin, int k)
				throws IOException {
			PriorityQueue<ParSet> resultPrev = algSSA.getResult();
//			System.out.println("size: " + resultPrev.size());
//			ArrayList<ArrayList<String>> canPars_list = algSSA.getCanPars_list();
			Partition sPartitionNew = CommonFunction.locPartition(sPointNew);
			Partition tPartitionNew = CommonFunction.locPartition(tPointNew);

			
			PriorityQueue<ParSet> resultNew = new PriorityQueue<ParSet>(Comparator.reverseOrder());

			/* Phase 1. check resultPrev */
			checkResultValidityCaseDE(algSSA, sPointNew, tPointNew, sPartitionNew, tPartitionNew, costMax, resultPrev, resultNew);

			if (resultNew.size() < k) 
			{

				/* Phase 2. add new result if needed */
				//-/-/-
				//re-calculate the candidate part
				//since it might be pruned
				CandidatePartitions candidatePartitions = new CandidatePartitions(QW, DataGenConstant.threshold);

				ArrayList<ArrayList<String>> canPars_list = candidatePartitions.findAllCandPars3();
				// pruning 1
				for (int i = 0; i < canPars_list.size(); i++) {
					ArrayList<String> par = canPars_list.get(i);
					int parId = Integer.parseInt(par.get(0));
					double lowerBound = CommonFunction.calLowerBound(sPointNew, tPointNew, parId);
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
						double costA = algSSA.alpha * (double) parA.getStaticCost() / DataGenConstant.SC_MAX
								+ (1.0 - algSSA.alpha) * (1 - Double.parseDouble(a.get(1)));
						double costB = algSSA.alpha * (double) parB.getStaticCost() / DataGenConstant.SC_MAX
								+ (1.0 - algSSA.alpha) * (1 - Double.parseDouble(b.get(1)));

						if (costA > costB)
							return 1;
						else if (costA == costB)
							return 0;
						else
							return -1;
					}

				});

				double wTimeMax = costMax;
				if (tPointNew != null)
					wTimeMax = algSSA.calWTimeMax(sPointNew, tPointNew, sPartitionNew, tPartitionNew, costMax);
				///-/--
				
				addNewResult(algSSA, sPointNew, tPointNew, sPartitionNew, tPartitionNew, costMax, k, QW.size(), canPars_list, resultNew, "caseDE");
			}
			// convert each result to string and return
			ArrayList<String> r = new ArrayList<>();
			while (resultNew.size() > 0) {
				ParSet parSet = resultNew.poll();
				r.add(parSet.toString() + " " + parSet.getTimeCost() + " " + parSet.getParSetPath());
//				System.out.println(parSet.toString());
			}
			Collections.reverse(r);

			return r;
		}
		

		private void checkResultValidityCaseDE(AlgSSA algSSA, Point sPointNew, Point tPointNew, Partition sPartitionNew,
											  Partition tPartitionNew, double costMax,  PriorityQueue<ParSet> resultPrev,
				PriorityQueue<ParSet> resultNew) {

			while (resultPrev.size() > 0) {
				ParSet parSet = resultPrev.poll();
				parSet.setParSetPath(null);
//				double timecost = parSet.getTimeCost();
////				System.out.println("timecost: " + timecost);
//				if (timecost < costMax) {
//					resultNew.add(parSet);
//					short[] parSetArr = parSet.getParSet();
//					String parSetString = "";
//					for (int i = 0; i < parSetArr.length; i++) {
//						parSetString += parSetArr[i];
//					}
//					algSSA.inResultParSet.add(parSetString);
////					System.out.println("parsetString: " + parSetString);
//				}
//				else 
				{
					String feasiblePath = algSSA.findFeasiblePath(sPointNew, tPointNew, sPartitionNew, tPartitionNew, parSet.getParSet(),
							parSet.getwTime(), costMax, algSSA.canPars_list, 0);

//					System.out.println("feasiblePath: " + feasiblePath);
					if (feasiblePath != null && feasiblePath != "") {
						ParSet pSet = new ParSet(parSet);
						double timeCost = Double.parseDouble(feasiblePath.split(" ")[0]);
						String path = feasiblePath.split(" ")[1];
						pSet.setTimeCost(timeCost);
						pSet.setParSetPath(path);
						resultNew.add(pSet);
						//---
						short[] parSetArr = parSet.getParSet();
						String parSetString = "";
						for (int i = 0; i < parSetArr.length; i++) {
							parSetString += parSetArr[i];
						}
						algSSA.inResultParSet.add(parSetString);
						//---
					}
				}
//				System.out.println("resultNew size: " + resultNew.size());
			}

		}


	public static void main(String[] args) throws Exception {
//		testRun();
		testRun_HSM();

	}

	public static void testRun() throws Exception {
		Init.init();

		AlgSSA algo = new AlgSSA();

		ArrayList<String> result = new ArrayList<>();
		ArrayList<String> resultNew = new ArrayList<>();

		// set up and run original query
		Point sPoint = new Point(862.0,632.0,4);
		Point tPoint = new Point(658.0,361.0,3);
		ArrayList<String> QW = new ArrayList<>(Arrays.asList("5578", "212", "4179", "2880"));
		double costMax = 3500;
		double scorMin = 0.5;
		int k = 7;
		result = algo.tikrq(sPoint, tPoint, QW, costMax, scorMin, k);
		// ----

		AlgExtension algoExt = new AlgExtension();
		double costMaxNew = 3325;
		ArrayList<String> QWNew = new ArrayList<>(Arrays.asList("5578", "212", "4179"));
		Point sPointNew = new Point(762.0,632.0,4);
		Point tPointNew = new Point(758.0,361.0,3);

		
		Runtime runtime1 = Runtime.getRuntime();
		runtime1.gc();
		long startMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long startTime1 = System.currentTimeMillis();

//		System.out.println("size.. " + algo.getResult().size());
//		System.out.println("remainFlag " + algo.ckpRemainFlag);

		// parameter changed and run result update
//		resultNew = algoExt.resultUpdateCaseC(algo, sPoint, tPoint, QW, costMax, scorMin, k, costMaxNew); // caseC
		resultNew = algoExt.resultUpdateCaseDE(algo, sPointNew, tPoint, QW, costMax, scorMin, k); // caseD
//		resultNew = algoExt.resultUpdateCaseF(algo, sPoint, tPoint, QW, costMax, scorMin, k, QWNew); // caseF

		long endTime1 = System.currentTimeMillis();
		long time1 = endTime1 - startTime1;
		long endMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long mem1 = (endMem1 - startMem1) / 1024 / 1024;
		// ----

		System.out.println("result--------------");
		for (String path : result) {
			System.out.println(path);
		}
		System.out.println("resultNew--------------");
		for (String path : resultNew) {
			System.out.println(path);
		}

		System.out.println("timeNew(ms): " + time1 + "; mem(mb): " + mem1);
		// ------------------------------
		// for comparsion and correctness check, we can run a query from stratch
		// but, the result could be different as ParSet ordering are different
		ArrayList<String> resultCompare = new ArrayList<>();
		AlgSSA algo1 = new AlgSSA();

		startTime1 = System.currentTimeMillis();
//		resultCompare = algo1.tikrq(sPoint, tPoint, QW, costMaxNew, scorMin, k); // caseC
		resultCompare = algo1.tikrq(sPointNew, tPoint, QW, costMaxNew, scorMin, k); // caseD
//		resultCompare = algo1.tikrq(sPoint, tPoint, QWNew, costMax, scorMin, k); // caseF
		
		 endTime1 = System.currentTimeMillis();
		 time1 = endTime1 - startTime1;

		System.out.println("resultCompare--------------");
		for (String path : resultCompare) {
			System.out.println(path);
		}
		System.out.println("timeCompare(ms): " + time1 + "; mem(mb): " + mem1);
		
		// ------------------------------


	}

	public static void testRun_HSM() throws Exception {
		Init.init_HSM();

		AlgSSA algo = new AlgSSA();

		ArrayList<String> result = new ArrayList<>();
		ArrayList<String> resultNew = new ArrayList<>();

		// set up and run original query
		Point sPoint = new Point(17.2, 100.0, 1);
		Point tPoint = new Point(28.0, 88.0, 4);
//		ArrayList<String> QW = new ArrayList<>(Arrays.asList("-779", "-1113", "-1109", "-810"));
		ArrayList<String> QW = new ArrayList<>(Arrays.asList("-779", "-1113"));

		double costMax = 300;
		double scorMin = 0.5;
		int k = 7;
		result = algo.tikrq(sPoint, tPoint, QW, costMax, scorMin, k);

		// ----

		System.out.println("-------------------");
		
		AlgExtension algoExt = new AlgExtension();
		double costMaxNew = 4500;
//		ArrayList<String> QWNew = new ArrayList<>(Arrays.asList("-779", "-1113", "-810"));
		ArrayList<String> QWNew = new ArrayList<>(Arrays.asList("-779"));
		Point sPointNew = new Point(27.2, 50.0, 1);
		Point tPointNew = new Point(28.0, 58.0, 3);
		
		Runtime runtime1 = Runtime.getRuntime();
		runtime1.gc();
		long startMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long startTime1 = System.currentTimeMillis();

//		System.out.println("size.. " + algo.getResult().size());
//		System.out.println("remainFlag " + algo.ckpRemainFlag);

		// parameter changed and run result update
//		resultNew = algoExt.resultUpdateCaseC(algo, sPoint, tPoint, QW, costMax, scorMin, k, costMaxNew); // caseC
		resultNew = algoExt.resultUpdateCaseDE(algo, sPointNew, tPoint, QW, costMax, scorMin, k); // caseD
//		resultNew = algoExt.resultUpdateCaseDE(algo, sPoint, tPointNew, QW, costMax, scorMin, k); // caseE
//		resultNew = algoExt.resultUpdateCaseF(algo, sPoint, tPoint, QW, costMax, scorMin, k, QWNew); // caseF
//		result = algo.tikrq(sPoint, tPoint, QW, costMax, scorMin, k,0);
//		resultNew = algo.tikrq(sPointNew, tPoint, QW, costMax, scorMin, k); // caseD


		long endTime1 = System.currentTimeMillis();
		long endMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long mem1 = (endMem1 - startMem1) / 1024 / 1024;
		long time1 = endTime1 - startTime1;
		// ----

		System.out.println("result--------------");
		for (String path : result) {
			System.out.println(path);
		}
		System.out.println("resultNew--------------");
		for (String path : resultNew) {
			System.out.println(path);
		}
		System.out.println("time(ms): " + time1 + "; mem(mb): " + mem1);

		// ------------------------------
		// for comparsion and correctness check, we can run a query from stratch
		// but, the result could be different as ParSet ordering are different
		ArrayList<String> resultCompare = new ArrayList<>();
		AlgSSA algo1 = new AlgSSA();
		AlgBaseline base = new AlgBaseline();
		
		startTime1 = System.currentTimeMillis();

		 
//		resultCompare = algo1.tikrq(sPoint, tPoint, QW, costMax, scorMin, k,1); // caseC
		resultCompare = algo1.tikrq(sPointNew, tPoint, QW, costMax, scorMin, k); // caseD
//		resultCompare = algo1.tikrq(sPoint, tPointNew, QW, costMax, scorMin, k); // caseE

//		resultCompare = algo1.tikrq(sPoint, tPoint, QWNew, costMax, scorMin, k); // caseF
//		resultCompare = base.baseline(sPoint, tPointNew, QWNew, costMax, scorMin, k); // caseF
		
		endTime1 = System.currentTimeMillis();
		time1 = endTime1 - startTime1;

		System.out.println("resultCompare--------------");
		for (String path : resultCompare) {
			System.out.println(path);
		}

		// ------------------------------

		System.out.println("time(ms): " + time1 + "; mem(mb): " + mem1);

	}
}
