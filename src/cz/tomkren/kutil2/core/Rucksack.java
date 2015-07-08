package cz.tomkren.kutil2.core;

import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.kutil2.kevents.Cmd;
import cz.tomkren.kutil2.kobjects.frame.Frame;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TODO
 */
public class Rucksack implements ActionListener {

    private Kutil kutil;

    private boolean    isSimulationRunning; // zda běží simulace

    private boolean    showInfo;        // zda ukazovat u objektů stručné info
    private KObject    selected;        // označený KObjekt - projevuje se červeným okrajem
    private Frame      selectedFrame;   // označený Frame   - projevuje se zeleným okrajem


    private KObject    onCursor;      // KObject vysící na kurzoru
    private Int2D      onCursorClickPos; //relativní pozice kliknutí vůči pozici KObjectu na kurzoru
    private boolean    lastTimeCut; //zda bylo naposledy vyjmuto (HAX)

    private Int2D      onDragAbsPos; // absolutní pozice kurzoru při přesouvání
    private Int2D      onDragPos;    // relativní pozice kurzoru při přesouvání

    private JFrame popupFrame;       // reference na pomocný frame při vytváření popup okna (pravé tlačítko)

    private int mouseX; // x-pozice myši
    private int mouseY; // y-pozice myši


    private KObject main; // reference na KObject do kterého se nahrává ze souboru a u kterého se zaznamenává editační historie
    private LinkedList<String> undoBuffer; //buffer editační historie směrem do minulosti
    private LinkedList<String> redoBuffer; //buffer editační historie směrem do budoucnosti

    private boolean    brushInsertMode;

    private KObject    inClipboard; // KObject ve schránce



    public Rucksack(Kutil kutil){

        this.kutil = kutil;

        selected            = null;
        selectedFrame       = null;
        showInfo            = false;
        onCursor            = null;
        popupFrame          = null;
        isSimulationRunning = false;
        brushInsertMode     = false;

        inClipboard = null;


        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


        console             = null;
        actualFigure        = null;


        fileChooser         = new JFileChooser(  );
        try{
            File f = new File(new File(".").getCanonicalPath());
            fileChooser.setCurrentDirectory(f);
        } catch(Exception e){}

        */

        undoBuffer = new LinkedList<>(); // todo zvážit
        redoBuffer = new LinkedList<>(); // todo zvážit

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        resetFrom();
        */
    }




    public void setMain(KObject o) {main = o;}

    private void saveStateToUndoBuffer() {
        if (main != null) {
            undoBuffer.addFirst(main.toXml().toString());
            redoBuffer.clear();
        }
    }

    private void loadToMain(String xmlString) {
        KObject parent = main.parent();

        main.delete();

        KObject o = KObjectFactory.newKObject(xmlString,kutil);
        parent.add(o);
        o.setParent(parent);

        Set<String> affectedIds = o.getIds();

        kutil.getScheduler().forEachTime(t -> t.resolveCopying_2(affectedIds));




        isSimulationRunning = false;
    }


    public boolean isSimulationRunning() {return isSimulationRunning;}
    public boolean showInfo() {return showInfo;}


    /**
     * Provede globální příkaz.
     * @param cmd textová forma příkazu
     */
    public List<String> cmd( String cmd ){
        String[] cmds = cmd.split(";");

        List<String> ret = new ArrayList<>();

        for (String cmd1 : cmds) {
            ret.add(oneCmd(cmd1));
        }

        Log.list(ret); // TODO dočasné

        return ret;
    }

