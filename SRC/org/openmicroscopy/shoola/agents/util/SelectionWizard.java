/*
 * org.openmicroscopy.shoola.agents.util.SelectionWizard 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A modal dialog to select collection of objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SelectionWizard 
	extends JDialog 
	implements ActionListener
{
	
	/** Bound property indicating the selected items. */
	public static final String	SELECTED_ITEMS_PROPERTY = "selectedItems";
	
	/** Action command ID to add a field to the result table. */
	private static final int ADD = 0;
	
	/** Action command ID to remove a field from the result table. */
	private static final int REMOVE = 1;
	
	/** Action command ID to add all fields to the result table. */
	private static final int ADD_ALL = 2;
	
	/** Action command ID to remove all fields from the result table. */
	private static final int REMOVE_ALL = 3;
	
	/** Action command ID to Accept the current field selection. */
	private static final int ACCEPT = 4;

	/** Action command ID to cancel the wizard. */
	private static final int CANCEL = 5;
	
	/** Action command ID to reset the current field selection. */
	private static final int RESET = 6;
	
	/** The original items before the user selects items. */
	private List<Object>		originalItems;
	
	/** Collection of available items. */
	private Collection<Object>	availableItems;

	/** Collection of all the selected items. */
	private Collection<Object>	selectedItems;
	
	/** The list box showing the available items. */
	private JList				availableItemsListbox;
	
	/** The list box showing the selected items. */
	private JList				selectedItemsListbox;
	
	/** The button to move an item from the remaining items to current items. */
	private JButton 			addButton;
	
	/** The button to move an item from the current items to remaining items. */
	private JButton 			removeButton;
	
	/** The button to move all items to the current items. */
	private JButton 			addAllButton;
	
	/** The button to move all items to the remaining items. */
	private JButton 			removeAllButton;
	
	/** The button to accept the current selection. */
	private JButton 			acceptButton;
	
	/** The button to reset the current selection. */
	private JButton 			resetButton;

	/** The button to cancel the current selection. */
	private JButton 			cancelButton;
	
	/** Sorts the object. */
	private ViewerSorter		sorter;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		sorter = new ViewerSorter();
		
		availableItemsListbox = new JList();
		availableItemsListbox.setCellRenderer(new DataObjectListCellRenderer());
		selectedItemsListbox = new JList();
		selectedItemsListbox.setCellRenderer(new DataObjectListCellRenderer());
		IconManager icons = IconManager.getInstance();
		addButton = new JButton(icons.getIcon(IconManager.RIGHT_ARROW));
		removeButton = new JButton(icons.getIcon(IconManager.LEFT_ARROW));
		addAllButton = new JButton(
								icons.getIcon(IconManager.DOUBLE_RIGHT_ARROW));
		removeAllButton = new JButton(
								icons.getIcon(IconManager.DOUBLE_LEFT_ARROW));
		acceptButton = new JButton("Accept");
		acceptButton.setToolTipText("Accept the selection.");
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Cancel the selection.");
		resetButton = new JButton("Reset");
		resetButton.setToolTipText("Reset the selection.");
		
		addButton.setActionCommand(""+ADD);
		addButton.addActionListener(this);
		addAllButton.setActionCommand(""+ADD_ALL);
		addAllButton.addActionListener(this);
		removeButton.setActionCommand(""+REMOVE);
		removeButton.addActionListener(this);
		removeAllButton.setActionCommand(""+REMOVE_ALL);
		removeAllButton.addActionListener(this);
		acceptButton.setActionCommand(""+ACCEPT);
		acceptButton.addActionListener(this);
		acceptButton.setEnabled(false);
		resetButton.setEnabled(false);
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		resetButton.setActionCommand(""+RESET);
		resetButton.addActionListener(this);
		getRootPane().setDefaultButton(cancelButton);
	}
	
	/** Resets the current selection to the original selection. */
	private void resetSelection()
	{
		availableItems.clear();
		for (Object item : originalItems)
			availableItems.add(item);
	}

	/**
	 * Creates a copy of the original selection set so it can be reset by user.  
	 */
	private void createOriginalSelection()
	{
		originalItems = new ArrayList<Object>();
		for (Object item : availableItems)
			originalItems.add(item);
	}
	
	/**
	 * Adds an item to the list and then sorts the list to maintain order. 
	 */
	private void addItem()
	{
		if (availableItemsListbox.getSelectedIndex() == -1) return;
		int [] indexes = availableItemsListbox.getSelectedIndices();
		DefaultListModel model = 
				(DefaultListModel) availableItemsListbox.getModel();
		Object object;
		for (int i = 0 ; i < indexes.length ; i++) {
			object = model.getElementAt(indexes[i]);
			if (availableItems.contains(object)) {
				selectedItems.add(object);
				availableItems.remove(object);
			}
		}
		sortLists();
		populateSelectedItems();
		populateAvailableItems();
		setButtonsEnabled();
	}
	
	/** Sorts the lists. */
	private void sortLists()
	{
		availableItems = sorter.sort(availableItems);
		selectedItems = sorter.sort(selectedItems);
	}
	
	/** Adds all the items to the selection. */
	private void addAllItems()
	{
		selectedItems.clear();
		for (Object item: availableItems)
			selectedItems.add(item);
		availableItems.clear();
		sortLists();
		populateAvailableItems();
		populateSelectedItems();
		setButtonsEnabled();
	}
	
	/** Removes an item from the selection. */
	private void removeItem()
	{
		if (selectedItemsListbox.getSelectedIndex() == -1) return;
		DefaultListModel model = (DefaultListModel)
									selectedItemsListbox.getModel();
		int [] indexes = selectedItemsListbox.getSelectedIndices();
		Object object; 
		for (int i = 0 ; i < indexes.length ; i++) {
			object = model.getElementAt(indexes[i]);
			if (selectedItems.contains(object)) {
				selectedItems.remove(object);
				availableItems.add(object);
			}
		}
		
		sortLists();	
		populateAvailableItems();
		populateSelectedItems();
		setButtonsEnabled();
	}
	
	/** Removes all items from the list. */
	private void removeAllItems()
	{
		resetSelection();
		sortLists();
		selectedItems.clear();
		populateAvailableItems();
		populateSelectedItems();
		setButtonsEnabled();
	}
	
	/**
	 * Sets the enabled flag of the {@link #acceptButton} and
	 * {@link #resetButton}
	 *
	 */
	private void setButtonsEnabled()
	{
		
		if (originalItems.size() != availableItems.size()) {
			acceptButton.setEnabled(true);
			resetButton.setEnabled(true);
		} else {
			acceptButton.setEnabled(false);
			int n = 0;
			Iterator i = availableItems.iterator();
			while (i.hasNext()) {
				if (originalItems.contains(i.next())) n++;
			}
			resetButton.setEnabled(n != originalItems.size());
		}
	}
	
	/** Closes and disposes. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/** Fires a property change with the selected items. */
	private void accept()
	{
		firePropertyChange(SELECTED_ITEMS_PROPERTY, null, selectedItems);
		cancel();
	}
	
	/** Resets the selection to the original selection. */
	private void reset()
	{
		resetSelection();
		selectedItems.clear();
		populateAvailableItems();
		populateSelectedItems();
		setButtonsEnabled();
	}
	
	/**
	 * Builds and lays out the selection components.
	 * 
	 * @return See above.
	 */
	private JPanel layoutSelectionPane()
	{
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.add(createAvailableItemsPane());
		container.add(Box.createHorizontalStrut(20));
		container.add(createSelectionPane());
		container.add(Box.createHorizontalStrut(20));
		container.add(createSelectedItemsPane());
		return container;
	}
	
	/** Builds and lays out the UI. */
	private void buildUI()
	{
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(layoutSelectionPane(), BorderLayout.CENTER);
		c.add(createControlsPane(), BorderLayout.SOUTH);
	}
	
	/**
	 * Builds and lays out the components hosting the controls.
	 * 
	 * @return See above.
	 */
	private JPanel createControlsPane()
	{
		JPanel controlPanel = new JPanel();
		controlPanel.setOpaque(false);
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(resetButton);
		controlPanel.add(acceptButton);
		controlPanel.add(cancelButton);
		return UIUtilities.buildComponentPanelRight(controlPanel);
	}
	
	/**
	 * Builds and lays out the list of selected tags.
	 * 
	 * @return See above.
	 */
	private JPanel createSelectedItemsPane()
	{
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(new JLabel("Selected:"), BorderLayout.NORTH);
		p.add(new JScrollPane(selectedItemsListbox), BorderLayout.CENTER);
		populateSelectedItems();
		return p;
	}
	
	/**
	 * Builds and lays out the buttons used to select tags.
	 * 
	 * @return See above.
	 */
	private JPanel createSelectionPane()
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createVerticalStrut(30));
		buttonPanel.add(addButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(removeButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(addAllButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(removeAllButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		return buttonPanel;
	}
	
	/**
	 * Builds and lays out the available tags.
	 * 
	 * @return See above.
	 */
	private JPanel createAvailableItemsPane()
	{
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(new JLabel("Available:"), BorderLayout.NORTH);
		p.add(new JScrollPane(availableItemsListbox), BorderLayout.CENTER);
		populateAvailableItems();
		return p;
	}
	
	/** Updates the currentFields list box. */
	private void populateAvailableItems()
	{
	    DefaultListModel listModel = new DefaultListModel();
		for (Object item : availableItems)
			listModel.addElement(item);
		availableItemsListbox.setModel(listModel);
		
	}

	/** Updates the remaining fields list box. */
	private void populateSelectedItems()
	{
	    DefaultListModel listModel = new DefaultListModel();
		for (Object item : selectedItems)
			listModel.addElement(item);
		
		selectedItemsListbox.setModel(listModel);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param owner				The owner of this dialog.
	 * @param availableItems	The collection of available tags.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> availableItems)
	{
		this(owner, availableItems, null);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param owner				The owner of this dialog.
	 * @param availableItems	The collection of available items.
	 * @param selectedItems		The collection of selected items.
	 */
	public SelectionWizard(JFrame owner, Collection<Object> availableItems, 
							Collection<Object> selectedItems)
	{
		super(owner);
		setModal(true);
		if (selectedItems == null) selectedItems = new ArrayList<Object>();
		this.availableItems = availableItems;
		this.selectedItems = selectedItems;
		createOriginalSelection();
		initComponents();
		buildUI();
		setSize(500, 500);
	}

	/**
	 * Sets the title, the text and the icon displayed in the header.
	 * 
	 * @param title		The title to set.
	 * @param text		Tht text to set.
	 */
	public void setTitle(String title, String text)
	{
		setTitle(title, text, null);
	}
	
	/**
	 * Sets the title, the text and the icon displayed in the header.
	 * 
	 * @param title		The title to set.
	 * @param text		Tht text to set.
	 * @param titleIcon	The icon to set.
	 */
	public void setTitle(String title, String text, Icon titleIcon)
	{
		setTitle(title);
		if (titleIcon == null) {
			IconManager icons = IconManager.getInstance();
			titleIcon = icons.getIcon(IconManager.WIZARD_48);
		}
		TitlePanel tp = new TitlePanel(title, text,	titleIcon);
		getContentPane().add(tp, BorderLayout.NORTH);
	}
	
	/**
	 * Reacts to event fired by the various controls.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt)
	{
		int id = Integer.parseInt(evt.getActionCommand());
		switch (id) {
			case ADD:
				addItem();
				break;
			case ADD_ALL:
				addAllItems();
				break;
			case REMOVE:
				removeItem();
				break;
			case REMOVE_ALL:
				removeAllItems();
				break;
			case ACCEPT:
				accept();
				break;
			case CANCEL:
				cancel();
				break;
			case RESET:
				reset();
		}
	}

}
