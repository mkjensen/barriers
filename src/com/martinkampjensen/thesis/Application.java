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

package com.martinkampjensen.thesis;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.martinkampjensen.thesis.analysis.LandscapeAnalyzer;
import com.martinkampjensen.thesis.barriers.AllMinimaPairsConstructor;
import com.martinkampjensen.thesis.barriers.Constructor;
import com.martinkampjensen.thesis.barriers.TrajectoryConstructor;
import com.martinkampjensen.thesis.barriers.coloring.Colorer;
import com.martinkampjensen.thesis.barriers.coloring.RmsdAngleDifferenceColorer;
import com.martinkampjensen.thesis.barriers.coloring.TrajectoryPositionColorer;
import com.martinkampjensen.thesis.barriers.structuring.WeightStructurer;
import com.martinkampjensen.thesis.connection.BeaconConnector;
import com.martinkampjensen.thesis.connection.BiasedRandomConnector;
import com.martinkampjensen.thesis.connection.Connector;
import com.martinkampjensen.thesis.connection.DirectConnector;
import com.martinkampjensen.thesis.job.Action;
import com.martinkampjensen.thesis.job.Analysis;
import com.martinkampjensen.thesis.job.Job;
import com.martinkampjensen.thesis.job.Minimization;
import com.martinkampjensen.thesis.minimization.GromacsMinimizer;
import com.martinkampjensen.thesis.minimization.Minimizer;
import com.martinkampjensen.thesis.minimization.NelderMeadMinimizer;
import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;
import com.martinkampjensen.thesis.model.impl.ImmutableModel;
import com.martinkampjensen.thesis.model.impl.OpenBabelZMatrix;
import com.martinkampjensen.thesis.sampling.RandomSampler;
import com.martinkampjensen.thesis.sampling.Sampler;
import com.martinkampjensen.thesis.sampling.StepSampler;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.FileHandler;
import com.martinkampjensen.thesis.util.Print;
import com.martinkampjensen.thesis.util.Random;
import com.martinkampjensen.thesis.util.Serialize;
import com.martinkampjensen.thesis.util.Serialize.BarrierForestStructure;
import com.martinkampjensen.thesis.util.Util;
import com.martinkampjensen.thesis.util.gromacs.XtcWriter;
import com.martinkampjensen.thesis.util.openbabel.OBMol;
import com.martinkampjensen.thesis.util.openbabel.OpenBabel;
import com.martinkampjensen.thesis.visualization.opengl.Visualizer;
import com.martinkampjensen.thesis.visualization.postscript.Generator;

/**
 * The {@link Application} class contains logic to initialize and call the
 * actual functionality of this project.
 */
public final class Application
{
	private Application()
	{
	}

	public static void seed(long seed)
	{
		Random.setSeed(seed);
	}

	public static void analyze(Model model, File output)
	{
		final LandscapeAnalyzer analyzer = new LandscapeAnalyzer();

		if(output == null) {
			output = new File("results/analysis.pdf");
		}

		analyzer.analyze(model, new NelderMeadMinimizer(), output);
	}

	public static void barriers(File moleculeFile, Model model,
			File postScriptFile, File structureFile)
	{
		Debug.line("Minimizing");
		final List<Model> minima = new GromacsMinimizer().minimize(model, 6); // TODO: Choose nMinima

		// Read minima from disk to ensure identical minima between different
		// runs. This should of course be handled in a decent way.
//		final List<Model> minima = new ArrayList<Model>(200);
//		
//		for(int i = 0, n = 50; i < n; i++) {
//			final File pdbFile = new File("minima-by-ob/" + i + ".pdb");
//			final Model m =
//				Util.instantiateModel(pdbFile.getAbsolutePath(), null);
//			minima.add(m);
//		}
		
		final Constructor constructor = new AllMinimaPairsConstructor();
		final BarrierForest forest =
			constructor.construct(minima, new BeaconConnector());

		barriers(moleculeFile, forest, new RmsdAngleDifferenceColorer(),
				postScriptFile, structureFile);
	}

	public static void barriers(File moleculeFile, File trajectoryFile,
			File energyFile, File postScriptFile, File structureFile,
			double minDistance, double maxDistance)
	{
		final Constructor constructor = new TrajectoryConstructor();
		final BarrierForest forest = constructor.construct(moleculeFile,
				trajectoryFile, energyFile, minDistance, maxDistance);

		barriers(moleculeFile, forest, new TrajectoryPositionColorer(),
				postScriptFile, structureFile);
	}

	public static void check(File file)
	{
		Instance.parseJob(file);		
		System.out.print(Instance.job());
	}

	public static void connect(Model model)
	{
		final List<Model> minima =
			new GromacsMinimizer().minimize(model, 10);
		final Model from = minima.get(0);
		final Model to = minima.get(1);
		Debug.line("Connecting...");

		Connector connector = new DirectConnector();
		Model barrier = connector.connect(from, to);
		Debug.line("(Direct) Energies: %f -> %f <- %f",
				from.evaluate(), barrier.evaluate(), to.evaluate());

		connector = new BiasedRandomConnector();
		barrier = connector.connect(from, to);
		Debug.line("(Biased) Energies: %f -> %f <- %f",
				from.evaluate(), barrier.evaluate(), to.evaluate());

		connector = new BeaconConnector();
		barrier = connector.connect(from, to);
		Debug.line("(Beacon) Energies: %f -> %f <- %f",
				from.evaluate(), barrier.evaluate(), to.evaluate());
	}

	public static void evaluate(Model model)
	{
		Print.line("Energy: %f", model.evaluate());
	}

