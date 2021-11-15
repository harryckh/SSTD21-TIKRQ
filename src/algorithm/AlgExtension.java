package algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;

import indoor_entitity.Point;

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
	public ArrayList<String> resultUpdate(Point sPoint, Point tPoint, ArrayList<String> QW, //
			double costMax, double scorMin, int k, double costMaxNew, //
			PriorityQueue<ParSet> resultPrev, ArrayList<ArrayList<String>> canPars_list)
			throws IOException {

		if (costMaxNew >= costMax)
			return null;

		PriorityQueue<ParSet> resultNew = new PriorityQueue<ParSet>();

		/* Phase 1. check resultPrev */
		checkResultValidity(resultPrev, costMaxNew, resultNew);

		if (resultNew.size() <= k) {

			/* Phase 2. add new result if needed */
			addNewResult(canPars_list, resultNew);

		}
		// convert each result to string and return
		ArrayList<String> r = new ArrayList<>();
		while (resultNew.size() > 0) {
			ParSet parSet = resultNew.poll();
			r.add(parSet.toString() + " " + parSet.path);
		}
		Collections.reverse(r);

		return r;
	}

	private void checkResultValidity(PriorityQueue<ParSet> resultPrev, double costMaxNew,
			PriorityQueue<ParSet> resultNew) {
		// TODO Harry: implementation here, more parameters might needed for this
		// function

	}

	private void addNewResult(ArrayList<ArrayList<String>> canPars_list,
			PriorityQueue<ParSet> resultNew) {
		// TODO Harry: implementation here, more parameters might needed for this
		// function

	}

	public static void main(String[] args) throws Exception {
		testRun();

	}

	public static void testRun() throws Exception {
		Init.init();
		AlgSSA algo = new AlgSSA();

		ArrayList<String> result = new ArrayList<>();
		ArrayList<String> resultNew = new ArrayList<>();

		// set up and run original query
		Point sPoint = new Point(170.0, 621.0, 3);
		Point tPoint = new Point(1074.0, 889.0, 4);
		ArrayList<String> QW = new ArrayList<>(Arrays.asList("-1053", "-131", "-574", "-811"));
		double costMax = 5000;
		double scorMin = 0.5;
		int k = 7;
		result = algo.tikrq(sPoint, tPoint, QW, costMax, scorMin, k);
		// ----

		AlgExtension algoExt = new AlgExtension();
		double costMaxNew = 4500;

		Runtime runtime1 = Runtime.getRuntime();
		runtime1.gc();
		long startMem1 = runtime1.totalMemory() - runtime1.freeMemory();
		long startTime1 = System.currentTimeMillis();

		// parameter changed and run result update
		resultNew = algoExt.resultUpdate(sPoint, tPoint, QW, costMax, scorMin, k, costMaxNew, //
				algo.getResult(), algo.getCanPars_list());

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

		// ------------------------------
		// for comparsion and correctness check, we can run a query from stratch
		// but, the result could be different as ParSet ordering are different
		ArrayList<String> resultCompare = new ArrayList<>();
		resultCompare = algo.tikrq(sPoint, tPoint, QW, costMaxNew, scorMin, k);
		System.out.println("resultCompare--------------");
		for (String path : resultCompare) {
			System.out.println(path);
		}

		// ------------------------------

		System.out.println("time(ms): " + time1 + "; mem(mb): " + mem1);

	}
}
