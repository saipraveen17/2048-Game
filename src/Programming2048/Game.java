package Programming2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;


public class Game extends JPanel implements KeyListener, Runnable {
    public static final int width = 500;
    public static final int height = 540;
    public static final Font main = new Font("Bebas Neue Regular",Font.PLAIN, 28);
    private Thread game;
    private boolean running;
    private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    private GameBoard board;

    public Game() {
        setFocusable(true);
        setPreferredSize(new Dimension(width, height));
        addKeyListener(this);

        board = new GameBoard(width/2-GameBoard.boardWidth/2, height-GameBoard.boardHeight-25);
    }

    private void update() {
        board.update();
        Input.update();
    }

    private void render() {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0,0, width, height);
        board.render(g);
        g.dispose();

        Graphics2D g2d = (Graphics2D) getGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        Input.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Input.keyReleased(e);
    }

    @Override
    public void run() {
        int fps = 0, updates = 0;
        long fpsTimer = System.currentTimeMillis();
        double nsPerUPdate  = 1000000000.0/60;

        //last update time in nanoseconds
        double then = System.nanoTime();
        double unprocessed = 0;

        while(running) {
            boolean shouldRender = false;
            double now = System.nanoTime();
            unprocessed += (now-then)/nsPerUPdate;
            then = now;

            //update queue
            while (unprocessed >= 1) {
                updates++;
                update();
                unprocessed--;
                shouldRender = true;
            }

            //render
            if (shouldRender) {
                fps++;
                render();
                shouldRender = false;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //FPS Timer
        if(System.currentTimeMillis()-fpsTimer > 1000) {
            System.out.printf("%d fps %d updates", fps, updates);
            System.out.println();
            fps = 0;
            updates = 0;
            fpsTimer += 100;
        }
    }

    public synchronized void start() {
        if(running) return;
        running = true;
        game = new Thread(this, "game");
        game.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        System.exit(0);
    }
}