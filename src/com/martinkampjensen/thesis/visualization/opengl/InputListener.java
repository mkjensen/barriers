/**
 * Copyright 2010-2011 Martin Kamp Jensen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.martinkampjensen.thesis.visualization.opengl;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.martinkampjensen.thesis.model.Model;

/**
 * An {@link InputListener} handles user input and interacts with a
 * {@link Camera} and a {@link Model} accordingly.
 */
public final class InputListener implements KeyListener, MouseListener,
MouseMotionListener, MouseWheelListener
{
	private static final double DELTA_ANGLE = 0.05;
	private final Camera _camera;
	private final Model _model;
	private int _angleId;
	private int _fromX;
	private int _fromY;
	private boolean _hasChanged;

	/**
	 * Constructs a new input listener.
	 * 
	 * @param camera the camera.
	 * @param model the model.
	 * @throws NullPointerException if <code>camera</code> or <code>model</code>
	 * is <code>null</code>.
	 */
	public InputListener(Camera camera, Model model)
	{
		if(camera == null) {
			throw new NullPointerException("camera == null");
		}
		else if(model == null) {
			throw new NullPointerException("model == null");
		}

		_camera = camera;
		_model = model;
		_angleId = 0;
		_fromX = 0;
		_fromY = 0;
		_hasChanged = true;
	}

	// KeyListener methods.

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		final int keyCode = e.getKeyCode();

		switch(keyCode) {
		case KeyEvent.VK_Q:
			addToAngleId(-10);
			return;
		case KeyEvent.VK_W:
			addToAngleId(10);
			return;
		case KeyEvent.VK_LEFT:
			addToAngleId(-1);
			return;
		case KeyEvent.VK_RIGHT:
			addToAngleId(1);
			return;
		case KeyEvent.VK_UP:
			addToAngleValue(_model, _angleId, DELTA_ANGLE);
			return;
		case KeyEvent.VK_DOWN:
			addToAngleValue(_model, _angleId, -DELTA_ANGLE);
			return;
		}

		// VK_0 thru VK_9 are the same as ASCII '0' thru '9' (0x30 - 0x39).
		final int angleId = keyCode - KeyEvent.VK_0;
		final int minAngleId = 0;
		final int maxAngleId = Math.min(_model.size() - 1, 9);

		if(angleId >= minAngleId && angleId <= maxAngleId) {
			_angleId = angleId;
			_hasChanged = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	// MouseListener methods.

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		_fromX = e.getX();
		_fromY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	// MouseMotionListener methods.

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if(e.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK) {
			final int x = e.getX();
			final int y = e.getY();
			final float angleX = 360 * (x - _fromX) / (float)_camera.getWidth();
			final float angleY = 360 * (_fromY - y) /(float)_camera.getHeight();

			_camera.rotateX(angleY);
			_camera.rotateY(angleX);
			_fromX = x;
			_fromY = y;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	// MouseWheelListener methods.

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		final int wheelRotations = e.getWheelRotation();

		if(wheelRotations > 0) {
			_camera.zoomOut();
		}
		else if(wheelRotations < 0) {
			_camera.zoomIn();
		}
	}

	/**
	 * Returns whether or not the state of this input listener has changed since
	 * the last time {@link #getAngleId()} was called.
	 * 
	 * @return <code>true</code> if and only if the state has changed.
	 */
	public boolean hasChanged()
	{
		return _hasChanged;
	}

	/**
	 * Returns the angle id that this input listener is focused on and sets the
	 * state of this input listener to not have changed.
	 * 
	 * @return the angle id.
	 * @see #hasChanged()
	 */
	public int getAngleId()
	{
		_hasChanged = false;
		return _angleId;
	}

	private static void addToAngleValue(Model model, int angleId, double value)
	{
		double angle = model.getAngle(angleId);
		angle += value;
		model.setAngle(angleId, angle);
	}

	private void addToAngleId(int value)
	{
		int angleId = _angleId + value;

		if(angleId < 0) {
			angleId = 0;
		}
		else if(angleId >= _model.size()) {
			angleId = _model.size() - 1;
		}

		if(angleId != _angleId) {
			_angleId = angleId;
			_hasChanged = true;
		}
	}
}
