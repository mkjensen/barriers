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

package com.martinkampjensen.thesis.evaluation;

import com.martinkampjensen.thesis.model.CartesianModel;
import com.martinkampjensen.thesis.util.Debug;

/**
 * An implementation of the {@link Evaluator} interface that uses the
 * Lennard-Jones potential between all pairs of points in Cartesian space as the
 * fitness value.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Lennard-Jones_potential">Lennard-Jones potential</a>
 */
public final class LennardJonesEvaluator extends AbstractEvaluator
{
	private static final int DIMENSIONS = 3;
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	private final double _a;
	private final double _b;
	private final double[] _p;
	private final double[] _q;

	/**
	 * Constructs a new {@link LennardJonesEvaluator} with default coefficients.
	 * The default coefficients are <code>1</code> for both <code>epsilon</code>
	 * and <code>sigma</code>.
	 */
	public LennardJonesEvaluator()
	{
		this(1d, 1d);
	}

	/**
	 * Constructs a new {@link LennardJonesEvaluator}.
	 * 
	 * @param epsilon the depth of the potential well.
	 * @param sigma the distance at which the inter-point potential is zero.
	 */
	public LennardJonesEvaluator(double epsilon, double sigma)
	{
		final double sigma6 = Math.pow(sigma, 6);
		_b = 4 * epsilon * sigma6;
		_a = _b * sigma6;

		_p = new double[DIMENSIONS];
		_q = new double[DIMENSIONS];

		Debug.line("Created LennardJonesEvaluator (%f potential depth, "
				+ "%f zero-potential distance)", epsilon, sigma);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The implementation of this evaluator does not have a preference and,
	 * hence, this method returns <code>false</code>.
	 */
	@Override
	public boolean prefersMultipleModels()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses the Lennard-Jones potential between all pairs of
	 * points in Cartesian space as the fitness value.
	 * 
	 * @see <a href="http://en.wikipedia.org/wiki/Lennard-Jones_potential">Lennard-Jones potential</a>
	 */
	@Override
	public double evaluate(CartesianModel model)
	{
		final int size = model.size();
		final int additionalSize = model.additionalSize();
		double value = 0d;

		for(int i = -additionalSize; i < 0; i++) {
			model.getAdditional(i, _p);

			// Additionals against additionals.
			for(int j = i + 1; j < 0; j++) {
				model.getAdditional(j, _q);
				value += ljp(_p, _q);
			}

			// Additionals against specifics.
			for(int j = 0; j < size; j++) {
				model.get(j, _q);
				value += ljp(_p, _q);
			}
		}

		for(int i = 0; i < size; i++) {
			model.get(i, _p);

			// Specifics against specifics.
			for(int j = i + 1; j < size; j++) {
				model.get(j, _q);
				value += ljp(_p, _q);
			}
		}

		return value;
	}

	private static double distance(double[] p, double[] q)
	{
		final double dx = p[X] - q[X];
		final double dx2 = dx * dx;
		final double dy = p[Y] - q[Y];
		final double dy2 = dy * dy;
		final double dz = p[Z] - q[Z];
		final double dz2 = dz * dz;

		return Math.sqrt(dx2 + dy2 + dz2);
	}

	private double ljp(double[] p, double[] q)
	{
		final double distance = distance(p, q);

		final double r6 = Math.pow(distance, 6);
		final double r12 = r6 * r6;
		final double repulsion = _a / r12;
		final double attraction = _b / r6;

		return repulsion - attraction; 
	}
}
