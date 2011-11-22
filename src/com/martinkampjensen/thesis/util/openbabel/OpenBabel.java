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

package com.martinkampjensen.thesis.util.openbabel;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

import jbcl.calc.structural.properties.TorsionalAngle;

import org.openbabel.OBAtom;
import org.openbabel.OBBond;
import org.openbabel.OBConversion;
import org.openbabel.OBFFConstraints;
import org.openbabel.OBMessageHandler;
import org.openbabel.OBResidue;
import org.openbabel.OBResidueAtomIter;
import org.openbabel.obMessageLevel;
import org.openbabel.openbabel_java;

import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.Util;

/**
 * TODO: Document {@link OpenBabel}.
 */
public final class OpenBabel
{
	public static final String FORMAT_FHZ = "fh";
	public static final String FORMAT_GROMOS = "gr96";
	public static final String FORMAT_PDB = "pdb";
	public static final String FORMAT_XTC = "xtc";
	private static final int BACKBONE_TORSIONS_PER_RESIDUE = 3;
	private static final String TYPE_BACKBONE_CH3 = " CH3";
	private static final String TYPE_BACKBONE_CA = " CA ";
	private static final String TYPE_BACKBONE_C = " C ";
	private static final String TYPE_BACKBONE_N = " N ";
	private static final OBConversion _pdbConverter;
	private static boolean _libraryIsLoaded = false;

	static
	{
		loadLibrary();
		_pdbConverter = new OBConversion();
		_pdbConverter.SetInAndOutFormats(FORMAT_PDB, FORMAT_PDB);
	}

	private OpenBabel()
	{
	}

	public static void loadLibrary()
	{
		if(!_libraryIsLoaded) {
			if(Util.isWindows()) {
				// TODO: Stupid hardcoded path in .dll workaround.
				System.load("c:\\users\\mkj\\desktop\\openbabel-trunk\\windows"
						+ "-vc2008\\build\\bin\\release\\openbabel-2.dll");
			}

			// Required because using classes and methods in openbabel.jar will
			// not automatically load the native file for interfacing with Open 
			// Babel.
			System.loadLibrary("openbabel_java-r4514");

			setErrorLevel();
		}
	}

	public static OBMol fromPdb(String pdb)
	{
		final OBMol molecule = new OBMol();
		_pdbConverter.ReadString(molecule, pdb);
		return molecule;
	}

	public static String toPdb(OBMol molecule)
	{
		final String pdb = _pdbConverter.WriteString(molecule);
		return pdb.substring(0, pdb.indexOf("CONECT")) + "TER\nENDMDL\n";
	}

	public static OpenBabelData createObDataFromPdb(File moleculeFile)
	{
		return createObDataFromPdb(moleculeFile, null, false);
	}

	public static OpenBabelData createObDataFromPdb(File moleculeFile,
			File topologyFile)
	{
		return createObDataFromPdb(moleculeFile, topologyFile, false);
	}

	public static OpenBabelData createObDataFromPdb(File moleculeFile,
			boolean findBackbonesCalculateDependencies)
	{
		return createObDataFromPdb(moleculeFile, null,
				findBackbonesCalculateDependencies);
	}

	public static OpenBabelData createObDataFromPdb(File moleculeFile,
			File topologyFile, boolean findBackbonesCalculateDependencies)
	{
		loadLibrary();

		final OBMol molecule = loadPdb(moleculeFile);
		final ImmutableZMatrix zMatrix = calculateZMatrix(molecule);
		final OBFFConstraints constraints = findConstraints(zMatrix);
		final int[][] torsions = findTorsions(molecule, zMatrix);
		final int[] backbones = (findBackbonesCalculateDependencies == true ?
				findBackbones(molecule) : null);
		final int[] dependencies = (findBackbonesCalculateDependencies == true ?
				calculateDependencies(torsions, backbones) : null);

		// Debug.
		/*final OBMol copyMolecule = new OBMol(molecule);
		final OBForceField forceField = OBForceField.FindForceField(
				OpenBabelData.DEFAULT_FORCE_FIELD.getId());
		forceField.Setup(copyMolecule);
		Debug.line("Original energy: " + forceField.Energy());
		forceField.SteepestDescent(OpenBabelMinimizer.DEFAULT_STEPS,
				OpenBabelMinimizer.DEFAULT_CONVERGENCE);
		Debug.line("Minimum unconstrained energy: " + forceField.Energy());
		forceField.Setup(copyMolecule, constraints);
		forceField.SteepestDescent(OpenBabelMinimizer.DEFAULT_STEPS,
				OpenBabelMinimizer.DEFAULT_CONVERGENCE);
		Debug.line("Minimum constrained energy: " + forceField.Energy());
		forceField.delete();
		copyMolecule.delete();*/

		return new OpenBabelData(moleculeFile, topologyFile, molecule, zMatrix,
				constraints, torsions, backbones, dependencies);
	}

