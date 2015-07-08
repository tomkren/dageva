package cz.tomkren.typewars.archiv;

import cz.tomkren.helpers.*;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.Sub;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.Types;
import cz.tomkren.typewars.reusable.Query;
import cz.tomkren.typewars.SmartLib;
import cz.tomkren.typewars.SmartSym;
import cz.tomkren.typewars.TMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Created by tom on 18. 6. 2015. */

public class Fun2 {

    public static int nextVarId = 0;

    public static List<PolyTree> generateAllUpTo_naive(SmartLib lib, String goalType, int treeSize) {
        List<PolyTree> ret = new ArrayList<>();
        for (int i = 1; i <= treeSize; i++) {
            ret.addAll( generateAll(lib, goalType, i).flatten() );
            Log.it("Generating size "+i);
        }
        return ret;
    }

    public static TMap<PolyTree> generateAll(SmartLib lib, String goalType, int treeSize) {
        return generateAll(lib, new Query(Types.parse(goalType),treeSize));
    }

    public static TMap<PolyTree> generateAll(SmartLib lib, Query query) {

        TMap<PolyTree> ret = new TMap<>();

        for (SmartSym sym : lib.getSyms()) {

            ABC<Type,List<Type>,Integer> freshResult = sym.freshenTypeVars(nextVarId);
                        Type symOutType        = freshResult._1();
            List<Type> symArgTypes = freshResult._2();
            nextVarId              = freshResult._3();

            Type goalType = query.getType();

            Sub sub = Sub.mgu(goalType, symOutType);

            if (!sub.isFail()) {

                List<List<Integer>> allSimpleProfiles = possibleSimpleProfiles(query.getTreeSize(), sym.getArity());

                TMap<PolyTree> subRet = new TMap<>();
                for (List<Integer> simpleProfile : allSimpleProfiles) {
                    Listek<Query> sonQueries = Listek.fromList( F.zipWith(symArgTypes, simpleProfile, Query::new) );
                    Listek<Listek<PolyTree>> acc = Listek.mkSingleton(null);

                    TMap<PolyTree> subSubRet = generateAll(lib, query, sym, sonQueries, sub, acc);
                    subRet.add(subSubRet);
                }
                ret.add(subRet);
            }
        }

        return ret;
    }

    private static TMap<PolyTree> generateAll(SmartLib lib, Query dadQuery, SmartSym sym, Listek<Query> sonQueries, Sub sub, Listek<Listek<PolyTree>> acc) {

        if (sonQueries == null) {

            TMap<PolyTree> ret = new TMap<>();

            Type originalType = dadQuery.getType();
            Type rootType = sub.apply(originalType);

            for (Listek<PolyTree> sons : Listek.toList(acc)) {
                PolyTree newTree = sym.mkTree(rootType, Listek.toReverseList(sons) );
                ret.add(rootType, newTree);
            }

            return ret;
        }

        Query sonQuery = new Query(sub, sonQueries.getHead());

        TMap<PolyTree> sonResult = generateAll(lib, sonQuery);

        TMap<PolyTree> ret = new TMap<>();

        if (sonResult.isEmpty()) {return ret;}

        for (Map.Entry<Type,List<PolyTree>> e : sonResult.entrySet()) {

            Type moreSpecificType = e.getKey();
            List<PolyTree> sonTrees = e.getValue();

            Sub sonSpecificSub = Sub.mgu( moreSpecificType, sonQuery.getType() );
            Sub newSub = Sub.dot(sonSpecificSub,sub);

            // newAcc vznikne obohacením acc o sonTrees
            Listek<Listek<PolyTree>> newAcc = null;
            for (Listek<PolyTree> preArgs : acc.toList()) {
                for (PolyTree sonTree : sonTrees) {
                    newAcc = Listek.mk( Listek.mk(sonTree,preArgs) , newAcc);
                }
            }

            TMap<PolyTree> subRet = generateAll(lib, dadQuery, sym, sonQueries.getTail(), newSub, newAcc);

            ret.add(subRet);
        }

        return ret;
    }




    public static List<List<Integer>> possibleSimpleProfiles(int fatherSize, int numArgs) {

        int size = fatherSize - 1;
        List<List<Integer>> ret = new ArrayList<>();

        if (size < numArgs) {
            return ret;
        }

        // todo tady sem to dal nově
        if (numArgs == 0) {
            if (size == 0) {
                ret.add(Collections.emptyList());
            }
            return ret;
        }

        if (numArgs == 1) {
            ret.add(F.singleton(size));
        } else {

            int n = size - (numArgs - 1);
            for (int i = 1; i <= n; i++) {

                List<List<Integer>> subResults = possibleSimpleProfiles(size - i + 1, numArgs - 1);

                for (List<Integer> subResult : subResults) {
                    List<Integer> newResult = new ArrayList<>();
                    newResult.add(i);
                    newResult.addAll(subResult);
                    ret.add(newResult);
                }
            }

        }

        return ret;
    }


    private static boolean doLog = false;
    public static void setDoLog(boolean doLog) {
        Fun2.doLog = doLog;
    }

    private static void log(Object o) {
        if (doLog) {
            Log.it(o);
        }
    }

    private static boolean isHax(int ods, String name, int treeSize, String profileStr, SmartSym sym, Query dadQuery, Listek<Query> sonQueries) {
        if (sym.getName().equals(name) && dadQuery.getTreeSize()==treeSize) {
            List<Integer> hax_simpleProfile = F.list(Listek.toList(sonQueries)).map(Query::getTreeSize).get();
            if (hax_simpleProfile.toString().equals(profileStr)) {
                log( F.fillStr(ods,"! ")+sonQueries);
                log( F.fillStr(ods,"! ")+hax_simpleProfile);
                return true;
            }
        }
        return false;
    }

}
