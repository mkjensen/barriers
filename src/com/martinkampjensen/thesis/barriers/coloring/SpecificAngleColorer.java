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

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;
import com.martinkampjensen.thesis.util.Util;

/**
 * An implementation of the {@link Colorer} interface that colors depending on
 * the value of a specific angle.
 * <p>
 * A node will be colored 100% black if the specific angle value is 0 degrees,
 * and 100% blue if the value is 180 degrees. The color will approach 100% blue
 * when going from 0 degrees to 180 degrees in either direction (0 to 180, 0 to
 * -180).
 */
public final class SpecificAngleColorer extends AbstractColorer
{
	private final int _angleId;

	/**
	 * Creates a {@link Colorer} that colors {@link Node}s depending on the
	 * value of a specific angle.
	 * 
	 * @param angleId the angle to base the coloring on.
	 */
	public SpecificAngleColorer(int angleId)
	{
		super();

		_angleId = angleId;
	}

	/**
	 * TODO: Possible exception because of illegal value of _angleId
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public void color(Node node)
	{
		if(node == null) {
			throw new NullPointerException("node == null");
		}

		final Model model = node.getModel();
		double value = Util.ensureAngleInterval(model.getAngle(_angleId));

		if(value > Constant.PI) {
			value = Constant.TWO_PI - value;
		}

		final float red = 0f;
		final float green = 0f;
		final float blue = (float)(value / Constant.PI);

		node.setColor(new Color(red, green, blue));
	}
}
