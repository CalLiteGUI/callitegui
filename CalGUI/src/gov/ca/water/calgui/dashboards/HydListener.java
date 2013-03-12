package gov.ca.water.calgui.dashboards;

import gov.ca.water.calgui.utils.DataFileTableModel;
import gov.ca.water.calgui.utils.FileUtils;
import gov.ca.water.calgui.utils.GUILinks;
import gov.ca.water.calgui.utils.GUIUtils;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.swixml.SwingEngine;

public class HydListener implements ItemListener {
	private final JFrame desktop;
	private final SwingEngine swix;
	private JComponent component;
	private final Boolean[] RegUserEdits;
	private final DataFileTableModel[] dTableModels;
	private final GUILinks gl;
	private int action_WSIDI;

	public HydListener(JFrame desktop, SwingEngine swix, Boolean[] RegUserEdits, DataFileTableModel[] dTableModels, GUILinks gl,
	        int action_WSIDI) {
		this.desktop = desktop;
		this.swix = swix;
		this.RegUserEdits = RegUserEdits;
		this.dTableModels = dTableModels;
		this.gl = gl;
		this.action_WSIDI = action_WSIDI;

	}

	@Override
	public void itemStateChanged(ItemEvent ie) {
		JComponent component = (JComponent) ie.getItem();

		String cName = component.getName();
		if (cName != null) {

			if (cName.startsWith("hyd_rdb20")) {

				// Handling for update of DSS files

				// Confirm CWP/SWP overwrite;

				if (((JRadioButton) component).isSelected() && action_WSIDI != 2) {

					// Actions are only performed if selection is being set to true

					GUIUtils.toggleEnComponentAndChildren(swix.find("hyd_CC"), false);

					int option = JOptionPane.YES_OPTION; // default is to overwrite everything
					boolean isntDefault = false;
					if (RegUserEdits != null) {
						if (RegUserEdits[Integer.parseInt(gl.tableIDForCtrl("op_btn1"))] != null)
							isntDefault = isntDefault || RegUserEdits[Integer.parseInt(gl.tableIDForCtrl("op_btn1"))];
						if (RegUserEdits[Integer.parseInt(gl.tableIDForCtrl("op_btn2"))] != null)
							isntDefault = isntDefault || RegUserEdits[Integer.parseInt(gl.tableIDForCtrl("op_btn2"))];

					}
					if ((action_WSIDI == 1) && (isntDefault))

						// Prompt is needed only in "regular" processing (action_WSIDI = 1) with non-default table

						option = JOptionPane
						        .showConfirmDialog(
						                desktop,
						                "You have made changes to the SWP and/or CVP WSI/DI curves. \n"
						                        + "Do you want to overwrite these changes with the default values for the configuration ("
						                        + ((JRadioButton) component).getText()
						                        + " ) you have selected?\n\n"
						                        + "Press YES to overwrite, NO to use these values in the selected configuration, or Cancel to revert");

					// Once option is determined, process ...

					if (option == JOptionPane.CANCEL_OPTION) {

						// Cancel: undo change by selecting "other" radio button
						// TODO: ISSUE 116 - this logic won't quite be right.

						String newcName = null;
						if (cName.equals("hyd_rdb2005"))
							newcName = "hyd_rdb2030";
						else if (cName.equals("hyd_rdb2030"))
							newcName = "hyd_rdb2005";
						else if (cName.equals("run_rdbD1641"))
							newcName = "run_rdbBO";
						else if (cName.equals("run_rdbBO"))
							newcName = "run_rdbD1641";

						action_WSIDI = 2; // Skip all actions on update

						System.out.println(cName + ":-" + newcName);
						((JRadioButton) swix.find(newcName)).setSelected(true);

						action_WSIDI = 1;

					} else {

						// Yes or no: first determine which GUI_link4.table row to use

						String hydDSSStrings[] = GUIUtils.getHydDSSStrings(swix);

						if (hydDSSStrings[1] != null) {

							// Then update GUI values, files in Default\Lookup\directory

							((JTextField) swix.find("hyd_DSS_Index")).setText(hydDSSStrings[0]);
							((JTextField) swix.find("hyd_DSS_SV")).setText(hydDSSStrings[1]);
							((JTextField) swix.find("hyd_DSS_SV_F")).setText(hydDSSStrings[2]);
							((JTextField) swix.find("hyd_DSS_Init")).setText(hydDSSStrings[3]);
							((JTextField) swix.find("hyd_DSS_Init_F")).setText(hydDSSStrings[4]);

							// TODO: ? is this call needed?
							FileUtils.copyWSIDItoLookup(hydDSSStrings[7], System.getProperty("user.dir") + "\\Default\\Lookup");

							if ((action_WSIDI == 1) && (option == JOptionPane.YES_OPTION)) {

								// Force CVP and SWP tables to be reset from files

								String fileName = System.getProperty("user.dir") + "\\Default\\Lookup\\"
								        + gl.tableNameForCtrl("op_btn1") + ".table";
								int tID = Integer.parseInt(gl.tableIDForCtrl("op_btn1"));
								dTableModels[tID] = new DataFileTableModel(fileName, tID);

								fileName = System.getProperty("user.dir") + "\\Default\\Lookup\\" + gl.tableNameForCtrl("op_btn2")
								        + ".table";
								tID = Integer.parseInt(gl.tableIDForCtrl("op_btn2"));
								dTableModels[tID] = new DataFileTableModel(fileName, tID);

								JTable table = (JTable) swix.find("tblOpValues");
								table.setModel(dTableModels[tID]);

							}
						}
					}
				}
			} else if (cName.startsWith("hyd_rdbCC")) {

				GUIUtils.toggleEnComponentAndChildren(swix.find("hyd_CC"), true);

			} else if (cName.startsWith("hyd_ckb")) {

				// Checkbox in Climate Scenarios page changed
				int selct = 0;
				JPanel hyd_CC1 = (JPanel) swix.find("hyd_CC");
				// JPanel hyd_CC2 = (JPanel) swix.find("hyd_CC2");
				selct = GUIUtils.countSelectedButtons(hyd_CC1, JCheckBox.class, selct);
				// selct = GUIUtils.countSelectedButtons(hyd_CC2, JCheckBox.class, selct);

				JLabel lab = (JLabel) swix.find("hydlab_selected");
				if (selct == 0) {
					lab.setText("0 realizations selected");
				} else if (selct == 1) {
					lab.setText("1 realization selected - Deterministic mode required");
				} else {
					lab.setText(selct + " realizations selected - Probabilistic mode required");
				}

			} else if (cName.startsWith("hyd_rdb")) {
				// Radio in Hydroclimate

				/*
				 * JPanel hyd_CC = (JPanel) swix.find("hyd_CC"); JPanel hyd_CC1 = (JPanel) swix.find("hyd_CC1"); JPanel hyd_CC2 =
				 * (JPanel) swix.find("hyd_CC2");
				 * 
				 * if (cName.startsWith("hyd_rdbHis")) { GUIUtils.toggleEnComponentAndChildren(hyd_CC, ie.getStateChange() !=
				 * ItemEvent.SELECTED); GUIUtils.toggleEnComponentAndChildren(hyd_CC1, ie.getStateChange() != ItemEvent.SELECTED);
				 * GUIUtils.toggleSelComponentAndChildren(hyd_CC1, false, JCheckBox.class);
				 * GUIUtils.toggleSelComponentAndChildren(hyd_CC2, false, JCheckBox.class); } else if
				 * (cName.startsWith("hyd_rdbMid") || cName.startsWith("hyd_rdbEnd")) {
				 * GUIUtils.toggleEnComponentAndChildren(hyd_CC, ie.getStateChange() == ItemEvent.SELECTED);
				 * GUIUtils.toggleEnComponentAndChildren(hyd_CC1, ie.getStateChange() == ItemEvent.SELECTED);
				 * GUIUtils.toggleEnComponentAndChildren(hyd_CC2, ie.getStateChange() == ItemEvent.SELECTED); }
				 */
			}
		}

	}

}