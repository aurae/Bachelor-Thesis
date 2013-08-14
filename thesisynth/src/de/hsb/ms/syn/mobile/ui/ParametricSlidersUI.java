package de.hsb.ms.syn.mobile.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.hsb.ms.syn.common.ui.PropertyTable;
import de.hsb.ms.syn.common.util.NetMessageFactory;
import de.hsb.ms.syn.common.util.NetMessages;
import de.hsb.ms.syn.common.util.NetMessages.Command;
import de.hsb.ms.syn.common.util.Utils;
import de.hsb.ms.syn.common.vo.NetMessage;
import de.hsb.ms.syn.common.vo.NodeProperties;
import de.hsb.ms.syn.common.vo.NodeProperty;
import de.hsb.ms.syn.mobile.abs.ControllerUI;

/**
 * Parametric Sliders UI
 * 
 * First iteration of Controller UI. This one simply allows to remotely add or
 * remove Nodes from the synthesizer's surface
 * 
 * @author Marcel
 * 
 */
public class ParametricSlidersUI extends ControllerUI {

	// UI components
	private Table listPanel;
	private Table sliderPanel;

	private Map<Integer, PropertyTable> propertyTables;
	private int selectedListItem = -1;

	private List nodeList;

	// Rendering
	private Camera camera;

	// Logic
	private Map<Integer, NodeProperties> properties = null;

	@Override
	public void init() {
		super.init();
		this.processor = new CreateNodesProcessor();
		camera = stage.getCamera();

		// Initialize UI
		listPanel = new Table();
		sliderPanel = new Table();
		propertyTables = new HashMap<Integer, PropertyTable>();
		

		// Nest ListPanel inside of a ScrollPane
		ScrollPane scroll = new ScrollPane(listPanel, getSkin());
		listPanel.align(Align.top | Align.left);
		scroll.setOverscroll(false, false);
		scroll.setSmoothScrolling(true);
		scroll.setScrollingDisabled(true, false);
		scroll.setScrollbarsOnTop(true);
		
		int h = Gdx.graphics.getHeight() - 50;
		contents.add(scroll).minHeight(h).maxHeight(h).minWidth(200).left();
		contents.add(sliderPanel).fillY().colspan(2).minWidth(500).left();
		
		// Fill the list panel
		nodeList = new List(new String[] { "" }, getSkin());
		listPanel.add(nodeList);

		// Initialize listeners
		nodeList.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				int selected = ((List) ac).getSelectedIndex();
				// Select the PropertySlider Table to be displayed
				selectSliderTable(selected);
				// Send a SELECTNODE message to Desktop side
				NetMessage msg = NetMessageFactory.create(Command.SELECTNODE, (Integer) properties
						.keySet().toArray()[selectedListItem]);
				connection.send(msg);
			}
		});
	}

	@Override
	public void render() {
		camera.update();

		// Render here
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60));
		stage.draw();
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}

	private void selectSliderTable(int selectedIndex) {
		selectedListItem = selectedIndex;
		sliderPanel.clear();
		
		if (selectedIndex > -1) {
			int id = (Integer) properties.keySet().toArray()[selectedListItem];
			
			// Create a new Table for the selected item's Sliders if they don't exist already
			if (!propertyTables.containsKey(id)) {
				PropertyTable table = new PropertyTable(id, properties, connection);
				propertyTables.put(id, table);
			}
	
			sliderPanel.add(propertyTables.get(id)).minHeight(100).padLeft(50);
		}
	}

	/**
	 * Nested processing class according to ControllerUI structure
	 */
	private class CreateNodesProcessor extends ControllerProcessor {
		@Override
		public void process(NetMessage message) {
			// Access the message's extras
			Set<String> extras = message.getExtras();

			// Send ID message: The SynConnectionManager has sent an ID for this device's connection
			if (extras.contains(NetMessages.CMD_SENDID)) {
				int id = message.getInt(NetMessages.EXTRA_CONNID);
				Utils.log("Got my ID from the Desktop Synthesizer. It is " + id);
				connection.setID(id);

				// Send a "HELLO" message to the desktop
				Utils.log("Connected.");
				NetMessage m = NetMessageFactory.create(Command.HELLO);
				m.addExtra(NetMessages.CMD_HELLO, "");
				connection.send(m);
			}
			
			// Send Nodes message: Update the property Tables etc.
			if (extras.contains(NetMessages.CMD_SENDNODES)) {
				@SuppressWarnings("unchecked")
				HashMap<Integer, NodeProperties> props = (HashMap<Integer, NodeProperties>) message
						.getExtra(NetMessages.EXTRA_NODESTRUCTURE);
				properties = props;
				
				String[] items = new String[properties.size()];
				Iterator<Integer> IDiter = properties.keySet().iterator();
				
				Utils.log("Got Sendnodes message: There are " + items.length + " items right now.");

				for (int index = 0; index < items.length; index++) {
					int id = IDiter.next();
					// Update UI list
					items[index] = String.format("%s%d", properties.get(id)
							.name(), id);
				}
				nodeList.setItems(items);

				// Auto-select the first item if none is selected at the moment
				if (selectedListItem == -1 && items.length > 0) {
					selectSliderTable(0);
					NetMessage msg = NetMessageFactory.create(Command.SELECTNODE, (Integer) properties
							.keySet().toArray()[selectedListItem]);
					connection.send(msg);
				} else if (items.length == 0) {
					// If no nodes remain on the synthesizer surface, delete the slider table
					selectSliderTable(-1);
				} else {
					nodeList.setSelectedIndex(selectedListItem);
				}
				
				// Update property Tables (remove any that are not there anymore)
				propertyTables.keySet().retainAll(properties.keySet());
				
				for (int i = 0; i < propertyTables.size(); i++) {
					int id = (Integer) properties.keySet().toArray()[selectedListItem];
					NodeProperties n = properties.get(id);
					PropertyTable t = propertyTables.get(id);
					t.updateSliderValues(n);
				}
			}
			
			// Change Param message: Update the corresponding property and its table
			if (extras.contains(NetMessages.CMD_CHANGEPARAM)) {
				Utils.log("Got a changeparam message");
				NodeProperty changed = (NodeProperty) message.getExtra(NetMessages.EXTRA_PROPERTY);
				int nodeIndex = message.getInt(NetMessages.EXTRA_NODEID);
				NodeProperties corresponding = properties.get(nodeIndex);
				corresponding.put(changed.id(), changed);
				
				PropertyTable t = propertyTables.get(nodeIndex);
				t.updateSliderValues(corresponding);
			}

			// Select Node message: Update property Table to reflect currently selected Node
			if (extras.contains(NetMessages.CMD_SELECTNODE)) {

				int newSelectionID = message.getInt(NetMessages.EXTRA_NODEID);
				int oldSelectionIndex = selectedListItem;

				Object[] keys = properties.keySet().toArray();
				for (int i = 0; i < keys.length; i++) {
					if (keys[i].equals(newSelectionID)) {
						selectedListItem = i;
						break;
					}
				}

				// If the new index is different from the one selected before,
				// update Table
				if (oldSelectionIndex != selectedListItem) {
					nodeList.setSelectedIndex(selectedListItem);
					selectSliderTable(selectedListItem);
				}
			}
		}
	}
}