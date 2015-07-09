package cz.tomkren.typewars.eva;

import cz.tomkren.helpers.AA;
import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.TypedDag;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.List;

/** Created by pejsek on 6.7.2015. */

public class DagEvolutionLogger implements Logger<PolyTree> {


    private final File runLogDir;

    public DagEvolutionLogger(JSONObject config, String logPath) {

        if (logPath == null) {
            runLogDir = null;
            return;
        }

        if (!(new File(logPath).exists())) {
            boolean success = new File(logPath).mkdirs();
            if (!success) {
                throw new Error("Unable to create log directory!");
            }
        }

        int i = 1;

        while (new File(logPath,"run_"+i).exists()) {
            i++;
        }

        runLogDir = new File(logPath,"run_"+i);


        boolean success = runLogDir.mkdir();

        if (!success) {
            throw new Error("Unable to create log directory!");
        }


        Log.it("log directory for this run: "+runLogDir);

        writeToFile("config.txt", config.toString(2));

    }

    private void writeToFile(String filename, String str) {

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(runLogDir, filename)), "utf-8"))) {
            writer.write(str);
            writer.write('\n');
        } catch (IOException e) {
            throw new Error("Logging error: "+ e.getMessage());
        }

    }

    public DagEvolutionLogger() {
        this(null,null);
    }

    @Override
    public void logPop(int run, int generation, EvaledPop<PolyTree> pop) {
        PolyTree best = pop.getBestIndividual();
        Log.it("gen" + generation + " \t best: [" + best.getProbability() + "] " + best);
        Log.it(pop);

        if (runLogDir != null) {

            AA<JSONArray> twoJsons = evaledPopToJson(pop);
            JSONArray readable = twoJsons._1();
            JSONArray parsable = twoJsons._2();

            writeToFile("gen_" + generation + "_readable.txt", readable.toString(2));
            writeToFile("gen_" + generation + "_parsable.txt", parsable.toString());
            writeToFile("gen_" + generation + "_parsable2.txt", parsable.toString(2));
        }



        /*for (PolyTree tree : pop.getIndividuals().getList()) {
            Log.it(  );
        }*/

    }

    public static AA<JSONArray> evaledPopToJson(EvaledPop<PolyTree> pop) {

        JSONArray readable = new JSONArray();
        JSONArray parsable = new JSONArray();


        for(AA<JSONObject> p:  F.map(F.sort(pop.getIndividuals().getList(), t -> -t.getFitVal().getVal()), DagEvolutionLogger::dagTreeIndividualToJson)) {
            readable.put(p._1());
            parsable.put(p._2());
        }

        return new AA<>(readable,parsable);
    }

    public static AA<JSONObject> dagTreeIndividualToJson(PolyTree tree) {

        //TODO trochu neefektivní počítat znova, ale snad to přežijem
        TypedDag dag = (TypedDag)tree.computeValue();

        JSONObject readable = new JSONObject();
        JSONObject parsable = new JSONObject();

        readable.put("fit", tree.getFitVal().getVal());
        readable.put("short", tree.toStringWithoutParams());
        readable.put("tree", tree.toString().replace('"', '\''));
        readable.put("json", dag.toJson().replace('"', '\'').replace("\n", "").replace(" ", ""));
        readable.put("kutil", dag.toKutilXML(new Int2D(100, 100)));

        parsable.put("fit", tree.getFitVal().getVal());
        parsable.put("json", new JSONObject(dag.toJson()) );

        return new AA<>(readable, parsable);
    }


}