    private String oneCmd( String cmd ){
        //Log.it( "[cmd]: "+ cmd );

        cmd = cmd.trim();

        String[] parts = cmd.split( "\\s+" );
        String cmdName = parts[0];

        Log.it(cmd); // TODO dočasné

        if (Cmd.play.equals(cmdName)) {
            togglePausePlay();
            return "Přepnuto.";
        }
        if (Cmd.undo.equals(cmdName)) {
            undo();
            return "Undone.";
        }
        if( Cmd.redo.equals(cmdName) ){
            redo();
            return "Redone.";
        }
        if (Cmd.changeInsertMode.equals(cmdName)) {
            changeInsertMode();
            return "Mód vkládání změněn.";
        }
        if( Cmd.showInfo.equals(cmdName) ){
            toggleShowInfo();
            return "Přepnuto.";
        }

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if( Cmd.sendTo.equals(cmdName) ){
            if( parts.length < 3 ) return "U sendTo musí být id a příkaz.";
            KObject o = Global.idDB().get( parts[1] );
            if( o != null ){
                return o.cmd( cmd.split("\\s+" , 3)[2] );
            }
            return parts[1] + " není platné id.";
        }
        if( Cmd.changeXML.equals(cmdName) ){
            if( parts.length != 3 ) return Cmd.changeXML+" potřebuje 2 argumenty.";
            changeXML( parts[1] , parts[2] , true );
            return "XML změněno.";
        }
        if( Cmd.console.equals(cmdName) ){
            openConsole();
            return "Konzole otevřena.";
        }
        if( Cmd.rename.equals(cmdName) ){
            if( parts.length != 3 ) return Cmd.rename+" potřebuje 2 argumenty.";
            return ( rename( parts[1], parts[2] )
                    ? "Povedlo se přejmenovat."
                    : "Nepovedlo se přejmenovat." );
        }
        if( Cmd.load.equals(cmdName) ){
            return load();
        }
        if( Cmd.save.equals(cmdName) ){
            return save();
        }
        if( Cmd.xml.equals(cmdName) ){
            if( parts.length < 2 ) return "U "+Cmd.xml + " musí být argument (vkládané xml).";
            String xml = cmd.split("\\s+" , 2)[1] ;
            fromXmlToCursor(xml);
            return "XML (snad) vloženo jako KObject.";
        }
        if( Cmd.bgcolor.equals(cmdName) ){
            if( parts.length < 3 ) return "U "+Cmd.bgcolor + " musí být 2 argumenty: [id] [color r g b]";
            String[] ps = cmd.split("\\s+" , 3);
            return changeBgcolor( ps[1] , ps[2] );
        }
        if( Cmd.selectedFrameTarger.equals(cmdName) ){
            if( parts.length < 2 ) return "U "+Cmd.selectedFrameTarger + " musí být 1 argumenty: [id]";
            return changeActualFrameTarget(parts[1]);
        }
        */

        return "Neplatný příkaz.";
    }

    private void togglePausePlay(){
        isSimulationRunning = !isSimulationRunning;
        if (isSimulationRunning) {
            saveStateToUndoBuffer();
        }
    }

    private void undo() {
        if (main != null && (!undoBuffer.isEmpty())) {

            String lastState = undoBuffer.getFirst();
            undoBuffer.removeFirst();
            redoBuffer.addFirst(main.toXml().toString());

            loadToMain(lastState);
        }
    }

    private void redo() {
        if (main != null && (!redoBuffer.isEmpty())) {

            String nextState = redoBuffer.getFirst();
            redoBuffer.removeFirst();
            undoBuffer.addFirst( main.toXml().toString() );

            loadToMain(nextState);
        }
    }

    private void changeInsertMode(){
        brushInsertMode = ! brushInsertMode;
    }







    public void setSelected(KObject o) {
        if( selected != null ){
            selected.setHighlighted(false);
        }

        selected = o;
        selected.setHighlighted(true);

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if( selected instanceof Figure ){
            setActualFigure((Figure)selected);
        }

        if( ! ((selected instanceof Inputable) || (selected instanceof Outputable) ) ){
            resetFrom();
        }
        */
    }

    public Frame getSelectedFrame() {return selectedFrame;}

    public void setSelectedFrame(Frame f) {
        if (selectedFrame != null) {
            selectedFrame.setHighlightedFrame(false);
        }
        selectedFrame = f;
        selectedFrame.setHighlightedFrame(true);
    }




