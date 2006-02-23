package au.gov.naa.digipres.xena.kernel.guesser;

import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser modules return an object of this type to indicate what they've
 * discovered. The two critical bits of information are the file type and how
 * certain we are of the result.
 *
 * @author     Chris Bitmead
 * @created    2 July 2002
 */
public class OldGuess {

	/**
	 *  Can't possibly be of this type.
	 */
	public final static int GUESS_NOT = 0;

	/**
	 *  It _could_ be this type, but it doesn't seem particularly likely.
	 */
	public final static int GUESS_POSSIBLE = 1;

	/**
	 * The data looks like it could be of the type.
	 */
	public final static int GUESS_DATA_LIKELY = 2;

	/**
	 * The magic number matches
	 */
	public final static int GUESS_MAGIC_LIKELY = 3;

	/**
	 * The file extension matches
	 */
	public final static int GUESS_EXT_LIKELY = 4;

	/**
	 * The mime type matches
	 */
	public final static int GUESS_MIME_LIKELY = 5;

	/**
	 * Certainty
	 */
	public final static int GUESS_CERTAIN = 6;

	private int result;

	private Type type;

	/**
	 *  Class constructor result always GUESS_NOT
	 */

	public OldGuess() {
		this.result = GUESS_NOT;
	}
    
    public String toString(){
        return "Guess: Type: " + type.toString() + " result = " + result;
    }

	/**
	 * @param  result  result of guess
	 * @param  type    FileType being "guessed"
	 */
	public OldGuess(int result, Type type) {
		this.result = result;
		this.type = type;
	}

	/**
	 * @param  result  result of guess
	 */
	public OldGuess(int result) {
		this.result = result;
	}

	/**
	 * @param  type  FileType being guessed
	 */
	public OldGuess(Type type) {
		this.result = GUESS_NOT;
		this.type = type;
	}

	/**
	 *  Sets the result
	 *
	 * @param  v  The new result value
	 */
	public void setResult(int v) {
		result = v;
	}

	/**
	 *  Sets the FileType
	 *
	 * @param  v  The new type value
	 */
	public void setType(FileType v) {
		type = v;
	}

	/**
	 *  Gets the result
	 *
	 * @return    The result value
	 */
	public int getResult() {
		return result;
	}

	/**
	 *  Getes the type
	 *
	 * @return    The type value
	 */
	public Type getType() {
		return type;
	}
}
