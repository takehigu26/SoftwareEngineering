import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.scene.transform.Translate;

public class Game {
    private Pane root;
    private Scene scene;
    private final int blocks = Main.blocks, blockSize = Main.blockSize, sceneLength = blocks * blockSize;
    private Character pacman, enemy, img;
    public static boolean isPacman;
    private boolean isLeft, isRight, isUp, isDown;
    private PrintWriter out;
    private String myName = "player1";
		private static boolean isRotatable;

    Game(Pane p, Scene s, boolean b)
    {
        root     = p;
        scene    = s;
        isPacman = b;
        scene.setOnKeyPressed(e->keyPressed(e)); // キーが押されたときの挙動を示す
        // scene.setOnKeyReleased(e -> keyReleased(e));//キーが離されたときの挙動を示す
        stageSet();

        // サーバに接続する
        Socket socket = null;
        try {
            // "localhost"は，自分内部への接続．localhostを接続先のIP Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
            // 10000はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
            socket = new Socket(Main.ipAddress, 10000);
        } catch (UnknownHostException e) {
            System.err.println("ホストの IP アドレスが判定できません: " + e);
        } catch (IOException e) {
            System.err.println("エラーが発生しました: " + e);
        }

        MesgRecvThread mrt = new MesgRecvThread(socket, myName); // 受信用のスレッドを作成する
        mrt.start(); // スレッドを動かす（Runが動く）
    }

    public void setStage()
    {
        // setting pane
        root.setPrefSize(sceneLength, sceneLength);
        root.setStyle("-fx-background-color: darkgray;");
        // set blocks
        for (int y = 0; y < blocks; y++)
        {
            for (int x = 0; x < blocks; x++)
            {
                if (Main.isBlock[x][y]) {
                    Rectangle r = new Rectangle();
                    r.setWidth(50);
                    r.setHeight(50);
                    r.setStyle("-fx-background-color: BLACK; -fx-border-color: BLUE;");
                    r.getTransforms().add(new Translate(x * blockSize, y * blockSize));
                    root.getChildren().add(r);
                }
            }
        }

        // set icons
        pacman = new Character(new Image("pacman.png"), 1, 1);
        enemy  = new Character(new Image("enemy.png"), 7, 7);
        pacman.setX(blockSize);
        pacman.setY(blockSize);
        enemy.setX(blockSize * 7);
        enemy.setY(blockSize * 7);
        pacman.setFitHeight(blockSize);
        pacman.setFitWidth(blockSize);
        enemy.setFitHeight(blockSize);
        enemy.setFitWidth(blockSize);
        root.getChildren().add(pacman);
        root.getChildren().add(enemy);
    }

    public class MesgRecvThread extends Thread {
        Socket socket;
        String myName;

        public MesgRecvThread(Socket s, String n)
        {
            socket = s;
            myName = n;
        }

        // 通信状況を監視し，受信データによって動作する
        public void run()
        {
            try {
                InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
                BufferedReader    br   = new BufferedReader(sisr);
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(myName); // 接続の最初に名前を送る
                boolean isFirst = true; // 最初の通信かどうか
                while (true) {
                    String inputLine = br.readLine(); // データを一行分だけ読み込んでみる
                    if (inputLine != null) { // 読み込んだときにデータが読み込まれたかどうかをチェックする
                        System.out.println(inputLine); // デバッグ（動作確認用）にコンソールに出力する
                        if (isFirst)
                        {
                            if (inputLine.equals("1")) { isPacman = true; }
                            else { isPacman = false; }
                            isFirst = false;
                        }
                        String[] inputTokens = inputLine.split(" ");    // 入力データを解析するために、スペースで切り分ける
                        String   cmd         = inputTokens[0]; // コマンドの取り出し．１つ目の要素を取り出す
                        if (cmd.equals("MOVE")) { // cmdの文字と"MOVE"が同じか調べる．同じ時にtrueとなる
                            // MOVEの時の処理(コマの移動の処理)
                            String    isPacmanStr = inputTokens[1]; // ボタンの名前（番号）の取得
                            Character c, d;
                            if (isPacmanStr.equals("p")) {
                                c = pacman;
                                d = enemy;
																isRotatable = true;
                            } else {
                                c = enemy;
                                d = pacman;
																isRotatable = false;
                            }
                            String command = inputTokens[2];
                            gameLoop(c, d, command);
                        }
                    } else {
                        break;
                    }
                }
                socket.close();
            } catch (IOException e) {
                System.err.println("エラーが発生しました: " + e);
            }
        }
    }

