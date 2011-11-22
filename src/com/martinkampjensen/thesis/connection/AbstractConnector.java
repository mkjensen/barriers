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

import java.util.ArrayList;
import java.util.List;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.evaluation.Evaluator;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Random;

/**
 * This class provides a skeletal implementation of the {@link Connector}
 * interface, to minimize the effort required to implement this interface.
 * <p>
 * An implementation only needs to implement
 * {@link #step(Model, Model, double, double[], double[], double[])} and
 * {@link #step(Model, Model, double, double[], List)} and can e.g. do so by
 * calling one of several methods in this class.
 */
public abstract class AbstractConnector implements Connector
{
	protected AbstractConnector()
	{
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is the same as calling
	 * {@link #connect(Model, Model, double)} with
	 * {@link Connector#DEFAULT_STEP_SIZE} as the step size.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public final Model connect(Model from, Model to)
	{
		return connect(from, to, DEFAULT_STEP_SIZE);
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public final Model connect(Model from, Model to, double stepSize)
	{
		check(from, to, stepSize);

		final double[] steps = calculateSteps(stepSize, from, to);
		final Evaluator evaluator = from.getEvaluator();

		if(evaluator != null && evaluator.prefersMultipleModels()) {
			return multipleEvalsConnect(from, to, stepSize, steps);
		}
		else {
			return singleEvalConnect(from, to, stepSize, steps);
		}
	}

	protected static final void check(Model from, Model to, double stepSize)
	{
		if(from == null) {
			throw new NullPointerException("from == null");
		}
		else if(to == null) {
			throw new NullPointerException("to == null");
		}
		else if(to.size() != from.size()) {
			throw new IllegalArgumentException("to.size() != from.size");
		}
		else if(stepSize <= 0) {
			throw new IllegalArgumentException("stepSize <= 0");
		}
	}

	protected static final double[] calculateSteps(double maxStepSize,
			Model from, Model to)
	{
		final double[] steps = new double[from.size()];
		calculateSteps(steps, maxStepSize, from, to);
		return steps;
	}

	protected static final void calculateSteps(double[] steps,
			double stepSize, Model from, Model to)
	{
		final int nAngles = steps.length;
		double scale = 1d;

		for(int i = 0; i < nAngles; i++) {
			double step = to.getAngle(i) - from.getAngle(i);

			// Choose the shortest possibility for connection.
			if(step > Constant.PI) {
				step = -(Constant.TWO_PI - step);
			}
			else if(step < -Constant.PI) {
				step = -(-Constant.TWO_PI - step);
			}

			steps[i] = step;

			final double stepScale = stepSize / Math.abs(step);
			if(stepScale < scale) {
				scale = stepScale;
			}
		}

		for(int i = 0; i < nAngles; i++) {
			final double step = steps[i] * scale;

			if(Math.abs(step) >= MINIMUM_STEP_SIZE) {
				steps[i] = step;
			}
			else {
				steps[i] = 0d;
			}
		}
	}

	protected static final boolean isNeighbors(Model a, Model b, double[] steps)
	{
		for(int i = 0, n = steps.length; i < n; i++) {
			final double absStep = Math.abs(steps[i]);
			final double absDist = Math.abs(a.getAngle(i) - b.getAngle(i));

			if(absStep < absDist) {
				if(absStep < Constant.TWO_PI - absDist) {
					return false;
				}
			}
		}

		return true;
	}

	protected static final void updateBarrier(Model model,
			double[] barrierValue, double[] barrierAngles)
	{
		final double value = model.evaluate();

		if(value > barrierValue[0]) {
			barrierValue[0] = value;
			model.getAngles(barrierAngles);
		}
	}

	protected static final void directStep(Model current, Model to,
			double[] steps)
	{
		for(int i = 0, n = steps.length; i < n; i++) {
			current.setAngle(i, current.getAngle(i) + steps[i]);
		}
	}

	protected static final void randomStep(Model model, double maxStepSize)
	{
		for(int i = 0, n = model.size(); i < n; i++) {
			final double step = Random.nextDouble(-maxStepSize, maxStepSize);
			model.setAngle(i, model.getAngle(i) + step);
		}
	}

	/**
	 * Called before every connection begins, that is, before a step method is
	 * called for the first time in a connection. Subclasses with advanced needs
	 * may override this method.
	 * 
	 * @param from the starting angle values configuration.
	 * @param to the ending angle values configuration.
	 * @param stepSize the step size.
	 * @param steps the calculated step sizes for each angle.
	 * @param usingMultipleEvaluations whether or not evaluations will be
	 *        performed using the
	 *        <code>Model.getEvaluator().evaluate(List&lt;Model&gt;)</code>
	 *        approach.
	 */
	protected void prepare(Model from, Model to, double stepSize,
			double[] steps, boolean usingMultipleEvaluations)
	{
	}

	/**
	 * Performs a step on the path from one angle values configuration of a
	 * {@link Model} to another.
	 * <p>
	 * If a step is more sophisticated than just adding or subtracting the step
	 * size for one or several angles, <code>barrierValue</code> and
	 * <code>barrierAngles</code> might need to be updated.
	 * <p>
	 * This method is used if <code>current.getEvaluator() == null</code> of if
	 * <code>current.getEvaluator().prefersMultipleModels() == false</code>.
	 * 
	 * @param current the current angle values configuration.
	 * @param to the ending angle values configuration.
	 * @param stepSize the step size.
	 * @param steps the calculated step sizes for each angle.
	 * @param barrierValue the currently known barrier value (as
	 *        <code>barrierValue[0]</code>).
	 * @param barrierAngles the angle values that, when assigned to
	 *        <code>current</code>, makes
	 *        <code>current.evaluate() == barrierValue</code>.
	 */
	protected abstract void step(Model current, Model to, double stepSize,
			double[] steps, double[] barrierValue, double[] barrierAngles);

	/**
	 * Performs a step on the path from one angle values configuration of a
	 * {@link Model} to another.
	 * <p>
	 * If a step is more sophisticated than just adding or subtracting the step
	 * size for one or several angles, intermediate models might need to be
	 * added to <code>models</code>.
	 * <p>
	 * This method is used if <code>current.getEvaluator() != null</code> and if
	 * <code>current.getEvaluator().prefersMultipleModels() == true</code>.
	 * 
	 * @param current the current angle values configuration.
	 * @param to the ending angle values configuration.
	 * @param stepSize the step size.
	 * @param steps the calculated step sizes for each angle.
	 * @param models the models visited during the connection so far.
	 */
	protected abstract void step(Model current, Model to, double stepSize,
			double[] steps, List<Model> models);

	private Model singleEvalConnect(Model from, Model to, double stepSize,
			double[] steps)
	{
		prepare(from, to, stepSize, steps, false);

		final double[] barrierValue = new double[] { from.evaluate() };
		final double[] barrierAngles = new double[from.size()];
		final Model current = from.copy();
		from.getAngles(barrierAngles);

		// Immediately change angles that are very close.
		for(int i = 0, n = steps.length; i < n; i++) {
			if(steps[i] == 0d) {
				current.setAngle(i, to.getAngle(i));
			}
		}
		updateBarrier(current, barrierValue, barrierAngles);

		while(!isNeighbors(current, to, steps)) {
			step(current, to, stepSize, steps, barrierValue, barrierAngles);
			updateBarrier(current, barrierValue, barrierAngles);
		}

		if(barrierValue[0] >= to.evaluate()) {
			// Use "current" to return a Model with the angles of the barrier.
			current.setAngles(barrierAngles);
			return current;
		}
		else {
			// Only happens if the fitness value of "to" is greater than the
			// greatest fitness value on the walk from "from" to "to". This
			// means that "to" is not a local minimum.
			return to.copy();
		}
	}

	private Model multipleEvalsConnect(Model from, Model to, double stepSize,
			double[] steps)
	{
		prepare(from, to, stepSize, steps, true);

		final Evaluator evaluator = from.getEvaluator();
		final List<Model> models = new ArrayList<Model>(); // TODO: capacity
		Model current = from;
		double barrierFitness = Double.NEGATIVE_INFINITY;
		Model barrier = null;

		// Immediately change angles that are very close.
		current = current.copy();
		for(int i = 0, n = steps.length; i < n; i++) {
			if(steps[i] == 0d) {
				current.setAngle(i, to.getAngle(i));
			}
		}

		models.add(from.copy());
		models.add(to.copy());
		models.add(current);

		while(!isNeighbors(current, to, steps)) {
			current = current.copy();
			step(current, to, stepSize, steps, models);
			models.add(current);

			if(models.size() >= 100) { // TODO: Why 100?
				final double[] fitness = evaluator.evaluate(models);
				int barrierId = -1;

				for(int i = 0, n = fitness.length; i < n; i++) {
					final double fitnessI = fitness[i];

					if(fitnessI > barrierFitness) {
						barrierFitness = fitnessI;
						barrierId = i;
					}
				}

				if(barrierId > -1) {
					barrier = models.get(barrierId);
				}

				models.clear();
			}
		}

		if(!models.isEmpty()) {
			final double[] fitness = evaluator.evaluate(models);
			int barrierId = -1;

			for(int i = 0, n = fitness.length; i < n; i++) {
				final double fitnessI = fitness[i];

				if(fitnessI > barrierFitness) {
					barrierFitness = fitnessI;
					barrierId = i;
				}
			}

			if(barrierId > -1) {
				barrier = models.get(barrierId);
			}
		}

		return barrier;
	}
}
