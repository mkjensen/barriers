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

package com.martinkampjensen.thesis.util.gromacs;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * This class defines tests for the {@link EnergyExtractor} class.
 */
public class EnergyExtractorTest
{
	private static final File DOUBLE_PRECISION_ENERGY_FILE =
		new File(".", "test/AcAANMe-10000-300_d.edr");
	private static final File DOUBLE_PRECISION_ENERGY_RESULTS_FILE =
		new File(".", "test/AcAANMe-10000-300_d.edr.txt");
	private static final File SINGLE_PRECISION_ENERGY_FILE =
		new File(".", "test/AcAANMe-10000-300_s.edr");
	private static final File SINGLE_PRECISION_ENERGY_RESULTS_FILE =
		new File(".", "test/AcAANMe-10000-300_s.edr.txt");

	@Test
	public void testNext() throws IOException
	{
		testNextHelper(DOUBLE_PRECISION_ENERGY_RESULTS_FILE,
				DOUBLE_PRECISION_ENERGY_FILE);
		testNextHelper(SINGLE_PRECISION_ENERGY_RESULTS_FILE,
				SINGLE_PRECISION_ENERGY_FILE);
	}

	private static void testNextHelper(File resultsFile, File energyFile)
	throws FileNotFoundException, IOException
	{
		BufferedReader reader = null;
		EnergyExtractor extractor = null;

		try {
			reader = new BufferedReader(new FileReader(resultsFile));
			extractor = new EnergyExtractor(energyFile);

			try {
				String line;
				int lineNo = 0;
				int nValues = 0;

				while((line = reader.readLine()) != null) {
					lineNo++;

					if(line.equals("")) {
						continue;
					}

					nValues++;
					final double expected = Double.parseDouble(line);
					final double actual = extractor.next();

					if(expected != actual) {
						throw new AssertionError(String.format("Line number "
								+ "%d: expected:<%f> but was:<%f>", lineNo,
								expected, actual));
					}

					assertEquals(nValues, extractor.values());
				}
			}
			catch(NoSuchElementException e) {
				throw new AssertionError("EnergyExtractor did not return "
						+ "enough values");
			}

			if(extractor.hasNext()) {
				throw new AssertionError("EnergyExtractor returned too many "
						+ "values");
			}
		}
		finally {
			extractor.close();
			reader.close();
		}
	}
}
