package cz.tomkren.typewars;

import cz.tomkren.helpers.AB;
import cz.tomkren.helpers.F;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.pikater.SimpleVertex;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

    private final int id;
    private final String name;
    private final JSONObject params;
    private final List<AB<Vertex,Integer>> successors;
    private int nextFreeSlot;
    private double x, y;

    private static int nextId = 1; // todo pak udělat míň haxově


    public Vertex(String boxName, JSONObject params) {
        id = nextId++;
        name = boxName;

        this.params = params;

        successors = new ArrayList<>();
        nextFreeSlot = 0;

        x = 0;
        y = 0;
    }

    public Vertex(String boxName) {
        this(boxName, new JSONObject());
    }

    public Vertex pseudoCopy() {return new Vertex(this);}

    private Vertex(Vertex v) {
        id = nextId++;
        name = v.name;

        params = v.params;  // TODO může bejt nebezpečný, kdyby někdo měnil ty parametry!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        int n = v.successors.size();
        successors = new ArrayList<>(n); // ty nekopírujeme, pač tam chceme hluboký kopie
        nextFreeSlot = v.nextFreeSlot;

        x = v.x;
        y = v.y;
    }

    public double getX() {return x;}
    public double getY() {return y;}

    public void moveX(double dx) {x += dx;}
    public void moveY(double dy) {y += dy;}

    public String getName() {return name;}
    public int getId() {return id;}
    public String getKutilId() {return "$v_"+id;}

    public List<Vertex> getSuccessors() {return F.map(successors,AB::_1);}

    public List<AB<Vertex,Integer>> getSuccessorsWithPorts() {return successors;}

    public void addSuccessor(Vertex v, int port) {
        successors.add(new AB<>(v,port));
    }

    public void addSuccessor(Vertex v) {
        successors.add(new AB<>(v,v.nextFreeSlot));
        v.nextFreeSlot++;
    }

    @Override
    public String toString() {return name+"("+id+")";}

    public enum Info { BEGIN, END, MIDDLE }

    public AB<SimpleVertex,Info> toSimpleVertex() {

        Info info = nextFreeSlot == 0 ? Info.BEGIN : (successors.isEmpty() ? Info.END : Info.MIDDLE);

        int numInputs  = Math.max(1, nextFreeSlot);
        int numOutputs = Math.max(1, successors.size());

        List<SimpleVertex.LinkTarget> targets = F.map(successors, p-> new SimpleVertex.LinkTarget(p._1().getId(), p._2()) );

        return new AB<>(new SimpleVertex(id, name, numInputs, numOutputs, targets), info);
    }

    public static final int X_1SIZE = 64;
    public static final int Y_1SIZE = 64;

    public static void toJson_input(StringBuilder sb, List<Vertex> ins) {
        String id   = "input";
        String name = "input";

        int numOutputs = ins.size();

        sb.append("\"").append(id).append("\" : [ [], \"").append(name).append("\", [");

        int i = 0;
        for (Vertex s : ins) {
            sb.append('\"').append(s.id).append(':').append(i).append("\"");
            if (i < numOutputs-1) {
                sb.append(", ");
            }
            i++;
        }

        sb.append("] ]");
    }

    public void toJson(StringBuilder sb) {
        int numInputs  = Math.max(1, nextFreeSlot);
        int numOutputs = Math.max(1, successors.size());

        sb.append("\"").append(id).append("\" : [ [");

        for (int i = 0; i < numInputs; i++) {
            sb.append('\"').append(id).append(':').append(i).append("\"");
            if (i < numInputs-1) {
                sb.append(", ");
            }
        }

        String paramsStr = params.toString(); //"{}"; // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        sb.append("], [\"").append(name).append("\", ").append(paramsStr).append("], [");

        int i = 0;
        for (AB<Vertex,Integer> p : successors) {
            Vertex s = p._1();
            int port = p._2();
            sb.append('\"').append(s.id).append(':').append(port).append("\"");
            if (i < numOutputs-1) {
                sb.append(", ");
            }
            i++;
        }

        sb.append("] ]");
    }

    public void toKutilXML(StringBuilder sb, Int2D pos) { //, int x, int y) {

        int xPos = (int)((0.6+x) * X_1SIZE ) + pos.getX();
        int yPos = (int)((0.5+y) * Y_1SIZE ) + pos.getY();

        int numInputs  = Math.max(1, nextFreeSlot);
        int numOutputs = Math.max(1, successors.size());

        sb.append("<o id=\"").append(getKutilId()).append("\"")
          .append(" shape=\"f ").append(numInputs).append(' ').append(numOutputs).append(' ').append(name);

        for (AB<Vertex,Integer> p : successors) {
            Vertex s = p._1();
            int port = p._2();
            sb.append(' ').append(s.getKutilId()).append(':').append(port);
        }
        sb.append("\"").append("\t pos=\"").append(xPos).append(' ').append(yPos).append("\"").append("/>");
    }


    public String successorsStr() {return name +"("+id+")->"+ successors.toString();}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return id == vertex.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
