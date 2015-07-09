package cz.tomkren.typewars.eva;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Evolver<Indiv extends FitIndiv> implements PopulationSolver<Indiv> {

    private final Opts<Indiv> opts;

    public Evolver(Opts<Indiv> opts) {this.opts = opts;}

    public static class Opts<Ind extends FitIndiv> {
        private final IndivGenerator<Ind> generator;
        private final FitFun fitness;
        private final TogetherFitFun tFitness;
        private final EvoOpts evoOpts;
        private final Random rand;
        private final Distribution<Operator<Ind>> operators;
        private final Selection<Ind> selection;
        private final Logger<Ind> logger;
        private final Comparator<Ind> comparator;

        private Opts(FitFun fitness, TogetherFitFun tFitness, EvoOpts evoOpts, IndivGenerator<Ind> generator,
                    Distribution<Operator<Ind>> operators, Selection<Ind> selection, Logger<Ind> logger, Random rand, Comparator<Ind> comparator) {
            this.generator = generator;
            this.fitness = fitness;
            this.tFitness = tFitness;
            this.evoOpts = evoOpts;
            this.rand = rand;
            this.operators = operators;
            this.selection = selection;
            this.logger = logger;
            this.comparator = comparator;

            if (fitness == null && tFitness == null) {
                throw new Error("At least one of the fitness or together-fitness must be not-null!");
            }
        }

        public Opts(FitFun fitness, EvoOpts evoOpts, IndivGenerator<Ind> generator,
                    Distribution<Operator<Ind>> operators, Selection<Ind> selection, Logger<Ind> logger, Random rand) {
            this(fitness, null, evoOpts, generator, operators, selection, logger, rand, null);
        }

        public Opts(TogetherFitFun tFitness, Comparator<Ind> comparator, EvoOpts evoOpts, IndivGenerator<Ind> generator,
                    Distribution<Operator<Ind>> operators, Selection<Ind> selection, Logger<Ind> logger, Random rand) {
            this(null, tFitness, evoOpts, generator, operators, selection, logger, rand, comparator);
        }

        public boolean isFitnessTogether() {
            return tFitness != null;
        }

        public Evolver<Ind> mk() {
            return new Evolver<>(this);
        }
    }

    @Override
    public List<Indiv> generatePop() {
        return opts.generator.generate(getPopSize());
    }

    @Override
    public EvaledPop<Indiv> evalPop(List<Indiv> pop, int gen) {
        if (opts.isFitnessTogether()) {
            return new TogetherEvaledPop<>(pop, opts.tFitness, gen, opts.comparator, opts.logger);
        } else {
            return new BasicEvaledPop<>(pop, opts.fitness, gen);
        }
    }

    @Override public int     getNumRuns() {return opts.evoOpts.getNumRuns();}
    @Override public int     getNumGens() {return opts.evoOpts.getNumGens();}
    @Override public int     getPopSize() {return opts.evoOpts.getPopSize();}
    @Override public boolean saveBest()   {return opts.evoOpts.isSaveBest();}

    @Override public boolean isUniqueCheckPerformed() {return opts.evoOpts.isUniqueCheckPerformed();}
    @Override public int getMaxNumUniqueCheckFails()  {return opts.evoOpts.getMaxNumUniqueCheckFails();}

    @Override public Random getRandom() {return opts.rand;}
    @Override public Distribution<Operator<Indiv>> getOperators() {return opts.operators;}
    @Override public Selection<Indiv> getSelection() {return opts.selection;}

    @Override public Logger<Indiv> getLogger() {return opts.logger;}

}
