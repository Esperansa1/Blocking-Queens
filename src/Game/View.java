package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class View extends JFrame implements Observer{

    private JPanel boardPanel;
    private Model model;
    private Controller controller;


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
                square.setPreferredSize(new Dimension(100, 100)); // Increase the size of the square
                square.addActionListener(e -> controller.handleMouseClick(finalRow, finalCol)); // Use ActionListener
                square.setBorderPainted(false);


                boardPanel.add(square);
            }
        }

        setVisible(true);
    }

    private void updateBoard() {
        for (int row = 0; row < Constants.BOARD_SIZE; row++) {
            for (int col = 0; col < Constants.BOARD_SIZE; col++) {
                JButton square = (JButton) boardPanel.getComponent(row * Constants.BOARD_SIZE + col);
                square.removeAll();
                int position = row * Constants.BOARD_SIZE + col;
                int piece = model.getPiece(position);
                if(piece == Constants.EMPTY) continue;
                if(piece == Constants.WALL){
                    square.setBackground(Color.BLACK);
                }
                else{
                    JLabel label = new JLabel(piece == Constants.WHITE ? " W" : "  B");
                    label.setForeground(Color.RED);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setVerticalAlignment(SwingConstants.CENTER);
                    label.setFont(label.getFont().deriveFont(40f));

                    square.add(label);
                }
            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();
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
