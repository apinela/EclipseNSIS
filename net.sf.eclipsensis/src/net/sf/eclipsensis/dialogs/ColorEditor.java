/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;


public class ColorEditor {
    /** The extent. */
    private Point mRect;
    /** The image for the push button. */
    private Image mImage;
    /** The current RGB color value. */
    private RGB mRGB;
    /** The current color. */
    private Color mColor;
    /** The image push button which open the color dialog. */
    private Button mButton;
    
    /**
     * Creates and returns a new color editor.
     * 
     * @param parent the parent composite of this color editor
     */
    public ColorEditor(Composite parent) {
        
        mButton= new Button(parent, SWT.PUSH);
        mRect= computeImageSize(parent);
        mImage= new Image(parent.getDisplay(), mRect.x, mRect.y);
        
        GC gc= new GC(mImage);
        gc.setBackground(mButton.getBackground());
        gc.fillRectangle(0, 0, mRect.x, mRect.y);
        gc.dispose();
        
        mButton.setImage(mImage);
        mButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                ColorDialog colorDialog= new ColorDialog(mButton.getShell());
                colorDialog.setRGB(mRGB);
                RGB newColor = colorDialog.open();
                if (newColor != null) {
                    mRGB= newColor;
                    updateColorImage();
                }
            }
        });
        
        mButton.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                if (mImage != null)  {
                    mImage.dispose();
                    mImage= null;
                }
                if (mColor != null) {
                    mColor.dispose();
                    mColor= null;
                }
            }
        });
    }
    
    /**
     * Returns the current RGB color value.
     * 
     * @return an rgb with the current color value
     */
    public RGB getColorValue() {
        return mRGB;
    }
    
    /**
     * Sets the current RGB color value.
     * 
     * @param rgb the new value for the rgb color value
     */
    public void setColorValue(RGB rgb) {
        mRGB= rgb;
        updateColorImage();
    }
    
    /**
     * Returns the image push button.
     * 
     * @return the button which shows the current color as image
     */
    public Button getButton() {
        return mButton;
    }
    
    /**
     * Updates the color of the button image.
     */
    protected void updateColorImage() {
        
        Display display= mButton.getDisplay();
        
        GC gc= new GC(mImage);
        gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        gc.drawRectangle(0, 2, mRect.x - 1, mRect.y - 4);
        
        if (mColor != null)
            mColor.dispose();
            
        mColor= new Color(display, mRGB);
        gc.setBackground(mColor);
        gc.fillRectangle(1, 3, mRect.x - 2, mRect.y - 5);
        gc.dispose();
        
        mButton.setImage(mImage);
    }
    
    
    /**
     * Computes the size for the image.
     * 
     * @param window the window on which to render the image
     * @return the point with the image size
     */
    protected Point computeImageSize(Control window) {
        GC gc= new GC(window);
        Font f= JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
        gc.setFont(f);
        int height= gc.getFontMetrics().getHeight();
        gc.dispose();
        Point p= new Point(height * 3 - 6, height);
        return p;
    }
}