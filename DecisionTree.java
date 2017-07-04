
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * Creates a tree to classify political figures based on how they vote
 *
 * @author Zachary Cohan
 */
public class DecisionTree {

    Node root = null;

    DataMap trainingData;
    DataMap tuningData;

    private class Node {

        Node yea;
        Node nay;
        Node nv;

        boolean isLeaf;
        int issue = -1;
        String classification = null;
        String majIfSplitNode = null;

        private Node(int issue, String m) {
            if (issue < 0) {
                throw new IllegalArgumentException();
            }
            isLeaf = false;
            this.issue = issue;
            majIfSplitNode = m;
        }

        private Node(String classification) {
            this.classification = classification;
            isLeaf = true;
        }

        private boolean isLeaf() {
            return isLeaf;
        }
    }

    /**
     * @param args the command line arguments
     */
    public DecisionTree(DataMap trainingSet) {
        this.trainingData = trainingSet;
        //System.out.println(getInitialSplit());
        root = new Node(getInitialSplit(), trainingSet.getMajClass());
        ArrayList<DataMap> initSplit = resplit(trainingSet, root.issue);
        createTree(root, initSplit);
        //System.out.println(root.issue);    
    }

    public DecisionTree() {

    }

    private void createTree(Node n, ArrayList<DataMap> splits) {

        if (splits == null) {
            return;
        }

        for (int i = 0; i < splits.size(); i++) {
            if (splits.get(i) == null) {
                continue;
            } else {
                calcEntropy(splits.get(i));
                if (i == 0 && splits.get(i).entropy == 0) {
                    n.yea = new Node(splits.get(i).data.get(0).classification);
                }
                if (i == 0 && splits.get(i).entropy > 0) {
                    int bs = findBestSplit(splits.get(i));
                    if (bs < 0) {
                        n.yea = new Node(splits.get(i).getMajClass());
                        return;
                    }
                    ArrayList<DataMap> a = split(splits.get(i), bs);
                    Node m = new Node(bs, splits.get(i).getMajClass());
                    n.yea = m;

                    createTree(m, a);
                }
                if (i == 1 && splits.get(i).entropy == 0) {
                    n.nay = new Node(splits.get(i).data.get(0).classification);
                }
                if (i == 1 && splits.get(i).entropy > 0) {
                    int bs = findBestSplit(splits.get(i));
                    if (bs < 0) {
                        n.nay = new Node(splits.get(i).getMajClass());
                        return;
                    }
                    ArrayList<DataMap> a = split(splits.get(i), bs);

                    Node m = new Node(bs, splits.get(i).getMajClass());
                    n.nay = m;
                    createTree(m, a);
                }
                if (i == 2 && splits.get(i).entropy == 0) {
                    n.nv = new Node(splits.get(i).data.get(0).classification);
                }
                if (i == 2 && splits.get(i).entropy > 0) {
                    int bs = findBestSplit(splits.get(i));
                    if (bs < 0) {
                        n.nv = new Node(splits.get(i).getMajClass());
                        return;
                    }
                    ArrayList<DataMap> a = split(splits.get(i), bs);

                    Node m = new Node(bs, splits.get(i).getMajClass());
                    n.nv = m;
                    createTree(m, a);
                }
            }
        }
    }

    public static double lb2(double num) {
        if (num == 0) {
            return 0;
        }

        return (round(Math.log(num), 8)) / (round(Math.log(2), 8));
    }

    private int getInitialSplit() {
        int bestSplitSoFar = -1;
        double temp;
        double bestGainSoFar = 0;

        for (int i = 0; i < 10; i++) {
            ArrayList<DataMap> splits = split(trainingData, i);
            temp = calcInfoGain(trainingData, splits);

            if (temp > bestGainSoFar) {
                bestGainSoFar = temp;
                bestSplitSoFar = i;
            }
        }

        return bestSplitSoFar;
    }

    public static double calcEntropy(DataMap data) {
        if (data == null) {
            return -1;
        }
        double dem = 0;
        double rep = 0;
        double total = data.size();
        double entropy;

        for (Datum d : data.data) {
            if (d.classification.equals("D")) {
                dem++;
            } else {
                rep++;
            }
        }

        double a = dem / total;
        double b = rep / total;

        entropy = -(a * (lb2(a))) - (b * (lb2(b)));

        data.entropy = entropy;

        return entropy;
    }

