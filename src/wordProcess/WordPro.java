package wordProcess;

import utilities.DataGenConstant;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Process words
 * @author Tiantian Liu
 */

public class WordPro {
    private static String fileInput_ciword_c = System.getProperty("user.dir") + "/words/ciword_c.txt";
    private static String fileOutput_ciword_i = System.getProperty("user.dir") + "/words/ciword_i.txt";
    private static String fileInput_partition_words = System.getProperty("user.dir") + "/words/partition_words.txt";
    private static String fileOutput_par_tword_p= System.getProperty("user.dir") + "/words/par_tword_p.txt";
    private static String fileOutput_par_tword_t = System.getProperty("user.dir") + "/words/par_tword_t.txt";
    private static String fileOutput_par_iword_p= System.getProperty("user.dir") + "/words/par_iword_p.txt";
    private static String fileOutput_par_iword_i = System.getProperty("user.dir") + "/words/par_iword_i.txt";
    private static String fileOutput_par_cword_p= System.getProperty("user.dir") + "/words/par_cword_p.txt";
    private static String fileOutput_par_cword_c = System.getProperty("user.dir") + "/words/par_cword_c.txt";

    private static HashMap<String, ArrayList<Integer>> category = new HashMap<>();
    private static HashMap<Integer, ArrayList<Integer>> par_twords = new HashMap<>();
    private static HashMap<Integer, Integer> par_iwords = new HashMap<>();
    private static HashMap<Integer, String> par_cwords = new HashMap<>();

    public void par_tword_pro() throws IOException {
        String result1 = "";
        String result2 = "";
        Path path = Paths.get(fileInput_partition_words);
        Scanner scanner = new Scanner(path);

        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] temp = line.split("\t");
            int parId = Integer.parseInt(temp[0]);
            ArrayList<Integer> twords = new ArrayList<>();

            result1 += parId + "\t";
            for (int i = 3; i < temp.length; i++) {
                twords.add(Integer.parseInt(temp[i]));
                result1 += temp[i] + "\t";
            }
            result1 += "\n";

            par_twords.put(parId, twords);
        }
        scanner.close();
        for (int i = 1; i <= DataGenConstant.tWordSize; i++) {
            ArrayList<Integer> parIds = getKey2(par_twords, i);
            result2 += i + "\t";
            for (int j = 0; j < parIds.size(); j++) {
                result2 += parIds.get(j) + "\t";
            }
            result2 += "\n";
        }

        try {
            FileWriter fw = new FileWriter(fileOutput_par_tword_p);
            fw.write(result1);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        try {
            FileWriter fw = new FileWriter(fileOutput_par_tword_t);
            fw.write(result2);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
    }

    public void par_iword_pro() throws IOException {
        String result1 = "";
        String result2 = "";
        Path path = Paths.get(fileInput_partition_words);
        Scanner scanner = new Scanner(path);

        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] temp = line.split("\t");
            int parId = Integer.parseInt(temp[0]);
            int iword = Integer.parseInt(temp[2]);
            result1 += parId + "\t" + iword + "\n";

            par_iwords.put(parId, iword);
        }
        scanner.close();
        for (int i = DataGenConstant.iWordSize; i >= 1; i--) {
            ArrayList<Integer> parIds = getKey3(par_iwords, i * (-1));
            result2 += i * (-1) + "\t";
            for (int j = 0; j < parIds.size(); j++) {
                result2 += parIds.get(j) + "\t";
            }
            result2 += "\n";
        }

        try {
            FileWriter fw = new FileWriter(fileOutput_par_iword_p);
            fw.write(result1);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        try {
            FileWriter fw = new FileWriter(fileOutput_par_iword_i);
            fw.write(result2);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
    }

    public void par_cword_pro() throws IOException {
        String result1 = "";
        String result2 = "";
        Path path = Paths.get(fileInput_partition_words);
        Scanner scanner = new Scanner(path);

        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] temp = line.split("\t");
            int parId = Integer.parseInt(temp[0]);
            String cword = temp[1];
            result1 += parId + "\t" + cword + "\n";

            par_cwords.put(parId, cword);
        }
        scanner.close();
        for (int i = 0; i < DataGenConstant.cWords.size(); i++) {
            ArrayList<Integer> parIds = getKey4(par_cwords, DataGenConstant.cWords.get(i));
            result2 += DataGenConstant.cWords.get(i)  + "\t";
            for (int j = 0; j < parIds.size(); j++) {
                result2 += parIds.get(j) + "\t";
            }
            result2 += "\n";
        }

        try {
            FileWriter fw = new FileWriter(fileOutput_par_cword_p);
            fw.write(result1);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        try {
            FileWriter fw = new FileWriter(fileOutput_par_cword_c);
            fw.write(result2);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
    }


    private void ciword_pro() throws IOException {
        String result = "";

        Path path = Paths.get(fileInput_ciword_c);
        Scanner scanner = new Scanner(path);

        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] temp = line.split("\t");

            String type = temp[0];
            String[] wordArr = temp[1].split(",");
            ArrayList<Integer> wordList = new ArrayList<>();

            for (int i = 0; i < wordArr.length; i++) {
                int word = Integer.parseInt(wordArr[i]);
                if (word > 0) {
                    System.out.println("type: " + type + ", word: " + word + ", i: " + i);
                }
                wordList.add(word);
            }
            category.put(type, wordList);
            System.out.println("type: " + type + ", wordList: " + wordList);
        }
        scanner.close();

        for (int i = DataGenConstant.iWordSize; i >= 1; i--) {
            String type = getKey(category, i * (-1));
            result += i * (-1) + "\t" + type + "\n";
//            System.out.println("word: " + i * (-1) + ", type: " + type);
        }

        try {
            FileWriter fw = new FileWriter(fileOutput_ciword_i);
            fw.write(result);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }


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

    private static ArrayList<Integer> getKey2(HashMap<Integer, ArrayList<Integer>> map, int value){
        ArrayList<Integer> pars = new ArrayList<>();
        for(int key: map.keySet()){
            if(map.get(key).contains(value)){
                pars.add(key);
            }
        }
        return pars;
    }

    private static ArrayList<Integer> getKey3(HashMap<Integer, Integer> map, int value){
        ArrayList<Integer> pars = new ArrayList<>();
        for(int key: map.keySet()){
            if(map.get(key).equals(value)){
                pars.add(key);
            }
        }
        return pars;
    }

    private static ArrayList<Integer> getKey4(HashMap<Integer, String> map, String value){
        ArrayList<Integer> pars = new ArrayList<>();
        for(int key: map.keySet()){
            if(map.get(key).equals(value)){
                pars.add(key);
            }
        }
        return pars;
    }

    public static void main(String[] arg) throws IOException {
    	System.out.println("Running WordPro...");
        WordPro wordPro = new WordPro();
        wordPro.ciword_pro();
        wordPro.par_tword_pro();
        wordPro.par_iword_pro();
        wordPro.par_cword_pro();
    }

}
