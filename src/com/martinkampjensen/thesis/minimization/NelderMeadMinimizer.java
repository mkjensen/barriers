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

package com.martinkampjensen.thesis.minimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.MultivariateRealOptimizer;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.apache.commons.math.optimization.direct.NelderMead;

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Debug;

/**
 * TODO: Document {@link NelderMeadMinimizer}.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Nelder%E2%80%93Mead_method">Nelder–Mead method</a>
 * @see <a href="http://commons.apache.org/math/api-2.2/org/apache/commons/math/optimization/direct/DirectSearchOptimizer.html">DirectSearchOptimizer</a>
 * @see <a href="http://commons.apache.org/math/api-2.2/org/apache/commons/math/optimization/direct/NelderMead.html">NelderMead</a>
 */
public final class NelderMeadMinimizer extends AbstractMinimizer
{
	/**
	 * The default initialization strategy to use.
	 */
	public static final InitializationStrategy DEFAULT_INITIALIZATION_STRATEGY =
		InitializationStrategy.RANDOM;

	/**
	 * The default reflection coefficient.
	 */
	public static final double DEFAULT_REFLECTION = 1.0;

	/**
	 * The default expansion coefficient.
	 */
	public static final double DEFAULT_EXPANSION = 2.0;

	/**
	 * The default contraction coefficient.
	 */
	public static final double DEFAULT_CONTRACTION = 0.5;

	/**
	 * The default shrinkage coefficient.
	 */
	public static final double DEFAULT_SHRINKAGE = 0.5;

	/**
	 * The default convergence checker.
	 */
	public static final RealConvergenceChecker DEFAULT_CONVERGENCE =
		new SimpleScalarValueChecker();

	/**
	 * The default maximum number of fitness function evaluations.
	 */
	public static final int DEFAULT_MAXIMUM_EVALUATIONS = Integer.MAX_VALUE;

	/**
	 * The default maximum number of iterations of the algorithm.
	 */
	public static final int DEFAULT_MAXIMUM_ITERATIONS = Integer.MAX_VALUE;

	private final InitializationStrategy _initialization;
	private final double _reflection;
	private final double _expansion;
	private final double _contraction;
	private final double _shrinkage;
	private final RealConvergenceChecker _convergence;
	private final int _maxEvaluations;
	private final int _maxIterations;
	private final MultivariateRealOptimizer _optimizer;

	public NelderMeadMinimizer()
	{
		this(DEFAULT_INITIALIZATION_STRATEGY);
	}

	public NelderMeadMinimizer(InitializationStrategy initialization)
	{
		this(initialization, DEFAULT_REFLECTION, DEFAULT_EXPANSION,
				DEFAULT_CONTRACTION, DEFAULT_SHRINKAGE, DEFAULT_CONVERGENCE,
				DEFAULT_MAXIMUM_EVALUATIONS, DEFAULT_MAXIMUM_ITERATIONS);
	}

	public NelderMeadMinimizer(InitializationStrategy initialization,
			double reflection, double expansion, double contraction,
			double shrinkage, RealConvergenceChecker convergence,
			int maxEvaluations, int maxIterations)
	{
		super();

		// TODO: Parameter checks.

		if(initialization != InitializationStrategy.UNCHANGED
				&& initialization != InitializationStrategy.RANDOM) {
			throw new IllegalArgumentException(
			"Unsupported InitializationStrategy");
		}

		_initialization = initialization;
		_reflection = reflection;
		_expansion = expansion;
		_contraction = contraction;
		_shrinkage = shrinkage;
		_convergence = convergence;
		_maxEvaluations = maxEvaluations;
		_maxIterations = maxIterations;
		_optimizer = createOptimizer();

		Debug.line("Created NelderMeadMinimizer (%s initialization, "
				+ "%f convergence, %f reflection, %f contraction, "
				+ "%f shrinkage, %s convergence, %d maxEvaluations, "
				+ "%d maxIterations)", _initialization, _reflection, _expansion,
				_contraction, _shrinkage, _convergence, _maxEvaluations,
				_maxIterations);
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public final List<Model> minimize(Model model, int nMinima)
	{
		check(model, nMinima);

		final MultivariateRealFunction objFunc = new ObjectiveFunction(model);
		final int size = model.size();
		final double[] startPoint = new double[size];
		final List<Model> minima = new ArrayList<Model>(nMinima);

		if(_initialization == InitializationStrategy.UNCHANGED) {
			createUnchangedStartPoint(model, startPoint);
		}

		for(int i = 0; i < nMinima; i++) {
			if(_initialization == InitializationStrategy.RANDOM) {
				createRandomStartPoint(model, startPoint);
			}

			RealPointValuePair pair = null;

			try {
				pair =
					_optimizer.optimize(objFunc, GoalType.MINIMIZE, startPoint);
			}
			catch(Exception e) {
				Main.errorExit(e, StatusCode.MINIMIZATION);
			}

			final Model minimum = model.copy();
			minimum.setAngles(pair.getPointRef());
			minima.add(minimum);
		}

		Collections.sort(minima);
		return minima;
	}

	private MultivariateRealOptimizer createOptimizer()
	{
		final MultivariateRealOptimizer optimizer =
			new NelderMead(_reflection, _expansion, _contraction, _shrinkage);

		optimizer.setConvergenceChecker(_convergence);
		optimizer.setMaxEvaluations(_maxEvaluations);
		optimizer.setMaxIterations(_maxIterations);

		return optimizer;
	}
}
