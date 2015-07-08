package cz.tomkren.kutil2.kobjects.frame;


import javax.swing.*;

public class MyInputMap extends InputMap {

    public MyInputMap() {
    }

    public void putNewInput( String str ){
        put( KeyStroke.getKeyStroke( str ) , str );
    }


}