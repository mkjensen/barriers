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

import java.io.File;
import java.util.List;

import com.martinkampjensen.thesis.model.CartesianModel;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.gromacs.Gromacs;

/**
 * TODO: Document {@link GromacsEvaluator}.
 */
public final class GromacsEvaluator extends AbstractEvaluator
{
	private final StringBuilder _sb;
	private final Gromacs _gromacs;

	public GromacsEvaluator(File moleculeFile, File topologyFile)
	{
		_sb = new StringBuilder(1024 * 1024);
		_gromacs = Gromacs.getInstance(moleculeFile, topologyFile);

		Debug.line("Created GromacsEvaluator");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The implementation of this evaluator prefers evaluating multiple models
	 * in one call. The cost per model difference between evaluating multiple
	 * models in one call and evaluating single models can be one or two orders
	 * of magnitude.
	 */
	@Override
	public boolean prefersMultipleModels()
	{
		return true;
	}

	@Override
	public double evaluate(Model model)
	{
		return _gromacs.evaluate(model.toPdb());
	}

	@Override
	public double evaluate(CartesianModel model)
	{
		return _gromacs.evaluate(model.toPdb());
	}

	@Override
	public double[] evaluate(List<? extends Model> models)
	{
		final int nModels = models.size();
		final StringBuilder sb = _sb;
		sb.delete(0, Integer.MAX_VALUE);

		for(int i = 0; i < nModels; i++) {
			final Model model = models.get(i);
			final String pdb = model.toPdb();
			sb.append(pdb, 0, pdb.indexOf("CONECT"));
			sb.append("TER\nENDMDL\n");
		}

		final double[] fitness = _gromacs.evaluate(sb.toString(), nModels);

		for(int i = 0, n = models.size(); i < n; i++) {
			final Model model = models.get(i);
			model.setFitness(fitness[i]);
		}

		return fitness;
	}
}
