package cz.tomkren.kutil2.items;


public class XmlText implements Xml {

    private String text;

    public XmlText(String text){
        this.text = text;
    }

    @Override
    public String toString() {
        return text + '\n';
    }


}
