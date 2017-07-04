
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zachary Cohan
 */
public class TreeTester {

    private static DataMap trainingSet;
    private static DataMap tuningSet;
    private static DataMap fullTestingSet;
    private static DataMap test;
    private static DecisionTree tree;
    
    public static void main(String[] args) {
        File vd = new File("src/voting-data.tsv");
        Scanner in = null;
        String[] datumString;
        Datum datum;
        try {
            in = new Scanner(vd);
        } catch (FileNotFoundException ex) {
            System.out.println("The file was not found");
        }
        
        trainingSet = new DataMap();
        tuningSet = new DataMap();
        fullTestingSet = new DataMap();
        
        int whichLine = 0;
        while (in.hasNext()) {
            datumString = in.nextLine().split("\t");
            datum = new Datum(datumString[0], datumString[1], datumString[2]);
            if (whichLine % 4 == 0) {
                tuningSet.add(datum);
                //System.out.println("added to tuning set: "+datum.toString());
            } else {
                trainingSet.add(datum);
                //System.out.println("training set: "+datum.toString());
            }
            fullTestingSet.add(datum);
            whichLine++;
        }

        tree = new DecisionTree(trainingSet);
        //tree = new DecisionTree();
        //System.out.println(tree.findBestSplit(trainingSet));
        tree.printTree();
        System.out.println();
        int correct = 0;
        int incorrect = 0;
        String s;
        for(Datum d:tuningSet.data)
        {
            s = tree.classify(d);
            if(s.equals(d.classification))correct++;
            else incorrect++;
        }
        System.out.println("The first tree correctly classifies ");
        System.out.println(correct+" out of the "+tuningSet.size()+" items in the tuning set. ("+((double)(correct)/(double)(tuningSet.size()))*100+"%)");
        System.out.println();
        
        correct = 0;
        incorrect = 0;
        Datum d;
        for(int i = 0;i<fullTestingSet.size();i++)
        {
            d = fullTestingSet.data.get(i);
            test = DecisionTree.createTrainingSet(fullTestingSet, i);
            tree = new DecisionTree(test);
            s = tree.classify(d);
            if(s.equals(d.classification))correct++;
            else incorrect++;   
        }
        double percentCorrect = ((double)correct/(double)fullTestingSet.size())*100;
        
        System.out.println("Leave-One-Out cross validation shows this tree to classify ");
        System.out.println("correctly "+DecisionTree.round(percentCorrect, 4)+" of the time");
        
        
        
        
        

    }
}
