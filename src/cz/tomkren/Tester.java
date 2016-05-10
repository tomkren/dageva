package cz.tomkren;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.KutilMain;
import cz.tomkren.typewars.*;
import cz.tomkren.typewars.eva.IndivGenerator;
import cz.tomkren.typewars.eva.RandomParamsPolyTreeGenerator;
import cz.tomkren.typewars.reusable.QuerySolver;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/** Created by tom on 9.5.2016. */

public class Tester {


    public static void main(String[] args) {


        //String jsonConfigFilename =  "config.json" ;
        //String jsonConfigFilename =  "config_stacking.json" ;
        String jsonConfigFilename =  "config_stackAndBoo.json" ;


        try {

            String configStr = Files.toString(new File(jsonConfigFilename), Charsets.UTF_8);
            Log.itln(jsonConfigFilename + " = " + configStr);
            JSONObject config = new JSONObject(configStr);

            Long seed = config.has("seed") ? config.getLong("seed") : null;
            Checker checker = new Checker(seed);
            Random rand = checker.getRandom();

            if (seed == null) {
                config.put("seed", checker.getSeed());
            }

            String testParamsInfoStr = "{\"DT\": {\"min_samples_split\": [1, 2, 5, 10, 20], \"criterion\": [\"gini\", \"entropy\"], \"max_features\": [0.05, 0.1, 0.25, 0.5, 0.75, 1], \"min_samples_leaf\": [1, 2, 5, 10, 20], \"max_depth\": [1, 2, 5, 10, 15, 25, 50, 100]}, \"gaussianNB\": {}, \"SVC\": {\"gamma\": [0.0, 0.0001, 0.001, 0.01, 0.1, 0.5], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"union\": {}, \"copy\": {}, \"PCA\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1], \"whiten\": [false, true]}, \"logR\": {\"penalty\": [\"l1\", \"l2\"], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"kMeans\": {}, \"kBest\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1]}, \"vote\": {}}";
            JSONObject allParamsInfo = new JSONObject(testParamsInfoStr);

            SmartLib lib = SmartLib.mk(allParamsInfo, config.getJSONArray("lib")); //SmartLib.mkDataScientistLib01FromParamsInfo();
            String goalTypeStr = config.getString("goalType");
            Type goalType = Types.parse(goalTypeStr);
            QuerySolver querySolver = new QuerySolver(lib, rand);

            IndivGenerator<PolyTree> generator = new RandomParamsPolyTreeGenerator(goalType, config.getInt("generatingMaxTreeSize"), querySolver);

            /*int upToTreeSize = 20;
            TMap<PolyTree> treeTMap = querySolver.generateAllUpTo(goalTypeStr, upToTreeSize);
            List<PolyTree> trees = treeTMap.get(goalType);*/

            List<PolyTree> trees = generator.generate(config.getInt("populationSize"));


            logList("trees", trees);

            List<Object> objs = F.map(trees, PolyTree::computeValue);
            List<TypedDag> dags = F.map(objs, o -> (TypedDag)o);
            List<String> jsonTrees = F.map(dags, dag -> dag.toJson());


            logList("json", jsonTrees);
            logList("trees", trees);
            Log.it("num trees: "+ trees.size());

            KutilMain.showDags(dags);





        } catch (IOException e) {
            Log.itln("Config file error: "+e.getMessage());
        } catch (JSONException e) {
            Log.itln("JSON error: " + e.getMessage());
        } /*catch (XmlRpcException e) {
            Log.it("Dag-evaluate server error: Server is probably not running (or it is starting right now). Start the server and try again, please.");
        }*/


    }

    private static void logList(String tag, List<?> list) {
        Log.it("<"+tag+" begin>");
        Log.list(list);
        Log.it("<"+tag+" end>\n");
    }




}
