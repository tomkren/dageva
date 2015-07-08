package cz.tomkren.typewars.archiv;


import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.util.Map;


// TODO promyslet jestli sloučit SizeNode a TypeNode do SignatureNode, a místo AB<Integer,Type> to zabalit do Signature

@Deprecated
public class SizeNode {

    private final QuerySolver parent;

    private final int n;
    private Map<String, TypeNode> types;
    private int num;

    public SizeNode(int n, QuerySolver parent) {
        this.n = n;
        this.parent = parent;


        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!

    }

    public TypeNode getTypeNode(Type type) {
        return types.get(type.toString());
    }


    public QuerySolver getTreeTree() {
        return parent;
    }

    public int getTreeSize() {
        return n;
    }
}