    /**
     * Metoda umožňující rucksacku vykreslit to co je na kurzoru do GUI.
     * @param g Graphics2D kam se bude kreslit
     */
    public void paintScreen(Graphics2D g) {
        try{
            if (lastTimeCut && onCursor != null) {

                Int2D center    = onDragAbsPos.minus(onDragPos);
                Int2D objectPos = onDragPos.plus(onCursorClickPos);

                onCursor.setPos( objectPos );
                onCursor.drawOutside(g, center , 0, 1.0, center); // todo opravdu dobře zoom center?

            }
            else if(!lastTimeCut && onCursor != null) {

                onCursor.setPos( new Int2D(mouseX, mouseY) );
                onCursor.drawOutside(g, Int2D.zero , 0, 1.0, Int2D.zero); // todo opravdu dobře zoomCenter ?

            }
        }
        catch(NullPointerException e){
            Log.it("Chycena výjimka při vykreslení, nevadí.");
        }
    }




    /**
     * Zpřístupňuje kódy všech použitých kláves.
     * @return pole kódů kláves
     */
    public String[] getKeyNames(){
        return keyNames;
    }

    private static final String[] keyNames =
            new String[]{ "ENTER", "BACK_SPACE", "I", "DELETE", "P" , "SPACE" ,
                    "control Z","control shift Z" , "F2" , "control R",
                    "LEFT","RIGHT","UP","DOWN", "B" ,
                    "control LEFT","control RIGHT","control UP","control DOWN",
                    "shift LEFT","shift RIGHT","shift UP","shift DOWN",
                    "control shift LEFT","control shift RIGHT","control shift UP","control shift DOWN"
            };



    /**
     * Implementuje reagování na zmačknutí klávesy (nebo kombinace kláves).
     * @param str kód klávesy
     */
    public void keyboardEvent(String str) {

        if     ( "ENTER"                .equals(str) ){ enter     ();      }
        else if( "BACK_SPACE"           .equals(str) ){ backspace ();      }
        else if( "I"                    .equals(str) ){ toggleShowInfo();  }
        else if( "DELETE"               .equals(str) ){ delete();          }
        else if( "P"                    .equals(str) ){ togglePausePlay(); }
        else if( "control Z"            .equals(str) ){ undo();
        } else if ("control shift Z"      .equals(str) ){ redo();            }
        else   { Log.it("ERR: neodchycená klávesa"); }

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        else if( "SPACE"                .equals(str) ){ openConsole();     }
        else if( "F2"                   .equals(str) ||
                "control R"            .equals(str) ){ renameDialog();    }
        else if( "LEFT"                 .equals(str) ){ figureCmd(Figure.FigureCmd.left);  }
        else if( "RIGHT"                .equals(str) ){ figureCmd(Figure.FigureCmd.right); }
        else if( "UP"                   .equals(str) ){ figureCmd(Figure.FigureCmd.up);    }
        else if( "DOWN"                 .equals(str) ){ figureCmd(Figure.FigureCmd.down);  }
        else if( "control LEFT"         .equals(str) ){ allFigureCmd(Figure.FigureCmd.left);  }
        else if( "control RIGHT"        .equals(str) ){ allFigureCmd(Figure.FigureCmd.right); }
        else if( "control UP"           .equals(str) ){ allFigureCmd(Figure.FigureCmd.up);    }
        else if( "control DOWN"         .equals(str) ){ allFigureCmd(Figure.FigureCmd.down);  }
        else if( "shift LEFT"           .equals(str) ){ figureCmd(Figure.FigureCmd.shiftLeft);  }
        else if( "shift RIGHT"          .equals(str) ){ figureCmd(Figure.FigureCmd.shiftRight); }
        else if( "shift UP"             .equals(str) ){ figureCmd(Figure.FigureCmd.shiftUp);    }
        else if( "shift DOWN"           .equals(str) ){ figureCmd(Figure.FigureCmd.shiftDown);  }
        else if( "control shift LEFT"   .equals(str) ){ allFigureCmd(Figure.FigureCmd.shiftLeft);  }
        else if( "control shift RIGHT"  .equals(str) ){ allFigureCmd(Figure.FigureCmd.shiftRight); }
        else if( "control shift UP"     .equals(str) ){ allFigureCmd(Figure.FigureCmd.shiftUp);    }
        else if( "control shift DOWN"   .equals(str) ){ allFigureCmd(Figure.FigureCmd.shiftDown);  }
        else if( "B"                    .equals(str) ){ changeFigure();  }

        //KeyEvent.VK_
        */

    }

