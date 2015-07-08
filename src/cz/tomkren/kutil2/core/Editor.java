package cz.tomkren.kutil2.core;


import cz.tomkren.kutil2.items.Int2D;

import javax.swing.*;
import java.awt.*;

/**
 * Třída sprostředkovávající komunikaci s oknem pro zobrazení XML reprezentace KObjectu, případně
 * Kispové reprezentace, nebo obecně jakéhokoli textu.
 */

public class Editor extends JPanel {
    protected JTextArea textArea;
    private Int2D loc;

    private static final Font  font = new Font( Font.MONOSPACED , Font.PLAIN , 12 );


    /**
     * vytvoří nové okno editoru
     * @param loc pozice okna na obrazovce
     * @param text text který bude zobrazen
     */
    public Editor(Int2D loc, String text) {
        super(new GridBagLayout());

        this.loc = loc;

        textArea = new JTextArea(20, 80);
        textArea.append( text );

        textArea.setFont(font);

        JScrollPane scrollPane = new JScrollPane(textArea);

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(scrollPane, c);

        createAndShowGUI();
    }


    private void createAndShowGUI() {
        JFrame frame = new JFrame("Editor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setLocation(loc.getX(),loc.getY());
        frame.add(this);

        frame.pack();
        frame.setVisible(true);
    }


}