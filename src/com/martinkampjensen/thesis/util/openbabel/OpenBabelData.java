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
import java.util.ArrayList;
import java.util.List;

import org.openbabel.OBAtom;
import org.openbabel.OBConformerSearch;
import org.openbabel.OBConversion;
import org.openbabel.OBConversion.Option_type;
import org.openbabel.OBFFConstraints;
import org.openbabel.OBForceField;

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Debug;

/**
 * TODO: Document {@link OpenBabelData}.
 */
public final class OpenBabelData
{
	/**
	 * The default force field.
	 */
	public static final ForceField DEFAULT_FORCE_FIELD =
		ForceField.GHEMICAL;

	/**
	 * The default optimization algorithm to use when minimizing.
	 */
	public static final OptimizationAlgorithm DEFAULT_MINIMIZATION_ALGORITHM =
		OptimizationAlgorithm.STEEPEST_DESCENT;

	/**
	 * The default number of steps to perform when minimizing.
	 */
	public static final int DEFAULT_MINIMIZATION_STEPS = 1000;

	/**
	 * The default energy convergence criteria when minimizing.
	 */
	public static final double DEFAULT_MINIMIZATION_CONVERGENCE = 1e-6;

	/**
	 * The default number of children to create when performing searching.
	 */
	public static final int DEFAULT_CONFORMERS_NUMBER_OF_CHILDREN = 5;

	/**
	 * The default mutability when performing searching.
	 */
	public static final int DEFAULT_CONFORMERS_MUTABILITY = 5;

	/**
	 * The default convergence when performing searching.
	 */
	public static final int DEFAULT_CONFORMERS_CONVERGENCE = 25;

	// TODO: Better field names.
	public static final int TORSION_A = 0;
	public static final int TORSION_B = 1;
	public static final int TORSION_C = 2;
	public static final int TORSION_D = 3;
	public static final int TORSION_LENGTH = TORSION_D + 1;
	private final File _moleculeFile;
	private final File _topologyFile;
	private final OBMol _molecule;
	private final ImmutableZMatrix _zMatrix;
	private final OBFFConstraints _constraints;
	private final int[][] _torsions;
	private final int[] _backbones;
	private final int[] _dependencies;
	private OBForceField _forceField;
	private OBConversion _pdbConversion;
	private OBConversion _gromosConversion;

	OpenBabelData(File moleculeFile, File topologyFile, OBMol molecule,
			ImmutableZMatrix zMatrix, OBFFConstraints constraints,
			int[][] torsions, int[] backbones, int[] dependencies)
	{
		_moleculeFile = moleculeFile;
		_topologyFile = topologyFile;
		_molecule = molecule;
		_zMatrix = zMatrix;
		_constraints = constraints;
		_backbones = backbones;
		_torsions = torsions;
		_dependencies = dependencies;
		_pdbConversion = new OBConversion();
		_gromosConversion = new OBConversion();

		setForceField(DEFAULT_FORCE_FIELD);

		_pdbConversion.SetOutFormat(OpenBabel.FORMAT_PDB);

		// Output coordinates in nm (not Angstroms) as expected by GROMACS.
		_gromosConversion.AddOption("n", Option_type.OUTOPTIONS);
		_gromosConversion.SetOutFormat(OpenBabel.FORMAT_GROMOS);
	}

	public File getMoleculeFile()
	{
		return _moleculeFile;
	}

	public File getTopologyFile()
	{
		return _topologyFile;
	}

	public OBMol getMolecule()
	{
		return _molecule;
	}

	public ImmutableZMatrix getZMatrix()
	{
		return _zMatrix;
	}

	public int[][] getTorsions()
	{
		return _torsions;
	}

	public int[] getBackbones()
	{
		return _backbones;
	}

	public int[] getDependencies()
	{
		return _dependencies;
	}

	public OBForceField getForceField()
	{
		return _forceField;
	}

	public void setForceField(ForceField forceField)
	{
		_forceField = OBForceField.FindForceField(forceField.getId());
		_forceField.Setup(_molecule, _constraints);
	}

	@Deprecated
	public double evaluate(Model model)
	{
		updateMolecule(_molecule, model);
		updateForceField(_forceField, _molecule);
		return _forceField.Energy();
	}

	@Deprecated
	public void minimize(Model model, OptimizationAlgorithm algorithm,
			int steps, double convergence)
	{
		final OBMol copy = new OBMol(_molecule);
		updateMolecule(copy, model);
		updateForceField(_forceField, copy);

		switch(algorithm) {
		case STEEPEST_DESCENT:
			_forceField.SteepestDescent(steps, convergence);
			break;
		case CONJUGATE_GRADIENTS:
			_forceField.ConjugateGradients(steps, convergence);
			break;
		}

		updateMolecule(copy, _forceField);
		updateModel(model, copy);
		copy.delete();
	}

	@Deprecated
	public void minimize(List<Model> models, OptimizationAlgorithm algorithm,
			int steps, double convergence)
	{
		final int nModels = models.size();
		for(int i = 0; i < nModels; i++) {
			final Model model = models.get(i);
			minimize(model, algorithm, steps, convergence);
		}
	}

