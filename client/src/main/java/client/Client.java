package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
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
			Panel contentPanel = new Panel(new GridLayout(4));

			GridLayout gridLayout = (GridLayout) contentPanel.getLayoutManager();
			gridLayout.setHorizontalSpacing(3);

			List<String> topLevelDirs = h.returnListOfAllTopLevelFolders();
			ComboBox<String> topLevelComboBox = new ComboBox<>(topLevelDirs);
			topLevelComboBox.setReadOnly(true);
			topLevelComboBox.addListener((int selectedIndex, int previousSelection) -> {
				if (selectedIndex != previousSelection){
					contentPanel.removeAllComponents();
					contentPanel.addComponent(topLevelComboBox);
				}
				addForkedComboBoxorOpenPresign(h, contentPanel, topLevelComboBox, selectedIndex, exec);
			});

			contentPanel.addComponent(topLevelComboBox);

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
			for (DirectoryTreeNode<String> parent : parentsOfSelection){
				if (parent.value != null){
					path = parent.value+"/"+path;
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