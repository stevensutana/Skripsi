package models;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Node8 on 1/3/16.
 */
public class NearbyResultCompare implements Comparator<ArrayList<Integer>> {

    @Override
    public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
        if(o1.get(2) > o2.get(2)){
            return +1;
        }else if(o1.get(2) < o2.get(2)){
            return -1;
        }else{
            return 0;
        }
    }
}