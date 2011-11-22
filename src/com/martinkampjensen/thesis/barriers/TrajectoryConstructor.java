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

package com.martinkampjensen.thesis.barriers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;
import com.martinkampjensen.thesis.barriers.neighborhood.Neighborhood;
import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;
import com.martinkampjensen.thesis.model.NodeFactory;
import com.martinkampjensen.thesis.model.impl.ImmutableModel;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.Print;
import com.martinkampjensen.thesis.util.R;
import com.martinkampjensen.thesis.util.Util;
import com.martinkampjensen.thesis.util.gromacs.EnergyExtractor;
import com.martinkampjensen.thesis.util.gromacs.XtcReader;
import com.martinkampjensen.thesis.util.openbabel.OBMol;
import com.martinkampjensen.thesis.util.openbabel.OpenBabel;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;

/**
 * TODO: Document {@link TrajectoryConstructor}.
 */
public final class TrajectoryConstructor extends AbstractConstructor
{
	public TrajectoryConstructor()
	{
		super();
	}

	// TODO: minDistance, maxDistance hints
	// Ethane: 0.55, 1.5
	// Propane: 0.425, ?
	// Butane: 1.65, ?
	// t-Butane: 0.65, ?
	// AcAANMe: 0.4, 0.9
	// AcAANMe/replica_0: 1.2, ?

