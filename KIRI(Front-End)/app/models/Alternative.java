package models;

/**
 * Created by Node8 on 3/2/16.
 */
public class Alternative {
    private double mw,wm,pt;


    public Alternative(double mw, double wm, double pt) {
        this.mw = mw;
        this.wm = wm;
        this.pt = pt;
    }

    public double getPt() {
        return pt;
    }


    public double getWm() {
        return wm;
    }


    public double getMw() {
        return mw;
    }

    public String print(){
        return this.mw+" "+this.wm +" "+this.pt;
    }
}
