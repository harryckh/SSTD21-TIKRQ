package algorithm;

import datagenerate.AssignWords;
import datagenerate.AssignWtimeCost;
import datagenerate.DataGen;
import iDModel.GenTopology;
import utilities.DataGenConstant;
import wordProcess.ReadWord;

import java.io.IOException;

public class Init {
    public static void init() throws IOException {
        DataGen dataGen = new DataGen();
        dataGen.genAllData(DataGenConstant.dataType, DataGenConstant.divisionType);

        GenTopology genTopology = new GenTopology();
        genTopology.genTopology();


        AssignWords assignWords = new AssignWords();
        assignWords.assignWords_read();

        AssignWtimeCost assignWtimeCost = new AssignWtimeCost();
        assignWtimeCost.assignWtimeCost_read();

        ReadWord readWord = new ReadWord();
        readWord.readRelation();
    }
}
