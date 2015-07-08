package cz.tomkren.kutil2.kobjects.frame;


import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.items.Int2D;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;

public class MyJPanel extends JPanel implements ComponentListener, MouseInputListener, MouseWheelListener {

    private Frame frame;
    private MyActionMap myActionMap;
    private MyInputMap myInputMap;

    private Image image;
    private Graphics2D graphics2D;
    private static final RenderingHints renderingHints =
            new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


    public MyJPanel( Frame f ){
        frame = f;

        myInputMap = new MyInputMap();

        myInputMap.setParent(getInputMap(JComponent.WHEN_FOCUSED));
        setInputMap(JComponent.WHEN_FOCUSED, myInputMap );

        myActionMap = new MyActionMap(frame);

        setActionMap(myActionMap);

        String[] keyNames = frame.kutil().rucksack().getKeyNames();
        for (String keyName : keyNames) {
            registerKeyboardEvent(keyName);
        }

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    private void resetImage(){

        image = createImage( frame.getSize().getX() , frame.getSize().getY() );

        if (image == null) {
            System.out.println("dbImage is null");
        }
        else{
            graphics2D = (Graphics2D) image.getGraphics();
            graphics2D.setRenderingHints(renderingHints);
            graphics2D.setClip(0, 0, frame.getSize().getX() , frame.getSize().getY() );
        }
    }

    public void paintScreen(){

        gameRender();

        Graphics g;
        try {
            g = this.getGraphics();
            if ((g != null) && (image != null))
                g.drawImage(image, 0, 0, null);
            if (g != null) {
                g.dispose();
            }
        }
        catch (Exception e)
        { System.out.println("Graphics context error: " + e);  }
    }

    private void gameRender()
    {
        if (image == null){
            resetImage();
        }

        if( graphics2D == null ){
            Log.it("CHYBA JE V TOM že graphics2D == null !!!");
            return;
        }

        graphics2D.setColor(Color.white);
        graphics2D.fillRect(0, 0, frame.getSize().getX(), frame.getSize().getY());

        frame.paintScreen(graphics2D);
        frame.kutil().rucksack().paintScreen(graphics2D);
    }

    public final void registerKeyboardEvent( String event ){
        myActionMap.putNewAction(event);
        myInputMap.putNewInput(event);
    }


    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        frame.mouseWheel(e);
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        frame.mouseClicked(e);
    }

    private int dragHelpX, dragHelpY;
    public void mousePressed(MouseEvent e) {
        frame.mousePressed(e);
        dragHelpX = e.getX() ;
        dragHelpY = e.getY() ;
    }

    public void mouseReleased(MouseEvent e){
        frame.mouseReleased(e);
    }

    public void mouseDragged(MouseEvent e) {
        frame.mouseDragged( e , new Int2D( e.getX() - dragHelpX , e.getY() - dragHelpY  ) );
        dragHelpX = e.getX();
        dragHelpY = e.getY();
    }

    public void mouseMoved(MouseEvent e)   {
        frame.mouseMoved(e);
    }


    public void mouseEntered(MouseEvent e) {  }
    public void mouseExited(MouseEvent e)  {  }


    public void componentHidden     (ComponentEvent e) { }
    public void componentMoved      (ComponentEvent e) { }
    public void componentShown      (ComponentEvent e) { }

    public void componentResized    (ComponentEvent e) {
        frame.resetSize(e.getComponent().getWidth(), e.getComponent().getHeight());
        resetImage();
    }


}
