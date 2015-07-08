package cz.tomkren.typewars.checkers;

import cz.tomkren.helpers.F;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.reusable.Query;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Created by tom on 25.6.2015. */

public class UniformityChecker {
    private QuerySolver solver;
    private int nDefault;

    private Map<String, Integer> mapa;
    private Set<String> set;

    private StringBuilder log;

    private Query lastQuery;

    public UniformityChecker(QuerySolver solver, int nDefault) {
        this.solver = solver;
        this.nDefault = nDefault;

        log = new StringBuilder();
        log.append("UniformityChecker:\n");

        reset();
    }

    private void reset() {
        mapa = new HashMap<>();
        set = new TreeSet<>();
    }

    public void test(String goalType, int treeSize) {
        test(goalType, treeSize, nDefault);
    }

    public void test(String goalType, int treeSize, int n) {
        lastQuery = new Query(goalType, treeSize);
        log.append("< ").append(lastQuery).append(" > \t (n=").append(n).append(") \t");
        for (int i = 0; i < n; i++) {
            PolyTree tree = solver.generateOne(goalType, treeSize);
            if (tree == null) {
                log.append("  [EMPTY]\n");
                return;
            }
            add(tree);
        }
        logTest();
        reset();
    }

    private void add(PolyTree tree) {
        String treeCode = tree.toString();
        mapa.merge(treeCode, 1, (x, y) -> x + y);
        set.add(treeCode);
    }

    public int size() {
        return F.list(mapa.values()).foldr(0, F::plus);
    }

    private void logTest() {
        int size = size();
        log.append(" numFoundTrees = ").append(set.size()).append("/").append(solver.query(lastQuery).getNum()).append("\n");
        for (String treeCode : set) {
            int num = mapa.get(treeCode);
            double percent = (100.0 * num) / size;
            log.append("  ").append(treeCode).append(" : ").append(num).append("/").append(size).append(" ").append(percent).append("% \n");
        }
    }

    @Override
    public String toString() {
        return log.toString();
    }
}
