package cz.tomkren.typewars.dag;

import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.core.KAtts;
import cz.tomkren.kutil2.core.XmlLoader;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.typewars.*;
import cz.tomkren.typewars.basicgen.Generator;
import cz.tomkren.typewars.ProtoNode;

import java.util.ArrayList;
import java.util.List;

public class DagGeneratorMacro {
    public static String mkXML(KAtts kAtts) {

        if (kAtts.getString("off") != null && kAtts.getString("off").equals("true")) {
            return "<o pos=\"-1000 -1000\"/>";
        }

        List<ProtoNode> nodes = new ArrayList<>();

        for (String part : kAtts.getString("lib").split(";")) {
            String row = part.trim();
            ProtoNode protoNode = new ProtoNode( row.split(",") );
            nodes.add(protoNode);
        }

        NodeLib lib = new NodeLib(nodes);
        Type goal = Types.parse(kAtts.getString("goal"));

        String[] ps = kAtts.getString("n").split("-");

        int from = 0;
        int to = 1;
        if (ps.length == 2) {
            from = Integer.parseInt( ps[0].trim() );
            to   = Integer.parseInt( ps[1].trim() );
        } else if (ps.length == 1) {
            to = Integer.parseInt( ps[0].trim() );
        }

        List<PolyTree> trees = Generator.generate(goal, lib, to).subList(from, to);
        List<Dag> dags = F.map(trees, Dag::fromTree);

        StringBuilder sb = new StringBuilder();

        String title = kAtts.getString("title");
        title = title == null ? "DAG-generator" : title;

        sb.append("<").append(XmlLoader.OBJECT_TAG).append(" type=\"frame\" size=\"")
                .append(kAtts.getString("size")).append("\"")
                .append(" pos=\"").append(kAtts.getString("pos")).append("\"")
                .append(" id=\"").append(kAtts.getString("id")).append("\"")
                .append(" guiStuff=\"true\" title=\"").append(title).append("\">");

        int i = 0;
        for (Dag dag : dags) {

            Int2D pos = new Int2D(i*500, 0);

            sb.append( dag.toKutilXML(pos) ).append("\n");

            Log.it(trees.get(i)+"\n");
            Log.it(dag.toString());

            i++;
        }

        //Log.it(sb.toString());

        sb.append("</").append(XmlLoader.OBJECT_TAG).append(">");

        return sb.toString();
    }
}
