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

import java.util.Collections;
import java.util.List;

import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.impl.OpenBabelZMatrix;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;
import com.martinkampjensen.thesis.util.openbabel.OptimizationAlgorithm;

/**
 * TODO: Document {@link OpenBabelMinimizer}.
 */
public final class OpenBabelMinimizer extends AbstractMinimizer
{
	/**
	 * The default initialization strategy to use.
	 */
	public static final InitializationStrategy DEFAULT_INITIALIZATION_STRATEGY =
		InitializationStrategy.RANDOM;

	/**
	 * The default optimization algorithm to use.
	 */
	public static final OptimizationAlgorithm DEFAULT_ALGORITHM =
		OpenBabelData.DEFAULT_MINIMIZATION_ALGORITHM;

	/**
	 * The default number of steps to perform.
	 */
	public static final int DEFAULT_STEPS =
		OpenBabelData.DEFAULT_MINIMIZATION_STEPS;

	/**
	 * The default energy convergence criteria.
	 */
	public static final double DEFAULT_CONVERGENCE =
		OpenBabelData.DEFAULT_MINIMIZATION_CONVERGENCE;

	private final InitializationStrategy _initialization;
	private final OptimizationAlgorithm _algorithm;
	private final int _steps;
	private final double _convergence;

	public OpenBabelMinimizer()
	{
		this(DEFAULT_INITIALIZATION_STRATEGY, DEFAULT_ALGORITHM);
	}

	public OpenBabelMinimizer(InitializationStrategy initialization,
			OptimizationAlgorithm algorithm)
	{
		this(initialization, algorithm, DEFAULT_STEPS);
	}

	public OpenBabelMinimizer(InitializationStrategy initialization,
			OptimizationAlgorithm algorithm, int steps)
	{
		this(initialization, algorithm, steps, DEFAULT_CONVERGENCE);
	}

	public OpenBabelMinimizer(InitializationStrategy initialization,
			OptimizationAlgorithm algorithm, int steps, double convergence)
	{
		super();

		// TODO: Parameter checks.

		if(initialization != InitializationStrategy.UNCHANGED
				&& initialization != InitializationStrategy.RANDOM
				&& initialization != InitializationStrategy.CONFORMER) {
			throw new IllegalArgumentException(
			"Unsupported InitializationStrategy");
		}

		_initialization = initialization;
		_algorithm = algorithm;
		_steps = steps;
		_convergence = convergence;

		Debug.line("Created OpenBabelMinimizer (%s initialization, "
				+ "%s algorithm, %d steps, %f convergence)", _initialization,
				_algorithm, _steps, _convergence);
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 * @throws UnsupportedOperationException if <code>model</code> is not an
	 * instance of {@link OpenBabelZMatrix}.
	 */
	@Override
	public final List<Model> minimize(Model model, int nMinima)
	{
		check(model, nMinima);

		if(!(model instanceof OpenBabelZMatrix)) {
			throw new UnsupportedOperationException(
			"model must be an instance of OpenBabelZMatrix");
		}

		final OpenBabelZMatrix obzm = ((OpenBabelZMatrix)model);
		List<Model> models = null;

		switch(_initialization) {
		case UNCHANGED:
			models = unchangedInitialization(obzm, nMinima);
			break;
		case RANDOM:
			models = randomInitialization(obzm, nMinima);
			break;
		case CONFORMER:
			models = conformerInitialization(obzm, nMinima);
			break;
		}

		minimize(models);
		Collections.sort(models);

		return models;
	}

	private List<Model> conformerInitialization(OpenBabelZMatrix obzm,
			int nMinima)
	{
		return obzm.conformerSearch(nMinima,
				OpenBabelData.DEFAULT_CONFORMERS_NUMBER_OF_CHILDREN,
				OpenBabelData.DEFAULT_CONFORMERS_MUTABILITY,
				OpenBabelData.DEFAULT_CONFORMERS_CONVERGENCE);
	}

	private void minimize(List<Model> models)
	{
		final int nModels = models.size();
		for(int i = 0; i < nModels; i++) {
			final OpenBabelZMatrix model = (OpenBabelZMatrix)models.get(i);
			Debug.line("Energy before minimization: " + model.evaluate());
			model.minimize(_algorithm, _steps, _convergence);
			Debug.line("Energy after minimization: " + model.evaluate());
		}
	}
}
