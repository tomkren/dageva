package cz.tomkren.typewars.eva;

import java.util.List;

public interface Operator<Indiv> extends Probable {
    int getNumInputs();
    List<Indiv> operate(List<Indiv> parents);
}
