package wordProcess;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Read word relationship
 * 
 * @author Tiantian Liu
 */

public class ReadWord {
	public static HashMap<Integer, ArrayList<Integer>> par_iword_i = new HashMap<>();
	public static HashMap<Integer, ArrayList<Integer>> par_tword_t = new HashMap<>();
	public static HashMap<Integer, ArrayList<Integer>> par_tword_p = new HashMap<>();
	public static HashMap<String, ArrayList<Integer>> ciword_c = new HashMap<>();
	public static HashMap<String, Integer> index_itword = new HashMap<>();
	public static HashMap<String, ArrayList<Integer>> par_cword_c = new HashMap<>();

	private static String inputFile_par_tword_t = System.getProperty("user.dir") + "/words/par_tword_t.txt";
	private static String inputFile_par_tword_p = System.getProperty("user.dir") + "/words/par_tword_p.txt";
	private static String inputFile_par_iword_i = System.getProperty("user.dir") + "/words/par_iword_i.txt";
	private static String inputFile_ciword_c = System.getProperty("user.dir") + "/words/ciword_c.txt";
	private static String inputFile_index_itword = System.getProperty("user.dir") + "/words/index_itword.txt";
	private static String inputFile_par_cword_c = System.getProperty("user.dir") + "/words/par_cword_c.txt";

	public void readRelation() throws IOException {
		Path path = Paths.get(inputFile_par_iword_i);
		Scanner scanner = new Scanner(path);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] tempArr = line.split("\t");
			int iword = Integer.parseInt(tempArr[0]);
			ArrayList<Integer> parList = new ArrayList<>();
			for (int i = 1; i < tempArr.length; i++) {
				parList.add(Integer.parseInt(tempArr[i]));
			}
			par_iword_i.put(iword, parList);
		}
		scanner.close();

		Path path1 = Paths.get(inputFile_ciword_c);
		Scanner scanner1 = new Scanner(path1);
		while (scanner1.hasNextLine()) {
			String line = scanner1.nextLine();
			String[] tempArr = line.split("\t");
			String cword = tempArr[0];
			String[] iwordsArr = tempArr[1].split(",");
			ArrayList<Integer> iwords = new ArrayList<>();
			for (int i = 0; i < iwordsArr.length; i++) {
				iwords.add(Integer.parseInt(iwordsArr[i]));
			}
			ciword_c.put(cword, iwords);
		}
		scanner1.close();

		Path path2 = Paths.get(inputFile_par_tword_t);
		Scanner scanner2 = new Scanner(path2);
		while (scanner2.hasNextLine()) {
			String line = scanner2.nextLine();
			String[] tempArr = line.split("\t");
			int tword = Integer.parseInt(tempArr[0]);
			ArrayList<Integer> parList = new ArrayList<>();
			for (int i = 1; i < tempArr.length; i++) {
				parList.add(Integer.parseInt(tempArr[i]));
			}
			par_tword_t.put(tword, parList);
		}
		scanner2.close();

		Path path3 = Paths.get(inputFile_par_tword_p);
		Scanner scanner3 = new Scanner(path3);
		while (scanner3.hasNextLine()) {
			String line = scanner3.nextLine();
			String[] tempArr = line.split("\t");
			int parId = Integer.parseInt(tempArr[0]);
			ArrayList<Integer> twords = new ArrayList<>();
			for (int i = 1; i < tempArr.length; i++) {
				twords.add(Integer.parseInt(tempArr[i]));
			}
			par_tword_p.put(parId, twords);
		}
		scanner3.close();
		Path path4 = Paths.get(inputFile_index_itword);
		Scanner scanner4 = new Scanner(path4);
		while (scanner4.hasNextLine()) {
			String line = scanner4.nextLine();
			String[] tempArr = line.split("\t");
			String word = tempArr[0];
			int index = Integer.parseInt(tempArr[1]);
			index_itword.put(word, index);
		}
		scanner4.close();

		Path path5 = Paths.get(inputFile_par_cword_c);
		Scanner scanner5 = new Scanner(path5);
		while (scanner5.hasNextLine()) {
			String line = scanner5.nextLine();
			String[] tempArr = line.split("\t");
			String cword = tempArr[0];
			ArrayList<Integer> parList = new ArrayList<>();
			for (int i = 1; i < tempArr.length; i++) {
				parList.add(Integer.parseInt(tempArr[i]));
			}
			par_cword_c.put(cword, parList);
		}
		scanner5.close();
	}

}
