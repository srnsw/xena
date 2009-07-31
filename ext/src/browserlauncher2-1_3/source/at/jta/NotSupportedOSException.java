// $Id$
package at.jta;

/********************************************************************************************************************************
 *
 * <p>Title: Exception is thrown if you use the regor class on not windows machines </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: Taschek Joerg</p>
 *
 * @author Taschek Joerg
 * @version 1.0
 *******************************************************************************************************************************/
public class NotSupportedOSException
    extends RuntimeException
{
  /******************************************************************************************************************************
   * Constructor with message to throw
   * @param str String
   *****************************************************************************************************************************/
  public NotSupportedOSException(String str)
  {
    super(str);
  }
}
