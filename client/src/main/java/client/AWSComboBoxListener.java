package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.Panel;

public class AWSComboBoxListener implements ComboBox.Listener {

	ComboBox<String> instance;
	Panel contentPanel;
	Handler handler;
	boolean topLevelInstance;
	List<ComboBox<String>> children;
	AWSComboBoxListener parentListener;
	String exec;

	AWSComboBoxListener(ComboBox<String> instance, Panel contentPanel, boolean topLevelInstance, Handler handler,
			String exec) {
		this.instance = instance;
		this.contentPanel = contentPanel;
		this.topLevelInstance = topLevelInstance;
		this.handler = handler;
		this.exec = exec;
	}

	AWSComboBoxListener(ComboBox<String> instance, Panel contentPanel, boolean topLevelInstance, Handler handler,
			String exec, AWSComboBoxListener parentListener) {
		this(instance, contentPanel, topLevelInstance, handler, exec);
		this.parentListener = parentListener;
	}

	public List<String> getSubObjects(String selectedValue) {
		try {
			return handler.returnEverythingUnder(selectedValue).stream().map(treeNode -> treeNode.toString())
					.collect(Collectors.toList());
		} catch (ValueNotFoundError e) {
			return new ArrayList<String>();
		}
	}

	@Override
	public void onSelectionChanged(int selectedIndex, int previousSelection) {
		String withSuffix = instance.getItem(selectedIndex);
		String selectedValue = withSuffix.replaceAll("/", "");
		if (selectedValue.equals("Open all as playlist")) {
			// open as playlist
			execAllOptions();
			return;
		}
		// now get the new selections
		List<String> subObjects = getSubObjects(selectedValue);
		if (subObjects.size() == 0 && withSuffix.charAt(withSuffix.length() - 1) != '/') {
			exec(selectedValue);
		} else {
			// if we had children, they need to go.
			if (this.children != null && this.children.size() > 0) {
				for (ComboBox<String> child : children) {
					contentPanel.removeComponent(child);
				}
				this.children = null;
			}
			// if we have new selections in the first place, add them
			// as a new combo box to the panel
			if (subObjects.size() != 0) {
				// right here we will add the option to open this in as a playlist
				subObjects.add("Open all as playlist");
				ComboBox<String> child = new ComboBox<String>(subObjects);
				addChild(child);
				child.addListener(new AWSComboBoxListener(child, contentPanel, false, handler, exec, this));
				contentPanel.addComponent(child);
			} else if (instance.getItem(selectedIndex).charAt(instance.getItem(selectedIndex).length() - 1) != '/') {
				exec(selectedValue);
			}

		}
	}

	public void addChild(ComboBox<String> child) {
		if (this.children == null) {
			this.children = new ArrayList<ComboBox<String>>();
		}
		this.children.add(child);
		if (this.parentListener != null) {
			this.parentListener.addChild(child);
		}
	}

	private void execAllOptions() {
		List<String> allOptions = new ArrayList<String>();
		allOptions.add(exec);
		// we use a magic -1 here because there is usually always a "Open all as
		// playlist option" and who knows
		// what would happen if we recursed there
		for (int i = 0; i < this.instance.getItemCount() - 1; i++) {
			String url;
			try {
				url = handler.generatePresignedUrlFromKey(buildPathFor(this.instance.getItem(i))).toString();
			} catch (ValueNotFoundError e) {
				url = "";
				e.printStackTrace();
			}
			allOptions.add(url);
		}
		String[] args = new String[allOptions.size()];
		createProcessBuilder(allOptions.toArray(args));
	}

	private String buildPathFor(String selectedValue) throws ValueNotFoundError {
		String path = "";
		List<DirectoryTreeNode<String>> above = handler.returnEverythingAbove(selectedValue);
		for (DirectoryTreeNode<String> parent : above) {
			if (parent.value != null) {
				path = parent.value + "/" + path;
			}
		}
		path += selectedValue;
		return path;
	}

	private void downloadFile(String presign, String outputName) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			createProcessBuilder("curl.exe", "--output", outputName, "--url", presign);
		} else {
			createProcessBuilder("wget", presign, "-O", outputName);
		}
	}

	private void exec(String selectedValue) {
		String path;
		try {
			path = buildPathFor(selectedValue);
		} catch (ValueNotFoundError e) {
			e.printStackTrace();
			return;
		}
		String presign = handler.generatePresignedUrlFromKey(path).toString();
		if (selectedValue.contains(".srt")) {
			downloadFile(presign, selectedValue);
		}
		// have to execute the apprent file as a presign
		else {
			String fileName = selectedValue.substring(0, selectedValue.lastIndexOf('.'));
			fileName += ".srt";
			String subtitlePath;
			try {
				subtitlePath = buildPathFor(fileName);
			} catch (ValueNotFoundError e) {
				subtitlePath = "";
			}
			String subtitlePresign = handler.generatePresignedUrlFromKey(subtitlePath).toString();
			if (!subtitlePath.equals("")) {
				downloadFile(subtitlePresign, fileName);
				if (exec.contains("vlc")) {
					createProcessBuilder(exec, presign, "--sub-file=" + fileName);
				}
			} else {
				createProcessBuilder(exec, presign);
			}
		}
	}

	private void createProcessBuilder(String... args) {
		ProcessBuilder pb = new ProcessBuilder(args);
		try {
			pb.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}