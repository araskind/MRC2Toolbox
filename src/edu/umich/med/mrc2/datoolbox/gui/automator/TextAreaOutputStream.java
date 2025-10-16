/*******************************************************************************
 * 
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
 * 
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *  
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.gui.automator;

/*
 * $Id: TextAreaOutputStream.java 3117 2008-01-31 05:53:22Z xlv $
 *
 * Copyright 2007 Bruno Lowagie.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

/**
 * Everything writing to this OutputStream will be shown in a JTextArea.
 */
public class TextAreaOutputStream extends OutputStream {
	/** The text area to which we want to write. */
	protected JTextArea text;
	/** Keeps track of the offset of the text in the text area. */
	protected int offset;

	/**
	 * Constructs a TextAreaOutputStream.
	 * 
	 * @param text
	 *            the text area to which we want to write.
	 * @throws IOException
	 */
	public TextAreaOutputStream(JTextArea text) throws IOException {
		this.text = text;
		clear();
	}

	/**
	 * Clear the text area.
	 */
	public void clear() {
		text.setText(null);
		offset = 0;
	}

	/**
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		byte[] snippet = new byte[1024];
		int bytesread;
		while ((bytesread = bais.read(snippet)) > 0) {
			write(snippet, 0, bytesread);
		}
	}

	/**
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		String snippet = new String(b, off, len);
		text.insert(snippet, offset);
		offset += len - off;
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int i) throws IOException {
		byte[] b = { (byte) i };
		write(b, 0, 1);
	}

}
