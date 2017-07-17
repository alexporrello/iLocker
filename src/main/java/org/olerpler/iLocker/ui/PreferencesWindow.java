package org.olerpler.iLocker.ui;

import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.olerpler.iLocker.encryption.Preferences;

import layout.GBC;
import text.JMLabel;
import clickables.JMCheckBox;
import colors.JMColor;
import displays.JMFrame;
import displays.JMPanel;

public class PreferencesWindow extends JMFrame {
	private static final long serialVersionUID = -1546829852242828531L;

	public Preferences preferences;

	public JComponent selectedWindow;

	PreferencesWindow(Preferences preferences) {
		setLocationByPlatform(true);
		setSize(500, 500);
		setLayout(new GridBagLayout());
		
		this.preferences = preferences;
		selectedWindow = new SecurityTab();
		
		GBC.addWithGBC(this, new OptionButtons(), 0.0, 0.0, 0, 0, GBC.HORIZ, 
				GBC.NORTHWEST, GBC.insets(10, 10, 10, 0),  1);		
		setWindowInView(selectedWindow);
	}

	private void setWindowInView(JComponent component) {
		if(!component.getName().equals(this.selectedWindow.getName())) {
			remove(selectedWindow);
			this.selectedWindow = component;
			GBC.addWithGBC(this, selectedWindow,   1.0, 1.0, 1, 0, GBC.NONE, 
					GBC.NORTHWEST, GBC.insets(10, 10, 20, 20), 1);

			revalidate();
			repaint();
		}
	}

	private class OptionButtons extends JMPanel {
		private static final long serialVersionUID = -8499199098088494143L;

		OptionButton general  = new OptionButton("General");
		OptionButton security = new OptionButton("Security");

		public OptionButtons() {
			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(JMColor.DARK_GRAY, 1),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			int y = 0;

			setUpButton(general, new GeneralTab());
			setUpButton(security, new SecurityTab());

			GBC.addWithGBC(this, general,  0.0, 1.0, 0, y++, GBC.BOTH, 
					GBC.EAST, GBC.insets(0, 0, 5, 0), 1);
			GBC.addWithGBC(this, security, 0.0, 1.0, 0, y++, GBC.BOTH, 
					GBC.EAST, GBC.insets(0, 0, 5, 0), 1);

		}
		
		private void setUpButton(OptionButton button, JComponent inView) {
			button.addActionListener(e -> {
				setWindowInView(inView);
				
				OptionButton[] ob = {general, security};
				for(OptionButton o : ob) {
					o.setBackground(getBackground());
				}
				
				button.setBackground(JMColor.LIGHT_ORANGE);
			});
		}
	}

	private class GeneralTab extends JMPanel {
		private static final long serialVersionUID = -3648786244184841884L;

		public GeneralTab() {
			setName("General Tab");
		}
	}

	private class SecurityTab extends JMPanel {
		private static final long serialVersionUID = 738456184350958897L;

		public JMCheckBox decryptAllOpen = new 
				JMCheckBox("Decrypt all files on open?", JMColor.BLACK, 
						preferences.decryptAllOnOpen);
		public JMCheckBox encryptSystemTray = new 
				JMCheckBox("Encrypt all files when system tray?", JMColor.BLACK, 
						preferences.encryptWhenMinToSysTray);

		public SecurityTab() {
			setName("Security Tab");
			setLayout(new GridBagLayout());

			decryptAllOpen.addActionListener(e -> {
				preferences.decryptAllOnOpen = decryptAllOpen.isEnabled();
			});
			encryptSystemTray.addActionListener(e -> {
				preferences.encryptWhenMinToSysTray = encryptSystemTray.isEnabled();
			});
			
			GBC.addWithGBC(this, decryptAllOpen,    0.0, 0.0, 0, 0, GBC.NONE, GBC.WEST, GBC.insets(0, 0, 0, 0), 1);
			GBC.addWithGBC(this, encryptSystemTray, 0.0, 0.0, 0, 1, GBC.NONE, GBC.WEST, GBC.insets(0, 0, 0, 0), 1);
		}

	}

	private class OptionButton extends JMLabel {
		private static final long serialVersionUID = -4879022369727459719L;

		public OptionButton(String text) {
			this.setText(text);
		}
		
		public void addActionListener(Consumer<MouseEvent> listener) {
			addMouseClickedListener(listener);
		}
	}
	
	public static void main(String[] args) {
		PreferencesWindow pw = new PreferencesWindow(new Preferences());
		pw.setDefaultCloseOperation(JMFrame.EXIT_ON_CLOSE);
		pw.setVisible(true);
	}

}

