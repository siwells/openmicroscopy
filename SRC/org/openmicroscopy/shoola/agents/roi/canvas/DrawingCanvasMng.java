/*
 * org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvasMng
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.roi.canvas;


//Java imports
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.ROIFactory;
import org.openmicroscopy.shoola.agents.roi.defs.ScreenPlaneArea;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DrawingCanvasMng
    implements MouseListener, MouseMotionListener
{
    
    /** Default line color. */
    public static final Color   LINE_COLOR = Color.RED;
    
    private static final int    DEFAULT_CURSOR = 0, HAND_CURSOR = 1;
    
    private static final int    LEFT = 0, RIGHT = 1, TOP = 2, BOTTOM = 3;
    
    private Cursor              handCursor, defaultCursor;
    
    /** Reference to the {@link DrawingCanvas view}. */
    private DrawingCanvas       view;
   
    /** Reference to the {@link ROIAgtCtrl control}. */
    private ROIAgtCtrl          control;
    
    /** Control to handle dragged event. */
    private boolean             dragging;
    
    /** Control to handle pressed event. */
    private boolean             pressed;

    private Point               anchor;
    
    private Rectangle           rectangleAnchor;
    
    private int                 state;
    
    private int                 shapeType;

    private Rectangle           drawingArea;
    
    private int                 xControl, yControl;

    private int                 resizeZone;
    
    PlaneArea                   planeArea;
    
    Color                       lineColor;

    public DrawingCanvasMng(DrawingCanvas view)
    {
        this.view = view;
        drawingArea = new Rectangle();
        defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        handCursor = new Cursor(Cursor.HAND_CURSOR);
        attachListeners();
        setDefault(-1, LINE_COLOR, ROIAgtUIF.NOT_ACTIVE_STATE); 
    }
   
    /** Set the drawing area. */
    public void setDrawingArea(Rectangle area)
    {
        drawingArea.setBounds(0, 0, area.width, area.height);
        //size the drawing canvas.
        view.setPreferredSize(new Dimension(area.width, area.height));
        view.setBounds(area);
    }

    /** Set the default. */
    public void setDefault(int index, Color c, int state)
    {
        lineColor = c;
        setState(state);
        view.indexSelected = index;
    }
    
    public void setControl(ROIAgtCtrl control) { this.control = control; }
    
    public void setState(int s)
    {
        state = ROIAgtUIF.NOT_ACTIVE_STATE;
        if (s == ROIAgtUIF.CONSTRUCTING || s == ROIAgtUIF.MOVING || 
            s == ROIAgtUIF.RESIZING) state = s;
    }
    
    /** Set the ROI type. */
    public void setType(int type)
    {
        if (type == ROIFactory.RECTANGLE || type == ROIFactory.ELLIPSE) 
            shapeType = type;
    }
    
    public int getShapeType() { return shapeType; }
    
    /** Mouse pressed event. */
    public void mousePressed(MouseEvent e)
    {
        Point p = e.getPoint();
        if (!dragging && drawingArea.contains(p)) {
            pressed = true;
            dragging = true;
            anchor = p;
            if (state == ROIAgtUIF.MOVING) setCursor(HAND_CURSOR);
            else setCursor(DEFAULT_CURSOR);
            if (state != ROIAgtUIF.CONSTRUCTING)
                handleMousePressed(p, e.getClickCount());
        }    
    }

    /** Mouse dragged event. */
    public void mouseDragged(MouseEvent e)
    {
        Point p = e.getPoint();
        if (dragging && drawingArea.contains(p)) {
            pressed = false;
            handleMouseDrag(p);
        }       
    }
    
    /** Mouse released event. */
    public void mouseReleased(MouseEvent e)
    {
        dragging = false;
        setCursor(DEFAULT_CURSOR); 
        switch (state) {
            case ROIAgtUIF.CONSTRUCTING: 
                if (!pressed) savePlaneArea();
                break;
            case ROIAgtUIF.MOVING: 
            case ROIAgtUIF.RESIZING: 
                state = ROIAgtUIF.CONSTRUCTING;
                if (planeArea != null) setPlaneArea();   
        } 
        state = ROIAgtUIF.NOT_ACTIVE_STATE;
        pressed = false;
    }
    
    /** 
     * Handle mouse moved event on existing planeArea.
     * Display the name or annotation if any.
     */
    public void mouseMoved(MouseEvent e)
    {
        Iterator i = view.listSPA.iterator();
        ScreenPlaneArea spa;
        PlaneArea pa;
        dragging = false;
        control.displayROIDescription(-1);
        while (i.hasNext()) {
            spa = (ScreenPlaneArea) (i.next());
            pa =  spa.getPlaneArea();
            if (pa != null && pa.contains(e.getPoint()))
                control.displayROIDescription(spa.getIndex()); 
        }     
    }

    /** Handle mouse pressed event. */
    private void handleMousePressed(Point p, int clickCount)
    {
        planeArea = null;
        Iterator i = view.listSPA.iterator();
        ScreenPlaneArea spa;
        PlaneArea pa;
        boolean showEditor = false;
        while (i.hasNext()) {
            spa = (ScreenPlaneArea) (i.next());
            pa = spa.getPlaneArea();
            if (pa != null) {
                if (pa.contains(p)) {
                    if (clickCount == 1)
                        clickCountOne(p, spa.getIndex(), pa);
                    else if (clickCount == 2) {
                        dragging = false;
                        state = ROIAgtUIF.NOT_ACTIVE_STATE;
                        showEditor = true;
                    }
                }
            }
        }
        if (showEditor) control.showROIEditor();
    }
    
    /** Handle one-click mouse pressed event. */
    private void clickCountOne(Point p, int index, PlaneArea pa)
    {
        Rectangle r, vLeft, vRight, hTop, hBottom;
        r = pa.getBounds();
        planeArea = pa;
        rectangleAnchor = r;
        xControl = r.x;
        yControl = r.y;
        control.setSelectedIndex(index);
        state = ROIAgtUIF.RESIZING;
        vLeft = ROIFactory.getVerticalControlArea(r.x, r.y, r.height);
        vRight = ROIFactory.getVerticalControlArea(r.x+r.width, r.y, r.height);
        hTop = ROIFactory.getHorizontalControlArea(r.x, r.y, r.width);
        hBottom = ROIFactory.getHorizontalControlArea(r.x, r.y+r.height, 
                                                    r.width);
        if (vLeft.contains(p))
            resizeZone = LEFT;
        else if (vRight.contains(p))
            resizeZone = RIGHT;
        else if (hTop.contains(p))
            resizeZone = TOP;
        else if (hBottom.contains(p))
            resizeZone = BOTTOM;
        else state = ROIAgtUIF.MOVING;
    }

    /** Handle mouse dragged event. */
    private void handleMouseDrag(Point p)
    {
        switch (state) {
            case ROIAgtUIF.CONSTRUCTING:
                construct(p); break;
            case ROIAgtUIF.MOVING: 
                if (planeArea != null) move(p);
                break;
            case ROIAgtUIF.RESIZING: 
                if (planeArea != null) resize(p);   
        }
    }
    
    private void construct(Point p)
    {
        planeArea = ROIFactory.makeShape(anchor, p, shapeType);
        view.repaint();
        Rectangle r = planeArea.getBounds();
        if (r.width>0 && r.height>0)
            control.setROIThumbnail(r.x, r.y, r.width, r.height);
    }
    
    /** Save the current PlaneArea. */
    private void savePlaneArea()
    {
        ScreenPlaneArea spa = view.getScreenPlaneArea(view.indexSelected);
        if (spa == null) {
            spa = new ScreenPlaneArea(view.indexSelected, planeArea, lineColor);
            view.addSPAToCanvas(spa);
        }                   
        else spa.setPlaneArea(planeArea);
        control.setPlaneArea(planeArea);
        planeArea = null;
        view.repaint();
    }
    
    private void setPlaneArea()
    {
        view.setPlaneArea(planeArea);
        control.setPlaneArea(planeArea);
        planeArea = null;
    }
    
    /** Move the current planeArea. */
    private void move(Point p)
    {
        Rectangle r = planeArea.getBounds();
        int x = xControl+p.x-anchor.x, y = yControl+p.y-anchor.y,
            w = r.width,  h = r.height;
        if (areaValid(x, y, w, h)) validArea(x, y, w, h);
    }

    /** Resize the current planeArea. */
    private void resize(Point p)
    {
        int x = rectangleAnchor.x, y = rectangleAnchor.y, 
            w = rectangleAnchor.width, h = rectangleAnchor.height;
        switch (resizeZone) {
            case LEFT:
                x = p.x;
                w += anchor.x-p.x;
                break;
            case RIGHT: 
                w += p.x-anchor.x;
                break;
            case TOP: 
                y = p.y;
                h += anchor.y-p.y; 
                break;
            case BOTTOM:
                h += p.y-anchor.y;  
        }
        if (areaValid(x, y, w, h)) validArea(x, y, w, h);
    }
    
    /** Set the new Area. */
    private void validArea(int x, int y, int w, int h)
    {
        planeArea.setBounds(x, y, w, h);
        view.repaint();
        control.setROIThumbnail(x, y, w, h);
    }
    
    /** Check if the shape is still in the drawingArea. */
    private boolean areaValid(int x, int y, int w, int h)
    {
        boolean b = true;
        if (!drawingArea.contains(x, y)) b = false;
        if (!drawingArea.contains(x+w, y)) b = false;
        if (!drawingArea.contains(x, y+h)) b = false;
        return b;
    }
    
    /** Set the cursor type according to event. */
    private void setCursor(int type)
    {
        Cursor c = null;
        if (type == DEFAULT_CURSOR) c = defaultCursor;
        else if (type == HAND_CURSOR) c = handCursor;
        if (c != null) view.setCursor(c);
    }

    /** Attach mouse listener. */
    private void attachListeners()
    {
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
    }
    
    /** 
     * Required by I/F but not actually needed in our case, 
     * no op implementation.
     */   
    public void mouseClicked(MouseEvent e) {}

    /** 
     * Required by I/F but not actually needed in our case, 
     * no op implementation.
     */   
    public void mouseEntered(MouseEvent e) {}
    
    /** 
     * Required by I/F but not actually needed in our case,
     * no op implementation.
     */   
    public void mouseExited(MouseEvent e) {}
   
}

