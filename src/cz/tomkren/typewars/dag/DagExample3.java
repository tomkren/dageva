package cz.tomkren.typewars.dag;

import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.CodeLib;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.TypedDag;
import cz.tomkren.typewars.archiv.Fun;
import cz.tomkren.typewars.SmartLib;
import cz.tomkren.typewars.TMap;

public class DagExample3 {

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

        codeLib.basicGenerate("D => LD", 10).forEach(tree -> {
            ch.itln("...");
            ch.itln(tree);
            ch.itln(tree.showWithTypes());

            TypedDag dag = (TypedDag) tree.computeValue();

            ch.itln(dag.toString());
            ch.itln(dag.toJson());
        });

        ch.it("\n");

        codeLib.basicGenerate("D => LD", 10).forEach(ch::it);
        Log.it();


        int num = 9;
        for (int n = 1; n <= num; n++) {
            checkQuery(n);
        }

        checkQuery(10); // TODO : tady ��k� chyba, m�lo by d�t 1 strom !!!!!!!!!!!

        Log.it("\n----------------------------------------\n");

        checkQuery("D => LD", 1); // OK
        checkQuery("V a 0", 1);   // OK
        checkQuery("V (D => LD) (S 0)", 3); // OK
        checkQuery("V (D => LD) (S (S 0))", 5); // OK
        checkQuery("D => (V LD (S (S 0)))", 7); // OK

        checkQuery("D => D", 1); // OK
        checkQuery("(V LD (S (S 0))) => LD", 1); // OK


        Fun.setDoLog(true);

        checkQuery("D => LD", 10); // todo tadfy se to teda rozbije

        // TODO tady to dela shit : sonQuery.getType() = (D => (V LD x2)) ; sonResult.size() = 0

        // TODO !!!!!!!! Skoro jist� je chyba v neofre�ov�v�n� typovejch prom�nnejch

        ch.results();
    }

    public static void checkQuery(String type, int n) {
        TMap<PolyTree> tMap = Fun.generateAll(lib, type, n);
        ch.it("("+type+" ; "+n+") ... tMap.size = "+tMap.size());
        ch.it(tMap);
    }

    public static void checkQuery(int n) {
        checkQuery("D => LD", n);
    }



}
