import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GameState {

    public List<String> history = new ArrayList<>();
    public String[][] board = new String[8][8];
    public boolean whiteTurn = true;

    private static final Map<String, String> unicodeToType = new HashMap<>();

    static {
        unicodeToType.put("♙","P"); unicodeToType.put("♖","R");
        unicodeToType.put("♘","N"); unicodeToType.put("♗","B");
        unicodeToType.put("♕","Q"); unicodeToType.put("♔","K");

        unicodeToType.put("♟","p"); unicodeToType.put("♜","r");
        unicodeToType.put("♞","n"); unicodeToType.put("♝","b");
        unicodeToType.put("♛","q"); unicodeToType.put("♚","k");
    }

    public GameState() {
        resetBoard();
        history.clear();
        history.add(generateFEN());
    }

    public void resetBoard() {
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
        history.clear();
        history.add(generateFEN());
    }

    // --- helpers ---
    public boolean isWhite(String p) { return p.matches("[♙♖♘♗♕♔]"); }
    public boolean isBlack(String p) { return p.matches("[♟♜♞♝♛♚]"); }

    private boolean isEnemy(int r1,int c1,int r2,int c2){
        return isWhite(board[r1][c1]) && isBlack(board[r2][c2])
                || isBlack(board[r1][c1]) && isWhite(board[r2][c2]);
    }

    private boolean inBounds(int r,int c){
        return r>=0 && r<8 && c>=0 && c<8;
    }

    public String generateFEN() {
        StringBuilder sb = new StringBuilder();

        // Recorremos filas de 0..7 donde 0 corresponde a la fila 8 (rank 8)
        for (int r = 0; r < 8; r++) {
            int empty = 0;
            for (int c = 0; c < 8; c++) {
                String p = board[r][c];
                if (p == null || p.isEmpty()) {
                    empty++;
                } else {
                    if (empty > 0) {
                        sb.append(empty);
                        empty = 0;
                    }
                    String letter = unicodeToType.get(p); // usa el mapa unicodeToType que debes tener
                    if (letter == null) {
                        // alternativa: tratar como vacío en vez de lanzar excepción:
                        // sb.append("?");
                        throw new IllegalStateException("generateFEN: pieza desconocida -> '" + p + "' en " + r + "," + c);
                    }
                    sb.append(letter);
                }
            }
            if (empty > 0) sb.append(empty);
            if (r < 7) sb.append('/');
        }

        // Turno y campos placeholder (ajústalos si manejas castling/en passant/half/full)
        sb.append(' ');
        sb.append(whiteTurn ? 'w' : 'b');
        sb.append(" - - 0 1");

        return sb.toString();
    }

    // ==========================================================
    //                      MOVIMIENTOS LEGALES
    // ==========================================================

    public List<int[]> getLegalMoves(int r, int c) {
        return getMoves(r, c, true);
    }

    private List<int[]> getMoves(int r, int c, boolean respectTurn) {
        List<int[]> out = new ArrayList<>();
        String p = board[r][c];
        if (p == null || p.isEmpty()) return out;

        boolean whitePiece = isWhite(p);
        if (respectTurn) {
            if (whiteTurn && !whitePiece) return out;
            if (!whiteTurn && whitePiece) return out;
        }

        String type = unicodeToType.get(p);
        if (type == null) return out;
        type = type.toUpperCase();

        switch (type) {
            case "P": addPawnMoves(r, c, out); break;
            case "N": addKnightMoves(r, c, out); break;
            case "B": addBishopMoves(r, c, out); break;
            case "R": addRookMoves(r, c, out); break;
            case "Q": addQueenMoves(r, c, out); break;
            case "K": addKingMoves(r, c, out); break;
        }
        return out;
    }

    public void loadFEN(String fen) throws Exception {
        if (fen == null || fen.isEmpty()) {
            throw new Exception("FEN vacío");
        }

        String[] parts = fen.split(" ");
        if (parts.length < 1) {
            throw new Exception("FEN inválido");
        }

        String[] rows = parts[0].split("/");
        if (rows.length != 8) {
            throw new Exception("FEN inválido: número de filas incorrecto");
        }

        // Crear mapa FEN -> símbolo Unicode según color
        Map<String, String> fenToWhite = new HashMap<>();
        Map<String, String> fenToBlack = new HashMap<>();
        for (Map.Entry<String, String> entry : unicodeToType.entrySet()) {
            String unicode = entry.getKey();
            String fenLetter = entry.getValue();
            if (isWhite(unicode)) {
                fenToWhite.put(fenLetter.toUpperCase(), unicode); // blanco
            } else {
                fenToBlack.put(fenLetter.toLowerCase(), unicode); // negro
            }
        }

        for (int i = 0; i < 8; i++) {
            String row = rows[i];
            int col = 0;

            for (char c : row.toCharArray()) {
                if (Character.isDigit(c)) {
                    int empty = Character.getNumericValue(c);
                    for (int j = 0; j < empty; j++) {
                        board[i][col] = "";
                        col++;
                    }
                } else {
                    String key = String.valueOf(c);
                    String pieceSymbol;

                    if (Character.isUpperCase(c)) {
                        pieceSymbol = fenToWhite.get(key);
                    } else {
                        pieceSymbol = fenToBlack.get(key);
                    }

                    if (pieceSymbol == null) {
                        throw new Exception("FEN inválido: pieza desconocida -> " + c);
                    }

                    board[i][col] = pieceSymbol;
                    col++;
                }
            }

            if (col != 8) {
                throw new Exception("FEN inválido: número de columnas incorrecto en fila " + (i + 1));
            }
        }

        // Turno (w/b)
        whiteTurn = parts.length > 1 && parts[1].equals("w");

        // Guardar FEN en historial
        history.add(fen);
    }

    // -------------------- PAWN ---------------------
    private void addPawnMoves(int r,int c,List<int[]> m){
        boolean white = isWhite(board[r][c]);
        int dir = white ? -1 : 1;

        int nr = r + dir;

        // avance normal
        if (inBounds(nr,c) && board[nr][c].isEmpty()) {
            m.add(new int[]{nr,c});

            // avance doble
            int startRank = white ? 6 : 1;
            if (r == startRank) {
                int nr2 = r + dir*2;
                if (board[nr2][c].isEmpty())
                    m.add(new int[]{nr2,c});
            }
        }

        // capturas
        int[] dc = {-1,1};
        for(int d : dc){
            int nc = c + d;
            if (inBounds(nr,nc) && !board[nr][nc].isEmpty() && isEnemy(r,c,nr,nc)){
                m.add(new int[]{nr,nc});
            }
        }
    }

    // -------------------- KNIGHT ---------------------
    private void addKnightMoves(int r,int c,List<int[]> m){
        int[][] jump = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for(int[] j : jump){
            int nr = r+j[0], nc = c+j[1];
            if(!inBounds(nr,nc)) continue;
            if(board[nr][nc].isEmpty() || isEnemy(r,c,nr,nc))
                m.add(new int[]{nr,nc});
        }
    }

    // -------------------- BISHOP ---------------------
    private void addBishopMoves(int r,int c,List<int[]> m){
        int[][] dirs = {{1,1},{1,-1},{-1,1},{-1,-1}};
        for(int[] d:dirs){
            int nr=r+d[0], nc=c+d[1];
            while(inBounds(nr,nc)){
                if(board[nr][nc].isEmpty()){
                    m.add(new int[]{nr,nc});
                } else {
                    if(isEnemy(r,c,nr,nc)) m.add(new int[]{nr,nc});
                    break;
                }
                nr+=d[0]; nc+=d[1];
            }
        }
    }

    // -------------------- ROOK ---------------------
    private void addRookMoves(int r,int c,List<int[]> m){
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for(int[] d:dirs){
            int nr=r+d[0], nc=c+d[1];
            while(inBounds(nr,nc)){
                if(board[nr][nc].isEmpty()){
                    m.add(new int[]{nr,nc});
                } else {
                    if(isEnemy(r,c,nr,nc)) m.add(new int[]{nr,nc});
                    break;
                }
                nr+=d[0]; nc+=d[1];
            }
        }
    }

    // -------------------- QUEEN ---------------------
    private void addQueenMoves(int r,int c,List<int[]> m){
        addRookMoves(r,c,m);
        addBishopMoves(r,c,m);
    }

    // -------------------- KING ---------------------
    private void addKingMoves(int r,int c,List<int[]> m){
        int[][] dirs = {
                {1,0},{-1,0},{0,1},{0,-1},
                {1,1},{1,-1},{-1,1},{-1,-1}
        };
        for(int[] d:dirs){
            int nr=r+d[0], nc=c+d[1];
            if(!inBounds(nr,nc)) continue;
            if(board[nr][nc].isEmpty() || isEnemy(r,c,nr,nc))
                m.add(new int[]{nr,nc});
        }
    }

    public boolean isInCheck(boolean whiteKing) {
        String king = whiteKing ? "♔" : "♚";
        int kr = -1, kc = -1;

        // encontrar rey
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c].equals(king)) {
                    kr = r; kc = c;
                    break;
                }
            }
            if (kr != -1) break;
        }

        if (kr == -1) return false; // rey no existe en tablero

        // recorrer piezas enemigas y ver si atacan la casilla del rey
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String p = board[r][c];
                if (p == null || p.isEmpty()) continue;

                // si p es enemiga respecto al color del rey buscado
                if (whiteKing && isBlack(p) || (!whiteKing && isWhite(p))) {
                    // generar movimientos SIN respetar el turno global
                    List<int[]> moves = getMoves(r, c, false);
                    for (int[] mv : moves) {
                        if (mv[0] == kr && mv[1] == kc) return true;
                    }
                }
            }
        }
        return false;
    }

}
