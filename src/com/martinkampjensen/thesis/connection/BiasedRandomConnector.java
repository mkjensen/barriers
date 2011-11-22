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

import java.util.List;

import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.Random;

/**
 * TODO: Document {@link BiasedRandomConnector}.
 */
public final class BiasedRandomConnector extends AbstractConnector
{
	private final double _randomProbability;

	/**
	 * Constructs a new {@link BiasedRandomConnector} with
	 * <code>randomProbability</code> set to <code>0.5</code>.
	 */
	public BiasedRandomConnector()
	{
		this(0.5);

		Debug.line("Created BiasedRandomConnector");
	}

	/**
	 * Constructs a new {@link BiasedRandomConnector}.
	 * <p>
	 * If <code>randomProbability</code> is set to <code>0</code>, this
	 * connector is effectively equal to the {@link DirectConnector}. If
	 * <code>randomProbability</code> is set to <code>1</code>, thie behavior of
	 * this connector will be completely random.
	 * 
	 * @param randomProbability the probability of a random step.
	 * @throws IllegalArgumentException if <code>randomProbability</code> is not
	 *         between <code>0</code> and <code>1</code> (inclusive).
	 */
	public BiasedRandomConnector(double randomProbability)
	{
		super();

		if(randomProbability < 0d) {
			throw new IllegalArgumentException("randomProbability < 0");
		}
		else if(randomProbability > 1d) {
			throw new IllegalArgumentException("randomProbability > 1");
		}

		_randomProbability = randomProbability;
	}

	@Override
	protected void step(Model current, Model to, double stepSize,
			double[] steps, double[] barrierValue, double[] barrierAngles)
	{
		step(current, to, stepSize, steps);
	}

	@Override
	protected void step(Model current, Model to, double stepSize,
			double[] steps, List<Model> model)
	{
		step(current, to, stepSize, steps);
	}

	private void step(Model current, Model to, double stepSize, double[] steps)
	{
		if(_randomProbability > Random.nextDouble()) {
			randomStep(current, stepSize);
		}
		else {
			calculateSteps(steps, stepSize, current, to);
			directStep(current, to, steps);
		}
	}
}
