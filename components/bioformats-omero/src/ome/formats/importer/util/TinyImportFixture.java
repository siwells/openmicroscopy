/*
 * ome.formats.importer.TinyImportFixture
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer.util;

// Java imports
import java.io.File;
import java.util.UUID;

import loci.formats.ImageReader;
import ome.formats.OMEROMetadataStore;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.model.containers.Dataset;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

/**
 * test fixture which uses a hard-coded file ("tinyTest.d3d.dv") from the 
 * classpath, and adds them to a new UUID-named dataset. 
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see OMEROMetadataStore
 * @see ExampleUnitTest
 * @since 3.0-M3
 */
// @RevisionDate("$Date$")
// @RevisionNumber("$Revision$")
public class TinyImportFixture extends ImportFixture
{

	/** Hard-coded filename of the image to be imported */
	public final static String FILENAME = "tinyTest.d3d.dv";
	
    Log                        log = LogFactory.getLog(TinyImportFixture.class);

    private Dataset d;
    
    private ServiceFactory sf;
    
    public TinyImportFixture(ServiceFactory services) throws Exception
    {
        super( new OMEROMetadataStore(services), new ImageReader() );
        this.sf = services;
    }

    /**
     * checks for the necessary fields and initializes the {@link ImportLibrary}
     * 
     * @throws Exception
     */
    public void setUp() throws Exception
    {
		d = new Dataset();
		d.setName(UUID.randomUUID().toString());
		d = sf.getUpdateService().saveAndReturnObject(d);
		
		File 	tinyTest = ResourceUtils.getFile("classpath:"+FILENAME);
		
		super.put( tinyTest, d );
    	super.setUp();
    }
    
    /** provides access to the created {@link Dataset} instance.
     */
    public Dataset getDataset()
    {
    	return d;
    }
}