    /*
        public void sendMsg() //AnimationTimerでループする
        {
            String direction, isPacmanStr;
            // フラグを元にdirectionを定める
            if(isLeft) direction = "LEFT";
            else if(isRight) direction = "RIGHT";
            else if(isUp) direction = "UP";
            else if(isDown) direction = "DOWN";
            else direction = "NO";

            //　pacmanかenemyか
            if(isPacman)	isPacmanStr = "p";
            else isPacmanStr = "e";

            String msg = "MOVE"+" "+isPacmanStr+" "+direction;

            //サーバに情報を送る
            out.println(msg);
            out.flush();
        }*/
    public void gameLoop(Character c,   Character d, String s)  // 受信したデータをもとに動かす c->動くキャラ　d->動かないキャラ
    {
        img = c;
        int mid = (blocks - 1) / 2;
        if (s.equals("LEFT")) {
            if(isRotatable) img.setRotate(180);
            if (img.x == 0 && img.y == mid) {
                img.setX((blocks - 1) * blockSize);
                img.x = blocks - 1;
            } else if (!Main.isBlock[img.x - 1][img.y]) { // 左のところが空白かまたはステージ無いかどうかを見る　今回はステージ内であることだけを確認
                img.setX(img.getX() - blockSize); // 左に移動可能なら座標を10マイナスする
                img.x--;
                // x-=1;//これもsetXと同じことをしている. 後述
            }
            isLeft = false; // 長押ししてると高速で移動しちゃうのであえて1回おきに移動するようにしている
        }
        else if (s.equals("RIGHT")) {
            if(isRotatable) img.setRotate(0);
            if (img.x == (blocks - 1) && img.y == mid) {
                img.setX(0);
                img.x = 0;
            } else if (!Main.isBlock[img.x + 1][img.y]) {
                img.setX(img.getX() + blockSize);
                img.x++;
                // img.pointx+=1;
            }
            isRight = false;
        }
        else if (s.equals("UP")) {
            if(isRotatable) img.setRotate(270);
            if (!Main.isBlock[img.x][img.y - 1]) {
                img.setY(img.getY() - blockSize);
                img.y--;
                // img.pointy-=1;
                isUp = false;
            } else { img.setY(img.getY()); }
        }
        else if (s.equals("DOWN")) {
            if(isRotatable) img.setRotate(90);
            if (!Main.isBlock[img.x][img.y + 1]) {
                img.setY(img.getY() + blockSize);
                img.y++;
                // img.pointy+=1;
                isDown = false;
            } else { img.setY(img.getY()); }
        }

				if(c.x == d.x && c.y == d.y) //キャラクターが重なった時
				{
					System.out.println("キャラが重なりました");
					Main.changeScene(Main.sceneType.Gameover);
				}
    }

    private void keyPressed(KeyEvent e)
    {
        String direction = "NO", isPacmanStr;
        switch (e.getCode()) {
            case LEFT:
                direction = "LEFT"; // 左が押されたの確認したらフラグを立てる
                break;
            case RIGHT:
                direction = "RIGHT";
                break;
            case UP:
                direction = "UP";
                break;
            case DOWN:
                direction = "DOWN";
                break;
            default:
                break;
        }
        if (isPacman) { isPacmanStr = "p"; }
        else { isPacmanStr = "e"; }
        String msg = "MOVE" + " " + isPacmanStr + " " + direction;
        // サーバに情報を送る
        out.println(msg);
        out.flush();
    }

    private void stageSet()
    {
        // System.out.println("here");
        try
        {
            File file = new File("stage.txt");
            // System.out.println("here----");
            FileReader     fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String         data;
            int            y = 0;
            while ((data = br.readLine()) != null) {
                // System.out.println(data);
                for (int i = 0; i < data.length(); i++) {
                    char c = data.charAt(i);
                    if (c - '0' == 0) { Main.isBlock[i][y] = false; }
                    else { Main.isBlock[i][y] = true; }
                    // System.out.print(isBlock[i][y]+"|");
                }
                System.out.println();
                y++;
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}


class Character extends ImageView {
    public int x, y;
    // public int pointx, pointy;
    Character(Image i, int x, int y)
    {
        super();
        Image image = i;
        this.setImage(image);
        this.x = x;
        this.y = y;
        // this.pointx = (int) (x/10)+1;
        // this.pointy = (int) (y/10)+1;
        this.setRotate(0);
        // System.out.println(pointx+" "+pointy);// デバック用　消しても何も変わらん
    }
}
