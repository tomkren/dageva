package cz.tomkren.kutil2.core;


import cz.tomkren.typewars.dag.DagGeneratorMacro;
import cz.tomkren.typewars.dag.TypedDagGeneratorMacro;

public class XmlMacroFactory {

    private Kutil kutil;

    public XmlMacroFactory(Kutil kutil) {
        this.kutil = kutil;
    }

    public String getXmlString(KAtts kAtts) {

        String type = kAtts.getString("type");

        switch (type) {
            case "DAG-generator" : return DagGeneratorMacro.mkXML(kAtts);
            case "TypedDagGenerator" : return TypedDagGeneratorMacro.mkXML(kAtts);
        }

        return null;
    }



}
