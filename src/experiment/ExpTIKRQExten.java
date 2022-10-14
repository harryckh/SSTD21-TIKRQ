package experiment;

import java.io.FileOutputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import algorithm.AlgAdaptKoE;
import algorithm.AlgBaseline;
import algorithm.AlgExtension;
import algorithm.AlgSSA;
import algorithm.Init;
import indoor_entitity.Point;

public class ExpTIKRQExten {

	/**
	 * 
	 * @param fileNamePrefix = the prefix of result.txt and stat.txt
	 * @param qListList      = the sets of queries to be executed, each
	 *                       ArrayList<Query> corresponds to a set and is averaged
	 *                       Each set corresponds to a row in stat file
	 * @param alg_opt        = 1:SSA, 2:KoE, 3:SSAnoP
	 * @param numOfRuns      = number of runs of each query to be averaged
	 * @throws IOException
	 */
	public static void exp(String fileNamePrefix, ArrayList<ArrayList<Query>> qListList, //
			int alg_opt, int numOfRuns) throws IOException {

		// ---
//		Init.init();
		AlgSSA algo_cikrq = new AlgSSA();
		AlgExtension algo_exten = new AlgExtension();
		AlgSSA algo_stratch = new AlgSSA();
		AlgBaseline algo_baseline = new AlgBaseline();
		AlgAdaptKoE algo_Adapt_KoE = new AlgAdaptKoE();
		// ---

		String outStatFile = System.getProperty("user.dir") + "/" + fileNamePrefix + "stat.txt";
		String outResultFile = System.getProperty("user.dir") + "/" + fileNamePrefix + "result.txt";

		FileOutputStream outputStat = new FileOutputStream(outStatFile, true);
		FileOutputStream outputResult = new FileOutputStream(outResultFile, true);

		System.out.println("Running alg_opt: " + alg_opt);
		System.out.println("Number of runs: " + numOfRuns);
		System.out.println("Output stat file: " + fileNamePrefix + "stat.txt");
		System.out.println("Output result file: " + fileNamePrefix + "result.txt");

		String statResult = "";
		statResult += "row" + "\t" + "time(ns)" + "\t" + "memory" + "\n";
		outputStat.write(statResult.getBytes());
		outputStat.flush();

		int row = 1;
		for (ArrayList<Query> qList : qListList) {

			System.out.println("running row (file) " + row);

			long resultTimeSum2 = 0;
			long resultMenSum2 = 0;
			long resultTimeAve2 = 0;
			long resultMenAve2 = 0;

			int subRow = 1;
			// for each query
			for (Query query : qList) {

				System.out.print("query instance=" + subRow + ":");
				long resultTimeSum1 = 0;
				long resultMenSum1 = 0;
				long resultTimeAve1 = 0;
				long resultMenAve1 = 0;
				ArrayList<String> QW = query.q_word;

				// remove the final one
				ArrayList<String> QWnew = new ArrayList<String>();
				for (int aa = 0; aa < QW.size() - 1; aa++) {
					QWnew.add(QW.get(aa));
				}

				Point ps = query.ps;
				Point pt = query.pt;

				ArrayList<String> tmp = new ArrayList<>();

				// run 10 times
//				int times = 10;
				for (int h = 0; h < numOfRuns; h++) {

					if (alg_opt%10==4) {
						algo_cikrq.tikrq(ps, pt, QW, query.timeMax, query.relThreshold, query.k);
					}

					System.out.print(h + " ");
					Runtime runtime = Runtime.getRuntime();
					runtime.gc();
					long startMem = runtime.totalMemory() - runtime.freeMemory();
					long start = System.nanoTime();

					/** actual running **/
					if (alg_opt == 1) {
						tmp = algo_cikrq.tikrq(ps, pt, QW, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 2) {
						tmp = algo_Adapt_KoE.KoE(ps, pt, QW, query.timeMaxNew, query.k);
					} else if (alg_opt == 3) {
						tmp = algo_baseline.baseline(ps, pt, QW, query.timeMaxNew, query.relThreshold, query.k);
					} else if (alg_opt == 4) {
						tmp = algo_exten.resultUpdateCaseC(algo_cikrq, ps, pt, QW, query.timeMax, query.relThreshold,
								query.k, query.timeMaxNew);
					} else if (alg_opt == 5) {
						tmp = algo_stratch.tikrq(ps, pt, QW, query.timeMaxNew, query.relThreshold, query.k);
						// ------------------------------------------------------------------------------------
					} else if (alg_opt == 11) {
						tmp = algo_cikrq.tikrq(ps, pt, QW, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 12) {
						tmp = algo_Adapt_KoE.KoE(ps, pt, QWnew, query.timeMax, query.k);
					} else if (alg_opt == 13) {
						tmp = algo_baseline.baseline(ps, pt, QWnew, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 14) {
						tmp = algo_exten.resultUpdateCaseF(algo_cikrq, ps, pt, QW, query.timeMax, query.relThreshold,
								query.k, QWnew);
					} else if (alg_opt == 15) {
						tmp = algo_stratch.tikrq(ps, pt, QWnew, query.timeMax, query.relThreshold, query.k);
						// ------------------------------------------------------------------------------------

					} else if (alg_opt == 21) {
						tmp = algo_cikrq.tikrq(ps, pt, QW, query.timeMax, query.relThreshold, query.k, 1);
						} else if (alg_opt == 22) {
							tmp = algo_Adapt_KoE.KoE(ps, pt, QW, query.timeMax, query.k,1);
						} else if (alg_opt == 23) {
							tmp = algo_baseline.baseline(ps, pt, QW, query.timeMax, query.relThreshold, query.k,1);
						// ------------------------------------------------------------------------------------

					} else if (alg_opt == 31) {
						tmp = algo_cikrq.tikrq(ps, null, QW, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 32) {
						tmp = algo_Adapt_KoE.KoE(ps, null, QW, query.timeMax, query.k);
					} else if (alg_opt == 33) {
						tmp = algo_baseline.baseline(ps, null, QW, query.timeMax, query.relThreshold, query.k);
						// ------------------------------------------------------------------------------------

					}else if (alg_opt == 41) {
						tmp = algo_cikrq.tikrq(ps, pt, QW, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 42) {
						tmp = algo_Adapt_KoE.KoE(ps, pt, QW, query.timeMax, query.k);
					} else if (alg_opt == 43) {
						tmp = algo_baseline.baseline(ps, pt, QW, query.timeMax, query.relThreshold, query.k);
						// ------------------------------------------------------------------------------------

					}	
					else if (alg_opt == 51) {
						tmp = algo_cikrq.tikrq(ps, pt, QW, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 52) {
						tmp = algo_Adapt_KoE.KoE(query.psNew, pt, QW, query.timeMax, query.k);
					} else if (alg_opt == 53) {
						tmp = algo_baseline.baseline(query.psNew, pt, QW, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 54) {
						tmp = algo_exten.resultUpdateCaseDE(algo_cikrq, query.psNew, pt, QW, query.timeMax, query.relThreshold,
								query.k);
					} else if (alg_opt == 55) {
						tmp = algo_stratch.tikrq(query.psNew, pt, QW, query.timeMax, query.relThreshold, query.k);
						// ------------------------------------------------------------------------------------
					} else if (alg_opt == 61) {
						tmp = algo_cikrq.tikrq(ps,  query.ptNew, QW, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 62) {
						tmp = algo_Adapt_KoE.KoE(ps,  query.ptNew, QW, query.timeMax, query.k);
					} else if (alg_opt == 63) {
						tmp = algo_baseline.baseline(ps,  query.ptNew, QW, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 64) {
						tmp = algo_exten.resultUpdateCaseDE(algo_cikrq, ps, query.ptNew, QW, query.timeMax, query.relThreshold,
								query.k);
					} else if (alg_opt == 65) {
						tmp = algo_stratch.tikrq(ps, query.ptNew, QW, query.timeMax, query.relThreshold, query.k);
						// ------------------------------------------------------------------------------------
					}

					long end = System.nanoTime();
					long endMem = runtime.totalMemory() - runtime.freeMemory();

					long time = end - start;
					long memory = (endMem - startMem) / 1024;

					resultTimeSum1 += time;
					resultMenSum1 += memory;

					if (h == 0) {
						String tmpString = "";
						tmpString += "Query #" + row + "." + (subRow++) + " " + time + " " + memory + "\n";
						tmpString += query.toString() + "\n";
						for (String s : tmp)
							tmpString += s + "\n";
						tmpString += "\n";
						outputResult.write(tmpString.getBytes());
						outputResult.flush();

					}
				}
				resultTimeAve1 = resultTimeSum1 / numOfRuns;
				resultMenAve1 = resultMenSum1 / numOfRuns;

				System.out.print(" " + resultTimeAve1 + " " + resultMenAve1);
				// ---------------------
				resultTimeSum2 += resultTimeAve1;
				resultMenSum2 += resultMenAve1;
				System.out.println();
			} /// end for this query

			resultTimeAve2 = resultTimeSum2 / qList.size();
			resultMenAve2 = resultMenSum2 / qList.size();

			statResult = (row++) + "\t" + resultTimeAve2 + "\t" + resultMenAve2 + "\n";
			System.out.println(statResult);
			outputStat.write(statResult.getBytes());
			outputStat.flush();

		} /// end this query file (i.e., a row in stat file)

		outputStat.close();
		outputResult.close();
	}

	public static ArrayList<Query> readQuery(String fileName) throws Exception {
		ArrayList<Query> queryList = new ArrayList<>();
		Path path1 = Paths.get(System.getProperty("user.dir") + fileName);
		Scanner scanner1 = new Scanner(path1);

		while (scanner1.hasNextLine()) {
			String line = scanner1.nextLine();
//			System.out.println(line);
			String[] tempArr = line.split(" ");
			Query query = new Query(tempArr);
			queryList.add(query);

		}
		scanner1.close();
		return queryList;
	}

	public static void main(String[] arg) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long start = System.nanoTime();

		Init.init();
//		Init.init_HSM();
		
		ArrayList<String> fileNameList = new ArrayList<>();
		

		
//		fileNameList.add("drq1.txt");
//		fileNameList.add("drq2.txt");
//		fileNameList.add("drq3.txt");
//		fileNameList.add("drq4.txt");
//		fileNameList.add("drq5.txt");
		
		fileNameList.add("drk1.txt");
		fileNameList.add("drk3.txt");
		fileNameList.add("drk5.txt");
		fileNameList.add("drk7.txt");
		fileNameList.add("drk9.txt");
		fileNameList.add("drk11.txt");
		
		fileNameList.add("dr3000.txt");
		fileNameList.add("dr3500.txt");
		fileNameList.add("dr4000.txt");
		fileNameList.add("dr4500.txt");
		fileNameList.add("dr5000.txt");
//		
//		fileNameList.add("dq1.txt");
//		fileNameList.add("dq2.txt");
//		fileNameList.add("dq3.txt");
//		fileNameList.add("dq4.txt");
//		fileNameList.add("dq5.txt");
		
//		fileNameList.add("d3000.txt");
//		fileNameList.add("d3500.txt");
//		fileNameList.add("d4000.txt");
//		fileNameList.add("d4500.txt");
//		fileNameList.add("d5000.txt");
		
//		fileNameList.add("dk1.txt");
//		fileNameList.add("dk3.txt");
//		fileNameList.add("dk5.txt");
//		fileNameList.add("dk7.txt");
//		fileNameList.add("dk9.txt");
//		fileNameList.add("dk11.txt");
		
		ArrayList<ArrayList<Query>> qListList = new ArrayList<>();
		for (String fileName : fileNameList)
			qListList.add(readQuery("/query/" + fileName));
//
////		exp("sc4", qListList, 4, 1);
////		exp("sc5", qListList, 5, 1);
//
//		exp("rk43", qListList, 3, 10);
		// ==============================================

		ArrayList<String> fileNameList2 = new ArrayList<>();

//		fileNameList2.add("rk1.txt");
//		fileNameList2.add("rk3.txt");
//		fileNameList2.add("rk5.txt");
//		fileNameList2.add("rk7.txt");
//		fileNameList2.add("rk9.txt");
//		fileNameList2.add("rk11.txt");
//
//		fileNameList2.add("erq1.txt");
//		fileNameList2.add("erq2.txt");
//		fileNameList2.add("erq3.txt");
//		fileNameList2.add("erq4.txt");
//		fileNameList2.add("erq5.txt");

//		fileNameList2.add("erk1.txt");
//		fileNameList2.add("erk3.txt");
//		fileNameList2.add("erk5.txt");
//		fileNameList2.add("erk7.txt");
//		fileNameList2.add("erk9.txt");
//		fileNameList2.add("erk11.txt");
//		
//		fileNameList2.add("er3000.txt");
//		fileNameList2.add("er3500.txt");
//		fileNameList2.add("er4000.txt");
//		fileNameList2.add("er4500.txt");
//		fileNameList2.add("er5000.txt");
		
		
////
//		fileNameList2.add("eq1.txt");
//		fileNameList2.add("eq2.txt");
//		fileNameList2.add("eq3.txt");
//		fileNameList2.add("eq4.txt");
		fileNameList2.add("eq5.txt");
//
		
//		fileNameList2.add("e3000.txt");
//		fileNameList2.add("e3500.txt");
//		fileNameList2.add("e4000.txt");
//		fileNameList2.add("e4500.txt");
//		fileNameList2.add("e5000.txt");
		
//		fileNameList2.add("ek1.txt");
//		fileNameList2.add("ek3.txt");
//		fileNameList2.add("ek5.txt");
//		fileNameList2.add("ek7.txt");
//		fileNameList2.add("ek9.txt");
//		fileNameList2.add("ek11.txt");
		

		ArrayList<ArrayList<Query>> qListList2 = new ArrayList<>();
		for (String fileName : fileNameList2)
			qListList2.add(readQuery("/query/" + fileName));

//		exp("rq41", qListList2, 41, 10);
//		exp("rq42", qListList2, 42, 10);
		
//		exp("drkt54", qListList, 54, 5);
//		exp("drkt55", qListList, 55, 5);

		exp("eq65", qListList2, 65, 50);
		exp("eq64", qListList2, 64, 50);

//		exp("rdm54", qListList2, 54, 5);
//		exp("rdm55", qListList2, 55, 5);
//		
//		exp("rem64", qListList2, 64, 5);
//		exp("rem65", qListList2, 65, 5);
		
//		exp("rk21", qListList2, 21, 1);
//		exp("rq31", qListList2, 31, 1);
//		exp("rq22", qListList2, 22, 1);
//		exp("rq32", qListList2, 32, 1);
//		exp("rq23", qListList2, 23, 1);
//		exp("rq33", qListList2, 33, 1);
		
//		exp("rk21", qListList2, 21, 10);
//		exp("rk31", qListList2, 31, 10);
//		exp("rk22", qListList2, 22, 10);
//		exp("rk32", qListList2, 32, 10);
//		exp("rk23", qListList2, 23, 10);
//		exp("rk33", qListList2, 33, 10);
//		
//		exp("rk41", qListList2, 1, 10);
//		exp("rk42", qListList2, 2, 10);
//		exp("rk43", qListList2, 3, 10);
		
		
		//2x = ordered
		//3x = no end point
		
		long end = System.nanoTime();

		long time = end - start;
		System.out.println("Done! Total time used:" + time);
	}

}
