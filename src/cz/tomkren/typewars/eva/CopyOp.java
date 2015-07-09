package cz.tomkren.typewars.eva;

import org.json.JSONObject;

import java.util.List;

public class CopyOp<Ind> implements Operator<Ind> {
    private double p;
    public CopyOp(double p) {this.p = p;}
    public CopyOp(JSONObject config) {this(config.getJSONObject("copyOp").getDouble("probability"));}
    @Override public int getNumInputs() {return 1;}
    @Override public List<Ind> operate(List<Ind> parents) {return parents;}
    @Override public double getProbability() {return p;}
}
