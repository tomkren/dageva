package cz.tomkren.kutil2.core;


import cz.tomkren.kutil2.kobjects.Button;
import cz.tomkren.kutil2.kobjects.Time;
import cz.tomkren.kutil2.kobjects.frame.Frame;

public class KObjectFactory {

    public static KObject newKObject(KAtts kAtts, Kutil kutil) {

        String typeName = kAtts.getString("type");

        // registrace typů KObjectů do programu:
        if ("time"  .equals(typeName)) return new Time  (kAtts, kutil);
        if ("frame" .equals(typeName)) return new Frame (kAtts, kutil);
        if ("button".equals(typeName)) return new Button(kAtts, kutil);

        return new KObject(kAtts, kutil);
    }

    public static KObject newKObject(String xmlString, Kutil kutil){
        XmlLoader loader = new XmlLoader();
        KAtts kAtts = loader.load(XmlLoader.LoadMethod.STRING, "<kutil>"+ xmlString +"</kutil>", kutil);
        return kAtts.get("kutil");
    }

    /**
     * Hojně používaná metoda, která zajišťuje správné vložení nového
     * objektu do systému. TODO (POZN toto je todo ještě ze staré verze !!!): odstranit tuto metodu tak aby vytvoření nového objektu automaticky implikovalo tyto kroky.
     * @param newKObject
     * @param parent
     * @return
     */
    public static KObject insertKObjectToSystem(KObject newKObject, KObject parent, Kutil kutil) {
        newKObject.parentInfo(parent);
        newKObject.init();

        newKObject.resolveCopying();
        kutil.getIdChangeDB().clear();

        return newKObject;
    }
}
