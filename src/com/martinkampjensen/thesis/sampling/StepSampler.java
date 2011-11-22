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

/**
 * A {@link StepSampler} is an implementation of the {@link Sampler} interface.
 * <p>
 * This implementation samples in steps, that is, each sample represents a
 * stepwise change of the angle values (and, hence, the resulting fitness value,
 * {@link Model#evaluate()}) compared to the previous sample.
 */
public final class StepSampler extends AbstractSampler
{
	/**
	 * Constructs a new {@link StepSampler}.
	 */
	public StepSampler()
	{
		super();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is using stepwise sampling.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public double[][] sample(Model model, int[] angleIds, int nSamples,
			double[] angleLowerBounds, double[] angleUpperBounds)
	{
		check(model, angleIds, nSamples, angleLowerBounds, angleUpperBounds);
		
		// Avoid changing the angles of the input model. Note that copying the
		// model may turn out to be too expensive and more expensive than
		// storing all angles and restoring them after sampling.
		model = model.copy();

		final int nAngles = angleIds.length;
		final int lastAngle = nAngles - 1;
		final double[] angles = new double[nAngles];
		final List<Sample> samples = new ArrayList<Sample>(nSamples);
		final double[] deltaAngles = createDeltaAngles(angleLowerBounds,
				angleUpperBounds, nAngles, nSamples);

		resetAngles(angles, angleLowerBounds, 0, nAngles);

		for(int i = lastAngle;; i = lastAngle) {
			addSample(samples, model, angleIds, angles, nAngles);

			while(i >= 0 && angles[i] + deltaAngles[i] >= angleUpperBounds[i]) {
				i--;
			}

			if(i < 0) {
				break;
			}

			angles[i] += deltaAngles[i];
			resetAngles(angles, angleLowerBounds, i + 1, nAngles);
		}

		return formatSamples(samples, angleIds);
	}

	@Override
	protected int getNumberOfSamples(int nSamples, int nAngles)
	{
		return (int)Math.pow(nSamples, 1d / nAngles);
	}

	private static double[] createDeltaAngles(double[] angleLowerBounds,
			double[] angleUpperBounds, int nAngles, int nSamples)
	{
		final double[] deltaAngles = new double[nAngles];

		for(int i = 0; i < nAngles; i++) {
			deltaAngles[i] =
				(angleUpperBounds[i] - angleLowerBounds[i]) / nSamples;
		}

		return deltaAngles;
	}

	private static void resetAngles(double[] angles, double[] angleLowerBounds,
			int from, int to)
	{
		for(int i = from; i < to; i++) {
			angles[i] = angleLowerBounds[i];
		}
	}
}
