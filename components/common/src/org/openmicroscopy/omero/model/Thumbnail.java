package org.openmicroscopy.omero.model;

import org.openmicroscopy.omero.BaseModelUtils;


import java.util.*;




/**
 * Thumbnail generated by hbm2java
 */
public class
Thumbnail 
implements java.io.Serializable ,
org.openmicroscopy.omero.OMEModel {

    // Fields    

     private Integer attributeId;
     private String path;
     private String mimeType;
     private Image image;
     private Repository repository;
     private ModuleExecution moduleExecution;


    // Constructors

    /** default constructor */
    public Thumbnail() {
    }
    
    /** constructor with id */
    public Thumbnail(Integer attributeId) {
        this.attributeId = attributeId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getAttributeId() {
        return this.attributeId;
    }
    
    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    /**
     * 
     */
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     */
    public String getMimeType() {
        return this.mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * 
     */
    public Image getImage() {
        return this.image;
    }
    
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * 
     */
    public Repository getRepository() {
        return this.repository;
    }
    
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * 
     */
    public ModuleExecution getModuleExecution() {
        return this.moduleExecution;
    }
    
    public void setModuleExecution(ModuleExecution moduleExecution) {
        this.moduleExecution = moduleExecution;
    }





	/** utility methods. Container may re-assign this. */	
	protected static org.openmicroscopy.omero.BaseModelUtils _utils = 
		new org.openmicroscopy.omero.BaseModelUtils();
	public BaseModelUtils getUtils(){
		return _utils;
	}
	public void setUtils(BaseModelUtils utils){
		_utils = utils;
	}



}
