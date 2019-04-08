import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

public class Application extends JPanel implements Runnable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Application application;
	
	private BlackPanel centerPanel;
	private StringBuffer text;
	private JTextArea textOutput;
	
	private int state;
	
	private String session;
	private int getState() {
		return state;
	}

	private void setState(int state) {
		this.state = state;
	}

	public static final int MODE_MAIN = 0;
	public static final int MODE_OUTPUT = 1;

	public Application() {
		text = new StringBuffer();
		centerPanel = new BlackPanel();
		textOutput = new JTextArea("Hello world");
		textOutput.setOpaque(true);
		
		
		this.setLayout(new BorderLayout());
		this.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setShowCount(true);
		
		Application.TypeListener listener = new TypeListener();
		centerPanel.addKeyListener(listener);
		
		Application.ToggleAction altAction = this.new ToggleAction();
		centerPanel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "alt");
		centerPanel.getActionMap().put("alt", altAction);
		textOutput.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "alt");
		textOutput.getActionMap().put("alt", altAction);
		
		this.state = MODE_MAIN;
		
		this.session = new Date().toString().replaceAll(":", " ");
		
		Thread thread = new Thread(this);
		thread.start();
	}

	public void addChar(char c) {
		if(c==' ') {
			centerPanel.breakWord();
			if(text.length()>0&&text.charAt(text.length()-1)!=' ') {
				text.append(' ');
			}
		}
		else {
			if(text.length()>0&&c=='\n') {
				centerPanel.breakWord();
				centerPanel.flash();
				if(text.charAt(text.length()-1)!='\n') {
					text.append("\n\n");
					this.save();
				}
			}
			else {
				centerPanel.addChar(c);
				text.append(c);
			}
		}
	}
	
	public void backspace() {
		if(text.length()==0) {
			return;
		}
		text.deleteCharAt(text.length()-1);
		if(text.length()==0) {
			centerPanel.backspace();
		}
		else if(text.charAt(text.length()-1)=='\n') {
			text.delete(text.length()-2, text.length());
			centerPanel.backspace();
			String[] words = text.toString().split("\\s+");
			centerPanel.setWord(words[words.length-1]);
			centerPanel.changeLoc();
		}
		else if(text.charAt(text.length()-1) == ' ') {
			text.deleteCharAt(text.length()-1);
			centerPanel.backspace();
			String[] words = text.toString().split("\\s+");
			centerPanel.setWord(words[words.length-1]);
			centerPanel.changeLoc();
		}
		else {
			centerPanel.backspace();
		}
	}
	
	public void recount() {
		Pattern whitespace = Pattern.compile("\\s+");
		Matcher matcher = whitespace.matcher(this.text);
		int count = 1;
		while(matcher.find()) {
			count++;
		}
		this.centerPanel.setWordCount(count);
	}
	
	public void save() {
		try {
			PrintWriter out = new PrintWriter(this.session+".txt");
			String[] lines = this.text.toString().split("\\n+");
			for(String string : lines) {
				out.println(string);
				out.println();
			}
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		application = new Application();
		JFrame frame = new JFrame("Black box");
		frame.setSize(700, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setContentPane(application);
		Thread thread = new Thread(application);
		thread.start();
	}
	
	private class TypeListener extends KeyAdapter {
		
		@Override
		public void keyTyped(KeyEvent e) {
			if(!(e.getKeyChar() == '\b')) {
				Application.this.addChar(e.getKeyChar());
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				Application.this.backspace();
				System.out.println("Backspacing");
			}
		}
	}
	
	private class ToggleAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			switch(Application.this.state){
			case MODE_MAIN:
				Application.this.remove(Application.this.centerPanel);
				Application.this.textOutput.setText(Application.this.text.toString());
				Application.this.add(Application.this.textOutput, BorderLayout.CENTER);
				Application.this.centerPanel.breakWord();
				Application.this.centerPanel.flash();
				Application.this.textOutput.requestFocus();
				Application.this.setState(MODE_OUTPUT);
				Application.this.revalidate();
				Application.this.textOutput.revalidate();
				Application.this.save();
				break;
			case MODE_OUTPUT:
				Application.this.remove(Application.this.textOutput);
				Application.this.text = new StringBuffer(Application.this.textOutput.getText());
				Application.this.add(Application.this.centerPanel, BorderLayout.CENTER);
				Application.this.centerPanel.flash();
				Application.this.recount();
				Application.this.centerPanel.requestFocus();
				Application.this.setState(MODE_MAIN);
				Application.this.revalidate();
				Application.this.centerPanel.revalidate();
				Application.this.save();
				break;
			}
		}
	}

	@Override
	public void run() {
		centerPanel.changeLoc();
		while(true) {
			this.centerPanel.doTick();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
