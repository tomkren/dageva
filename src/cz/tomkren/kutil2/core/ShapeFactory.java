package cz.tomkren.kutil2.core;


import cz.tomkren.helpers.AB;
import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.kutil2.shapes.FunctionShape;
import cz.tomkren.kutil2.shapes.RectangleShape;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ShapeFactory {


    public KShape newKShape(String shapeCmd, Kutil kutil) {

        if (shapeCmd == null) {
            return mkDefaultShape();
        }

        String[] parts = shapeCmd.split( "\\s+" );
        String shapeName = parts[0];

        if ("rectangle".equals(shapeName)) {

            if (parts.length == 4) {

                int x     = Integer.parseInt(parts[1]);
                int y     = Integer.parseInt(parts[2]);
                Color col = Color.PINK;

                try {
                    col = Color.decode(parts[3]);}
                catch(NumberFormatException e){
                    Log.it("Incorrect color code : " + e.getMessage());
                }

                return new RectangleShape( new Int2D( x,y ), col );
            }

            if( parts.length != 3 ) return mkDefaultShape();
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            return new RectangleShape( new Int2D( x,y ), Color.black );
        }

        if ("f".equals(shapeName)) {

            int numInputs  = 1;
            int numOutputs = 1;
            String name    = null;
            List<AB<String,Integer>> targets = null;

            if (parts.length > 1) {numInputs  = Integer.parseInt(parts[1]);}
            if (parts.length > 2) {numOutputs = Integer.parseInt(parts[2]);}
            if (parts.length > 3) {name = parts[3];}

            if (parts.length > 4) {
                targets = new ArrayList<>();
                for (int i = 4; i<parts.length; i++) {

                    if ("null".equals(parts[i])) {
                        targets.add(null);
                    } else {
                        String[] subParts = parts[i].split(":");
                        if (subParts.length == 2) {
                            targets.add(new AB<>(subParts[0], Integer.parseInt(subParts[1])));
                        }
                    }
                }
            }


            return new FunctionShape(numInputs, numOutputs, name, targets, kutil);
        }

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

        return mkDefaultShape();
    }

    public static KShape mkDefaultShape() {return new RectangleShape(new Int2D(32,32), Color.black );}

}
