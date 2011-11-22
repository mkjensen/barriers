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

package com.martinkampjensen.thesis.model.impl;

import java.awt.Color;

import com.martinkampjensen.thesis.model.Point;

/**
 * This class provides an implementation of the {@link Point}
 * interface, to minimize the effort required to implement this interface.
 */
public abstract class AbstractPoint implements Point
{
	private int _x;
	private int _y;
	private Color _color;

	/**
	 * Constructs a new point with its coordinates set to <code>(0,0)</code>
	 * and its color set to black, that is, {@link Color#BLACK}.
	 */
	protected AbstractPoint()
	{
		this(0, 0, Color.BLACK);
	}

	/**
	 * Constructs a new point.
	 * 
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 * @param color the color of the point.
	 * @throws NullPointerException if <code>color</code> is <code>null</code>.
	 */
	protected AbstractPoint(int x, int y, Color color)
	{
		if(color == null) {
			throw new NullPointerException("color == null");
		}

		_x = x;
		_y = y;
		_color = color;
	}

	@Override
	public final int getX()
	{
		return _x;
	}

	@Override
	public final void setX(int x)
	{
		_x = x;
	}

	@Override
	public final int getY()
	{
		return _y;
	}

	@Override
	public final void setY(int y)
	{
		_y = y;
	}

	@Override
	public final Color getColor()
	{
		return _color;
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public final void setColor(Color color)
	{
		if(color == null) {
			throw new NullPointerException("color == null");
		}

		_color = color;
	}
}