	private static void setErrorLevel()
	{
		// Avoid warnings such as the following:
		// *** Open Babel Warning  in parseAtomRecord
		// WARNING: Problems reading a PDB file
		// Problems reading a HETATM or ATOM record.
		// According to the PDB specification,
		// columns 77-78 should contain the element symbol of an atom.
		// but OpenBabel found '  ' (atom 22)
		final OBMessageHandler messageHandler = new OBMessageHandler();
		messageHandler.SetOutputLevel(obMessageLevel.obError);
		openbabel_java.setObErrorLog(messageHandler);
	}

	private static OBMol loadPdb(File file)
	{
		Debug.line("Loading %s", file.getName());

		final OBConversion conversion = new OBConversion();
		conversion.SetInFormat(FORMAT_PDB);
		final OBMol molecule = new OBMol();
		conversion.ReadFile(molecule, file.getAbsolutePath());

		return molecule;
	}

	private static ImmutableZMatrix calculateZMatrix(OBMol molecule)
	{
		Debug.line("Calculating Z-matrix");

		// Convert the molecule to a Fenske-Hall Z-Matrix that is easy to parse.
		final OBConversion conversion = new OBConversion();
		conversion.SetOutFormat(FORMAT_FHZ);
		final String fhz = conversion.WriteString(molecule);

		// Use the atom ids from the Fenske-Hall Z-Matrix, but use the values
		// (bond lengths, bond angles, torsion angles) directly from the
		// molecule to avoid precision errors.
		final Scanner scanner = new Scanner(fhz);
		final int atoms = scanner.nextInt();
		final int size = atoms + ImmutableZMatrix.MINIMUM_ID;
		final double[][] matrix =
			new double[size][ImmutableZMatrix.ATOM_DATA_LENGTH];
		final String[] types = new String[size];

		// First atom.
		final String fstAtomType = scanner.next();
		final int fstAtomId = scanner.nextInt();
		matrix[fstAtomId][ImmutableZMatrix.BOND_LENGTH_ATOM_ID_INDEX] =
			fstAtomId;
		types[fstAtomId] = fstAtomType;

		// Second atom.
		final int sndAtomId = fstAtomId + 1;
		types[sndAtomId] = scanner.next();
		final int sndBondLengthAtomId = scanner.nextInt();
		final double sndBondLengthFallback = scanner.nextDouble();

		final OBAtom sndAtom = molecule.GetAtom(sndAtomId);
		final OBAtom sndBondLengthAtom = molecule.GetAtom(sndBondLengthAtomId);

		final OBBond sndBond = molecule.GetBond(sndAtom, sndBondLengthAtom);
		final double sndBondLength = (sndBond != null ?
				sndBond.GetLength() : sndBondLengthFallback);

		final double[] sndRow = matrix[sndAtomId];
		sndRow[ImmutableZMatrix.BOND_LENGTH_ATOM_ID_INDEX] =
			sndBondLengthAtomId;
		sndRow[ImmutableZMatrix.BOND_LENGTH_INDEX] = sndBondLength;

		// Third atom.
		final int trdAtomId = sndAtomId + 1;
		types[trdAtomId] = scanner.next();
		final int trdBondLengthAtomId = scanner.nextInt();
		final double trdBondLengthFallback = scanner.nextDouble();
		final int trdBondAngleAtomId = scanner.nextInt();
		scanner.nextDouble(); // Bond angle.

		final OBAtom trdAtom = molecule.GetAtom(trdAtomId);
		final OBAtom trdBondLengthAtom = molecule.GetAtom(trdBondLengthAtomId);
		final OBAtom trdBondAngleAtom = molecule.GetAtom(trdBondAngleAtomId);

		final OBBond trdBond = molecule.GetBond(trdAtom, trdBondLengthAtom);
		final double trdBondLength = (trdBond != null ?
				trdBond.GetLength() : trdBondLengthFallback); 
		final double trdBondAngle =
			molecule.GetAngle(trdAtom, trdBondLengthAtom, trdBondAngleAtom);

		final double[] trdRow = matrix[trdAtomId];
		trdRow[ImmutableZMatrix.BOND_LENGTH_ATOM_ID_INDEX] =
			trdBondLengthAtomId;
		trdRow[ImmutableZMatrix.BOND_LENGTH_INDEX] = trdBondLength;
		trdRow[ImmutableZMatrix.BOND_ANGLE_ATOM_ID_INDEX] = trdBondAngleAtomId;
		trdRow[ImmutableZMatrix.BOND_ANGLE_INDEX] =
			Math.toRadians(trdBondAngle);

		// Remaining atoms.
		for(int atomId = trdAtomId + 1; atomId <= atoms; atomId++) {
			types[atomId] = scanner.next();
			final int bondLengthAtomId = scanner.nextInt();
			final double bondLengthFallback = scanner.nextDouble();
			final int bondAngleAtomId = scanner.nextInt();
			scanner.nextDouble(); // Bond angle.
			final int torsionAngleAtomId = scanner.nextInt();
			scanner.nextDouble(); // Torsion angle.

			final OBAtom atom = molecule.GetAtom(atomId);
			final OBAtom bondLengthAtom = molecule.GetAtom(bondLengthAtomId);
			final OBAtom bondAngleAtom = molecule.GetAtom(bondAngleAtomId);
			final OBAtom torsionAngleAtom =
				molecule.GetAtom(torsionAngleAtomId);

			final OBBond bond = molecule.GetBond(atom, bondLengthAtom);
			final double bondLength = (bond != null ?
					bond.GetLength() : bondLengthFallback);
			final double bondAngle =
				molecule.GetAngle(atom, bondLengthAtom, bondAngleAtom);
			final double torsionAngle = molecule.GetTorsion(atom,
					bondLengthAtom, bondAngleAtom, torsionAngleAtom);

			final double[] row = matrix[atomId];
			row[ImmutableZMatrix.BOND_LENGTH_ATOM_ID_INDEX] = bondLengthAtomId;
			row[ImmutableZMatrix.BOND_LENGTH_INDEX] = bondLength;
			row[ImmutableZMatrix.BOND_ANGLE_ATOM_ID_INDEX] = bondAngleAtomId;
			row[ImmutableZMatrix.BOND_ANGLE_INDEX] = Math.toRadians(bondAngle);
			row[ImmutableZMatrix.TORSION_ANGLE_ATOM_ID_INDEX] =
				torsionAngleAtomId;
			row[ImmutableZMatrix.TORSION_ANGLE_INDEX] =
				TorsionalAngle.convertToStartFromZero(
						Math.toRadians(torsionAngle));
		}

		return new ImmutableZMatrix(matrix, types);
	}

