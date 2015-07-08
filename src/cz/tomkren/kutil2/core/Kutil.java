package cz.tomkren.kutil2.core;


import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.Log;
import net.phys2d.raw.shapes.Shape;

import java.util.Random;

/**
 * TODO sem postupně přesunout funkcionalitu Global tak aby byla lokalizovaná a mohlo v pohodě běžet víc instancí.
 */
public class Kutil {

    private IdDB         idDB;
    private IdChangeDB   idChangeDB;
    private Rucksack     rucksack;
    private Scheduler    scheduler;
    private ShapeFactory shapeFactory;
    private XmlMacroFactory xmlMacroFactory;

    private Checker checker;
    private Random   random;

    public static final boolean PERFORM_XML_LOAD_TEST = true;

    public Kutil() {this(null);}

    public Kutil(Long seed) {
        checker = new Checker(seed);
        random = checker.getRandom();
    }

    private void init() {
        idDB            = new IdDB();
        idChangeDB      = new IdChangeDB();
        rucksack        = new Rucksack(this);
        shapeFactory    = new ShapeFactory();
        xmlMacroFactory = new XmlMacroFactory(this);
    }


    public void start(XmlLoader.LoadMethod loadMethod, String loadInput) {

        init();

        XmlLoader loader = new XmlLoader();
        KAtts loaded = loader.load(loadMethod, loadInput, this);

        if (loaded != null) {

            if (!PERFORM_XML_LOAD_TEST) {
                Log.it(loaded);
                new Scheduler(loaded, this);
            } else {

                String xmlString1 = loaded.toXMLString();

                init();

                KAtts loaded2 = loader.load(XmlLoader.LoadMethod.STRING, xmlString1, this);
                String xmlString2 = loaded2.toXMLString();

                checker.eqStrSilent(xmlString1,xmlString2);

                //checker.it(xmlString2);

                new Scheduler(loaded2, this);
            }



        } else {
            Log.it("[XML-ERROR] Chyba při nahrávání, nic se nenahrálo.");
        }

        checker.results();
    }

    public IdDB getIdDB() {return idDB;}
    public IdChangeDB getIdChangeDB() {return idChangeDB;}
    public Rucksack rucksack() {return rucksack;}
    public Scheduler getScheduler() {return scheduler;}
    public Checker getChecker() {return checker;}
    public ShapeFactory shapeFactory() {return shapeFactory;}
    public XmlMacroFactory getXmlMacroFactory() {return xmlMacroFactory;}

    public void setScheduler(Scheduler s) {scheduler = s;}



}
