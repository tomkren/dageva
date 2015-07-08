package cz.tomkren.typewars;

import cz.tomkren.helpers.ABC;
import cz.tomkren.helpers.F;

import java.util.*;

public class ProtoNode {

    private final String name;
    private final Type out;
    private final List<Type> ins;

    public ProtoNode(String name, Type out, List<Type> ins) {
        this.name = name;
        this.out = out;
        this.ins = ins;
    }

    public ProtoNode(ProtoNode node) {
        this.name = node.name;
        this.out  = node.out;
        this.ins  = node.ins;
    }

    public ProtoNode(String[] args) {
        this(args[0].trim(), F.map( Arrays.asList(args).subList(1, args.length), Types::parse) );
    }

    public ProtoNode(String name, List<Type> ts) {
        this(name, ts.get(0), ts.subList(1,ts.size()) );
    }

    public ProtoNode(String name, Type out) {
        this(name, out, EMPTY_INS);
    }
    private static final List<Type> EMPTY_INS = Arrays.asList();

    public ProtoNode(String name, String out, String... ins) {
        this(name, Types.parse(out), F.map(ins , Types::parse) );
    }

    public ProtoNode(String name, String out) {
        this(name, Types.parse(out));
    }


    public void getVarIds(Set<Integer> ret) {
        out.getVarIds(ret);
        ins.forEach(t->t.getVarIds(ret));
    }

    public ProtoNode simpleMatchOut(Type goal) {
        Sub sub = Sub.mgu(out, goal);
        if (sub.isFail()) {return null;}
        return new ProtoNode(name, sub.apply(out) , F.map(sub::apply, ins) );
    }



    public ABC<ProtoNode,Sub,Integer> matchOut(Type goal, int startVarId) {
        List<Type> ts = new ArrayList<>(ins.size()+1);
        ts.add(out); ts.addAll(ins); // todo nebylo by lepší teda držet to prostě v seznamu

        List<Type> ts2 = Types.freshenVars(ts, startVarId);
        //List<Type> ts2 = p._1();
        //int nextVarId  = p._2();

        Type out2 = ts2.get(0);
        Sub mgu = Sub.mgu(out2, goal);

        if (mgu.isFail()) {return null;}

        ProtoNode resultNode = new ProtoNode(name,F.map(mgu::apply,ts2));

        // nevracíme celou mgu, ale jen ty proměnné co se vyskytují ve vstupním goalu anebo ve výsledným protoNodu.
        Set<Integer> varsSet = new HashSet<>();
        goal.getVarIds(varsSet);
        mgu.removeAllBut(varsSet);

        //vybereme nextVarId: vložíme do varsSet ještě navíc proměnné v resultNodu a startVarId a vezmeme maximum
        resultNode.getVarIds(varsSet); // Není třeba pro mgu.removeAllBut. Pokud totiž v resultNode je proměnná,
                                       // tak určitě není v mgu, pač pokud by byla, tak by zmizela aplikací mgu.
        varsSet.add(startVarId - 1); // -1 páč pak přičítáme 1, tzn pokud je jediná, tak zůstane nextVarId na startVarId
        int nextVarId = 1 + Collections.max(varsSet);

        return new ABC<>( resultNode , mgu , nextVarId );
    }

    public String getName() {return name;}
    public Type getOut() {return out;}
    public List<Type> getIns() {return ins;}
    public int getArity() {return ins.size();}



    @Override
    public String toString() {
        return out +" <-< "+ name+ (ins.isEmpty() ? "" : " <-< "+ ins) ;
    }
}