	private static OBFFConstraints findConstraints(ImmutableZMatrix zMatrix)
	{
		Debug.line("Finding constraints");

		final OBFFConstraints constraints = new OBFFConstraints();
		constraints.SetFactor(50000); // TODO: Find suitable value.

		// Second atom, first atom does not need constraints.
		final int sndAtomId = ImmutableZMatrix.MINIMUM_LENGTH_ID;
		final int sndBondLengthAtomId = zMatrix.bondLengthAtomId(sndAtomId);
		final double sndBondLength = zMatrix.bondLength(sndAtomId);
		constraints.AddDistanceConstraint(sndAtomId, sndBondLengthAtomId,
				sndBondLength);

		// Third atom.
		final int trdAtomId = ImmutableZMatrix.MINIMUM_ANGLE_ID;
		final int trdBondLengthAtomId = zMatrix.bondLengthAtomId(trdAtomId);
		final double trdBondLength = zMatrix.bondLength(trdAtomId);
		final int trdBondAngleAtomId = zMatrix.bondAngleAtomId(trdAtomId);
		final double trdBondAngle = zMatrix.bondAngle(trdAtomId);
		constraints.AddDistanceConstraint(trdAtomId, trdBondLengthAtomId,
				trdBondLength);
		constraints.AddAngleConstraint(trdAtomId, trdBondLengthAtomId,
				trdBondAngleAtomId, Math.toDegrees(trdBondAngle));

		// Constrain the atoms defined with torsion angles.
		final int atoms = zMatrix.atoms();
		for(int i = ImmutableZMatrix.MINIMUM_TORSION_ID; i <= atoms; i++) {
			final int atomId = i;
			final int bondLengthAtomId = zMatrix.bondLengthAtomId(i);
			final double bondLength = zMatrix.bondLength(i);
			final int bondAngleAtomId = zMatrix.bondAngleAtomId(i);
			final double bondAngle = zMatrix.bondAngle(i);

			constraints.AddDistanceConstraint(atomId, bondLengthAtomId,
					bondLength);
			constraints.AddAngleConstraint(atomId, bondLengthAtomId,
					bondAngleAtomId, Math.toDegrees(bondAngle));
		}

		return constraints;
	}

