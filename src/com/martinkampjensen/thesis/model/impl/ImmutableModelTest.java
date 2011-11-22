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

package com.martinkampjensen.thesis.model.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.junit.Test;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.gromacs.XtcReader;
import com.martinkampjensen.thesis.util.openbabel.OBMol;
import com.martinkampjensen.thesis.util.openbabel.OpenBabel;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;

/**
 * This class defines tests for the {@link ImmutableModel} class.
 */
public class ImmutableModelTest
{
	/**
	 * The expected precision. Note that due to e.g. conversion between degrees
	 * and radians, the precision cannot be very high.
	 */
	private static final double PRECISION = 0.001;

	private static final File MOLECULE_FILE =
		new File(".", "test/AcAANMe.pdb");
	private static final File DOUBLE_PRECISION_TRAJECTORY_FILE =
		new File(".", "test/AcAANMe-10000-300_d.xtc");
	private static final File SINGLE_PRECISION_TRAJECTORY_FILE =
		new File(".", "test/AcAANMe-10000-300_s.xtc");

	@Test
	public void test() throws IOException
	{
		testHelper(MOLECULE_FILE, DOUBLE_PRECISION_TRAJECTORY_FILE);
		testHelper(MOLECULE_FILE, SINGLE_PRECISION_TRAJECTORY_FILE);
	}

	public void testHelper(File moleculeFile, File trajectoryFile)
	throws IOException
	{
		Locale.setDefault(Constant.LOCALE);

		final OpenBabelData obData =
			OpenBabel.createObDataFromPdb(moleculeFile, true);
		final OBMol molecule = obData.getMolecule();
		final int[][] torsionMatrix = obData.getTorsions();

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

		final int nTorsions = nAtoms - 3;
		int nConformations = 0;
		double fitness = 0d;

		try {
			while(xtcReader.hasNext()) {
				xtcReader.next(coordinates);

				for(int i = 0; i < nAtoms; i++) {
					final double[] xyz = coordinates[i];
					molecule.GetAtom(i + 1).SetVector(xyz[0], xyz[1], xyz[2]);
				}
				final Model obModel = new ImmutableModel(nConformations,
						fitness, molecule, torsionMatrix);

				final Model model = new ImmutableModel(nConformations, fitness,
						coordinates, torsionMatrix);

				assertEquals(nConformations, obModel.getId());
				assertEquals(nConformations, model.getId());

				assertEquals(fitness, obModel.evaluate(), 0d);
				assertEquals(fitness, model.evaluate(), 0d);

				for(int i = 0; i < nTorsions; i++) {
					assertEquals(obModel.getAngle(i), model.getAngle(i),
							PRECISION);
				}

				nConformations++;
				fitness += 42d;				
			}
		}
		finally {
			xtcReader.close();
		}
	}
}
