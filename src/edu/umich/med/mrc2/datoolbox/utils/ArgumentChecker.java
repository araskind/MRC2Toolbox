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

package edu.umich.med.mrc2.datoolbox.utils;

/**
 * From Orson Charts
 *	Arguments passed to methods are validated and exceptions thrown for invalid cases
 * (the idea is to fail fast, which usually helps when tracking down errors
 * in programming logic).
 */
public final class ArgumentChecker {

    /**
     * Checks if the specified argument is {@code null} and, if it is,
     * throws an {@code IllegalArgumentException}.
     *
     * @param arg  the argument to check ({@code null} permitted).
     * @param name  the parameter name ({@code null} not permitted).
     */
    public static void nullNotPermitted(Object arg, String name) {
        if (arg == null) {
            throw new IllegalArgumentException("Null '" + name + "' argument.");
        }
    }

    /**
     * Checks if the specified argument is negative and, if it is, throws an
     * {@code IllegalArgumentException}.
     *
     * @param value  the value.
     * @param name  the parameter name ({@code null} not permitted).
     */
    public static void negativeNotPermitted(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException("Param '" + name
                    + "' cannot be negative");
        }
    }

    /**
     * Checks if the specified argument is positive and, if it is NOT, throws an
     * {@code IllegalArgumentException}.
     *
     * @param value  the value.
     * @param name  the parameter name ({@code null} not permitted).
     */
    public static void positiveRequired(double value, String name) {
        if (value <= 0.0) {
            throw new IllegalArgumentException("Param '" + name
                    + "' must be positive.");
        }
    }

    /**
     * Checks if the specified argument is finite and, if it is NOT, throws an
     * {@code IllegalArgumentException}.
     *
     * @param value  the value.
     * @param name  the parameter name ({@code null} not permitted).
     *
     * @since 1.4
     */
    public static void finiteRequired(double value, String name) {
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException("Param '" + name
                    + "' must be finite.");
        }
    }

    /**
     * Checks if the specified argument is finite and positive and,
     * if it is NOT, throws an {@code IllegalArgumentException}.
     *
     * @param value  the value.
     * @param name  the parameter name ({@code null} not permitted).
     *
     * @since 1.4
     */
    public static void finitePositiveRequired(double value, String name) {
        if (value <= 0.0 || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Param '" + name
                    + "' must be finite and positive.");
        }
    }

    /**
     * Checks that the index is less than the specified {@code arrayLimit}
     * and throws an {@code IllegalArgumentException} if it is not.
     *
     * @param index  the array index.
     * @param name  the parameter name (to display in the error message).
     * @param arrayLimit  the array size.
     */
    public static void checkArrayBounds(int index, String name,
            int arrayLimit) {
        if (index >= arrayLimit) {
            throw new IllegalArgumentException("Requires '" + name
                    + "' in the range 0 to " + (arrayLimit - 1));
        }
    }

}
