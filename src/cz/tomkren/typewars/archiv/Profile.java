package cz.tomkren.typewars.archiv;


import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Listek;
import cz.tomkren.typewars.Sub;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.reusable.Query;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Deprecated
public class Profile {

    private BigInteger num;

    // private Map<?,PolyTree> trees;


    public Profile(Query dadQuery, List<Query> sonQueries) {
        num = howMany(dadQuery, sonQueries);
    }




    private static BigInteger howMany(Query dadQuery, Listek<Query> sonQueries, Sub sub, BigInteger num) {

        if (sonQueries == null) {return num;}

        QueryResult_old sonResult = null; // TODO hax aby šlo zkompilovat //dadQuery.query(new Query(sub, sonQueries.getHead()));

        BigInteger sum = BigInteger.ZERO;

        if (F.isZero(sonResult.getNum())) {return sum;}

        for (Map.Entry<Type,SubResult> e : sonResult.getSubResultMap().entrySet()) {

            SubResult subResult = e.getValue();
            Sub subResultSub = subResult.getSub();
            BigInteger subResultNum = subResult.getNum();

            Sub newSub = Sub.dot(subResultSub,sub);
            BigInteger newNum = subResultNum.multiply(num);

            sum = sum.add(howMany(dadQuery, sonQueries.getTail(), newSub, newNum));
        }

        return sum;
    }

    public static BigInteger howMany(Query dadQuery, List<Query> sonQueries) {
        return howMany(dadQuery, Listek.fromList(sonQueries), new Sub(), BigInteger.ONE);
    }


}
