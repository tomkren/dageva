package cz.tomkren.kutil2.core;

import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.kutil2.kobjects.Text;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO
 */
public class KAtts {

    private Map<String,List<KAttVal>> atts; // seznam vnitřních objektů indexovaný jménem

    public KAtts() {
        atts = new HashMap<>();
    }


    public KAtts add(String tag , KAttVal o) {
        List<KAttVal> list = atts.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            atts.put(tag, list);
        }
        list.add(o);
        return this;
    }


    public void parentInfo(){
        for (List<KAttVal> list : atts.values()) {
            list.stream().filter(o -> o instanceof KObject).forEach(o -> ((KObject) o).parentInfo(null));
        }
    }

    public void init(){
        for (List<KAttVal> list : atts.values()) {
            list.stream().filter(o -> o instanceof KObject).forEach(o -> ((KObject) o).init());
        }
    }


    // --- GETTERS ---

    public KObject get(String tag) {
        List<KAttVal> list = atts.get(tag);
        if (list == null) {return null;}
        if(list.isEmpty()){return null;}
        KAttVal ret = list.get(0);
        return ret instanceof KObject ? (KObject)ret : null;
    }

    public List<KObject> getList(String tag) {
        List<KAttVal> list = atts.get(tag);
        if (list == null) {return new ArrayList<>(0);}
        return list.stream().filter(o -> o instanceof KObject).map(o -> (KObject) o).collect(Collectors.toList());
    }

    public <T> T get_parseFirst(String key, Function<String,T> parse) {
        List<KAttVal> list = atts.get(key);
        if (list == null || list.isEmpty()) {return null;}
        KAttVal o = list.get(0);
        return (o instanceof Text ? parse.apply(o.toString()) : null);
    }

    public <T> T get_tryOrDefault(String key, Function<String,T> getter, T defaultVal) {
        T ret = getter.apply(key);
        return (ret == null ? defaultVal : ret);
    }

    public String getString(String key) {return get_parseFirst(key, str -> str);}
    public String getString(String key, String defaultVal) {return get_tryOrDefault(key, this::getString, defaultVal);}

    public Double getDouble(String key) {return get_parseFirst(key, Double::parseDouble);}
    public Double getDouble(String key, Double defaultVal) {return get_tryOrDefault(key, this::getDouble, defaultVal);}

    public Integer getInteger(String key) {return get_parseFirst(key, Integer::parseInt);}
    public Integer getInteger(String key, Integer defaultVal) {return get_tryOrDefault(key, this::getInteger, defaultVal);}

    public Boolean getBoolean(String key) {return get_parseFirst(key, Boolean::parseBoolean);}
    public Boolean getBoolean(String key, Boolean defaultVal) {return get_tryOrDefault(key, this::getBoolean, defaultVal);}

    public Int2D getInt2D(String key) {return get_parseFirst(key, Int2D::parseInt2D);}
    public Int2D getInt2D(String key, Int2D defaultVal) {return get_tryOrDefault(key, this::getInt2D, defaultVal);}



    public String toXMLString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<kutil>\n");
        for (KObject o : getList("kutil")) {
            sb.append( o.toXml().toString() );
        }
        sb.append("</kutil>\n");
        return sb.toString();
    }

    public String toMacroStr() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,List<KAttVal>> e :atts.entrySet()) {
            sb.append( e.getKey() ).append(" -> ").append( e.getValue() ).append('\n');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toXMLString();
    }

}
