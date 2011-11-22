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

import org.junit.Test;

/**
 * This class defines tests for the {@link XtcReader} class.
 */
public class XtcReaderTest
{
	private static final File DOUBLE_PRECISION_TRAJECTORY_FILE =
		new File(".", "test/AcAANMe-10000-300_d.xtc");
	private static final File DOUBLE_PRECISION_TRAJECTORY_RESULTS_FILE =
		new File(".", "test/AcAANMe-10000-300_d.xtc.txt");
	private static final File SINGLE_PRECISION_TRAJECTORY_FILE =
		new File(".", "test/AcAANMe-10000-300_s.xtc");
	private static final File SINGLE_PRECISION_TRAJECTORY_RESULTS_FILE =
		new File(".", "test/AcAANMe-10000-300_s.xtc.txt");

	@Test
	public void test() throws IOException
	{
		testHelper(DOUBLE_PRECISION_TRAJECTORY_RESULTS_FILE,
				DOUBLE_PRECISION_TRAJECTORY_FILE);
		testHelper(SINGLE_PRECISION_TRAJECTORY_RESULTS_FILE,
				SINGLE_PRECISION_TRAJECTORY_FILE);
	}

	private static void testHelper(File resultsFile, File trajectoryFile)
	throws FileNotFoundException, IOException
	{
		BufferedReader reader = null;
		XtcReader xtcReader = null;

		try {
			reader = new BufferedReader(new FileReader(resultsFile));
			xtcReader = new XtcReader(trajectoryFile);
			String line;
			int lineNo = 1;
			int nConformations = 0;
			int atomNo = -1;
			double[][] frame = null;

			assertEquals(Integer.parseInt(reader.readLine()),
					xtcReader.atoms());

			while((line = reader.readLine()) != null) {
				lineNo++;

				if(line.equals("")) {
					atomNo = -1;
					continue;
				}

				atomNo++;

				if(atomNo == 0) {
					if(!xtcReader.hasNext()) {
						throw new AssertionError("XtcTrajectory did not return "
								+ "enough frames");
					}

					nConformations++;
					frame = xtcReader.next();
				}

				final String[] doubleStrs = line.split(" ");
				final double[] actual = frame[atomNo];

				for(int i = 0; i < 3; i++) {
					final double expected = Double.parseDouble(doubleStrs[i]);

					if(expected != actual[i]) {
						throw new AssertionError(String.format("Line number "
								+ "%d, coordinate number %d: expected:<%f> but "
								+ "was:<%f>", lineNo, i + 1, expected,
								actual[i]));
					}
				}

				assertEquals(nConformations, xtcReader.conformations());
			}

			if(xtcReader.hasNext()) {
				throw new AssertionError("XtcTrajectory returned too many "
						+ "frames");
			}
		}
		finally {
			xtcReader.close();
			reader.close();
		}
	}
}
