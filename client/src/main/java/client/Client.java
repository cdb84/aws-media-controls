package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.TextGUI.Listener;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

enum UIType {
	CLI, GUI
}

public class Client {

	static String exec;

	public static void main(String[] args) throws Exception {
		String bucketName = null;
		exec = null;
		UIType uiType = UIType.CLI;
		if (args.length <= 0) {
			throw new IllegalArgumentException();
		}
		if (args.length >= 1) {
			bucketName = args[0];
		}
		if (args.length == 2) {
			exec = args[1];
		} else {
			exec = "vlc"; // probably best default in mind
		}
		if (args.length == 3 && args[2].equals("--gui")) {
			uiType = UIType.GUI;
		}
		System.err.println("Initializing handler for " + bucketName + " executing " + exec);
		Handler h = new Handler(bucketName, AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build());

		DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
		Screen screen = null;
		try {
			Terminal terminal = null;
			if (uiType == UIType.CLI) {
				terminal = terminalFactory.createTerminal();
			} else {
				terminal = terminalFactory.createTerminalEmulator();
			}
			screen = new TerminalScreen(terminal);
			screen.startScreen();

			final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
			final Window window = new BasicWindow("AWS Media Console - " + bucketName);
			Panel contentPanel = new Panel(new GridLayout(1));

			GridLayout gridLayout = (GridLayout) contentPanel.getLayoutManager();
			gridLayout.setHorizontalSpacing(4);

			List<String> topLevelDirs = h.returnListOfAllTopLevelFolders();
			ComboBox<String> topLevelComboBox = new ComboBox<>(topLevelDirs);
			Stack<List<String>> comboBoxHistory = new Stack<List<String>>();
			topLevelComboBox.setReadOnly(true);
			topLevelComboBox.addListener((int selectedIndex, int previousSelection) -> {
				if (selectedIndex != previousSelection) {
					String selectedValue = topLevelComboBox.getItem(selectedIndex).replaceAll("/", "");
					
					// save the selections we had previously
					List<String> currentSelections = new ArrayList<String>();
					for (int i = 0; i < topLevelComboBox.getItemCount(); i++){
						currentSelections.add(topLevelComboBox.getItem(i));
					}
					System.err.println("Pushing to combobox history");
					comboBoxHistory.push(currentSelections);

					// now get the new selections
					List<String> subDirs = h.returnEverythingUnder(selectedValue)
							.stream().map(treeNode -> treeNode.toString()).collect(Collectors.toList());
					// if we have new selections in the first place, add them
					if (subDirs.size() != 0){
						for (String prevItem : currentSelections){
							topLevelComboBox.removeItem(prevItem);
						}
						for (String item : subDirs) {
							topLevelComboBox.addItem(item);
						}
					}
				}
			});

			Button backButton = new Button("Back");
			backButton.addListener((Button button) -> {
				if (comboBoxHistory.size() == 0){ return; }
				// get whatever is in there currently 
				List<String> currentSelections = new ArrayList<String>();
				for (int i = 0; i < topLevelComboBox.getItemCount(); i++){
					currentSelections.add(topLevelComboBox.getItem(i));
				}

				// remove it
				for (String item : currentSelections){
					System.err.println("Removing "+item);
					topLevelComboBox.removeItem(item);
				}

				// now add back what was in there previously
				for (String item : comboBoxHistory.pop()){
					System.err.println("Adding "+item);
					topLevelComboBox.addItem(item);
				}
			});
			Button launchButton = new Button("Launch in " + exec);
			launchButton.addListener((Button button) -> {
				String selectedValue = topLevelComboBox.getSelectedItem().replaceAll("/", "");
				// we have to launch whatever was picked
				String path = "";
				for (DirectoryTreeNode<String> parent : h.returnEverythingAbove(selectedValue)){
					if (parent.value != null){
						path = parent.value +"/"+ path;
					}
				}
				path += selectedValue;
				ProcessBuilder pb = new ProcessBuilder(exec, h.generatePresignedUrlFromKey(path).toString());
				try {
					pb.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			// Button launchPlayListButton = new Button("Launch as playlist in " + exec);

			contentPanel.addComponent(topLevelComboBox);
			contentPanel.addComponent(backButton);
			contentPanel.addComponent(launchButton);

			window.setComponent(contentPanel);

			textGUI.addWindowAndWait(window);

			Thread.sleep(200000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addForkedComboBoxorOpenPresign(Handler handler, Panel contentPanel, ComboBox<String> prevComboBox,
			int selection, String executionBinary) {
		// just get the children of whatever was selected
		String selectionValue = prevComboBox.getItem(selection).replaceAll("/", "");
		List<DirectoryTreeNode<String>> childrenOfSelection = handler.returnEverythingUnder(selectionValue);
		List<String> childrenOfSelectionValues = childrenOfSelection.stream().map(treeNode -> treeNode.toString())
				.collect(Collectors.toList());
		if (childrenOfSelection.size() == 0) {
			List<DirectoryTreeNode<String>> parentsOfSelection = handler.returnEverythingAbove(selectionValue);
			// should really execute here....
			String path = "";
			for (DirectoryTreeNode<String> parent : parentsOfSelection) {
				if (parent.value != null) {
					path = parent.value + "/" + path;
				}
			}
			path += selectionValue;
			ProcessBuilder pb = new ProcessBuilder(executionBinary, handler.generatePresignedUrlFromKey(path).toString());
			// pb.inheritIO(); // <-- passes IO from forked process.
			try {
				Process p = pb.start(); // <-- forkAndExec on Unix
				// p.waitFor(); // <-- waits for the forked process to complete.
			} catch (Exception e) {
				e.printStackTrace();
			}
			contentPanel.removeComponent(prevComboBox);
			return;
		}
		ComboBox<String> childComboBox = new ComboBox<>(childrenOfSelectionValues);
		childComboBox.setReadOnly(true);
		childComboBox.addListener((int selectedIndex, int previousSelection) -> {
			// contentPanel.removeComponent(childComboBox);
			addForkedComboBoxorOpenPresign(handler, contentPanel, childComboBox, selectedIndex, executionBinary);
		});
		prevComboBox.addListener((int selectedIndex, int previousSelection) -> {
			contentPanel.removeComponent(childComboBox);
		});
		contentPanel.addComponent(childComboBox);
	}
}