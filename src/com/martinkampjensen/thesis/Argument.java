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

/**
 * {@link Argument} contains strings used to define command line arguments.
 */
public enum Argument
{
	// Actions.

	/**
	 * Group name for actions.
	 */
	ACTIONS("Actions"),

	/**
	 * Strings for the "analyze" option.
	 */
	ANALYZE("Analysis", "analyze", "analyze instance", "file|class"),

	/**
	 * Strings for the "output" argument of the "analyze" option.
	 */
	ANALYZE_OUTPUT("output", "write output to file", "file"),

	/**
	 * Strings for the "barriers" option.
	 */
	BARRIERS("Barrier forest generation", "barriers",
			"construct a barrier forest using instance", "file|class"),

	/**
	 * Strings for the "trajectory" argument of the "barriers" option.
	 */
	BARRIERS_TOPOLOGY("topology", "topology for molecule", "file"),

	/**
	 * Strings for the "postscript" argument of the "barriers" option.
	 */
	BARRIERS_POSTSCRIPT("postscript", "write PostScript to file", "file"),

	/**
	 * Strings for the "structure" argument of the "barriers" option.
	 */
	BARRIERS_STRUCTURE("structure", "write structure to file", "file"),

	/**
	 * Strings for the "trajectory" argument of the "barriers" option.
	 */
	BARRIERS_TRAJECTORY("Using trajectory", "trajectory",
			"trajectory containing conformations", "file"),

	/**
	 * Strings for the "energy" argument of the "barriers" option.
	 */
	BARRIERS_ENERGY("energy", "energy file for trajectory", "file"),

	/**
	 * Strings for the "pruning" argument of the "barriers" option.
	 */
	BARRIERS_PRUNING("pruning", "pruning threshold (minDistance)", "value"),

	/**
	 * Strings for the "neighbor" argument of the "barriers" option.
	 */
	BARRIERS_NEIGHBOR("neighbor", "neighbor threshold (maxDistance)", "value"),

	/**
	 * Strings for the "check" option.
	 */
	CHECK("check", "check job", "file"),

	/**
	 * Strings for the "connect" option.
	 */
	CONNECT("connect", "connect two minima of instance", "file|class"),

	/**
	 * Strings for the "evaluate" option.
	 */
	EVALUATE("evaluate", "evaluate instance", "file|class"),

	/**
	 * Strings for the "execute" option.
	 */
	EXECUTE("execute", "execute job", "file"),

	/**
	 * Strings for the "extract" option.
	 */
	EXTRACT("Barrier forest processing", "extract", "extract data from forest",
			"file"),

	/**
	 * Strings for the "conformation" argument of the "extract" option.
	 */
	EXTRACT_CONFORMATION("id", "id of conformation(s) to extract"),

	/**
	 * Strings for the "trajectory" argument of the "extract" option.
	 */
	EXTRACT_TRAJECTORY("trajectory", "create trajectory between conformations",
			"file"),

	/**
	 * Strings for the "minimize" option.
	 */
	MINIMIZE("Minimization", "minimize", "minimize energy value of instance",
			"file|class"),

	/**
	 * Strings for the "random" argument of the "minimize" option.
	 */
	MINIMIZE_RANDOM("random", "minimize using a random start configuration"),

	/**
	 * Strings for the "print" option.
	 */
	PRINT("print", "print instance details", "file|class"),

	/**
	 * Strings for the "sample" option.
	 */
	SAMPLE("Sampling", "sample", "perform sampling using instance",
	"file|class"),

	/**
	 * Strings for the "angleId" argument of the "sample" option.
	 */
	SAMPLE_ANGLEIDS("angleId", "id of angle(s) used in sampling"),

	/**
	 * Strings for the "random" argument of the "sample" option.
	 */
	SAMPLE_RANDOM("random", "sample randomly"),

	/**
	 * Strings for the "visualize" option.
	 */
	VISUALIZE("visualize", "visualize instance", "file|class"),

	// Options.

	/**
	 * Group name for options.
	 */
	OPTIONS("Options"),

	/**
	 * Strings for the "seed" option.
	 */
	SEED("seed", "set the seed of the random generator", "value");

	private final String _groupName;
	private final String _longName;
	private final String _description;
	private final String _argumentName;

	private Argument(String groupName)
	{
		this(groupName, null, null, null);
	}

	private Argument(String longName, String description)
	{
		this(null, longName, description, null);
	}

	private Argument(String longName, String description, String argumentName)
	{
		this(null, longName, description, argumentName);
	}

	private Argument(String groupName, String longName, String description,
			String argumentName)
	{
		_groupName = groupName;
		_longName = longName;
		_description = description;
		_argumentName = argumentName;
	}

	/**
	 * Returns the group name of this argument.
	 * 
	 * @return the group name.
	 */
	public String groupName()
	{
		return _groupName;
	}

	/**
	 * Returns the long name of this argument.
	 * 
	 * @return the long name.
	 */
	public String longName()
	{
		return _longName;
	}

	/**
	 * Returns the description of this argument.
	 * 
	 * @return the description.
	 */
	public String description()
	{
		return _description;
	}

	/**
	 * Returns the argument name of this argument.
	 * 
	 * @return the argument name.
	 */
	public String argumentName()
	{
		return _argumentName;
	}
}
