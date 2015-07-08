package cz.tomkren.typewars;

import com.google.common.base.Joiner;
import cz.tomkren.helpers.*;
import cz.tomkren.typewars.eva.FitIndiv;
import cz.tomkren.typewars.eva.FitVal;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class PolyTree implements FitIndiv, Comparable<PolyTree> {


    @Override
    public int compareTo(PolyTree o) {
        return QuerySolver.compareTrees.compare(this, o);
    }

    private final CodeNode codeNode;
    private Type type;
    private final List<PolyTree> sons;

    private FitVal fitVal; // TODO | zbytečně i v podstromech, chtělo by udělat nějakej individual wrapper, kterej by měl PolyTree jako položku !!!!!!!!!!!
                           // TODO | Ideálně asi jako Individual<Genotype, Phenotype> ...

    public PolyTree(CodeNode codeNode, Type type, List<PolyTree> sons) {
        this.codeNode = codeNode;
        this.type = type;
        this.sons = sons;
        this.fitVal = null;
    }

    public PolyTree randomizeAllParams(Random rand) {
        CodeNode newCodeNode = codeNode instanceof CodeNodeWithParams ? ((CodeNodeWithParams)codeNode).randomizeAllParams(rand) : codeNode;
        return new PolyTree(newCodeNode, type, F.map(sons,s->s.randomizeAllParams(rand)));
    }

    public PolyTree randomlyShiftOneParam(Random rand, List<AB<Integer,Double>> shiftsWithProbabilities) {
        if (codeNode instanceof CodeNodeWithParams) {
            CodeNode newCodeNode = ((CodeNodeWithParams) codeNode).randomlyShiftOneParam(rand, shiftsWithProbabilities);
            return new PolyTree(newCodeNode, type, sons);
        } else {
            throw new Error("Method randomizeOneParam can be applied only to tree with CodeNodeWithParams as codeNode.");
        }
    }

    @Override
    public Object computeValue() {
        if (getCode() == null) {throw new Error("Null-code in computeValue().");}

        if (isTerminal()) {
            return getCode().compute(F.singleton(type));
        } else {
            return getCode().compute(F.map(sons, PolyTree::computeValue));
        }
    }

    public String getName() {return codeNode.getName();}

    public String getNameWithParams() {
        return codeNode.getNameWithParams();
    }

    public Type getType() {return type;}
    public List<PolyTree> getSons() {return sons;}
    public Comb0 getCode() {return codeNode.getCode();}
    public CodeNode getCodeNode() {return codeNode;}

    public boolean isTerminal() {
        return sons.isEmpty();
    }

    public List<SubtreePos> getAllSubtreePosesWhere(Predicate<PolyTree> isTrue) {
        List<SubtreePos> ret = new ArrayList<>();

        if (isTrue.test(this)) {
            ret.add(SubtreePos.root(type));
        }

        int sonIndex = 0;
        for (PolyTree son : sons) {
            List<SubtreePos> sonSubtreePoses = son.getAllSubtreePosesWhere(isTrue);
            for (SubtreePos subtreePosInSon : sonSubtreePoses) {
                ret.add(SubtreePos.reverseStep(sonIndex, subtreePosInSon));
            }
            sonIndex++;
        }

        return ret;
    }

    public List<SubtreePos> getAllSubtreePoses() {
        return getAllSubtreePosesWhere(t->true);
    }

    public TMap<SubtreePos> getAllSubtreePoses_byTypes() {
        return new TMap<>(getAllSubtreePoses(), SubtreePos::getType);
    }

    // TODO asi by bylo slušný udělat efektivnějc
    public SubtreePos getRandomSubtreePos(Random rand) {
        return F.randomElement(getAllSubtreePoses(), rand);
    }

    public PolyTree getSubtree(SubtreePos pos) {
        if (pos.isRoot()) {
            return this;
        } else {
            return sons.get(pos.getSonIndex()).getSubtree(pos.getTail());
        }
    }

    public PolyTree changeSubtree(SubtreePos pos, PolyTree newSubtree) {
        if (pos.isRoot()) {
            return newSubtree;
        } else {
            List<PolyTree> newSons = new ArrayList<>(sons.size());
            int sonIndex = pos.getSonIndex();
            int i = 0;
            for (PolyTree son : sons) {
                newSons.add( i == sonIndex ? son.changeSubtree(pos.getTail(),newSubtree) : son );
                i++;
            }
            return  new PolyTree(codeNode,type,newSons);
        }
    }

    public static AA<PolyTree> xover(PolyTree mum, PolyTree dad, SubtreePos mumPos, SubtreePos dadPos) {
        PolyTree daughter = mum.changeSubtree(mumPos, dad.getSubtree(dadPos));
        PolyTree son      = dad.changeSubtree(dadPos, mum.getSubtree(mumPos));
        return new AA<>(daughter,son);
    }

    // TODO otázka zda to stojí za porušení immutability, ale slouží to k do-upřesnění typů při reusable generování
    public void applySub(Sub sub) {
        type = sub.apply(type);
        sons.forEach(s->s.applySub(sub));
    }

    @Override
    public String toString() {
        return isTerminal() ? getNameWithParams() : "("+ getNameWithParams() +" "+ Joiner.on(' ').join( sons ) +")";
    }

    public String toStringWithoutParams() {
        return isTerminal() ? getName() : "("+ getName() +" "+ Joiner.on(' ').join( F.map(sons, PolyTree::toStringWithoutParams) ) +")";
    }

    private String showHead() {
        return "<"+getName()+":"+type+">";
    }

    public String showWithTypes() {
        return isTerminal() ? showHead() : "("+ showHead() +" "+ Joiner.on(' ').join( F.map(sons, PolyTree::showWithTypes) ) +")";
    }

    public int getSize() {
        int sum = 0;
        for (PolyTree son : sons) {sum += son.getSize();}
        return 1 + sum;
        // return 1 + F.list(sons).map(PolyTree::getSize).foldr(0,(x,y)->x+y);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public FitVal getFitVal() {
        return fitVal;
    }

    @Override
    public void setFitVal(FitVal fitVal) {
        this.fitVal = fitVal;
    }

    @Override
    public double getProbability() {
        if (fitVal == null) {throw new Error("fitVal must be not-null!");}
        return fitVal.getVal();
    }

    public static void main(String[] args) {
        Checker ch = new Checker();

        SmartLib lib = SmartLib.DATA_SCIENTIST_01;
        List<PolyTree> trees = new QuerySolver(lib, ch.getRandom()).simpleUniformGenerate("D => LD", 20, 1000);

        for (PolyTree tree : trees) {
            List<SubtreePos> allPoses = tree.getAllSubtreePoses();
            ch.eq(tree.getSize(), allPoses.size());
            SubtreePos randomPos = F.randomElement(allPoses,ch.getRandom());
            Log.it(randomPos);
            ch.eqStr(tree.toString(), tree.changeSubtree(randomPos, tree.getSubtree(randomPos)));
        }

        ch.results();
    }
}
