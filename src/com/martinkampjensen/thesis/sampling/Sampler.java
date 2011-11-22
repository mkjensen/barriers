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

package com.martinkampjensen.thesis.sampling;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.model.Model;

/**
 * A {@link Sampler} samples the angle values of a {@link Model}. A sample is a
 * tuple of angle values and the fitness value ({@link Model#evaluate()})
 * resulting from those angle values.  
 */
public interface Sampler
{
	/**
	 * The default number of samples to be performed.
	 */
	int DEFAULT_NUMBER_OF_SAMPLES = 10000;

	/**
	 * The default lower bound for angle values.
	 */
	double DEFAULT_ANGLE_LOWER_BOUND = 0d;

	/**
	 * The default upper bound for angle values.
	 */
	double DEFAULT_ANGLE_UPPER_BOUND = Constant.TWO_PI;

	/**
	 * Creates and returns samples from a {@link Model}.
	 * 
	 * @param model the model to sample from.
	 * @return a two-dimensional array with the sample values.
	 * @throws NullPointerException if <code>model == null</code>.
	 */
	double[][] sample(Model model);

	/**
	 * TODO: Documentation.
	 * 
	 * @param model the model to sample from.
	 * @param nSamples the total number of samples desired.
	 * @return a two-dimensional array with the sample values.
	 * @throws NullPointerException if <code>model == null</code>.
	 * @throws IllegalArgumentException if <code>nSamples &lt; 1</code>.
	 */
	double[][] sample(Model model, int nSamples);

	/**
	 * Creates and returns samples from a {@link Model}.
	 * <p>
	 * Same as {@link #sample(Model, int[], int)} except <code>nSamples</code>
	 * is implementation-specific.
	 * 
	 * @param model the model to sample from.
	 * @param angleIds the angle ids of the model to sample.
	 * @return a two-dimensional array with the sample values.
	 * @throws NullPointerException if <code>model == null</code> or if
	 *         <code>anglesIds == null</code>.
	 * @throws IllegalArgumentException if <code>angleIds.length &lt; 1</code>
	 *         or if <code>angleIds.length > model.size()</code> or if any of
	 *         the elements in <code>angleIds</code> is less than <code>0</code>
	 *         or if any of the elements in <code>angleIds</code> is greater
	 *         than <code>model.size() - 1</code>.
	 */
	double[][] sample(Model model, int[] angleIds);

	/**
	 * Creates and returns samples from a {@link Model}.
	 * <p>
	 * Same as {@link #sample(Model, int[], int, double[], double[])} except
	 * <code>angleLowerBounds</code> and <code>angleUpperBounds</code> are
	 * implementation-specific.
	 * 
	 * @param model the model to sample from.
	 * @param angleIds the angle ids of the model to sample.
	 * @param nSamples the total number of samples desired.
	 * @return a two-dimensional array with the sample values.
	 * @throws NullPointerException if <code>model == null</code> or if
	 *         <code>anglesIds == null</code>.
	 * @throws IllegalArgumentException if <code>angleIds.length &lt; 1</code>
	 *         or if <code>angleIds.length > model.size()</code> or if any of
	 *         the elements in <code>angleIds</code> is less than <code>0</code>
	 *         or if any of the elements in <code>angleIds</code> is greater
	 *         than <code>model.size() - 1</code> or if
	 *         <code>nSamples &lt; 1</code>.
	 */
	double[][] sample(Model model, int[] angleIds, int nSamples);

	/**
	 * Creates and returns samples from a {@link Model}. A sample is a tuple of
	 * angle values and the fitness value ({@link Model#evaluate()}) resulting
	 * from those angle values.
	 * 
	 * @param model the model to sample from.
	 * @param angleIds the angle ids of the model to sample.
	 * @param nSamples the total number of samples desired.
	 * @param angleLowerBounds lower bounds on the angles specified by
	 *        <code>angleIds</code>.
	 * @param angleUpperBounds upper bounds on the angles specified by
	 *        <code>angleIds</code>.
	 * @return a two-dimensional array with the sample values.
	 * @throws NullPointerException if <code>model == null</code> or if
	 *         <code>anglesIds == null</code> or if
	 *         <code>angleLowerBounds == null</code> or if
	 *         <code>angleUpperBounds == null</code>.
	 * @throws IllegalArgumentException if <code>angleIds.length &lt; 1</code>
	 *         or if <code>angleIds.length > model.size()</code> or if any of
	 *         the elements in <code>angleIds</code> is less than <code>0</code>
	 *         or if any of the elements in <code>angleIds</code> is greater
	 *         than <code>model.size() - 1</code> or if
	 *         <code>nSamples &lt; 1</code> or if
	 *         <code>angleLowerBounds.length != angleIds.length</code> or if
	 *         <code>angleUpperBounds.length != angleIds.length</code>.
	 */
	double[][] sample(Model model, int[] angleIds, int nSamples,
			double[] angleLowerBounds, double[] angleUpperBounds);

	/**
	 * Creates and returns a string of tab-separated samples from a
	 * {@link Model}.
	 * <p>
	 * The implementation should use {@link #sample(Model, int[])} to create
	 * the samples.
	 * 
	 * @param model the model to sample from.
	 * @param angleIds the angle ids of the model to sample.
	 * @return a string with tab-separated angle values and sample values.
	 * @throws NullPointerException if <code>model == null</code> or if
	 *         <code>anglesIds == null</code>.
	 * @throws IllegalArgumentException if <code>angleIds.length &lt; 1</code>
	 *         or if <code>angleIds.length > model.size()</code> or if any of
	 *         the elements in <code>angleIds</code> is less than <code>0</code>
	 *         or if any of the elements in <code>angleIds</code> is greater
	 *         than <code>model.size() - 1</code>.
	 */
	String sampleToString(Model model, int[] angleIds);
}
