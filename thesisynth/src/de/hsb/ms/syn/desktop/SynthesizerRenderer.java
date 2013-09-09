package de.hsb.ms.syn.desktop;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.hsb.ms.syn.common.audio.Scale;
import de.hsb.ms.syn.common.audio.fx.LFO;
import de.hsb.ms.syn.common.audio.fx.TapDelay;
import de.hsb.ms.syn.common.audio.gen.Sawtooth;
import de.hsb.ms.syn.common.audio.gen.Sinewave;
import de.hsb.ms.syn.common.audio.gen.Square;
import de.hsb.ms.syn.common.audio.gen.Triangle;
import de.hsb.ms.syn.common.interfaces.Connection;
import de.hsb.ms.syn.common.ui.ConnectionStatusIcon;
import de.hsb.ms.syn.common.util.Constants;
import de.hsb.ms.syn.common.util.Utils;
import de.hsb.ms.syn.common.vo.FxNode;
import de.hsb.ms.syn.common.vo.GenNode;
import de.hsb.ms.syn.common.vo.NodesStage;

/**
 * Rendering unit of the Synthesizer.
 * This Singleton is responsible for the correct display of
 * relevant components, such as the Node graph structure.
 * @author Marcel
 *
 */
public class SynthesizerRenderer {
	
	// Singleton instance
	private static SynthesizerRenderer instance;
	
	private static Skin skin;
	
	// Graphical elements of the renderer
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	// Map containing entries relating mobile devices to Color objects to use when displaying the highlighted nodes
	private Map<Integer, Color> mapConnectionColors;
	
	// Background textures
	private Texture background;
	private Texture shine;
	private float bgScrollX;
	private int wrapThreshold;
	
	// Stages: One for UI, one for Node graph
	private NodesStage stage;
	private Stage ui;
	private ConnectionStatusIcon connectionStatus;
	
	private float width = 800;
	private float height = 600;

	/**
	 * Constructor
	 */
	private SynthesizerRenderer(Connection connection) {

		// Init graphical elements
		camera = new OrthographicCamera(width, height);
		camera.update();
		
		mapConnectionColors = new HashMap<Integer, Color>();
		
		// Init background textures
		background = new Texture(Gdx.files.internal(String.format(Constants.PATH_UI, "bg")));
		shine = new Texture(Gdx.files.internal(String.format(Constants.PATH_UI, "shine")));
		bgScrollX = 0;
		wrapThreshold = background.getWidth() - Gdx.graphics.getWidth();
		
		// Init stages
		stage = new NodesStage(width, height, true);
		stage.setCamera(camera);
		ui = new Stage(width, height, true);
		
		connectionStatus = new ConnectionStatusIcon(connection);
		int x = (Gdx.graphics.getWidth() / 2) - connectionStatus.getWidth();
		int y = (Gdx.graphics.getHeight() / 2) - connectionStatus.getHeight();
		connectionStatus.setPosition(x, y);
		
		batch = stage.getSpriteBatch();
		
		// Init user interface components
		this.initUI();
	}
	