    /**
     * calculates the information gain of a parent dataMap and an arrayList of
     * its children after a split
     *
     */
    private double calcInfoGain(DataMap data, ArrayList<DataMap> splits) {
        double parentEntropy;
        double weightedSum = 0;
        double[] splitEntropies = new double[splits.size()];

        if (data.entropy == null) {
            parentEntropy = calcEntropy(data);
        } else {
            parentEntropy = data.entropy;
        }

        for (int i = 0; i < splits.size(); i++) {
            splitEntropies[i] = calcEntropy(splits.get(i));
        }

        for (int i = 0; i < splitEntropies.length; i++) {
            if (splitEntropies[i] == -1) {//if there is no array, skip this iteration
                continue;
            }
            weightedSum += (splitEntropies[i] * ((double) splits.get(i).size() / (double) data.size()));
        }

        return parentEntropy - weightedSum;
    }

    public ArrayList<DataMap> resplit(DataMap m, int issue) {
        if (issue == -1) {
            return null;
        }
        if (m.alreadySplitOn(issue)) {
            return null;
        }

        ArrayList<DataMap> toReturn = new ArrayList<>();

        ArrayList<Datum> votedYea = new ArrayList<>();
        ArrayList<Datum> votedNay = new ArrayList<>();
        ArrayList<Datum> nv = new ArrayList<>();

        for (Datum d : m.data) {
            if (d.getVote(issue) == 1) {
                votedYea.add(d);
            } else if (d.getVote(issue) == -1) {
                votedNay.add(d);
            } else {
                nv.add(d);
            }
        }
        boolean[] newSplits = new boolean[m.asoi.length];
        for (int i = 0; i < newSplits.length; i++) {
            newSplits[i] = m.asoi[i];
        }
        newSplits[issue] = true;

        if (votedYea.size() > 0) {
            DataMap yea = new DataMap(votedYea, newSplits);
            toReturn.add(0, yea);

        } else {
            toReturn.add(0, null);
        }

        if (votedNay.size() > 0) {
            DataMap nay = new DataMap(votedNay, newSplits);
            toReturn.add(1, nay);

        } else {
            toReturn.add(1, null);
        }

        if (nv.size() > 0) {
            DataMap noVote = new DataMap(nv, newSplits);
            toReturn.add(2, noVote);
        } else {
            toReturn.add(2, null);
        }

        return toReturn;

    }

    /*
    * splits a dataMap based on a specific issue
     */
    private ArrayList<DataMap> split(DataMap m, int issue) {
        if (m.alreadySplitOn(issue)) {//if a dataMap has been split on an issue, return
            return null;
        }

        ArrayList<DataMap> toReturn = new ArrayList<>();

        ArrayList<Datum> votedYea = new ArrayList<>();
        ArrayList<Datum> votedNay = new ArrayList<>();
        ArrayList<Datum> nv = new ArrayList<>();

        //this for loop goes through and adds to separate arrayLists based on how a Datum voted on that issue
        for (Datum d : m.data) {
            if (d.getVote(issue) == 1) {
                votedYea.add(d);
            } else if (d.getVote(issue) == -1) {
                votedNay.add(d);
            } else {
                nv.add(d);
            }
        }

        boolean[] newSplits = new boolean[m.asoi.length];
        for (int i = 0; i < newSplits.length; i++) {
            newSplits[i] = m.asoi[i];
        }
        newSplits[issue] = true;

        //the following if/else blocks add DataMaps with a modified boolean array to the arrayLists
        if (votedYea.size() > 0) {
            DataMap yea = new DataMap(votedYea, newSplits);
            toReturn.add(0, yea);

        } else {
            toReturn.add(0, null);
        }

        if (votedNay.size() > 0) {
            DataMap nay = new DataMap(votedNay, newSplits);
            toReturn.add(1, nay);

        } else {
            toReturn.add(1, null);
        }

        if (nv.size() > 0) {
            DataMap noVote = new DataMap(nv, newSplits);
            toReturn.add(2, noVote);
        } else {
            toReturn.add(2, null);
        }

        return toReturn;

    }

//THIS CODE TAKEN FROM STACK OVERFLOW. ROUNDS A DECIMAL TO THE SPECIFIED NUMBER OF PLACES
    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * finds the best issue on which to split
     *
     * @param m the DataMap that is being split
     * @return the issue to split on
     */
    private int findBestSplit(DataMap m) {
        int bestSplitSoFar = -1;
        double temp;
        double bestGainSoFar = 0;

        for (int i = 0; i < 10; i++) {
            if (m.asoi[i]) {
                continue;
            }
            ArrayList<DataMap> splits = split(m, i);
            temp = calcInfoGain(m, splits);

            if (temp > bestGainSoFar) {
                bestGainSoFar = temp;
                bestSplitSoFar = i;
            }
        }

        return bestSplitSoFar;
    }

