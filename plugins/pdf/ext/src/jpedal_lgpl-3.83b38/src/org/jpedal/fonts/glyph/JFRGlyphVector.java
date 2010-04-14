package org.jpedal.fonts.glyph;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jpedal.fonts.JFRFont;

public class JFRGlyphVector extends GlyphVector {

	boolean debug = false;
	boolean useSuper = false;
	JFRFont f;
	FontRenderContext fontContext;
	char[] chars;

	public JFRGlyphVector() {
		if(debug){
			System.out.println("JFRGlyphVector()");
			System.out.println();
		}
	}

	public JFRGlyphVector(JFRFont font, FontRenderContext frc, char[] chars) {
		if(debug){
			System.out.println("JFRGlyphVector(JFRFont font, FontRenderContext frc, char[] chars)");
			System.out.println("font = "+font);
			System.out.println("frc = "+frc);
			for(int i=0; i!=chars.length; i++)
				System.out.println("chars["+i+"]="+chars[i]);
			System.out.println();
		}
		this.fontContext = frc;
		this.chars = chars;
		this.f = font;
	}

	public boolean equals(GlyphVector set) {
		if(debug){
			System.out.println("equals(GlyphVector set)");
			System.out.println("set = "+set);
			System.out.println();
		}

		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.equals((Object)set);
		else
			return false;

	}

	public Font getFont() {
		if(debug){
			System.out.println("getFont()");
			System.out.println();
		}
		return f;
	}

