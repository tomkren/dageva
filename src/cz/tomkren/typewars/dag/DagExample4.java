package cz.tomkren.typewars.dag;

import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.F;
import cz.tomkren.kutil2.KutilMain;
import cz.tomkren.typewars.CodeLib;
import cz.tomkren.typewars.TypedDag;

import java.util.List;

/** Created by tom on 18. 6. 2015. */

public class DagExample4 {

    public static void main(String[] args) {
        Checker ch = new Checker();

        String goalType = "D => LD";
        int numTrees = 500;
        String[] libStrs = {
                "TypedDag.dia( TypedDag: a => a , TypedDag: a => (V b n) , TypedDag: (V b n) => b ) : a => b",
                "TypedDag.split( TypedDag: a => (V a n) , MyList: V (a => b) n ) : a => (V b n)",
                "MyList.cons( Object: a , MyList: V a n ) : V a (S n)",
                "MyList.nil : V a 0",

                "PCA : D => D", // PCA, kBest
                //"kBest : D => D",
                "kMeans : D => (V D (S(S n)))", // kMeans, copy
                "SVC : D => LD",
                //"logR       : D => LD",
                //"gaussianNB : D => LD",
                "DT : D => LD",
                "union : (V LD (S(S n))) => LD" // union, vote

                //"pre : D => D", // PCA, kBest
                //"spliter : D => (V D (S(S n)))", // kMeans, copy
                //"model : D => LD", // SVC, logR, gaussianNB, DT
                //"merger : (V LD (S(S n))) => LD" // union, vote

                //"PCA   : (D a) => (D a)",
                //"kBest : (D a) => (D a)",
                //"kMeans : (D a) => (V (D Dis) (S(S n)))",
                //"copy   : (D a) => (V (D Con) (S(S n)))",
                //"SVC        : (D a) => (LD a)",
                //"logR       : (D a) => (LD a)",
                //"gaussianNB : (D a) => (LD a)",
                //"DT         : (D a) => (LD a)",
                //"union : (V (LD Dis) (S(S n))) => (LD Dis)",
                //"vote  : (V (LD Con) (S(S n))) => (LD Dis)"
        };

        String[] libStrs2 = {
                "TypedDag.dia( TypedDag: a => a , TypedDag: a => (V b n) , TypedDag: (V b n) => b ) : a => b",
                "TypedDag.split( TypedDag: a => (V a n) , MyList: V (a => b) n ) : a => (V b n)",
                "MyList.cons( Object: a , MyList: V a n ) : V a (S n)",
                "MyList.nil : V a 0",
                "kBest : D => D",
                "copy : D => (V D (S(S n)))",
                "logR       : D => LD",
                "gaussianNB : D => LD",
                "vote : (V LD (S(S n))) => LD"
        };

        CodeLib codeLib1 = CodeLib.mk(libStrs);
        CodeLib codeLib2 = CodeLib.mk(libStrs2);

        String popJson = mkPopJson(codeLib1, goalType, numTrees);
        String popJson2 = mkPopJson(codeLib2, goalType, numTrees);
        ch.it(popJson);
        ch.it(popJson2);

        F.writeFile("population.json", popJson);
        F.writeFile("population2.json", popJson2);

        ch.results();

        List<TypedDag> dags = mkDags(codeLib1, goalType, numTrees);

        KutilMain.showDag(dags.get(125));

        //KutilMain.startLib(libStrs, goalType, numTrees);
    }

    public static List<TypedDag> mkDags(CodeLib lib, String type, int n) {
        return F.list(lib.basicGenerate(type, n)).map(tree ->(TypedDag) tree.computeValue()).get();
    }

    public static String mkPopJson(CodeLib lib, String type, int n) {
        return F.list(lib.basicGenerate(type, n)).map(t->(TypedDag)t.computeValue()).reduce(TypedDag::toJson);
    }

}
