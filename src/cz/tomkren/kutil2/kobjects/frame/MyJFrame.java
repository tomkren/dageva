package cz.tomkren.kutil2.kobjects.frame;


import cz.tomkren.kutil2.core.Kutil;
import cz.tomkren.kutil2.items.Int2D;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

public class MyJFrame extends JFrame implements WindowListener, ComponentListener {

    protected MyJPanel myJPanel;
    private   Frame    frame;

    public MyJFrame (Frame frame) {
        super(frame.getTitle());

        this.frame = frame;

        addWindowListener(this);
        addComponentListener(this);

        myJPanel = new MyJPanel(frame);
        myJPanel.setPreferredSize(new Dimension(frame.getSize().getX(), frame.getSize().getY()));
        myJPanel.setBackground(Color.white);

        add(myJPanel);

        pack();
        setResizable(true);
        setLocation(frame.pos().getX(), frame.pos().getY());

        // setIconImage(Global.shapeFactory().appleImg);  // TODO ikonka !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        setVisible(true);
    }

    public void windowClosing       (WindowEvent e)    {
        dispose();
        frame.kutil().getScheduler().terminate();
    }
    public void windowActivated     (WindowEvent e)    { }
    public void windowDeactivated   (WindowEvent e)    { }
    public void windowDeiconified   (WindowEvent e)    { }
    public void windowIconified     (WindowEvent e)    { }
    public void windowClosed        (WindowEvent e)    { }
    public void windowOpened        (WindowEvent e)    { }

    public void componentHidden     (ComponentEvent e) { }
    public void componentMoved      (ComponentEvent e) {
        int x = (int) e.getComponent().getLocation().getX();
        int y = (int) e.getComponent().getLocation().getY();
        frame.setPos(new Int2D(x, y));
    }
    public void componentShown      (ComponentEvent e) { }
    public void componentResized    (ComponentEvent e) { }
}