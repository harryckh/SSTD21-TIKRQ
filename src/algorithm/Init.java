package algorithm;

import datagenerate.*;
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
        assignWtimeCost.assignWtimeCost_read("syn");

        ReadWord readWord = new ReadWord();
        readWord.readRelation("syn");
    }

    public static void init_HSM() throws IOException {
        HSMDataGenRead hsmDataGenRead = new HSMDataGenRead();
        hsmDataGenRead.dataGen("hsm");

        GenTopology genTopology = new GenTopology();
        genTopology.genTopology();

        AssignWordsHSM assignWordsHSM = new AssignWordsHSM();
        assignWordsHSM.assignWords_read();

        AssignWtimeCost assignWtimeCost = new AssignWtimeCost();
        assignWtimeCost.assignWtimeCost_read("hsm");

        ReadWord readWord = new ReadWord();
        readWord.readRelation("hsm");


    }

    public static void main(String[] args) throws IOException {
        init();
    }
}
