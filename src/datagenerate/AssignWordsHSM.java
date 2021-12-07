package datagenerate;

import indoor_entitity.IndoorSpace;
import indoor_entitity.Partition;
import utilities.DataGenConstant;
import utilities.RoomType;
import wordProcess.WordPro;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Assign words to partitions (HSM dataset)
 *
 * @author Tiantian Liu
 */

public class AssignWordsHSM {
    private static String fileInput_ciword_i = System.getProperty("user.dir") + "/words/ciword_i.txt";
    private static String fileInput_ciword_c = System.getProperty("user.dir") + "/words/ciword_c.txt";
    private static String fileInput_iword = System.getProperty("user.dir") + "/words/itword_i.txt";
    private static String fileOutput = System.getProperty("user.dir") + "/HSM/partition_words.txt";
    private static HashMap<Integer, ArrayList<Integer>> itword_i = new HashMap<>();
    private static HashMap<Integer, String> ciword_i = new HashMap<>();
    private static HashMap<Integer, ArrayList<Integer>> floor_iword_f = new HashMap<>();

    public void assignWords_save() throws IOException {
        findRelationship();
        assignFloor();
        assignIwords();

        // update the relationship between partition and words
        WordPro wordPro = new WordPro();
        wordPro.par_tword_pro("hsm");
        wordPro.par_iword_pro("hsm");
        wordPro.par_cword_pro("hsm");
    }

    public void assignWords_read() throws IOException {
        Path path = Paths.get(fileOutput);
        Scanner scanner = new Scanner(path);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] temp = line.split("\t");
            int parId = Integer.parseInt(temp[0]);
            String type = temp[1];
            int iword = Integer.parseInt(temp[2]);
            ArrayList<Integer> twords = new ArrayList<>();
            for (int i = 3; i < temp.length; i++) {
                twords.add(Integer.parseInt(temp[i]));
            }

            Partition par = IndoorSpace.iPartitions.get(parId);

            par.setIkeyword(iword);
            par.setCategory(type);
            par.setTkeywords(twords);

        }
        scanner.close();
    }

    private void assignIwords() {
        String result = "";
        for (int i = 0; i < IndoorSpace.iPartitions.size(); i++) {
            Partition par = IndoorSpace.iPartitions.get(i);
            if (par.getmType() != RoomType.STORE)
                continue;
            int floor = par.getmFloor();
            ArrayList<Integer> iwords = floor_iword_f.get(floor);

            ArrayList<Integer> twords = null;
            int iword = 0;
            do {
                iword = iwords.get((int) (Math.random() * (iwords.size() - 1)));
                par.setIkeyword(iword);
                par.setCategory(ciword_i.get(iword));
                twords = assignTwords(iword);
            } while (twords == null);

            par.setTkeywords(twords);

            System.out.println(
                    "parId: " + i + "; iword: " + iword + "; type: " + ciword_i.get(iword) + "; rwords: " + twords);

            result += i + "\t" + ciword_i.get(iword) + "\t" + iword + "\t";
            for (int j = 0; j < twords.size(); j++) {
                result += twords.get(j) + "\t";
            }
            result += "\n";

        }

        try {
            FileWriter fw = new FileWriter(fileOutput);
            fw.write(result);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

    }

    private ArrayList<Integer> assignTwords(int iword) {
        ArrayList<Integer> twords_all = itword_i.get(iword);
        ArrayList<Integer> twords = new ArrayList<>();

        int trial = 0;
        int num = (int) (twords_all.size() / 2 + Math.random() * (twords_all.size() - twords_all.size() / 2 + 1));
        while (twords.size() < num) {
            int index = (int) (Math.random() * (twords_all.size()));
            int rword = twords_all.get(index);

            if (!twords.contains(rword)) {
                twords.add(rword);
            }
            trial++;
            if (trial > 10000) {
                return null;
            }
        }
        return twords;
    }

    private void findRelationship() throws IOException {
        String result = "";

        Path path = Paths.get(fileInput_ciword_i);
        Scanner scanner = new Scanner(path);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] temp = line.split("\t");

            int iword = Integer.parseInt(temp[0]);
            String type = temp[1];
            ciword_i.put(iword, type);
        }
        scanner.close();

        Path path1 = Paths.get(fileInput_iword);
        Scanner scanner1 = new Scanner(path1);

        while (scanner1.hasNextLine()) {
            String line = scanner1.nextLine();
            String[] temp = line.split("\t");

            int iword = Integer.parseInt(temp[0]);
            ArrayList<Integer> twords = new ArrayList<>();
            for (int i = 1; i < temp.length; i++) {
                twords.add(Integer.parseInt(temp[i]));
            }
            itword_i.put(iword, twords);
        }
        scanner1.close();
    }

    private void assignFloor() throws IOException {
        Path path = Paths.get(fileInput_ciword_c);
        Scanner scanner = new Scanner(path);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] temp = line.split("\t");
            String[] iwords_arr = temp[1].split(",");

            String cword = temp[0];
            ArrayList<Integer> iwords = new ArrayList<>();
            for (int i = 0; i < iwords_arr.length; i++) {
                int iword = Integer.parseInt(iwords_arr[i]);
                iwords.add(iword);
            }

            int floor = -1;
            if (cword.equals("cosmetics") || cword.equals("bank")) {
                floor = 0;
            }
            if (cword.equals("clothing")) {
                floor = 1;
            }
            if (cword.equals("shoes")) {
                floor = 2;
            }
            if (cword.equals("accessories") || cword.equals("bags")) {
                floor = 3;
            }
            if (cword.equals("electronics")) {
                floor = 4;
            }
            if (cword.equals("travel") || cword.equals("others")) {
                floor = 5;
            }
            if (cword.equals("restaurant") || cword.equals("food")) {
                floor = 6;
            }

            if (floor_iword_f.get(floor) == null) {
                floor_iword_f.put(floor, iwords);
            }
            else {
                ArrayList<Integer> iwords_temp = new ArrayList<>();
                iwords_temp = floor_iword_f.get(floor);
                iwords_temp.addAll(iwords);
                floor_iword_f.put(floor, iwords_temp);
            }
        }
        scanner.close();

    }

    public static void main(String[] arg) throws IOException {
        HSMDataGenRead hsmDataGenRead = new HSMDataGenRead();
        hsmDataGenRead.dataGen("hsm");

        AssignWordsHSM assignHSM = new AssignWordsHSM();
//        assignHSM.assignWords_read();
        assignHSM.assignWords_save();
        for (int i = 0; i < IndoorSpace.iPartitions.size(); i++) {
            Partition par = IndoorSpace.iPartitions.get(i);
            if (par.getmType() == RoomType.STORE) {
                System.out.println("parId: " + i + "; iword: " + par.getIkeyword() + "; type: " + par.getCategory()
                        + "; rwords: " + par.getTkeywords());
            }
        }
    }
}

