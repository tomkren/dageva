package cz.tomkren.typewars.eva;

import java.util.Random;

/** Created by tom on 2.7.2015.*/

public interface Selection<T extends Probable> {

    T select(Distribution<T> dist);


    class Roulette<T extends Probable> implements Selection<T> {

        private final Random rand;
        public Roulette(Random rand) {this.rand = rand;}

        @Override
        public T select(Distribution<T> dist) {
            return dist.get(rand);
        }
    }

    class Tournament<T extends Probable> implements Selection<T> {

        private final double pReturnWinner;
        private final Random rand;

        public Tournament(double pReturnWinner, Random rand) {
            this.pReturnWinner = pReturnWinner;
            this.rand = rand;
        }

        @Override
        public T select(Distribution<T> dist) {
            return dist.tournamentGet(pReturnWinner, rand);
        }
    }

}
