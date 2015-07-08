package cz.tomkren.typewars.eva;

/** Created by tom on 6.7.2015. */

// TODO něco takovýho udělat aby nemusel bejt fitval ve stromě... zatím jen naznačeno

public class IndivWrapper<Genotype,Phenotype> {

    private final Genotype genotype;
    private Phenotype phenotype;
    private FitVal fitVal;

    public IndivWrapper(Genotype genotype, Phenotype phenotype) {
        this.genotype = genotype;
        this.phenotype = phenotype;

    }



}
