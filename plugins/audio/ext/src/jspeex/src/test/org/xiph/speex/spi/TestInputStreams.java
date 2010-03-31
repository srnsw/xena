/******************************************************************************
 *                                                                            *
 * Copyright (c) 1999-2004 Wimba S.A., All Rights Reserved.                   *
 *                                                                            *
 * COPYRIGHT:                                                                 *
 *      This software is the property of Wimba S.A.                           *
 *      This software is redistributed under the Xiph.org variant of          *
 *      the BSD license.                                                      *
 *      Redistribution and use in source and binary forms, with or without    *
 *      modification, are permitted provided that the following conditions    *
 *      are met:                                                              *
 *      - Redistributions of source code must retain the above copyright      *
 *      notice, this list of conditions and the following disclaimer.         *
 *      - Redistributions in binary form must reproduce the above copyright   *
 *      notice, this list of conditions and the following disclaimer in the   *
 *      documentation and/or other materials provided with the distribution.  *
 *      - Neither the name of Wimba, the Xiph.org Foundation nor the names of *
 *      its contributors may be used to endorse or promote products derived   *
 *      from this software without specific prior written permission.         *
 *                                                                            *
 * WARRANTIES:                                                                *
 *      This software is made available by the authors in the hope            *
 *      that it will be useful, but without any warranty.                     *
 *      Wimba S.A. is not liable for any consequence related to the           *
 *      use of the provided software.                                         *
 *                                                                            *
 * Class: TestJSpeexSPI.java                                                  *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: 25th March 2005                                                      *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex.spi;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit Tests for JSpeex SPI InputStreams
 *
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class TestInputStreams
  extends TestCase
{
  /**
   * Constructor
   * @param arg0
   */
  public TestInputStreams(String arg0) {
    super(arg0);
  }
  
  /**
   * Command line entrance.
   * @param args
   */
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestInputStreams.suite());
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // TestCase classes to override
  ///////////////////////////////////////////////////////////////////////////

  /**
   * 
   */
  protected void setUp()
  {
  }
  
  /**
   * 
   */
  protected void tearDown()
  {
  }
  
  /**
   * 
   */
//  protected void runTest()
//  {
//  }
  
  /**
   * Builds the Test Suite.
   * @return the Test Suite.
   */
  public static Test suite()
  {
    return new TestSuite(TestInputStreams.class);
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // Tests
  ///////////////////////////////////////////////////////////////////////////
  
  /**
   * Test
   */
  public void testSuccess()
  {
    assertTrue("It failed", true);
  }
}
