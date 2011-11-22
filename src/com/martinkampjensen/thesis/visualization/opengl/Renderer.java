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

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.gl2.GLUgl2;

import jbcl.data.dict.AtomType;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.martinkampjensen.thesis.model.AtomModel;
import com.martinkampjensen.thesis.util.Debug;

/**
 * TODO: Document {@link Renderer}.
 */
public final class Renderer implements GLEventListener
{
	private final Camera _camera;
	private final InputListener _inputListener;
	private final AtomModel _model;
	private final TextRenderer _textRenderer;
	private final Renderable[] _renderables;
	private final int _textMargin = 10;
	private String _energyValue;
	private String _atomId;

	public Renderer(Camera camera, InputListener inputListener, AtomModel model)
	{
		if(model == null) {
			throw new NullPointerException("model");
		}

		_camera = camera;
		_inputListener = inputListener;
		_model = model;
		_textRenderer = new TextRenderer(
				new Font(Font.SANS_SERIF, Font.PLAIN, 20), true, false);
		_renderables = initializeRenderables();

		updateModel();
		updateInputListener();
	}

	/**
	 * Called by the drawable immediately after the OpenGL context is
	 * initialized.
	 */
	@Override
	public void init(GLAutoDrawable drawable)
	{
		if(Debug.isDebug()) {
			// Composable pipeline which wraps an underlying GL implementation,
			// providing error checking after each OpenGL method call. If an
			// error occurs, causes a GLException to be thrown at exactly the
			// point of failure.
			drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
		}

		final GL2 gl = drawable.getGL().getGL2();

		Debug.line("Renderer using OpenGL %s on %s",
				gl.glGetString(GL.GL_VERSION), gl.glGetString(GL.GL_RENDERER));

		// Enable vertical sync.
		gl.setSwapInterval(1);

		// Background color.
		gl.glClearColor(0f, 0f, 0f, 0f);

		// Lines.
		gl.glLineWidth(2);
		gl.glEnable(GL.GL_LINE_SMOOTH);

		// Draw objects in the correct order (back-to-front).
		gl.glClearDepth(1.0);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_DEPTH_TEST);

		// Lighting.
		final float[] lightAmbient = { 0.75f, 0.75f, 0.75f, 1.0f };
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);

		// TextRenderer.
		_textRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
	}

	/**
	 * Called by the drawable during the first repaint after the component has
	 * been resized.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height)
	{
		_camera.setResolution(width, height);
	}

	/**
	 * Called by the drawable to initiate OpenGL rendering by the client.
	 */
	@Override
	public void display(GLAutoDrawable drawable)
	{
		if(update()) {
			render(drawable);
		}
	}

	/**
	 * Called by the drawable before the OpenGL context is destroyed by an
	 * external event.
	 */
	@Override
	public void dispose(GLAutoDrawable drawable)
	{
	}

	private Renderable[] initializeRenderables()
	{
		final int size = _model.size();
		final int refSize = _model.additionalSize();
		final Renderable[] renderables = new Renderable[size + refSize];

		final AtomType fstAtomType = _model.getAdditionalType(-refSize);
		final double[] fstAtom = _model.getAdditional(-refSize);
		renderables[0] = new AtomRenderable(fstAtomType, fstAtom, fstAtom);

		// This might seem a bit weird, but it is because how the ids of
		// additional atoms are defined in the CartesianModel interface.
		for(int i = -refSize + 1; i < 0; i++) {
			final AtomType atomType = _model.getAdditionalType(i);
			final double[] a = _model.getAdditional(i);
			final double[] b = _model.getAdditionalBond(i);
			renderables[i + refSize] = new AtomRenderable(atomType, a, b);
		}

		for(int i = 0; i < size; i++) {
			final AtomType atomType = _model.getType(i);
			final double[] a = _model.get(i);
			final double[] b = _model.getBond(i);
			renderables[i + refSize] = new AtomRenderable(atomType, a, b);
		}

		return renderables;
	}

	private void updateModel()
	{
		_energyValue = Double.toString(_model.evaluate());
	}

	private void updateInputListener()
	{
		_atomId = Integer.toString( _inputListener.getAngleId());
	}

	private boolean update()
	{
		boolean update = false;

		if(_model.hasChanged()) {
			updateModel();
			update = true;
		}

		if(_inputListener.hasChanged()) {
			updateInputListener();
			update = true;
		}

		return update || _camera.hasChanged();
	}

	private void render(GLAutoDrawable drawable)
	{
		final GL2 gl = drawable.getGL().getGL2();
		final GLUgl2 glu = new GLUgl2();

		_camera.update(gl, glu);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		final int nRenderables = _renderables.length;
		for(int i = 0; i < nRenderables; i++) {
			_renderables[i].render(gl, glu);
		}	

		_textRenderer.beginRendering(_camera.getWidth(), _camera.getHeight());
		_textRenderer.draw(_energyValue, _textMargin, _textMargin);
		_textRenderer.endRendering();

		final Rectangle2D rectangle = _textRenderer.getBounds(_atomId);
		final int atomIdX =
			_camera.getWidth() - (int)rectangle.getWidth() - _textMargin;

		_textRenderer.beginRendering(_camera.getWidth(), _camera.getHeight());
		_textRenderer.draw(_atomId, atomIdX, _textMargin);
		_textRenderer.endRendering();

		gl.glFlush();
	}
}
