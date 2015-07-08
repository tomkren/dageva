package cz.tomkren.typewars.eva;

import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.Types;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.util.List;

/** Created by tom on 6.7.2015. */

public class RandomParamsPolyTreeGenerator implements IndivGenerator<PolyTree> {

    private final Type goalType;
    private final int maxTreeSize;
    private final QuerySolver querySolver;

    public RandomParamsPolyTreeGenerator(Type goalType, int maxTreeSize, QuerySolver querySolver) {
        this.goalType = goalType;
        this.maxTreeSize = maxTreeSize;
        this.querySolver = querySolver;
    }

    public RandomParamsPolyTreeGenerator(String goalType, int maxTreeSize, QuerySolver querySolver) {
        this(Types.parse(goalType), maxTreeSize, querySolver);
    }

    @Override
    public List<PolyTree> generate(int numIndivs) {
        return querySolver.uniformGenerateWithRandomizedParams(goalType, maxTreeSize, numIndivs);
    }
}
