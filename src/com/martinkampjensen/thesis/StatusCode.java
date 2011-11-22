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
 * {@link StatusCode} contains status codes to be used with
 * {@link System#exit(int)}.
 */
public enum StatusCode
{
	/**
	 * Successful termination.
	 */
	SUCCESS(0),

	/**
	 * An error related to command line arguments.
	 */
	ARGUMENT(1),

	/**
	 * An error related to parsing.
	 */
	PARSING(2),

	/**
	 * An error related to I/O.
	 */
	IO(3),

	/**
	 * An error related to instantiation via reflection.
	 */
	REFLECTION(4),

	/**
	 * An error related to generation of conformers.
	 */
	CONFORMERS(5),

	/**
	 * An error related to minimization.
	 */
	MINIMIZATION(6),

	/**
	 * An error related to R.
	 */
	R(7),

	/**
	 * An error related to GROMACS.
	 */
	GROMACS(8),

	/**
	 * An error related to XTC trajectories.
	 */
	XTC(8),

	/**
	 * An error related to parsing a GROMACS energy file.
	 */
	ENERGY(9),

	/**
	 * An error related to serialization.
	 */
	SERIALIZATION(10);

	private final int _statusCode;

	private StatusCode(int statusCode)
	{
		_statusCode = statusCode;
	}

	/**
	 * Returns the status code for use with {@link System#exit(int)}.
	 * 
	 * @return the status code.
	 */
	public int getCode()
	{
		return _statusCode;
	}
}
