package cz.tomkren.kutil2.items;


import cz.tomkren.kutil2.core.KObject;

import java.util.ArrayList;
import java.util.List;

public class XmlElement implements Xml {

    private String tag;
    private List<Xml> inside;
    private List<StringString> atts;


    public XmlElement(String tag) {
        this.tag = tag;
        inside   = new ArrayList<>();
        atts     = new ArrayList<>();
    }

    public void addInside(Xml xml) {
        inside.add(xml);
    }

    public void add(String key, Object value) {

        if (value == null) return;

        if (value instanceof List) {

            List list = (List) value;

            if (list.isEmpty()) return;

            XmlElement el = new XmlElement(key);

            for (Object o : list) {

                if (o instanceof KObject) {
                    el.addInside(((KObject) o).toXml());
                } else {
                    throw new Error("List should be List<KObject>.");
                }

            }

            addInside(el);


        } else if(value instanceof KObject) {

            XmlElement el = new XmlElement(key);
            el.addInside(((KObject) value).toXml());
            addInside(el);

        } else {
            atts.add(new StringString(key,value.toString()));
        }

    }

    private class StringString {
        public String s1;
        public String s2;

        public StringString( String att , String val ){
            s1 = att;
            s2 = val;
        }

        @Override
        public String toString() {
            return s1+"=\""+s2+"\"";
        }
    }


    @Override
    public String toString() {
        return toStringOdsad(0,true);
    }

    private String toStringOdsad(int ods, boolean vypisTag) {

        StringBuilder sb = new StringBuilder();

        if (vypisTag) {

            sb.append(ods(ods)).append("<").append(tag);
            for (StringString ss : atts) {
                sb.append(" ").append(ss.toString());
            }
        }

        if (!inside.isEmpty()) {

            if (vypisTag) {sb.append( ">\n" );}

            for (Xml xml : inside) {
                if (xml instanceof XmlText) {
                    sb.append(ods(ods + 1)).append( xml.toString());
                } else {

                    XmlElement elem = (XmlElement) xml;

                    if(elem.tag.equals("inside")) {
                        sb.append( elem.toStringOdsad(ods, false) );
                    } else {
                        sb.append( elem.toStringOdsad(ods+1, true) );
                    }

                }
            }

            if (vypisTag) {
                sb.append(ods(ods)).append("</").append(tag).append( ">");
            }
        } else if(vypisTag) {
            sb.append("/>");
        }

        if (vypisTag) {sb.append("\n");}

        return sb.toString();
    }



    private String ods( int ods ){
        StringBuilder ret = new StringBuilder();
        for( int i=0 ; i<ods; i++ ){
            ret.append("  ");
        }
        return ret.toString();
    }

}
