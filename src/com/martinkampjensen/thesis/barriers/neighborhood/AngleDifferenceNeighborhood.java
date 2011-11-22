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
 * neighborhood using the angle differences in models. 
 */
public final class AngleDifferenceNeighborhood extends AbstractNeighborhood
implements Serializable
{
	private static final long serialVersionUID = -1873364594362599548L;

	public AngleDifferenceNeighborhood()
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
	 * This implementation calculates and returns the average difference between
	 * angles in the two models.
	 */
	@Override
	public double distance(Model first, Model second)
	{
		final int nAngles = first.size();
		double absDiffSum = 0d;

		for(int i = 0; i < nAngles; i++) {
			absDiffSum +=
				Util.angleDifference(first.getAngle(i), second.getAngle(i));
		}

		return absDiffSum / nAngles;
	}
}
