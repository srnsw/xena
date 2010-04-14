package org.jpedal.fonts;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.peer.FontPeer;
import java.text.CharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Locale;
import java.util.Map;

import org.jpedal.fonts.glyph.JFRGlyphVector;

public class JFRFont extends Font {

	boolean useSuper = false;
	boolean debug = false;

	public JFRFont(Map arg0) {
		super(arg0);
	}

	public JFRFont(String name, int style, int size) {
		super(name, style, size);
		this.name = name;
		this.style = style;
		this.size = size;

	}

	public boolean canDisplay(char c) {
		if(debug){
			System.out.println("canDisplay(char c)");
			System.out.println("c = "+c);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.canDisplay(c);
		else
			return false;
	}

	public int canDisplayUpTo(char[] text, int start, int limit) {
		if(debug){
			System.out.println("canDisplayUpTo(char[] text, int start, int limit)");
			for(int i=0;i != text.length; i++)
				System.out.println("test["+i+"]"+text[i]);
			System.out.println("start = "+start);
			System.out.println("limit = "+limit);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.canDisplayUpTo(text, start, limit);
		else
			return 0;
	}

	public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
		if(debug){
			System.out.println("canDisplayUpTo(CharacterIterator iter, int start, int limit)");
			System.out.println("iter = "+iter);
			System.out.println("start = "+start);
			System.out.println("limit = "+limit);
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.canDisplayUpTo(iter, start, limit);
		else
			return 0;
	}

	public int canDisplayUpTo(String str) {
		if(debug){
			System.out.println("canDisplayUpTo(String str)");
			System.out.println("str = "+str);
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.canDisplayUpTo(str);
		else
			return 0;
	}

	public GlyphVector createGlyphVector(FontRenderContext frc, char[] chars) {
		if(debug){
			System.out.println("createGlyphVector(FontRenderContext frc, char[] chars)");
			System.out.println("frc = "+frc);
			for(int i=0; i!=chars.length; i++)
				System.out.println("chars["+i+"] = "+chars[i]);
			System.out.println();
		}
		boolean useSuper = false; 
		if(useSuper)
			return super.createGlyphVector(frc, chars);
		else{
			
			JFRGlyphVector gv = new JFRGlyphVector(this, frc, chars);

			return gv;
		}
	}

	public GlyphVector createGlyphVector(FontRenderContext frc, CharacterIterator ci) {
		if(debug){
			System.out.println("createGlyphVector(FontRenderContext frc, CharacterIterator ci)");
			System.out.println("frc = "+frc);
			System.out.println("ci = "+ci);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return (JFRGlyphVector)super.createGlyphVector(frc, ci);
		else
			return null;
	}

	public GlyphVector createGlyphVector(FontRenderContext frc, int[] glyphCodes) {
		if(debug){
			System.out.println("createGlyphVector(FontRenderContext frc, int[] glyphCodes)");
			System.out.println("frc = "+frc);
			for(int i=0; i!= glyphCodes.length; i++)
				System.out.println("glyphCodes["+i+"] = "+glyphCodes[i]);
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return (JFRGlyphVector)super.createGlyphVector(frc, glyphCodes);
		else
			return null;
	}

	public GlyphVector createGlyphVector(FontRenderContext frc, String str) {
		if(debug){
			System.out.println("createGlyphVector(FontRenderContext frc, String str)");
			System.out.println("frc = "+frc);
			System.out.println("str = "+str);
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return (JFRGlyphVector)super.createGlyphVector(frc, str);
		else
			return null;
	}

	public Font deriveFont(AffineTransform trans) {
		if(debug){
			System.out.println("deriveFont(AffineTransform trans)");
			System.out.println("trans = "+trans);
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.deriveFont(trans);
		else
			return null;
	}

	public Font deriveFont(float size) {
		if(debug){
			System.out.println("deriveFont(AffineTransform trans)");
			System.out.println("size = "+size);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.deriveFont(size);
		else
			return null;
	}

	public Font deriveFont(int style, AffineTransform trans) {
		if(debug){
			System.out.println("deriveFont(AffineTransform trans)");
			System.out.println("trans = "+trans);
			System.out.println("style = "+style);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.deriveFont(style, trans);
		else
			return null;
	}

	public Font deriveFont(int style, float size) {
		if(debug){
			System.out.println("deriveFont(AffineTransform trans)");
			System.out.println("style = "+style);
			System.out.println("size = "+size);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.deriveFont(style, size);
		else
			return null;
	}

	public Font deriveFont(int style) {
		if(debug){
			System.out.println("deriveFont(AffineTransform trans)");
			System.out.println("style = "+style);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.deriveFont(style);
		else
			return null;
	}

	public Font deriveFont(Map map) {
		if(debug){
			System.out.println("deriveFont(AffineTransform trans)");
			System.out.println("map = "+map.toString());
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.deriveFont(map);
		else
			return null;
	}

	public boolean equals(Object obj) {
		if(debug){
			System.out.println("equals(Object obj)");
			System.out.println("obj = "+obj);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.equals(obj);
		else
			return false;
	}

	protected void finalize() throws Throwable {
		if(debug){
			System.out.println("finalize()");
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			super.finalize();

	}

	public Map getAttributes() {
		if(debug){
			System.out.println("getAttributes()");
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getAttributes();
		else
			return null;
	}

	public Attribute[] getAvailableAttributes() {
		if(debug){
			System.out.println("getAvailableAttributes()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getAvailableAttributes();
		else
			return null;
	}

	public byte getBaselineFor(char c) {
		if(debug){
			System.out.println("getBaselineFor(char c)");
			System.out.println("c = "+c);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getBaselineFor(c);
		else
			return 0;
	}

	public String getFamily() {
		if(debug){
			System.out.println("getFamily()");
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getFamily();
		else
			return null;
	}

	public String getFamily(Locale l) {
		if(debug){
			System.out.println("getFamily(Locale l)");
			System.out.println("l = "+l);
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getFamily(l);
		else
			return null;
	}

	public String getFontName() {
		if(debug){
			System.out.println("getFontName()");
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getFontName();
		else
			return null;
	}

	public String getFontName(Locale l) {
		if(debug){
			System.out.println("getFontName(Locale l)");
			System.out.println("l = "+l);
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getFontName(l);
		else 
			return null;
	}

	public float getItalicAngle() {
		if(debug){
			System.out.println("getItalicAngle()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getItalicAngle();
		else
			return 0;
	}

	public LineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, FontRenderContext frc) {
		if(debug){
			System.out.println("getLineMetrics(char[] chars, int beginIndex, int limit, FontRenderContext frc)");
			for(int i=0; i!=chars.length; i++)
				System.out.println("chars["+i+"] = "+chars[i]);
			System.out.println("beginIndex = "+beginIndex);
			System.out.println("limit = "+limit);
			System.out.println("frc = "+frc);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getLineMetrics(chars, beginIndex, limit, frc);
		else
			return null;
	}

	public LineMetrics getLineMetrics(CharacterIterator ci, int beginIndex,
			int limit, FontRenderContext frc) {
		if(debug){
			System.out.println("getLineMetrics(CharacterIterator ci, int beginIndex, int limit, FontRenderContext frc)");
			System.out.println("ci = "+ci);
			System.out.println("beginIndex = "+beginIndex);
			System.out.println("limit = "+limit);
			System.out.println("frc = "+frc);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getLineMetrics(ci, beginIndex, limit, frc);
		else
			return null;
	}

	public LineMetrics getLineMetrics(String str, FontRenderContext frc) {
		if(debug){
			System.out.println("getLineMetrics(String str, FontRenderContext frc)");
			System.out.println("str = "+str);
			System.out.println("frc = "+frc);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getLineMetrics(str, frc);
		else
			return null;
	}

	public LineMetrics getLineMetrics(String str, int beginIndex, int limit,FontRenderContext frc) {
		if(debug){
			System.out.println("getLineMetrics(String str, int beginIndex, int limit,FontRenderContext frc)");
			System.out.println("str = "+str);
			System.out.println("beginIndex = "+beginIndex);
			System.out.println("limit = "+limit);
			System.out.println("frc = "+frc);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getLineMetrics(str, beginIndex, limit, frc);
		else
			return null;
	}

	public Rectangle2D getMaxCharBounds(FontRenderContext frc) {
		if(debug){
			System.out.println("getMaxCharBounds(FontRenderContext frc)");
			System.out.println("frc = "+frc);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getMaxCharBounds(frc);
		else
			return null;
	}

	public int getMissingGlyphCode() {
		if(debug){
			System.out.println("getMissingGlyphCode()");
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getMissingGlyphCode();
		else
			return 0;
	}

	public String getName() {
		if(debug){
			System.out.println("getName()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getName();
		else
			return null;
	}

	public int getNumGlyphs() {
		if(debug){
			System.out.println("getNumGlyphs()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getNumGlyphs();
		else
			return 0;
	}

	public FontPeer getPeer() {
		if(debug){
			System.out.println("getPeer()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getPeer();
		else
			return null;
	}

	public String getPSName() {
		if(debug){
			System.out.println("getPSName()");
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getPSName();
		else
			return null;
	}

	public int getSize() {
		if(debug){
			System.out.println("getSize()");
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getSize();
		else
			return 0;
	}

	public float getSize2D() {
		if(debug){
			System.out.println("getSize2D()");
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getSize2D();
		else
			return 0;
	}

	public Rectangle2D getStringBounds(char[] chars, int beginIndex, int limit, FontRenderContext frc) {
		if(debug){
			System.out.println("getStringBounds(char[] chars, int beginIndex, int limit, FontRenderContext frc)");
			for(int i=0; i!=chars.length; i++)
				System.out.println("chars["+i+"] = "+chars[i]);
			System.out.println("beginIndex = "+beginIndex);
			System.out.println("limit = "+limit);
			System.out.println("frc = "+frc);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getStringBounds(chars, beginIndex, limit, frc);
		else
			return null;
	}

	public Rectangle2D getStringBounds(CharacterIterator ci, int beginIndex, int limit, FontRenderContext frc) {
		if(debug){
			System.out.println("getStringBounds(CharacterIterator ci, int beginIndex, int limit, FontRenderContext frc)");
			System.out.println("ci = "+ci);
			System.out.println("beginIndex = "+beginIndex);
			System.out.println("limit = "+limit);
			System.out.println("frc = "+frc);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getStringBounds(ci, beginIndex, limit, frc);
		else
			return null;
	}

	public Rectangle2D getStringBounds(String str, FontRenderContext frc) {
		if(debug){
			System.out.println("getStringBounds(String str, FontRenderContext frc)");
			System.out.println("str = "+str);
			System.out.println("frc = "+frc);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getStringBounds(str, frc);
		else
			return null;
	}

	public Rectangle2D getStringBounds(String str, int beginIndex, int limit, FontRenderContext frc) {
		if(debug){
			System.out.println("getStringBounds(String str, int beginIndex, int limit, FontRenderContext frc)");
			System.out.println("str = "+str);
			System.out.println("beginIndex = "+beginIndex);
			System.out.println("limit = "+limit);
			System.out.println("frc = "+frc);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getStringBounds(str, beginIndex, limit, frc);
		else
			return null;
	}

	public int getStyle() {
		if(debug){
			System.out.println("getStyle()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getStyle();
		else
			return 0;
	}

	public AffineTransform getTransform() {
		if(debug){
			System.out.println("getTransform()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.getTransform();
		else
			return null;
	}

	public int hashCode() {
		if(debug){
			System.out.println("hashCode()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.hashCode();
		else return 0;
	}

	public boolean hasUniformLineMetrics() {
		if(debug){
			System.out.println("hasUniformLineMetrics()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.hasUniformLineMetrics();
		else
			return false;
	}

	public boolean isBold() {
		if(debug){
			System.out.println("isBold()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.isBold();
		else
			return false;
	}

	public boolean isItalic() {
		if(debug){
			System.out.println("isItalic()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.isItalic();
		else
			return false;
	}

	public boolean isPlain() {
		if(debug){
			System.out.println("isPlain");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.isPlain();
		else
			return false;
	}

	public boolean isTransformed() {
		if(debug){
			System.out.println("isTransformed");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.isTransformed();
		else
			return false;
	}

	public GlyphVector layoutGlyphVector(FontRenderContext frc, char[] text, int start, int limit, int flags) {
		if(debug){
			System.out.println("GlyphVector layoutGlyphVector(FontRenderContext frc, char[] text, int start, int limit, int flags)");
			System.out.println("frc = "+frc);
			for(int i=0; i!=text.length; i++)
				System.out.println("text["+i+"] = "+text[i]);
			System.out.println("start = "+start);
			System.out.println("limit = "+limit);
			System.out.println("flags = "+flags);
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.layoutGlyphVector(frc, text, start, limit, flags);
		else
			return null;
	}

	public String toString() {
		if(debug){
			System.out.println("toString()");
			System.out.println();
		}
		boolean useSuper = this.useSuper; 
		if(useSuper)
			return super.toString();
		else
			return null;
	}

}
