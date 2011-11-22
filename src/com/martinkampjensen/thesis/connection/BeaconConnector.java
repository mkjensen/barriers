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

package com.martinkampjensen.thesis.connection;

import java.util.ArrayList;
import java.util.List;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.barriers.neighborhood.Neighborhood;
import com.martinkampjensen.thesis.barriers.neighborhood.RmsdAngleDifferenceNeighborhood;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.Random;

/**
 * TODO: Document {@link BeaconConnector}.
 * <p>
 * NOTE: THIS CLASS IS NOT THREAD-SAFE! That is, funny things will happen if the
 * connect method is called again before it has returned.
 */
public final class BeaconConnector extends AbstractConnector
{
	private final int _nSamples;
	private final double _directProbabilityIncrease;
	private final double _directProbabilityDecrease;
	private final List<Model> _samples;
	private final Neighborhood _neighborhood;
	private double _directProbability;
	private double[] _lowerBounds;
	private double[] _upperBounds;
	private double[] _angles;
	private double[] _bestAngles;
	private Model _beacon;

	/**
	 * Constructs a new {@link BeaconConnector} with <code>nSamples</code>,
	 * <code>directProbabilityIncrease</code>, and
	 * <code>directProbabilityDecrease</code> set to <code>50</code>,
	 * <code>0.1</code>, and <code>0.01</code>, respectively.
	 */
	public BeaconConnector()
	{
		this(50, 0.1, 0.01); // TODO: Why these values?
	}

	/**
	 * Constructs a new {@link BeaconConnector}.
	 * 
	 * @param nSamples the number of samples to perform on the borders of the
	 * hybercube.
	 * @param directProbabilityIncrease how much to increase the probability of
	 *        performing a direct connnection after a beacon connection that
	 *        resulted in the connection moving away from the target was
	 *        performed.
	 * @param directProbabilityDecrease how much to decrease the probability of
	 *        performing a direct connection after a direct connection was
	 *        performed or after a beacon connection that resulted in the
	 *        connection moving closer to the target was performed.
	 * @throws IllegalArgumentException if <code>nBeacons &lt; 1</code> or if
	 *         <code>directProbabilityIncrease &lt; 0</code> or if
	 *         <code>directProbabilityIncrease &gt; 1</code> or if
	 *         <code>directProbabilityDecrease &lt; 0</code> or if
	 *         <code>directProbabilityDecrease &gt; 1</code>.
	 */
	public BeaconConnector(int nSamples, double directProbabilityIncrease,
			double directProbabilityDecrease)
	{
		super();

		if(nSamples < 1) {
			throw new IllegalArgumentException("nSamples < 1");
		}
		else if(directProbabilityIncrease < 0) {
			throw new IllegalArgumentException("directProbabilityIncrease < 0");
		}
		else if(directProbabilityIncrease > 1) {
			throw new IllegalArgumentException("directProbabilityIncrease > 1");
		}
		else if(directProbabilityDecrease < 0) {
			throw new IllegalArgumentException("directProbabilityDecrease < 0");
		}
		else if(directProbabilityDecrease > 1) {
			throw new IllegalArgumentException("directProbabilityDecrease > 1");
		}

		_nSamples = nSamples;
		_directProbabilityIncrease = directProbabilityIncrease;
		_directProbabilityDecrease = directProbabilityDecrease;
		_samples = new ArrayList<Model>(nSamples);
		_neighborhood = new RmsdAngleDifferenceNeighborhood();

		Debug.line("Created BeaconConnector (%d nSamples, "
				+ "%f directProbabilityIncrease, %f directProbabilityDecrease)",
				_nSamples, _directProbabilityIncrease,
				_directProbabilityDecrease);
	}

	@Override
	protected void prepare(Model from, Model to, double stepSize,
			double[] steps, boolean usingMultipleEvaluations)
	{
		final int nAngles = steps.length;

		_directProbability = 0d;
		_lowerBounds = new double[nAngles];
		_upperBounds = new double[nAngles];

		if(usingMultipleEvaluations) {
			final List<Model> samples = _samples;
			samples.clear();

			// TODO: from is copied before each connection because bond lengths and bond angles may differ :(
			for(int i = 0, n = _nSamples; i < n; i++) {
				samples.add(from.copy());
			}
		}
		else {
			_angles = new double[nAngles];
			_bestAngles = new double[nAngles];
			_beacon = from.copy();
		}
	}

