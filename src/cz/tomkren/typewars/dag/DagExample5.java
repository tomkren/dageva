package cz.tomkren.typewars.dag;

import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.*;
import cz.tomkren.typewars.archiv.Fun2;
import cz.tomkren.typewars.checkers.GeneratorChecker;

import java.util.List;

/** Created by tom on 18.6.2015. */

public class DagExample5 {

    static Checker ch = new Checker();

    static CodeLib codeLib = CodeLib.mk(
            "TypedDag.dia( TypedDag: a => a , TypedDag: a => (V b n) , TypedDag: (V b n) => b ) : a => b",
            "TypedDag.split( TypedDag: a => (V a n) , MyList: V (a => b) n ) : a => (V b n)",
            "MyList.cons( Object: a , MyList: V a n ) : V a (S n)",
            "MyList.nil : V a 0",
            "PCA : D => D",
            "k-means : D => (V D (S(S n)))",
            "MLP : D => LD",
            "U : (V LD (S(S n))) => LD"
    );

    static SmartLib lib = new SmartLib(codeLib);

    public static void main(String[] args) {

        SmartSym dia = lib.getSym(7);

        ch.it(dia);
        ch.it(dia.freshenTypeVars(100), "<(x100 => x101),[(x100 => x100), (x100 => (V x101 x102)), ((V x101 x102) => x101)],103>");

        checkQuery("D => LD", 1, 1);
        checkQuery("V a 0", 1, 1);
        checkQuery("V (D => LD) (S 0)", 3, 1);
        checkQuery("V (D => LD) (S (S 0))", 5, 1);
        checkQuery("D => (V LD (S (S 0)))", 7, 1);
        checkQuery("D => D", 1, 1);
        checkQuery("(V LD (S (S 0))) => LD", 1, 1);


        checkQuery("D => LD", 10, 1);
        checkQuery("D => LD", 12, 1);


        //Log.list( F.list(Fun2.generateAllUpTo_naive(lib, "D => LD", 20)).map(PolyTree::toString).get() );

        List<PolyTree> oldMethodTrees = codeLib.basicGenerate("D => LD", 100);
        List<PolyTree> newMethodTrees = Fun2.generateAllUpTo_naive(lib, "D => LD", 20); //25

        GeneratorChecker genCheck = new GeneratorChecker(oldMethodTrees, false);
        genCheck.check(newMethodTrees, true);

        //Log.list(genCheck.toNormalizedList());

        ch.results();
    }

    public static void checkQuery(String type, int n) {
        checkQuery(type, n, null);
    }

    public static void checkQuery(String type, int n, Integer expectedNumTrees) {
        TMap<PolyTree> tMap = Fun2.generateAll(lib, type, n);
        ch.it("("+type+" ; "+n+") ... tMap.size = "+tMap.size());
        ch.it(tMap);

        if (expectedNumTrees != null) {
            ch.it(tMap.size(), expectedNumTrees);
            Log.it();
        }
    }

}
