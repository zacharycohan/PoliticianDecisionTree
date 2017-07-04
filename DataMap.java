
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This holds a large amount of Datums
 * 
 * @author Zachary Cohan
 */
public class DataMap {

    protected ArrayList<Datum> data;
    protected Double entropy = null;
    protected boolean[] asoi;
    
    /**
     * 
     * @param data the array of data backing the DataMap
     */
    public DataMap(ArrayList<Datum> data)
    {
        this.data = new ArrayList<>();
        for(Datum d : data){
            this.data.add(d);
        }
        
        asoi = new boolean[10];
    }
    
    /**
     * the default constructor. Creates an empty DataMap
     */
    public DataMap()
    {
        this.data = new ArrayList<>();
        asoi = new boolean[10];
    }
    
    /**
     * creates a dataMap from an array of data, the boolean array keeps track of whether or
     * not a particular issue was the cause of a split in the decision tree
     */    
    public DataMap(ArrayList<Datum> data,boolean[] votes)
    {
        this.data = new ArrayList<>();
        for(Datum d : data){
            this.data.add(d);
        }
        
        
        asoi = new boolean[10];
        for(int i = 0;i<asoi.length;i++)
        {
            asoi[i] = votes[i];
        }
    }
    
    /**
     * adds a datum to the DataMap
     * 
     * @param d the datum to add
     */
    public void add(Datum d)
    {
        data.add(d);
    }

    /**
     * prints the map
     */
    public void printMap()
    {
        System.out.print("size: "+data.size()+", This Map has been split on issue(s): ");
        for(int i = 0;i<asoi.length;i++){
            if(asoi[i])System.out.print(i+", ");
        }
        System.out.println();
        //for(Datum d : data)System.out.println(d);
        
    }
    
    /**
     * 
     * @return the number of elements in the dataMap
     */
    public int size()
    {
        return data.size();
    }

    /**
     * 
     * 
     * @param issue the issue being checked
     * @return whether the issue has been voted on or not
     */
    public boolean alreadySplitOn(int issue) {
    
        return asoi[issue];
    }
    
    /**
     * 
     * @return the majority classification of the contained Datum's
     */
    public String getMajClass()
    {
        int dem = 0;
        int rep = 0;
        for(Datum d : data)
        {
            if(d.classification.equals("D"))dem++;
            else rep++;            
        }
        if(rep > dem)return "R";
        else return "D";
        
    }
}
