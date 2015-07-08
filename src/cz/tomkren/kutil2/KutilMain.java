package cz.tomkren.kutil2;

import com.google.common.base.Joiner;
import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.core.Kutil;
import cz.tomkren.kutil2.core.XmlLoader;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.typewars.TypedDag;

import java.util.List;

/**
 * KUTIL V2
 * V2 started by Tomáš Křen on 31. 10. 2014.
 */

public class KutilMain {

    private static final String VERSION = "alpha 2.0.2";
    private static final String defaultXmlFile = "funwars.xml"; // "minimal.xml"; // "minimal_problem.xml"; //  "fun.xml"; //  // todo udělat spíš nějakej "config file"

    public static void main(String[] args) {
        Log.it("KUTIL (v "+VERSION+"), hello!\n");

        boolean isArgsInput = args.length > 0;
        XmlLoader.LoadMethod loadMethod = isArgsInput ? XmlLoader.LoadMethod.FILE : XmlLoader.LoadMethod.RESOURCE ;
        String loadInput = isArgsInput ? args[0] : defaultXmlFile;

        new Kutil().start(loadMethod, loadInput);

        Log.it("Good bye!");
    }



    public static void startLib(String[] lib, String goalType, Integer numTrees) {
        new Kutil().start(XmlLoader.LoadMethod.STRING, wrapInKutilShower(lib, goalType, numTrees.toString()));
    }

    public static void showDag(TypedDag dag) {
        new Kutil().start(XmlLoader.LoadMethod.STRING, wrapInKutilShower(mkFrameWith(dag.toKutilXML(new Int2D(64, 64)))));
    }

    public static void showDags(List<TypedDag> dags) {
        StringBuilder sb = new StringBuilder();

        int width = 16000;
        int okraj = 20;
        int init  = 3*okraj;

        int x = init;
        int y = init;

        int maxHeight = 0;

        for (TypedDag dag : dags) {
            sb.append( dag.toKutilXML(new Int2D(x,y)) ).append("\n");

            x += dag.getPxWidth() + okraj;

            if (dag.getPxHeight() > maxHeight) {
                maxHeight = dag.getPxHeight();
            }

            if (x > width) {
                x = init;
                y += maxHeight + okraj;

            }
        }

        String xml = wrapInKutilShower(mkFrameWith(sb.toString()));
        new Kutil().start(XmlLoader.LoadMethod.STRING, xml);
    }



    private static String wrapInKutilShower(String[] lib, String goalType, String numTrees) {
        return wrapInKutilShower(mkLibMacro(lib, goalType, numTrees));
    }


    private static String wrapInKutilShower(String xml) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kutil>\n" +
                "    <o type=\"time\" ups=\"80\">\n" +
                "\n" +
                "        <o type=\"frame\" title=\"Pokusy s DAGama\" id=\"$window\">\n" +
                "\n" +
                "            <o type=\"frame\" title=\"Pohled na frame okna\" showXML=\"true\" target=\"$window\" pos=\"221 -579\" size=\"640 480\" />\n" +
                "            <o type=\"frame\" title=\"Pohled na frame s resultem\" showXML=\"true\" target=\"$pokus3\" pos=\"-455 -580\" size=\"640 480\" />\n" +
                "\n" +
                "\n" + xml +
                "\n" +
                "        </o>\n" +
                "\n" +
                "\n" +
                "    </o>\n" +
                "</kutil>";
    }

    // id:

    private static String mkLibMacro(String[] lib, String goalType, String numTrees) {
        return  "            <macro type=\"TypedDagGenerator\" id=\"$pokus3\" title=\"Title...\" size=\"2500 2000\" pos=\"4 4\">\n" +
                "                <n>"+numTrees+"</n>\n" +
                "                <goal>"+goalType+"</goal>\n" +
                "                <lib>"+ Joiner.on(";\n").join(lib) +"</lib>\n" +
                "            </macro>\n" ;
    }

    private static String mkFrameWith(String innerXML) {
        return  "<o type=\"frame\" id=\"$pokus3\" title=\"Title...\" size=\"2500 2000\" pos=\"4 4\">\n" +
                innerXML+
                "</o>\n" ;
    }


}
