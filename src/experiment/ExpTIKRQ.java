package experiment;

import java.io.FileOutputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import algorithm.AlgAdaptKoE;
import algorithm.AlgBaseline;
import algorithm.AlgSSA;
import algorithm.Init;
import indoor_entitity.Point;

public class ExpTIKRQ {

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
				Point ps = query.ps;
				Point pt = query.pt;

				ArrayList<String> tmp = new ArrayList<>();

				// run 10 times
//				int times = 10;
				for (int h = 0; h < numOfRuns; h++) {
					System.out.print(h + " ");
					Runtime runtime = Runtime.getRuntime();
					runtime.gc();
					long startMem = runtime.totalMemory() - runtime.freeMemory();
					long start = System.nanoTime();

					/** actual running **/
					if (alg_opt == 1) {
						tmp = algo_cikrq.tikrq(ps, pt, QW, query.timeMax, query.relThreshold, query.k);
					} else if (alg_opt == 2) {
						tmp = algo_Adapt_KoE.KoE(ps, pt, QW, query.timeMax, query.k);
					} else if (alg_opt == 3) {
						tmp = algo_baseline.baseline(ps, pt, QW, query.timeMax, query.relThreshold, query.k);
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

//		Init.init();
		Init.init_HSM();
		

		ArrayList<String> fileNameList = new ArrayList<>();

//		fileNameList.add("k1.txt");
//		fileNameList.add("k3.txt");
//		fileNameList.add("k5.txt");
//		fileNameList.add("k7.txt");
//		fileNameList.add("k9.txt");
//		fileNameList.add("k11.txt");
		fileNameList.add("rq5.txt");

		ArrayList<ArrayList<Query>> qListList = new ArrayList<>();
		for (String fileName : fileNameList)
			qListList.add(readQuery("/query/" + fileName));

//		exp("q41", qListList, 1, 10);
//		exp("q42", qListList, 2, 1);
//		exp("q43", qListList, 3, 1);
		
		exp("q5test", qListList, 1, 1);

		long end = System.nanoTime();

		long time = end - start;
		System.out.println("Done! Total time used:" + time);
	}

}
