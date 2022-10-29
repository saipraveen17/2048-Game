package Programming2048;

import java.awt.event.KeyEvent;

public class Input {

    public static boolean[] keys = new boolean[256];
    public static boolean[] lastKeys = new boolean[256];

    private Input() {

    }
    public static void update() {
        for(int i = 0; i < 4; i++) {
            if(i==0) lastKeys[KeyEvent.VK_LEFT] = keys[KeyEvent.VK_LEFT];
            if(i==1) lastKeys[KeyEvent.VK_RIGHT] = keys[KeyEvent.VK_RIGHT];
            if(i==2) lastKeys[KeyEvent.VK_UP] = keys[KeyEvent.VK_UP];
            if(i==3) lastKeys[KeyEvent.VK_DOWN] = keys[KeyEvent.VK_DOWN];
        }
    }

    public static boolean typed(int keyEvent) {
        return !keys[keyEvent]&&lastKeys[keyEvent];
    }

    public static void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    public static void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }
}
