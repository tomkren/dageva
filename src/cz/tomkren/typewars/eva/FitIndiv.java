package cz.tomkren.typewars.eva;

import cz.tomkren.helpers.F;

import java.util.ArrayList;
import java.util.List;

public interface FitIndiv extends Probable {

    FitVal getFitVal();
    void setFitVal(FitVal fitVal);
    Object computeValue();




}
