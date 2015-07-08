package cz.tomkren.typewars.basicgen;

import cz.tomkren.typewars.ProtoNode;

import java.util.ArrayList;
import java.util.List;

public interface CreationLog {

    public List<Row> toRowList();

    public class Row {
        private final ProtoNode node;
        private final List<Integer> succIs;
        private final int jNext;

        public Row(ProtoNode node, int j) {
            this.node = node;

            int numIns = node.getIns().size();
            succIs = new ArrayList<>(numIns);

            for (int k = 0; k < numIns; k++) {
                succIs.add(j+k);
            }

            jNext = j + numIns;
        }

        public ProtoNode getNode() {
            return node;
        }

        public List<Integer> getSuccIs() {
            return succIs;
        }

        public String toString(int i) {
            return i +":\t"+ node +(succIs.isEmpty() ? "" : " ... "+ succIs) ;
        }
    }

    public class Start implements CreationLog {
        private Start() {}

        @Override
        public List<Row> toRowList() {
            return new ArrayList<>();
        }

        @Override
        public String toString() {
            return "[]";
        }
    }

    public static final Start START = new Start();

    public class Step implements CreationLog {
        private final CreationLog before;
        private final Row row;
        private final int i;

        public Step(CreationLog before, ProtoNode node) {
            this.before = before;
            boolean isStep = before instanceof Step;
            i     = isStep ? ((Step)before).i+1       : 0;
            int j = isStep ? ((Step)before).row.jNext : 1;
            row = new Row(node,j);
        }

        @Override
        public List<Row> toRowList() {
            List<Row> ret = before.toRowList();
            ret.add(row);
            return ret;
        }

        @Override
        public String toString() {
            String rowStr = row.toString(i);
            return before instanceof Step ? before+"\n  "+rowStr : "  "+rowStr ;
        }
    }


    
}