	/**
	 * Initialization of UI components
	 */
	private void initUI() {

		skin = new Skin(Gdx.files.internal("data/ui.json"));
		//skin = new Skin(Gdx.files.internal("data/pack.json"));
		
		// Initialize table wrapper
		Table buttonTable = new Table();
		buttonTable.setFillParent(true);
		buttonTable.align(Align.bottom | Align.left);
		buttonTable.pad(0);
		buttonTable.row().fill();
		ui.addActor(buttonTable);
		
		// Initialize control messages for top left corner
		Table messageTable = new Table();
		messageTable.setFillParent(true);
		messageTable.align(Align.top | Align.left);
		messageTable.pad(20);
		messageTable.row().fill();
		ui.addActor(messageTable);
		
		// Initialize buttons
		final ImageButton addButtonSq = new ImageButton(skin);
		addButtonSq.add(new Image(new Texture(String.format(Constants.PATH_NODE, "node_square"))));
		addButtonSq.row();
		addButtonSq.add(new Label("Square", skin));
		
		final ImageButton addButtonSw = new ImageButton(skin);
		addButtonSw.add(new Image(new Texture(String.format(Constants.PATH_NODE, "node_sinewave"))));
		addButtonSw.row();
		addButtonSw.add(new Label("Sine", skin));
		
		final ImageButton addButtonSt = new ImageButton(skin);
		addButtonSt.add(new Image(new Texture(String.format(Constants.PATH_NODE, "node_sawtooth"))));
		addButtonSt.row();
		addButtonSt.add(new Label("Saw", skin));
		
		final ImageButton addButtonTr = new ImageButton(skin);
		addButtonTr.add(new Image(new Texture(String.format(Constants.PATH_NODE, "node_triangle"))));
		addButtonTr.row();
		addButtonTr.add(new Label("Tri", skin));
		
		final ImageButton addButtonLfoSw = new ImageButton(skin);
		addButtonLfoSw.add(new Image(new Texture(String.format(Constants.PATH_NODE, "node_lfo"))));
		addButtonLfoSw.row();
		addButtonLfoSw.add(new Label("LFO Sine", skin));
		
		final ImageButton addButtonLfoSt = new ImageButton(skin);
		addButtonLfoSt.add(new Image(new Texture(String.format(Constants.PATH_NODE, "node_lfo"))));
		addButtonLfoSt.row();
		addButtonLfoSt.add(new Label("LFO Saw", skin));
		
		final ImageButton addButtonDl = new ImageButton(skin);
		addButtonDl.add(new Image(new Texture(String.format(Constants.PATH_NODE, "node_delay"))));
		addButtonDl.row();
		addButtonDl.add(new Label("Tap Delay", skin));
		
//		final TextButton removeButton = new TextButton("Undo", skin);
		
		// initialize messages
		final Label captionLabel		= new Label("Controls:", skin);
		// final Label doubleClickLabel	= new Label("[Double left-click] Select Node on all mobile devices", skin);
		final Label rightClickLabel		= new Label("[Right-click] Remove Node", skin);
		
		// Setup UI
		float segWidth = width / 7;
		buttonTable.add(addButtonSq).minWidth(segWidth).maxWidth(segWidth);
		buttonTable.add(addButtonSw).minWidth(segWidth).maxWidth(segWidth);
		buttonTable.add(addButtonSt).minWidth(segWidth).maxWidth(segWidth);
		buttonTable.add(addButtonTr).minWidth(segWidth).maxWidth(segWidth);
		buttonTable.add(addButtonLfoSw).minWidth(segWidth).maxWidth(segWidth);
		buttonTable.add(addButtonLfoSt).minWidth(segWidth).maxWidth(segWidth);
		buttonTable.add(addButtonDl).minWidth(segWidth).maxWidth(segWidth);
//		buttonTable.add(removeButton).minWidth(segWidth).maxWidth(segWidth);
		
		messageTable.add(captionLabel).row().fill();
		// messageTable.add(doubleClickLabel).row().fill();
		messageTable.add(rightClickLabel);
		
		// Initialize listeners
		addButtonSq.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				GenNode n = new GenNode(0, Utils.randomPosition());
				n.setAlgorithm(new Square(new Scale(Scale.BASE_C, Scale.MODE_MAJ_PENTA)));
				SynthesizerAudioProcessor.getInstance().addNode(n);
			}
		});

		addButtonSw.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				GenNode n = new GenNode(0, Utils.randomPosition());
				n.setAlgorithm(new Sinewave(new Scale(Scale.BASE_C, Scale.MODE_MAJ_OCTAVE)));
				SynthesizerAudioProcessor.getInstance().addNode(n);
			}
		});

		addButtonSt.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				GenNode n = new GenNode(0, Utils.randomPosition());
				n.setAlgorithm(new Sawtooth(new Scale(Scale.BASE_A, Scale.MODE_MIN_OCTAVE)));
				SynthesizerAudioProcessor.getInstance().addNode(n);
			}
		});
		
		addButtonTr.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				GenNode n = new GenNode(0, Utils.randomPosition());
				n.setAlgorithm(new Triangle(new Scale(Scale.BASE_A, Scale.MODE_MIN_OCTAVE)));
				SynthesizerAudioProcessor.getInstance().addNode(n);
			}
		});
		addButtonLfoSw.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				FxNode n = new FxNode(Constants.LFO_INPUTS, Utils.randomPosition());
				n.setAlgorithm(new LFO(0.66f, Sinewave.class));
				SynthesizerAudioProcessor.getInstance().addNode(n);
			}
		});

		addButtonLfoSt.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				FxNode n = new FxNode(Constants.LFO_INPUTS, Utils.randomPosition());
				n.setAlgorithm(new LFO(2f, Sawtooth.class));
				SynthesizerAudioProcessor.getInstance().addNode(n);
			}
		});

		addButtonDl.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				FxNode n = new FxNode(Constants.TAPDELAY_INPUTS, Utils.randomPosition());
				n.setAlgorithm(new TapDelay(0.5f, 0.6f, 0.4f));
				SynthesizerAudioProcessor.getInstance().addNode(n);
			}
		});
		
