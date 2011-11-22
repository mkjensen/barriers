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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Writes atom coordinates of conformations (frames) to an XTC trajectory. Uses
 * the GROMACS XTC Library (xdrfile) via Java Native Access (JNA).
 * <p>
 * This class exposes limited writing functionality. It is for example not
 * possible to influence the step and time values of a frame, they will be
 * incremented automatically. Additionally, the box will always consist of
 * zeroes.
 * <p>
 * Note that this implementation is not synchronized and that this class has not
 * been prepared to handle the existence of more than one instance at a time.
 * <p>
 * libxdrfile.[so,dylib] or xdrfile.dll must be in
 * <code>jna.library.path</code>.
 * 
 * @see <a href="http://www.gromacs.org">GROMACS</a>
 * @see <a href="https://github.com/twall/jna">Java Native Access</a>
 */
public final class XtcWriter implements Closeable
{
	/**
	 * Name of the native library. Will be libLIBRARY_NAME.so on Linux,
	 * libLIBRARY_NAME.dylib on Mac OS X, and LIBRARY_NAME.dll on Windows.
	 */
	private static final String LIBRARY_NAME = "xdrfile";

	/**
	 * <code>exdrOK</code> from xdrfile.h.
	 */
	private static final int STATUS_SUCCESS = 0;

	/**
	 * <code>exdrENDOFFILE</code> from xdrfile.h.
	 */
	private static final int STATUS_END = 11;

	/**
	 * From xdrfile.h, <code>"r"</code> for reading.
	 */
	private static final String MODE_WRITE = "w";

	/**
	 * Default precision used by GROMACS.
	 */
	private static final int PRECISION = 1000;

	/**
	 * Number of coordinates per point/atom.
	 */
	private static final int DIMENSIONS = 3;

	private final String _xdrPath;
	private final Pointer _xdrPointer;
	private final int _frameNatoms;
	private final float[] _frameBox;
	private final float[] _frameX;
	private final int _framePrec;
	private int _frameStep;
	private int _frameTime;
	private boolean _isClosed;
	private int _nConformations;

	public XtcWriter(File trajectory, int natoms) throws IOException
	{
		this(trajectory, natoms, PRECISION);
	}

	public XtcWriter(File trajectory, int natoms, int prec) throws IOException
	{
		if(trajectory.exists() && !trajectory.isFile()) {
			throw new IOException("trajectory already exists as non-file");
		}

		Native.register(LIBRARY_NAME);

		_xdrPath = trajectory.getAbsolutePath();
		_xdrPointer = xdrfile_open(_xdrPath, MODE_WRITE);

		if(_xdrPointer == null) {
			throw new IOException("Unknown error when trying to open "
					+ "trajectory");
		}

		_frameNatoms = natoms;
		_frameBox = new float[DIMENSIONS * DIMENSIONS];
		_frameX = new float[_frameNatoms * DIMENSIONS];
		_framePrec = prec;
		_frameStep = 0;
		_frameTime = 0;
		_isClosed = false;
		_nConformations = 0;
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

		final int statusCode = xdrfile_close(_xdrPointer);

		Native.unregister();

		if(statusCode != STATUS_SUCCESS) {
			throw new IOException("Error " + statusCode + " when trying to "
					+ "close trajectory");
		}
	}

	/**
	 * Returns the number of atoms in each conformation (frame) of the
	 * trajectory.
	 * 
	 * @return the number of atoms. 
	 */
	public int atoms()
	{
		return _frameNatoms;
	}

	/**
	 * Returns the number of conformations (frames) written.
	 * <p>
	 * When {@link #close()} has been called , this is the total number of
	 * conformations written to the trajectory.
	 * 
	 * @return the number of frames.
	 */
	public int conformations()
	{
		return _nConformations;
	}

	/**
	 * Writes atom coordinates to a new frame in the trajectory being written
	 * to. The coordinates are expected to be in nm, but they are written in
	 * angstrom.
	 * 
	 * @param coordinates an array with coordinates to write.
	 * @throws IllegalStateException if {@link #close()} has been called.
	 * @throws IOException if an I/O error occurs while writing to the
	 *         trajectory.
	 * @throws NullPointerException if <code>coordinates == null</code>.
	 * @throws IllegalArgumentException if <code>coordinates.length</code> is
	 *         not equals to the number of atoms supposed to be in each frame.
	 * @throws IndexOutOfBoundsException if <code>coordinates</code> has illegal
	 *         dimensions.
	 */
	public void write(double[][] coordinates)
	{
		checkState();

		if(coordinates.length != _frameNatoms) {
			throw new IllegalArgumentException(
			"coordinates.length must be equal to the number of atoms");
		}

		fillArray(_frameX, coordinates, _frameNatoms, DIMENSIONS);
		fromAngstromToNm(_frameX);

		write_xtc(_xdrPointer, _frameNatoms, _frameStep, _frameTime, _frameBox,
				_frameX, _framePrec);

		_frameStep++;
		_frameTime++;
		_nConformations++;
	}

	/**
	 * This implementation calls {@link #close()}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	protected void finalize() throws IOException
	{
		close();
	}

	/**
	 * Fills an array using a matrix.
	 * 
	 * @param array the array.
	 * @param matrix the matrix.
	 * @param rows the number of rows in the matrix.
	 * @param columns the number of columns in the matrix.
	 * @return the matrix.
	 * @throws NullPointerException if <code>array == null</code> or if
	 *         <code>matrix == null</code>.
	 * @throws IndexOutOfBoundsException if there is a mismatch between
	 *         <code>array</code>, <code>matrix</code>, <code>rows</code>, and
	 *         <code>columns</code>.
	 */
	private static void fillArray(float[] array, double[][] matrix, int rows,
			int columns)
	{
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				array[i * columns + j] = (float)matrix[i][j];
			}
		}
	}

	/**
	 * Converts an array of values in angstrom to values in nm.
	 * <p>
	 * <code>1 nm == 1 * 10^-9</code>, <code>1 angstrom == 1 * 10^-10</code>.
	 * 
	 * @param array the array.
	 * @see <a href="http://en.wikipedia.org/wiki/Angstrom">Angstrom</a>
	 */
	private static void fromAngstromToNm(float[] array)
	{
		final int length = array.length;
		for(int i = 0; i < length; i++) {
			array[i] /= 10;
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

	// Direct Mapping using Java Native Access. Selected methods from xdrfile.h
	// and xdrfile_xtc.h of the GROMACS XTC Library (xdrfile).
	private native Pointer xdrfile_open(String path, String mode);
	private native int xdrfile_close(Pointer xfp);
	private native int write_xtc(Pointer xd, int natoms, int step, float time,
			float[] box, float[] x, float prec);
}
