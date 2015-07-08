package cz.tomkren.typewars.archiv;

import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Listek;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.Sub;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.Types;
import cz.tomkren.typewars.reusable.Query;
import cz.tomkren.typewars.SmartLib;
import cz.tomkren.typewars.SmartSym;
import cz.tomkren.typewars.TMap;

import java.util.*;

/** Created by tom on 13. 6. 2015. */

public class Fun {

    private static boolean doLog = false;

    public static void setDoLog(boolean doLog) {
        Fun.doLog = doLog;
    }

    public static TMap<PolyTree> generateAll(SmartLib lib, String goalType, int treeSize) {
        return generateAll(lib, new Query(Types.parse(goalType),treeSize));
    }

    /*public static TMap<PolyTree> generateAll(SmartLib lib, Query query) {
        query.setLib(lib);
        return generateAll(query);
    }*/

    public static TMap<PolyTree> generateAll(SmartLib lib, Query query) {
        //return F.list(query.getLib().getSyms()).map(sym -> generateAll(query,sym)).reduce(TMap::merge);

        boolean hax_in = query.toString().equals("(D => (V LD x2)) ; 7") ;
        if (hax_in) {log("BOND HERE! "+query);}

        boolean hax_in2 = query.toString().equals("(V (D => LD) (S (S x0))) ; 5") ;
        if (hax_in2) {log("čmaňa HERE! "+query);}



        TMap<PolyTree> ret = new TMap<>();

        for (SmartSym sym : lib.getSyms()) {

            Type symOutType = sym.getOutputType();
            Type goalType   = query.getType();

            Sub sub = Sub.mgu(goalType, symOutType);

            if (!sub.isFail()) {

                if (hax_in) {log("BOND HERE2!" + sym.getName());}
                if (hax_in2) {log("čmaňa HERE2!" + sym.getName());}

                TMap<PolyTree> subRet = generateAll(lib, query, sym, sub);
                ret.add(subRet);

                if (hax_in) {log("BOND HERE3! ret.isEmpty() = " + ret.isEmpty());}
                if (hax_in2) {log("čmaňa HERE3! ret.isEmpty() = " + ret.isEmpty());}

            }
        }

        return ret;
    }

    public static TMap<PolyTree> generateAll(SmartLib lib, Query query, SmartSym sym, Sub sub) {

        /*boolean hax_inRoot = sym.getName().equals("dia") && query.getTreeSize()==10;
        if (hax_inRoot) {log("in dia with n=10 ...");}*/

        List<List<Integer>> allSimpleProfiles = possibleSimpleProfiles(query.getTreeSize(), sym.getArity());

        TMap<PolyTree> ret = new TMap<>();

        if (allSimpleProfiles.isEmpty()) {return ret;}

        List<Type> argTypes = sym.getArgTypes();

        for (List<Integer> simpleProfile : allSimpleProfiles) {

            //if (hax_inRoot && simpleProfile.toString().equals("[1, 7, 1]")) {log("Náš profil");}

            List<Query> sonQueries = F.zipWith(argTypes, simpleProfile, Query::new);

            TMap<PolyTree> subRet = generateAll(lib, query, sym, sonQueries, sub);
            ret.add(subRet);
        }

        /*if (hax_inRoot) {
            log("až dole ...");
            log( "ret.isEmpty() = " +ret.isEmpty() );
        }*/

        return ret;
    }

    public static TMap<PolyTree> generateAll(SmartLib lib, Query dadQuery, SmartSym sym, List<Query> sonQueries, Sub sub) {
        return generateAll(lib, dadQuery, sym, Listek.fromList(sonQueries), sub, Listek.mkSingleton(null) );
    }


    private static TMap<PolyTree> generateAll(SmartLib lib, Query dadQuery, SmartSym sym, Listek<Query> sonQueries, Sub sub, Listek<Listek<PolyTree>> acc) {



        boolean hax_inRoot  = isHax(1, "dia", 10, "[1, 7, 1]", sym, dadQuery, sonQueries);
        boolean hax_inRoot2 = isHax(2, "dia", 10, "[7, 1]"   , sym, dadQuery, sonQueries);
        boolean hax_inRoot3 = isHax(3, "split",7, "[1, 5]"   , sym, dadQuery, sonQueries);
        boolean hax_inRoot4 = isHax(4, "split",7, "[5]"      , sym, dadQuery, sonQueries);
        boolean hax_inRoot5 = isHax(5, "cons", 5, "[1, 3]"    , sym, dadQuery, sonQueries);


        if (sonQueries == null) {

            TMap<PolyTree> ret = new TMap<>();

            Type originalType = dadQuery.getType();

            for (Listek<PolyTree> sons : Listek.toList(acc)) {
                Type rootType = sub.apply(originalType);
                PolyTree newTree = sym.mkTree(rootType, Listek.toReverseList(sons) );
                ret.add(rootType, newTree);
            }

            return ret;
        }

        Query sonQuery = new Query(sub, sonQueries.getHead()); // TODO tady možná potřeba nějaká normalizace

        TMap<PolyTree> sonResult = generateAll(lib, sonQuery);

        if (hax_inRoot || hax_inRoot2 || hax_inRoot3 || hax_inRoot4 || hax_inRoot5) {
            log("sonQuery: "+sonQuery);
            //log("sonQuery.getType() = "+sonQuery.getType());
            log("sonResult.size() = "+sonResult.size());
        }

        TMap<PolyTree> ret = new TMap<>();

        if (sonResult.isEmpty()) {return ret;}

        for (Map.Entry<Type,List<PolyTree>> e : sonResult.entrySet()) {

            Type moreSpecificType = e.getKey();
            List<PolyTree> sonTrees = e.getValue();

            Sub sonSpecificSub = Sub.mgu( moreSpecificType, sonQuery.getType() );
            Sub newSub = Sub.dot(sonSpecificSub,sub);

            if (hax_inRoot || hax_inRoot2 || hax_inRoot3) {
                log("moreSpecificType = "+moreSpecificType);
                log("sub = "+ sub);
                log("newSub = "+ newSub);
            }

            // newAcc vznikne obohacenĂ­m acc o sonTrees
            Listek<Listek<PolyTree>> newAcc = null;
            for (Listek<PolyTree> preArgs : acc.toList()) {
                for (PolyTree sonTree : sonTrees) {
                    newAcc = Listek.mk( Listek.mk(sonTree,preArgs) , newAcc);
                }
            }

            TMap<PolyTree> subRet = generateAll(lib, dadQuery, sym, sonQueries.getTail(), newSub, newAcc);

            if (hax_inRoot || hax_inRoot2 || hax_inRoot3) {
                log("subRet.isEmpty() = "+ subRet.isEmpty());
                log(sonQueries.getTail());
            }

            ret.add(subRet);
        }

        return ret;
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

    public static List<List<Integer>> possibleSimpleProfiles(int fatherSize, int numArgs) {

        int size = fatherSize - 1;
        List<List<Integer>> ret = new ArrayList<>();

        if (size < numArgs) {
            return ret;
        }

        // todo tady sem to dal novÄ›
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



    private static void log(Object o) {
        if (doLog) {
            Log.it(o);
        }
    }

}
