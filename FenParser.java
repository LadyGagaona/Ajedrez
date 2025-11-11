import java.util.HashMap;
import java.util.Map;

public class FenParser {

    private static final Map<Character, String> unicodePieces = new HashMap<>();

    static {
        unicodePieces.put('p', "♟"); unicodePieces.put('r', "♜");
        unicodePieces.put('n', "♞"); unicodePieces.put('b', "♝");
        unicodePieces.put('q', "♛"); unicodePieces.put('k', "♚");

        unicodePieces.put('P', "♙"); unicodePieces.put('R', "♖");
        unicodePieces.put('N', "♘"); unicodePieces.put('B', "♗");
        unicodePieces.put('Q', "♕"); unicodePieces.put('K', "♔");
    }

    public static String[][] parseBoard(String fen) throws Exception {

        String[] parts = fen.split(" ");
        if (parts.length < 1)
            throw new Exception("La cadena FEN está incompleta.");

        String placement = parts[0];
        String[] ranks = placement.split("/");

        if (ranks.length != 8)
            throw new Exception("El tablero debe tener 8 filas separadas por '/'.");

        String[][] board = new String[8][8];

        for (int row = 0; row < 8; row++) {
            String rank = ranks[row];
            int col = 0;

            for (char c : rank.toCharArray()) {

                if (Character.isDigit(c)) {
                    int empty = c - '0';
                    if (empty < 1 || empty > 8)
                        throw new Exception("Número inválido en una fila: " + c);

                    for (int k = 0; k < empty; k++) {
                        if (col >= 8)
                            throw new Exception("Fila con más de 8 columnas.");
                        board[row][col++] = "";
                    }

                } else if (unicodePieces.containsKey(c)) {
                    if (col >= 8)
                        throw new Exception("Fila con más de 8 columnas.");
                    board[row][col++] = unicodePieces.get(c);

                } else {
                    throw new Exception("Símbolo inválido encontrado: " + c);
                }
            }

            if (col != 8)
                throw new Exception("La fila " + (row + 1) + " no completa 8 columnas.");
        }

        return board;
    }

    public class GameState {

        public String[][] board = new String[8][8];
        public boolean whiteTurn = true; // true = blancas, false = negras

        public GameState() {
            resetBoard();
        }

        public void resetBoard() {
            resetBoard(null);
        }

        public void resetBoard(String fen) {
            if (fen == null || fen.isBlank()) {
                // Tablero inicial estándar
                board = new String[][]{
                        {"♜","♞","♝","♛","♚","♝","♞","♜"},
                        {"♟","♟","♟","♟","♟","♟","♟","♟"},
                        {"","","","","","","",""},
                        {"","","","","","","",""},
                        {"","","","","","","",""},
                        {"","","","","","","",""},
                        {"♙","♙","♙","♙","♙","♙","♙","♙"},
                        {"♖","♘","♗","♕","♔","♗","♘","♖"}
                };
                whiteTurn = true;
                return;
            }

            try {
                board = parseBoard(fen);
                whiteTurn = fen.contains(" w "); // turno indicado por FEN
            } catch (Exception e) {
                System.out.println("Error al aplicar FEN: " + e.getMessage());
                resetBoard(); // fallback
            }
        }

        }
}