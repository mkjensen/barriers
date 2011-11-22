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

package com.martinkampjensen.thesis.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;
import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.impl.ImmutableModel;
import com.martinkampjensen.thesis.model.impl.OpenBabelZMatrix;
import com.martinkampjensen.thesis.util.openbabel.OpenBabel;

/**
 * TODO: Document {@link Serialize}.
 */
public final class Serialize
{
	private static final int BARRIER_FOREST_VERSION = 1;

	private Serialize()
	{
	}

	/**
	 * Serializes a barrier forest to a file. ZIP compression is used.
	 * 
	 * @param file the file to serialize to.
	 * @param moleculeFile the file containing the molecule used to create the
	 *        forest.
	 * @param forest the object.
	 */
	public static void fromBarrierForest(File file, File moleculeFile,
			BarrierForest forest)
	{
		ZipOutputStream zos = null;

		try {
			zos = new ZipOutputStream(new BufferedOutputStream(
					new FileOutputStream(file)));
			zos.putNextEntry(new ZipEntry(""));

			final ObjectOutputStream oos = new ObjectOutputStream(zos);
			oos.writeInt(BARRIER_FOREST_VERSION);

			final Model model = forest.getMinimum().getModel();
			if(model instanceof ImmutableModel) {
				oos.writeInt(BarrierForestType.IMMUTABLEMODEL.getId());
			}
			else if(model instanceof OpenBabelZMatrix) {
				oos.writeInt(BarrierForestType.OPENBABELZMATRIX.getId());
			}
			else {
				oos.writeInt(BarrierForestType.UNKNOWN.getId());
			}

			oos.writeObject(FileHandler.read(moleculeFile));
			oos.writeObject(forest);
		}
		catch(IOException e) {
			Main.errorExit(e, StatusCode.IO);
		}
		finally {
			try {
				zos.close();
			}
			catch(IOException e) {
				// Ignore.
			}
		}
	}

	/**
	 * Deserializes a barrier forest from a file that is compressed in ZIP
	 * format.
	 * 
	 * @param file the file to deserialize from.
	 * @return the barrier forest and addition data.
	 */
	public static BarrierForestStructure toBarrierForest(File file)
	{
		ZipInputStream zis = null;

		try {
			zis = new ZipInputStream(new BufferedInputStream(
					new FileInputStream(file)));
			zis.getNextEntry();

			final ObjectInputStream ois = new ObjectInputStream(zis);

			final int version = ois.readInt();
			if(version != BARRIER_FOREST_VERSION) {
				throw new IllegalStateException("Wrong version");
			}

			final int typeId = ois.readInt();
			final String pdb = (String)ois.readObject();

			OpenBabel.loadLibrary();
			final BarrierForest forest = (BarrierForest)ois.readObject();

			return new BarrierForestStructure(typeId, pdb, forest);
		}
		catch(IOException e) {
			Main.errorExit(e, StatusCode.IO);
		}
		catch(ClassNotFoundException e) {
			Main.errorExit(e, StatusCode.SERIALIZATION);
		}
		finally {
			try {
				zis.close();
			}
			catch(IOException e) {
				// Ignore.
			}
		}

		return null;
	}

	public static final class BarrierForestStructure
	{
		private final BarrierForestType _type;
		private final String _pdb;
		private final BarrierForest _forest;

		private BarrierForestStructure(int typeId, String pdb,
				BarrierForest forest)
		{
			_type = BarrierForestType.getType(typeId);
			_pdb = pdb;
			_forest = forest;
		}

		public BarrierForestType getType()
		{
			return _type;
		}

		public String getPdb()
		{
			return _pdb;
		}

		public BarrierForest getForest()
		{
			return _forest;
		}
	}

	public enum BarrierForestType
	{
		UNKNOWN(-1),

		IMMUTABLEMODEL(1),

		OPENBABELZMATRIX(2);

		private int _id;

		private BarrierForestType(int id)
		{
			_id = id;
		}

		public static BarrierForestType getType(int id)
		{
			final BarrierForestType[] types = values();

			for(int i = 0, n = types.length; i < n; i++) {
				final BarrierForestType type = types[i];

				if(type.getId() == id) {
					return type;
				}
			}

			return UNKNOWN;
		}

		public int getId()
		{
			return _id;
		}
	}
}
