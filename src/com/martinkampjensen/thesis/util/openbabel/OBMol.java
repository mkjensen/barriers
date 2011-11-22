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

import java.util.Iterator;

import org.openbabel.OBAtom;
import org.openbabel.OBBond;
import org.openbabel.OBMolAtomIter;
import org.openbabel.OBMolBondIter;
import org.openbabel.OBResidue;
import org.openbabel.OBResidueIter;

import com.martinkampjensen.thesis.util.Util;

/**
 * This class contains a workaround for an Open Babel bug.
 * <p>
 * This class overrides the {@link #delete()} method, but otherwise works as
 * the original class, {@link org.openbabel.OBMol}.
 * <p>
 * Note that this class introduces a memory leak that eventually will bring down
 * the JVM.
 * 
 * @see <a href="http://goo.gl/d8xeT">Open Babel bug 3376708</a> 
 */
public final class OBMol extends org.openbabel.OBMol
{
	private boolean _isDeleted;

	/**
	 * Delegates to {@link org.openbabel.OBMol#OBMol()}.
	 */
	public OBMol()
	{
		super();
	}

	/**
	 * Delegates to {@link org.openbabel.OBMol#OBMol(org.openbabel.OBMol)}.
	 */
	public OBMol(OBMol arg0)
	{
		super(arg0);
	}

	/**
	 * Delegates to {@link org.openbabel.OBMol#OBMol(long, boolean)}.
	 */
	public OBMol(long cPtr, boolean cMemoryOwn)
	{
		super(cPtr, cMemoryOwn);
	}

	/**
	 * Workaround for Open Babel bug: Only delete this object when
	 * {@link #NumConformers()} <code> &le; 1</code> and do not allow subsequent
	 * deletes. Note that this method introduces a memory leak that eventually
	 * will bring down the JVM.
	 * 
	 * @see <a href="http://goo.gl/d8xeT">Open Babel bug 3376708</a>  
	 */
	@Override
	public synchronized void delete()
	{
		// Just in case someone calls this method more than once. It will
		// eventually be called from the finalize method. When this method is
		// called via the finalize method, then it will not be called again.
		if(_isDeleted) {
			return;
		}
		else {
			_isDeleted = true;
		}

		if(NumConformers() > 1) {
			// In this case, calling super.delete() will result in a crash in
			// native code. The following is an attempt to leak as little
			// memory as possible.
			if(Util.isMacOs() || Util.isLinux()) {
				// The following is more or less identical to what Clear() does.
				// A notable exception is the deletion of conformers.
				destroyAtoms();
				destroyBonds();
				destroyResidues();
			}
			else {
				// Causes something like "java(1140,0x100501000) malloc: ***
				// error for object 0x1001a2ed0: pointer being freed was not
				// allocated" on Mac OS X 10.6.8.
				// Causes something like "*** glibc detected *** java: double
				// free or corruption (out): 0xb2af8b28 ***" on Ubuntu 10.10.
				Clear();
			}
		}
		else {
			// In this case it should be fine to proceed as normal.
			super.delete();
		}
	}

	private void destroyAtoms()
	{
		for(Iterator<OBAtom> it = new OBMolAtomIter(this); it.hasNext();) {
			DestroyAtom(it.next());
		}
	}

	private void destroyBonds()
	{
		for(Iterator<OBBond> it = new OBMolBondIter(); it.hasNext();) {
			DestroyBond(it.next());
		}
	}

	private void destroyResidues()
	{
		for(Iterator<OBResidue> it = new OBResidueIter(this); it.hasNext();) {
			DestroyResidue(it.next());
		}
	}
}
