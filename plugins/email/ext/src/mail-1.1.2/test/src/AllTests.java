import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;
//import junit.swingui.TestRunner;
import junit.textui.TestRunner;

public class AllTests
  extends TestSuite
{

  public AllTests()
    throws Exception
  {
    addTest(SessionTest.suite());
    addTest(TransportTest.suite());
    addTest(StoreTest.suite());
    addTest(FolderTest.suite());
    addTest(NonFolderTest.suite());
    addTest(MimeMessageTest.suite());
  }
  
  public static void main(String[] args)
  {
    try
      {
        //TestResult result = new TestResult();
        //new AllTests().run(result);
        TestRunner.run(new AllTests());
      }
    catch (Exception e)
      {
        e.printStackTrace(System.err);
        System.exit(1);
      }
  }
	
}
