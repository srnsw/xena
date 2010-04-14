package org.jpedal.decompression;

public class ChangingPicElement {
	
	private int start = 0;
	private int lenght = 0;
	private int color = 0;
	
	public ChangingPicElement(int start, int lenght, int color){
		this.start = start;
		this.lenght = lenght;
		this.color = color;
	}
	
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getLenght() {
		return lenght;
	}
	public void setLenght(int lenght) {
		this.lenght = lenght;
	}
	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}
}
