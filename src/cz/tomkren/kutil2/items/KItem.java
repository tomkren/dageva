package cz.tomkren.kutil2.items;

public class KItem<T> {

    private String  key;
    private T       val;

    private T defaultVal;

    public KItem(String key, T val, T defaultVal) {
        this.key = key;
        this.val = val;
        this.defaultVal = defaultVal;
    }

    public KItem(String key, T val) {
        this(key, val, null);
    }


    public void set(T newVal) {val = newVal;}
    public T get() {return val;}

    public void addToXmlElement(XmlElement xmlElement) {
        if (val == null) {return;}
        if (!val.equals(defaultVal)) {
            xmlElement.add(key, val);
        }
    }



}
