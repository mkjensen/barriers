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

import java.util.List;

import com.martinkampjensen.thesis.model.CartesianModel;
import com.martinkampjensen.thesis.model.Model;

/**
 * An {@link Evaluator} is a fitness function that must be designed so that the
 * fitness value has to be minimized in order to achieve optimality.
 * <p>
 * An evaluator can support any number of of evaluate methods, but it should of
 * course at least support one.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Fitness_function">Fitness function</a>
 */
public interface Evaluator
{
	/**
	 * Returns <code>true</code> if the performance of this evaluator is better
	 * when evaluating multiple models in one call instead of evaluating only
	 * one model at a time.
	 * 
	 * @return <code>true</code> if and only if
	 *         {@link #evaluate(Model) is more expensive than the average cost
	 *         per model when using {@link #evaluate(List)}.
	 */
	boolean prefersMultipleModels();

	/**
	 * Calculates and returns the fitness value of a model (optional operation).
	 * 
	 * @param model the model.
	 * @return the fitness value.
	 * @throws UnsupportedOperationException if this method is not supported.
	 */
	double evaluate(Model model) throws UnsupportedOperationException;

	/**
	 * Calculates and returns the fitness value of a model (optional operation).
	 * 
	 * @param model the model.
	 * @return the fitness value.
	 * @throws UnsupportedOperationException if this method is not supported.
	 */
	double evaluate(CartesianModel model) throws UnsupportedOperationException;

	/**
	 * Calculates and returns the fitness values of a list of models (optional
	 * operation).
	 * 
	 * @param models the models to evaluate.
	 * @return the fitness values of the models in <code>models</code>.
	 * @throws UnsupportedOperationException if this method is not supported.
	 */
	double[] evaluate(List<? extends Model> models)
	throws UnsupportedOperationException;
}
