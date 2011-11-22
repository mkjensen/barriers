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

package com.martinkampjensen.thesis.model;

import java.awt.Color;

/**
 * A {@link Point} is a colored point in two-dimensional Cartesian space.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Cartesian_coordinate_system">Cartesian coordinate system</a>
 */
public interface Point
{
	/**
	 * Returns the x coordinate of this point.
	 * 
	 * @return the x coordinate.
	 */
	int getX();

	/**
	 * Sets the x coordinate of this point.
	 * 
	 * @param x the x coordinate.
	 */
	void setX(int x);

	/**
	 * Returns the y coordinate of this point.
	 * 
	 * @return the y coordinate.
	 */
	int getY();

	/**
	 * Sets the y coordinate of this point.
	 * 
	 * @param y the y coordinate.
	 */
	void setY(int y);

	/**
	 * Returns the color of this point.
	 * 
	 * @return the color of this point.
	 */
	Color getColor();

	/**
	 * Sets the color of this point.
	 * 
	 * @param color the color.
	 * @throws NullPointerException if <code>color</code> is <code>null</code>.
	 */
	void setColor(Color color);
}
