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

import java.util.List;

import com.martinkampjensen.thesis.model.Model;

/**
 * A {@link Minimizer} optimizes the angles of a {@link Model} so that the
 * {@link Model#evaluate()} method returns a value that is as small as possible.
 * <p>
 * Note that there is no quality assurances, that is, the minima computed may be
 * local minima with a fitness value much higher than the actual minimum and the
 * smallest local minima. 
 */
public interface Minimizer
{
	/**
	 * Computes and returns a local minimum.
	 * 
	 * @param model the model to minimize.
	 * @return a local minimum for the model.
	 * @throws NullPointerException if <code>model</code> is <code>null</code>.
	 */
	Model minimize(Model model);

	/**
	 * Computes and returns a list of local minima. The list will be sorted in
	 * increasing order by fitness value.
	 * 
	 * @param model the model to minimize.
	 * @param nMinima the number of minima to find.
	 * @return a list of local minima for the model.
	 * @throws NullPointerException if <code>model</code> is <code>null</code>.
	 * @throws IllegalArgumentException if <code>nMinima &lt; 1</code>.
	 */
	List<Model> minimize(Model model, int nMinima);
}
