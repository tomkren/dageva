package cz.tomkren.kutil2.core;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO Nejdříve revidujeme IdDB držící objekty pomocí jejich jmen...
 */

public class IdDB {

    private Map<String,KObject> tab;
    private int nextId;

    public IdDB(){
        tab    = new HashMap<>();
        nextId = 1;
    }

    public void put(String id, KObject o) {
        tab.put(id, o);
    }

    public KObject get(String id) {
        if (id == null) return null;
        return tab.get(id);
    }

    public void remove(String id) {
        tab.remove(id);
    }


    public String getUniqueID() {
        String candidate = "$" + nextId;
        if (tab.containsKey(candidate)) {
            nextId ++;
            return getUniqueID();
        }
        return candidate;
    }

}
