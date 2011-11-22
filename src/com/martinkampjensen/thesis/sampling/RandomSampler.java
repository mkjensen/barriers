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

package com.martinkampjensen.thesis.sampling;

import java.util.ArrayList;
import java.util.List;

import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Random;

/**
 * A {@link RandomSampler} is an implementation of the {@link Sampler}
 * interface.
 * <p>
 * This implementation samples randomly, that is, each sample consists of random
 * angle values and the resulting fitness value ({@link Model#evaluate()}).
 */
public final class RandomSampler extends AbstractSampler
{
	public RandomSampler()
	{
		super();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation samples randomly.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public double[][] sample(Model model, int[] angleIds, int nSamples,
			double[] fromAngles, double[] toAngles)
	{
		check(model, angleIds, nSamples, fromAngles, toAngles);

		// Avoid changing the angles of the input model. Note that copying the
		// model may turn out to be too expensive and more expensive than
		// storing all angles and restoring them after sampling.
		model = model.copy();

		final int nAngles = angleIds.length;
		final double[] angles = new double[nAngles];
		final List<Sample> samples = new ArrayList<Sample>(nSamples);

		for(int i = 0; i < nSamples; i++) {
			for(int j = 0; j < nAngles; j++) {
				angles[j] = Random.nextDouble(fromAngles[j], toAngles[j]);
			}

			addSample(samples, model, angleIds, angles, nAngles);
		}

		return formatSamples(samples, angleIds);
	}

	@Override
	protected int getNumberOfSamples(int nSamples, int nAngles)
	{
		return nSamples;
	}
}
