package cz.tomkren.typewars.dag;


import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.core.KAtts;
import cz.tomkren.kutil2.core.XmlLoader;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.typewars.*;

import java.util.List;

public class TypedDagGeneratorMacro {
    public static String mkXML(KAtts kAtts) {

        if (kAtts.getString("off") != null && kAtts.getString("off").equals("true")) {
            return "<o pos=\"-1000 -1000\"/>";
        }

        CodeLib lib = CodeLib.mk( kAtts.getString("lib") );//new CodeLib(nodes);
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

        List<PolyTree> trees = lib.basicGenerate(goal, to).subList(from, to);
        List<TypedDag> dags = F.map(trees, t -> (TypedDag) t.computeValue());

        StringBuilder sb = new StringBuilder();

        String title = kAtts.getString("title");
        title = title == null ? "DAG-generator" : title;

        sb.append("<").append(XmlLoader.OBJECT_TAG).append(" type=\"frame\" size=\"")
                .append(kAtts.getString("size")).append("\"")
                .append(" pos=\"").append(kAtts.getString("pos")).append("\"")
                .append(" id=\"").append(kAtts.getString("id")).append("\"")
                .append(" guiStuff=\"true\" title=\"").append(title).append("\">");

        int okraj = 20;
        int i = 0;
        int x = 3*okraj;
        for (TypedDag dag : dags) {

            Int2D pos = new Int2D(x, 3*okraj);
            x += dag.getPxWidth() + okraj;

            sb.append( dag.toKutilXML(pos) ).append("\n");



            Log.it(trees.get(i) + "\n");
            Log.it(dag.toString());

            i++;
        }

        sb.append("</").append(XmlLoader.OBJECT_TAG).append(">");

        return sb.toString();
    }
}