	private static int[][] findTorsions(OBMol molecule,
			ImmutableZMatrix zMatrix)
	{
		Debug.line("Finding torsion angles");

		final int atoms = zMatrix.atoms();
		final int[][] torsions =
			new int[atoms + 1][OpenBabelData.TORSION_LENGTH];

		for(int i = ImmutableZMatrix.MINIMUM_TORSION_ID; i <= atoms; i++) {
			final int bondLengthAtomId = zMatrix.bondLengthAtomId(i);
			final int bondAngleAtomId = zMatrix.bondAngleAtomId(i);
			final int torsionAngleAtomId = zMatrix.torsionAngleAtomId(i);

			final int[] torsion = torsions[i];
			torsion[OpenBabelData.TORSION_A] = i;
			torsion[OpenBabelData.TORSION_B] = bondLengthAtomId;
			torsion[OpenBabelData.TORSION_C] = bondAngleAtomId;
			torsion[OpenBabelData.TORSION_D] = torsionAngleAtomId;
		}

		return torsions;
	}

	private static int[] findBackbones(OBMol molecule)
	{
		// 0 < r < molecule.NumResidues() - 1
		// 
		// Omega: CA(r-1) C(r-1) N(r) CA(r) [CA := CH3 iff r = 1]
		// Phi:   C(r-1) N(r) CA(r) C(r)
		// Psi:   N(r) CA(r) C(r) N(r+1)
		// 
		// Omega: prevCA prevC thisN thisCA
		// Phi:   prevC thisN thisCA thisC
		// Psi:   thisN thisCA thisC nextN

		Debug.line("Finding backbone torsion angles");

		final int secondResidueId = 1;
		final int secondLastResidueId = (int)molecule.NumResidues() - 2;
		final int nTriplets = secondLastResidueId;
		final int nBackbones = BACKBONE_TORSIONS_PER_RESIDUE * nTriplets;
		final int[] backbones = new int[nBackbones];

		for(int i = secondResidueId; i <= secondLastResidueId; i++) {
			final OBResidue prevResidue = molecule.GetResidue(i - 1);
			final OBResidue thisResidue = molecule.GetResidue(i);
			final OBResidue nextResidue = molecule.GetResidue(i + 1);

			OBAtom prevCA = null, prevC = null, thisN = null, thisCA = null;
			OBAtom thisC = null, nextN = null;

			// Previous residue.
			for(OBAtom atom : new OBResidueAtomIter(prevResidue)) {
				if(i == 1 && isType(prevResidue, atom, TYPE_BACKBONE_CH3)) {
					prevCA = atom;
				}
				else if(isType(prevResidue, atom, TYPE_BACKBONE_CA)) {
					prevCA = atom;
				}
				else if(isType(prevResidue, atom, TYPE_BACKBONE_C)) {
					prevC = atom;
				}
			}

			// Current residue.
			for(OBAtom atom : new OBResidueAtomIter(thisResidue)) {
				if(isType(thisResidue, atom, TYPE_BACKBONE_N)) {
					thisN = atom;
				}
				else if(isType(thisResidue, atom, TYPE_BACKBONE_CA)) {
					thisCA = atom;
				}
				else if(isType(thisResidue, atom, TYPE_BACKBONE_C)) {
					thisC = atom;
				}
			}

			// Next residue.
			for(OBAtom atom : new OBResidueAtomIter(nextResidue)) {
				if(isType(nextResidue, atom, TYPE_BACKBONE_N)) {
					nextN = atom;
					break;
				}
			}

			if(prevCA == null || prevC == null || thisN == null
					|| thisCA == null || thisC == null || nextN == null) {
				throw new IllegalStateException(
						"Backbone torsion angle(s) could not be found. Please "
						+ "check the definitions of the input molecule.");
			}

			final int fstI = (i - 1) * BACKBONE_TORSIONS_PER_RESIDUE;
			final int sndI = fstI + 1;
			final int trdI = sndI + 1;

			// Backbone torsion angles.
			backbones[fstI] = (int)thisCA.GetIdx();
			backbones[sndI] = (int)thisC.GetIdx();
			backbones[trdI] = (int)nextN.GetIdx();
		}

		return backbones;
	}