	@Deprecated
	public List<Model> calculateConformers(Model model, int nConformers)
	{
		return calculateConformers(model, nConformers,
				DEFAULT_CONFORMERS_NUMBER_OF_CHILDREN,
				DEFAULT_CONFORMERS_MUTABILITY, DEFAULT_CONFORMERS_CONVERGENCE);
	}

	/**
	 * Conformer searching using a genetic algorithm.
	 * <p>
	 * The genetic algorithm starts by generating the initial population of
	 * rotor keys. A rotor key is simply an array of values specifying the
	 * rotations around rotatable bonds. The initial population contains up to
	 * <code>nConformers</code>, duplicate keys are ignored.
	 * <p>
	 * For each generation, <code>nChildren</code> children are created by
	 * permuting the parent rotor keys. The <code>mutability</code> setting
	 * determines how frequent a permutation is made (e.g. 5 means 1/5 bonds are
	 * permuted, 10 means 1/10). Again, duplicated and filtered molecules are
	 * ignored. The population now contains up to <code>nConformers * (1 +
	 * nChildren)</code>.
	 * <p>
	 * New generations are generated until the specified number of generations
	 * (i.e. <code>convergence</code>) don't improve the score.
	 * 
	 * @param model the initial conformer.
	 * @param nConformers the number of conformers that should be generated.
	 *        This is also the number of conformers selected for each
	 *        generation.
	 * @param nChildren when a new generation is generated, for each of the
	 *        <code>nConformers</code> conformers, <code>nChildren</code>
	 *        children are created.
	 * @param mutability the mutability determines how frequent a permutation
	 *        occurs when generating the next generation.
	 * @param convergence the number of identical generations before considering
	 *        the process converged.
	 * @return a list of generated models.
	 * @see <a href="http://goo.gl/45qlR">Conformer Searching</a>
	 * @see <a href="http://goo.gl/FQx0n">OBConformerSearch</a>
	 */
	@Deprecated
	public List<Model> calculateConformers(Model model, int nConformers,
			int nChildren, int mutability, int convergence)
			{
		final OBConformerSearch cs = new OBConformerSearch();
		final OBMol copy = new OBMol(_molecule);
		updateMolecule(copy, model);

		if(!cs.Setup(copy, nConformers, nChildren, mutability, convergence)) {
			Main.errorExit("Could not initialize conformer generation using "
					+ "Open Babel", StatusCode.CONFORMERS);
		}

		Debug.line("calculateConformers (%d conformers, %d children, "
				+ "%d mutability, %d convergence)", nConformers, nChildren,
				mutability, convergence);

		cs.Search();
		cs.GetConformers(copy);
		final List<Model> conformers = new ArrayList<Model>(nConformers);

		for(int i = 0; i < nConformers; i++) {
			copy.SetConformer(i);
			final Model conformer = model.copy();
			updateModel(conformer, copy);
			conformers.add(conformer);
		}

		copy.delete();
		cs.delete();

		return conformers;
			}

	public String toGromos(Model model)
	{
		updateMolecule(_molecule, model);
		return _gromosConversion.WriteString(_molecule);
	}

	public String toPdb(Model model)
	{
		updateMolecule(_molecule, model);
		return _pdbConversion.WriteString(_molecule);
	}

	@Deprecated
	private static void updateMolecule(OBMol molecule,
			OBForceField forceField)
	{
		forceField.GetConformers(molecule);
	}

	@Deprecated
	private static void updateForceField(OBForceField forceField,
			OBMol molecule)
	{
		forceField.Setup(molecule);
	}

