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
import com.martinkampjensen.thesis.model.impl.GromacsZMatrix;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;

/**
 * TODO: Document {@link GromacsMinimizer}.
 */
public final class GromacsMinimizer extends AbstractMinimizer
{
	/**
	 * The default initialization strategy to use.
	 */
	public static final InitializationStrategy DEFAULT_INITIALIZATION_STRATEGY =
		InitializationStrategy.RANDOM;

	private final InitializationStrategy _initialization;

	public GromacsMinimizer()
	{
		this(DEFAULT_INITIALIZATION_STRATEGY);
	}

	public GromacsMinimizer(InitializationStrategy initialization)
	{
		super();

		if(initialization != InitializationStrategy.UNCHANGED
				&& initialization != InitializationStrategy.RANDOM
				&& initialization != InitializationStrategy.CONFORMER) {
			throw new IllegalArgumentException(
			"Unsupported InitializationStrategy");
		}

		_initialization = initialization;

		Debug.line("Created GromacsMinimizer (%s initialization)",
				_initialization);
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public final List<Model> minimize(Model model, int nMinima)
	{
		check(model, nMinima);

		if(!(model instanceof GromacsZMatrix)) {
			throw new UnsupportedOperationException(
			"model must be an instance of GromacsZMatrix");
		}

		final GromacsZMatrix gzm = ((GromacsZMatrix)model);
		List<Model> models = null;

		switch(_initialization) {
		case UNCHANGED:
			models = unchangedInitialization(gzm, nMinima);
			break;
		case RANDOM:
			models = randomInitialization(gzm, nMinima);
			break;
		case CONFORMER:
			models = conformerInitialization(gzm, nMinima);
			break;
		}

		minimize(models);
		gzm.evaluate(models);
		Collections.sort(models);

		return models;
	}

	private List<Model> conformerInitialization(GromacsZMatrix gzm,
			int nMinima)
	{
		return gzm.conformerSearch(nMinima,
				OpenBabelData.DEFAULT_CONFORMERS_NUMBER_OF_CHILDREN,
				OpenBabelData.DEFAULT_CONFORMERS_MUTABILITY,
				OpenBabelData.DEFAULT_CONFORMERS_CONVERGENCE);
	}

	private void minimize(List<Model> models)
	{
		final int nModels = models.size();

		// For status.
		final int calculationsTotal = nModels;
		final boolean performStatus = (calculationsTotal >= 10);
		final int calculationsTwentieth =
			Math.max(1, (int)Math.floor(calculationsTotal / 20d));
		int calculationsMilestone = calculationsTwentieth;
		int calculationsDone = 0;
		if(performStatus) System.err.print("Minimizations performed: [0%");

		for(int i = 0; i < nModels; i++) {
			final GromacsZMatrix model = (GromacsZMatrix)models.get(i);
			model.minimize();

			// For status.
			if(performStatus) {
				calculationsDone++;
				if(calculationsDone >= calculationsMilestone) {
					System.err.print("..." + (int)(100 * calculationsDone /
							(double)calculationsTotal) + "%");
					if(calculationsDone == calculationsTotal)
						System.err.println("]");
					while(calculationsDone >= calculationsMilestone)
						calculationsMilestone += calculationsTwentieth;
					calculationsMilestone =
						Math.min(calculationsMilestone, calculationsTotal);
				}
			}
		}
	}
}
