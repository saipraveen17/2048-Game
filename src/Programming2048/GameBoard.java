package Programming2048;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

public class GameBoard {

    public static final int rows = 4;
    public static final int cols = 4;

    public final int startingTiles = 2;
    private Tile[][] board;
    private boolean dead;
    private boolean won;
    private BufferedImage gameBoard;
    private BufferedImage finalBoard;
    private int x;
    private int y;
    private int score = 0;
    private int highScore = 0;
    private Font scoreFont;

    private static int spacing = 10;
    public static int boardWidth = (cols+1)*spacing+cols*Tile.width;
    public static int boardHeight = (rows+1)*spacing+rows*Tile.height;
    private boolean hasStarted;

    private long elapsedMS;
    private long fastestMS;
    private long startTime;
    private String formattedTime = "00:00:000";

    //Saving
    private String saveDataPath;
    private String fileName = "SaveData";

    public GameBoard(int x, int y) {
        try {
            saveDataPath = GameBoard.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            //saveDataPath = System.getProperty("user.home")+"\\foldername";
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        scoreFont = Game.main.deriveFont(24f);
        this.x = x;
        this.y = y;
        board = new Tile[rows][cols];
        gameBoard = new BufferedImage(boardWidth, boardHeight, BufferedImage.TYPE_INT_RGB);
        finalBoard = new BufferedImage(boardWidth, boardHeight, BufferedImage.TYPE_INT_RGB);
        startTime = System.nanoTime();

        loadHighScore();
        createBoardImage();
        start();
    }

    private void createSaveData() {
        try {
            File file = new File(saveDataPath, fileName);

            FileWriter output = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(output);
            writer.write(""+0);
            writer.newLine();
            writer.write(""+Integer.MAX_VALUE);
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadHighScore() {
        try {
            File file = new File(saveDataPath, fileName);
            if(!file.isFile()) {
                createSaveData();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            highScore = Integer.parseInt(reader.readLine());
            fastestMS = Long.parseLong(reader.readLine());
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHighScore() {
        FileWriter output = null;
        try {
            File file = new File(saveDataPath, fileName);
            output = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(output);

            writer.write((""+highScore));
            writer.newLine();

            if(elapsedMS<=fastestMS&&won) {
                writer.write(""+elapsedMS);
            }
            else {
                writer.write(""+fastestMS);
            }
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createBoardImage() {
        Graphics2D g = (Graphics2D) gameBoard.getGraphics();
        g.setColor(Color.darkGray);
        g.fillRect(0,0,boardWidth,boardHeight);
        g.setColor(Color.lightGray);

        for(int row = 0; row<rows; row++) {
            for(int col = 0; col<cols; col++) {
                int x = spacing*(col+1) + Tile.width*col;
                int y = spacing*(row+1) + Tile.height*row;
                g.fillRoundRect(x,y,Tile.width,Tile.height,Tile.arcWidth,Tile.arcHeight);
            }
        }
    }

    private void start() {
        for(int i=0; i<startingTiles; i++) {
            spawnRandom();
        }
    }

    private void spawnRandom() {
        Random random = new Random();
        boolean notValid = true;

        while (notValid) {
            int location = random.nextInt(rows*cols);
            int row = location/rows;
            int col = location%cols;
            Tile current = board[row][col];
            if(current==null) {
                int value  = random.nextInt(10)<9?2:4;
                Tile tile = new Tile(value, getTileX(col), getTileY(row));
                board[row][col] = tile;
                notValid = false;
            }
        }
    }

    public int getTileX(int col) {
        return spacing+col*Tile.width+col*spacing;
    }

    public int getTileY(int row) {
        return spacing+row*Tile.height+row*spacing;
    }

    public void render(Graphics2D g) {
        Graphics2D g2d = (Graphics2D) finalBoard.getGraphics();
        g2d.drawImage(gameBoard,0,0,null);

        //draw tiles
        for(int row=0; row<rows; row++) {
            for(int col = 0; col<cols; col++) {
                Tile current = board[row][col];
                if(current==null) continue;
                current.render(g2d);
            }
        }

        g.drawImage(finalBoard,x,y,null);
        g2d.dispose();

        g.setColor(Color.lightGray);
        g.setFont(scoreFont);
        g.drawString(""+score,30,40);
        g.setColor(Color.red);
        g.drawString("Best: "+highScore, Game.width-Tile.getMessageWidth("Best: "+highScore, scoreFont, g)-20, 40);
//        g.setColor(Color.black);
//        g.drawString("Time: "+formattedTime, 30, 90);
//        g.setColor(Color.red);
//        g.drawString("Fastest: "+formatTime(fastestMS), Game.width-Tile.getMessageWidth("Fastest: "+formatTime(fastestMS), scoreFont, g), 90);
    }

    public void update() {
        if(!won && !dead) {
            if(hasStarted) {
                elapsedMS = (System.nanoTime()-startTime)/1000000;
                formattedTime = formatTime(elapsedMS);
            }
            else {
                startTime = System.nanoTime();
            }
        }
        checkKeys();

        if(score>=highScore) {
            highScore = score;
        }

        for(int row = 0; row<rows; row++) {
            for(int col=0; col<cols; col++) {
                Tile current = board[row][col];
                if(current==null) continue;
                current.update();
                //reset position
                resetPosition(current,row,col);
                if(current.getValue()==2048) {
                    won = true;
                }
            }
        }
    }

    private String formatTime(long millis) {
        String formattedTime;

        String hourFormat = "";
        int hours = (int)(millis/3600000);
        if(hours>=1) {
            millis -= hours*3600000;
            if(hours<10) {
                hourFormat = "0"+hours;
            }
            else {
                hourFormat = "0"+hours;
            }
            hourFormat += ":";
        }

        String minuteFormat;
        int minutes = (int)(millis/60000);
        if(minutes>=1) {
            millis -= minutes*60000;
            if(minutes<10) {
                minuteFormat = "0"+minutes;
            }
            else {
                minuteFormat = ""+minutes;
            }
        }
        else {
            minuteFormat = "00";
        }

        String secondFormat;
        int seconds = (int)(millis/1000);
        if(seconds>=1) {
            millis -= seconds*1000;
            if(seconds<10) {
                secondFormat = "0"+seconds;
            }
            else {
                secondFormat = ""+seconds;
            }
        }
        else {
            secondFormat = "00";
        }

        String milliFormat;
        if(millis>99) {
            milliFormat = ""+millis;
        }
        else if(millis>9) {
            milliFormat = "0"+millis;
        }
        else {
            milliFormat = "00"+millis;
        }

        formattedTime = hourFormat+minuteFormat+":"+secondFormat+":"+milliFormat;
        return formattedTime;
    }

    private void resetPosition(Tile current, int row, int col) {
        if(current==null) return;
        int x = getTileX(col);
        int y = getTileY(row);
        int distX = current.getX()-x;
        int distY = current.getY()-y;

        if(Math.abs(distX)<Tile.slideSpeed) {
            current.setX(current.getX()-distX);
        }

        if(Math.abs(distY)<Tile.slideSpeed) {
            current.setY(current.getY()-distY);
        }

        if(distX<0) {
            current.setX(current.getX()+Tile.slideSpeed);
        }
        if(distY<0) {
            current.setY(current.getY()+Tile.slideSpeed);
        }
        if(distX>0) {
            current.setX(current.getX()-Tile.slideSpeed);
        }
        if(distY>0) {
            current.setY(current.getY()-Tile.slideSpeed);
        }
    }

    private boolean move(int row, int col, int horizontalDirection, int verticalDirection, Direction dir) {
        boolean canMove = false;
        Tile current = board[row][col];
        if(current==null) return false;
        boolean move = true;
        int newCol = col;
        int newRow = row;
        while (move) {
            newCol += horizontalDirection;
            newRow += verticalDirection;
            if(checkOutOfBounds(dir, newRow, newCol)) break;
            if(board[newRow][newCol] == null) {
                board[newRow][newCol] = current;
                board[newRow-verticalDirection][newCol-horizontalDirection] = null;
                board[newRow][newCol].setSlideTo(new Point(newRow,newCol));
                canMove = true;
            }
            else if(board[newRow][newCol].getValue()==current.getValue()&&board[newRow][newCol].canCombine()) {
                board[newRow][newCol].setCanCombine(false);
                board[newRow][newCol].setValue(board[newRow][newCol].getValue()*2);
                canMove =true;
                board[newRow-verticalDirection][newCol-horizontalDirection] = null;
                board[newRow][newCol].setSlideTo(new Point(newRow, newCol));
                board[newRow][newCol].setCombineAnimation(true);
                score += board[newRow][newCol].getValue();
            }
            else {
                move = false;
            }
        }
        return canMove;
    }

    private boolean checkOutOfBounds(Direction dir, int row, int col) {
        if(dir==Direction.left) {
            return col<0;
        }
        else if(dir==Direction.right) {
            return col>cols-1;
        }
        else if(dir==Direction.up) {
            return row<0;
        }
        else if(dir==Direction.down) {
            return row>rows-1;
        }
        return false;
    }

    private void moveTiles(Direction dir) {
        boolean canMove = false;
        int horizontalDirection = 0;
        int verticalDirection = 0;

        if(dir==Direction.left) {
            horizontalDirection =-1;
            for(int row = 0; row<rows; row++) {
                for(int col=0; col<cols; col++) {
                    if(!canMove) {
                        canMove = move(row, col, horizontalDirection, verticalDirection, dir);
                    }
                    else move(row, col, horizontalDirection, verticalDirection, dir);
                }
            }
        }
        else if(dir==Direction.right) {
            horizontalDirection =1;
            for(int row = 0; row<rows; row++) {
                for(int col=cols-1; col>=0; col--) {
                    if(!canMove) {
                        canMove = move(row, col, horizontalDirection, verticalDirection, dir);
                    }
                    else move(row, col, horizontalDirection, verticalDirection, dir);
                }
            }
        }
        else if(dir==Direction.up) {
            verticalDirection =-1;
            for(int row = 0; row<rows; row++) {
                for(int col=0; col<cols; col++) {
                    if(!canMove) {
                        canMove = move(row, col, horizontalDirection, verticalDirection, dir);
                    }
                    else move(row, col, horizontalDirection, verticalDirection, dir);
                }
            }
        }
        else if(dir==Direction.down) {
            verticalDirection = 1;
            for(int row = rows-1; row>=0; row--) {
                for(int col=0; col<cols; col++) {
                    if(!canMove) {
                        canMove = move(row, col, horizontalDirection, verticalDirection, dir);
                    }
                    else move(row, col, horizontalDirection, verticalDirection, dir);
                }
            }
        }
        else {
            System.out.println(dir+" is not valid direction.");
        }

        for(int row = 0; row<rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile current = board[row][col];
                if(current==null) continue;
                current.setCanCombine(true);
            }
        }

        if(canMove) {
            spawnRandom();
            checkDead();

        }
    }

    private void checkDead() {
        for(int row=0; row<rows; row++) {
            for(int col=0; col<cols; col++) {
                if(board[row][col]==null) return;;
                if(checkSurroundingTiles(row, col, board[row][col])) {
                    return;
                }
            }
        }
        dead = true;
        if(score>=highScore) highScore = score;
        setHighScore();
    }

    private boolean checkSurroundingTiles(int row, int col, Tile current) {
        if(row>0) {
            Tile check = board[row-1][col];
            if(check==null) return true;
            if(current.getValue()==check.getValue()) return true;
        }
        if(row<rows-1) {
            Tile check = board[row+1][col];
            if(check==null) return true;
            if(current.getValue() == check.getValue()) return true;
        }
        if(col>0) {
            Tile check = board[row][col-1];
            if(check==null) return true;
            if(current.getValue()==check.getValue()) return true;
        }
        if(col<cols-1) {
            Tile check = board[row][col+1];
            if(check==null) return true;
            if(current.getValue() == check.getValue()) return true;
        }
        return false;
    }

    private void checkKeys() {
        if(Input.typed(KeyEvent.VK_LEFT)) {
            moveTiles(Direction.left);
            if(!hasStarted) hasStarted = true;
        }
        if(Input.typed(KeyEvent.VK_RIGHT)) {
            moveTiles(Direction.right);
            if(!hasStarted) hasStarted = true;
        }
        if(Input.typed(KeyEvent.VK_UP)) {
            moveTiles(Direction.up);
            if(!hasStarted) hasStarted = true;
        }
        if(Input.typed(KeyEvent.VK_DOWN)) {
            moveTiles(Direction.down);
            if(!hasStarted) hasStarted = true;
        }
    }
}
