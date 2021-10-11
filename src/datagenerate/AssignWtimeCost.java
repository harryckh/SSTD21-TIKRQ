package datagenerate;

import indoor_entitity.IndoorSpace;
import indoor_entitity.Partition;
import utilities.DataGenConstant;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class AssignWtimeCost {

    private static String fileInput = System.getProperty("user.dir") + "/info_cost_wtime/info_cost_wtime.txt";

    private Random random = new Random(1);
    
    public void assignWtimeCost_save() {
        String result = "";
        for(Partition par: IndoorSpace.iPartitions) {
            // cost: [0, 10]
            int cost = (int)(random.nextDouble() * (DataGenConstant.SC_MAX + 1));
            par.setStaticCost(cost);

            // wait time: [0, 300] second
            int waitTime = (int)(random.nextDouble() * (100 + 1));
            par.setWaitTime(waitTime);

            result += par.getmID() + "\t" + cost + "\t" + waitTime + "\n";
        }

        try {
            FileWriter fw = new FileWriter(System.getProperty("user.dir") + "/info_cost_wtime/info_cost_wtime.txt");
            fw.write(result);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
    }

    public void assignWtimeCost_read() throws IOException {
        Path path = Paths.get(fileInput);
        Scanner scanner = new Scanner(path);

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] temp = line.split("\t");
            int parId = Integer.parseInt(temp[0]);
            int cost = Integer.parseInt(temp[1]);
            int waitTime = Integer.parseInt(temp[2]);

            Partition par = IndoorSpace.iPartitions.get(parId);
            par.setStaticCost(cost);
            par.setWaitTime(waitTime);

        }
        scanner.close();
    }

    public static void main(String[] arg) throws IOException{
        DataGen dataGen = new DataGen();
        dataGen.genAllData(DataGenConstant.dataType, DataGenConstant.divisionType);

        AssignWtimeCost assignWtimeCost = new AssignWtimeCost();
        assignWtimeCost.assignWtimeCost_save();
//        assignWtimeCost.assignWtimeCost_read();
        for (Partition par: IndoorSpace.iPartitions) {
            System.out.println("parId: " + par.getmID() + "; cost: " + par.getStaticCost() + "; wtime: " + par.getWaitTime());
        }

    }
}
