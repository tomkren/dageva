package cz.tomkren;


import com.google.common.base.Charsets;
import com.google.common.io.Files;
import cz.tomkren.helpers.AB;
import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.SmartLib;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.Types;
import cz.tomkren.typewars.dag.DataScientistFitness;
import cz.tomkren.typewars.eva.*;
import cz.tomkren.typewars.reusable.QuerySolver;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

/** Created by tom on 6.7.2015. */

public class Main {

    public static void main(String[] args) {


        if (args.length < 2 || args[0].equals("--help")) {
            Log.it("You must provide two arguments: <json-config-filename> <log-dir-path>");
            return;
        }



        String jsonConfigFileName = args[0];
        String logPath = args[1];

        Log.it("jsonConfigFileName: "+ jsonConfigFileName);
        Log.itln("logPath: "+ logPath);

        try {
            String jsonStr = Files.toString(new File(jsonConfigFileName), Charsets.UTF_8);

            //Log.itln(jsonStr);

            JSONObject config = new JSONObject(jsonStr);

            Log.itln(config.toString(2));

            Long seed = config.has("seed") ? config.getLong("seed") : null;

            Checker ch = new Checker(seed);
            Random rand = ch.getRandom();

            DataScientistFitness fitness = new DataScientistFitness(config.getString("serverUrl"), config.getString("dataset"), true);
            EvoOpts evoOpts = new EvoOpts(config.getInt("numGenerations"),config.getInt("populationSize"),config.getBoolean("saveBest"));

            SmartLib lib = SmartLib.mkDataScientistLib01FromParamsInfo(fitness.getAllParamsInfo_mayThrowUp());
            QuerySolver querySolver = new QuerySolver(lib, rand);
            Type goalType = Types.parse(config.getString("goalType"));

            IndivGenerator<PolyTree> generator = new RandomParamsPolyTreeGenerator(goalType, config.getInt("generatingMaxTreeSize"), querySolver);
            Selection<PolyTree> selection = new Selection.Tournament<>(config.getDouble("tournamentBetterWinsProbability"), rand);
            Distribution<Operator<PolyTree>> operators = new Distribution<>(Arrays.asList(
                    new BasicTypedXover(config.getDouble("basicTypedXoverProbability"), rand),
                    new SameSizeSubtreeMutation(config.getDouble("sameSizeSubtreeMutationProbability"), querySolver, config.getInt("mutationMaxSubtreeSize")),
                    new OneParamMutation(config.getDouble("oneParamMutationProbability"), ch.getRandom(), Arrays.asList(
                            AB.mk(-2, 0.1),
                            AB.mk(-1, 0.4),
                            AB.mk(1, 0.4),
                            AB.mk(2, 0.1)
                    )),
                    new CopyOp<>(config.getDouble("copyOpProbability"))
            ));

            Evolver<PolyTree> evolver = new Evolver.Opts<>(fitness, evoOpts, generator, operators, selection, new PolyTreeEvolutionLogger(), rand).mk();

            Log.it("Generating initial population...");
            evolver.startRun();


            fitness.killServer();

            ch.results();

        } catch (IOException e) {
            Log.itln("Config file error: "+e.getMessage());
        } catch (JSONException e) {
            Log.itln("JSON error: " + e.getMessage());
        }catch (XmlRpcException e) {
            Log.it("Dag-evaluate server error: Server is probably not running (or it is starting right now). Start the server and try again, please.");
        }


    }



}
