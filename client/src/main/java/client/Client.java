package client;

import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

enum UIType {
	CLI, GUI
}

public class Client {

	static String exec;
	static String bucketName = null;
	static UIType uiType = UIType.CLI;
	static boolean desktopMode = false;

	public static void main(String[] args) throws Exception {
		if (args.length <= 0) {
			// in actuality, we want to make this desktop-safe.
			desktopMode = true;
			uiType = UIType.GUI;
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
		DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
		terminalFactory.setTerminalEmulatorTitle("AWS Media Console");
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

			if (desktopMode){
				// let's get all the information we can gather from the args
				Label bucketPrompt = new Label("S3 Bucket Name");
				TextBox bucketTextBox = new TextBox();
				Label execPrompt = new Label("Executable to run for media");
				TextBox execTextBox = new TextBox();
				Button submitButton = new Button("Submit");

				final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
				final Window window = new BasicWindow("AWS Media Console - Enter Bucket Info");
				Panel contentPanel = new Panel(new GridLayout(1));
				GridLayout gridLayout = (GridLayout) contentPanel.getLayoutManager();
				gridLayout.setHorizontalSpacing(4);

				contentPanel.addComponent(bucketPrompt);
				contentPanel.addComponent(bucketTextBox);
				contentPanel.addComponent(execPrompt);
				contentPanel.addComponent(execTextBox);

				submitButton.addListener((Button button) -> {
					bucketName = bucketTextBox.getText();
					exec = execTextBox.getText();
					window.close();
				});

				contentPanel.addComponent(submitButton);
				window.setComponent(contentPanel);
				textGUI.addWindowAndWait(window);
			}


			System.err.println("Initializing handler for " + bucketName + " executing " + exec);
			Handler h = new Handler(bucketName, AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build());

			final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
			final Window window = new BasicWindow("AWS Media Console - " + bucketName);
			Panel contentPanel = new Panel(new GridLayout(1));

			GridLayout gridLayout = (GridLayout) contentPanel.getLayoutManager();
			gridLayout.setHorizontalSpacing(4);

			List<String> topLevelDirs = h.returnListOfAllTopLevelFolders();
			ComboBox<String> topLevelComboBox = new ComboBox<>(topLevelDirs);
			topLevelComboBox.setReadOnly(true);
			topLevelComboBox.addListener(new AWSComboBoxListener(topLevelComboBox, contentPanel, true, h, exec));

			contentPanel.addComponent(topLevelComboBox);

			window.setComponent(contentPanel);

			textGUI.addWindowAndWait(window);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
