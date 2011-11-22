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

import hep.io.xdr.XDRDataInput;
import hep.io.xdr.XDRInputStream;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.collections.primitives.DoubleIterator;
import org.apache.commons.collections.primitives.adapters.DoubleIteratorIterator;

import com.martinkampjensen.thesis.util.Debug;

/**
 * Extracts energy values from GROMACS energy files (.edr files). The
 * correctness of this implementation has been tested using GROMACS 4.5.4.
 * <p>
 * Note that this implementation is not synchronized.
 * 
 * @see <a href="http://www.gromacs.org">GROMACS</a>
 */
public final class EnergyExtractor
implements Closeable, DoubleIterator, Iterable<Double>
{
	/**
	 * The supported "magic value" in the energy file header.
	 */
	private static final int FILE_MAGIC = -55555;

	/**
	 * The supported file version in the energy file header.
	 */
	private static final int FILE_VERSION = 5;

	/**
	 * The first real value of an energy frame needs to be &le; to this value.
	 * Otherwise the energy file is an older, unsupported version.
	 */
	private static final double FRAME_FIRST_REAL_CHECK = -1e10;

	/**
	 * The supported "magic value" in the energy frames.
	 */
	private static final int FRAME_MAGIC = -7777777;

	/**
	 * The supported frame version in the energy frames.
	 */
	private static final int FRAME_VERSION = FILE_VERSION;

	/**
	 * Bytes used for a single precision energy value in a GROMACS energy file
	 * (.edr).
	 */
	private static final int BYTES_PER_SINGLE_PRECISION_ENERGY_VALUE = 4;

	/**
	 * Bytes used for a double precision energy value in a GROMACS energy file
	 * (.edr).
	 */
	private static final int BYTES_PER_DOUBLE_PRECISION_ENERGY_VALUE = 8;

	/**
	 * Name of the potential energy type (according to g_energy).
	 */
	private static final String POTENTIAL_ENERGY_TYPE = "Potential";

	private final XDRInputStream _input;
	private final boolean _useDoublePrecision;
	private final int _bytesBeforeFirstValue;
	private final int _bytesBetweenFirstAndSecondValues;
	private final int _bytesBetweenValues;
	private boolean _isClosed;
	private boolean _hasNext;
	private double _next;
	private int _nValues;

	/**
	 * Delegates to {@link #EnergyExtractor(File, String)} with
	 * <code>energyType ==</code> {@value #POTENTIAL_ENERGY_TYPE}.
	 * 
	 * @param energyFile the energy file.
	 * @throws FileNotFoundException if <code>file</code> is not found.
	 * @throws IOException if an I/O error occurs.
	 */
	public EnergyExtractor(File energyFile)
	throws FileNotFoundException, IOException
	{
		this(energyFile, POTENTIAL_ENERGY_TYPE);
	}

	/**
	 * @param energyFile the energy file.
	 * @param energyType text id (according to g_energy) of the energy value to
	 *        extract from the frames.
	 * @throws FileNotFoundException if <code>file</code> is not found.
	 * @throws IOException if an I/O error occurs.
	 */
	public EnergyExtractor(File energyFile, String energyType)
	throws FileNotFoundException, IOException
	{
		if(energyType == null) {
			throw new NullPointerException("energyType == null");
		}

		final EdrFile edrFile = open(energyFile, energyType);

		_input = edrFile.input;
		_useDoublePrecision = edrFile.useDoublePrecision;
		_bytesBeforeFirstValue = edrFile.bytesBeforeFirstValue;
		_bytesBetweenFirstAndSecondValues =
			edrFile.bytesBetweenFirstAndSecondValues;
		_bytesBetweenValues = edrFile.bytesBetweenRemainingValues;
		_isClosed = false;
		_hasNext = true;
		_nValues = 0;

		try {
			skipBytes(_input, _bytesBeforeFirstValue);
			_next = readReal(_input, _useDoublePrecision);
			skipBytes(_input, _bytesBetweenFirstAndSecondValues);
		}
		catch(IOException e) {
			_input.close();
			throw e;
		}
	}

	@Override
	public void close() throws IOException
	{
		if(_isClosed) {
			return;
		}
		else {
			_isClosed = true;
		}

		_input.close();
	}

	@Override
	public boolean hasNext()
	{
		return _hasNext;
	}

	/**
	 * Reads and returns the next energy value.
	 * 
	 * @return the energy value.
	 * @throws IllegalStateException if {@link #close()} has been called.
	 * @throws NoSuchElementException if there are no more energy values. This
	 *         means that {@link #hasNext()} would have returned
	 *         <code>false</code> immediately before this method was called or
	 *         that an {@link IOException} occurred.
	 */
	@Override
	public double next()
	{
		checkState();

		if(!_hasNext) {
			throw new NoSuchElementException();
		}

		_nValues++;
		final double toReturn = _next;

		try {
			_next = readReal(_input, _useDoublePrecision);
			skipBytes(_input, _bytesBetweenValues);
		}
		catch(EOFException e) {
			_hasNext = false;
		}
		catch(IOException e) {
			final NoSuchElementException nsee = new NoSuchElementException();
			nsee.initCause(e);

			try {
				_input.close();
			}
			catch(IOException e2) {
				// Ignore second IOException.
			}

			throw nsee;
		}

		return toReturn;
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Note that internally, values are stored as <code>double</code> values
	 * which means that using the iterator returned by this method will result
	 * in boxing and unboxing from/to {@link Double}.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Double> iterator()
	{
		return DoubleIteratorIterator.wrap(this);
	}

	/**
	 * Returns the number of energy values (frames) read.
	 * <p>
	 * When {@link #hasNext()} has returned <code>false</code>, this it the
	 * total number of energy values in the energy file.
	 * 
	 * @return the number of values.
	 */
	public int values()
	{
		return _nValues;
	}

	/**
	 * Extracts and returns a specific energy value from the first frame of an
	 * energy file generated by GROMACS (.edr). Additional frames are ignored.
	 * <p>
	 * If the characteristics of the energy file differ from those of the energy
	 * file specified when this object was created, the behavior of this method
	 * is undefined. That is, values may still be returned, but they will not
	 * necessarily be the energy values expected.
	 * 
	 * @param energyFile the energy file.
	 * @throws FileNotFoundException if <code>energyFile</code> is not found.
	 * @throws IOException if an I/O error occurs.
	 * @throws EOFException if the energy file does not contain any frames.
	 */
	public double extractEnergyValue(File energyFile)
	throws FileNotFoundException, IOException
	{
		final XDRDataInput input = new XDRInputStream(
				new BufferedInputStream(new FileInputStream(energyFile)));
		skipBytes(input, _bytesBeforeFirstValue);
		final double value = readReal(input, _useDoublePrecision);
		input.close();
		return value;
	}

	/**
	 * Extracts and returns a specific energy value from a number of frames in
	 * an energy file generated by GROMACS (.edr).
	 * <p>
	 * If the characteristics of the energy file differ from those specified
	 * when this object was created, the behavior of this method is undefined.
	 * That is, values may still be returned, but they will not be the energy
	 * values expected.
	 * 
	 * @param energyFile the energy file.
	 * @param nFrames the number of frames, that is, the number of energy values
	 *        to extract.
	 * @return an array of energy values with length <code>nFrames</code>.
	 * @throws FileNotFoundException if <code>energyFile</code> is not found.
	 * @throws IOException if an I/O error occurs.
	 * @throws NegativeArraySizeException if <code>nFrames &lt; 0</code>.
	 * @throws ArrayIndexOutOfBoundsException if <code>nFrames == 0</code>.
	 * @throws EOFException if <code>nFrames</code> is larger than the actual
	 *         number of frames in the energy file.
	 */
	public double[] extractEnergyValues(File energyFile, int nFrames)
	throws FileNotFoundException, IOException
	{
		final XDRInputStream input = new XDRInputStream(
				new BufferedInputStream(new FileInputStream(energyFile)));
		final double[] values = new double[nFrames];
		final int bytesBetweenValues = _bytesBetweenValues;
		skipBytes(input, _bytesBeforeFirstValue);

		if(_useDoublePrecision) {
			for(int i = 0; i < nFrames; i++) {
				values[i] = input.readDouble();
				skipBytes(input, bytesBetweenValues);
			}
		}
		else {
			for(int i = 0; i < nFrames; i++) {
				values[i] = input.readFloat();
				skipBytes(input, bytesBetweenValues);
			}
		}

		return values;
	}

	/**
	 * Opens an energy file and reads and parses the header and the first frame
	 * to simplify later processing.
	 * 
	 * @param energyFile the energy file.
	 * @param energyType text id (according to g_energy) of the energy value to
	 *        extract from the frames.
	 * @return a class that facilitates interaction with the energy file.
	 * @throws FileNotFoundException if <code>energyFile</code> is not found.
	 * @throws IOException if an I/O error occurs.
	 */
	private static EdrFile open(File energyFile, String energyType)
	throws FileNotFoundException, IOException
	{
		final XDRInputStream input = new XDRInputStream(
				new BufferedInputStream(new FileInputStream(energyFile)));
		EdrHeader header = null;
		EdrFrame firstFrame = null;
		EdrFrame secondFrame = null;

		// Mark the starting position of the energy file to enable jumping back
		// using the reset method. The header is less than 1 KiB.
		input.mark(1024);

		try {
			for(int i = 1; i <= 2; i++) {
				final boolean useDoublePrecision = (i % 2 == 1);

				try {
					header = processHeader(input, energyType);
					firstFrame =
						processFrame(input, header, useDoublePrecision);
					secondFrame =
						processFrame(input, header, useDoublePrecision);
				}
				catch(IllegalStateException e) {
					if(useDoublePrecision) {
						Debug.line("Failed to read energy file in double "
								+ "precision, trying single precision");
						input.reset();
						continue;
					}
					else {
						Debug.line("Also failed to read energy file in single "
								+ "precision. Maybe it is not an energy file "
								+ "or a new version has been introduced.");
						input.close();
						throw e;
					}
				}

				// Success!
				input.reset();
				return new EdrFile(input, header, firstFrame, secondFrame);
			}
		}
		catch(IOException e) {
			input.close();
			throw e;
		}

		return null;
	}

	/**
	 * Reads and parses the header of an energy file.
	 * 
	 * @param input the energy file stream.
	 * @param energyType text id (according to g_energy) of the energy value to
	 *        extract from the frames.
	 * @return the header.
	 * @throws IllegalStateException if unexpected data is read from
	 *         <code>input</code>.
	 * @throws IOException if an operation on <code>input</code> fails.
	 */
	private static EdrHeader processHeader(XDRInputStream input,
			String energyType) throws IOException
			{
		final int magic = input.readInt();
		if(magic != FILE_MAGIC) {
			throw new IllegalStateException("Unknown magic value in energy "
					+ "file header. Only " + FILE_MAGIC + " is supported.");
		}

		final int version = input.readInt();
		if(version != FILE_VERSION) {
			throw new IllegalStateException("Unknown version value in energy "
					+ "file header. Only " + FILE_VERSION + " is supported.");
		}

		final int types = input.readInt();
		if(types < 1) {
			throw new IllegalStateException("According to energy file header, "
					+ "this file contains less than one energy value per "
					+ "energy frame.");
		}

		int id = -1;

		for(int i = 0; i < types; i++) {
			final String type = input.readString();
			input.readString(); // Unit is not used.

			if(energyType.equals(type)) {
				id = i;
			}
		}

		if(id == -1) {
			throw new IllegalStateException("An energy type matching \""
					+ energyType + "\" was not found ");
		}

		return new EdrHeader((int)input.getBytesRead(), types, id);
			}

	/**
	 * Reads and parses a frame in an energy file.
	 * 
	 * @param input the energy file stream.
	 * @param header the energy file header.
	 * @param useDoublePrecision whether or not to use double precision.
	 * @return the frame.
	 * @throws IllegalStateException if unexpected data is read from
	 *         <code>input</code>.
	 * @throws IOException if an operation on <code>input</code> fails.
	 */
	private static EdrFrame processFrame(XDRInputStream input, EdrHeader header,
			boolean useDoublePrecision) throws IOException
			{
		final int bytesReadBefore = (int)input.getBytesRead();

		final double firstReal = readReal(input, useDoublePrecision);
		if(firstReal > FRAME_FIRST_REAL_CHECK) {
			throw new IllegalStateException("Unknown first real value "
					+ "in frame. Only <= " + FRAME_FIRST_REAL_CHECK
					+ " is supported.");
		}

		final int magic = input.readInt();
		if(magic != FRAME_MAGIC) {
			throw new IllegalStateException("Unknown magic value in energy "
					+ "frame header. Only " + FRAME_MAGIC + " is supported.");
		}

		final int version = input.readInt();
		if(version != FRAME_VERSION) {
			throw new IllegalStateException("Unknown version value in energy "
					+ "frame header. Only " + FRAME_VERSION + " is supported.");
		}

		input.readDouble(); // t_enxframe.t
		input.readLong(); // t_enxframe.step

		boolean threeValuesPerType = false;
		final int nsum = input.readInt(); // t_enxframe.nsum
		if(nsum > 0) {
			// Instead of one value per energy type there will also be an
			// average and a sum.
			threeValuesPerType = true;
		}

		input.readLong(); // t_enxframe.nsteps
		input.readDouble(); // t_enxframe.dt

		final int nre = input.readInt(); // t_enxframe.nre
		if(nre != header.types) {
			throw new IllegalStateException("Number of energy types in energy "
					+ "frame differs from number of types in header.");
		}

		input.readInt(); // Reserved for possible future use.

		final int nblock = input.readInt(); // t_enxframe.nblock
		if(nblock > 0) {
			throw new IllegalStateException("Energy file blocks are not "
					+ "supported.");
		}

		input.readInt(); // t_enxframe.e_size
		input.readInt(); // Reserved for possible future use.
		input.readInt(); // For compatibility with old code.

		final int bytesBeforeFirstValue =
			(int)input.getBytesRead() - bytesReadBefore;
		final int valuesPerType = (threeValuesPerType ? 3 : 1);
		final int bytesPerValue = (useDoublePrecision ?
				BYTES_PER_DOUBLE_PRECISION_ENERGY_VALUE :
					BYTES_PER_SINGLE_PRECISION_ENERGY_VALUE);

		final int bytesBeforeValue =
			bytesBeforeFirstValue + header.id * valuesPerType * bytesPerValue;
		final int bytesAfterValue =
			(valuesPerType - 1) * bytesPerValue
			+ (header.types - header.id - 1) * valuesPerType * bytesPerValue;

		// The actual energy values follows, but are not read here.
		skipBytes(input, bytesBeforeValue + bytesPerValue + bytesAfterValue
				- bytesBeforeFirstValue);

		return new EdrFrame(bytesBeforeValue, bytesAfterValue,
				useDoublePrecision);
			}

	/**
	 * Skips over <code>n</code> bytes of data from the input stream, discarding
	 * the skipped bytes.
	 * 
	 * @param input the stream in which to skip bytes.
	 * @param n the number of bytes to be skipped.
	 * @throws IOException if an I/O error occurs.
	 * @see DataInput#skipBytes(int)
	 */
	private static void skipBytes(DataInput input, int n) throws IOException
	{
		while(n > 0) {
			n -= input.skipBytes(n);
		}
	}

	private static double readReal(DataInput input, boolean useDoublePrecision)
	throws IOException
	{
		if(useDoublePrecision) {
			return input.readDouble();
		}
		else {
			return input.readFloat();
		}
	}

	/**
	 * Checks if the state of this object is legal, that is, if {@link #close()}
	 * has not been called. If the state is illegal, an
	 * {@link IllegalStateException} is thrown.
	 * 
	 * @throws IllegalStateException if the state is illegal.
	 */
	private void checkState()
	{
		if(_isClosed) {
			throw new IllegalStateException("close() has been called");
		}
	}

	private static final class EdrFile
	{
		private final XDRInputStream input;
		private final boolean useDoublePrecision;
		private final int bytesBeforeFirstValue;
		private final int bytesBetweenFirstAndSecondValues;
		private final int bytesBetweenRemainingValues;

		private EdrFile(XDRInputStream input, EdrHeader header,
				EdrFrame firstFrame, EdrFrame secondFrame)
		{
			this.input = input;
			this.useDoublePrecision = firstFrame.useDoublePrecision;
			this.bytesBeforeFirstValue =
				header.bytesBeforeFirstFrame + firstFrame.bytesBeforeValue;
			this.bytesBetweenFirstAndSecondValues =
				firstFrame.bytesAfterValue + secondFrame.bytesBeforeValue;
			this.bytesBetweenRemainingValues =
				secondFrame.bytesAfterValue + secondFrame.bytesBeforeValue;
		}
	}

	private static final class EdrHeader
	{
		private final int bytesBeforeFirstFrame;
		private final int types;
		private final int id;

		private EdrHeader(int bytesBeforeFirstFrame, int types, int id)
		{
			this.bytesBeforeFirstFrame = bytesBeforeFirstFrame;
			this.types = types;
			this.id = id;
		}
	}

	private static final class EdrFrame
	{
		private final int bytesBeforeValue;
		private final int bytesAfterValue;
		private final boolean useDoublePrecision;

		private EdrFrame(int bytesBeforeValue, int bytesAfterValue,
				boolean useDoublePrecision)
		{
			this.bytesBeforeValue = bytesBeforeValue;
			this.bytesAfterValue = bytesAfterValue;
			this.useDoublePrecision = useDoublePrecision;
		}
	}
}
