/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.utils;

import java.lang.reflect.Field;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;

public class FieldUtilsTest {

    @Test
    public void testGetAllFieldsList() {

        // Get all fields in this class and all of its parents
        final List<Field> allFields = FieldUtils.getAllFieldsList(LinkedList.class);

        // Get the fields form each individual class in the type's hierarchy
        final List<Field> allFieldsClass = Arrays.asList(LinkedList.class.getFields());
        final List<Field> allFieldsParent = Arrays.asList(AbstractSequentialList.class.getFields());
        final List<Field> allFieldsParentsParent = Arrays.asList(AbstractList.class.getFields());
        final List<Field> allFieldsParentsParentsParent = Arrays.asList(AbstractCollection.class.getFields());

        // Test that `getAllFieldsList` did truly get all of the fields of the the class and all its parents 
        Assert.assertTrue(allFields.containsAll(allFieldsClass));
        Assert.assertTrue(allFields.containsAll(allFieldsParent));
        Assert.assertTrue(allFields.containsAll(allFieldsParentsParent));
        Assert.assertTrue(allFields.containsAll(allFieldsParentsParentsParent));
    }
}
