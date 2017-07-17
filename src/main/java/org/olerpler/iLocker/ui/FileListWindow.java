package org.olerpler.iLocker.ui;

import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import layout.GBC;

import org.olerpler.iLocker.encryption.EncryptionManager;

import clickables.JMButton;
import colors.HoverColor;
import colors.JMColor;
import displays.JMFrame;
import displays.JMPanel;
import displays.JMScrollPane;

public class FileListWindow extends JMPanel {
	private static final long serialVersionUID = 2446692568487393150L;

	ArrayList<FancyFileButton> buttons = new ArrayList<FancyFileButton>();

	EncryptionManager em;

	public FileListWindow(EncryptionManager em) {
		this.em = em;
		setLayout(new GridBagLayout());
		addAllFilesToView();
		setUpDropTarget(this);
	}

	public JMScrollPane generateScrollingPane(FileListWindow flw) {
		JMScrollPane scroll = new JMScrollPane(flw);
		scroll.getViewport().setBackground(JMColor.WHITE);
		scroll.setScrollBarWidth(15);
		scroll.setButtonsVisible(false);
		setUpDropTarget(scroll);
		return scroll;
	}

	private void addAllFilesToView() {
		File[] files = EncryptionManager.ENCRYPTED_FOLDER.listFiles();

		buttons.clear();

		int y = 0;
		for(File f : files) {
			try {
				if(!f.getAbsolutePath().equals(EncryptionManager.ENCRYPTED_TEST.getAbsolutePath())) {
					buttons.add(new FancyFileButton(f, em));
				}
			} catch (NoSuchFileException e) {
				e.printStackTrace();
			}
		}

		for(FancyFileButton ffb : buttons) {
			ffb.addToComponent(this, y++);
		}
	}

	private void rebuildFromButtonArray() {
		removeAll();

		int y = 0;
		for(FancyFileButton ffb : buttons) {
			ffb.addToComponent(this, y++);
		}
	}

	private void addFileToView(File file) {
		try {
			buttons.add(new FancyFileButton(file, em));
			rebuildFromButtonArray();
			revalidate();
			repaint();
		} catch (NoSuchFileException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Makes it so users can open files by dragging and dropping
	 */
	private void setUpDropTarget(JComponent component) {
		component.setDropTarget(new DropTarget(this, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent e) {				
				try {
					Transferable t = e.getTransferable();
					int d = e.getDropAction();
					e.acceptDrop(d);

					String url = t.getTransferData(DataFlavor.javaFileListFlavor).toString();
					url = url.substring(1, url.length()-1);

					e.dropComplete(true);

					String[] urls = url.split(", ");

					for(String s : urls) {
						addFileToView(transferFile(s));
					}
				} catch (UnsupportedFlavorException | IOException e1) {
					e1.printStackTrace();
				}
			}
		}));
	}

	private File transferFile(String url) throws NoSuchFileException, IOException {
		File original = new File(url);

		if(original.exists()) {
			String name = original.getName();

			File newFile = new File(EncryptionManager.DECRYPTED_FOLDER + "/" + name);

			Files.move(original.toPath(), newFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);

			return newFile;
		} else {
			throw new NoSuchFileException("The file coud not be moved because "
					+ "no such file exists.");
		}
	}

	public void makeSortingButtonsAndAddToComponent(JMFrame component) {
		JMButton name     = new JMButton("Name");
		JMButton modified = new JMButton("Date modified");
		JMButton size     = new JMButton("Size");

		setUpFileSortingButton(name,     FileSortMethod.NAME);
		setUpFileSortingButton(modified, FileSortMethod.NAME);
		setUpFileSortingButton(size,     FileSortMethod.NAME);

		GBC gbc     = new GBC();
		gbc.fill    = GBC.HORIZ;

		gbc.weightx = 1.0;
		gbc.gridx   = 0;
		gbc.insets  = GBC.insets(0, 0, 0, 1);
		component.add(name, gbc);
		gbc.weightx = 0.5;
		gbc.gridx   = 1;
		component.add(modified, gbc);
		gbc.weightx = 0.5;
		gbc.gridx   = 2;
		gbc.insets  = GBC.insets(0, 0, 0, 0);
		component.add(size, gbc);
	}
	
	/**
	 * Sets up a given file sorting button
	 * @param button the button to be setup
	 * @param fsm the file sort method the button should trigger
	 */
	private void setUpFileSortingButton(JMButton button, FileSortMethod fsm) {
		button.focusDrawHeight = 0;
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.font = JMColor.BLACK;
		button.setColor(new HoverColor(JMColor.ACCENT, JMColor.WHITE));
		button.addActionListener(e -> {
			for(FancyFileButton ffb : buttons) {
				ffb.lestToGreatest = !ffb.lestToGreatest;
				ffb.setFileSortMethod(fsm);
			}

			Collections.sort(buttons);
			rebuildFromButtonArray();
			revalidate();
			repaint();
		});
	}
}