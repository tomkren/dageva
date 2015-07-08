package cz.tomkren.typewars.phalanx;

import cz.tomkren.helpers.*;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.SmartLib;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.Types;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.util.*;

/** Created by tom on 6.7.2015. */

public class Island {


    private final List<SkeletonCar> skeletonCars;

    private final Archipelago archipelago;
    private final Random rand;

    private final int maxGenerateTreeSize;
    private int nextSizeToTry;
    private boolean foundAnything;
    
    public List<SkeletonCar> getSkeletons() {return skeletonCars;}
    public int getMaxTreeSize() {return maxGenerateTreeSize;}

    private double bestFitVal;


    public Island(int maxGenerateTreeSize, Archipelago archipelago) {

        skeletonCars = new ArrayList<>();

        this.archipelago = archipelago;
        rand = archipelago.getRand();

        this.maxGenerateTreeSize = maxGenerateTreeSize;
        this.nextSizeToTry = 1;
        this.foundAnything = false;
        

        addNewSkeleton();
    }

    private Island(List<SkeletonCar> skeletonCars, Island parent) {
        this(skeletonCars, parent.archipelago, parent.rand, parent.maxGenerateTreeSize, 1);
    }

    private Island(List<SkeletonCar> skeletonCars, Archipelago archipelago, Random rand, int maxGenerateTreeSize, int nextSizeToTry) {
        this.skeletonCars = skeletonCars;
        this.archipelago = archipelago;
        this.rand = rand;
        this.maxGenerateTreeSize = maxGenerateTreeSize;
        this.nextSizeToTry = nextSizeToTry;
        this.foundAnything = skeletonCars.isEmpty();

        computeBestFitVal();
    }

    private void addNewSkeleton() {
        skeletonCars.add(new SkeletonCar(archipelago.getGoalType(), this));
    }

    public static Island merge(Island island1, Island island2) {

        List<SkeletonCar> cars = new ArrayList<>();
        cars.addAll(island1.getSkeletons());
        cars.addAll(island2.getSkeletons());

        return new Island(cars, island1);
    }

    public boolean isSplittable() {
        return skeletonCars.size() > 1 || (skeletonCars.size() == 1 && skeletonCars.get(0).isSplittable());
    }

    public AA<Island> split() {

        if (skeletonCars.size() == 0) {throw new Error("Un-split-able island");}

        List<SkeletonCar> cars1 = new ArrayList<>();
        List<SkeletonCar> cars2 = new ArrayList<>();

        if (skeletonCars.size() == 1) {
            SkeletonCar onlyCar = skeletonCars.get(0);

            // TODO ošetřit případ kdy už to nejde (tzn asi je tam komplet strom)
            AA<SkeletonCar> twoCars = onlyCar.split(rand);

            if (twoCars == null) {
                Log.it("un-split-able car "+onlyCar);
                return null;
            }

            cars1.add(twoCars._1());
            cars2.add(twoCars._2());


        } else {

            for (SkeletonCar car : skeletonCars) {
                List<SkeletonCar> cars = (rand.nextBoolean() ? cars1 : cars2);
                cars.add(car);
            }

            if (cars1.isEmpty()) {cars1.add(cars2.remove(cars2.size()-1));}
            if (cars2.isEmpty()) {cars2.add(cars1.remove(cars1.size()-1));}

        }


        Island island1 = new Island(cars1, this);
        Island island2 = new Island(cars2, this);

        return new AA<>(island1, island2);
    }

    private static final int LIMIT = 10000;

    public PolyTree generateOneWithParams() {
        
        PolyTree newTree = null;
        int iteration = 0;
        while (newTree == null) {
            iteration++;

            if (iteration > LIMIT) {
                return null;
            }

            if (nextSizeToTry > maxGenerateTreeSize) {
                if (foundAnything) {
                    nextSizeToTry = 1;
                } else {
                    performSelfDestruction(); // TODO ted muze nastat i když pro někerej island by to bylo možný vygenerovat
                    return null;
                }
            }

            SkeletonCar skeletonCar = F.randomElement(skeletonCars, rand);
                        
            newTree = generateOneWithParams(skeletonCar.toTree(), nextSizeToTry);
            nextSizeToTry++;

            if (newTree != null) {
                foundAnything = true;

                // TODO dodat nějakej test aby se nezacyklilo když se vyčerpá generující potenciál
                if (skeletonCar.getPopulation().contains(newTree)) {
                    newTree = null;
                } else {
                    skeletonCar.getPopulation().add(newTree);
                }
            }


        }
        
        return newTree;
    }


    
    private void performSelfDestruction() {
        Log.it("Island BUM !!! TODO !!!");
    }
    
    protected PolyTree generateOneWithParams(SkeletonTree skeletonTree, int treeSize) {
        return archipelago.getQuerySolver().generateOneWithParams(skeletonTree, archipelago.getGoalType(), treeSize);
    }

    protected void computeBestFitVal() {
        double best = -Integer.MAX_VALUE;
        for (SkeletonCar car : skeletonCars) {
            car.computeBestFitVal();
            double fitVal = car.getBestFitVal();
            if (fitVal > best) {
                best = fitVal;
            }
        }
        bestFitVal = best;
    }

    public double getBestFitVal() {
        return bestFitVal;
    }

    public SmartLib getLib() {return archipelago.getQuerySolver().getLib();}



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int i=0;
        for (SkeletonCar car : skeletonCars) {
            sb.append("  skeleton #").append(++i).append(" ").append(car).append("\n");
            sb.append(car.showPopulation(!archipelago.isSomethingWaitingToEvaluate()));
        }

        return sb.toString();

    }

    public static void main(String[] args) {
        Checker ch = new Checker();

        Type goalType = Types.parse("D => LD");
        SmartLib lib  = SmartLib.DATA_SCIENTIST_WITH_PARAMS_01;

        Archipelago archipelago = new Archipelago(goalType, 8, 2, 15, new QuerySolver(lib,ch.getRandom()));

        Island island = new Island(15, archipelago);

        for (int i = 0; i < 100; i++) {
            PolyTree tree = island.generateOneWithParams();
            Log.it( tree.getSize() +":\t"+ tree);
        }



        ch.results();
    }
}
