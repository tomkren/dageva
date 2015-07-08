package cz.tomkren.typewars;


import cz.tomkren.helpers.Comb0;
import cz.tomkren.helpers.F;
import cz.tomkren.typewars.basicgen.Generator;
import org.json.JSONObject;

import java.util.*;

//todo možná udělat že implements Lib a tu dát i k NodeLib kterou přejmenovat na ProtoLib

public class CodeLib extends NodeLib {

    Map<String,CodeNode> codeMap = new HashMap<>();

    public  CodeLib(List<CodeNode> lib) {
        super(F.map(lib,x->x));
        lib.forEach(node -> codeMap.put(node.getName(), node));
    }

    public CodeLib(CodeNode... lib) {
        this(Arrays.asList(lib));
    }

    public Comb0 getCode(String name) {
        CodeNode codeNode = getCodeNode(name);
        return codeNode == null ? null : codeNode.getCode();
    }

    public CodeNode getCodeNode(String name) {
        return codeMap.get(name);
    }

    public List<CodeNode> getCodeNodes() {
        return F.map( getNodeList() , n -> (CodeNode)n );
    }

    public static CodeLib mk(String semicolonSeparatedLines) {
        return mk(semicolonSeparatedLines.split(";"));
    }

    public static CodeLib mk(String... codeNodeLines) {
        return mk(new JSONObject(), codeNodeLines);
    }

    public static CodeLib mk(JSONObject allParamsInfo, String... codeNodeLines) {
        List<CodeNode> libList = new ArrayList<>(codeNodeLines.length);
        for (String line : codeNodeLines) {
            String trimLine = line.trim();
            if (!"".equals(trimLine)) {
                libList.add(CodeNode.mk(trimLine, allParamsInfo));
            }
        }
        return new CodeLib(libList);
    }

    public List<PolyTree> basicGenerate(String goal, int n) {
        return Generator.generate(goal, this, n);
    }

    public List<PolyTree> basicGenerate(Type goal, int n) {
        return Generator.generate(goal, this, n);
    }



}