    private void enter() {
        if (selected == null || selectedFrame == null) return;
        if (selected.getIsGuiStuff()) return;

        saveStateToUndoBuffer();

        selectedFrame.resetTarget(selected.id());
        selectedFrame.setCam(Int2D.zero());

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String cmd = selected.getOnEnterCmd();
        if(cmd != null) {
            cmd(cmd);
        }
        */
    }

    private void backspace(){
        if( selectedFrame                      == null ) return;
        if( selectedFrame.getTarget().parent() == null ) return;

        if( selectedFrame.getTarget().getIsGuiStuff() ) return;

        saveStateToUndoBuffer();

        selectedFrame.resetTarget( selectedFrame.getTarget().parent().id() );
        selectedFrame.setCam(Int2D.zero());
    }

    private void toggleShowInfo(){
        showInfo = !showInfo;
    }

    private void delete(){
        if( selected == null ) return;
        if( selected == selectedFrame ) return;
        if( selected.getIsGuiStuff() ) return;

        saveStateToUndoBuffer();

        selected.delete();

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (selected == actualFigure) {
            changeFigure(actualFigure);
        }
        */

        selected = null;
    }




    /**
     * Informuje rucksack o tom, že bylo kliknuto pravé tlačítko, aby
     * věděl že má otevřít popup.
     * @param frame Frame ve kterém bylo kliknuto
     * @param mouseX x-ová pozice myši na obrazovce
     * @param mouseY y-ová pozice myši na obrazovce
     */
    public void clickedRightMouseButton(Frame frame, int mouseX , int mouseY) {

        if (onCursor != null) {
            onCursor = null;
            return;
        }

        if (popupFrame != null) {
            popupFrame.dispose();
        }

        popupFrame = new JFrame("popup");
        popupFrame.setLocation(0, 0);
        popupFrame.setVisible(true);

        JPopupMenu popup = new JPopupMenu();


        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if( selected instanceof Function ){
            addItemToPopup("add in and out", popup);
        }

        if( selected.getBasic().getIsBrick()  ){
            boolean hasBrick = false;
            for( KObject o : selected.parent().inside() ){
                if( o.getBasic().getIsBrick() ){
                    hasBrick = true;
                }
            }

            if(hasBrick) { addItemToPopup("glue bricks", popup); }
        }
        */

        addItemToPopup("undo", popup);
        addItemToPopup("copy", popup);
        addItemToPopup("paste to cursor", popup);
        addItemToPopup("copy to cursor", popup);
        addItemToPopup("console", popup);
        addItemToPopup("show XML", popup);
        addItemToPopup("show Kisp", popup);
        Point p = frame.getJPanel().getLocationOnScreen();
        Int2D rightClickLocation = new Int2D(p.x+mouseX, p.y+mouseY);
        popup.show(popupFrame, rightClickLocation.getX(), rightClickLocation.getY()  );


    }

    private void addItemToPopup(String cmd, JPopupMenu popup) {
        JMenuItem menuItem = new JMenuItem( cmd );
        menuItem.addActionListener(this);
        menuItem.setActionCommand(cmd);
        popup.add(menuItem);
    }

    /**
     * Pro implemetaci ActionListener.
     * @param e ActionEvent
     */
    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();

