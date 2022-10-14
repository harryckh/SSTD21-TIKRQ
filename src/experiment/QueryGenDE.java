package experiment;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import algorithm.AlgSSA;
import algorithm.CommonFunction;
import algorithm.Init;
import indoor_entitity.Partition;
import indoor_entitity.Point;
import utilities.DataGenConstant;
import wordProcess.WordPartitionRelationship;

/**
 * 
 * @author harry
 *
 *         This generate new ps and pt based on existing query to be a valid
 *         query, feasible routes should be found
 */
public class QueryGenDE {
	private static Random random = new Random();

	/**
	 * 
	 * @param min
	 * @param max
	 * @return a random number in range [min,max)
	 */
	public static int rand_i(int min, int max) {
		return random.nextInt(max - min) + min;
	}

	public static Point genPoint() {
		Point ps;
		Partition sPartition;
		do {
			int psx = rand_i(0, (int) (DataGenConstant.floorRangeX + 1));
			int psy = rand_i(0, (int) (DataGenConstant.floorRangeY + 1));
			int psf = rand_i(0, DataGenConstant.nFloor);

			ps = new Point(psx, psy, psf);

			sPartition = CommonFunction.locPartition(ps);
		} while (sPartition == null);

		return ps;

	}

	/**
	 * 
	 * @return a list of query with the (same) parameter setting
	 * 
	 *         type = 0 = newps type = 1 = newpt
	 */
	public static ArrayList<Query> gen_query_set_from_existing(String fileName, int type,
			ArrayList<Query> qList) throws Exception {

		AlgSSA algo_cikrq = new AlgSSA(); // testing if the query is valid

		ArrayList<Query> q_set = new ArrayList<>();

		String queryResult = "";

		int j = 1;
		for (Query query : qList) {
			System.out.println("generating query " + (j));
			int success = 0;

			while(true) {
				 // for save/load purpose
				// each case change one of them only
					query.ptNew = genPoint();
					query.psNew = query.ptNew;

//				System.out.println(query.psNew.toString());

				ArrayList<String> result = algo_cikrq.tikrq(query.ps, query.pt, query.q_word,
						query.timeMax, query.relThreshold, query.k);
			if (result != null) 
				{
				System.out.println("result.size():" + result.size());
//					for (String s : result)
//						System.out.println(s);
				}
				ArrayList<String> result2;
				if (type == 0)
					result2 = algo_cikrq.tikrq(query.psNew, query.pt, query.q_word, query.timeMax,
							query.relThreshold, query.k);
				else// type==1
					result2 = algo_cikrq.tikrq(query.ps, query.ptNew, query.q_word, query.timeMax,
							query.relThreshold, query.k);

//			if (result != null && result.size() == query.k) {
				if (result2 != null  && result2.size() >0) {
					System.out.println(query);
					System.out.println("success");
					success = 1;
					q_set.add(query);

					// -------
					// print actual result
					String tmp = "";
					tmp += "Query #" + j + "\n";
					tmp += query.toString() + "\n";
					for (String s : result2)
						tmp += s + "\n";
					queryResult += tmp + "\n\n";
					System.out.println(tmp);
					// ------
					j++;
					break;
				}
			}
		}
		
		String outResultFile = System.getProperty("user.dir") + "/query/r" + fileName;
		FileOutputStream output2 = new FileOutputStream(outResultFile, false);
		output2.write(queryResult.getBytes());
		output2.flush();
		output2.close();
		return q_set;
	}

	/**
	 * 
	 * @param fileName = output file name
	 * @param q_set    = query set to be printed
	 * 
	 *                 format in each row: psx psy psf ptx pty ptf querySize w1 w2
	 *                 .. w5 timeTime threshold k
	 * @throws Exception
	 */
	public static void printQuery(String fileName, ArrayList<Query> q_set) throws Exception {
		String outFile = System.getProperty("user.dir") + fileName;
		String result = "";
		for (Query q : q_set) {
			result += q.print() + "\n";
		}

		FileOutputStream output = new FileOutputStream(outFile, false);
		output.write(result.getBytes());
		output.flush();
		output.close();
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<String> fileNameList = new ArrayList<>();

//		Init.init();
//		fileNameList.add("q1.txt");
//		fileNameList.add("q2.txt");
//		fileNameList.add("q3.txt");
//		fileNameList.add("q4.txt");
//		fileNameList.add("q5.txt");

		Init.init_HSM();
		fileNameList.add("rq1.txt");
		fileNameList.add("rq2.txt");
		fileNameList.add("rq3.txt");
		fileNameList.add("rq4.txt");
		fileNameList.add("rq5.txt");

		ArrayList<ArrayList<Query>> qListList = new ArrayList<>();
		for (String readFileName : fileNameList)
			qListList.add(ExpTIKRQExten.readQuery("/query/" + readFileName));

		// -----
		int i = 0;
		for (ArrayList<Query> qList : qListList) {
			random.setSeed(3);
			String newQueryFileNameD = "d" + fileNameList.get(i);
			ArrayList<Query> q_setD = gen_query_set_from_existing(newQueryFileNameD, 0, qList);
			printQuery("/query/" + newQueryFileNameD, q_setD);

			String newQueryFileNameE = "e" + fileNameList.get(i);
			ArrayList<Query> q_setE = gen_query_set_from_existing(newQueryFileNameE, 1, qList);
			printQuery("/query/" + newQueryFileNameE, q_setE);

			System.gc();
			i++;
		}

		System.out.println("generate query set finish");

//		System.out.println("query: " + q_set);

	}

}
