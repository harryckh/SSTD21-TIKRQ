package wordProcess;
/**
 * given a word, transform it to a partition list with word and relevance
 * @author Tiantian
 */

import indoor_entitity.IndoorSpace;
import indoor_entitity.Partition;
import utilities.Constant;
import utilities.DataGenConstant;
import utilities.RoomType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class WordPartitionRelationship {
    private int wordId;
    private double threshold;
    private ArrayList<ArrayList<String>> candPars = new ArrayList<>();
    private ArrayList<Integer> partitionList = new ArrayList<>();


    public WordPartitionRelationship(int wordId, double threshold) {
        this.wordId = wordId;
        this.threshold = threshold;
    }

    // find all partitions for a keyword
    public ArrayList<ArrayList<String>> transform(int i) {

        //if the word is an i-word
        if ( -(DataGenConstant.iWordSize) <= wordId && wordId < 0 ) {
            // directly
            ArrayList<Integer> parList = ReadWord.par_iword_i.get(wordId);
            for (int parId: parList) {
                ArrayList<String> temp = new ArrayList<>();
                temp.add(parId + "");
                temp.add(Constant.iWordDMatchScore + "");
                temp.add(i+"");
                candPars.add(temp);
                partitionList.add(parId);
            }
            // others
            String cword = getKey(ReadWord.ciword_c, wordId);
            ArrayList<Integer> iwords = ReadWord.ciword_c.get(cword);
            for (int iword: iwords) {
                ArrayList<Integer> parList1 = ReadWord.par_iword_i.get(iword);
                for (int parId: parList1) {
                    if (partitionList.contains(parId)) continue;
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(parId + "");
                    temp.add(Constant.iWordIndMatchScore + "");
                    temp.add(i+"");
                    candPars.add(temp);
                    partitionList.add(parId);
                }
            }


        }

        // if the word is a t-word
        else if (wordId > 0 && wordId <= DataGenConstant.tWordSize) {
            ArrayList<Integer> twords_all = new ArrayList<>();
            // directly
            ArrayList<Integer> parList = ReadWord.par_tword_t.get(wordId);
            for (int parId: parList) {
                ArrayList<String> temp = new ArrayList<>();
                temp.add(parId + "");
                temp.add(Constant.tWordMatchScore + "");
                temp.add(i+"");
                candPars.add(temp);
                partitionList.add(parId);
                twords_all.addAll(ReadWord.par_tword_p.get(parId));
            }

            // others
            for (Partition par: IndoorSpace.iPartitions) {
                if (par.getmType() != RoomType.STORE) continue;
                int parId = par.getmID();
                if (partitionList.contains(parId)) continue;
                double rel = calRelevance(twords_all, parId);
                if (rel > threshold) {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(parId + "");
                    temp.add(rel + "");
                    temp.add(i+"");
                    candPars.add(temp);
                    partitionList.add(parId);
                }
            }
        }
        // if the word is not in our setting
        else {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(0 + "");
            temp.add(0 + "");
            temp.add(i+"");
            candPars.add(temp);
        }
        return candPars;
    }

    private double calRelevance(ArrayList<Integer> twords_all, int parId) {
        double rel = 0;
        ArrayList<Integer> union = new ArrayList<>();
        ArrayList<Integer> inter = new ArrayList<>();
        for (int i = 0; i < twords_all.size(); i++) {
            if (!union.contains(twords_all.get(i))) {
                union.add((int) twords_all.get(i));
            }
        }
        ArrayList<Integer> twords = ReadWord.par_tword_p.get(parId);

        //--
        if(twords==null || twords.size()==0)
        	return 0;
        //--
        for (int word: twords){
            if (!union.contains(word)) {
                union.add(word);
            }
            if (twords_all.contains(word)) {
                if (!inter.contains(word)) {
                    inter.add(word);
                }
            }
        }

        rel = (double)(inter.size())/(double)(union.size());
        return rel;
    }

    private static String getKey(HashMap<String, ArrayList<Integer>> map, int value){
        String type = "";
        for(String key: map.keySet()){
            if(map.get(key).contains(value)){
                type = key;
            }
        }
        return type;
    }

    // test
    public static void main (String arg[]) throws IOException {
        ReadWord readWord = new ReadWord();
        readWord.readRelation();
        WordPartitionRelationship wr = new WordPartitionRelationship(504, 0.04);
        ArrayList<ArrayList<String>> cw = wr.transform(1);
        for (int i = 0; i < cw.size(); i++)
        {
            ArrayList<String> cwr = cw.get(i);
            System.out.println(cwr.get(0) + "  " + cwr.get(1));
        }
    }

}