	public static void execute(File file)
	{
		Instance.parseJob(file);		
		final Job job = Instance.job();

		Debug.line("Executing job \"%s\"", job.name());
		Debug.line("Job description \"%s\"", job.description());
		Debug.line("Job input \"%s\"", job.input().location());

		// Options.
		Random.setSeed(job.options().seed());

		// Input.
		final Model model = Util.instantiateModel(job.input().location());

		// Action.
		final Action action = job.action();

		switch(action) {
		case ANALYZE:
			final Analysis analysis = job.options().analysis();
			final LandscapeAnalyzer analyzer = new LandscapeAnalyzer();
			analyzer.analyze(model, job.options().minimization().minimizer(),
					new File(analysis.output()), analysis.configs(),
					analysis.deltas(), analysis.samples(), analysis.delta());
			break;
		case EVALUATE:
			evaluate(model);
			break;
		case MINIMIZE:
			final Minimization minimization = job.options().minimization();
			final Minimizer minimizer = minimization.minimizer();
			final int nMinima = minimization.minima(); 
			minimize(model, minimizer, nMinima);
			break;
		case PRINT:
			print(model);
			break;
		default:
			Main.errorExit("Job XML: Action not implemented.",
					StatusCode.ARGUMENT);
			break;
		}
	}

	public static void extract(File file, int[] conformationIds)
	{
		Debug.line("Reconstructing original molecule and barrier forest");
		final BarrierForestStructure bfs = Serialize.toBarrierForest(file);
		final OBMol molecule = OpenBabel.fromPdb(bfs.getPdb());
		final BarrierForest forest = bfs.getForest();

		final int nConformationIds = conformationIds.length;
		for(int i = 0; i < nConformationIds; i++) {
			final int conformationId = conformationIds[i];

			Debug.line("Searching for conformation %d", conformationId);
			final Node node = forest.find(conformationId);
			if(node == null) {
				System.err.println("Error: Conformation " + conformationId
						+ " was not found.\n");
				continue;
			}

			Debug.line("Printing conformation in PDB format to stdout");
			final Model model = node.getModel();

			if(model instanceof ImmutableModel) {
				final ImmutableModel im = (ImmutableModel)model;
				Print.line(im.toPdb(molecule));
			}
			else if(model instanceof OpenBabelZMatrix) {
				final OpenBabelZMatrix obzm = (OpenBabelZMatrix)model;
				Print.line(obzm.toPdb(molecule));
			}
		}
	}

	// TODO: Doesn't work for OpenBabelZMatrix, only for ImmutableModel.
	public static void extract(File file, int fromId, int toId,
			File trajectoryFile)
	{
		Debug.line("Reconstructing original molecule and barrier forest");
		final BarrierForestStructure bfs = Serialize.toBarrierForest(file);

		if(bfs.getType() != Serialize.BarrierForestType.IMMUTABLEMODEL) {
			System.err.println("Not supported.");
			return;
		}

		final OBMol molecule = OpenBabel.fromPdb(bfs.getPdb());
		final BarrierForest forest = bfs.getForest();

		Debug.line("Finding path between nodes %d and %d", fromId, toId);
		final List<Model> models = forest.findConnectingModels(fromId, toId);
		final int nModels = models.size();

		Debug.line("Writing trajectory to \"%s\"", trajectoryFile.getName());
		final int nAtoms = (int)molecule.NumAtoms();
		XtcWriter xtcWriter = null;
		try { xtcWriter = new XtcWriter(trajectoryFile, nAtoms); }
		catch(IOException e) { Main.errorExit(e, StatusCode.IO); }

		try {
			for(int i = 0; i < nModels; i++) {
				final ImmutableModel imModel = (ImmutableModel)models.get(i);
				final double[][] coordinates = imModel.getCoordinates();
				xtcWriter.write(coordinates);
			}
		}
		finally {
			try {
				xtcWriter.close();
			}
			catch(IOException e) {
				Main.errorExit(e, StatusCode.IO);
			}
		}

		Debug.line("For fitting, use e.g. trjconv -f %s -o fitted.xtc "
				+ "-s original.pdb -fit progressive", trajectoryFile.getName());
	}

	public static void minimize(Model model, boolean random)
	{
		// TODO: Remove --random or actually use it.
		if(random) {
			Main.errorExit("--random is not implemented.", StatusCode.ARGUMENT);
		}

		minimize(model, new NelderMeadMinimizer(), 3);
	}

	public static void minimize(Model model, Minimizer minimizer, int nMinima)
	{
		Debug.line("Minimizing");
		final List<Model> minima = minimizer.minimize(model, nMinima);

		Print.line("Original energy: %f", model.evaluate());
		for(int i = 0; i < nMinima; i++) {
			Print.line("  Minima energy: %f", minima.get(i).evaluate());
		}
	}

	public static void print(Model model)
	{
		Debug.line("Printing model");

		Print.line(model.toString());
	}

	public static void sample(Model model, int[] angleIds, boolean random)
	{
		final Sampler sampler =
			(random ? new RandomSampler() : new StepSampler());
		Print.line(sampler.sampleToString(model, angleIds));
	}

	public static void visualize(Model model)
	{
		final Visualizer visualizer = new Visualizer(model);
		visualizer.start();
	}

	private static void barriers(File moleculeFile, BarrierForest forest,
			Colorer colorer, File postScriptFile, File structureFile)
	{
		// To print out stats.
		forest.getTotalConnectionValue();

		if(postScriptFile != null) {
			Debug.line("Writing PostScript to \"%s\"",
					postScriptFile.getName());
			final String postScript = Generator.barrierForest(forest,
					new WeightStructurer(), colorer);
			FileHandler.write(postScript, postScriptFile);
		}

		if(structureFile != null) {
			Debug.line("Writing structure to \"%s\"", structureFile.getName());
			Serialize.fromBarrierForest(structureFile, moleculeFile, forest);
		}
	}
}