	@Override
	protected void step(Model current, Model to, double stepSize,
			double[] steps, double[] barrierValue, double[] barrierAngles)
	{
		final boolean toIsInHypercube = updateHypercube(current, to, stepSize);

		if(toIsInHypercube) {
			directConnect(current, to, stepSize, steps, barrierValue,
					barrierAngles);
		}
		else if(_directProbability >= Random.nextDouble()) {
			calculateSteps(steps, stepSize, current, to);
			directStep(current, to, steps);
			decreaseDirectProbability();
		}
		else {
			beaconStep(current, to, stepSize, steps, barrierValue,
					barrierAngles);
		}
	}

	@Override
	protected void step(Model current, Model to, double stepSize,
			double[] steps, List<Model> models)
	{
		final boolean toIsInHypercube = updateHypercube(current, to, stepSize);

		if(toIsInHypercube) {
			directConnect(current, to, stepSize, steps, models);
		}
		else if(_directProbability >= Random.nextDouble()) {
			calculateSteps(steps, stepSize, current, to);
			directStep(current, to, steps);
			decreaseDirectProbability();
		}
		else {
			beaconStep(current, to, stepSize, steps, models);
		}
	}

	private static final void directConnect(Model current, Model to,
			double stepSize, double[] steps, double[] barrierValue,
			double[] barrierAngles)
	{
		calculateSteps(steps, stepSize, current, to);

		while(!isNeighbors(current, to, steps)) {
			directStep(current, to, steps);
			updateBarrier(current, barrierValue, barrierAngles);
		}

		current.set(to);
	}

	private static final void directConnect(Model current, Model to,
			double stepSize, double[] steps, List<Model> models)
	{
		final Model temp = current;
		calculateSteps(steps, stepSize, current, to);

		while(!isNeighbors(current, to, steps)) {
			current = current.copy();
			directStep(current, to, steps);
			models.add(current);
		}

		temp.set(to);
	}

	private boolean updateHypercube(Model current, Model to, double stepSize)
	{
		final int nAngles = current.size();
		final double backwardFactor = 1d; // TODO: Why these values? Take into account that angles are cyclical?
		final double forwardFactor = 3d;
		final double overshootFactor = 2d;
		boolean toIsInHypercube = true;

		for(int i = 0; i < nAngles; i++) {
			final double currentAngle = current.getAngle(i);
			final double toAngle = to.getAngle(i);
			double lowerBound;
			double upperBound;

			if(toAngle - currentAngle > 0) {
				// currentAngle must increase.
				lowerBound = Math.min(currentAngle - stepSize * backwardFactor,
						toAngle - stepSize * overshootFactor);
				upperBound = Math.min(currentAngle + stepSize * forwardFactor,
						toAngle + stepSize * overshootFactor);
			}
			else {
				// currentAngle must decrease.
				lowerBound = Math.max(currentAngle - stepSize * forwardFactor,
						toAngle - stepSize * overshootFactor);
				upperBound = Math.max(currentAngle + stepSize * backwardFactor,
						toAngle + stepSize * overshootFactor);
			}

			_lowerBounds[i] = lowerBound;
			_upperBounds[i] = upperBound;

			if(lowerBound >= 0d) {
				if(lowerBound < Constant.TWO_PI) {
					if(toAngle < lowerBound) {
						toIsInHypercube = false;
						continue;
					}
				}
				else if(toAngle < lowerBound - Constant.TWO_PI) {
					toIsInHypercube = false;
					continue;
				}
			}
			else if(toAngle < lowerBound + Constant.TWO_PI) {
				toIsInHypercube = false;
				continue;
			}

			if(upperBound >= 0d) {
				if(upperBound < Constant.TWO_PI) {
					if(toAngle > upperBound) {
						toIsInHypercube = false;
						continue;
					}
				}
				else if(toAngle > upperBound - Constant.TWO_PI) {
					toIsInHypercube = false;
					continue;
				}
			}
			else if(toAngle > upperBound + Constant.TWO_PI) {
				toIsInHypercube = false;
				continue;
			}
		}

		return toIsInHypercube;
	}

