package cz.tomkren.typewars;

import cz.tomkren.helpers.Comb0;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CodeNode extends ProtoNode {

    private Comb0 code;

    public CodeNode(ProtoNode protoNode, Comb0 code) {
        super(protoNode);
        this.code = code;
    }

    public String getNameWithParams() {
        return getName();
    }

    public Comb0 getCode() {return code;}

    public static final String CLASS_PREFIX = "cz.tomkren.typewars.";

    public static CodeNode mk(String nodeLine) {
        return mk(nodeLine, new JSONObject());
    }

    public static CodeNode mk(String nodeLine, JSONObject allParamsInfo) {

        int colonPos = nodeLine.lastIndexOf(':');
        String rest = nodeLine.substring(0,colonPos).trim();
        String outTypeStr = nodeLine.substring(colonPos+1).trim();

        String[] ps = rest.split("\\(", 2);
        String classDotMethod = ps[0].trim();

        boolean isTerminal = ps.length < 2;

        if (!isTerminal) {
            rest = ps[1].trim();
            if (rest.charAt(rest.length()-1) != ')') {
                throw new Error("Missing ')' at the end of: "+rest);
            }
            rest = rest.substring(0, rest.length() - 1).trim();
        }

        ps = classDotMethod.split("\\.", 2);

        String homeClassStr, methodName;

        if (ps.length == 1) {
            homeClassStr = null;
            methodName   = ps[0].trim();
        } else if (ps.length == 2) {
            homeClassStr = ps[0].trim();
            methodName   = ps[1].trim();
        } else {
            throw new Error("Wrong format of the method name.");
        }

        if (isTerminal) {
            return fromStatic0(outTypeStr, homeClassStr, methodName, allParamsInfo);
        }

        ps = rest.split(",");
        int n = ps.length;

        String[] args = new String[2*n];

        for (int i = 0; i < n; i++) {
            String[] ps2 = ps[i].trim().split(":", 2);
            args[2*i  ] = ps2[0].trim();
            args[2*i+1] = ps2[1].trim();
        }

        //Log.it(buildingSymbolDescription);
        //Log.it(outTypeStr);
        //Log.it(homeClassStr);
        //Log.it(methodName);
        //Log.list(Arrays.asList(args));

        return fromStatic(outTypeStr, homeClassStr, methodName, args);
    }

    public static Class<?> getClass(String className) throws ClassNotFoundException {
        switch (className) {
            case "Object" : return Object.class;
            default : return Class.forName(CLASS_PREFIX + className);
        }
    }

    public static CodeNode fromStatic0(String outTypeStr, String homeClassStr, String methodName) {
        return fromStatic0(outTypeStr, homeClassStr, methodName, new JSONObject());
    }


    public static CodeNode fromStatic0(String outTypeStr, String homeClassStr, String methodName,  JSONObject allParamsInfo) {

        if (homeClassStr == null) {return fromStatic0(outTypeStr, methodName, allParamsInfo);}

        try {
            Type outType = Types.parse(outTypeStr);
            Class<?> homeClass = getClass(homeClassStr);

            ProtoNode protoNode = new ProtoNode(methodName, outType);
            Comb0 comb = Comb0.fromStatic(methodName, homeClass);
            return new CodeNode(protoNode, comb);

        } catch (ClassNotFoundException e) {
            throw new Error("ClassNotFoundException! : " + e.getMessage());
        }
    }

    private static CodeNode fromStatic0(String outTypeStr, String methodName, JSONObject allParamsInfo) {

        // TODO dočasný, zobecnit !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return TypedDag.mkCodeNode(allParamsInfo, methodName, outTypeStr);
    }

    /**
     * Reflection using approach to defining a building symbol with implementation.
     * @param outTypeStr Fishtron output type.
     * @param homeClassStr Path from CodeNode.CLASS_PREFIX with class name where the method resides.
     * @param methodName Method name.
     * @param args There must be even number of input-type args for fromStatic method. Each pair has the form ('Java class', 'Fishtron type').
     * @return Newly constructed CodeNode (building symbol with implementation) for the static method.
     */
    public static CodeNode fromStatic(String outTypeStr, String homeClassStr, String methodName, String... args) {
        if (args.length % 2 != 0) {throw new Error(
            "There must be even number of input-type args for fromStatic method. "+
            "Each pair has the form (\'Java class\', \'Fishtron type\').");
        }
        if (homeClassStr == null) {throw new Error("Function node must specify homeClass, but it was null.");}

        TypeParser typeParser = new TypeParser();

        Type outType = typeParser.parse(outTypeStr); // TODO Chytat i TypesParseErrory !

        int numIns = args.length / 2;
        Class<?>[] inClasses = new Class[numIns];
        List<Type> inTypes   = new ArrayList<>(numIns);

        try {
            Class<?> homeClass = getClass(homeClassStr);

            for (int i = 0; i < args.length; i+=2) {
                inClasses[i/2] = getClass(args[i]);
                inTypes.add(typeParser.parse(args[i + 1]));
            }

            ProtoNode protoNode = new ProtoNode(methodName, outType, inTypes);
            Comb0 comb = Comb0.fromStatic(methodName, homeClass, inClasses);
            return new CodeNode(protoNode, comb);

        } catch (ClassNotFoundException e) {
            throw new Error("ClassNotFoundException! : "+e.getMessage());
        }
    }


}
