/*
 * org.openmicroscopy.shoola.agents.roi.results.util.FileFormatChooser
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

package org.openmicroscopy.shoola.agents.roi.results.saver;


//Java imports
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.TEXTFilter;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class FileFormatChooser
    extends JFileChooser
{

    private static final String     SAVE_AS = "Save the result in the " +
                                            "specified format.";
    
    /** Default extension format. */
    private static final String     DEFAULT_FORMAT = XMLFilter.XML;
    
    private ROISaverMng        manager;
    
    /** Display or not the fileChooser, when the dialog is shown. */
    private boolean                 display;
    
    public FileFormatChooser(ROISaverMng manager)
    {
        this.manager = manager;
        display = false;
        buildGUI();
    }
    
    void setDisplay(boolean b) { display = b; }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setDialogType(SAVE_DIALOG);
        setFileSelectionMode(FILES_ONLY);
        XMLFilter filter = new XMLFilter();
        setFileFilter(filter);
        addChoosableFileFilter(filter); 
        TEXTFilter txtFilter = new TEXTFilter();
        setFileFilter(txtFilter);
        addChoosableFileFilter(txtFilter); 
        setAcceptAllFileFilterUsed(false);
        setApproveButtonToolTipText(UIUtilities.formatToolTipText(SAVE_AS));
        setApproveButtonText("Save as");
    }
    
    /** Override the {@link #cancelSelection} method. */
    public void cancelSelection() { manager.disposeView(); }
    
    /** Override the {@link #approveSelection} method. */
    public void approveSelection()
    {
        File file = getSelectedFile();
        if (file != null) {
            String format = getFormat(getFileFilter());
            String  fileName = file.getAbsolutePath();
            String message = ROISaver.MSG_DIR+""+getCurrentDirectory();
            setSelection(format, fileName, message, 
                                getCurrentDirectory().listFiles());
            setSelectedFile(null);
            if (display) return;    // to check
        }      
        // No file selected, or file can be written - let OK action continue
        super.approveSelection();
    }
    
    /**
     * Retrieve the File format selected.
     * @param filter    filter specified.
     * @return See above.
     */
    private String getFormat(FileFilter filter)
    {
        String format = DEFAULT_FORMAT;
        if (filter instanceof TEXTFilter) 
            format = TEXTFilter.TEXT;
        else if (filter instanceof XMLFilter) 
            format = XMLFilter.XML;
        return format;
    }
    
    /** 
     * Check if the fileName specified already exists if not the image is saved
     * in the specified format.
     * 
     * @param format        format selected <code>jpeg<code>, 
     *                      <code>png<code> or <code>tif<code>.
     * @param fileName      image's name.
     * @param message       message displayed after the image has been created.
     * @param list          lis of files in the current directory.
     */
    private void setSelection(String format, String fileName, String message,
                                File[] list)
    {
        boolean exist = false;
        String name = fileName + "." + format;
        for (int i = 0; i < list.length; i++)
            if ((list[i].getAbsolutePath()).equals(name)) exist = true;
        
        if (!exist) {
            display = false;
            manager.saveROIResult(format, fileName, message);
            cancelSelection(); 
        } else manager.showSelectionDialog(format, fileName, message);
    }

}

