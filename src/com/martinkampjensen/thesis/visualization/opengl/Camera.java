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

import javax.media.opengl.GL2;
import javax.media.opengl.glu.gl2.GLUgl2;

/**
 * TODO: Document {@link Camera}.
 */
public final class Camera
{
	private static final float FOV = 90f;
	private static final float ZNEAR = 0.25f;
	private static final float ZFAR = 20f;
	private static final float DELTA_ZOOM = 0.1f;
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	private int _width = 640;
	private int _height = 480;
	private float _aspectRatio = _width / _height;
	private float _zoomFactor = 1f;
	private float[] _eye = new float[] { 0f, 0f, 5f };
	private float[] _center = new float[] { 0f, 0f, 0f };
	private float[] _up = new float[] { 0f, 1f, 0f };
	private float _rotateX = 0f;
	private float _rotateY = 0f;
	private boolean _hasChanged = true;

	public Camera()
	{
	}

	public boolean hasChanged()
	{
		return _hasChanged;
	}

	public void update(GL2 gl, GLUgl2 glu)
	{
		if(_hasChanged) {
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			glu.gluPerspective(_zoomFactor * FOV, _aspectRatio, ZNEAR, ZFAR);

			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			glu.gluLookAt(
					_eye[X], _eye[Y], _eye[Z],
					_center[X], _center[Y], _center[Z],
					_up[X], _up[Y], _up[Z]
			);

			gl.glRotatef(_rotateX, 1f, 0f, 0f);
			gl.glRotatef(_rotateY, 0f, 1f, 0f);

			_hasChanged = false;
		}
	}

	public int getWidth()
	{
		return _width;
	}

	public int getHeight()
	{
		return _height;
	}

	public void setResolution(int width, int height)
	{
		_width = width;
		_height = height;

		if(height <= 0) {
			_aspectRatio = 1;
		}
		else {
			_aspectRatio = width / (float)height;
		}

		_hasChanged = true;
	}

	public void rotateX(float deltaAngle)
	{
		_rotateX += deltaAngle;
		_hasChanged = true;
	}

	public void rotateY(float deltaAngle)
	{
		_rotateY += deltaAngle;
		_hasChanged = true;
	}

	public void zoomIn()
	{
		_zoomFactor -= DELTA_ZOOM;

		if(_zoomFactor < 0) {
			_zoomFactor = 0;
		}

		_hasChanged = true;
	}

	public void zoomOut()
	{
		_zoomFactor += DELTA_ZOOM;

		if(_zoomFactor * FOV > 180) {
			_zoomFactor = 180 / FOV;
		}

		_hasChanged = true;
	}
}
