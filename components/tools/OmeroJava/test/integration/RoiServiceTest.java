/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.*;

//Java imports

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IRoiPrx;
import omero.api.RoiOptions;
import omero.model.ImageI;
import omero.model.RectI;
import omero.model.RoiI;
import omero.model.Shape;

/**
 * Collections of tests for the handling ROIs.
 */
@Test(groups = { "client", "integration", "blitz" })
public class RoiServiceTest 
	extends AbstractTest 
{
	/**
	 * Tests the creation of ROIs with rectangular shapes and removes one shape.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test(groups = "ticket:1679")
    public void testRemoveShape() 
    	throws Exception
    {
        IRoiPrx roiService = factory.getRoiService();
        ImageI image = (ImageI) iUpdate.saveAndReturnObject(simpleImage(0));
        RoiI roi = new RoiI();
        roi.setImage(image);
        RectI rect;
        RoiI serverROI = (RoiI) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            serverROI.addShape(rect);
        }
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        Shape shape = serverROI.getShape(0);
        serverROI.removeShape(shape);
        
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        roiService.findByImage(image.getId().getValue(), new RoiOptions());
    }

	
}