//		removeButton.addListener(new ChangeListener() {
//			public void changed(ChangeEvent ev, Actor ac) {
//				Collection<Node> nodes = SynAudioProcessor.getInstance().getNodes().values();
//				if (nodes.size() > 0) {
//					SynAudioProcessor.getInstance().removeLastNode();
//				}
//			}
//		});
	}
	
	/**
	 * Singleton access method
	 * @return
	 */
	public static SynthesizerRenderer getInstance() {
		if (instance == null)
			instance = new SynthesizerRenderer(null);
		return instance;
	}
	
	public static SynthesizerRenderer getInstance(Connection newConnection) {
		if (instance == null)
			instance = new SynthesizerRenderer(newConnection);
		else
			instance.connectionStatus.setConnection(newConnection);
		return instance;
	}
	
	/**
	 * Render method.
	 * The heart of the rendering unit. Update camera and matrices,
	 * order the stage to draw itself and draw the UI on top.
	 */
	public void render() {
		// OGL clear commands
		Gdx.gl.glClearColor(0.8f, 0.8f, 0.947f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// Update camera and projection matrices
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		// Draw moving background (with wrap-around)
		batch.begin();
		batch.draw(background, (-Gdx.graphics.getWidth()/2) - bgScrollX, -Gdx.graphics.getHeight()/2);
		// If the scrolling summand exceeds the texture's right bound, draw another instance to fill the gap
		bgScrollX = (bgScrollX > background.getWidth()) ? 0 : bgScrollX + 1;
		if (bgScrollX > wrapThreshold) {
			float diff = bgScrollX - wrapThreshold;
			batch.draw(background, (Gdx.graphics.getWidth()/2) - diff, -Gdx.graphics.getHeight()/2);
		}
		batch.draw(shine, -shine.getWidth()/2, -shine.getHeight()/2);
		batch.end();
		
		// Draw the stage and handle input events
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60));
		// Draw connections between Nodes
		stage.drawConnections();
		// Draw Nodes themselves
		stage.draw();
		
		// Draw the UI on top
		ui.draw();
		
		// Draw the connection status icon
		connectionStatus.draw(batch);
	}
	
	/**
	 * Get the Node graph Stage
	 * @return
	 */
	public Stage getNodesStage() {
		return stage;
	}
	
	/**
	 * Get the UI Stage
	 * @return
	 */
	public Stage getUIStage() {
		return ui;
	}

	/**
	 * Create a new Color for the given connection ID to use for Highlight messages
	 * @param newID
	 * @return
	 */
	public float[] makeColorForConnection(int newID) {
		float r = (float) Math.random();
		float g = (float) Math.random();
		float b = (float) Math.random();
		Color color = new Color(r, g, b, 1.0f);
		Utils.log("Made new color: " + color);
		mapConnectionColors.put(newID, color);
		return new float[] {r, g, b};
	}

	public Color getColorForConnection(int highlightingConnectionId) {
		return mapConnectionColors.get(highlightingConnectionId);
	}
}