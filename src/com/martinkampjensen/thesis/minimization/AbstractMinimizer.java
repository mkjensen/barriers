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
import java.util.List;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Random;

/**
 * This class provides a skeletal implementation of the {@link Minimizer}
 * interface, to minimize the effort required to implement this interface.
 * <p>
 * An implementation only needs to implement
 * {@link Minimizer#minimize(Model, boolean, int)}.
 */
public abstract class AbstractMinimizer implements Minimizer
{
	protected AbstractMinimizer()
	{
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public final Model minimize(Model model)
	{
		return minimize(model, 1).get(0);
	}

	protected static final void check(Model model, int nMinima)
	{
		if(model == null) {
			throw new NullPointerException("model == null");
		}
		else if(nMinima < 1) {
			throw new IllegalArgumentException("nMinima < 1");
		}
	}

	protected static final List<Model> unchangedInitialization(Model model,
			int nMinima)
	{
		final List<Model> minima = new ArrayList<Model>(nMinima);

		for(int i = 0; i < nMinima; i++) {
			minima.add(model.copy());
		}

		return minima;
	}

	protected static final List<Model> randomInitialization(Model model,
			int nMinima)
	{
		final int size = model.size();
		final double[] startPoint = new double[size];
		final List<Model> minima = new ArrayList<Model>(nMinima);

		for(int i = 0; i < nMinima; i++) {
			createRandomStartPoint(model, startPoint);

			final Model copy = model.copy();
			copy.setAngles(startPoint);

			minima.add(copy);
		}

		return minima;
	}

	protected static final void createUnchangedStartPoint(Model model,
			double[] startPoint)
	{
		final int size = model.size();
		for(int i = 0; i < size; i++) {
			startPoint[i] = model.getAngle(i);
		}
	}

	protected static final void createRandomStartPoint(Model model,
			double[] startPoint)
	{
		final int size = model.size();
		for(int i = 0; i < size; i++) {
			startPoint[i] = Random.nextDouble(0, Constant.TWO_PI);
		}
	}
}
