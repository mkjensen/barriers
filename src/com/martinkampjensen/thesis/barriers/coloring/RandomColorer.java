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
import com.martinkampjensen.thesis.util.Random;

/**
 * An implementation of the {@link Colorer} interface that colors randomly, but
 * never completely white.
 */
public final class RandomColorer extends AbstractColorer
{
	private static final double MAX_BRIGHTNESS = 0.95;

	public RandomColorer()
	{
		super();
	}

	/**
	 * Colors a {@link Node} randomly, but never completely white.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public void color(Node node)
	{
		if(node == null) {
			throw new NullPointerException("node == null");
		}

		final float red = (float)Random.nextDouble(0, MAX_BRIGHTNESS);
		final float green = (float)Random.nextDouble(0, MAX_BRIGHTNESS);
		final float blue = (float)Random.nextDouble(0, MAX_BRIGHTNESS);

		node.setColor(new Color(red, green, blue));
	}
}
