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

package com.martinkampjensen.thesis.barriers.coloring;

import java.awt.Color;

import com.martinkampjensen.thesis.model.Node;

/**
 * An implementation of the {@link Colorer} interface that colors using a fixed
 * color.
 */
public final class FixedColorer extends AbstractColorer
{
	private final Color _color;

	/**
	 * Creates a {@link Colorer} that colors {@link Node}s in a
	 * specific color.
	 *  
	 * @param color the color to use.
	 * @throws NullPointerException if <code>color</color> is <code>null</code>.
	 */
	public FixedColorer(Color color)
	{
		super();

		if(color == null) {
			throw new NullPointerException("color == null");
		}

		_color = color;
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public void color(Node node)
	{
		if(node == null) {
			throw new NullPointerException("node == null");
		}

		node.setColor(_color);
	}
}