	// TODO: Expensive method, works.
	@Deprecated
	private void updateMolecule(OBMol molecule, Model model)
	{
		final int size = model.size();
		final int offset = ImmutableZMatrix.MINIMUM_TORSION_ID;

		for(int i = 0; i < size; i++) {
			final int[] torsion = _torsions[i + offset];
			final int atomId = torsion[TORSION_A];
			final int bondLengthAtomId = torsion[TORSION_B];
			final int bondAngleAtomId = torsion[TORSION_C];
			final int torsionAngleAtomId = torsion[TORSION_D];
			double torsionAngle = model.getAngle(i);

			// TODO: Because of absolute/relative torsion angle bullshit in ZMatrixImpl/ZMatrixRow.
			//Print.line("%d %d %d %d relative %f", atomId, bondLengthAtomId,
			//		bondAngleAtomId, torsionAngleAtomId,
			//		Math.toDegrees(torsionAngle));
			final int masterAtomId = _dependencies[i + offset];
			if(masterAtomId != 0) {
				final double masterTorsionAngle =
					model.getAngle(masterAtomId - offset);
				torsionAngle += masterTorsionAngle;
			}
			//Print.line("   absolute %f", Math.toDegrees(torsionAngle));

			final OBAtom atom = molecule.GetAtom(atomId);
			final OBAtom bondLengthAtom = molecule.GetAtom(bondLengthAtomId);
			final OBAtom bondAngleAtom = molecule.GetAtom(bondAngleAtomId);
			final OBAtom torsionAngleAtom =
				molecule.GetAtom(torsionAngleAtomId);

			molecule.SetTorsion(atom, bondLengthAtom, bondAngleAtom,
					torsionAngleAtom, torsionAngle);
		}

		/*Print.line("updateMolecule");
		for(int i = 0; i < size; i++) {
			final int[] torsion = _torsions[i + offset];
			final int atomId = torsion[TORSION_A];
			final int bondLengthAtomId = torsion[TORSION_B];
			final int bondAngleAtomId = torsion[TORSION_C];
			final int torsionAngleAtomId = torsion[TORSION_D];
			double torsionAngle = model.getAngle(i);

			final OBAtom atom = molecule.GetAtom(atomId);
			final OBAtom bondLengthAtom = molecule.GetAtom(bondLengthAtomId);
			final OBAtom bondAngleAtom = molecule.GetAtom(bondAngleAtomId);
			final OBAtom torsionAngleAtom =
				molecule.GetAtom(torsionAngleAtomId);

			Print.line("%2d (%8.3f)%8.3f%8.3f(%8.3f)",
					i,
					Math.toDegrees(Util.ensureAngleInterval(Math.toRadians(molecule.GetTorsion(atom, bondLengthAtom, bondAngleAtom, torsionAngleAtom)))),
					molecule.GetTorsion(atom, bondLengthAtom, bondAngleAtom, torsionAngleAtom),
					Math.toDegrees(torsionAngle),
					Math.toDegrees(Util.ensureAngleInterval(torsionAngle)));
		}
		//System.exit(1);*/
	}

	// TODO: Cheaper method, has problem(s).
	/*@Deprecated
	private void updateMolecule(OBMol molecule, CartesianModel model)
	{
		final int additionalSize = cm.additionalSize();
		for(int i = 0; i < additionalSize; i++) {
			final double[] position = _positions[i];
			cm.getAdditional(i - additionalSize, position);
			_atoms[i].SetVector(position[X], position[Y], position[Z]);
		}

		final int size = _atoms.length;
		for(int i = additionalSize; i < size; i++) {
			final double[] position = _positions[i];
			cm.get(i - additionalSize, position);
			_atoms[i].SetVector(position[X], position[Y], position[Z]);
		}
	}*/

	@Deprecated
	private void updateModel(Model model, OBMol molecule)
	{
		final int size = model.size();
		final int offset = ImmutableZMatrix.MINIMUM_TORSION_ID;

		for(int i = 0; i < size; i++) {
			final int[] torsion = _torsions[i + offset];
			final int atomId = torsion[TORSION_A];
			final int bondLengthAtomId = torsion[TORSION_B];
			final int bondAngleAtomId = torsion[TORSION_C];
			final int torsionAngleAtomId = torsion[TORSION_D];
			double torsionAngle = molecule.GetTorsion(atomId,
					bondLengthAtomId, bondAngleAtomId, torsionAngleAtomId);

			// TODO: Because of absolute/relative torsion angle bullshit in ZMatrixImpl/ZMatrixRow.
			//Print.line("%d %d %d %d absolute %f", atomId, bondLengthAtomId,
			//		bondAngleAtomId, torsionAngleAtomId, torsionAngle);
			final int masterAtomId = _dependencies[i + offset];
			if(masterAtomId != 0) {
				final int[] masterTorsion = _torsions[masterAtomId];
				final int masterBondLengthAtomId = masterTorsion[TORSION_B];
				final int masterBondAngleAtomId = masterTorsion[TORSION_C];
				final int masterTorsionAngleAtomId = masterTorsion[TORSION_D];
				final double masterTorsionAngle =
					molecule.GetTorsion(masterAtomId,
							masterBondLengthAtomId,
							masterBondAngleAtomId,
							masterTorsionAngleAtomId);
				torsionAngle -= masterTorsionAngle;
			}
			//Print.line("   relative %f", torsionAngle);

			model.setAngle(i, Math.toRadians(torsionAngle));
		}

		/*Print.line("updateModel");
		for(int i = 0; i < size; i++) {
			final int[] torsion = _torsions[i + offset];
			final int atomId = torsion[TORSION_A];
			final int bondLengthAtomId = torsion[TORSION_B];
			final int bondAngleAtomId = torsion[TORSION_C];
			final int torsionAngleAtomId = torsion[TORSION_D];
			double torsionAngle = molecule.GetTorsion(atomId,
					bondLengthAtomId, bondAngleAtomId, torsionAngleAtomId);

			model.setAngle(i, Math.toRadians(torsionAngle));

			Print.line("%2d (%8.3f)%8.3f%8.3f(%8.3f)",
					i,
					Math.toDegrees(Util.ensureAngleInterval(Math.toRadians(torsionAngle))),
					torsionAngle,
					Math.toDegrees(model.getAngle(i)),
					Math.toDegrees(Util.ensureAngleInterval(model.getAngle(i))));
		}
		//System.exit(1);*/
	}
}
