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

import static com.martinkampjensen.thesis.Argument.ACTIONS;
import static com.martinkampjensen.thesis.Argument.ANALYZE;
import static com.martinkampjensen.thesis.Argument.ANALYZE_OUTPUT;
import static com.martinkampjensen.thesis.Argument.BARRIERS;
import static com.martinkampjensen.thesis.Argument.BARRIERS_ENERGY;
import static com.martinkampjensen.thesis.Argument.BARRIERS_NEIGHBOR;
import static com.martinkampjensen.thesis.Argument.BARRIERS_POSTSCRIPT;
import static com.martinkampjensen.thesis.Argument.BARRIERS_PRUNING;
import static com.martinkampjensen.thesis.Argument.BARRIERS_STRUCTURE;
import static com.martinkampjensen.thesis.Argument.BARRIERS_TOPOLOGY;
import static com.martinkampjensen.thesis.Argument.BARRIERS_TRAJECTORY;
import static com.martinkampjensen.thesis.Argument.CHECK;
import static com.martinkampjensen.thesis.Argument.CONNECT;
import static com.martinkampjensen.thesis.Argument.EVALUATE;
import static com.martinkampjensen.thesis.Argument.EXECUTE;
import static com.martinkampjensen.thesis.Argument.EXTRACT;
import static com.martinkampjensen.thesis.Argument.EXTRACT_CONFORMATION;
import static com.martinkampjensen.thesis.Argument.EXTRACT_TRAJECTORY;
import static com.martinkampjensen.thesis.Argument.MINIMIZE;
import static com.martinkampjensen.thesis.Argument.MINIMIZE_RANDOM;
import static com.martinkampjensen.thesis.Argument.OPTIONS;
import static com.martinkampjensen.thesis.Argument.PRINT;
import static com.martinkampjensen.thesis.Argument.SAMPLE;
import static com.martinkampjensen.thesis.Argument.SAMPLE_ANGLEIDS;
import static com.martinkampjensen.thesis.Argument.SAMPLE_RANDOM;
import static com.martinkampjensen.thesis.Argument.SEED;
import static com.martinkampjensen.thesis.Argument.VISUALIZE;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;

import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.Util;

/**
 * The {@link Main} class contains the only {@link #main(String[])} method in
 * this project, that is, start here!
 */
public final class Main
{
	private static final GroupBuilder GB = new GroupBuilder();
	private static final DefaultOptionBuilder OB = new DefaultOptionBuilder();
	private static final ArgumentBuilder AB = new ArgumentBuilder();
	private static Option _oSeed;
	private static Option _oAnalyze, _oAnalyzeOutput;
	private static Option _oBarriers, _oBarriersTopology;
	private static Option _oBarriersPostScript, _oBarriersStructure;
	private static Option _oBarriersTrajectory, _oBarriersEnergy;
	private static Option _oBarriersPruning, _oBarriersNeighbor;
	private static Option _oCheck;
	private static Option _oConnect;
	private static Option _oEvaluate;
	private static Option _oExecute;
	private static Option _oExtract, _oExtractConformation, _oExtractTrajectory;
	private static Option _oMinimize, _oMinimizeRandom;
	private static Option _oPrint;
	private static Option _oSample, _oSampleAngleIds, _oSampleRandom;
	private static Option _oVisualize;

	private Main()
	{
	}

	/**
	 * The one and only Java main method in this project.
	 * <p>
	 * After parsing command line arguments and setting options, control will be
	 * delegated to the {@link Application} class.
	 * 
	 * @param args an array containing command line arguments.
	 */
	public static void main(String[] args)
	{
		Locale.setDefault(Constant.LOCALE);

		Debug.line("Application started");

		final Group optionGroup = createOptionGroup();
		final CommandLine cmdLine = parse(args, optionGroup);

		if(!query(cmdLine)) {
			printHelp(optionGroup);
		}

		Debug.line("Application ended");
	}

	/**
	 * Shut downs the JVM.
	 * 
	 * @param statusCode the exit status.
	 * @see System#exit(int)
	 */
	public static void exit(StatusCode statusCode)
	{
		System.exit(statusCode.getCode());
	}

