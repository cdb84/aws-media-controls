package client;

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

	AWSComboBoxListener(ComboBox<String> instance, Panel contentPanel, boolean topLevelInstance, Handler handler, String exec) {
		this.instance = instance;
		this.contentPanel = contentPanel;
		this.topLevelInstance = topLevelInstance;
		this.handler = handler;
		this.exec = exec;
	}

	AWSComboBoxListener(ComboBox<String> instance, Panel contentPanel, boolean topLevelInstance, Handler handler, String exec, AWSComboBoxListener parentListener){
		this(instance, contentPanel, topLevelInstance, handler, exec);
		this.parentListener = parentListener;
	}

	@Override
	public void onSelectionChanged(int selectedIndex, int previousSelection) {
		String selectedValue = instance.getItem(selectedIndex).replaceAll("/", "");
		// now get the new selections
		List<String> subObjects = handler.returnEverythingUnder(selectedValue).stream().map(treeNode -> treeNode.toString())
				.collect(Collectors.toList());
		if (selectedIndex != previousSelection) {
			// if we had children, they need to go.
			if (this.children != null && this.children.size() > 0){
				for(ComboBox<String> child : children){
					contentPanel.removeComponent(child);
				}
				this.children = null;
			}
			// if we have new selections in the first place, add them
			// as a new combo box to the panel
			if (subObjects.size() != 0) {
				if (this.children == null){
					this.children = new ArrayList<ComboBox<String>>();
				}
				ComboBox<String> child = new ComboBox<String>(subObjects);
				addChild(child);
				child.addListener(new AWSComboBoxListener(child, contentPanel, false, handler, exec, this));
				contentPanel.addComponent(child);
			}
			else if (instance.getItem(selectedIndex).charAt(instance.getItem(selectedIndex).length()-1) != '/'){
				exec(selectedValue);
			}
			
		}else if (subObjects.size() == 0 && instance.getItem(selectedIndex).charAt(instance.getItem(selectedIndex).length()-1) != '/'){
			exec(selectedValue);
		}

	}

	public void addChild(ComboBox<String> child){
		this.children.add(child);
		if(this.parentListener != null){
			this.parentListener.addChild(child);
		}
	}

	private void exec(String selectedValue){
		// have to execute the apprent file as a presign
		System.err.println("Executing via "+exec);
		String path = "";
		for (DirectoryTreeNode<String> parent : handler.returnEverythingAbove(selectedValue)){
			if (parent.value != null){
				path = parent.value +"/"+ path;
			}
		}
		path += selectedValue;
		ProcessBuilder pb = new ProcessBuilder(exec, handler.generatePresignedUrlFromKey(path).toString());
		try {
			pb.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}