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
 * A {@link Renderable} can be rendered by the JOGL library.
 * 
 * @see <a href="http://jogamp.org/jogl/www/">JOGL</a>
 */
public interface Renderable
{
	/**
	 * Renders one or more objects using the JOGL library.
	 * 
	 * @param gl the OpenGL interface.
	 * @param glu a OpenGL Utility Library instance.
	 * @see GL2
	 * @see GLUgl2
	 */
	void render(GL2 gl, GLUgl2 glu);
}
