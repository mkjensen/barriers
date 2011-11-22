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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.martinkampjensen.thesis.model.Model;

/**
 * This class provides a skeletal implementation of the {@link Sampler}
 * interface, to minimize the effort required to implement this interface.
 * <p>
 * An implementation only needs to implement
 * {@link Sampler#sample(Model, int[], int, double[], double[])}.
 */
public abstract class AbstractSampler implements Sampler
{
	protected AbstractSampler()
	{
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses {@link #DEFAULT_NUMBER_OF_SAMPLES} for the
	 * number of samples.
	 */
	@Override
	public final double[][] sample(Model model)
	{
		return sample(model, DEFAULT_NUMBER_OF_SAMPLES);
	}

	@Override
	public final double[][] sample(Model model, int nSamples)
	{
		final int size = model.size();
		final int[] angleIds = new int[size];

		for(int i = 0; i < size; i++) {
			angleIds[i] = i;
		}

		return sample(model, angleIds, nSamples);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses {@link #DEFAULT_NUMBER_OF_SAMPLES} for the
	 * number of samples.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public final double[][] sample(Model model, int[] angleIds)
	{
		return sample(model, angleIds, getNumberOfSamples(angleIds));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses {@link #DEFAULT_ANGLE_LOWER_BOUND} and
	 * {@link #DEFAULT_ANGLE_UPPER_BOUND} as values for the angle lower bounds
	 * and angle upper bounds, respectively.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public final double[][] sample(Model model, int[] angleIds, int nSamples)
	{
		final int nAngles = angleIds.length;
		final double[] angleLowerBounds = new double[nAngles];
		final double[] angleUpperBounds = new double[nAngles];

		for(int i = 0; i < nAngles; i++) {
			angleLowerBounds[i] = DEFAULT_ANGLE_LOWER_BOUND;
			angleUpperBounds[i] = DEFAULT_ANGLE_UPPER_BOUND;
		}

		return sample(model, angleIds, nSamples, angleLowerBounds,
				angleUpperBounds);
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public final String sampleToString(Model model, int[] angleIds)
	{
		final double[][] samples = sample(model, angleIds);
		return arrayToString(samples, angleIds);
	}

	protected static final void check(Model model, int[] angleIds, int nSamples,
			double[] angleLowerBounds, double[] angleUpperBounds)
	{
		if(model == null) {
			throw new NullPointerException("model == null");
		}
		else if(angleIds == null) {
			throw new NullPointerException("angleIds == null");
		}
		else if(angleLowerBounds == null) {
			throw new NullPointerException("angleLowerBounds == null");
		}
		else if(angleUpperBounds == null) {
			throw new NullPointerException("angleUpperBounds == null");

		}

		final int size = model.size();
		final int nAngleIds = angleIds.length;

		if(nAngleIds < 1) {
			throw new IllegalArgumentException(
			"angleIds.length < 1");
		}
		else if(nAngleIds > size) {
			throw new IllegalArgumentException(
			"angleIds.length > model.size()");
		}
		else if(nSamples < 1) {
			throw new IllegalArgumentException("nSamples < 1");
		}
		else if(angleLowerBounds.length != angleIds.length) {
			throw new IllegalArgumentException(
			"angleLowerBounds.length != angleIds.length");
		}
		else if(angleUpperBounds.length != angleIds.length) {
			throw new IllegalArgumentException(
			"angleUpperBounds.length != angleIds.length");
		}

		for(int i = 0; i < nAngleIds; i++) {
			final int angleId = angleIds[i];

			if(angleId < 0 ) {
				throw new IllegalArgumentException(
						"angleIds[" + i + "] < 0)");
			}
			else if(angleId >= size) {
				throw new IllegalArgumentException(
						"angleIds[" + i + "] >= model.size()");
			}
		}
	}

	protected static final void addSample(List<Sample> samples, Model model,
			int[] angleIds, double[] angles, int nAngles)
	{
		for(int i = 0; i < nAngles; i++) {
			model.setAngle(angleIds[i], angles[i]);
		}

		final double value = model.evaluate();
		final Sample sample = new Sample(angles, value);

		samples.add(sample);
	}

	protected static final double[][] formatSamples(List<Sample> samples,
			int[] angleIds)
	{
		if(samples.isEmpty()) {
			return new double[0][0];
		}

		Collections.sort(samples);

		final int nSamples = samples.size();
		final int nAngles = angleIds.length;
		final double[][] samplesArray = new double[nAngles + 1][nSamples];

		for(int i = 0; i < nSamples; i++) {
			final Sample sample = samples.get(i);

			for(int j = 0; j < nAngles; j++) {
				final double angle = sample.angles[j];

				samplesArray[j][i] = Math.toDegrees(angle);
			}

			samplesArray[nAngles][i] = sample.value;
		}

		return samplesArray;
	}

	// TODO: Find a better/more precise way to determine the number of samples.
	protected abstract int getNumberOfSamples(int nSamples, int nAngles);

	private final int getNumberOfSamples(int[] nAngles)
	{
		return getNumberOfSamples(DEFAULT_NUMBER_OF_SAMPLES, nAngles.length);
	}

	private static final String arrayToString(double[][] samples,
			int[] angleIds)
	{
		if(samples.length == 0) {
			return "";
		}

		final int nSamples = samples[0].length;
		final int nAngles = angleIds.length;
		final StringBuilder sb = new StringBuilder();

		for(int i = 0; i < nAngles; i++) {
			sb.append("Angle");
			sb.append(angleIds[i]);
			sb.append('\t');
		}

		sb.append("Energy");
		sb.append('\n');

		for(int i = 0; i < nSamples; i++) {
			// When sampling two angles, gnuplot's pm3d method requires that
			// rows with differing first column values are separated by a blank
			// line.
			if(nAngles == 2 && i >= 1
					&& samples[0][i - 1] != samples[0][i]) {
				sb.append('\n');
			}

			for(int j = 0; j < nAngles; j++) {
				sb.append(samples[j][i]);
				sb.append('\t');
			}

			sb.append(samples[nAngles][i]);
			sb.append('\n');
		}

		return sb.toString();
	}

	protected static final class Sample implements Comparable<Sample>
	{
		double[] angles;
		double value;

		Sample(double[] angles, double value)
		{
			this.angles = Arrays.copyOf(angles, angles.length);
			this.value = value;
		}

		@Override
		public int compareTo(Sample other)
		{
			for(int i = 0; i < angles.length; i++) {
				final double thisAngle = angles[i];
				final double otherAngle = other.angles[i];

				if(thisAngle < otherAngle) {
					return -1;
				}
				else if(thisAngle > otherAngle) {
					return 1;
				}
			}

			return 0;
		}
	}
}
