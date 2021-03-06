/*
 * $Id$
 *
 *   Copyright 2006-2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package spec.schema;

//Java imports
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

//Third-party libraries
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.springframework.util.ResourceUtils;

//Application-internal dependencies
import spec.AbstractTest;
import spec.XMLMockObjects;
import spec.XMLWriter;

/** 
 * Collections of tests.
 * Checks if the downgrade from current schema to 2008-02 schema works.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @author Andrew Patterson &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:ajpatterson@lifesci.dundee.ac.uk">ajpatterson@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SchemaCurrent_TO_2008_02_Test 
	extends AbstractTest
{

	/** The collection of files that have to be deleted. */
	private List<File> files;
	
	/** The transforms */
	private InputStream STYLESHEET_A;
	private InputStream STYLESHEET_B;
	
	/** The target schema */
	private StreamSource[] schemaArray;
	
	/**
	 * Checks if the <code>Image</code> tag was correctly transformed.
	 * 
	 * @param destNode The node from the transformed file.
	 * @param srcNode The Image node from the source file
	 */
	private void checkImageNode(Node destNode, Node srcNode)
	{
		assertNotNull(destNode);
		assertNotNull(srcNode);
		NamedNodeMap attributesSrc = srcNode.getAttributes();
		String nameSrc = "";
		String idSrc = "";
		Node n;
		String name;
		for (int i = 0; i < attributesSrc.getLength(); i++) {
			n = attributesSrc.item(i);
			name = n.getNodeName();
			if (name.equals(XMLWriter.ID_ATTRIBUTE))
				idSrc = n.getNodeValue();
			else if (name.equals(XMLWriter.NAME_ATTRIBUTE))
				nameSrc = n.getNodeValue();
		}
		
		// compare the stored values for ID and Name attributes
		// to those on the output node
		NamedNodeMap attributes = destNode.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			n = attributes.item(i);
			if (n != null) {
				name = n.getNodeName();
				if (name.equals(XMLWriter.ID_ATTRIBUTE))
					assertEquals(n.getNodeValue(), idSrc);
				else if (name.equals(XMLWriter.NAME_ATTRIBUTE))
					assertEquals(n.getNodeValue(), nameSrc);
			}
		}

		// Find the pixels node in the input image node
		// (if more than one last will be found)
		Node pixelsNode = null;
		NodeList list = srcNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			n = list.item(i);
			if (n != null) {
				name = n.getNodeName();
				if (name.contains(XMLWriter.PIXELS_TAG))
					pixelsNode = n;
			}
		}
		// Find the pixels node in the output image node
		// (if more than one this will be incorrect)
		list = destNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			n = list.item(i);
			if (n != null) {
				name = n.getNodeName();
				if (name.contains(XMLWriter.PIXELS_TAG))
					//  - compare the found node to the input node stored above
					checkPixelsNode(n, pixelsNode);
			}
		}
	}
	
	/**
	 * Checks if the <code>Pixels</code> tag was correctly transformed.
	 * 
	 * @param destNode The node from the transformed file.
	 * @param srcNode The Image node from the source file
	 */
	private void checkPixelsNode(Node destNode, Node srcNode)
	{
		// store the values for ID and Name attribute on the input node
		NamedNodeMap attributesSrc = srcNode.getAttributes();
		String nameSrc = "";
		String idSrc = "";
		String sizeX = "";
		String sizeY = "";
		String sizeZ = "";
		String sizeC = "";
		String sizeT = "";
		String pixelsType = "";
		String dimensionOrder = "";
		Node n;
		String name;
		for (int i = 0; i < attributesSrc.getLength(); i++) {
			n = attributesSrc.item(i);
			name = n.getNodeName();
			if (name.equals(XMLWriter.ID_ATTRIBUTE))
				idSrc = n.getNodeValue();
			else if (name.equals(XMLWriter.NAME_ATTRIBUTE))
				nameSrc = n.getNodeValue();
			else if (name.equals(XMLWriter.SIZE_X_ATTRIBUTE))
				sizeX = n.getNodeValue();
			else if (name.equals(XMLWriter.SIZE_Y_ATTRIBUTE))
				sizeY = n.getNodeValue();
			else if (name.equals(XMLWriter.SIZE_Z_ATTRIBUTE))
				sizeZ = n.getNodeValue();
			else if (name.equals(XMLWriter.SIZE_C_ATTRIBUTE))
				sizeC = n.getNodeValue();
			else if (name.equals(XMLWriter.SIZE_T_ATTRIBUTE))
				sizeT = n.getNodeValue();
			else if (name.equals(XMLWriter.PIXELS_TYPE_ATTRIBUTE))
				pixelsType = n.getNodeValue();
			else if (name.equals(XMLWriter.DIMENSION_ORDER_ATTRIBUTE))
				dimensionOrder = n.getNodeValue();
		}
		String bigEndianDst = "";
		NamedNodeMap attributes = destNode.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			n = attributes.item(i);
			if (n != null) {
				name = n.getNodeName();
				if (name.equals(XMLWriter.ID_ATTRIBUTE))
					assertEquals(n.getNodeValue(), idSrc);
				else if (name.equals(XMLWriter.NAME_ATTRIBUTE))
					assertEquals(n.getNodeValue(), nameSrc);
				else if (name.equals(XMLWriter.SIZE_X_ATTRIBUTE))
					assertEquals(n.getNodeValue(), sizeX);
				else if (name.equals(XMLWriter.SIZE_Y_ATTRIBUTE))
					assertEquals(n.getNodeValue(), sizeY);
				else if (name.equals(XMLWriter.SIZE_Z_ATTRIBUTE))
					assertEquals(n.getNodeValue(), sizeZ);
				else if (name.equals(XMLWriter.SIZE_C_ATTRIBUTE))
					assertEquals(n.getNodeValue(), sizeC);
				else if (name.equals(XMLWriter.SIZE_T_ATTRIBUTE))
					assertEquals(n.getNodeValue(), sizeT);
//				else if (name.equals(XMLWriter.PIXELS_TYPE_ATTRIBUTE))
//					assertEquals(n.getNodeValue(), pixelsType);
				else if (name.equals(XMLWriter.DIMENSION_ORDER_ATTRIBUTE))
					assertEquals(n.getNodeValue(), dimensionOrder);
				else if (name.equals(XMLWriter.BIG_ENDIAN_ATTRIBUTE))
				    //  - also store the Big Endian value used in the output
					bigEndianDst = n.getNodeValue();
			}
		}
		// Create two arrays of BinData nodes
		NodeList list = srcNode.getChildNodes();
		List<Node> binDataNodeSrc = new ArrayList<Node>();
		for (int i = 0; i < list.getLength(); i++) {
			n = list.item(i);
			if (n != null) {
				name = n.getNodeName();
				if (name.contains(XMLWriter.BIN_DATA_TAG))
					//  - Add node to the input list
					binDataNodeSrc.add(n);
			}
		}
		list = destNode.getChildNodes();
		List<Node> binDataNodeDest = new ArrayList<Node>();
		for (int i = 0; i < list.getLength(); i++) {
			n = list.item(i);
			if (n != null) {
				name = n.getNodeName();
				if (name.contains(XMLWriter.BIN_DATA_TAG))
					//  - Add node to the output list
					binDataNodeDest.add(n);
			}
		}
        // Compare the lengths of the lists
		assertTrue(binDataNodeSrc.size() > 0);
		assertEquals(binDataNodeSrc.size(), binDataNodeDest.size());
		// Compare the contents of the lists
		for (int i = 0; i < binDataNodeDest.size(); i++) {
			checkBinDataNode(binDataNodeDest.get(i), binDataNodeSrc.get(i));
		}
		// Compare the Big Endian value from the output stored above 
		// with the value used in the input file
		n = binDataNodeSrc.get(0);
		attributesSrc = n.getAttributes();
		//now check that 
		for (int i = 0; i < attributesSrc.getLength(); i++) {
			n = attributesSrc.item(i);
			if (n != null) {
				name = n.getNodeName();
				if (name.contains(XMLWriter.BIG_ENDIAN_ATTRIBUTE))
					assertEquals(n.getNodeValue(), bigEndianDst);
			}
		}
	}
	
	/**
	 * Checks if the <code>Image</code> tag was correctly transformed.
	 * 
	 * @param destNode The node from the transformed file.
	 * @param srcNode The Image node from the source file
	 */
	private void checkBinDataNode(Node destNode, Node srcNode)
	{
		assertNotNull(destNode);
		assertNotNull(srcNode);

		// store the values for Compression attribute on the input node
		NamedNodeMap attributesSrc = srcNode.getAttributes();
		String compression = "";
		Node n;
		String name;
		for (int i = 0; i < attributesSrc.getLength(); i++) {
			n = attributesSrc.item(i);
			name = n.getNodeName();
			if (name.equals(XMLWriter.COMPRESSION_ATTRIBUTE))
				compression = n.getNodeValue();
		}
		
		// compare the stored value for the Compression attribute
		// to that on the output node
		NamedNodeMap attributes = destNode.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			n = attributes.item(i);
			if (n != null) {
				name = n.getNodeName();
				if (name.equals(XMLWriter.COMPRESSION_ATTRIBUTE)) {
					assertEquals(n.getNodeValue(), compression);
				}
			}
		}
		// compare the contents of the BinData node		
		assertEquals(destNode.getTextContent(), srcNode.getTextContent());
	}
	
	/**
	 * Overridden to initialize the list.
	 * @see AbstractTest#setUp()
	 */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception
    {
    	super.setUp();

		/** The target schema file */
//		SCHEMA_2008_02 = ResourceUtils.getFile("classpath:2008-02/V2/ome.xsd");
		//components/specification/Released-Schema/2008-02/V2/ome.xsd 

		schemaArray = new StreamSource[1];

		schemaArray[0] = new StreamSource(this.getClass().getResourceAsStream("/Released-Schema/2008-02/V2/ome.xsd"));

		/** The transforms */
		STYLESHEET_A = this.getClass().getResourceAsStream("/Xslt/2011-06-to-2010-06.xsl");
		STYLESHEET_B = this.getClass().getResourceAsStream("/Xslt/2010-06-to-2008-02.xsl");
		//components/specification/Xslt/

    	files = new ArrayList<File>();
    }
    
	/**
	 * Overridden to delete the files.
	 * @see AbstractTest#tearDown()
	 */
    @Override
    @AfterClass
    public void tearDown() 
    	throws Exception
    {
    	Iterator<File> i = files.iterator();
    	while (i.hasNext()) {
			i.next().delete();
		}
    	files.clear();
    }
	
	/**
     * Tests the XSLT used to downgrade from current schema to 2008-02.
     * An XML file with an image is created and the stylesheet is applied.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
	public void testDowngradeTo200802ImageNoMetadata()
		throws Exception
	{
		File inFile = File.createTempFile("testDowngradeTo200802ImageNoMetadata",
				"."+OME_XML_FORMAT);
		files.add(inFile);
		File middleFile = File.createTempFile("testDowngradeTo200802ImageNoMetadataMiddle",
				"."+OME_XML_FORMAT);
		files.add(middleFile);
		File outputFile = File.createTempFile(
				"testDowngradeTo200802ImageNoMetadataOutput",
				"."+OME_XML_FORMAT);
		files.add(outputFile);
		XMLMockObjects xml = new  XMLMockObjects();
		XMLWriter writer = new XMLWriter();
		writer.writeFile(inFile, xml.createImage(), true);

		transformFileWithStream(inFile, middleFile, STYLESHEET_A);
		transformFileWithStream(middleFile, outputFile, STYLESHEET_B);
		
		Document doc = parseFileWithStreamArray(outputFile, schemaArray);
		assertNotNull(doc);
		
		//Should only have one root node i.e. OME node
		NodeList list = doc.getChildNodes();
		assertEquals(list.getLength(), 1);
		Node root = list.item(0);
		assertEquals(root.getNodeName(), XMLWriter.OME_TAG);
		//now analyse the root node
		list = root.getChildNodes();
		String name;
		Node n;
		Document docSrc = parseFile(inFile, null);
		Node rootSrc = docSrc.getChildNodes().item(0);
		Node imageNode = null;
		NodeList listSrc = rootSrc.getChildNodes();
		for (int i = 0; i < listSrc.getLength(); i++) {
			n = listSrc.item(i);
			name = n.getNodeName();
			if (name != null) {
				if (name.contains(XMLWriter.IMAGE_TAG))
					imageNode = n;
			}
		}
		
		for (int i = 0; i < list.getLength(); i++) {
			n = list.item(i);
			name = n.getNodeName();
			if (name != null) {
				//TODO: add other node
				if (name.contains(XMLWriter.IMAGE_TAG) && imageNode != null)
					checkImageNode(n, imageNode);
			}
		}
	}
}
