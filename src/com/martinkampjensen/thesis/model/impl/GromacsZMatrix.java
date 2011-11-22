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

package com.martinkampjensen.thesis.model.impl;

import java.io.Serializable;
import java.util.List;

import com.martinkampjensen.thesis.evaluation.Evaluator;
import com.martinkampjensen.thesis.evaluation.GromacsEvaluator;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.ZMatrix;
import com.martinkampjensen.thesis.model.impl.OpenBabelZMatrix.CartesianBasedOpenBabelZMatrix;
import com.martinkampjensen.thesis.util.gromacs.Gromacs;
import com.martinkampjensen.thesis.util.openbabel.OBMol;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;
import com.martinkampjensen.thesis.util.openbabel.OptimizationAlgorithm;

/**
 * This class is a sad hack to create a class that uses GROMACS for energy
 * evaluation. The original idea was to use the {@link Evaluator} interface to
 * specify the method of energy evaluation and then just have different classes
 * implementing the {@link ZMatrix} interface without any evaluation logic. Oh
 * well, maybe some day...
 * <p>
 * Note that {@link #evaluate()} and {@link #evaluate(List)} are not
 * thread-safe per instance of this class. That can be fixed by using multiple
 * evaluators or letting the evaluator handle thread-safety (e.g. by
 * communicating with several instances of GROMACS).
 * 
 * @see <a href="http://www.gromacs.org/">GROMACS</a>
 */
public final class GromacsZMatrix extends CartesianBasedOpenBabelZMatrix 
implements Serializable
{
	private static final long serialVersionUID = -6753065242300964715L;
	private static Evaluator _evaluator;

	// TODO: This class should not exist. Instead, OpenBabelZMatrix should support different Evaluators.
	public GromacsZMatrix(OpenBabelData obData)
	{
		super(obData);

		if(_evaluator == null) {
			_evaluator = new GromacsEvaluator(obData.getMoleculeFile(),
					obData.getTopologyFile());
		}
	}

	public GromacsZMatrix(GromacsZMatrix gzm)
	{
		super(gzm);
	}

	private GromacsZMatrix(GromacsZMatrix gzm, OBMol molecule)
	{
		super(gzm, molecule);
	}

	@Override
	public Evaluator getEvaluator()
	{
		return _evaluator;
	}

	@Override
	public GromacsZMatrix copy()
	{
		return new GromacsZMatrix(this);
	}

	@Override
	public void evaluate(List<? extends Model> models)
	{
		final double[] fitness = _evaluator.evaluate(models);

		for(int i = 0, n = models.size(); i < n; i++) {
			final Model model = models.get(i);
			model.setFitness(fitness[i]);
		}
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void minimize(OptimizationAlgorithm algorithm, int steps,
			double convergence)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Minimizes this object using GROMACS.
	 */
	public void minimize()
	{
		// TODO: Let the minimizer take care of interfacing with the Gromacs class.
		fromPdb(Gromacs.getInstance().minimize(toPdb()));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses GROMACS.
	 * 
	 * @see <a href="http://www.gromacs.org/">GROMACS</a>
	 */
	@Override
	protected double calculateFitness()
	{
		updateOpenBabel();
		return _evaluator.evaluate(this);
	}

	@Override
	protected GromacsZMatrix createConformer(OBMol molecule)
	{
		return new GromacsZMatrix(this, molecule);
	}
}
