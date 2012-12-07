package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import jp.co.csk.vdm.toolbox.VDM.CGException;
import jp.co.csk.vdm.toolbox.VDM.UTIL;
import vdm.Game;
import vdm.Player;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String BOARD_TEXTURE = "board.png";
	private BufferedImage background = null;
	private Game game = null;
	private ArrayList<Polygon> blackUnplayed = null;
	private ArrayList<Polygon> whiteUnplayed = null;
	private HashMap<Vector<Integer>, Polygon> places = null;
	private Vector<Integer> selected = null;
	private Vector<Integer> originSelected = null;
	private MainFrame parent = null;
	private boolean hasMill = false;
	private boolean gameOver = false;
	
	public MainPanel(boolean db, Game game, MainFrame parent) throws CGException {
		super(db);
		try {
			background = ImageIO.read(getClass().getClassLoader().getResourceAsStream(BOARD_TEXTURE));
			this.game = game;
			this.parent = parent;
			setupUnplayedPieces();
			setupPiecePlaces();
			setupListeners();
			setPreferredSize(new Dimension(background.getWidth(), background.getHeight()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void reset() throws CGException {
		hasMill = false;
		gameOver = false;
		game.reset();
		selected = null;
		originSelected = null;
	}
	
	private void setupListeners() {
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					// Find what was the selected position
					findSelected(arg0.getPoint());
					
					// Return if game is over or nothing selected
					if(selected == null || gameOver)
						return;
					
					// Removing rival piece
					if(hasMill)
						removeMill();
					// Piece placing phase
					else if(game.isPhaseOne()) {
						placePiece();
					}
					// Moving phase
					else {
						movePiece();
					}
					repaint();
				} catch (CGException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void movePiece() throws CGException {
		if(originSelected == null) {
			if(UTIL.equals(game.getBoard().getPiece(selected), game.getCurrentPieceType()))
				originSelected = selected;
			else
				selected = null;
		} else {
			if(game.movable(originSelected, selected)) {
				hasMill = game.move(originSelected, selected);
				if(hasMill) {
					parent.setStatusMessage("You can remove one of your opponent's pieces.");
				} else {
					game.changePlayer();
					String msg = "Player " + game.getCurrentPieceType() + " turn. ";
					msg += game.isPhaseOne() ? "Place a piece in the board." : "Move one of your pieces.";
					parent.setStatusMessage(msg);
				}
			} else {
				parent.setStatusMessage("Can't move that piece.");
			}
			selected = null;
			originSelected = null;
		}
	}
	
	private void placePiece() throws CGException {
		// Piece can be placed
		if(game.puttable(selected)) {
			hasMill = game.put(selected);
			game.getCurrentPlayer().decUnplayedPieces();
			if(hasMill) {
				parent.setStatusMessage("You can remove one of your opponent's pieces.");
			} else {
				game.changePlayer();
				String msg = "Player " + game.getCurrentPieceType() + " turn. ";
				msg += game.isPhaseOne() ? "Place a piece in the board." : "Move one of your pieces.";
				parent.setStatusMessage(msg);
			}
		} else {
			parent.setStatusMessage("Can't place the piece in that place.");
		}
		selected = null;
	}
	
	private void removeMill() throws CGException {
		if(game.removable(selected)) {
			game.remove(selected);
			game.changePlayer();
			game.getCurrentPlayer().decTotalPieces();
			gameOver = game.getCurrentPlayer().lost();
			if(gameOver) {
				game.changePlayer();
				parent.setStatusMessage("Game over, player " + game.getCurrentPieceType() + " won.");
			} else {
				hasMill = false;
				String msg = "Player " + game.getCurrentPieceType() + " turn. ";
				msg += game.isPhaseOne() ? "Place a piece in the board." : "Move one of your pieces.";
				parent.setStatusMessage(msg);
			}
		} else {
			parent.setStatusMessage("Can't remove that piece.");
		}
		selected = null;
	}
	
	private void findSelected(Point p) {
		// Find selected coordinates
		for(Entry<Vector<Integer>, Polygon> entry : places.entrySet()) {
			if(entry.getValue().contains(p)) {
				selected = entry.getKey();
				break;
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupPiecePlaces() throws CGException {
		places = new HashMap<Vector<Integer>, Polygon>();
		HashMap map = game.getBoard().getBoard();
		Iterator it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry entry = (Entry) it.next();
			Vector<Integer> vec = (Vector<Integer>) entry.getKey();
			places.put(vec, drawCircle(50 + vec.get(1) * 77, vec.get(0) * 77, 25f, 6));
		}
	}
	
	private void setupUnplayedPieces() throws CGException {
		blackUnplayed = new ArrayList<Polygon>();
		whiteUnplayed = new ArrayList<Polygon>();
		for(int i = 0; i < Player.TOTAL_uPIECES; ++i) {
			whiteUnplayed.add(drawCircle(50, 100 + 50 * i, 25f, 6));
			blackUnplayed.add(drawCircle(background.getWidth() - 50, 100 + 50 * i, 25f, 6));
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		try {
			// Draw background
			g.drawImage(background, 0, 0, null);
			
			// Draw unplaced pieces
			paintUnplayedPieces(g);
			
			// Draw placed pieces
			paintPlayedPieces(g);
			
			// Draw selected piece
			Vector<Integer> sel = selected;
			if(sel != null && game.isPhaseTwo()) {
				g.setColor(Color.red);
				g.fillPolygon(places.get(sel));
			}
			g.dispose();
		} catch (CGException e) {
			e.printStackTrace();
		}
	}
	
	private void paintPlayedPieces(Graphics g) throws CGException {
		// Build stroke
		Graphics2D g2 = (Graphics2D) g;
		BasicStroke stroke = new BasicStroke(5f);
		g2.setStroke(stroke);
				
		for(Entry<Vector<Integer>, Polygon> entry : places.entrySet()) {
			if(UTIL.equals(game.getBoard().getPiece(entry.getKey()), new quotes.BLACK())) {
				g2.setColor(Color.black);
				g2.fillPolygon(entry.getValue());
				g2.setColor(Color.white);
				g2.drawPolygon(entry.getValue());
			} else if(UTIL.equals(game.getBoard().getPiece(entry.getKey()), new quotes.WHITE())) {
				g2.setColor(Color.white);
				g2.fillPolygon(entry.getValue());
				g2.setColor(Color.black);
				g2.drawPolygon(entry.getValue());
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void paintUnplayedPieces(Graphics g) throws CGException {
		// Build stroke
		Graphics2D g2 = (Graphics2D) g;
		BasicStroke stroke = new BasicStroke(5f);
		g2.setStroke(stroke);
		
		HashMap map = game.getUnplayedPieceCount();
		Iterator it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry entry = (Entry) it.next();
			if(UTIL.equals(entry.getKey(), new quotes.WHITE())) {
				for(int i = 0; i < (Integer) entry.getValue(); ++i) {
					g2.setColor(Color.white);
					g2.fillPolygon(whiteUnplayed.get(i));
					g2.setColor(Color.black);
					g2.drawPolygon(whiteUnplayed.get(i));
				}
			} else {
				g.setColor(Color.black);
				for(int i = 0; i < (Integer) entry.getValue(); ++i) {
					g2.setColor(Color.black);
					g2.fillPolygon(blackUnplayed.get(i));
					g2.setColor(Color.white);
					g2.drawPolygon(blackUnplayed.get(i));
				}
			}
		}
	}
	
	private static Polygon drawCircle(int x, int y, float radius, int sections) {
		Polygon poly = new Polygon();
		for(int i = 0; i < sections; ++i) {
			float angle = (float) ((2 * Math.PI / sections) * i);
			poly.addPoint((int) (radius * Math.cos(angle)) + x, (int) (radius * Math.sin(angle)) + y);
		}
		return poly;
	}

}
