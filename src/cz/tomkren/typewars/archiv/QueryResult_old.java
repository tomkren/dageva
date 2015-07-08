package cz.tomkren.typewars.archiv;

import cz.tomkren.helpers.TODO;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.reusable.Query;
import cz.tomkren.typewars.SmartSym;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by tom on 7. 6. 2015. */

@Deprecated
public class QueryResult_old {

    private final Map<Type,SubResult> result;
    private BigInteger num;


    public QueryResult_old(Query query) {


        //  pozor !!! pro jeden sym dostanu víc RootNodes

        result = new HashMap<>();

        for (SmartSym sym : query.getAllSyms()) { // TODO tady pak nahradit lokalně pro parentSym v query

            Map<Type,List<RootNode>> rootsMapForSym = mkRootNodesForSym(sym, query);

            for (Map.Entry<Type,List<RootNode>> e : rootsMapForSym.entrySet()) {

                Type rootType = e.getKey();
                List<RootNode> rootsForSym = e.getValue();

                SubResult subResult = result.get(rootType);

                if (subResult == null) {
                    subResult = new SubResult(query, rootType);
                    result.put(rootType, subResult);
                }

                subResult.addRoots(rootsForSym);

            }

            num = BigInteger.ZERO;

            // a ještě spočtem num
            for (Map.Entry<Type,SubResult> e : result.entrySet()) {
                num = num.add(e.getValue().getNum());
            }

        }
    }

    private Map<Type,List<RootNode>> mkRootNodesForSym(SmartSym sym, Query query) {
        Map<Type,List<RootNode>> rootsMap = new HashMap<>();



        // TODO zajistit aby nevracelo žádny prázdný rootNody v tom seznamu.....

        // TODO !!!

        throw new TODO();
    }


    public Map<Type, SubResult> getSubResultMap() {return result;}

    public BigInteger getNum() {return num;}


}
