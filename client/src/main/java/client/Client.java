package client;

import java.io.IOException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class Client {

	public static void main(String[] args) throws Exception {
		String bucketName = null;
		String exec = null;
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
		System.err.println("Initializing handler for " + bucketName + " executing " + exec);
		Handler h = new Handler(bucketName, AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build());

		DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();

		Terminal terminal = null;
		try {
			terminal = defaultTerminalFactory.createTerminal();
			terminal.enterPrivateMode();
			terminal.clearScreen();
			terminal.setCursorVisible(false);
			final TextGraphics textGraphics = terminal.newTextGraphics();

			textGraphics.putString(2, 1, "AWS Media Console - Press ESC to exit", SGR.BOLD);
			textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
			textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
			textGraphics.putString(5, 3, "Terminal Size: ", SGR.BOLD);
			textGraphics.putString(5 + "Terminal Size: ".length(), 3, terminal.getTerminalSize().toString());

			terminal.flush();

			KeyStroke keyStroke = terminal.readInput();
			Thread.sleep(2000);

			while (keyStroke.getKeyType() != KeyType.Escape) {
				textGraphics.drawLine(5, 4, terminal.getTerminalSize().getColumns() - 1, 4, ' ');
				textGraphics.putString(5, 4, "Last Keystroke: ", SGR.BOLD);
				textGraphics.putString(5 + "Last Keystroke: ".length(), 4, keyStroke.toString());
				terminal.flush();
				keyStroke = terminal.readInput();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (terminal != null) {
				try {
					terminal.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}