package cz.tomkren.kutil2.kobjects.frame;


import javax.swing.*;
import java.awt.event.ActionEvent;

public class MyActionMap extends ActionMap {

    private Frame frame;

    public MyActionMap(Frame f) {frame = f;}

    public void putNewAction(String str) {
        final String str2 = str;
        put( str ,
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        frame.kutil().rucksack().keyboardEvent(str2);
                    }
                }
        );
    }
}