        if( "copy".equals(cmd) ){
            inClipboard = selected;
        }
        else if( "undo".equals(cmd) ){
            undo();
        }
        else if( "paste to cursor".equals(cmd) ){
            copyToCursor(inClipboard);
        }
        else if("copy to cursor".equals(cmd)){
            copyToCursor(selected);
        }
        else if( "show XML".equals(cmd) ){
            openEditor( selected.toXml().toString() );
        }
        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        else if( "console".equals(cmd) ){
            openConsole();
        }
        else if( "show Kisp".equals(cmd) ){
            openEditor( selected.toKisp() );
        }
        else if( "add in and out".equals(cmd) ){
            ((Function)selected).addInAndOut();
        }
        else if( "glue bricks".equals(cmd) ){
            selected.getBasic().glueBricks();
        }
        */
        else {
            Log.it("Unimplemented right-click action.");
        }

        if( popupFrame != null ){
            popupFrame.dispose();
        }
    }


    private void openEditor(String str) {
        Editor ed = new Editor( new Int2D(100,100) , str );
    }

    /**
     * Odpovídá na otázku, zda právě něco visí na kurzoru.
     * @return odpověď na otázku , zda právě něco visí na kurzoru.
     */
    public boolean somethingOnCursor(){
        return onCursor != null;
    }

    /**
     * zkopíruje KObjekt na kurzor.
     * @param o co se má zkopírovat
     */
    public void copyToCursor(KObject o) {
        if (onCursor != null) return;

        KObject copy = o.copy();

        KObjectFactory.insertKObjectToSystem(copy, null, kutil);

        onCursor         = copy;
        onCursorClickPos = Int2D.zero;

        setSelected(onCursor);

        lastTimeCut = false;
    }

    /**
     * Vyjmout na kurzor.
     * @param o co se má vyjmout
     * @param clickPos pozice kliknutí
     */
    public void cutToCursor( KObject o , Int2D clickPos ){
        if(onCursor != null) return;

        KObject parent = o.parent();
        if (parent == null) {return;}

        saveStateToUndoBuffer();

        parent.remove(o);
        onCursor = o;
        onCursorClickPos = onCursor.pos().minus(clickPos);

        setSelected(onCursor);

        lastTimeCut = true;
    }

    /**
     * Vlož z kurzoru do virtuálního světa.
     * @param newParent nový rodičovský KObbject
     * @param clickPos pozice kam bylo kliknuto pro vložení
     */
    public void pasteFromCursor( KObject newParent , Int2D clickPos ){
        if( onCursor == null ) return;

        Int2D pos = clickPos.plus(onCursorClickPos);

        /* TODO !!!
        if( ((Basic)onCursor).getAlign15() ){
            pos = pos.align(15);
        }
        */

        onCursor.setPos(pos);
        onCursor.setParent(newParent);
        newParent.add(onCursor);

        onCursor.setSpeed(Int2D.zero);

        KObject oldOnCursor = onCursor;

        onCursor = null;

        if (brushInsertMode) {
            copyToCursor(oldOnCursor);
        }
    }



    /**
     * Metoda volaná pro informování rucksacku o relativní pozici myši při táhnutí
     * @param mousePos pozice myši při táhnutí
     */
    public void onDrag(Int2D mousePos){
        onDragPos = mousePos;
    }
    /**
     * Metoda volaná pro informování rucksacku o absolutní pozici myši při táhnutí.
     * @param e MouseEvent způsobený táhnutím myši
     */
    public void onDrag(MouseEvent e){
        onDragAbsPos = new Int2D( e.getX(), e.getY() );
    }
    /**
     * Metoda volaná pro informování rucksacku o kliknutí, aby zavřel popup.
     * @param e MouseEvent
     */
    public void onClick( MouseEvent e ){
        if( popupFrame != null ){
            popupFrame.dispose();
        }
    }
    /**
     * Metoda volaná pro informování rucksacku o aktalní pozici myši.
     * @param e MouseEvent
     */
    public void onMove( MouseEvent e ){
        mouseX = e.getX();
        mouseY = e.getY();
    }










}
