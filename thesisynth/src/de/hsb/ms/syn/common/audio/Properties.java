package de.hsb.ms.syn.common.audio;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hsb.ms.syn.common.util.Utils;

/**
 * Each Delegate that is assigned to a DraggableNode holds
 * an instance of this class, which encapsulates necessary parameters
 * for the Delegate's algorithm. This includes general parameters
 * needed for all types of sound manipulation algorithms (volume, pan,
 * frequency), but may also involve specific parameters that may be added
 * indepently
 * @author Marcel
 *
 */
public class Properties implements Serializable, Iterable<Property> {
	
	private static final long serialVersionUID = 3099702165119324979L;
	
	// Type String constants
	public static final String TYPE_GEN	= "GEN";
	public static final String TYPE_FX	= "FX";
	
	// Parameter constants for general purpose params
	public static final int PROP_VOLUME		= 0x12;
	public static final int PROP_FREQUENCY	= 0x13;
	public static final int PROP_PAN		= 0x14;
	public static final int PROP_TONE		= 0x15;
	
	// Internal map of the parameters connected to specific NodeProperty objects
	private Map<Integer, Property> properties;
	
	// Name of the DraggableNode that holds these NodeProperties
	private String name;
	
	// Index of the Node that holds these NodeProperties
	private int nodeIndex;
	
	/**
	 * Constructor
	 * @param name		Name of the Node these properties belong to
	 * @param nodeIndex Index of the Node these properties belong to
	 * @param volume	Initial volume (default should be 0.1f)
	 * @param frequency	Frequency
	 * @param pan		Initial pan (from -1.0 (all the way left) to +1.0 (all the way right))
	 */
	public Properties(String name, int nodeIndex, float volume, float frequency, float pan) {
		
		this.name = name;
		this.nodeIndex = nodeIndex;
		
		properties = new HashMap<Integer, Property>();
		properties.put(PROP_VOLUME, new Property(PROP_VOLUME, "Volume", 0.0f, 1.0f, 0.01f, volume));
		properties.put(PROP_FREQUENCY, new Property(PROP_FREQUENCY, "Frequency", 20, 2000, 100, frequency));
		properties.put(PROP_PAN, new Property(PROP_PAN, "Pan", -1.0f, 1.0f, 0.1f, pan));
	}
	
	public Properties(Properties toCopyFrom) {
		this.name = toCopyFrom.name();
		this.nodeIndex = toCopyFrom.nodeIndex();
		properties = new HashMap<Integer, Property>();
		for (Property prop : toCopyFrom.properties.values()) {
			if (!prop.isHidden())
				properties.put(prop.id(), new Property(prop, prop.val()));
		}
	}
	
	/**
	 * Get the type of Node that these NodeProperties belong to
	 * @return
	 */
	public String name() {
		return name;
	}
	
	/**
	 * Get the Node index
	 * @return
	 */
	public int nodeIndex() {
		return nodeIndex;
	}
	
	/**
	 * Get a specific NodeProperty using a key either from this class or the specific algorithm implementation
	 * @param key
	 * @return
	 */
	public Property get(int key) {
		if (this.has(key))
			return properties.get(key);
		
		Utils.log("NodeProperties: Can't find Key " + key);
		return null;
	}
	
	/**
	 * Checks if a specific key exists in these NodeProperties
	 * @param key
	 * @return
	 */
	public boolean has(int key) {
		return (properties.containsKey(key));
	}
	
	/**
	 * Put a NodeProperty into the map using the given key
	 * @param key
	 * @param prop
	 */
	public void put(int key, Property prop) {
		properties.put(key, prop);
	}
	
	/**
	 * Remove a NodeProperty by key from the list of NodeProperties
	 * @param key
	 */
	public void remove(int key) {
		if (this.has(key))
			properties.remove(key);
	}
	
	/**
	 * toString override
	 */
	public String toString() {
		String s = "";
		for (Property p : properties.values())
			s += String.format("{%s},", p);
		return s;
	}

	@Override
	public Iterator<Property> iterator() {
		return properties.values().iterator();
	}

	public void setNodeIndex(int id) {
		this.nodeIndex = id;
	}

	public Properties copy() {
		return new Properties(this);
	}
}
