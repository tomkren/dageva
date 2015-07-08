package cz.tomkren.typewars.eva;

import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.Types;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.util.List;
import java.util.function.BiFunction;

/** Created by tom on 30. 6. 2015. */

public class SimplePolyTreeGenerator implements IndivGenerator<PolyTree> {

    private final Type goalType;
    private final int maxTreeSize;
    private final QuerySolver querySolver;

    public SimplePolyTreeGenerator(Type goalType, int maxTreeSize, QuerySolver querySolver) {
        this.goalType = goalType;
        this.maxTreeSize = maxTreeSize;
        this.querySolver = querySolver;
    }

    public SimplePolyTreeGenerator(String goalType, int maxTreeSize, QuerySolver querySolver) {
        this(Types.parse(goalType), maxTreeSize, querySolver);
    }

    @Override
    public List<PolyTree> generate(int numIndivs) {
        return querySolver.simpleUniformGenerate(goalType, maxTreeSize, numIndivs);
    }
}
