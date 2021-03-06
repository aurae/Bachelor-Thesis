package de.hsb.ms.syn.common.vo;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

import de.hsb.ms.syn.common.audio.AudioAlgorithm;
import de.hsb.ms.syn.common.util.Utils;
import de.hsb.ms.syn.desktop.SynthesizerAudioProcessor;

/**
 * Base class for Nodes that can be dragged around with the mouse.
 * It is extended by GenNode and FXNode
 * @author Marcel
 *
 */
public abstract class DraggableNode extends Node {

	/** The Node's "executor", basically the algorithm that fills this Node's buffer */
	protected AudioAlgorithm algorithm;

	/**
	 * Constructor
	 * @param inputs
	 * @param pos
	 */
	protected DraggableNode(int inputs, Vector2 pos) {
		super(inputs, pos);
		setTouchable(Touchable.enabled);

		// Add event listener for touch handling
		final Node n = this;
		DragListener drag = new DragListener() {
			@Override
			public void dragStart(InputEvent event, float x, float y,
					int pointer) {
				super.dragStart(event, x, y, pointer);
				n.dragged = true;
			}

			@Override
			public void drag(InputEvent event, float dx, float dy, int pointer) {
				super.drag(event, dx, dy, pointer);
				n.setNodePosition(n.getX() + dx, n.getY() + dy);
				SynthesizerAudioProcessor.getInstance().arrangeAll();
			}

			@Override
			public void dragStop(InputEvent event, float x, float y, int pointer) {
				super.dragStop(event, x, y, pointer);
				n.dragged = false;
			}
		};
		
		ClickListener rightClick = new ClickListener(Input.Buttons.RIGHT) {
			public void clicked(InputEvent event, float x, float y) {
				// Delete the node
				SynthesizerAudioProcessor.getInstance().removeNode(DraggableNode.this);
			};
		};

		drag.setTapSquareSize(5);
		addListener(drag);
		addListener(rightClick);
	}

	/**
	 * Sets the Node's delegate to the given reference.
	 * Note that FXNode objects can only delegate to Delegates in the
	 * vo.fx package, and GenNode objects can only delegate to vo.gen Delegates.
	 * This method checks the class of the passed-in delegate and logs a warning
	 * if this check fails
	 * @param algorithm
	 */
	public void setAlgorithm(AudioAlgorithm algorithm) {
		if (this.getClass().equals(algorithm.getServedClass())) {
			this.algorithm = algorithm;
//			this.delegate.setNodeIndex(this.id);
			this.init(this.algorithm.getSpriteName());
		} else
			Utils.log(String.format(
					"%s %s can't be assigned a Delegate of type %s",
					this.getClass().getSimpleName(), this.getName(),
					algorithm.getClass().getSimpleName()));
	}
	
	/**
	 * Get the DraggableNode's delegate object (to access its NodeProperties, for instance)
	 * @return
	 */
	public AudioAlgorithm getAlgorithm() {
		return algorithm;
	}
}
