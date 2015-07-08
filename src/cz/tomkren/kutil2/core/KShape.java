package cz.tomkren.kutil2.core;

import java.awt.Graphics2D;
import net.phys2d.math.ROVector2f;
import net.phys2d.raw.shapes.DynamicShape;
import cz.tomkren.kutil2.items.Int2D;

public interface KShape {

    void draw(Graphics2D g, boolean isSel, String info, Int2D pos, Int2D c, double rot, boolean isRotable, double zoom, Int2D zoomCenter);
    boolean isHit(Int2D pos, Int2D clickPos, double rot, double zoom, Int2D zoomCenter);
    DynamicShape getPhys2dShape();
    ROVector2f getPhys2dCenter(Int2D pos);
    Int2D getPosByPhys2dCenter(ROVector2f v);

}
