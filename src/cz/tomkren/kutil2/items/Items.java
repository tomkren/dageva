package cz.tomkren.kutil2.items;


import cz.tomkren.kutil2.core.KAtts;
import cz.tomkren.kutil2.core.KObject;

import java.util.ArrayList;
import java.util.List;

public class Items {

    private List<KItem> itemList;

    public Items(){
        itemList = new ArrayList<>();
    }

    public <T> KItem<T> add(String key, T val, T defaultVal) {
        KItem<T> item = new KItem<>(key, val, defaultVal);
        itemList.add(item);
        return item;
    }

    public <T> KItem<T> add(String key, T val) {
        return add(key, val, null);
    }

    public KItem<String> addString(KAtts kAtts, String key, String defaultVal) {
        return add(key, kAtts.getString(key, defaultVal), defaultVal);
    }

    public KItem<Double> addDouble(KAtts kAtts, String key, Double defaultVal) {
        return add(key, kAtts.getDouble(key, defaultVal), defaultVal);
    }

    public KItem<Integer> addInteger(KAtts kAtts, String key, Integer defaultVal) {
        return add(key, kAtts.getInteger(key, defaultVal), defaultVal);
    }

    public KItem<Boolean> addBoolean(KAtts kAtts, String key, Boolean defaultVal) {
        return add(key, kAtts.getBoolean(key, defaultVal), defaultVal);
    }

    public KItem<Int2D> addInt2D(KAtts kAtts, String key, Int2D defaultVal) {
        return add(key, kAtts.getInt2D(key, defaultVal), defaultVal);
    }


    public KItem<List<KObject>> addList(KAtts kAtts, String key) {
        return add(key, kAtts.getList(key));
    }

    public KItem<List<KObject>> addEmptyList(String key) {
        return add(key, new ArrayList<>());
    }


    public void addAttsToXmlElement(XmlElement xmlElement) {
        for (KItem item : itemList) {
            item.addToXmlElement(xmlElement);
        }
    }

}
