package Game;

import javax.swing.*;
import java.awt.*;

public class View extends JFrame implements Observer{

    private final JPanel boardPanel;
    private final Model model;
    private final Controller controller;

    public static final int SQUARE_SIZE = 100;
    public static final String asset_path = "src/Game/Assets";

    public View(Model model, Controller controller) {
        this.model = model;
        this.controller = controller;

        setTitle("Bomboclaut");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        boardPanel = new JPanel(new GridLayout(Constants.BOARD_SIZE, Constants.BOARD_SIZE));
        boardPanel.setPreferredSize(new Dimension(800, 800));


        add(boardPanel);

        createAndShowGUI();
        updateBoard();

    }

    private void createAndShowGUI() {
        for (int row = 0; row < Constants.BOARD_SIZE; row++) {
            for (int col = 0; col < Constants.BOARD_SIZE; col++) {
                JButton square = new JButton();
                square.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.DARK_GRAY);
                int finalRow = row;
                int finalCol = col;
                square.setPreferredSize(new Dimension(SQUARE_SIZE, SQUARE_SIZE)); // Increase the size of the square
                square.addActionListener(e -> controller.handleMouseClick(finalRow, finalCol)); // Use ActionListener
                square.setBorderPainted(false);
                square.setFocusPainted(false);  // Disable the focus border
                square.setOpaque(true);

                boardPanel.add(square);
            }
        }

        setVisible(true);
    }

    private void updateBoard() {
        for (int row = 0; row < Constants.BOARD_SIZE; row++) {
            for (int col = 0; col < Constants.BOARD_SIZE; col++) {
                JButton square = (JButton) boardPanel.getComponent(row * Constants.BOARD_SIZE + col);
                // Clear the previous icon
                square.setIcon(null);
                int position = row * Constants.BOARD_SIZE + col;
                int piece = model.getPiece(position);
                if (piece == Constants.EMPTY){
                    square.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.DARK_GRAY);
                }
                else if (piece == Constants.WALL) {
                    square.setBackground(Color.BLACK);
                } else {
                    ImageIcon pieceIcon = getImageIcon(piece, square);
                    // Set the icon to the button
                    square.setIcon(pieceIcon);
                    square.setHorizontalAlignment(SwingConstants.CENTER);
                    square.setVerticalAlignment(SwingConstants.CENTER);
                }
                square.setText(row * Constants.BOARD_SIZE + col + " ");
                Font currentFont = square.getFont();
                Font newFont = currentFont.deriveFont(currentFont.getStyle(), 20f); // 20f is the new font size
                square.setFont(newFont);            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private static ImageIcon getImageIcon(int piece, JButton square) {
        String imagePath = piece == Constants.WHITE ? asset_path+"/white_queen.png" : asset_path+"/black_queen.png";
        ImageIcon pieceIcon = new ImageIcon(imagePath);

        // Scale the image to fit the button
        Image image = pieceIcon.getImage();
        Image scaledImage = image.getScaledInstance((int) (square.getWidth() * 0.8), (int) (square.getHeight() * 0.8), Image.SCALE_SMOOTH);
        pieceIcon = new ImageIcon(scaledImage);
        return pieceIcon;
    }


    public void gameOver(){
        for (int row = 0; row < Constants.BOARD_SIZE; row++) {
            for (int col = 0; col < Constants.BOARD_SIZE; col++) {
                JButton square = (JButton) boardPanel.getComponent(row * Constants.BOARD_SIZE + col);
                square.setEnabled(false);
            }
        }
    }


    @Override
    public void onBoardChanged() {
        updateBoard();
    }

    @Override
    public void onGameOver() {
        gameOver();
    }
}