	/**
	 * Prints a message to {@link System#err} and shut downs the JVM.
	 * 
	 * @param message the message to print.
	 * @param statusCode the exit status.
	 * @see System#exit(int)
	 */
	public static void errorExit(String message, StatusCode statusCode)
	{
		System.err.println(message);
		System.exit(statusCode.getCode());
	}

	/**
	 * Prints a stack trace of an {@link Exception} to {@link System#err} and
	 * shut downs the JVM.
	 * 
	 * @param exception the exception containing the stack trace.
	 * @param statusCode the exit status.
	 * @see System#exit(int)
	 */
	public static void errorExit(Exception exception, StatusCode statusCode)
	{
		exception.printStackTrace(System.err);
		System.exit(statusCode.getCode());
	}

	private static Group createOptionGroup()
	{
		// A local GroupBuilder is needed here because the global one would have
		// its state changed when the individual options are created.
		final GroupBuilder gb = new GroupBuilder();

		final Group options =
			gb
			.withName(OPTIONS.groupName())
			.withOption(createSeed())
			.create();

		final Group actions =
			gb
			.withName(ACTIONS.groupName())
			.withOption(createAnalyze())
			.withOption(createBarriers())
			.withOption(createCheck())
			.withOption(createConnect())
			.withOption(createEvaluate())
			.withOption(createExecute())
			.withOption(createExtract())
			.withOption(createMinimize())
			.withOption(createPrint())
			.withOption(createSample())
			.withOption(createVisualize())
			.withMinimum(0)
			.withMaximum(1)
			.create();

		final Group group =
			gb
			.withOption(options)
			.withOption(actions)
			.create();

		return group;
	}

