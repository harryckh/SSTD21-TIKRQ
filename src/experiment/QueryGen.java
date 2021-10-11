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

public class QueryGen {
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

	/**
	 * 
	 * @param size number of query keywords
	 * @param c    prob for c word
	 * @param i    prob for i word
	 * @param t    prob for t word
	 * @return the set of query keywords
	 * 
	 *         note that c+i+t should be =1
	 */
	public static ArrayList<String> gen_query_word(int size, double c, double i, double t) {
		ArrayList<String> key = new ArrayList<>();

		double t1 = c + i;
		int j = 0;
		while (j < size) {

			double r = random.nextDouble();// [0,1]
			String word = "";
			if (r < c) {// gen c
				int cindex = rand_i(0, DataGenConstant.cWords.size());
				word = DataGenConstant.cWords.get(cindex) + "";
			} else if (r < t1) {// gen i
				WordPartitionRelationship wr;
				do {
					word = (-1 * rand_i(1, DataGenConstant.iWordSize)) + "";
					wr = new WordPartitionRelationship(Integer.parseInt(word), DataGenConstant.threshold);
				} while (wr.transform(0).size() == 0);
			} else {// gen t
				WordPartitionRelationship wr;
				do {
					word = rand_i(1, DataGenConstant.tWordSize) + "";
					wr = new WordPartitionRelationship(Integer.parseInt(word), DataGenConstant.threshold);
				} while ( wr.transform(0).size() == 0);
			}
			if (!key.contains(word)) {
				key.add(word);
				j++;
			}
		}

		return key;
	}
	/**
	 * 
	 * @param size number of query keywords
	 * @param c    prob for c word
	 * @param i    prob for i word
	 * @param t    prob for t word
	 * @return the set of query keywords
	 * 
	 *         note that c+i+t should be =1
	 */
	public static ArrayList<String> gen_query_word2(int size, double c, double i, double t,
			ArrayList<String> cword, ArrayList<Integer> iword, ArrayList<Integer> tword) {
		ArrayList<String> key = new ArrayList<>();

		double t1 = c + i;
		int j = 0;
		while (j < size) {

			double r = random.nextDouble();// [0,1]
			String word = "";
			if (r < c) {// gen c
				int cindex = rand_i(0, cword.size());
				word = cword.get(cindex) + "";
			} else if (r < t1) {// gen i
				int cindex = rand_i(0, iword.size());
				word = iword.get(cindex) + "";
			} else {// gen t
				int cindex = rand_i(0, tword.size());
				word = tword.get(cindex) + "";
			}
			if (!key.contains(word)) {
				key.add(word);
				j++;
			}
		}

		return key;
	}
	public static ArrayList<ArrayList<String>> gen_query_word_set(int num, int size, double c, double i, double t) {
		ArrayList<ArrayList<String>> q_word_set = new ArrayList<>();
		for (int j = 0; j < num; j++) {
			q_word_set.add(gen_query_word(size, c, i, t));
		}
		return q_word_set;
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
	 */
	public static ArrayList<Query> gen_query_set(int num, int size, double c, double i, double t, double timeMax,
			double relThreshold, int k, String fileName) throws Exception {

		AlgSSA algo_cikrq = new AlgSSA(); // testing if the query is valid

		ArrayList<Query> q_set = new ArrayList<>();

		String queryResult = "";

		int j = 1;
		while (j <= num) {
			System.out.println("generating query " + j);
			Query query = new Query();

			// gen query word
			query.q_word = gen_query_word(size, c, i, t);

			// gen start and end point

			query.ps = genPoint();
			query.pt = genPoint();

			Partition sPartition = CommonFunction.locPartition(query.ps);
			Partition tPartition = CommonFunction.locPartition(query.pt);

//			// gen timeMax
//			String fast_path = CommonFunction.findFastestPathP2P(query.ps, query.pt, sPartition, tPartition);
//			double time = Double.parseDouble(fast_path.split("\t")[0]);
//			query.timeMax = time * 1.6;
////			query.timeMax = timeMax;
//			System.out.println("time: " + time + " timeMax: " + query.timeMax);
//
//			// ---
//			ArrayList<String> cword = new ArrayList<>();
//			ArrayList<Integer> iword = new ArrayList<>();
//			ArrayList<Integer> tword = new ArrayList<>();
//			for (Partition par : IndoorSpace.iPartitions) {
//
//				if (par.getmType() == RoomType.STORE) {
//					for (int dId : par.getmDoors()) {
//
//						Door door = IndoorSpace.iDoors.get(dId);
//						String path1 = CommonFunction.findFastestPathP2P(query.ps, door, sPartition, par);
//						String path2 = CommonFunction.findFastestPathP2P(door, query.pt, par, tPartition);
//						double time1 = Double.parseDouble(path1.split("\t")[0]);
//						double time2 = Double.parseDouble(path2.split("\t")[0]);
//						if (time1 + time2 < time) {
////						System.out.println("time1: " + time1 + " time2: " + time2 + " "+ par.getmID());
//							// this part is qualified in range
//							if (!cword.contains(par.getCategory()))
//								cword.add(par.getCategory());
//							if (!iword.contains(par.getIkeyword()))
//								iword.add(par.getIkeyword());
//							for (Integer twordid : par.getTkeywords()) {
//								if (!tword.contains(twordid))
//									tword.add(twordid);
//							}
//							break;
//						}
//					}
//				}
//			}
//			System.out.println("--" + cword.size() + " " + iword.size() + " " + tword.size());
//			System.out.println(cword);
//			System.out.println("--");
//			System.out.println(iword);
//			System.out.println("--");
//			System.out.println(tword);
//			query.q_word = gen_query_word2(size, c, i, t,cword,iword,tword);

			// ---
			query.timeMax = timeMax;
			query.relThreshold = relThreshold;
			query.k = k;

			System.out.println(query);

			ArrayList<String> result = algo_cikrq.tikrq(query.ps, query.pt, query.q_word, query.timeMax,
					query.relThreshold, query.k);

			if (result != null && result.size() == k) {
				System.out.println("success");
				q_set.add(query);

				// --
				// print actual result
				String tmp = "";
				tmp += "Query #" + j + "\n";
				tmp += query.toString() + "\n";
				for (String s : result)
					tmp += s + "\n";
				queryResult += tmp + "\n\n";
				System.out.println(tmp);
				// --
				j++;
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

		Init.init();
//		for(Door door:IndoorSpace.iDoors) {
//			System.out.println( door.toString());
//		}
		int set = 1;
		for (int j = 1; j <= 1; j = j + 1) {
			random.setSeed(2);
			System.out.println("generating query set " + (set++));
			/* parameter setting */
			String fileName = "test" + ".txt";
			int numOfQueries = 10;
			int q_word = 4;
			double c, i, t;
//			t = j/10.0;
//			c = (10.0-j)/20.0;
//			i = (10.0-j)/20.0;
//			System.out.println("----------- "+ c + " " + i + " " + t);
			c = 0.2;
			i = 0.4;
			t = 0.4;
			double timeMax = 3500;
			int k = 7;

			double relthreshold = 1;// useless

			ArrayList<Query> q_set = gen_query_set(numOfQueries, q_word, //
					c, i, t, timeMax, relthreshold, k, fileName);
			printQuery("/query/" + fileName, q_set);
			System.gc();
		}

		System.out.println("generate query set finish");

//		System.out.println("query: " + q_set);

	}

}
