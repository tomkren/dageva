package cz.tomkren.typewars.archiv;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class TypeNode {

    private final SizeNode parent;
    private final List<RootNode> roots;
    private BigInteger num;

    public BigInteger getNum() {return num;}

    public static TypeNode mk(SizeNode parent) {

        List<RootNode> roots = new ArrayList<>();

        // TODO
        /*for (SmartSym sym : parent.getTreeTree().getLib().getSyms()) {
            RootNode rn = RootNode.mk(sym, parent);
            if (rn != null) {roots.add(rn);}
        }*/

        if (roots.isEmpty()) {return null;}
        return new TypeNode(parent, roots);
    }

    private TypeNode(SizeNode parent, List<RootNode> roots) {
        this.parent = parent;
        this.roots  = roots;
    }




    //public TreeTree getSolver() {return parent.getSolver();}
    // public int getTreeSize() {return parent.getTreeSize();}

}
