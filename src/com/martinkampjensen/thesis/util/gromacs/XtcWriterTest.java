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

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class defines tests for the {@link XtcWriter} class.
 */
public class XtcWriterTest
{
	@Test
	public void test() throws IOException
	{
		final File trajectory = File.createTempFile("xtcwritertest", null);
		final int nAtoms = 10;
		final int coordsPerAtom = 3;
		final double[][] expected = new double[nAtoms][coordsPerAtom];
		XtcWriter xtcWriter = null;
		XtcReader xtcReader = null;
		double[][] actual = null;

		for(int i = 0; i < nAtoms; i++) {
			for(int j = 0; j < coordsPerAtom; j++) {
				expected[i][j] = i * j + 0.01;
			}
		}

		try {
			xtcWriter = new XtcWriter(trajectory, nAtoms);
			xtcWriter.write(expected);
		}
		finally {
			xtcWriter.close();
		}

		Assert.assertEquals(nAtoms, xtcWriter.atoms());

		try {
			xtcReader = new XtcReader(trajectory);
			actual = xtcReader.createCoordinatesArray();
			xtcReader.next(actual);
		}
		finally {
			xtcReader.close();
		}

		Assert.assertEquals(nAtoms, xtcReader.atoms());

		for(int i = 0; i < nAtoms; i++) {
			Assert.assertArrayEquals(expected[i], actual[i], 0.001d);
		}

		trajectory.deleteOnExit();
	}
}
