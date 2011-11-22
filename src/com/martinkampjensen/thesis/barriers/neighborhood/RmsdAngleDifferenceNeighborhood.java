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

package com.martinkampjensen.thesis.barriers.neighborhood;

import java.io.Serializable;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Util;

/**
 * An implementation of the {@link Neighborhood} interface that defines the
 * neighborhood using root mean square deviation (RMSD) on the angle differences
 * in models.
 * <p>
 * Using RMSD means that two models where one angle differs by 30 degrees is
 * considered more distant than two models where 30 angles each differ by 1
 * degree.
 * 
 * @see <a href="http://goo.gl/3dWYm">Root mean square deviation</a>
 */
public final class RmsdAngleDifferenceNeighborhood extends AbstractNeighborhood
implements Serializable
{
	private static final long serialVersionUID = -5592433695190982624L;

	public RmsdAngleDifferenceNeighborhood()
	{
		super();
	}

	@Override
	public double maximumDistance(Model model)
	{
		return Constant.PI;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses RMSD on the angle differences meaning that two
	 * models where one angle differs by 30 degrees is considered more distant
	 * than two models where 30 angles each differ by 1 degree.
	 */
	@Override
	public double distance(Model first, Model second)
	{
		final int nAngles = first.size();
		double squaredDiffSum = 0d;

		for(int i = 0; i < nAngles; i++) {
			final double diff =
				Util.angleDifference(first.getAngle(i), second.getAngle(i));
			squaredDiffSum += diff * diff;
		}

		return Math.sqrt(squaredDiffSum / nAngles);
	}
}
