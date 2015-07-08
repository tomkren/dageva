package cz.tomkren.typewars.checkers;

import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.TMap;

import java.util.*;
import java.util.stream.Collectors;

/** Created by tom on 19.6.2015. */

public class GeneratorChecker {

    private Map<Integer,Set<String>> checkMap;
    private int maxSizeSoFar;
    private boolean isWholeSizes;

    public GeneratorChecker(List<PolyTree> trees, boolean isWholeSizes) {
        checkMap = new HashMap<>();
        maxSizeSoFar = 0;
        this.isWholeSizes = isWholeSizes;

        trees.forEach(this::add);
    }

    public void add(PolyTree tree) {
        int size = tree.getSize();

        if (size > maxSizeSoFar) {maxSizeSoFar = size;}

        Set<String> set = checkMap.get(size);

        if (set == null) {
            set = new TreeSet<>();
            checkMap.put(size, set);
        }

        set.add(tree.toString());
    }

    public List<Integer> getNumsForSizes() {
        List<Integer> ret = new ArrayList<>();
        for (int i = 1; i < (isWholeSizes ? maxSizeSoFar+1 : maxSizeSoFar); i++) {
            Set<String> set = checkMap.get(i);
            ret.add( set == null ? 0 : set.size() );
        }
        return ret;
    }

    public List<String> toNormalizedList() {
        List<String> ret = new ArrayList<>();

        // maxSizeSoFar nebereme, protože může být nekompletní:
        for (int i = 1; i < (isWholeSizes ? maxSizeSoFar+1 : maxSizeSoFar); i++) {
            Set<String> set = checkMap.get(i);
            if (set != null) {
                ret.addAll(F.list(set).get());
            }
        }

        return ret;
    }

    public boolean check(TMap<PolyTree> treesMap, boolean isWholeSizes) {
        return check(new GeneratorChecker(treesMap.flatten(), isWholeSizes));
    }

    public boolean check(List<PolyTree> trees, boolean isWholeSizes) {
        return check(new GeneratorChecker(trees, isWholeSizes));
    }

    public boolean check(GeneratorChecker tempChecker) {

        boolean isOk = true;

        List<String> shouldBeList = toNormalizedList();
        List<String> resultList   = tempChecker.toNormalizedList();

        /*if (shouldBeList.size() != resultList.size()) {
            Log.it("GeneratorChecker: Sizes do not match: " + shouldBeList.size() +" != "+ resultList.size());
            return false;
        }*/

        int minLen = Math.min(shouldBeList.size() , resultList.size());

        Log.it("Num trees to be checked: " + minLen +" = min("+shouldBeList.size() +","+ resultList.size()+")" );

        for (int i = 0; i < minLen; i++) {
            if (!shouldBeList.get(i).equals(resultList.get(i))) {
                isOk = false;
                Log.it("  GeneratorChecker: Trees do not match:\n    " + shouldBeList.get(i) +"\n    "+ resultList.get(i));
            }
        }

        Log.it( "RESULT : " +(isOk ? "OK" : "KO") );


        return isOk;
    }




}
