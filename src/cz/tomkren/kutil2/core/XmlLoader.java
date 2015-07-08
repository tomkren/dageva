package cz.tomkren.kutil2.core;


import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.kobjects.Text;
import cz.tomkren.kutil2.kobjects.Time;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/** Převádí XML reprezentaci do hierarchie KObjectů. */
public class XmlLoader  implements ContentHandler {


    public static final String OBJECT_TAG = "o";


    private Kutil kutil;

    /** Aktuální stav automatu zpracovávajícího XML soubor. */
    private enum State { init, insideObject , insideTag, insideInnerObject }

    private State state; //Aktuální stav automatu zpracovávajícího XML soubor


    private Stack<State>   stateStack;   //zásobník stavů
    private Stack<String> tagStack;     //zásobník tagů
    private Stack<KAtts>  kAttsStack;   //zásobník s kAtts


    private List<Time> times;    //reference na objevivší se instance Time


    private Locator locator; // Umožnuje zacílit místo v dokumentu, kde vznikla aktuální událost
    private StringBuilder textBuffer; // pomáhá načítat text
    private StringBuilder odsazeni;   // pouze pro ladící účely, textové odsazení

    private static final boolean loguj = false;


    public XmlLoader() {}

    private void init(Kutil kutil) {
        state = State.init;
        this.kutil  = kutil;

        stateStack = new Stack<>();
        tagStack = new Stack<>();
        kAttsStack  = new Stack<>();
        kAttsStack.add( new KAtts() );

        textBuffer  = new StringBuilder();
        odsazeni    = new StringBuilder();

        times       = new ArrayList<>(); //todo zvážit
    }

    public enum LoadMethod {RESOURCE, FILE, STRING}

    public KAtts load(LoadMethod loadMethod, String str, Kutil kutil) {
        switch (loadMethod) {
            case RESOURCE: return loadResource (str, kutil);
            case FILE:     return loadFile     (str, kutil);
            case STRING:   return loadString   (str, kutil);
        }
        return null;
    }

