package cz.tomkren.typewars.dag;

import com.martinpilat.DagEvalInterface;
import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.CodeLib;
import cz.tomkren.typewars.TypedDag;

import java.util.List;

/** Created by tom on 18. 6. 2015. */

public class DagExample06 {

    public static void main(String[] args) {
        Checker ch = new Checker();

        String goalType = "D => LD";
        int numTrees = 20; //65536; //500;
        String[] libStrs = {
                "TypedDag.dia( TypedDag: D => D , TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
                "TypedDag.dia0( TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
                "TypedDag.split( TypedDag: D => (V D n) , MyList: V (D => LD) n ) : D => (V LD n)",

                "MyList.cons( Object: a , MyList: V a n ) : V a (S n)",
                "MyList.nil : V a 0",

                "PCA : D => D",
                "kBest : D => D",

                "kMeans : D => (V D (S(S n)))",
                "copy : D => (V D (S(S n)))",

                "SVC        : D => LD",
                "logR       : D => LD",
                "gaussianNB : D => LD",
                "DT         : D => LD",

                "vote : (V LD (S(S n))) => LD"
        };


        CodeLib codeLib1 = CodeLib.mk(libStrs);


        List<TypedDag> dags = mkDags(codeLib1, goalType, numTrees);
        String popJson = mkPopJson(dags);
        ch.it(popJson);

        F.writeFile("population_" + numTrees + ".json", popJson);


        try {

            DagEvalInterface evaluator = new DagEvalInterface("http://127.0.0.1:8080");

            String datasetFile = "winequality-white.csv"; // "wilt.csv" ;

            System.out.println(evaluator.getMethodParams(datasetFile));

            String dagStr   =  "[";
            dagStr          += "{\"input\" : [ [], \"input\", [\"1:0\"] ], \"1\" : [ [\"1:0\"], [\"SVC\", {\"C\": 8, \"gamma\": 0.0001}], [] ]},";
            dagStr          += "{\"input\" : [ [], \"input\", [\"1:0\"] ], \"1\" : [ [\"1:0\"], [\"lsdfogR\", {}], [] ]},"; //this should fail
            dagStr          += "{\"input\" : [ [], \"input\", [\"1:0\"] ], \"1\" : [ [\"1:0\"], \"DT\", [] ]},";
            dagStr          += "{\"input\" : [ [], \"input\", [\"1:0\"] ], \"1\" : [ [\"1:0\"], [\"gaussianNB\", {}], [] ]}";
            dagStr          += "]";

            Log.it(dagStr);

            /*String problemovej = "[{\n" +
                    "\t\"input\": [[], \"input\", [\"12:0\"]], \n" +
                    "\t\"12\": [[\"12:0\"], [\"copy\", {}], [\"13:0\", \"14:0\"]], \n" +
                    "\t\"13\": [[\"13:0\"], [\"gaussianNB\", {}], [\"15:0\"]], \n" +
                    "\t\"14\": [[\"14:0\"], [\"DT\", {}], [\"15:1\"]], \n" +
                    "\t\"15\": [[\"15:0\", \"15:1\"], [\"vote\", {}], []] \n" +
                    "}]";*/



            List<double[]> scores = evaluator.eval(popJson, datasetFile);

            for (double[] darr : scores) {
                if (darr.length == 0)
                    System.out.println("Evaluation error");
                else
                    System.out.println(darr[0]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ch.results();

        //int winnerId = 478 ; //17572;
        //KutilMain.showDag(dags.get(winnerId));

        //KutilMain.startLib(libStrs,goalType,numTrees);
    }

    public static String mkPopJson(List<TypedDag> dags) {
        return F.list(dags).reduce(TypedDag::toJson);
    }

    public static List<TypedDag> mkDags(CodeLib lib, String type, int n) {
        return F.list(lib.basicGenerate(type, n)).map(tree ->(TypedDag) tree.computeValue()).get();
    }

}
