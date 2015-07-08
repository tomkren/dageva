package cz.tomkren.pikater;

import cz.tomkren.helpers.F;

import java.util.ArrayList;
import java.util.List;

public class SimpleVertex {

    private final int id;
    private final String name;
    private final int numIn;
    private final int numOut;
    private final List<LinkTarget> targets;

    public static class LinkTarget {
        private final int id;
        private final int port;

        public LinkTarget(int id, int port) {this.id = id; this.port = port;}
        public int getId() {return id;}
        public int getPort() {return port;}

        @Override
        public String toString() {
            return id + ":" + port ;
        }
    }

    public SimpleVertex(int id, String name, int numIn, int numOut, List<LinkTarget> targets) {
        this.id = id;
        this.name = name;
        this.numIn = numIn;
        this.numOut = numOut;
        this.targets = targets;
    }

    public void addTarget(LinkTarget target) {
        targets.add(target);
    }

    public static SimpleVertex read(String str) {
        String[] parts = str.trim().split("\\s+");

        int id = Integer.parseInt(parts[0]);
        String name = parts[1];
        int numIn = Integer.parseInt(parts[2]);
        int numOut = Integer.parseInt(parts[3]);

        List<LinkTarget> targets = new ArrayList<>(parts.length-4);

        for (int i = 4; i < parts.length; i++) {
            String linkStr = parts[i].trim();
            LinkTarget link;

            if ("null".equals(linkStr)) {
                link = null;
            } else {
                String[] subParts = linkStr.split(":");
                link = new LinkTarget(  Integer.parseInt(subParts[0]) ,
                                        Integer.parseInt(subParts[1]) );
            }

            targets.add(link);
        }

        return new SimpleVertex(id, name, numIn, numOut, targets);
    }

    @Override
    public String toString() {
        return id + " " + name + " " + numIn + " " + numOut + " " + targets ;
    }

    public static List<SimpleVertex> readLines(String... strs) {
        return F.map(strs, SimpleVertex::read);
    }

    public int getId() {return id;}
    public String getName() {return name;}
    public int getNumIn() {return numIn;}
    public int getNumOut() {return numOut;}
    public List<LinkTarget> getTargets() {return targets;}

}



