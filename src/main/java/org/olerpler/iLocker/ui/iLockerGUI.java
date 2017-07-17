package org.olerpler.iLocker.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

import layout.GBC;

import org.olerpler.iLocker.encryption.EncryptionException;
import org.olerpler.iLocker.encryption.EncryptionManager;

import text.JMFont;
import text.JMLabel;
import clickables.JMButton;
import colors.HoverColor;
import colors.JMColor;
import displays.JMFrame;
import displays.JMPanel;

public class iLockerGUI extends JMFrame {
	private static final long serialVersionUID = -1920730013149358942L;

	EncryptionManager em = new EncryptionManager();
	PasswordWindow    pw = new PasswordWindow();

	public iLockerGUI() {
		setTitle("iLocker");
		setPreferredSize(new Dimension(650, 400));
		setLayout(new BorderLayout());
		setIconImages(imageIcon());
		setBackground(JMColor.DARK_GRAY);
		setLocationByPlatform(true);
		addWindowClosingListener();
		add(pw, BorderLayout.CENTER);
		
		try {
			em.runProgram("3Francis");
			openFileListWindow();
		} catch (EncryptionException | IOException e) {
			e.printStackTrace();
		}
		
		systemTray();
		
		pack();
	}
	
	private void minimizeToSystemTray() {
		this.setVisible(false);
	}
	
	private void restoreWindow() {
		this.setVisible(true);
	}
	
	private void systemTray() {
		TrayIcon trayIcon = null;
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			PopupMenu popup = new PopupMenu();

			MenuItem restore = new MenuItem("Restore Window");
			restore.addActionListener(e -> {
				restoreWindow();
			});
			popup.add(restore);
			
			MenuItem close = new MenuItem("Close");
			close.addActionListener(e -> {
				minimizeToSystemTray();
				em.exit();
				System.exit(0);
			});
			popup.add(close);
			
			trayIcon = new TrayIcon(loadImage("/iLocker_icon_16x16.png"), 
					"Tray Demo", popup);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println(e);
			}
		}
	}
	
	public void addWindowClosingListener() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				minimizeToSystemTray();
				em.exit();
				System.exit(0);
			}
		});
	}
	
	/**
	 * @return An array of Calvin's ImageIcons
	 */
	private ArrayList<Image> imageIcon() {
		ArrayList<Image> icons = new ArrayList<Image>();

		icons.add(loadImage("/iLocker_icon_256x256.png"));
		icons.add(loadImage("/iLocker_icon_128x128.png"));
		icons.add(loadImage("/iLocker_icon_16x16.png"));
		icons.add(loadImage("/iLocker_icon_32x32.png"));
		icons.add(loadImage("/iLocker_icon_48x48.png"));
		
		return icons;
	}
	
	/**
	 * For use in the {@link #imageIcon()} method.
	 * @param url the image's url
	 * @return the loaded image
	 */
	private Image loadImage(String url) {
		return new ImageIcon(
				iLockerGUI.class.getClass().getResource(url)).getImage();
	}
	
	private void openFileListWindow() {
		remove(pw);
		setLayout(new GridBagLayout());
		
		FileListWindow flw = new FileListWindow(em);		
		flw.makeSortingButtonsAndAddToComponent(this);
		GBC.addWithGBC(this, flw.generateScrollingPane(flw), 
				1.0, 1.0, 0, 1, GBC.BOTH,  GBC.CENT, GBC.insets(0, 0, 0, 0), 3);
		
		setJMenuBar(new MenuBar());
		
		revalidate();
		repaint();
	}

	private class PasswordWindow extends JMPanel {
		private static final long serialVersionUID = 8723599947895055439L;

		JMLabel label = new JMLabel("Please enter a valid password.");
		
		public PasswordWindow() {
			setLayout(new GridBagLayout());
			setBackground(JMColor.WHITE);
			
			label.setOpaque(false);
			label.setForeground(JMColor.LIGHT_ORANGE);
			label.setVisible(false);
			JLabel iLocker = new JLabel(new ImageIcon(
					iLockerGUI.class.getClass().getResource("/iLocker.png")));
			JMLabel string = new JMLabel(determineString());
			string.setOpaque(false);
			JMButton unlock = new JMButton("Unlock");
			unlock.setColor(new HoverColor(JMColor.LIGHT_ORANGE, JMColor.LIGHT_GRAY));
			unlock.setForeground(JMColor.WHITE);
			
			GBC.addWithGBC(this, iLocker,
					0.0, 0.0, 0, 0, GBC.NONE, GBC.CENT, GBC.insets(10, 25, 10, 25), 1);
			GBC.addWithGBC(this, string,
					0.0, 0.0, 0, 1, GBC.HORIZ, GBC.CENT, GBC.insets(0, 15, 0, 15), 1);
			GBC.addWithGBC(this, password(), 
					0.0, 0.0, 0, 2, GBC.HORIZ, GBC.CENT, GBC.insets(0, 10, 0, 10), 1);
			GBC.addWithGBC(this, label, 
					0.0, 0.0, 0, 3, GBC.HORIZ, GBC.CENT, GBC.insets(0, 10, 0, 10), 1);
			GBC.addWithGBC(this, unlock, 
					0.0, 0.0, 0, 4, GBC.HORIZ, GBC.CENT, GBC.insets(5, 10, 10, 10), 1);
		}
		
		public String determineString() {
			if(EncryptionManager.ENCRYPTED_TEST.exists()) {
				return "";
			} else {
				return "Please enter a new password.";
			}
		}
		
		public JPasswordField password() {
			JPasswordField password = new JPasswordField();
			password.setBackground(JMColor.LIGHT_ORANGE);
			password.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			password.setHorizontalAlignment(SwingConstants.CENTER);
			password.setFont(JMFont.DEFAULT);
			
			password.addKeyListener(new KeyAdapter() {				
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						try {
							String pass = "";
							for(char c : password.getPassword()) {
								pass = pass + c;
							}

							em.runProgram(pass);
							openFileListWindow();
						} catch (EncryptionException e1) {
							label.setVisible(true);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			
			return password;
		}
	}
	
	private class MenuBar extends JMenuBar {
		private static final long serialVersionUID = -542440701382387549L;

		public MenuBar() {
			JMenu file = new JMenu("File");
			
			JMenuItem preferences = new JMenuItem("Preferences");
			preferences.addActionListener(e -> {
				new PreferencesWindow(em.preferences).setVisible(true);
			});
			file.add(preferences);
			
			file.addSeparator();
			
			JMenuItem minimize = new JMenuItem("Minimize to the System Tray");
			minimize.addActionListener(e -> {
				minimizeToSystemTray();
			});
			file.add(minimize);
			
			add(file);
		}
	}
}
