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

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.martinkampjensen.thesis.job.Job;

// TODO: Extend with "result" functionality. E.g. a barrier tree structure can be saved and then structuring/coloring can be performed later.
/**
 * TODO: Document {@link Instance}.
 */
public final class Instance
{
	private static Job _job;

	private Instance()
	{
	}

	/**
	 * Parses a Job XML file.
	 * <p>
	 * The method will shut down the JVM if an error occurs while parsing.
	 * 
	 * @param file the Job XML file.
	 */
	public static void parseJob(File file)
	{
		try {
			_job = Job.parse(file);
		}
		catch(SAXException e) {
			Main.errorExit(e, StatusCode.PARSING);
		}
		catch(JAXBException e) {
			Main.errorExit(e, StatusCode.PARSING);
		}

		// TODO: Check whether or not the input is valid/legal.
	}

	/**
	 * Returns the current job, if any.
	 * 
	 * @return the current job or <code>null</code>.
	 */
	public static Job job()
	{
		return _job;
	}
}