	public int getGlyphCharIndex(int glyphIndex) {
		if(debug){
			System.out.println("getGlyphCharIndex(int glyphIndex)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println(); 
		}


		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getGlyphCharIndex(glyphIndex);
		else
			return 0;
	}

	public int[] getGlyphCharIndices(int beginGlyphIndex, int numEntries, int[] codeReturn) {
		if(debug){
			System.out.println("getGlyphCharIndices(int beginGlyphIndex, int numEntries, int[] codeReturn)");
			System.out.println("beginGlyphIndex = "+beginGlyphIndex);
			System.out.println("numEntries = "+numEntries);
			for(int i=0; i!=codeReturn.length; i++)
				System.out.println("codeReturn["+i+"]="+codeReturn[i]);
			System.out.println();
		}


		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getGlyphCharIndices(beginGlyphIndex, numEntries, codeReturn);
		else{
			if (codeReturn == null) {
				codeReturn = new int[numEntries];
			}
			for (int i = 0, j = beginGlyphIndex; i < numEntries; ++i, ++j) {
				codeReturn[i] = getGlyphCharIndex(j);
			}

			return codeReturn;
		}

	}

	public Shape getGlyphOutline(int glyphIndex, float x, float y) {
		if(debug){
			System.out.println("getGlyphOutline(int glyphIndex, float x, float y)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println("x = "+x);
			System.out.println("y = "+y);
			System.out.println();
		}

		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getGlyphOutline(glyphIndex, x, y);
		else{
			Shape s = getGlyphOutline(glyphIndex);
			AffineTransform at = AffineTransform.getTranslateInstance(x,y);
			return at.createTransformedShape(s);
		}
	}

	public Rectangle getGlyphPixelBounds(int index, FontRenderContext renderFRC, float x, float y) {
		if(debug){
			System.out.println("getGlyphPixelBounds(int index, FontRenderContext renderFRC, float x, float y)");
			System.out.println("index = "+index);
			System.out.println("renderFRC = "+renderFRC);
			System.out.println("x = "+x);
			System.out.println("y = "+y);
			System.out.println();
		}

		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getGlyphPixelBounds(index, renderFRC, x, y);
		else{
			Rectangle2D rect = getGlyphVisualBounds(index).getBounds2D();
			int l = (int)Math.floor(rect.getX() + x);
			int t = (int)Math.floor(rect.getY() + y);
			int r = (int)Math.ceil(rect.getMaxX() + x);
			int b = (int)Math.ceil(rect.getMaxY() + y);
			return new Rectangle(l, t, r - l, b - t);
		}

	}

	public int getLayoutFlags() {
		if(debug){
			System.out.println("getLayoutFlags()");
			System.out.println();
		}
		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getLayoutFlags();
		else
			return 0;
	}

	public Rectangle getPixelBounds(FontRenderContext renderFRC, float x, float y) {
		if(debug){
			System.out.println("getPixelBounds(FontRenderContext renderFRC, float x, float y)");
			System.out.println("renderFRC = "+renderFRC);
			System.out.println("x = "+x);
			System.out.println("y = "+y);
			System.out.println();
		}

		boolean useSuper = this.useSuper;
		if(useSuper)
			return super.getPixelBounds(renderFRC, x, y);
		else{
			Rectangle2D rect = getVisualBounds();
			int l = (int)Math.floor(rect.getX() + x);
			int t = (int)Math.floor(rect.getY() + y);
			int r = (int)Math.ceil(rect.getMaxX() + x);
			int b = (int)Math.ceil(rect.getMaxY() + y);

			return new Rectangle (l, t, r - l, b - t);

		}
	}

	public FontRenderContext getFontRenderContext() {
		if(debug){
			System.out.println("getFontRenderContext()");
			System.out.println();
		}
		return fontContext;
	}

	public int getGlyphCode(int glyphIndex) {
		if(debug){
			System.out.println("getGlyphCode(int glyphIndex)");
			System.out.println("glyphIndex = "+glyphIndex);		
			System.out.println();
		}
		return 0;
	}

	public int[] getGlyphCodes(int beginGlyphIndex, int numEntries, int[] codeReturn) {
		if(debug){
			System.out.println("getGlyphCodes(int beginGlyphIndex, int numEntries, int[] codeReturn)");
			System.out.println("beginGlyphIndex = "+beginGlyphIndex);
			System.out.println("numEntries = "+numEntries);
			for(int i=0; i!= codeReturn.length; i++)
				System.out.println("codeReturn["+i+"] = "+codeReturn[i]);
			System.out.println();
		}
		if (codeReturn == null) {
			codeReturn = new int[numEntries];
		}
		for (int i = 0, j = beginGlyphIndex; i < numEntries; ++i, ++j) {
			codeReturn[i] = getGlyphCode(j);
		}
		return codeReturn;
		//return null;
	}

	public GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex) {
		if(debug){
			System.out.println("getGlyphJustificationInfo(int glyphIndex)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println();
		}
		return null;
	}

	public Shape getGlyphLogicalBounds(int glyphIndex) {
		if(debug){
			System.out.println("getGlyphLogicalBounds(int glyphIndex)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println();
		}
		return null;
	}

	public GlyphMetrics getGlyphMetrics(int glyphIndex) {
		if(debug){
			System.out.println("getGlyphMetrics(int glyphIndex)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println();
		}
		return null;
	}

	public Shape getGlyphOutline(int glyphIndex) {
		if(debug){
			System.out.println("getGlyphOutline(int glyphIndex)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println();
		}
		return null;
	}

	public Point2D getGlyphPosition(int glyphIndex) {
		if(debug){
			System.out.println("getGlyphPosition(int glyphIndex)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println();
		}
		return null;
	}

	public float[] getGlyphPositions(int beginGlyphIndex, int numEntries,
			float[] positionReturn) {
		if(debug){
			System.out.println("getGlyphPositions(int beginGlyphIndex, int numEntries,float[] positionReturn)");
			System.out.println("beginGlyphIndex = "+beginGlyphIndex);
			System.out.println("numEntries = "+numEntries);
			for(int i=0; i!=positionReturn.length; i++)
				System.out.println("positionReturn["+i+"] = "+positionReturn[i]);
			System.out.println();
		}
		return null;
	}

	public AffineTransform getGlyphTransform(int glyphIndex) {
		if(debug){
			System.out.println("getGlyphTransform(int glyphIndex)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println();
		}
		return null;
	}

	public Shape getGlyphVisualBounds(int glyphIndex) {
		if(debug){
			System.out.println("getGlyphVisualBounds(int glyphIndex)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println();
		}
		return new Rectangle(0,0,15,15);
	}

	public Rectangle2D getLogicalBounds() {
		if(debug){
			System.out.println("getLogicalBounds()");
			System.out.println();
		}
		return null;
	}

	public int getNumGlyphs() {
		if(debug){
			System.out.println("getNumGlyphs()");
			System.out.println();
		}		
		return 0;
	}

	public Shape getOutline() {
		if(debug){
			System.out.println("getOutline()");
			System.out.println();
		}
		return null;
	}

	public Shape getOutline(float x, float y) {
		if(debug){
			System.out.println("getOutline(float x, float y)");
			System.out.println("x = "+x);
			System.out.println("y = "+y);
			System.out.println();
		}
		return null;
	}

	public Rectangle2D getVisualBounds() {
		if(debug){
			System.out.println("getVisualBounds()");
			System.out.println();
		}
		return null;
	}

	public void performDefaultLayout() {
		if(debug){
			System.out.println("performDefaultLayout()");
			System.out.println();
		}
	}

	public void setGlyphPosition(int glyphIndex, Point2D newPos) {
		if(debug){
			System.out.println("setGlyphPosition(int glyphIndex, Point2D newPos)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println("newPos = "+newPos);
			System.out.println();
		}
	}

	public void setGlyphTransform(int glyphIndex, AffineTransform newTX) {
		if(debug){
			System.out.println("setGlyphTransform(int glyphIndex, AffineTransform newTX)");
			System.out.println("glyphIndex = "+glyphIndex);
			System.out.println("newTX = "+newTX);
			System.out.println();
		}


	}

}