	/**
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public BarrierForest construct(File moleculeFile, File trajectoryFile,
			File energyFile, Neighborhood neighborhood, double minDistance,
			double maxDistance)
	{
		check(moleculeFile, trajectoryFile, energyFile, neighborhood,
				minDistance, maxDistance);

		final boolean useSpecifiedMaxDistance = (maxDistance != -1d);

		final Models models = createModels(moleculeFile, trajectoryFile,
				energyFile, neighborhood, minDistance);
		final List<Model> modelsList = models.list;

		Debug.line("Sorting models by non-decreasing fitness value");
		Collections.sort(modelsList);

		if(!useSpecifiedMaxDistance) {
			Debug.line("Approximating smallest neighborhood threshold "
					+ "(maxDistance) that results in a single tree");
			final double minBinarySearchSpan = 0.02;
			final double maxMaxDistance =
				neighborhood.maximumDistance(modelsList.get(0));
			double lowerMaxDistance;

			// Binary search like approach for finding the smallest value of
			// maxDistance that results in a single tree. The smallest value is
			// not found exactly, but one quite close will be.
			for(double min = minDistance, max = maxMaxDistance;;) {
				lowerMaxDistance = (min + max) / 2d;
				final BarrierForest forest = construct(modelsList,
						models.totalConformations, neighborhood, minDistance,
						lowerMaxDistance, false);
				final int nTrees = forest.getNumberOfTrees();

				if(nTrees == 1) {
					Debug.line("Neighbor threshold %f results in 1 tree",
							lowerMaxDistance);
					max = lowerMaxDistance;

					if(max - min < minBinarySearchSpan) {
						break;
					}
				}
				else {
					Debug.line("Neighbor threshold %f results in %d trees",
							lowerMaxDistance, nTrees);
					min = lowerMaxDistance;
				}
			}

			Debug.line("Approximating smallest neighborhood threshold "
					+ "(maxDistance) that results in a single leaf");
			double upperMaxDistance;

			// Binary search like approach for finding the smallest value of
			// maxDistance that results in a single leaf. The smallest value is
			// not found exactly, but one quite close will be.
			for(double min = maxDistance, max = maxMaxDistance;;) {
				upperMaxDistance = (min + max) / 2d;
				final BarrierForest forest = construct(modelsList,
						models.totalConformations, neighborhood, minDistance,
						upperMaxDistance, false);
				final int nLeaves = forest.getNumberOfLeaves();

				if(nLeaves == 1) {
					Debug.line("Neighbor threshold %f results in 1 leaf",
							upperMaxDistance);
					max = upperMaxDistance;

					if(max - min < minBinarySearchSpan) {
						break;
					}
				}
				else {
					Debug.line("Neighbor threshold %f results in %d leaves",
							upperMaxDistance, nLeaves);
					min = upperMaxDistance;
				}
			}

			Debug.line("Calculating data points for R plots");
			final int steps = 25;
			final double resolution = upperMaxDistance / steps;
			final double[] x = new double[steps + 1];
			final double[] yTrees = new double[steps + 1];
			final double[] yLeaves = new double[steps + 1];

			for(int i = 0; i <= steps; i++) {
				final double value = i * resolution;
				final BarrierForest forest = construct(modelsList,
						models.totalConformations, neighborhood, minDistance,
						value, false);

				x[i] = value;
				yTrees[i] = forest.getNumberOfTrees();
				yLeaves[i] = forest.getNumberOfLeaves();
			}

			Debug.line("Creating R plots");
			final String title = String.format("%s-%s-%f",
					moleculeFile.getName(), trajectoryFile.getName(),
					minDistance);
			R.open();
			R.dualPlot(x, yTrees, yLeaves, true, R.PlotType.BOTH, title,
					"maxDistance", "nTrees", "nLeaves",
					new File(".", title + "-maxDistance.pdf"));
			R.close();

			Print.line("The neighborhood threshold should probably be between "
					+ "approximately %f and %f", lowerMaxDistance,
					upperMaxDistance);
			Debug.line("Using %f as neighbor threshold", lowerMaxDistance);
			maxDistance = lowerMaxDistance;
		}

		return construct(modelsList, models.totalConformations,
				neighborhood, minDistance, maxDistance, true);
	}

	/**
	 * TODO: Remove this temp. method.
	 */
	//	private static void doSomethingTemp(List<Model> models)
	//	{
	//		/*Print.line("b diff a\tc diff a");
	//		Print.line("# a = (6-2-1-3), b = (7-2-1-3), c = (8-2-1-3)");
	//
	//		final int nModels = models.size();
	//
	//		for(int i = 0; i < nModels; i++) {
	//			final Model model = models.get(i);
	//			final double alpha = model.getAngle(2);
	//			final double beta = model.getAngle(3);
	//			final double gamma = model.getAngle(4);
	//
	//			final double betaMinusAlpha =
	//				Math.toDegrees(Util.angleDifference(beta, alpha));
	//			final double gammaMinusAlpha =
	//				Math.toDegrees(Util.angleDifference(gamma, alpha));
	//
	//			Print.line("%f\t%f", betaMinusAlpha, gammaMinusAlpha);
	//		}*/
	//
	//		final int nModels = models.size();
	//		final int angleId = 2;
	//
	//		Print.line("Angle%d\tFitness", angleId);
	//
	//		Collections.sort(models, new Comparator<Model>() {
	//			@Override
	//			public int compare(Model a, Model b)
	//			{
	//				return Double.compare(a.getAngle(angleId), b.getAngle(angleId));
	//			}
	//		});
	//
	//		for(int i = 0; i < nModels; i++) {
	//			final Model model = models.get(i);
	//			final double alpha = model.getAngle(angleId);
	//			final double fitness = model.evaluate();
	//
	//			Print.line("%f\t%f", Math.toDegrees(alpha), fitness);
	//		}
	//
	//		/*Print.line("Distance\tFitness");
	//
	//		final int nModels = models.size();		
	//		final int angleOffset = 2;
	//		final double[][] optimals = new double[][] {
	//				{ 60, 180, 300 },
	//				{ 60, 300, 180 },
	//				{ 180, 60, 300 },
	//				{ 180, 300, 60 },
	//				{ 300, 60, 180 },
	//				{ 300, 180, 60 },
	//		};
	//
	//		for(int i = 0; i < optimals.length; i++) {
	//			final double[] optimal = optimals[i];
	//			for(int j = 0; j < optimal.length; j++) {
	//				optimal[j] = Math.toRadians(optimal[j]);
	//			}
	//		}
	//
	//		for(int i = 0; i < nModels; i++) {
	//			final Model model = models.get(i);
	//			final double dist = distTemp(optimals, model, angleOffset);
	//
	//			Print.line("%f\t%f", Math.toDegrees(dist), model.evaluate());
	//		}*/
	//
	//		System.exit(0);
	//	}

	//	private static double distTemp(double[][] optimals, Model model,
	//			int angleOffset)
	//	{
	//		double minDist = Double.POSITIVE_INFINITY;
	//
	//		for(int i = 0; i < optimals.length; i++) {
	//			final double[] optimal = optimals[i];
	//			double dist = 0d;
	//
	//			for(int j = 0; j < optimal.length; j++) {
	//				dist += Util.angleDifference(optimal[j],
	//						model.getAngle(j + angleOffset));
	//			}
	//
	//			if(dist < minDist) {
	//				minDist = dist;
	//			}
	//		}
	//
	//		return minDist;
	//	}

