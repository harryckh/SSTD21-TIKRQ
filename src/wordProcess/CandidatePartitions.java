package wordProcess;

import utilities.Constant;

/**
 * given a query keyword list, return its candidate partition list
 * @author Tiantian
 */

import utilities.DataGenConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CandidatePartitions {
	private ArrayList<ArrayList<ArrayList<String>>> allCandPars = new ArrayList<>();
	private ArrayList<ArrayList<String>> allCandPars3 = new ArrayList<>();
	
	private ArrayList<String> queryWords;
	private double threshold;

	public CandidatePartitions(ArrayList<String> queryWords, double threshold) {
		this.queryWords = queryWords;
		this.threshold = threshold;
	}

	// 3D arrayList: first level is the list of query word
	// second level: "ArrayList<ArrayList<String>>" is the list of part with this
	// query word
//	third level "ArrayList<String>"[0] = parID, [1]=score
	public ArrayList<ArrayList<ArrayList<String>>> findAllCandPars() throws IOException {
		for (int i = 0; i < queryWords.size(); i++) {
			String word = queryWords.get(i);
//			System.out.println(word);
			// if the keyword is a c-word
			if (DataGenConstant.cWords.contains(word)) {

				ArrayList<ArrayList<String>> candPars = new ArrayList<>();
				ArrayList<Integer> parList = ReadWord.par_cword_c.get(word);
				for (int parId : parList) {
					ArrayList<String> temp = new ArrayList<>();
					temp.add(parId + "");
					temp.add(Constant.cWordMatchScore + ""); /// score for the partition with this c-word
					temp.add(i+"");
					candPars.add(temp);
				}
				allCandPars.add(candPars);
			}
			// if the keyword is a i-word/t-word
			else if(word.matches("-?\\d+")) ///by Harry: to handle gen query i/t-word represented by int
			{
				// find all partitions for the keyword
				WordPartitionRelationship wr = new WordPartitionRelationship(Integer.parseInt(word), threshold);
				allCandPars.add(wr.transform(i));
//				System.out.println(wr.transform(i));
				
			}
			else if (ReadWord.index_itword.get(word) != null) {
//				System.out.println(3);
				// find all partitions for the keyword
				WordPartitionRelationship wr = new WordPartitionRelationship(ReadWord.index_itword
						.get(word), threshold);
				allCandPars.add(wr.transform(i));
			}
			// if no such a word in our setting
			else {
//				System.out.println(4);
				ArrayList<ArrayList<String>> candPars = new ArrayList<>();
				ArrayList<String> temp = new ArrayList<>();
				temp.add(0 + "");
				temp.add(0 + "");
				temp.add(i+"");
				candPars.add(temp);
				allCandPars.add(candPars);
			}
		}

		return allCandPars;
	}
	// 3D arrayList: first level is the list of query word
	// second level: "ArrayList<ArrayList<String>>" is the list of part with this
	// query word
//	third level "ArrayList<String>"[0] = parID, [1]=score
	public ArrayList<ArrayList<String>> findAllCandPars3() throws IOException {
		for (int i = 0; i < queryWords.size(); i++) {
			String word = queryWords.get(i);
//			System.out.println(word);
			// if the keyword is a c-word
			if (DataGenConstant.cWords.contains(word)) {

				ArrayList<ArrayList<String>> candPars = new ArrayList<>();
				ArrayList<Integer> parList = ReadWord.par_cword_c.get(word);
				for (int parId : parList) {
					ArrayList<String> temp = new ArrayList<>();
					temp.add(parId + "");
					temp.add(Constant.cWordMatchScore + ""); /// score for the partition with this c-word
					temp.add(i+"");
					allCandPars3.add(temp);
				}
			}
			// if the keyword is a i-word/t-word
			else if(word.matches("-?\\d+")) /// to handle gen query i/t-word represented by int
			{
				// find all partitions for the keyword
				WordPartitionRelationship wr = new WordPartitionRelationship(Integer.parseInt(word), threshold);
				allCandPars3.addAll(wr.transform(i));
//				System.out.println(wr.transform(i));
				
			}
			else if (ReadWord.index_itword.get(word) != null) {
//				System.out.println(3);
				// find all partitions for the keyword
				WordPartitionRelationship wr = new WordPartitionRelationship(ReadWord.index_itword
						.get(word), threshold);
				allCandPars3.addAll(wr.transform(i));
			}
			// if no such a word in our setting
			else {
//				System.out.println(4);
				ArrayList<ArrayList<String>> candPars = new ArrayList<>();
				ArrayList<String> temp = new ArrayList<>();
				temp.add(0 + "");
				temp.add(0 + "");
				temp.add(i+"");
				allCandPars3.add(temp);
			}
		}

		return allCandPars3;
	}

	// test
	public static void main(String[] arg) throws IOException {
		ReadWord readWord = new ReadWord();
		readWord.readRelation("hsm");
		CandidatePartitions candidatePartitions = new CandidatePartitions(new ArrayList<>(Arrays
				.asList("shoes", "china construction bank (asia)")), 0.04);
		ArrayList<ArrayList<ArrayList<String>>> allPars = candidatePartitions.findAllCandPars();

		for (int i = 0; i < allPars.size(); i++) {
			ArrayList<ArrayList<String>> parList = allPars.get(i);
			System.out.println("one word---------");
			for (int j = 0; j < parList.size(); j++) {
				ArrayList<String> par = parList.get(j);
				System.out.println(par.get(0) + "  " + par.get(1));
			}
		}
	}
}
