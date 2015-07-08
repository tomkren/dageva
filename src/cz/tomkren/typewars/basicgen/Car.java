package cz.tomkren.typewars.basicgen;

import cz.tomkren.helpers.Comb0;
import cz.tomkren.helpers.AB;
import cz.tomkren.helpers.ABC;
import cz.tomkren.helpers.F;
import cz.tomkren.typewars.*;

import java.util.*;

public class Car {

    private final CreationLog log;
    private int nextVarId;
    private final Deque<Type> goalsQueue;
    private Sub sub;
    private int treeSize;

    public Car(Type goal) {
        log = CreationLog.START;

        AB<Type,Integer> fresheningResult = goal.freshenVars(0, new Sub());
        goal = fresheningResult._1();
        nextVarId = fresheningResult._2();

        goalsQueue = new ArrayDeque<>();
        goalsQueue.offer(goal);

        sub = new Sub();

        treeSize = 1; // góly počítáme taky
    }

    private Car(Car dad, ProtoNode newNode, Sub mgu, int nextVarId) {
        log = new CreationLog.Step(dad.log, newNode);
        this.nextVarId = nextVarId;

        // todo dyštak nějak taky udělat sdílený ve stylu podobném CreationLogu
        goalsQueue = new ArrayDeque<>(dad.goalsQueue);
        List<Type> inputTypes = newNode.getIns();
        inputTypes.forEach(goalsQueue::offer);

        mgu.dot(dad.sub);
        sub = mgu;

        treeSize = dad.treeSize + inputTypes.size(); // + 1 - 1
    }

    public List<Car> step(NodeLib lib) {
        Type goal = goalsQueue.poll();
        if (goal == null) {return null;} // hurá, hotovo
        goal = sub.apply(goal);

        List<Car> successors = new ArrayList<>();
        for (ProtoNode node : lib.getNodeList()) {
            ABC<ProtoNode,Sub,Integer> matchRes = node.matchOut(goal,nextVarId);
            if (matchRes != null) {
                successors.add(new Car(this, matchRes._1(), matchRes._2(), matchRes._3()));
            }
        }
        return successors;
    }

    public PolyTree mkPolyTree(NodeLib nodeLib) {
        if (!goalsQueue.isEmpty()) {throw new Error("Unfinished car cannot be transformed to a PolyTree.");}
        CodeLib codeLib = nodeLib instanceof CodeLib ? (CodeLib)nodeLib : null;
        return mkPolyTree(0, log.toRowList(), codeLib);
    }

    private PolyTree mkPolyTree(int i, List<CreationLog.Row> rows, CodeLib lib) {
        CreationLog.Row row = rows.get(i);
        ProtoNode node = row.getNode();
        List<PolyTree> sons = F.map(row.getSuccIs(), j -> mkPolyTree(j, rows, lib));

        String name = node.getName();
        CodeNode codeNode = lib == null ? null : lib.getCodeNode(name);

        return new PolyTree(codeNode, sub.apply(node.getOut()) , sons);
    }

    @Override
    public String toString() {
        return "Car{" +
                "\n log:\n" + log +
                "\n nextVarId: " + nextVarId +
                "\n goalsQueue: " + goalsQueue +
                "\n sub: " + sub +
                "\n treeSize: " + treeSize +
                "\n}\n";
    }

    public static class MinTreeSizeComparator implements Comparator<Car> {
        public int compare(Car c1, Car c2) {
            return Integer.compare(c1.treeSize, c2.treeSize);
        }
    }

}
