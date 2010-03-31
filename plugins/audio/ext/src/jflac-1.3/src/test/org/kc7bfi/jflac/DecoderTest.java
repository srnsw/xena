package org.kc7bfi.jflac;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 *
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 */
public class DecoderTest extends AbstractTestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DecoderTest( String testName ) {
        super( testName );
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( DecoderTest.class );
    }
    
    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertEquals( "maven kicks ass", "maven kicks ass" );
    }
}
