package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import jp.co.csk.vdm.toolbox.VDM.CGException;
import vdm.Game;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JLabel statusBar = null;
	private MainPanel mainPanel = null;
	private Game game = null;
	private static final String FRAME_TITLE = "Nine Men's Morris";
	
	public MainFrame(Game game) throws CGException {
		this.game = game;
		setupFrame();
		setupMenuBar();
		setupMainPanel();
		setupStatusBar();
	}
	
	private void setupFrame() {
		// Window behavior and look settings
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle(FRAME_TITLE);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
	}
	
	private void setupMenuBar() {
		// Define game menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Game");
		JMenuItem restart = new JMenuItem("Restart");
		menu.add(restart);
		restart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					mainPanel.reset();
					mainPanel.repaint();
				} catch (CGException e) {
					e.printStackTrace();
				}
			}
		});
		JMenuItem close = new JMenuItem("Close");
		menu.add(close);
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.dispose();
				System.exit(0);
			}
		});
		
		// Add menus
		menuBar.add(menu);
		setJMenuBar(menuBar);
	}
	
	private void setupMainPanel() throws CGException {
		mainPanel = new MainPanel(true, game, this);
		getContentPane().add(mainPanel);
        pack();
	}
	
	public void setStatusMessage(String message) {
		statusBar.setText(message);
		statusBar.repaint();
	}
	
	private void setupStatusBar() throws CGException {
		statusBar = new JLabel("Player " + game.getCurrentPieceType() + " turn. Place a piece in the board.");
		getContentPane().add(statusBar, BorderLayout.SOUTH);
	}
	
	public static void main(String[] args) throws CGException {
		Game game = new Game();
		new MainFrame(game).setVisible(true);
	}

}