	/**
	 * Returns a list of models created using a trajectory (a sequence of
	 * conformations), an energy file, and the original molecule (from which the
	 * conformations originate). The returned list of models is pruned
	 * on-the-fly so that models that are too similar are removed. The algorithm
	 * is as follows:
	 * <p>
	 * The first model, if any, of <code>models</code> is always accepted (that
	 * is, not removed). A model will be accepted if it has a distance of at
	 * least <code>minDistance</code> to the previously accepted model, and the
	 * second previously accepted model, and the fourth previously accepted
	 * model, etc. If a model <code>next</code> has a greater distance (it is
	 * too similar) to a previously accepted model <code>prev</code>,
	 * <code>prev</code> will be exchanged in favor of <code>next</code> if and
	 * only if <code>next.evaluate() &lt; prev.evaluate()</code>.
	 * 
	 * @param moleculeFile the molecule.
	 * @param trajectoryFile the trajectory.
	 * @param energyFile the energy file.
	 * @param neighborhood the neighborhood to use for pruning.
	 * @param minDistance the minimum distance to a previously included model to
	 *        include a model, for pruning, as per
	 *        {@link Neighborhood#distance(Model, Model)}.
	 * @return the list of models.
	 */
	private static Models createModels(File moleculeFile,
			File trajectoryFile, File energyFile, Neighborhood neighborhood,
			double minDistance)
	{
		final boolean performPruning = (minDistance != 0d);

		// Get molecule structure to use when calculating torsion angles of
		// conformations in trajectory.
		final OpenBabelData obData =
			OpenBabel.createObDataFromPdb(moleculeFile, false);
		final OBMol molecule = obData.getMolecule();
		final int[][] torsionMatrix = obData.getTorsions();

		Debug.line("Loading conformations from %s", trajectoryFile.getName());
		XtcReader xtcReader = null;
		try { xtcReader = new XtcReader(trajectoryFile); }
		catch(IOException e) { Main.errorExit(e, StatusCode.IO); }
		final int nAtoms = xtcReader.atoms();
		if(nAtoms != molecule.NumAtoms()) {
			Main.errorExit("Number of atoms per frame in trajectory does not "
					+ "match number of atoms in PDB. Check your input files.",
					StatusCode.IO);
		}
		final double[][] coordinates = xtcReader.createCoordinatesArray();

		Debug.line("Loading energy values from %s", energyFile.getName());
		EnergyExtractor energyExtractor = null;
		try { energyExtractor = new EnergyExtractor(energyFile); }
		catch(IOException e) { Main.errorExit(e, StatusCode.IO); }

		Debug.line("Creating models (pruning threshold %f)",
				minDistance);
		final List<Model> models = new ArrayList<Model>();
		final double[] firstEnergies = new double[3];
		int nConformations = 0;

		// For distance measures as if pruning on-the-fly was not performed.
		final boolean isDebug = Debug.isDebug();
		Model previousModel = null;
		double beforePruningDistSum = 0d;
		double beforePruningDistMax = Double.NEGATIVE_INFINITY;
		double beforePruningDistMin = Double.POSITIVE_INFINITY;

		// For status.
		final int conformationsMilestoneStep = 100000;
		int conformationsMilestone = conformationsMilestoneStep;
		System.err.print("Conformations processed: [0");

		try {
			if(xtcReader.hasNext()) {
				xtcReader.next(coordinates);

				final double fitness = energyExtractor.next();
				firstEnergies[0] = fitness; 

				final ImmutableModel firstModel = new ImmutableModel(
						nConformations++, fitness, coordinates, torsionMatrix);

				firstModel.attachCoordinates(coordinates);
				models.add(firstModel);
				previousModel = firstModel;
			}
			else {
				System.err.println("]");
				Debug.line("No conformations read");
				return new Models();
			}

			while(xtcReader.hasNext()) {
				xtcReader.next(coordinates);

				final double fitness = energyExtractor.next();
				if(nConformations < firstEnergies.length) {
					firstEnergies[nConformations] = fitness;
				}

				final ImmutableModel model = new ImmutableModel(nConformations,
						fitness, coordinates, torsionMatrix);

				// Distance measures as if on-the-fly pruning was not performed.
				if(isDebug) {
					final double avgDistBeforePruning =
						neighborhood.distance(previousModel, model);
					previousModel = model;
					beforePruningDistSum += avgDistBeforePruning;
					if(avgDistBeforePruning > beforePruningDistMax) {
						beforePruningDistMax = avgDistBeforePruning;
					}
					else if(avgDistBeforePruning < beforePruningDistMin) {
						beforePruningDistMin = avgDistBeforePruning;
					}
				}

				if(performPruning) {
					offer(model, coordinates, models, neighborhood,
							minDistance);
				}
				else {
					model.attachCoordinates(coordinates);
					models.add(model);
				}

				nConformations++;

				// For status.
				if(nConformations >= conformationsMilestone) {
					System.err.print("..." + nConformations);
					conformationsMilestone += conformationsMilestoneStep;
				}
			}

			// For status.
			System.err.println("..." + nConformations + "]");

			Debug.line("First three energy values: %f, %f, %f",
					firstEnergies[0], firstEnergies[1], firstEnergies[2]);

			if(energyExtractor.hasNext()) {
				Main.errorExit("Found less conformations than energy values. "
						+ "Check your input files.", StatusCode.XTC);
			}

			Debug.line("Read %d conformations and %d energy values",
					nConformations, nConformations);

			// Measures as if on-the-fly pruning had not been performed.
			calculateDistanceMeasures(nConformations, beforePruningDistSum,
					beforePruningDistMin, beforePruningDistMax);

			if(!performPruning) {
				return new Models(models, nConformations);
			}

			final int nRemain = models.size();
			final int nRemoved = nConformations - nRemain;
			Debug.line("On-the-fly pruning discarded %d models (%f%%), "
					+ "%d models remain", nRemoved,
					(nRemoved) / (double)nConformations * 100, nRemain);

			// Measures after on-the-fly pruning.
			calculateDistanceMeasures(models, neighborhood);

			final List<Model> pruned = prune(models, neighborhood, minDistance);

			// Measures after pruning.
			calculateDistanceMeasures(pruned, neighborhood);

			return new Models(pruned, nConformations);
		}
		catch(NoSuchElementException e) {
			Debug.line("First three energy values: %f, %f, %f",
					firstEnergies[0], firstEnergies[1], firstEnergies[2]);
			Main.errorExit("Found less energy values than conformations. "
					+ "Check your input files.", StatusCode.ENERGY);
		}
		catch(IOException e) {
			Main.errorExit(e, StatusCode.IO);
		}
		finally {
			try {
				energyExtractor.close();
				xtcReader.close();
			}
			catch(IOException e) {
				Main.errorExit(e, StatusCode.IO);
			}
		}

		return null;
	}

