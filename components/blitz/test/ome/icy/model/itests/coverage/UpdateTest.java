/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.coverage;

import ome.icy.model.itests.IceTest;
import omero.JString;
import omero.api.IUpdatePrx;

import org.testng.annotations.Test;

public class UpdateTest extends IceTest {

    @Test
    public void testSaveAndReturnObject() throws Exception {
        IUpdatePrx prx = ice.getServiceFactory().getUpdateService();
        assertNotNull(prx);

        omero.model.ImageI obj = new omero.model.ImageI();
        obj.setName(new JString("foo"));

        omero.model.CategoryImageLinkI link = new omero.model.CategoryImageLinkI();
        omero.model.CategoryI cat = new omero.model.CategoryI();
        cat.setName(new JString("bar"));
        link.setParent(cat);
        link.setChild(obj);
        obj.addCategoryImageLink(link);

        obj = (omero.model.ImageI) prx.saveAndReturnObject(obj);
        link = (omero.model.CategoryImageLinkI) obj.copyCategoryLinks().get(0);
        cat = (omero.model.CategoryI) link.getParent();

        assertTrue("foo".equals(obj.getName().val));
        if (cat == null) {
            fail("Cat is null");
        } else {
            assertTrue("bar".equals(cat.getName().val));
        }
    }

    @Test
    public void testDeleteObject() throws Exception {
        IUpdatePrx prx = ice.getServiceFactory().getUpdateService();
        assertNotNull(prx);

        String uuid = Ice.Util.generateUUID();
        omero.model.ImageI obj = new omero.model.ImageI();
        obj.setName(new JString(uuid));
        obj = (omero.model.ImageI) prx.saveAndReturnObject(obj);

        prx.deleteObject(obj);
        assertTrue(ice.getServiceFactory().getQueryService().findAllByString(
                "Image", "name", uuid, false, null).size() == 0);
    }
}