	private static Option createSeed()
	{
		return _oSeed =
			OB
			.withLongName(SEED.longName())
			.withDescription(SEED.description())
			.withArgument(AB
					.withName(SEED.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();
	}

	private static Option createAnalyze()
	{
		_oAnalyzeOutput =
			OB
			.withLongName(ANALYZE_OUTPUT.longName())
			.withDescription(ANALYZE_OUTPUT.description())
			.withArgument(AB
					.withName(ANALYZE_OUTPUT.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();

		final Group gAnalyze =
			GB
			.withName(ANALYZE.groupName())
			.withOption(_oAnalyzeOutput)
			.create();

		return _oAnalyze =
			OB
			.withLongName(ANALYZE.longName())
			.withDescription(ANALYZE.description())
			.withArgument(AB
					.withName(ANALYZE.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.withChildren(gAnalyze)
					.create();
	}

	private static Option createBarriers()
	{
		_oBarriersTrajectory =
			OB
			.withLongName(BARRIERS_TRAJECTORY.longName())
			.withDescription(BARRIERS_TRAJECTORY.description())
			.withRequired(true)
			.withArgument(AB
					.withName(BARRIERS_TRAJECTORY.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();

		_oBarriersEnergy =
			OB
			.withLongName(BARRIERS_ENERGY.longName())
			.withDescription(BARRIERS_ENERGY.description())
			.withRequired(true)
			.withArgument(AB
					.withName(BARRIERS_ENERGY.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();

		_oBarriersPruning =
			OB
			.withLongName(BARRIERS_PRUNING.longName())
			.withDescription(BARRIERS_PRUNING.description())
			.withRequired(true)
			.withArgument(AB
					.withName(BARRIERS_PRUNING.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();

		_oBarriersNeighbor =
			OB
			.withLongName(BARRIERS_NEIGHBOR.longName())
			.withDescription(BARRIERS_NEIGHBOR.description())
			.withRequired(true)
			.withArgument(AB
					.withName(BARRIERS_NEIGHBOR.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();

		final Group gBarriersTrajectory =
			GB
			.withName(BARRIERS_TRAJECTORY.groupName())
			.withOption(_oBarriersTrajectory)
			.withOption(_oBarriersEnergy)
			.withOption(_oBarriersPruning)
			.withOption(_oBarriersNeighbor)
			.create();

		_oBarriersTopology =
			OB
			.withLongName(BARRIERS_TOPOLOGY.longName())
			.withDescription(BARRIERS_TOPOLOGY.description())
			.withArgument(AB
					.withName(BARRIERS_TOPOLOGY.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();

		_oBarriersPostScript =
			OB
			.withLongName(BARRIERS_POSTSCRIPT.longName())
			.withDescription(BARRIERS_POSTSCRIPT.description())
			.withArgument(AB
					.withName(BARRIERS_POSTSCRIPT.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();

		_oBarriersStructure =
			OB
			.withLongName(BARRIERS_STRUCTURE.longName())
			.withDescription(BARRIERS_STRUCTURE.description())
			.withArgument(AB
					.withName(BARRIERS_STRUCTURE.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();

		final Group gBarriers =
			GB
			.withName(BARRIERS.groupName())
			.withOption(_oBarriersTopology)
			.withOption(_oBarriersPostScript)
			.withOption(_oBarriersStructure)
			.withOption(gBarriersTrajectory)
			.create();

		return _oBarriers =
			OB
			.withLongName(BARRIERS.longName())
			.withDescription(BARRIERS.description())
			.withArgument(AB
					.withName(BARRIERS.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.withChildren(gBarriers)
					.create();
	}

	private static Option createCheck()
	{
		return _oCheck =
			OB
			.withLongName(CHECK.longName())
			.withDescription(CHECK.description())
			.withArgument(AB
					.withName(CHECK.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();
	}

	private static Option createConnect()
	{
		return _oConnect =
			OB
			.withLongName(CONNECT.longName())
			.withDescription(CONNECT.description())
			.withArgument(AB
					.withName(CONNECT.argumentName())
					.withMinimum(1)
					.withMaximum(2) // TODO: This approach means that a PDB and a TOP file can be specified, but it is maybe not pretty. It works for now.
					.create())
					.create();
	}

	private static Option createEvaluate()
	{
		return _oEvaluate =
			OB
			.withLongName(EVALUATE.longName())
			.withDescription(EVALUATE.description())
			.withArgument(AB
					.withName(EVALUATE.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();
	}

	private static Option createExecute()
	{
		return _oExecute =
			OB
			.withLongName(EXECUTE.longName())
			.withDescription(EXECUTE.description())
			.withArgument(AB
					.withName(EXECUTE.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();
	}

	private static Option createExtract()
	{
		_oExtractConformation =
			AB
			.withName(EXTRACT_CONFORMATION.longName())
			.withDescription(EXTRACT_CONFORMATION.description())
			.withMinimum(1)
			.withSubsequentSeparator(',')
			.create();

		_oExtractTrajectory =
			OB
			.withLongName(EXTRACT_TRAJECTORY.longName())
			.withDescription(EXTRACT_TRAJECTORY.description())
			.withArgument(AB
					.withName(EXTRACT_TRAJECTORY.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();

		final Group gExtract =
			GB
			.withName(EXTRACT.groupName())
			.withOption(_oExtractConformation)
			.withOption(_oExtractTrajectory)
			.create();

		return _oExtract =
			OB
			.withLongName(EXTRACT.longName())
			.withDescription(EXTRACT.description())
			.withArgument(AB
					.withName(EXTRACT.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.withChildren(gExtract)
					.create();
	}

	private static Option createMinimize()
	{
		_oMinimizeRandom =
			OB
			.withLongName(MINIMIZE_RANDOM.longName())
			.withDescription(MINIMIZE_RANDOM.description())
			.create();

		final Group gMinimize =
			GB
			.withName(MINIMIZE.groupName())
			.withOption(_oMinimizeRandom)
			.create();

		return _oMinimize =
			OB
			.withLongName(MINIMIZE.longName())
			.withDescription(MINIMIZE.description())
			.withArgument(AB
					.withName(MINIMIZE.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.withChildren(gMinimize)
					.create();
	}

	private static Option createPrint()
	{
		return _oPrint =
			OB
			.withLongName(PRINT.longName())
			.withDescription(PRINT.description())
			.withArgument(AB
					.withName(PRINT.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();
	}

	private static Option createSample()
	{
		_oSampleAngleIds =
			AB
			.withName(SAMPLE_ANGLEIDS.longName())
			.withDescription(SAMPLE_ANGLEIDS.description())
			.withMinimum(1)
			.withSubsequentSeparator(',')
			.create();

		_oSampleRandom =
			OB
			.withLongName(SAMPLE_RANDOM.longName())
			.withDescription(SAMPLE_RANDOM.description())
			.create();

		final Group gSample =
			GB
			.withName(SAMPLE.groupName())
			.withOption(_oSampleAngleIds)
			.withOption(_oSampleRandom)
			.create();

		return _oSample =
			OB
			.withLongName(SAMPLE.longName())
			.withDescription(SAMPLE.description())
			.withArgument(AB
					.withName(SAMPLE.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.withChildren(gSample)
					.create();
	}

	private static Option createVisualize()
	{
		return _oVisualize =
			OB
			.withLongName(VISUALIZE.longName())
			.withDescription(VISUALIZE.description())
			.withArgument(AB
					.withName(VISUALIZE.argumentName())
					.withMinimum(1)
					.withMaximum(1)
					.create())
					.create();
	}

	private static CommandLine parse(String[] args, Group group)
	{
		final Parser parser = new Parser();
		parser.setGroup(group);
		CommandLine cmdLine = null;

		try {
			cmdLine = parser.parse(args);
		}
		catch(OptionException e) {
			System.out.println(e.getMessage());
			exit(StatusCode.ARGUMENT);
		}

		return cmdLine;
	}

	private static boolean query(CommandLine cmdLine)
	{
		// Options.
		if(cmdLine.hasOption(_oSeed)) {
			seed(cmdLine);
		}

		// Actions.
		if(cmdLine.hasOption(_oAnalyze)) {
			analyze(cmdLine);
		}
		else if(cmdLine.hasOption(_oBarriers)) {
			barriers(cmdLine);
		}
		else if(cmdLine.hasOption(_oCheck)) {
			check(cmdLine);
		}
		else if(cmdLine.hasOption(_oConnect)) {
			connect(cmdLine);
		}
		else if(cmdLine.hasOption(_oEvaluate)) {
			evaluate(cmdLine);
		}
		else if(cmdLine.hasOption(_oExecute)) {
			execute(cmdLine);
		}
		else if(cmdLine.hasOption(_oExtract)) {
			extract(cmdLine);
		}
		else if(cmdLine.hasOption(_oMinimize)) {
			minimize(cmdLine);
		}
		else if(cmdLine.hasOption(_oPrint)) {
			print(cmdLine);
		}
		else if(cmdLine.hasOption(_oSample)) {
			sample(cmdLine);
		}
		else if(cmdLine.hasOption(_oVisualize)) {
			visualize(cmdLine);
		}
		else {
			return false;
		}

		return true;
	}

	private static void printHelp(Group group)
	{
		final HelpFormatter hf = new HelpFormatter();

		hf.setGroup(group);
		hf.setShellCommand(Constant.SHELL_COMMAND);

		hf.print();
	}

	private static void seed(CommandLine cmdLine)
	{
		final long seed = parseLong(cmdLine, _oSeed);

		Application.seed(seed);
	}

	private static void analyze(CommandLine cmdLine)
	{
		final String fileOrClassName = (String)cmdLine.getValue(_oAnalyze);
		final Model model = Util.instantiateModel(fileOrClassName);
		final String output = (String)cmdLine.getValue(_oAnalyzeOutput);
		final File file = (output == null ? null : new File(output));

		Application.analyze(model, file);
	}

	private static void barriers(CommandLine cmdLine)
	{
		final String fileOrClassName = (String)cmdLine.getValue(_oBarriers);
		final String postScript =
			(String)cmdLine.getValue(_oBarriersPostScript);
		final String structure =
			(String)cmdLine.getValue(_oBarriersStructure);
		final File moleculeFile = new File(fileOrClassName);
		final File postScriptFile =
			(postScript == null ? null : new File(postScript));
		final File structureFile =
			(structure == null ? null : new File(structure));

		if(cmdLine.hasOption(_oBarriersTrajectory)) {
			final String trajectory =
				(String)cmdLine.getValue(_oBarriersTrajectory);
			final String energy = (String)cmdLine.getValue(_oBarriersEnergy);
			final File trajectoryFile = new File(trajectory);
			final File energyFile = new File(energy);
			final double pruning = parseDouble(cmdLine, _oBarriersPruning);
			final double neighbor = parseDouble(cmdLine, _oBarriersNeighbor);
			Application.barriers(moleculeFile, trajectoryFile, energyFile,
					postScriptFile, structureFile, pruning, neighbor);
		}
		else {
			final String topology =
				(String)cmdLine.getValue(_oBarriersTopology);
			final File topologyFile =
				(topology == null ? null : new File(topology));
			final Model model =
				Util.instantiateModel(fileOrClassName, topologyFile);
			Application.barriers(moleculeFile, model, postScriptFile,
					structureFile);
		}
	}

	private static void check(CommandLine cmdLine)
	{
		final String fileName = (String)cmdLine.getValue(_oCheck);
		final File file = new File(fileName);

		Application.check(file);
	}

	private static void connect(CommandLine cmdLine)
	{
		// TODO: This approach means that a PDB and a TOP file can be specified, but it is maybe not pretty. It works for now.
		final List<?> values = cmdLine.getValues(_oConnect);
		final String fileOrClassName = (String)values.get(0);
		final File topologyFile =
			(values.size() == 2 ? new File((String)values.get(1)) : null);
		final Model model =
			Util.instantiateModel(fileOrClassName, topologyFile);

		Application.connect(model);
	}

	private static void evaluate(CommandLine cmdLine)
	{
		final String fileOrClassName = (String)cmdLine.getValue(_oEvaluate);
		final Model model = Util.instantiateModel(fileOrClassName);

		Application.evaluate(model);
	}

	private static void execute(CommandLine cmdLine)
	{
		final String fileName = (String)cmdLine.getValue(_oExecute);
		final File file = new File(fileName);

		Application.execute(file);
	}

	private static void extract(CommandLine cmdLine)
	{
		final String fileName = (String)cmdLine.getValue(_oExtract);
		final int[] conformationIds =
			parseIntArray(cmdLine, _oExtractConformation);
		final String trajectory = (String)cmdLine.getValue(_oExtractTrajectory);

		final File trajectoryFile = (trajectory == null ?
				null : new File(trajectory));

		if(trajectoryFile != null) {
			if(conformationIds.length != 2) {
				errorExit("Specify exactly two ids", StatusCode.ARGUMENT);
			}

			Application.extract(new File(fileName), conformationIds[0],
					conformationIds[1], trajectoryFile);
		}
		else {
			Application.extract(new File(fileName), conformationIds);
		}
	}

	private static void minimize(CommandLine cmdLine)
	{
		final String fileOrClassName = (String)cmdLine.getValue(_oMinimize);
		final Model model = Util.instantiateModel(fileOrClassName);
		final boolean random = cmdLine.hasOption(_oMinimizeRandom);

		Application.minimize(model, random);
	}

	private static void print(CommandLine cmdLine)
	{
		final String fileOrClassName = (String)cmdLine.getValue(_oPrint);
		final Model model = Util.instantiateModel(fileOrClassName);

		Application.print(model);
	}

	private static void sample(CommandLine cmdLine)
	{
		final String fileOrClassName = (String)cmdLine.getValue(_oSample);
		final Model model = Util.instantiateModel(fileOrClassName);
		final int[] angleIds = parseIntArray(cmdLine, _oSampleAngleIds);
		final boolean random = cmdLine.hasOption(_oSampleRandom);

		Application.sample(model, angleIds, random);
	}

	private static void visualize(CommandLine cmdLine)
	{
		final String fileOrClassName = (String)cmdLine.getValue(_oVisualize);
		final Model model = Util.instantiateModel(fileOrClassName);

		Application.visualize(model);
	}

	private static long parseLong(CommandLine cmdLine, Option option)
	{
		long value = 0L;

		try {
			value = Long.parseLong((String)cmdLine.getValue(option));
		}
		catch(NumberFormatException e) {
			errorExit(e, StatusCode.ARGUMENT);
		}

		return value;
	}

	private static double parseDouble(CommandLine cmdLine, Option option)
	{
		double value = 0d;

		try {
			value = Double.parseDouble((String)cmdLine.getValue(option));
		}
		catch(NumberFormatException e) {
			errorExit(e, StatusCode.ARGUMENT);
		}

		return value;
	}

	private static int[] parseIntArray(CommandLine cmdLine, Option option)
	{
		final List<?> list = cmdLine.getValues(option);
		final int values[] = new int[list.size()];

		for(int i = 0; i < values.length; i++) {
			values[i] = parseInt(list.get(i));
		}

		return values;
	}

	private static int parseInt(Object stringObj)
	{
		int value = 0;

		try {
			value = Integer.parseInt((String)stringObj);
		}
		catch(NumberFormatException e) {
			errorExit(e, StatusCode.ARGUMENT);
		}

		return value;
	}
}
