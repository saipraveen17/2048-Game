package Programming2048;

import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) {
        Game game = new Game();

        JFrame frame = new JFrame();
        frame.setResizable(false);
        frame.setTitle("2048");
        frame.add(game);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        game.start();
    }
}