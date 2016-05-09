package cz.tomkren;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
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
import java.util.Comparator;
import java.util.Random;

/** Created by tom on 6.7.2015. */

public class Main {


    public static final String VERSION = "v0.5 beta";


    public static void main(String[] args) {

        Log.it("dageva ["+ VERSION +"]");


        if (args.length < 2 || args[0].equals("--help")) {
            Log.it("You must provide two program arguments: <json-config-filename> <log-dir-path>");
            return;
        }

        String jsonConfigFilename = args[0];
        String logPath = args[1];

        Log.it("Program arguments:");
        Log.it("  jsonConfigFileName : " + jsonConfigFilename);
        Log.itln("  logPath            : " + logPath);

        try {
            String configStr = Files.toString(new File(jsonConfigFilename), Charsets.UTF_8);
            Log.itln(jsonConfigFilename +" = "+ configStr);
            JSONObject config = new JSONObject(configStr);

            Long seed = config.has("seed") ? config.getLong("seed") : null;
            Checker checker = new Checker(seed);
            Random rand = checker.getRandom();

            if (seed == null) {
                config.put("seed", checker.getSeed());
            }

            DataScientistFitness fitness = new DataScientistFitness(config.getString("serverUrl"), config.getString("dataset"), true);
            JSONObject allParamsInfo = fitness.getAllParamsInfo_mayThrowUp();

            SmartLib lib = SmartLib.mk(allParamsInfo, config.getJSONArray("lib")); //SmartLib.mkDataScientistLib01FromParamsInfo();
            Type goalType = Types.parse(config.getString("goalType"));
            QuerySolver querySolver = new QuerySolver(lib, rand);

            IndivGenerator<PolyTree> generator = new RandomParamsPolyTreeGenerator(goalType, config.getInt("generatingMaxTreeSize"), querySolver);
            Selection<PolyTree> selection = new Selection.Tournament<>(config.getDouble("tournamentBetterWinsProbability"), rand);
            Distribution<Operator<PolyTree>> operators = new Distribution<>(Arrays.asList(
                    new BasicTypedXover(config, rand),
                    new SameSizeSubtreeMutation(config, querySolver),
                    new OneParamMutation(config, rand),
                    new CopyOp<>(config)
            ));

            Comparator<PolyTree> comparator = (x,y) -> QuerySolver.compareTrees.compare(y,x);

            Evolver<PolyTree> evolver = new Evolver.Opts<>(fitness, comparator, new EvoOpts(config), generator, operators, selection, new DagEvolutionLogger(config,logPath,checker), rand).mk();

            Log.it("Config [OK] ...");
            Log.it("Generating initial population...");

            evolver.startRun();

            if (config.getBoolean("killServer")) {
                fitness.killServer();
            }

            checker.results();

        } catch (IOException e) {
            Log.itln("Config file error: "+e.getMessage());
        } catch (JSONException e) {
            Log.itln("JSON error: " + e.getMessage());
        }catch (XmlRpcException e) {
            Log.it("Dag-evaluate server error: Server is probably not running (or it is starting right now). Start the server and try again, please.");
        }

    }
}
