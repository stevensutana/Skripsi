package models;

/**
 * Created by Node8 on 3/2/16.
 */
public class Alternatives {
    private double mw,wm,pt;


    public Alternatives(double mw,double wm,double pt) {
        this.mw = mw;
        this.wm = wm;
        this.pt = pt;
    }

    public double getPt() {
        return pt;
    }

    public void setPt(double pt) {
        this.pt = pt;
    }

    public double getWm() {
        return wm;
    }

    public void setWm(double wm) {
        this.wm = wm;
    }

    public double getMw() {
        return mw;
    }

    public void setMw(double mw) {
        this.mw = mw;
    }

    public String print(){
        return this.mw+" "+this.wm +" "+this.pt;
    }
}
