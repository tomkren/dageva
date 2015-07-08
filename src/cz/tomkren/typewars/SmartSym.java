package cz.tomkren.typewars;


import cz.tomkren.helpers.AB;
import cz.tomkren.helpers.ABC;

import java.util.ArrayList;
import java.util.List;

public class SmartSym {

    private final CodeNode codeNode;
    private List<List<SmartSym>> applicableSons; // TODO ......................

    public SmartSym(CodeNode codeNode) {
        this.codeNode = codeNode;
    }
    protected void initApplicableSons(List<SmartSym> allLibSymbols) {

        applicableSons = new ArrayList<>(getArgTypes().size());

        ABC<Type,List<Type>,Integer> freshResult = freshenTypeVars(0);

        //Type freshOutType = freshResult._1();
        List<Type> freshInTypes = freshResult._2();
        int nextVarId = freshResult._3();


        for (Type freshArgType : freshInTypes) {

            List<SmartSym> applicableSonsForThisArg = new ArrayList<>();

            for (SmartSym smartSym : allLibSymbols) {

                AB<Type,Integer> innerFreshResult = smartSym.getOutputType().freshenVars(nextVarId, new Sub());
                Type freshOutType = innerFreshResult._1();
                nextVarId = innerFreshResult._2();

                // TODO šlo by dělat fikanějc, např pro cons první arg se chytne všchno, ale nemá to pak podporu v druhym a vystupnim typu

                Sub maybeMgu = Sub.mgu(freshArgType, freshOutType);  // todo.. možná se vyplatí tuto substituci nezahodit, ale pak jí používat aby se nepočítala furt nanovo

                if (!maybeMgu.isFail()) {
                    applicableSonsForThisArg.add(smartSym);
                }

            }

            applicableSons.add(applicableSonsForThisArg);
        }

    }

    public CodeNode getCodeNode() {
        return codeNode;
    }

    public ABC<Type,List<Type>,Integer> freshenTypeVars(int startVarId) {

        List<Type> typeList = new ArrayList<>(1+getArity());
        typeList.add(getOutputType());
        typeList.addAll(getArgTypes());

        TypeTerm helperTerm = new TypeTerm(typeList);
        AB<Type,Integer> p = helperTerm.freshenVars(startVarId, new Sub());

        TypeTerm freshHelperTerm = (TypeTerm) p._1();
        int nextVarId = p._2();

        List<Type> freshTypeList = freshHelperTerm.getArgs();

        Type       freshOutType = freshTypeList.get(0);
        List<Type> freshInTypes = freshTypeList.subList(1,freshTypeList.size());

        return new ABC<>(freshOutType, freshInTypes, nextVarId);
    }


    public List<List<SmartSym>> getApplicableSons() {
        return applicableSons;
    }

    public PolyTree mkTree(Type rootType, List<PolyTree> sons) {
        return new PolyTree(codeNode, rootType, sons);
    }

    public String getName() {
        return codeNode.getName();
    }

    public Type getOutputType() {return codeNode.getOut();}

    public List<Type> getArgTypes() {
        return codeNode.getIns();
    }

    public int getArity() {
        return codeNode.getArity();
    }

    @Override
    public String toString() {
        return codeNode.toString();
    }
}
