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

package edu.umich.med.mrc2.datoolbox.utils.filter;

import java.io.Serializable;

/**
 * Represents a polynomial of degree <SPAN CLASS="MATH"><I>n</I></SPAN> in power form. Such a polynomial is of
 * the form
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>p</I>(<I>x</I>) = <I>c</I><SUB>0</SUB> + <I>c</I><SUB>1</SUB><I>x</I> + <SUP> ... </SUP> + <I>c</I><SUB>n</SUB><I>x</I><SUP>n</SUP>,
 * </DIV><P></P>
 * where 
 * <SPAN CLASS="MATH"><I>c</I><SUB>0</SUB>,&#8230;, <I>c</I><SUB>n</SUB></SPAN> are the coefficients of the polynomial.
 * 
 */
public class Polynomial implements  Serializable, Cloneable {
	
   private static final long serialVersionUID = -2911550952861456470L;
   private double[] coeff;

   /**
    * Constructs a new polynomial with coefficients <TT>coeff</TT>. The value of
    *  <TT>coeff[i]</TT> in this array corresponds to <SPAN CLASS="MATH"><I>c</I><SUB>i</SUB></SPAN>.
    * 
    * @param coeff the coefficients of the polynomial.
    * 
    *    @exception NullPointerException if <TT>coeff</TT> is <TT>null</TT>.
    * 
    *    @exception IllegalArgumentException if the length of <TT>coeff</TT> is 0.
    * 
    * 
    */
   public Polynomial (double... coeff) {
      if (coeff == null)
         throw new NullPointerException ();
      if (coeff.length == 0)
         throw new IllegalArgumentException (
               "At least one coefficient is needed");
      this.coeff = coeff.clone ();
   }


   /**
    * Returns the degree of this polynomial.
    * 
    * @return the degree of this polynomial.
    * 
    */
   public int getDegree () {
      return coeff.length - 1;
   }


   /**
    * Returns an array containing the coefficients of the polynomial.
    * 
    * @return the array of coefficients.
    * 
    */
   public double[] getCoefficients () {
      return coeff.clone ();
   }


   /**
    * Returns the <SPAN CLASS="MATH"><I>i</I></SPAN>th coefficient of the polynomial.
    * 
    * @return the array of coefficients.
    * 
    */
   public double getCoefficient (int i) {
      return coeff[i];
   }


   /**
    * Sets the array of coefficients of this polynomial to <TT>coeff</TT>.
    * 
    * @param coeff the new array of coefficients.
    * 
    *    @exception NullPointerException if <TT>coeff</TT> is <TT>null</TT>.
    * 
    *    @exception IllegalArgumentException if the length of <TT>coeff</TT> is 0.
    * 
    * 
    */
   public void setCoefficients (double... coeff) {
      if (coeff == null)
         throw new NullPointerException ();
      if (coeff.length == 0)
         throw new IllegalArgumentException (
               "At least one coefficient is needed");
      this.coeff = coeff.clone ();
   }


   public double evaluate (double x) {
      double res = coeff[coeff.length - 1];
      for (int i = coeff.length - 2; i >= 0; i--)
         res = coeff[i] + x * res;
      return res;
   }

   public double derivative (double x) {
      return derivative (x, 1);
   }

   public double derivative (double x, int n) {
      if (n < 0)
         throw new IllegalArgumentException ("n < 0");
      if (n == 0)
         return evaluate (x);
      if (n >= coeff.length)
         return 0;
//      double res = coeff[coeff.length - 1]*(coeff.length - 1);
//      for (int i = coeff.length - 2; i >= n; i--)
//         res = i*(coeff[i] + x * res);
      double res = getCoeffDer (coeff.length - 1, n);
      for (int i = coeff.length - 2; i >= n; i--)
         res = getCoeffDer (i, n) + x * res;
      return res;
   }

   /**
    * Returns a polynomial corresponding to the <SPAN CLASS="MATH"><I>n</I></SPAN>th derivative of
    * this polynomial.
    * 
    * @param n the degree of the derivative.
    * 
    *    @return the derivative.
    * 
    */
   public Polynomial derivativePolynomial (int n) {
      if (n < 0)
         throw new IllegalArgumentException ("n < 0");
      if (n == 0)
         return this;
      if (n >= coeff.length)
         return new Polynomial (0);
      final double[] coeffDer = new double[coeff.length - n];
      for (int i = coeff.length - 1; i >= n; i--)
         coeffDer[i - n] = getCoeffDer (i, n);
      return new Polynomial (coeffDer);
   }


   private double getCoeffDer (int i, int n) {
      double coeffDer = coeff[i];
      for (int j = i; j > i - n; j--)
         coeffDer *= j;
      return coeffDer;
   }

   public double integral (double a, double b) {
      return integralA0 (b) - integralA0 (a);
   }

   private double integralA0 (double u) {
      final int n = coeff.length - 1;
      double res = u * coeff[n] / (n + 1);
      for (int i = coeff.length - 2; i >= 0; i--)
         res = coeff[i] * u / (i + 1) + u * res;
      return res;
   }

   /**
    * Returns a polynomial representing the integral of this polynomial.
    *  This integral is of the form
    * 
    * <P></P>
    * <DIV ALIGN="CENTER" CLASS="mathdisplay">
    * &int;<I>p</I>(<I>x</I>)<I>dx</I> = <I>c</I> + <I>c</I><SUB>0</SUB><I>x</I> + 1#1 + <SUP> ... </SUP> + 2#2,
    * </DIV><P></P>
    * where <SPAN CLASS="MATH"><I>c</I></SPAN> is a user-defined constant.
    * 
    * @param c the constant for the integral.
    * 
    *    @return the polynomial representing the integral.
    * 
    */
   public Polynomial integralPolynomial (double c) {
      final double[] coeffInt = new double[coeff.length + 1];
      coeffInt[0] = c;
      for (int i = 0; i < coeff.length; i++)
         coeffInt[i + 1] = coeff[i] / (i + 1);
      return new Polynomial (coeffInt);
   }

   @Override
   public Polynomial clone () {
      Polynomial pol;
      try {
         pol = (Polynomial) super.clone ();
      }
      catch (final CloneNotSupportedException cne) {
         throw new IllegalStateException ("Clone not supported");
      }
      pol.coeff = coeff.clone ();
      return pol;
   }
}