	/**
	 * Performs on-the-fly pruning.
	 * 
	 * @param model the model to accept (add to <code>acceptedModels</code>) or
	 *              discard.
	 * @param coordinates the coordinates of <code>model</code>.
	 * @param acceptedModels the previously accepted models.
	 * @param neighborhood the neighborhood to use.
	 * @param minDistance the minimum distance to previously accepted models to
	 *        accept <code>model</code>, as per
	 *        {@link Neighborhood#distance(Model, Model)}.
	 */
	private static void offer(ImmutableModel model, double[][] coordinates,
			List<Model> acceptedModels, Neighborhood neighborhood,
			double minDistance)
	{
		// TODO: nPreviousChecks could be set > 0, but maybe not necessary.
		final int nPreviousChecks = 0;
		final int lastAcceptedId = acceptedModels.size() - 1;
		final int limit = Math.max(0, lastAcceptedId - nPreviousChecks);
		int acceptedId = lastAcceptedId;
		boolean isAccepted = true;

		for(; acceptedId > limit; acceptedId--) {
			if(!isAccepted(model, coordinates, acceptedModels, acceptedId,
					neighborhood, minDistance)) {
				isAccepted = false;
				break;
			}
		}

		if(isAccepted) {
			// acceptedId = acceptedId, acceptedId - 1, acceptedId - 2,
			//              acceptedId - 4, acceptedId - 8, ... 
			for(int i = 1; acceptedId >= 0; acceptedId -= i, i *= 2) {  
				if(!isAccepted(model, coordinates, acceptedModels, acceptedId,
						neighborhood, minDistance)) {
					isAccepted = false;
					break;
				}
			}

			if(isAccepted) {
				model.attachCoordinates(coordinates);
				acceptedModels.add(model);
			}
		}
	}

	/**
	 * Computes and returns whether or not a model is accepted when compared to
	 * a previously accepted model.
	 * <p>
	 * Note that even if a model is not accepted it may be exchanged with the
	 * previously accepted model because of a lower energy value.
	 * 
	 * @param model the model to accept or discard.
	 * @param coordinates the coordinates of <code>model</code>.
	 * @param acceptedModels the previously accepted models.
	 * @param acceptedModelId id of the previously accepted model to compare to
	 *        <code>model</code>.
	 * @param neighborhood the neighborhood to use.
	 * @param minDistance the minimum distance to the previously accepted model
	 *        to accept <code>model</code>, as per
	 *        {@link Neighborhood#distance(Model, Model)}.
	 * @return <code>true</code> if and only if <code>model</code> is accepted. 
	 */
	private static boolean isAccepted(ImmutableModel model,
			double[][] coordinates, List<Model> acceptedModels,
			int acceptedModelId, Neighborhood neighborhood, double minDistance)
	{
		final Model acceptedModel = acceptedModels.get(acceptedModelId);
		final double distance = neighborhood.distance(acceptedModel, model);

		if(distance < minDistance) {
			if(model.evaluate() < acceptedModel.evaluate()) {
				model.attachCoordinates(coordinates);
				acceptedModels.set(acceptedModelId, model);
			}

			return false;
		}

		return true;
	}

