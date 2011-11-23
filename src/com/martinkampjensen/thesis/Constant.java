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
import java.util.Locale;

import org.apache.commons.cli2.util.HelpFormatter;

/**
 * The {@link Constant} interface specifies constant values of general interest.
 */
public interface Constant
{
	/**
	 * The default locale for this instance of the JVM.
	 * 
	 * @see Locale#setDefault(Locale)
	 */
	Locale LOCALE = Locale.ENGLISH;

	/**
	 * The command name for running this application.
	 * 
	 * @see HelpFormatter#setShellCommand(String)
	 */
	String SHELL_COMMAND = "barriers";

	/**
	 * The file containing the XML Schema for job XML files. 
	 */
	File JOB_XML_SCHEMA = new File("jobs/job.xsd");

	/**
	 * The extension of PDB input files.
	 * <p>
	 * This is used when deciding whether to treat a input instance string as a
	 * PDB file or a Java class.  
	 * 
	 * @see <a href="http://en.wikipedia.org/wiki/Protein_Data_Bank_(file_format)">Protein Data Bank (file format)</a>
	 */
	String PDB_FILENAME_EXTENSION = ".pdb";

	/**
	 * The level of precision of <code>double</code> values, that is, two values
	 * <code>a</code> and <code>b</code> should be considered equal if and only
	 * if <code>Math.abs(a - b)</code> is less than or equal to this value.
	 */
	double DOUBLE_PRECISION = 1e-10;

	/**
	 * The mathematical constant pi.
	 */
	double PI = Math.PI;

	/**
	 * Half of the mathematical constant pi.
	 */
	double HALF_PI = PI / 2;

	/**
	 * Two times the mathematical constant pi.
	 */
	double TWO_PI = 2 * PI;
}
