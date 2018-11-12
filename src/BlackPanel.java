import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JPanel;

public class BlackPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Font WORD_FONT = new Font("Serif", Font.BOLD, 28);
	public static final int X_MARGIN = 50;
	public static final int Y_MARGIN = 10;
	public static final double MAX_TONE = 255;
	public static final double HALFTIME = 100;
	public static final double CHANGE_RATIO = 0.1;
	public static final long TIME_THRESHOLD = 30000;
	private StringBuffer thisWord;
	private int wordX, wordY;
	private LinkedList<FadeWord> words;
	private long flashTime;
	private int wordCount;
	private boolean showCount;
	private double currentPace;
	private long lastTime;

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public boolean isShowingCount() {
		return showCount;
	}

	public void setShowCount(boolean showCount) {
		this.showCount = showCount;
	}

	public void addChar(char c) {
		thisWord.append(c);
	}

	public void breakWord() {
		if (thisWord.length()>0) {
			FadeWord word = new FadeWord(thisWord.toString(), wordX, wordY, this);
			words.add(word);
			thisWord.delete(0, thisWord.length());
			changeLoc();
			this.setWordCount(this.getWordCount() + 1);
			long time = System.currentTimeMillis();
			if (time-lastTime<TIME_THRESHOLD) {
				//This means that the time counted is valid.
				if (currentPace>0) {
					//Current pace is set
					currentPace = (time - lastTime) * CHANGE_RATIO + currentPace * (1 - CHANGE_RATIO);
					System.out.println(60000 / currentPace);
				}
				else {
					//Current pace not set
					currentPace = time-lastTime;
				}
			}
			lastTime = time;
		}
	}

	public void changeLoc() {
		wordX = (int) ((this.getWidth() - X_MARGIN) * Math.random());
		wordY = (int) ((this.getHeight() - Y_MARGIN) * Math.random());
	}

	public void flash() {
		this.flashTime = System.currentTimeMillis();
	}

	public void doTick() {
		this.repaint();
	}

	public int getShade() {
		return (int) (MAX_TONE/(1.0d+(System.currentTimeMillis()-this.flashTime)/HALFTIME));
	}

	@Override
	public void paintComponent(Graphics g) {
		int tone = this.getShade();
		g.setColor(new Color(tone, tone, tone));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		boolean takeOne = false;
		if (!words.isEmpty()) {
			FadeWord[] array = words.toArray(new FadeWord[words.size()]);
			for (FadeWord w : array) {
				w.draw(g);
				if(w.getTone()<2) {
					takeOne = true;
				}
			}
		}
		if(takeOne) {
			words.pollFirst();
		}
		g.setFont(WORD_FONT);
		g.setColor(Color.GREEN);
		g.drawString(thisWord.toString(), wordX, wordY);
		if(showCount) {
			g.drawString(wordCount+"", (this.getWidth()-X_MARGIN)/2, (this.getHeight()-Y_MARGIN)/2);
		}
	}

	public BlackPanel() {
		super();
		thisWord = new StringBuffer("");
		wordX=0;
		wordY=0;
		words = new LinkedList<FadeWord>();
		this.addMouseListener(this.new ClickListener());
		wordCount = 0;
		showCount = false;
		currentPace = -1;
	}

	private class ClickListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			BlackPanel.this.requestFocusInWindow();
			BlackPanel.this.flash();
		}
	}

}