    /**
     * Nahraje interní XML soubor z balíčku kutil.xml
     * @param filename jméno souboru z balíčku kutil.xml
     * @return hierarchie KObjektů zabalená v třídě KAtts
     */
    public KAtts loadResource(String filename, Kutil kutil) {
        try {
            filename = "/cz/tomkren/kutil2/xml/" + filename;
            InputStream inputStream = getClass().getResource(filename).openStream();
            InputSource source = new InputSource(inputStream);
            return loadFromInputSource(source, kutil);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Nahraje externí XML soubor, aktuální adresář je ten kde byl spuštěn program.
     * @param filename jméno souboru
     * @return hierarchie KObjektů zabalená v třídě KAtts
     */
    public KAtts loadFile(String filename, Kutil kutil) {
        try {
            InputSource source = new InputSource( filename );
            return loadFromInputSource(source, kutil);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Nahraje z textového řetězce reprezentujícího XML data.
     * @param str XML data
     * @return hierarchie KObjektů zabalená v třídě KAtts
     */
    public KAtts loadString(String str, Kutil kutil) {
        try {
            InputSource source = new InputSource(new StringReader(str));
            return loadFromInputSource(source, kutil);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private KAtts loadFromInputSource(InputSource source, Kutil kutil) {

        init(kutil);

        try {

            XMLReader parser = XMLReaderFactory.createXMLReader(); // Vytvoříme instanci parseru.
            parser.setContentHandler(this); // Nastavíme náš vlastní content handler pro obsluhu SAX událostí.
            parser.parse( source ); // Zpracujeme vstupní proud XML dat.

            KAtts ret = kAttsStack.peek();

            ret.parentInfo();
            ret.init();

            for (Time t : times) {
                ret.add("times", t);
            }

            return ret;
        }
        catch (XmlLoaderException e) {
            return null;
        }
        catch (SAXException e) {
            Log.it("[" + locator.getLineNumber() + "] XML-ERROR: " + e.getMessage());
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class XmlLoaderException extends SAXException {
        public XmlLoaderException(String msg) {super(msg);}
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        if (state == State.init && !"kutil".equals(localName)) {
            error("Kořenový element musí být <kutil>.");
        }

        stateStack.push(state);

        if (OBJECT_TAG.equals(localName) || "macro".equals(localName)) {

            if (state == State.insideTag) {
                state = State.insideObject;
                kAttsStack.push(new KAtts());
            }
            else if (state == State.insideObject || state == State.insideInnerObject) { //object uvnitř objectu
                state = State.insideInnerObject;
                kAttsStack.push(new KAtts());
            }

            //zpracuji atributy
            KAtts kAtts = kAttsStack.peek();
            for (int i=0; i < atts.getLength(); i++) {
                kAtts.add( atts.getLocalName(i) , new Text(atts.getValue(i)) );
                if(loguj) {Log.it( "[ATTRIBUTE] "+ atts.getLocalName(i) +" : "+ atts.getValue(i) );}
            }

        } else { //TAG

            if (state == State.insideTag) {
                error("Na místo <"+ localName +"> by měl být element <o> nebo text.");
            }

            state = State.insideTag;
            tagStack.push(localName);
        }

        if(loguj) {Log.it( odsazeni + " -> Vstupuji do elementu <" + localName + ">, jsem ve stavu "+ state +"." );}
        odsazeni.append(" ");
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (state == State.insideTag) {
            if (textBuffer.length() != 0) {

                String str = textBuffer.toString().replaceAll("\\s+", " ").trim();
                textBuffer.setLength(0);

                if (!"".equals(str)) {
                    Text text = new Text(str);
                    if(loguj) {Log.it( odsazeni + "[text] " + text );}
                    kAttsStack.peek().add(tagStack.peek(), text);
                }
            }
            tagStack.pop();

        } else if (state == State.insideObject || state == State.insideInnerObject) {

            if(loguj) {Log.it("[vytvářím OBJECT a vkládám ho do tagu "+ tagStack.peek() +"]");}

            KAtts kAttsProNovyObject = kAttsStack.pop();

            KObject newKObject = null;

            if (OBJECT_TAG.equals(localName)) {
                newKObject = KObjectFactory.newKObject(kAttsProNovyObject, kutil);
            } else if ("macro".equals(localName)) {

                //Log.it("... MACRO ...................................");
                //Log.it( kAttsProNovyObject.toMacroStr() );
                //Log.it(".............................................");

                String xmlCreatedByMacro = kutil.getXmlMacroFactory().getXmlString(kAttsProNovyObject);

                if (xmlCreatedByMacro == null) {
                    error("MACRO (respektive xmlMacroFactory) vrátilo null místo XML stringu.");
                } else {
                    newKObject = KObjectFactory.newKObject(xmlCreatedByMacro, kutil);
                }
            }

            String id = kAttsProNovyObject.getString("id");
            if (id != null) {
                kutil.getIdDB().put(id, newKObject);
                if(loguj) {Log.it("přidáno id "+id);}
            }

            String tag = (state == State.insideObject ? tagStack.peek() : "inside");

            kAttsStack.peek().add(tag, newKObject);

            if (newKObject instanceof Time) {
                times.add((Time)newKObject);
            }


        }

        state = stateStack.pop();

        if(loguj)Log.it( odsazeni + "<- Vystupuji z elementu <" + localName + ">, jsem ve stavu "+ state +"."+
                (state == State.insideTag ? " Uvnitř <"+ tagStack.peek()+">." : "" ) );
        odsazeni.deleteCharAt(0);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        textBuffer.append(ch, start, length);
    }

    private void error(String msg) throws XmlLoaderException {
        Log.it("[" + locator.getLineNumber() + "] XML-ERROR: " + msg);
        throw new XmlLoaderException(msg);
    }

    @Override public void startDocument() throws SAXException {}
    @Override public void endDocument() throws SAXException {}
    @Override public void startPrefixMapping(String prefix, String uri) throws SAXException {}
    @Override public void endPrefixMapping(String prefix) throws SAXException {}
    @Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
    @Override public void processingInstruction(String target, String data) throws SAXException {}
    @Override public void skippedEntity(String name) throws SAXException {}



}
