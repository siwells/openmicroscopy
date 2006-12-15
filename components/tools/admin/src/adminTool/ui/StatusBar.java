/*
 * ome.formats.testclient.StatusBar
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package src.adminTool.ui;

// Java imports
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import src.adminTool.main.AdminMain;
import src.adminTool.main.MainPanel;


// Third-party libraries

/**
 * Presents the progress of the data retrieval.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: 2986 $ $Date:
 *          2005-11-25 10:31:00 +0000 (Fri, 25 Nov 2005) $) </small>
 * @since OME2.2
 */
public class StatusBar extends JPanel
{

    /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar progressBar;

    /** Displays the status message. */
    private JLabel       status;

    /**
     * Initializes the components.
     * 
     * @param statusIcon The icon displayed in the left corner.
     */
    private void initComponents()
    {
        progressBar = new JProgressBar();
        status = new JLabel();
        status.setFont(getFont().deriveFont(11.0f));
    }

    /** Build and lay out the UI. */
    private void buildUI()
    {
        Border compoundBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(), BorderFactory
                        .createEmptyBorder(0, 4, 0, 15));

        setPreferredSize(new Dimension(10, 28));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(0, -2, -2, -2), compoundBorder));
        add(status);
      //  add(buildComponentPanelRight(progressBar));
    }

    /**
     * Creates a new instance.
     * 
     * @param statusIcon The icon displayed in the left corner.
     */
    public StatusBar()
    {
        initComponents();
        buildUI();
    }

    /**
     * Sets the status message.
     * 
     * @param s The message to display.
     */
    public void setStatus(String s)
    {
        status.setText(s);
    }

    /**
     * Sets the value of the progress bar.
     * 
     * @param visible Pass <code>false</code> to hide the progress bar,
     *            <code>true</otherwise>
     * @param perc  The value to set at the % for the progress bar.
     * @param string A string value to set for the progress bar instead of perc
     */
    public void setProgress(boolean visible, int perc, String string)
    {
        progressBar.setVisible(visible);
        if (perc < 0) progressBar.setIndeterminate(true);
        else
        {
            progressBar.setFont(getFont().deriveFont(11.0f));
            progressBar.setPreferredSize(new Dimension(220, 20));
            progressBar.setStringPainted(true);
            progressBar.setValue(perc);
            if (string.length() > 0) 
            {
                progressBar.setStringPainted(true);
                progressBar.setString("   " + string + "   ");
            }
        }
    }

    public void setProgressMaximum(int max)
    {
        progressBar.setMaximum(max);
    }
    
    public void setProgressValue(int val)
    {
        progressBar.setValue(val);
    }
    
    public JPanel buildComponentPanelRight(JComponent component)
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(component);
        return p;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    public void setStatusIcon(ImageIcon icon, String description)
    {
        status.setIcon(icon);
        status.setText(description);
    }
}
