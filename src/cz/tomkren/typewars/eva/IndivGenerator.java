package cz.tomkren.typewars.eva;

import java.util.List;

public interface IndivGenerator<Indiv> {
    List<Indiv> generate(int numIndivs);
}
