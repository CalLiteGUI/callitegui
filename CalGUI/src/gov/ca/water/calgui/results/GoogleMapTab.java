package gov.ca.water.calgui.results;

import gov.ca.water.calgui.CalLiteHelp;
import gov.ca.water.calgui.MainMenu;
import gov.ca.water.calgui.utils.GUIUtils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserType;
import com.teamdev.jxbrowser.events.TitleChangedEvent;
import com.teamdev.jxbrowser.events.TitleListener;

public class GoogleMapTab {

	private static Logger log = Logger.getLogger(GoogleMapTab.class.getName());
	private String urlString = "http://callitewebapp.appspot.com";
	private Browser browser = BrowserFactory.createBrowser(BrowserType.Mozilla);
	private static SwingEngine swix = MainMenu.getSwix();

	private Component bc;

	public GoogleMapTab() {

		this.initializeBrowser();

	}

	private void initializeBrowser() {

		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
			// Object objData = urlConnect.getContent();
		} catch (UnknownHostException e) {

			this.showErrorDialog(e);
			log.debug(e.getMessage());
			return;

		} catch (Exception e) {
			this.showErrorDialog(e);
			log.debug(e.getMessage());
			return;

		}

		browser.navigate(urlString);

		browser.addTitleListener(new TitleListener() {
			@Override
			public void titleChanged(TitleChangedEvent arg0) {

				String title = browser.getTitle();
				if (title.contains(":")) {
					String[] subtitles = title.split(":");
					if (subtitles.length == 2) {
						if (!subtitles[1].startsWith("AD_") && !subtitles[1].startsWith("I_")) {
							JList lstScenarios = (JList) swix.find("SelectedList");
							if (lstScenarios.getModel().getSize() < 1) {
								if (GUIUtils.controlFrame == null)
									GUIUtils.controlFrame = new ControlFrame(swix);
								GUIUtils.controlFrame.display();
								JOptionPane.showMessageDialog(GUIUtils.controlFrame, "No scenarios loaded.");

								System.out.println("No scenarios loaded!");
							} else {
								DisplayFrame.showDisplayFrames(DisplayFrame.quickState() + ";Locs-" + subtitles[1] + ";Index-"
								        + subtitles[1], lstScenarios);
							}
						}
					}
				}
			}
		});

	}

	public JPanel getWebTab() {

		JPanel jPanel = new JPanel();

		jPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 5, 5);

		bc = browser.getComponent();
		setSizes(1000, 670);

		jPanel.add(bc, c);

		JButton btnControls = new JButton("Controls");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 5, 5);
		btnControls.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (GUIUtils.controlFrame == null)
					GUIUtils.controlFrame = new ControlFrame(swix);

				GUIUtils.controlFrame.display();

			}
		});

		jPanel.add(btnControls, c);

		JButton btnHelp = new JButton("Help");
		c.gridx = 1;
		btnHelp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JTabbedPane jtp = (JTabbedPane) swix.find("tabbedPane1");
				String label = jtp.getTitleAt(jtp.getSelectedIndex());
				CalLiteHelp calLiteHelp = new CalLiteHelp();

				calLiteHelp.showHelp(label);

			}
		});

		jPanel.add(btnHelp, c);

		JButton btnTest = new JButton("JOptionPane Test");
		c.gridx = 2;
		btnTest.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "Test message");
			}
		});

		jPanel.add(btnTest, c);
		return jPanel;
	}

	public void setSizes(int width, int height) {
		bc.setMinimumSize(new Dimension(width, height));
		bc.setPreferredSize(new Dimension(width, height));

	}

	private void showErrorDialog(Exception e) {

		JOptionPane.showMessageDialog(null, "Couldn't find the website at " + urlString + ". " + e.getMessage(), "Warning!",
		        JOptionPane.WARNING_MESSAGE);
	}

}