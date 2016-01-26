package models;

import java.util.*;

/**
 * Created by Node8 on 1/12/15.
 */
public class Constants {

    private HashMap<String,ProtoRegion> hm = new HashMap<String,ProtoRegion>();


    private ProtoRegion[] pr = new ProtoRegion[4];

    public Constants(){

        hm.put("bandung",new ProtoRegion(-6.91474,107.60981,17000,12,", *(bandung|bdg)$","Bandung"));
        hm.put("jakarta",new ProtoRegion(-6.21154,106.84517,15000,11,", *(jakarta|jkt)$","Jakarta"));
        hm.put("surabaya",new ProtoRegion(-7.27421,112.71908,15000,12,", *(surabaya|sby)$","Surabaya"));
        hm.put("malang",new ProtoRegion(-7.9812985,112.6319264,15000,13,", *(malang|mlg)$","Malang"));
    }

    public double getLat(String key){
        ProtoRegion tmp = hm.get(key);
        return tmp.getLat();
    }
}
