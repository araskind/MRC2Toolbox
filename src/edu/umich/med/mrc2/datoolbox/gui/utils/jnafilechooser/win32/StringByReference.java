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

package edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.win32;

import com.sun.jna.ptr.ByReference;

public class StringByReference extends ByReference {

	public StringByReference() {
        this(0);
    }

    public StringByReference(int size) {
        super(size < 4 ? 4 : size);
        getPointer().clear(size < 4 ? 4 : size);
    }

    public StringByReference(String str) {
        super(str.length() < 4 ? 4 : str.length() + 1);
        setValue(str);
    }

    private void setValue(String str) {
        getPointer().setString(0, str);
    }

    public String getValue() {
        return getPointer().getString(0);
    }
}
