import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.LinkedList;

import javax.swing.JPanel;

public class BlackPanel extends JPanel {

	public enum Mode {
		FORWARD,
		BACKWARD
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Font WORD_FONT = new Font("Dialog", Font.BOLD, 24);
	public static final int X_MARGIN = 100;
	public static final int Y_MARGIN = 20;
	public static final double MAX_TONE = 255;
	public static final double HALFTIME = 100;
	public static final double CHANGE_RATIO = 0.001;
	public static final long TIME_THRESHOLD = 30000;
	public static final long MIN_TIME = 500;
	public static final DecimalFormat SPEED_FORMAT = new DecimalFormat("0.0");
	private StringBuffer thisWord;
	private int wordX, wordY;
	private LinkedList<FadeWord> words;
	private long flashTime;
	private int wordCount;
	private boolean showCount;
	private double currentSpeed;
	private long lastTime;
	private Mode mode;

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
		mode = Mode.FORWARD;
	}

	public void breakWord() {
		if (thisWord.length()>0) {
			FadeWord word = new FadeWord(thisWord.toString(), wordX, wordY, this);
			words.add(word);
			thisWord.delete(0, thisWord.length());
			changeLoc();
			this.setWordCount(this.getWordCount() + 1);
			long time = System.currentTimeMillis();
			long diff = time-lastTime;
			if (diff<TIME_THRESHOLD) {
				currentSpeed = (60000 / (diff) * rate(diff)) + (currentSpeed * (1 - rate(diff)));
			}
			lastTime = time;
		}
	}

	public double rate(long diff) {
		return 1-Math.pow(1-CHANGE_RATIO, (diff));
	}
	
	public double getSpeed() {
		long time = System.currentTimeMillis();
		long diff = timeCorrect(time);
		if(diff<TIME_THRESHOLD) {
			return (60000 / (diff) * rate(diff)) + (currentSpeed * (1 - rate(diff)));
		}
		else {
			return 0;
		}
	}

	public long timeCorrect(long time) {
		return time - lastTime + MIN_TIME;
	}
	
	public void rewindLoc() {
		if(!words.isEmpty()) {
			FadeWord word = words.pollLast();
			wordX = word.getX();
			wordY = word.getY();
		}
		else {
			changeLoc();
		}
	}

	public void changeLoc() {
		wordX = (int) ((this.getWidth() - 2*X_MARGIN) * Math.random()) + X_MARGIN;
		wordY = (int) ((this.getHeight() - Y_MARGIN) * Math.random());
	}

	public void backspace() {
		this.mode = Mode.BACKWARD;
		if(thisWord.length()==0) {
			this.setWordCount(this.getWordCount()-1);
			return;
		}
		this.thisWord.deleteCharAt(thisWord.length()-1);
	}
	
	public void setWord(String word) {
		this.thisWord = new StringBuffer(word);
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
		int tone;
		switch(mode) {
		case FORWARD:
			tone = this.getShade();
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
				drawData(g);
			}
			break;
		case BACKWARD:
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.setFont(WORD_FONT);
			g.setColor(Color.RED);
			g.drawString(thisWord.toString(), wordX, wordY);
			if(showCount) {
				drawData(g);
			}
			break;
		}
	}

	public void drawData(Graphics g) {
		g.drawString("Words written: "+this.getWordCount(), (this.getWidth()-X_MARGIN)/2, (this.getHeight()-Y_MARGIN)/2);
		g.drawString("WPS: "+getSpeedString(), (this.getWidth()-X_MARGIN)/2, (this.getHeight()-Y_MARGIN)/2+30);
	}

	public String getSpeedString() {
		return SPEED_FORMAT.format(this.getSpeed());
	}

	public BlackPanel() {
		super();
		thisWord = new StringBuffer("");
		wordX=0;
		wordY=0;
		words = new LinkedList<FadeWord>();
		this.mode = Mode.FORWARD;
		this.addMouseListener(this.new ClickListener());
		wordCount = 0;
		showCount = false;
		currentSpeed = 0.0;
		System.out.println(60000/MIN_TIME*rate(MIN_TIME));
	}
	
	private class ClickListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			BlackPanel.this.requestFocusInWindow();
			BlackPanel.this.flash();
		}
	}

}
