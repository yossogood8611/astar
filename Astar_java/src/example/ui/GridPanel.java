package example.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

import pathfinding.AStarAlgorithm;
import pathfinding.element.Network;
import pathfinding.element.Node;
import example.element.Grid;
import example.element.Tile;

import static example.element.Tile.TILE_SIZE;
import static example.ui.ControlsPanel.levelType;

public class GridPanel extends JPanel implements Observer {

    private boolean check = true;
    private Image backImage;
    private Grid grid;

    private ArrayList<Tile> path;
    private Tile user;

    private Tile monster;
    public ControlsPanel controls;

    private BasicStroke defaultStroke;

    private BasicStroke widerStroke;
    // 추가된 변수
//    private int currentIndex; // 현재 경로 인덱스

    public Timer monsterTimer; // 경로 이동 타이머
    private int currentIndex = 0;
    private AStarAlgorithm algorithm;

    public KeyAdapter userMovement;
    public Timer itemTimer;
    public Tile item;
    public static String[] header = {"난이도", "생존 시간", "남은 생명", "점수"};
    public static String[][] contents = new String[50][4]; //{{"Hard", "60", "3", "321223(점수)"}};
    public static int contentSize = 0;

    public void RemoveKeyListener() {
        removeKeyListener(userMovement);
    }