	/**
	 * An implementation of the flooding algorithm for creating barrier trees.
	 * 
	 * @param models the models sorted by increasing fitness value.
	 * @param neighborMatrix neighbor information where the ids are based on the
	 *        ids of models in <code>models</code>.
	 * @return a list containing the barrier tree roots.
	 */
	private static List<Node> flooding(List<Model> models,
			int[][] neighborMatrix)
	{
		final int nModels = models.size();
		final Set<Node> knownBasins = new LinkedHashSet<Node>();
		final Node[] nodes = new Node[nModels];

		// This ensures that the ids of the created Node objects will start at
		// 0. Hence, even if this method is called more than once, the node
		// representing the global minimum will get id 0.
		NodeFactory.reset();

		for(int i = 0; i < nModels; i++) {
			final int[] neighborRow = neighborMatrix[i];
			final int nNeighbors = neighborRow.length;
			knownBasins.clear();

			for(int j = 0; j < nNeighbors; j++) {
				final int neighborId = neighborRow[j];
				final Node neighborBasin = nodes[neighborId];

				if(neighborBasin != null) {
					knownBasins.add(neighborBasin);
				}
			}

			final Model model = models.get(i);

			switch(knownBasins.size()) {
			case 0:
				// Model is a local minimum (a new basin).
				nodes[i] = NodeFactory.create(model);
				break;
			case 1:
				// Model is in the same basin as its known neighbor(s).
				final Node node = knownBasins.iterator().next();
				node.addAdditionalModel(model);
				nodes[i] = node;
				break;
			default:
				// Model is a saddle point between two or more basins.
				final Iterator<Node> iterator = knownBasins.iterator();
				Node firstBasin = iterator.next();

				do {
					final Node secondBasin = iterator.next();
					final Node barrierNode = NodeFactory.create(model,
							firstBasin, secondBasin);
					nodes[i] = barrierNode;
					Util.changeReferences(nodes, firstBasin, secondBasin,
							barrierNode);
					firstBasin = barrierNode;
				} while(iterator.hasNext());
				break;
			}
		}

		return Util.removeDuplicates(nodes);
	}

	private static double calculateDistanceMeasures(List<Model> models,
			Neighborhood neighborhood)
	{
		final int nModels = models.size();
		double sum = 0d;
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;

		for(int i = 1; i < nModels; i++) {
			final Model previous = models.get(i - 1);
			final Model current = models.get(i);
			final double distance = neighborhood.distance(previous, current);

			sum += distance;

			// TODO: This is wrong: 1) max is set, 2) max is set again, but min is not set to former max
			if(distance > max) {
				max = distance;
			}
			else if(distance < min) {
				min = distance;
			}
		}

		return calculateDistanceMeasures(nModels, sum, min, max);
	}

	private static double calculateDistanceMeasures(int nModels, double sum,
			double min, double max)
	{
		// Avoid insane measures in borderline cases.
		double avg = 0d;
		if(nModels < 2) {
			min = max = 0d;
		}
		else if(nModels == 2) {
			avg = min = max;
		}
		else {
			avg = sum / (nModels - 1);
		}

		Debug.line("Distance between consecutive pairs of models: "
				+ "min. %f, max. %f, avg. %f", min, max, avg);

		return avg;
	}

	private static BarrierForest construct(List<Model> models,
			int totalConformations, Neighborhood neighborhood,
			double minDistance, double maxDistance, boolean allowDebugPrints)
	{
		if(allowDebugPrints) Debug.line("Calculating neighbors (%d models, "
				+ "%f threshold)", models.size(), maxDistance);
		final int[][] neighborMatrix =
			neighborhood.calculateNeighbors(models, maxDistance,
					allowDebugPrints);

		if(allowDebugPrints) Debug.line("Executing the flooding algorithm");
		final List<Node> roots = flooding(models, neighborMatrix);

		return createForest(roots, totalConformations, minDistance,
				maxDistance, neighborhood, allowDebugPrints);
	}

	private static final class Models
	{
		private final List<Model> list;
		private final int totalConformations;

		private Models()
		{
			list = Collections.emptyList();
			totalConformations = 0;
		}

		private Models(List<Model> list, int totalConformations)
		{
			this.list = list;
			this.totalConformations = totalConformations;
		}
	}
}
