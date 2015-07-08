package cz.tomkren.typewars.dag;

import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.*;
import cz.tomkren.typewars.basicgen.Generator;
import cz.tomkren.typewars.ProtoNode;

import java.util.List;
import java.util.function.Consumer;

public class DagExample {

    public static void main(String[] args) {
        Checker ch = new Checker();

        ProtoNode seri = new ProtoNode("SERI", "x1 -> x3", "x1 -> x2", "x2 -> x3");

        ProtoNode diamant = new ProtoNode("DIA",   "x1 -> x2",         "x1 -> x1", "x1 -> (List x2)", "(List x2) -> x2");
        ProtoNode split   = new ProtoNode("SPLIT", "x1 -> (List x2)" , "x1 -> (NList x1 x3)" , "NList (x1 -> x2) x3" );
        ProtoNode cons    = new ProtoNode(":",     "NList x1 (S x2)" , "x1" , "NList x1 x2" );
        ProtoNode nil     = new ProtoNode("[]",    "NList x1 0");

        ProtoNode PCA     = new ProtoNode("PCA",     "D -> D");
        ProtoNode kmeans  = new ProtoNode("k-means", "D -> (NList D (S(S x1)))");
        ProtoNode MLP     = new ProtoNode("MLP",     "D -> LD");
        ProtoNode RBF     = new ProtoNode("RBF",     "D -> LD");
        ProtoNode CNN     = new ProtoNode("CNN",     "D -> LD");
        ProtoNode U       = new ProtoNode("U",       "(List LD) -> LD");

        NodeLib lib  = new NodeLib(diamant, split, cons, nil, PCA, kmeans, MLP, RBF, CNN, U);
        NodeLib lib2 = new NodeLib(diamant, split, cons, nil, PCA, kmeans, MLP, U);

        NodeLib lib3 = new NodeLib(seri, split, cons, nil, PCA, kmeans, MLP, U);

        Generator.printGenerateSequence("D -> LD", lib, 4, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 1);

        // TODO
        List<PolyTree> ts = Generator.generate("D -> LD", lib3, 1000);

        Consumer<PolyTree> printTree = tree -> {
            Log.it("................................................\n");
            Log.it(tree + "\n");
            Log.it(Dag.fromTree(tree));
        };

        ts.forEach(printTree);

        // Tohle vypadá jako špatně převedený na dag, ale je to jen zdání, protože tam jsou dvě U a to co ukazje na [] je to koncový,
        // ke kterýmu se dá dostat už na 4. vrstvě. Ale je to poučení proto, že pro hezkou vizualizaci je potřeba spočíst "maximální vrstvu"..
        printTree.accept( ts.get(6) );


        ch.results();
    }
}