    /*
     * used to convert from the int representation of an issue to the string representation
     */
    private String whichIssue(int i) {
        switch (i) {
            case 0:
                return "ISSUE A:";
            case 1:
                return "ISSUE B:";
            case 2:
                return "ISSUE C:";
            case 3:
                return "ISSUE D:";
            case 4:
                return "ISSUE E:";
            case 5:
                return "ISSUE F:";
            case 6:
                return "ISSUE G:";
            case 7:
                return "ISSUE H:";
            case 8:
                return "ISSUE I:";
            case 9:
                return "ISSUE J:";
            default:
                return "BAD VALUE";
        }
//        if (i == 0) {
//            return "ISSUE A:";
//        } else if (i == 1) {
//            return "ISSUE B:";
//        } else if (i == 2) {
//            return "ISSUE C:";
//        } else if (i == 3) {
//            return "ISSUE D:";
//        } else if (i == 4) {
//            return "ISSUE E:";
//        } else if (i == 5) {
//            return "ISSUE F:";
//        } else if (i == 6) {
//            return "ISSUE G:";
//        } else if (i == 7) {
//            return "ISSUE H:";
//        } else if (i == 8) {
//            return "ISSUE I:";
//        } else if (i == 9) {
//            return "ISSUE J:";
//        } else {
//            return "BADV";
//        }
    }

    /**
     * helper method. prints the tree
     */
    public void printTree() {
        printTree(root, 0, -2);
    }

    /**
     * recursively prints the tree
     */
    private void printTree(Node n, int spaces, int howNodeVoted) {
        if (n == null) {
            return;
        }
        if (n.isLeaf()) {
            for (int i = 0; i < spaces; i++) {
                System.out.print("  ");
            }
            if (howNodeVoted == 1) {
                System.out.print("+ ");
            } else if (howNodeVoted == 0) {
                System.out.print(". ");
            } else if (howNodeVoted == -1) {
                System.out.print("- ");
            } else {
                System.out.println("");
            }
            System.out.println(n.classification);
        } else {
            for (int i = 0; i < spaces; i++) {
                System.out.print("  ");
            }
            if (howNodeVoted == 1) {
                System.out.print("+ ");
            } else if (howNodeVoted == 0) {
                System.out.print(". ");
            } else if (howNodeVoted == -1) {
                System.out.print("- ");
            } else {
                System.out.println("");
            }
            System.out.println(whichIssue(n.issue));
            if (n.yea != null) {
                printTree(n.yea, spaces + 1, 1);
            }
            if (n.nay != null) {
                printTree(n.nay, spaces + 1, -1);
            }
            if (n.nv != null) {
                printTree(n.nv, spaces + 1, 0);
            }

        }
    }

    /**
     * classifies a Datum. Calls a private recursive method.
     *
     * @param d the datum to classify
     * @return the classification
     */
    public String classify(Datum d) {
        return classify(d, root);
    }

    private String classify(Datum d, Node n) {
        while (!n.isLeaf()) {
            char v = d.votes.charAt(n.issue);
            if (v == '+') {
                if (n.yea == null) {
                    return n.majIfSplitNode;
                } else {
                    return classify(d, n.yea);
                }
            }
            if (v == '-') {
                if (n.nay == null) {
                    return n.majIfSplitNode;
                } else {
                    return classify(d, n.nay);
                }
            }
            if (v == '.') {
                if (n.nv == null) {
                    return n.majIfSplitNode;
                } else {
                    return classify(d, n.nv);
                }
            }
        }
        return n.classification;
    }

    /*
     * This method was supposed to help tune the tree. Is not used.
     */
    private boolean changeIntoLeaf(Node n) {
        if (n.isLeaf() || n == null) {
            return false;
        } else {
            n = new Node(n.majIfSplitNode);
        }
        return true;
    }

    /**
     * separates a training set from a full set of data
     *
     * @param testSet the full set
     * @param index the offset used to determine which elements are used in the
     * tuning set versus the training set
     * @return
     */
    public static DataMap createTrainingSet(DataMap testSet, int index) {
        DataMap toReturn = new DataMap();
        int mod = index % 4;
        int count = 0;
        for (Datum d : testSet.data) {
            if (count % 4 != mod) {
                toReturn.add(d);
            }
            count++;
        }

        return toReturn;
    }

}