    public GridPanel(ControlsPanel controls, AStarAlgorithm algorithm) {
        this.controls = controls;

        this.defaultStroke = new BasicStroke();
        this.widerStroke = new BasicStroke(2);
        this.algorithm = algorithm;

        this.item = new Tile(0, 0);

        setBorder(new LineBorder(Color.gray));

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (!check) {
                    return;
                }
                int x = evt.getX();
                int y = evt.getY();

                int tileX = x / TILE_SIZE;
                int tileY = y / TILE_SIZE;

                createWall(tileX, tileY);
            }
        });
        monsterTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (path != null && currentIndex < path.size()) {
                    monster = path.get(currentIndex);
                    currentIndex++;
                    repaint();
                } else {
                    if(path==null){

                    }
                    resetGameSet();
                }
            }
        });

        itemTimer = new Timer(10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Tile randomEmptyTile = grid.findEmptyTile();
                if (randomEmptyTile != null) {
                    if (item == null) {
                        return;
                    }
                    item.setCheck(false);
                    item.setX(randomEmptyTile.getX());
                    item.setY(randomEmptyTile.getY());
                } else {
                    System.out.println("No empty tile found.");
                }
            }
        });
    }

    private void resetGameSet() {
        ControlsPanel.endLife = 0;
        ControlsPanel.endTime = ControlsPanel.remainingTime;
        RemoveKeyListener();
        System.out.println("게임이 끝났습니다.");
        setRequestFocusEnabled(false);
        check = true;
        controls.resetGameSetting();
        showEndGameDialog(false);
        easyMap();
    }

    //좌표값에 벽을 생성함
    public void createWall(int x, int y) {
        Tile t = grid.find(x, y);

        if (t != null) {
            controls.selectTile(t);
        }
    }

    //게임 종료 시 메시지 출력
    public void showEndGameDialog(boolean isGameWon) {
        String message;
        String title;

        if (isGameWon) {
            message = "축하합니다. 당신이 이겼습니다.";
            title = "You Win!";
        } else {
            message = "아쉽네요. 게임에 졌습니다.";
            title = "Game Over!";
            setBackground(Color.black);
        }
        setRank();
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        setBackground(new Color(238, 238, 238));
    }

    //게임 종료 후 우측 하단에 랭킹 설정
    private void setRank() {
        contents[contentSize][0] = ControlsPanel.levelType.name();
        int temp = 0;
        switch (levelType.name()) {
            case "EASY":
                temp = 20;
                break;
            case "NORMAL":
                temp = 40;
                break;
            case "HARD":
                temp = 60;
                break;
            case "CUSTOM":
                return;
        }
        int time = temp - ControlsPanel.endTime;
        contents[contentSize][1] = String.valueOf(time);
        contents[contentSize][2] = String.valueOf(ControlsPanel.endLife);
        int score = 0;
        if (contents[contentSize][0] == ControlsPanel.LevelType.HARD.name())
            score += 100;
        else if (contents[contentSize][0] == ControlsPanel.LevelType.NORMAL.name())
            score += 80;
        else if (contents[contentSize][0] == ControlsPanel.LevelType.EASY.name())
            score += 60;
        else
            score += 100;
        score += time;
        contents[contentSize][3] = String.valueOf(score + Integer.parseInt(contents[contentSize][2].replace("생명: ", "")) * 10);
        controls.putRank();
        contentSize++;
    }

    //벽을 생성할 수 없음을 다이얼로그로 표시
    public void showCanNotBuild(String s) {
        JOptionPane.showMessageDialog(this, s, "warning", JOptionPane.INFORMATION_MESSAGE);
    }

    //사용자의 이동을 시작
    //levelType에 따라 이동 속도를 설정
    public void startUserMovement(ControlsPanel.LevelType levelType) {
        userMovement = getUserMoveMent(algorithm, algorithm.getNetwork());
        addKeyListener(userMovement);
        setFocusable(true);
        requestFocusInWindow(); // 포커스를 요청하여 키보드 입력을 받을 수 있도록 합니다.
        int speed = 500;
        switch (levelType) {
            case EASY:
                speed = 500;
                break;
            case NORMAL:
                speed = 250;
                break;
            case HARD:
                speed = 100;
                break;
            case CUSTOM:
                speed = Integer.parseInt(ControlsPanel.setSpeedText.getText());
                break;
        }
        monsterTimer.setDelay(speed);
        monsterTimer.start();
    }

    //사용자가 키보드로 움직이는 이벤트
    //algorithm 통해 계속 몬스터가 유저의 최적화 경로를 찾음
    //solve()
    public KeyAdapter getUserMoveMent(AStarAlgorithm algorithm, Network network) {
        KeyAdapter UserMovement = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                int x = user.getX();
                int y = user.getY();
                if (user != null) {
                    switch (keyCode) {
                        case KeyEvent.VK_UP: {
                            if (y == 0 || !grid.find(x, y - 1).isValid()) {
                                lifeDown();
                                return;
                            }
                            user = new Tile(x, y - 1);
                            if (x == item.getX() && y - 1 == item.getY()) {
                                eatItem();
                            }

                        }
                        break;

                        case KeyEvent.VK_DOWN: {
                            if (y > 18 || !grid.find(x, y + 1).isValid()) {
                                lifeDown();
                                return;
                            }
                            if (x == item.getX() && y + 1 == item.getY()) {
                                eatItem();
                            }
                            user = new Tile(x, y + 1);

                        }
                        break;


                        case KeyEvent.VK_LEFT: {
                            if (x == 0 || !grid.find(x - 1, y).isValid()) {
                                lifeDown();
                                return;
                            }
                            if (x - 1 == item.getX() && y == item.getY()) {
                                eatItem();
                            }
                            user = new Tile(x - 1, y);

                        }
                        break;

                        case KeyEvent.VK_RIGHT: {
                            if (x > 18 || !grid.find(x + 1, y).isValid()) {
                                lifeDown();
                                return;
                            }
                            if (x + 1 == item.getX() && y == item.getY()) {
                                eatItem();
                            }
                            user = new Tile(x + 1, y);

                        }
                        break;

                    }
                }
                repaint();

                user.calculateNeighbours(algorithm.getNetwork());
                algorithm.reset(user, monster);
                algorithm.solve();
            }

            private void lifeDown() {
                System.out.println("범위 벗어남");
                controls.lifeDown();
                gameOver();
            }

            private void gameOver() {
                if (controls.isLifeZero()) {
                    resetGameSet();
                }
            }
        };
        return UserMovement;
    }

    private void eatItem() {
        item.setCheck(true);
        controls.lifeUp();
    }

    //마우스 이벤트 비활성화
    public void disableMouseEvents() {
        addMouseListener(new MouseAdapter() {
        });
        addMouseMotionListener(new MouseAdapter() {
        });
    }

    //패널 그림
    @Override
    protected void paintComponent(Graphics g1) {
        super.paintComponent(g1);


        ImageIcon background = new ImageIcon("grass.jpg");
        backImage = background.getImage();
        g1.drawImage(backImage, 0, 0, getWidth(), getHeight(), null);

        Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (path != null) {
            g.setColor(new Color(229, 142, 229));
            for (int i = 0; i < path.size() - 1; i++) {
                Tile t = path.get(i);
                Tile t2 = path.get(i + 1);

                int x = (t.getX() * Tile.TILE_SIZE) + (Tile.TILE_SIZE / 2) - 5;
                int y = (t.getY() * Tile.TILE_SIZE) + (Tile.TILE_SIZE / 2) - 5;

                int xx = (t2.getX() * Tile.TILE_SIZE) + (Tile.TILE_SIZE / 2);
                int yy = (t2.getY() * Tile.TILE_SIZE) + (Tile.TILE_SIZE / 2);

                g.setStroke(widerStroke);
                g.fillOval(x, y, 10, 10);
                g.setStroke(defaultStroke);
                g.drawLine(x + 5, y + 5, xx, yy);
            }
        }

        if (user != null) {
            int x = (user.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 15;
            int y = (user.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 15;


            Image userImage = null;
            try {
                userImage = ImageIO.read(new File("user.png"));

            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedImage myImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = myImage.createGraphics();
            g2d.drawImage(userImage, 0, 0, 30, 30, null);
            g.drawImage(myImage, x, y, 150, 150, null);
            g.setStroke(widerStroke);
        }

        if (monster != null) {
            int x = (monster.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 15;
            int y = (monster.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 15;

            Image monsterImage = null;
            try {
                monsterImage = ImageIO.read(new File("monster.png"));

            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedImage myImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = myImage.createGraphics();
            g2d.drawImage(monsterImage, 0, 0, 30, 30, null);
            g2d.dispose();
            g.drawImage(myImage, x, y, 150, 150, null);
            g.setStroke(widerStroke);
        }
        g.setStroke(defaultStroke);
        if (grid != null && grid.getTiles() != null) {
            for (Tile t : grid.getTiles()) {
                g.setColor(new Color(220, 220, 220));
                g.drawRect(t.getX() * TILE_SIZE, t.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                if (!t.isValid()) {
                    int x = (t.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 15;
                    int y = (t.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 15;

                    Image blockImage = null;
                    try {
                        blockImage = ImageIO.read(new File("block.png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedImage myImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = myImage.createGraphics();
                    g2d.drawImage(blockImage, 0, 0, 30, 30, null);

                    g.drawImage(myImage, x, y, 150, 150, null);
                    g.setStroke(widerStroke);
                }
                if (t.isHill()) {
                    int x = (t.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 15;
                    int y = (t.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 15;

                    Image grassImage = null;
                    try {
                        grassImage = ImageIO.read(new File("grass.png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedImage myImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = myImage.createGraphics();
                    g2d.drawImage(grassImage, 0, 0, 30, 30, null);

                    g.drawImage(myImage, x, y, 150, 150, null);
                    g.setStroke(widerStroke);
                }
            }
        }

        if (itemTimer.isRunning()) {
            if (item == null) {
                return;
            }
            if (!item.isCheck()) {
                Image blockImage = null;
                try {
                    blockImage = ImageIO.read(new File("hp.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BufferedImage myImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = myImage.createGraphics();
                g2d.drawImage(blockImage, 0, 0, 30, 30, null);
                g2d.dispose();
                g.drawImage(myImage, (item.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 10, (item.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 10, 130, 130, null);
                AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
                g.setComposite(alphaComposite);
            } else {
                g.setColor(new Color(238, 238, 238));
                AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.01f);
                g.setComposite(alphaComposite);
                g.fillOval((item.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 10, (item.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 10, 20, 20);
            }
        }



        g.drawRect(getWidth() - 1, 0, 1, getHeight());
        g.drawRect(0, getHeight() - 1, getWidth(), 1);
    }

    //그래픽 객체의 상태를 업데이트하고 화면을 다시 그리는 메서드
    @Override
    public void update(Observable o, Object o1) {
        AStarAlgorithm alg = (AStarAlgorithm) o;
        Grid grid = (Grid) alg.getNetwork();
        ArrayList<Node> path = alg.getPath();
        Node start = alg.getStart();
        Node end = alg.getEnd();

        this.grid = grid;
        this.path = new ArrayList<>();

        if (path != null) {
            for (Node n : path) {
                if (n instanceof Tile) {
                    this.path.add((Tile) n);
                }
            }
        }

        if (start != null && start instanceof Tile) {
            this.user = (Tile) start;
        } else {
            this.user = null;
        }

        if (end != null && end instanceof Tile) {
            this.monster = (Tile) end;
        } else {
            this.monster = null;
        }

        currentIndex = 0; // 인덱스 초기화
        if (this.path != null && this.path.size() > 1) {
            monsterTimer.start();
        }


        repaint();
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public void startMap(Grid grid) {
        ControlsPanel.selectionType = ControlsPanel.SelectionType.REVERSE;
        this.grid = grid;
        easyMap();
        ControlsPanel.selectionType = ControlsPanel.SelectionType.START;
    }

    public void easyMap() {
        algorithm.reset();
        ControlsPanel.selectionType = ControlsPanel.SelectionType.REVERSE;
        ArrayList<Integer> xs = new ArrayList<>();
        ArrayList<Integer> ys = new ArrayList<>();

        int count = 40;

        getWalls(xs, ys, count);

        for (int i = 0; i < count; i++) {
            try {
                createWall(xs.get(i), ys.get(i));
            } catch (Exception e) {

            }
        }
        ControlsPanel.selectionType = ControlsPanel.SelectionType.START;
    }

    public void normalMap() {
        algorithm.reset();
        ControlsPanel.selectionType = ControlsPanel.SelectionType.REVERSE;
        ArrayList<Integer> xs = new ArrayList<>();
        ArrayList<Integer> ys = new ArrayList<>();

        int count = 60;

        getWalls(xs, ys, count);

        for (int i = 0; i < count; i++) {
            try {
                createWall(xs.get(i), ys.get(i));
            } catch (Exception e) {

            }
        }
        ControlsPanel.selectionType = ControlsPanel.SelectionType.START;
    }

    public void hardMap() {
        algorithm.reset();
        ControlsPanel.selectionType = ControlsPanel.SelectionType.REVERSE;
        ArrayList<Integer> xs = new ArrayList<>();
        ArrayList<Integer> ys = new ArrayList<>();

        int count = 80;

        getWalls(xs, ys, count);

        for (int i = 0; i < count; i++) {
            try {
                createWall(xs.get(i), ys.get(i));
            } catch (Exception e) {

            }
        }
        ControlsPanel.selectionType = ControlsPanel.SelectionType.START;
    }

    private static void getWalls(ArrayList<Integer> xs, ArrayList<Integer> ys, int count) {
        for (int j = 0; j < count; j++) {
            int x = new Random().nextInt(20);
            int y = new Random().nextInt(20);

            if (!(xs.contains(x) && ys.contains(y))) {
                xs.add(new Random().nextInt(20));
                ys.add(new Random().nextInt(20));
            } else {
                xs.add(x);
                ys.add(y);
            }
        }
    }

    public void customMap() {
        algorithm.reset();
    }
}
