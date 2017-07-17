package org.olerpler.iLocker.ui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.NoSuchFileException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import layout.GBC;

import org.olerpler.iLocker.encryption.EncryptionManager;

import text.JMLabel;
import colors.JMColor;

/**
 * A FileButton that displays a file's name, modified date, and size.	
 * @author Alexander Porrello
 */
public class FancyFileButton implements Comparable<FancyFileButton> {
	JMLabel name;
	JMLabel modified;
	JMLabel size;
	
	/** The file associated with this FancyFileButton **/
	File file;
	
	/** Determines the sorting method of the files **/
	FileSortMethod fsm = FileSortMethod.NAME;
	
	/** Are files sorted least to greatest or opposite? **/
	boolean lestToGreatest = true;

	/**
	 * Constructor used to create a FancyFileButton out of a given file.
	 * @param file the file to be displayed in this button
	 * @throws NoSuchFileException if the file cannot be found
	 */
	public FancyFileButton(File file, EncryptionManager em) throws NoSuchFileException {
		this.file = file;

		if(file.exists()) {
			name = new JMLabel(file.getName());
			modified = new JMLabel(new SimpleDateFormat("dd/MM/yyyy hh:mm a").
					format(new Date(file.lastModified())));
			size = new JMLabel(readableFileSize(file.length()));
			size.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

			setUpButton(name, em);
			setUpButton(modified, em);
			setUpButton(size, em);
		} else {
			throw new NoSuchFileException("");
		}
	}

	/**
	 * Changes the sorting method of the FancyFileButton.
	 * @param fsm the FileSortMethod to change to
	 */
	public void setFileSortMethod(FileSortMethod fsm) {
		this.fsm = fsm;
	}

	/**
	 * Returns a long file size as an easy-to-read string
	 * @param size the size to be converted
	 * @return an easy-to-read string of the file's size.
	 */
	private String readableFileSize(long size) {
		if(size <= 0) return "0";
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size/Math.pow(
				1024, digitGroups)) + " " + units[digitGroups];
	}

	/**
	 * Sets up a given JMLabel as a button.
	 * @param button the button to be set up
	 */
	private void setUpButton(JMLabel button, EncryptionManager em) {
		button.setBackground(JMColor.WHITE);
		button.setForeground(JMColor.BLACK);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				changeAllColor(JMColor.LIGHT_ORANGE);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if(!button.hasFocus()) {
					changeAllColor(JMColor.WHITE);
				}
			}				
		});

		button.addMouseClickedListener(e -> {
			if(e.getClickCount() == 2) {
				em.openFile(file, true);
			}

			changeAllColor(JMColor.LIGHT_ORANGE);
			button.requestFocus();
		});
		button.addFocusGainedListener(e -> changeAllColor(
				JMColor.LIGHT_ORANGE));
		button.addFocusLostListener(e -> changeAllColor(
				JMColor.WHITE));
	}

	/**
	 * Changes the background color of all of the buttons.
	 * @param color the color to which to change the buttons
	 */
	private void changeAllColor(Color color) {
		name.setBackground(color);
		modified.setBackground(color);
		size.setBackground(color);
	}

	/**
	 * Adds the buttons to a given component, given this button's y posn.
	 * @param component the component to add this button to
	 * @param y the y position of the button in a GridBagLayout
	 */
	public void addToComponent(JComponent component, int y) {
		GBC gbc     = new GBC();
		gbc.fill    = GBC.HORIZ;
		gbc.gridy   = y++;

		gbc.weightx = 1.0;
		gbc.gridx   = 0;
		component.add(name, gbc);
		gbc.weightx = 0.5;
		gbc.gridx   = 1;
		component.add(modified, gbc);
		gbc.weightx = 0.5;
		gbc.gridx   = 2;
		component.add(size, gbc);
	}

	@Override
	public int compareTo(FancyFileButton e) {
		if(fsm.equals(FileSortMethod.NAME)) {
			if(lestToGreatest) {
				return e.name.getText().compareTo(name.getText());
			} else {
				return name.getText().compareTo(e.name.getText());
			}
		} else if(fsm.equals(FileSortMethod.MODIFIED)) {
			if(lestToGreatest) {
				return modified.getText().compareTo(e.modified.getText());
			} else {
				return e.modified.getText().compareTo(modified.getText());
			}
		} else {
			if(lestToGreatest) {
				return (file.length() < e.file.length())?-1:1;
			} else {
				return (file.length() > e.file.length())?-1:1;
			}
		}
	}
}
