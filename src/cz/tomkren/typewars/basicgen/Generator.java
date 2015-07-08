package cz.tomkren.typewars.basicgen;


import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.NodeLib;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.Types;
import cz.tomkren.typewars.dag.Dag;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Generator {

    public static List<PolyTree> generate(Type goal, NodeLib lib, int n) {
        List<PolyTree> results = new ArrayList<>(n);

        PriorityQueue<Car> queue = new PriorityQueue<>(new Car.MinTreeSizeComparator());
        queue.add(new Car(goal));

        int i = 0;
        while (!queue.isEmpty() && results.size() < n) {
            Car smallest = queue.poll();
            List<Car> successors = smallest.step(lib);

            if (successors == null) {
                results.add(smallest.mkPolyTree(lib));
                Log.it(++i);
            } else {
                successors.forEach(queue::offer);
            }

            //Log.it("q size: "+queue.size());
        }

        return results;
    }

    public static void printGenerateSequence(Type goal, NodeLib lib, Integer... seq) {

        Car car = new Car(goal);
        Log.it(car);

        for (Integer i : seq) {
            car = car.step(lib).get(i-1);
            Log.it(car);
        }

        Log.it("~~~~~~~~~~~~~~~~~~\n");

        List<Car> cars =car.step(lib);
        if(cars == null) {
            Log.it("FINISHED, result tree:\n");
            PolyTree tree = car.mkPolyTree(lib);
            Log.it( tree+"\n" );

            Dag dag = Dag.fromTree(tree);
            Log.it(dag);

        } else {
            cars.forEach(Log::it);
        }
    }

    public static List<PolyTree> generate(String goal, NodeLib lib, int n) {
        return generate(Types.parse(goal), lib, n);
    }


    public static void printGenerateSequence(String goal, NodeLib lib, Integer... seq) {
        printGenerateSequence(Types.parse(goal), lib, seq);
    }
}
