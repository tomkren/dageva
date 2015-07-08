package cz.tomkren.typewars.dag;


import cz.tomkren.typewars.MyList;
import cz.tomkren.helpers.Comb0;
import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.*;
import cz.tomkren.typewars.basicgen.Generator;
import cz.tomkren.typewars.ProtoNode;


public class DagExample2 {

    public static CodeNode mkSplit2(String name, String outType, String... inTypes) {
        ProtoNode protoNode = new ProtoNode(name, outType, inTypes);
        Comb0 comb = Comb0.fromStatic(name, TypedDag.class, TypedDag.class, MyList.class);
        return new CodeNode(protoNode, comb);
    }




    public static void main(String[] args) {
        Checker ch = new Checker();


        CodeNode seri_old = TypedDag.mkCodeNode("seri", "x1 => x3", "x1 => x2", "x2 => x3");
        CodeNode para_old = TypedDag.mkCodeNode("para", "(P x1 x2) => (P x3 x4)", "x1 => x3", "x2 => x4");

        CodeNode MLP_old      = TypedDag.mkCodeNode("MLP", "D => LD");
        CodeNode twoMeans_old = TypedDag.mkCodeNode("2-means", "D => (P D D)");
        CodeNode twoU_old     = TypedDag.mkCodeNode("2-U", "(P LD LD) => LD");

        CodeNode PCA_old    = TypedDag.mkCodeNode("PCA", "D => D");
        CodeNode kMeans_old = TypedDag.mkCodeNode("k-means", "D => (V D (S(S x1)))");
        CodeNode U_old      = TypedDag.mkCodeNode("U", "(V LD (S(S x1))) => LD");


        CodeNode cons_old = new CodeNode(new ProtoNode("cons", "V x1 (S x2)", "x1", "V x1 x2"), Comb0.mkFun2(MyList.Cons::new));
        CodeNode dia_old = TypedDag.mkCodeNode("dia", "x1 => x2", "x1 => x1", "x1 => (V x2 x3)", "(V x2 x3) => x2");
        CodeNode split_1 = mkSplit2("split", "x1 => (V x2 x3)", "x1 => (V x1 x3)", "V (x1 => x2) x3");
        CodeNode split_2 = CodeNode.fromStatic("x1 => (V x2 x3)" /*::*/,  "TypedDag"/*.*/,"split"/*(*/, "TypedDag"/*:*/,"x1 => (V x1 x3)" , "MyList"/*:*/,"V (x1 => x2) x3"/*)*/ );
        CodeNode nil_old = new CodeNode(new ProtoNode("nil", "V x1 0"), fakeInputs -> MyList.NIL);


        CodeNode MLP      = CodeNode.mk("MLP : D => LD");
        CodeNode twoMeans = CodeNode.fromStatic0("D => (P D D)", null, "2-means");
        CodeNode twoU     = CodeNode.fromStatic0("(P LD LD) => LD", null, "2-U");

        CodeNode PCA    = CodeNode.fromStatic0("D => D", null, "PCA");
        CodeNode kMeans = CodeNode.fromStatic0("D => (V D (S(S x1)))", null, "k-means");
        CodeNode U      = CodeNode.fromStatic0("(V LD (S(S x1))) => LD", null, "U");


        CodeNode nil = CodeNode.fromStatic0("V x1 0", "MyList", "nil");


        CodeNode seri = CodeNode.mk("TypedDag.seri(TypedDag: x1 => x2, TypedDag: x2 => x3) : x1 => x3 ");
        CodeNode para = CodeNode.mk("TypedDag.para(TypedDag: x1 => x3, TypedDag: x2 => x4) : (P x1 x2) => (P x3 x4)");

        CodeNode cons  = CodeNode.mk("MyList.cons( Object: x1 , MyList: V x1 x2 ) : V x1 (S x2)");
        CodeNode dia   = CodeNode.mk("TypedDag.dia( TypedDag: x1 => x1 , TypedDag: x1 => (V x2 x3) , TypedDag: (V x2 x3) => x2 ) : x1 => x2");
        CodeNode split = CodeNode.mk("TypedDag.split( TypedDag: x1 => (V x1 x3) , MyList: V (x1 => x2) x3 ) : x1 => (V x2 x3)");


        CodeLib lib0 = new CodeLib(seri_old, para_old, MLP_old, twoMeans_old, twoU_old);

        CodeLib lib1 = new CodeLib(seri, para, MLP, twoMeans, twoU);
        CodeLib lib2 = new CodeLib(seri, para, MLP, kMeans, U, cons, nil);
        CodeLib lib3 = new CodeLib(dia, split, cons, nil, PCA, kMeans, MLP, U);

        CodeLib lib4 = new CodeLib(dia_old, split_1, cons_old, nil_old, PCA_old, kMeans_old, MLP_old, U_old);

        // TODO !!!!! pozor pak při křížení potřeba dodělat seri a para co pracuje s kopiema !!!!!


        Generator.printGenerateSequence("D => LD", lib1, 2, 2, 1, 2, 1, 1);// todo ,1 udělá chybu protože ešte nefunguje trasform do dagu

        Generator.generate("D => LD", lib1, 10).forEach(tree->{
            Log.itln("................................................");
            Log.itln(tree);
            Object value = tree.computeValue();
            Log.it(value);
        });



        /**/
        Generator.generate("D => LD", lib4, 10).forEach(tree->{
            Log.itln("********************************************************");
            Log.itln(tree);
            Log.itln(tree.showWithTypes());
            Object value = tree.computeValue();
            Log.it(value);
        });
        Generator.generate("D => LD", lib0, 2).forEach(tree->{
            Log.itln("--------------------------------------------------------");
            Log.itln(tree);
            Object value = tree.computeValue();
            Log.it(value);
        });


        Generator.printGenerateSequence("D => LD", lib3, 2,1,1,1,1,1,1,1,1,2,1); // TODO ,1

        Generator.generate("D => LD", lib3, 10).forEach(tree->{
            Log.itln("................................................");
            Log.itln(tree);
            Log.itln(tree.showWithTypes());
            Object value = tree.computeValue();
            Log.it(value);
        });


        ch.results();
    }


}
