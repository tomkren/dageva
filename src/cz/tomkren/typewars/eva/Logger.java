package cz.tomkren.typewars.eva;

public interface Logger<Indiv extends Probable> {
    void logPop(int run, int generation, EvaledPop<Indiv> pop);
    default void logRun(int run) {}
}