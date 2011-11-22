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

import java.awt.Color;
import java.awt.Frame;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.Animator;
import com.martinkampjensen.thesis.model.AtomModel;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Debug;

/**
 * TODO: Document {@link Visualizer}.
 */
public final class Visualizer
{
	private static final String WINDOW_TITLE = "Visualizer - ";
	private static final int WINDOW_WIDTH = 800;
	private static final int WINDOW_HEIGHT = 600;
	private final AtomModel _model;
	private final Frame _frame;
	private final GLCanvas _canvas;
	private final Animator _animator;

	public Visualizer(Model model)
	{
		if(!(model instanceof AtomModel)) {
			throw new UnsupportedOperationException(
			"model !instanceof AtomModel");
		}

		Debug.line("Initializing Visualizer");

		_model = (AtomModel)model;

		initializeGLProfile();
		_frame = initializeFrame(_model);
		_canvas = initializeGLCanvas(_model);

		_frame.add(_canvas);
		_animator = new Animator(_canvas);
	}

	public void start()
	{
		Debug.line("Starting Visualizer");

		_frame.setVisible(true);
		_canvas.requestFocusInWindow();
		_animator.start();
	}

	private static void initializeGLProfile()
	{
		// Applications shall call this methods ASAP, before any other UI
		// invocation. In case applications are able to initialize JOGL before
		// any other UI action, (...) [they will] benefit from fast native
		// multithreading support on all platforms if possible.
		final boolean firstUIActionOnProcess = true;
		GLProfile.initSingleton(firstUIActionOnProcess);
	}

	private static Frame initializeFrame(AtomModel model)
	{
		final Frame frame = new Frame(
				WINDOW_TITLE + model.getClass().getSimpleName());

		frame.setBackground(Color.BLACK);
		frame.addWindowListener(new WindowListener());
		frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		// Center window on screen.
		frame.setLocationRelativeTo(null);

		return frame;
	}

	private static GLCanvas initializeGLCanvas(AtomModel model)
	{
		final GLProfile glp = GLProfile.getDefault();
		final GLCapabilities glc = initializeGLCapabilities(glp);
		final GLCanvas canvas = new GLCanvas(glc);

		final Camera camera = new Camera();
		final InputListener inputListener =
			new InputListener(camera, model);
		final Renderer renderer = new Renderer(camera, inputListener, model);

		canvas.addGLEventListener(renderer);
		canvas.addKeyListener(inputListener);
		canvas.addMouseListener(inputListener);
		canvas.addMouseMotionListener(inputListener);
		canvas.addMouseWheelListener(inputListener);

		return canvas;
	}

	private static GLCapabilities initializeGLCapabilities(GLProfile glp)
	{
		final GLCapabilities glc = new GLCapabilities(glp);

		glc.setHardwareAccelerated(true);
		glc.setDoubleBuffered(true);

		// Enable anti-aliasing.
		glc.setNumSamples(4);
		glc.setSampleBuffers(true);

		return glc;
	}
}
