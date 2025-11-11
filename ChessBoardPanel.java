import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChessBoardPanel extends JPanel {

    private final GameState game;
    private int selectedRow = -1, selectedCol = -1;
    private List<int[]> legalMoves = List.of();

    public ChessBoardPanel(GameState g) {
        this.game = g;

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int size = getWidth() / 8;
                int col = e.getX() / size;
                int row = e.getY() / size;

                // ------------------------------
                //  INTENTAR SELECCIONAR UNA PIEZA
                // ------------------------------
                if (selectedRow == -1) {

                    String p = game.board[row][col];

                    // No hay pieza
                    if (p.isEmpty()) return;

                    // PIEZA NO CORRESPONDE AL TURNO
                    if (game.whiteTurn && game.isBlack(p)) {
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ChessBoardPanel.this),
                                "Es turno de BLANCAS.",
                                "Turno incorrecto", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    if (!game.whiteTurn && game.isWhite(p)) {
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ChessBoardPanel.this),
                                "Es turno de NEGRAS.",
                                "Turno incorrecto", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // SI ESTÁS EN JAQUE → impedir mover la pieza (solo alerta)
                    if (game.isInCheck(game.whiteTurn)) {
                        JOptionPane.showMessageDialog(ChessBoardPanel.this,
                                "Estás en JAQUE.",
                                "Jaque", JOptionPane.WARNING_MESSAGE);
                    }

                    // SELECCIONAR
                    selectedRow = row;
                    selectedCol = col;
                    legalMoves = game.getLegalMoves(row, col);
                    repaint();
                }

                // ------------------------------
                //  INTENTAR MOVER UNA PIEZA
                // ------------------------------
                else {
                    if (isLegal(row, col)) {

                        // ¿SE COMERÁ AL REY?
                        String destino = game.board[row][col];
                        if (destino.equals("♔") || destino.equals("♚")) {
                            String ganador = game.whiteTurn ? "Blancas" : "Negras";
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ChessBoardPanel.this),
                                    "¡Jaque Mate a favor de " + ganador + "!",
                                    "JAQUE MATE", JOptionPane.INFORMATION_MESSAGE);
                        }

                        // MOVER
                        game.board[row][col] = game.board[selectedRow][selectedCol];
                        game.board[selectedRow][selectedCol] = "";

                        // CAMBIAR TURNO
                        game.whiteTurn = !game.whiteTurn;
                        game.history.add(game.generateFEN());
                    }

                    selectedRow = -1;
                    selectedCol = -1;
                    legalMoves = List.of();
                    repaint();
                }
            }

        });
    }

    private boolean isLegal(int r,int c){
        for(int[] m : legalMoves)
            if(m[0]==r && m[1]==c) return true;
        return false;
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        int size = getWidth()/8;

        for(int r=0;r<8;r++){
            for(int c=0;c<8;c++){

                boolean light = (r+c)%2==0;
                g.setColor(light?new Color(240,217,181):new Color(181,136,99));
                g.fillRect(c*size, r*size, size, size);

                if(isLegal(r,c)){
                    g.setColor(new Color(0,255,0,120));
                    g.fillRect(c*size, r*size, size, size);
                }

                String piece = game.board[r][c];
                if(!piece.isEmpty()){
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Serif", Font.PLAIN, size-10));
                    FontMetrics fm = g.getFontMetrics();
                    int w=fm.stringWidth(piece);
                    int h=fm.getAscent();
                    g.drawString(piece, c*size+(size-w)/2, r*size+(size+h)/2);
                }
            }
        }
    }
}