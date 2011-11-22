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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.gl2.GLUgl2;

import jbcl.data.dict.AtomType;

/**
 * TODO: Document {@link AtomRenderable}.
 */
public final class AtomRenderable implements Renderable
{
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	private final double[] _a;
	private final double[] _b;
	private final float[] _lighting;

	/**
	 * Constructs a new atom renderable that renders an atom and its bond to
	 * another atom.
	 * 
	 * @param atomType the type of the atom.
	 * @param a the atom to be rendered.
	 * @param b the atom to which a bond is to be rendered.
	 */
	public AtomRenderable(AtomType atomType, double[] a, double[] b)
	{
		_a = a;
		_b = b;

		switch(atomType)
		{
		case C:
			_lighting = Color.GREY;
			break;
		case H:
			_lighting = Color.WHITE;
			break;
		case N:
			_lighting = Color.BLUE;
			break;
		case O:
			_lighting = Color.RED;
			break;
		case S:
			_lighting = Color.YELLOW;
			break;
		default:
			_lighting = Color.PINK;
		}
	}

	@Override
	public void render(GL2 gl, GLUgl2 glu)
	{
		final double ax = _a[X];
		final double ay = _a[Z];
		final double az = -_a[Y];

		// Atom.
		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, _lighting, 0);
		gl.glEnable(GL2.GL_LIGHT2);
		gl.glPushMatrix();
		gl.glTranslated(ax, ay, az);
		final GLUquadric atom = glu.gluNewQuadric();
		glu.gluSphere(atom, 0.125, 25, 25);
		glu.gluDeleteQuadric(atom);
		gl.glPopMatrix();
		gl.glDisable(GL2.GL_LIGHT2);

		final double bx = _b[X];
		final double by = _b[Z];
		final double bz = -_b[Y];

		// Bond.
		gl.glColor4fv(Color.LIGHT_GREY, 0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(ax, ay, az);
		gl.glVertex3d(bx, by, bz);
		gl.glEnd();
	}
}
