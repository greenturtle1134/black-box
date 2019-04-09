import java.awt.Color;
import java.awt.Graphics;

public class FadeWord {
	private static final int MAX_TONE = 255;
	private static final long HALFTIME = 100;
	
	private String text;
	private long startTime;
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	private int x, y;
	private BlackPanel container;
	
	public FadeWord(String text, int x, int y, BlackPanel container) {
		this.text = text;
		this.x = x;
		this.y = y;
		this.startTime = System.currentTimeMillis();
		this.container = container;
	}
	
	public int getTone() {
		return (int) (MAX_TONE/(1+(System.currentTimeMillis()-this.startTime)/HALFTIME));
	}
	
	public void draw(Graphics g) {
		g.setFont(BlackPanel.WORD_FONT);
		int tone = Math.min(this.getTone()+container.getShade(), MAX_TONE);
		g.setColor(new Color(tone,tone,tone));
		g.drawString(text, x, y);
	}
}