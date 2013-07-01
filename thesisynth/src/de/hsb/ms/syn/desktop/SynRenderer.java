package de.hsb.ms.syn.desktop;

import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.hsb.ms.syn.common.ui.ConnectionStatusIcon;
import de.hsb.ms.syn.common.util.Constants;
import de.hsb.ms.syn.common.util.Utils;
import de.hsb.ms.syn.common.vo.NodesStage;
import de.hsb.ms.syn.common.vo.fx.LFO;
import de.hsb.ms.syn.common.vo.fx.TapDelay;
import de.hsb.ms.syn.common.vo.gen.Sawtooth;
import de.hsb.ms.syn.common.vo.gen.Sinewave;
import de.hsb.ms.syn.common.vo.gen.Square;
import de.hsb.ms.syn.common.vo.nodes.FXNode;
import de.hsb.ms.syn.common.vo.nodes.GenNode;
import de.hsb.ms.syn.desktop.abs.Node;

/**
 * Rendering unit of the Synthesizer.
 * This Singleton is responsible for the correct display of
 * relevant components, such as the Node graph structure.
 * @author Marcel
 *
 */
public class SynRenderer {
	
	// Singleton instance
	private static SynRenderer instance;
	
	private static Skin skin;
	
	// Graphical elements of the renderer
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	// Background textures
	private Texture background;
	private Texture shine;
	private float bgScrollX;
	private int wrapThreshold;
	
	// Stages: One for UI, one for Node graph
	private NodesStage stage;
	private Stage ui;
	private ConnectionStatusIcon connectionStatus;

	/**
	 * Constructor
	 */
	private SynRenderer() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		// Init graphical elements
		camera = new OrthographicCamera(w, h);
		camera.update();
		
		// Init background textures
		background = new Texture(Gdx.files.internal(String.format(Constants.PATH_UI, "bg")));
		shine = new Texture(Gdx.files.internal(String.format(Constants.PATH_UI, "shine")));
		bgScrollX = 0;
		wrapThreshold = background.getWidth() - Gdx.graphics.getWidth();
		
		// Init stages
		stage = new NodesStage(w, h, true);
		stage.setCamera(camera);
		ui = new Stage(w, h, true);
		
		connectionStatus = new ConnectionStatusIcon(Synthesizer.connection);
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
		
		Table wrapper = new Table();
		wrapper.setFillParent(true);
		wrapper.align(Align.top | Align.left);
		wrapper.pad(50);
		wrapper.row().fill();
		
		skin = new Skin(Gdx.files.internal("data/ui.json"));
		ui.addActor(wrapper);
		
		final TextButton addButtonsq = new TextButton("Square", skin);
		final TextButton addButtonsw = new TextButton("Sinewave", skin);
		final TextButton addButtonst = new TextButton("Sawtooth", skin);
		final TextButton addButtonfx = new TextButton("LFO (Sinewave)", skin);
		final TextButton addButtonfx2 = new TextButton("LFO (Sawtooth)", skin);
		final TextButton addButtondl = new TextButton("Tap Delay", skin);
		final TextButton removeButton = new TextButton("Remove last Node", skin);
		final TextButton mapButton = new TextButton("Print Node map", skin);
		final ImageButton connectButton = new ImageButton(skin);
		connectButton.add(new Image(Synthesizer.connection.getIconTexture()));
		
		addButtonsq.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				GenNode n = new GenNode(0, Utils.randomPosition());
				n.setDelegate(new Square(Utils.randomFrequency()));
				SynProcessor.getInstance().addNode(n);
			}
		});
		
		wrapper.add(addButtonsq);
		wrapper.row().fill();

		addButtonsw.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				GenNode n = new GenNode(0, Utils.randomPosition());
				n.setDelegate(new Sinewave(Utils.randomFrequency()));
				SynProcessor.getInstance().addNode(n);
			}
		});
		
		wrapper.add(addButtonsw);
		wrapper.row().fill();

		addButtonst.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				GenNode n = new GenNode(0, Utils.randomPosition());
				n.setDelegate(new Sawtooth(Utils.randomFrequency()));
				SynProcessor.getInstance().addNode(n);
			}
		});
		
		wrapper.add(addButtonst);
		wrapper.row().fill();

		addButtonfx.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				FXNode n = new FXNode(2, Utils.randomPosition());
				n.setDelegate(new LFO(0.66f, Sinewave.class));
				SynProcessor.getInstance().addNode(n);
			}
		});
		
		wrapper.add(addButtonfx);
		wrapper.row().fill();

		addButtonfx2.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				FXNode n = new FXNode(2, Utils.randomPosition());
				n.setDelegate(new LFO(2f, Sawtooth.class));
				SynProcessor.getInstance().addNode(n);
			}
		});
		
		wrapper.add(addButtonfx2);
		wrapper.row().fill();

		addButtondl.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				FXNode n = new FXNode(2, Utils.randomPosition());
				n.setDelegate(new TapDelay(0.5f, 0.6f, 0.4f));
				SynProcessor.getInstance().addNode(n);
			}
		});
		
		wrapper.add(addButtondl);
		wrapper.row().fill();
		
		removeButton.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				Collection<Node> nodes = SynProcessor.getInstance().getNodes().values();
				if (nodes.size() > 0) {
					SynProcessor.getInstance().removeLastNode();
				}
			}
		});
		
		wrapper.add(removeButton);
		wrapper.row().fill();
		
		mapButton.addListener(new ChangeListener() {
			public void changed(ChangeEvent ev, Actor ac) {
				SynProcessor.getInstance().printNodeMap();
			}
		});
		
		wrapper.add(mapButton);
		wrapper.row().fill();
	}
	
	/**
	 * Singleton access method
	 * @return
	 */
	public static SynRenderer getInstance() {
		if (instance == null)
			instance = new SynRenderer();
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
}