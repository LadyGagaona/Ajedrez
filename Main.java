import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // ✅ Crear ventana
            JFrame f = new JFrame("Ajedrez");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(600, 700);
            f.setLayout(new BorderLayout());

            // ✅ Crear estado del juego y panel del tablero
            GameState game = new GameState();
            ChessBoardPanel board = new ChessBoardPanel(game);

            f.add(board, BorderLayout.CENTER);

            // ✅ Panel inferior para botones
            JPanel bottom = new JPanel();
            f.add(bottom, BorderLayout.SOUTH);

            // ✅ Botón de RESET
            JButton resetButton = new JButton("INGRESAR");
            resetButton.setFont(new Font("Arial", Font.PLAIN, 26));
            bottom.add(resetButton);

            resetButton.addActionListener(ev -> {
                // Crear opciones
                String[] options = {"INGRESAR POSICION INICUAL", "INGRESAR FEN"};
                int choice = JOptionPane.showOptionDialog(
                        f,
                        "¿Cómo deseas visualizar el tablero?",
                        "Reiniciar Tablero",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (choice == 0) {
                    // Reiniciar a posición inicial
                    game.resetBoard();  // Método sin parámetros
                    game.history.clear();
                    game.history.add(game.generateFEN());
                    board.repaint();
                } else if (choice == 1) {
                    // Solicitar FEN
                    String fen = JOptionPane.showInputDialog(f, "Ingresa el código FEN:");
                    if (fen != null && !fen.trim().isEmpty()) {
                        try {
                            game.loadFEN(fen); // Método que carga un FEN
                            game.history.clear();
                            game.history.add(fen);
                            board.repaint();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(f,
                                    "Error cargando FEN:\n" + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            // ✅ Botón de HISTORIAL
            JButton historyBtn = new JButton("HISTORIAL");
            historyBtn.setFont(new Font("Arial", Font.PLAIN, 26));
            bottom.add(historyBtn);

            historyBtn.addActionListener(e -> {

                JDialog dialog = new JDialog(f, "Historial de Jugadas", true);
                dialog.setSize(500, 400);
                dialog.setLocationRelativeTo(f);

                DefaultListModel<String> model = new DefaultListModel<>();
                for (int i = 0; i < game.history.size(); i++) {
                    model.addElement((i + 1) + ". " + game.history.get(i));
                }

                JList<String> list = new JList<>(model);
                list.setFont(new Font("Monospaced", Font.PLAIN, 14));

                JScrollPane scroll = new JScrollPane(list);
                dialog.add(scroll, BorderLayout.CENTER);

                JButton loadBtn = new JButton("Cargar posición");
                dialog.add(loadBtn, BorderLayout.SOUTH);

                // ✅ Cargar FEN seleccionada
                loadBtn.addActionListener(ev -> {
                    int index = list.getSelectedIndex();
                    if (index >= 0) {
                        String fen = game.history.get(index);
                        try {
                            game.resetBoard();
                            board.repaint();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Error cargando FEN:\n" + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                dialog.setVisible(true);
            });

            // ✅ Mostrar ventana
            f.setVisible(true);
        });
    }
}