	private static boolean isType(OBResidue residue, OBAtom atom, String type)
	{
		final String atomString = residue.GetAtomID(atom);
		return atomString.contains(type);
	}

	private static int[] calculateDependencies(int[][] torsions,
			int[] backbones)
	{
		Debug.line("Calculating torsion angle dependencies");

		final int size = torsions.length;
		final int[] dependencies = new int[size];
		final int[][] sortedTorsions = torsions.clone();
		Arrays.sort(sortedTorsions, ImmutableZMatrix.MINIMUM_TORSION_ID, size,
				new TorsionComparator(backbones));

		for(int i = ImmutableZMatrix.MINIMUM_TORSION_ID; i < size;) {
			final int[] master = sortedTorsions[i];

			for(i++; i < size; i++) {
				final int[] slave = sortedTorsions[i];

				if(slave[OpenBabelData.TORSION_B]
				         != master[OpenBabelData.TORSION_B] ||
				         slave[OpenBabelData.TORSION_C]
				               != master[OpenBabelData.TORSION_C] ||
				               slave[OpenBabelData.TORSION_D]
				                     != master[OpenBabelData.TORSION_D]) {
					// TODO: Documentation.
					break;
				}

				final int slaveId = slave[OpenBabelData.TORSION_A];
				final int masterId = master[OpenBabelData.TORSION_A];
				dependencies[slaveId] = masterId;
			}
		}

		return dependencies;
	}

	private static final class TorsionComparator implements Comparator<int[]>
	{
		private final int[] _backbones;

		private TorsionComparator(int[] backbones)
		{
			_backbones = backbones;
		}

		@Override
		public int compare(int[] fst, int[] snd)
		{
			final int fstBondLengthAtomId = fst[OpenBabelData.TORSION_B];
			final int sndBondLengthAtomId = snd[OpenBabelData.TORSION_B];

			if(fstBondLengthAtomId > sndBondLengthAtomId) {
				return 1;
			}
			else if(fstBondLengthAtomId < sndBondLengthAtomId) {
				return -1;
			}

			final int fstBondAngleAtomId = fst[OpenBabelData.TORSION_C];
			final int sndBondAngleAtomId = snd[OpenBabelData.TORSION_C];

			if(fstBondAngleAtomId > sndBondAngleAtomId) {
				return 1;
			}
			else if(fstBondAngleAtomId < sndBondAngleAtomId) {
				return -1;
			}

			final int fstTorsionAngleAtomId = fst[OpenBabelData.TORSION_D];
			final int sndTorsionAngleAtomId = snd[OpenBabelData.TORSION_D];

			if(fstTorsionAngleAtomId > sndTorsionAngleAtomId) {
				return 1;
			}
			else if(fstTorsionAngleAtomId < sndTorsionAngleAtomId) {
				return -1;
			}

			final int fstAtomId = fst[OpenBabelData.TORSION_A];
			final int sndAtomId = snd[OpenBabelData.TORSION_A];

			// TODO: Maybe change this very naive implementation.
			for(int i = 0; i < _backbones.length; i++) {
				final int backbone = _backbones[i];

				if(fstAtomId == backbone) {
					return -1;
				}
				else if(sndAtomId == backbone) {
					return 1;
				}
			}

			return 0;
		}
	}
}