	private void beaconStep(Model current, Model to, double stepSize,
			double[] steps, double[] barrierValue, double[] barrierAngles)
	{
		final int nAngles = steps.length;
		final double[] lowerBounds = _lowerBounds;
		final double[] upperBounds = _upperBounds;
		final Model beacon = _beacon;
		double[] angles = _angles;
		double[] bestAngles = _bestAngles;
		double minFitness = Double.POSITIVE_INFINITY;

		// Sample angle values on the sides of the hybercube.
		for(int i = 0, n = _nSamples; i < n; i++) {
			// Fix one of the angles to select one of the sides of the
			// hybercube.
			final int fixedAngleId = Random.nextInt(nAngles);
			if(Random.nextBoolean()) {
				angles[fixedAngleId] = lowerBounds[fixedAngleId];
			}
			else {
				angles[fixedAngleId] = upperBounds[fixedAngleId];
			}

			// Randomize the rest of the angles along the side of the hybercube.
			for(int j = 0; j < nAngles; j++) {
				if(j != fixedAngleId) {
					angles[j] =
						Random.nextDouble(lowerBounds[j], upperBounds[j]);
				}
			}

			// Check whether or not it was a new best angle configuration.
			beacon.setAngles(angles);
			final double fitness = beacon.evaluate();
			if(fitness < minFitness) {
				minFitness = fitness;
				final double[] temp = bestAngles;
				bestAngles = angles;
				angles = temp;
			}
		}

		// Use direct connection from the current configuration to the beacon.
		beacon.setAngles(bestAngles);
		final double oldDistance = distance(current, to);
		directConnect(current, beacon, stepSize, steps, barrierValue,
				barrierAngles);
		final double newDistance = distance(current, to);

		// Determine if the new angles are closer to or farther from the goal.
		if(newDistance < oldDistance) {
			decreaseDirectProbability();
		}
		else {
			increaseDirectProbability();
		}
	}

	private void beaconStep(Model current, Model to, double stepSize,
			double[] steps, List<Model> models)
	{
		final int nSamples = _nSamples;
		final List<Model> samples = _samples;
		final int nAngles = steps.length;
		final double[] lowerBounds = _lowerBounds;
		final double[] upperBounds = _upperBounds;

		// Sample angle values on the sides of the hybercube.
		for(int i = 0; i < nSamples; i++) {
			final Model sample = samples.get(i);

			// Fix one of the angles to select one of the sides of the
			// hybercube.
			final int fixedAngleId = Random.nextInt(nAngles);
			if(Random.nextBoolean()) {
				sample.setAngle(fixedAngleId, lowerBounds[fixedAngleId]);
			}
			else {
				sample.setAngle(fixedAngleId, upperBounds[fixedAngleId]);
			}

			// Randomize the rest of the angles along the side of the hybercube.
			for(int j = 0; j < nAngles; j++) {
				if(j != fixedAngleId) {
					sample.setAngle(j,
							Random.nextDouble(lowerBounds[j], upperBounds[j]));
				}
			}
		}

		final double[] fitness = current.getEvaluator().evaluate(samples);
		double beaconFitness = fitness[0];
		int beaconId = 0;

		for(int i = 1; i < nSamples; i++) {
			final double fitnessI = fitness[i];

			if(fitnessI < beaconFitness) {
				beaconFitness = fitnessI;
				beaconId = i;
			}
		}

		// Use direct connection from the current configuration to the beacon.
		final Model beacon = samples.get(beaconId);
		final double oldDistance = distance(current, to);
		directConnect(current, beacon, stepSize, steps, models);
		final double newDistance = distance(current, to);

		// Determine if the new angles are closer to or farther from the goal.		
		if(newDistance < oldDistance) {
			decreaseDirectProbability();
		}
		else {
			increaseDirectProbability();
		}
	}

	private double distance(Model current, Model to)
	{
		return _neighborhood.distance(current, to);
	}

	private void increaseDirectProbability()
	{
		_directProbability += _directProbabilityIncrease;

		if(_directProbability > 1d) {
			_directProbability = 1d;
		}
	}

	private void decreaseDirectProbability()
	{
		_directProbability -= _directProbabilityDecrease;

		if(_directProbability < 0d) {
			_directProbability = 0d;
		}
	}
}
