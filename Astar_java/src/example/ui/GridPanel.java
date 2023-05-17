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

public class GridPanel extends JPanel implements Observer {

    private boolean check = true;

    private Grid grid;

    private ArrayList<Tile> path;
    private Tile user;

    private Tile monster;
    private ControlsPanel controls;

    private BasicStroke defaultStroke;

    private BasicStroke widerStroke;
    // 추가된 변수
//    private int currentIndex; // 현재 경로 인덱스

    public Timer pathTimer; // 경로 이동 타이머
    private int currentIndex = 0;


    public Timer timer;
    private AStarAlgorithm algorithm;

    public KeyAdapter userMovement;

    public static String[] header = {"난이도", "생존 시간", "남은 생명"};
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

        // 경로 이동 타이머 초기화
        timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (path != null && currentIndex < path.size()) {
                    monster = path.get(currentIndex);
                    currentIndex++;
                    repaint();
                } else {
                    showEndGameDialog(false);
                    timer.stop();
                    RemoveKeyListener();
                    System.out.println("게임이 끝났습니다.");
                    check = true;
                    setRequestFocusEnabled(false);
                    controls.resetGameSetting();
                    algorithm.reset();
                    algorithm.updateUI();
                    easyMap();
                }
            }
        });
    }

    public void createWall(int x, int y) {
        Tile t = grid.find(x, y);

        if (t != null) {
            controls.selectTile(t);
        }
    }

    public void showEndGameDialog(boolean isGameWon) {
        String message;
        String title;

        if (isGameWon) {
            message = "Congratulations! You won the game.";
            title = "You Win";
        } else {
            message = "Game Over. You lose the game.";
            title = "Game Over";
        }

        setRank();

        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void setRank() {
        for (int i = 0; i < contentSize + 1; i++) {
            contents[i][0] = ControlsPanel.levelType.name();
            contents[i][1] = ControlsPanel.timerLabel.getText();
            contents[i][2] = ControlsPanel.lifeLabel.getText();
            int score = 0;
            if (contents[i][0] == ControlsPanel.LevelType.HARD.name())
                score += 60;
            else if (contents[i][0] == ControlsPanel.LevelType.NORMAL.name())
                score += 80;
            else if (contents[i][0] == ControlsPanel.LevelType.EASY.name())
                score += 90;
            else
                score += 100;

            contents[i][3] = String.valueOf(score - Integer.parseInt(contents[i][1].replace("Time: ", "")) - Integer.parseInt(contents[i][2].replace("Life: ", "")));

            controls.putRank();

        }
        contentSize++;
    }

    public void showCanNotBuild() {
        JOptionPane.showMessageDialog(this, "생성 불가능 합니다.", "warning", JOptionPane.INFORMATION_MESSAGE);
    }

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
        pathTimer = new Timer(speed, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (path != null && currentIndex < path.size()) {
                    monster = path.get(currentIndex);
                    currentIndex++;
                    repaint();
                } else {
                    showEndGameDialog(false);
                    timer.stop();
                    RemoveKeyListener();
                    System.out.println("게임이 끝났습니다.");
                    check = true;
                    setRequestFocusEnabled(false);
                    controls.resetGameSetting();
                    algorithm.reset();
                    algorithm.updateUI();
                    easyMap();
                }
            }
        });
        pathTimer.start();

    }

    public KeyAdapter getUserMoveMent(AStarAlgorithm algorithm, Network network) {
        KeyAdapter UserMovement = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                int x = user.getX();
                int y = user.getY();
                if (keyCode == KeyEvent.VK_UP) {
                    if (user != null) {
                        if (y == 0) {
                            lifeDown();
                            return;
                        }
                        if (!grid.find(x, y - 1).isValid()) {
                            lifeDown();
                            return;
                        }
                        user = new Tile(x, y - 1);
                        repaint();
                    }
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    if (user != null) {
                        if (y > 18) {
                            lifeDown();
                            return;
                        }

                        if (!grid.find(x, y + 1).isValid()) {
                            lifeDown();
                            return;
                        }

                        user = new Tile(x, y + 1);
                        repaint();
                    }
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    if (user != null) {
                        if (x == 0) {
                            lifeDown();
                            return;
                        }
                        if (!grid.find(x - 1, y).isValid()) {
                            lifeDown();
                            return;
                        }
                        user = new Tile(x - 1, y);
                        repaint();
                    }
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    if (user != null) {
                        if (x > 18) {
                            lifeDown();
                            return;
                        }
                        if (!grid.find(x + 1, y).isValid()) {
                            lifeDown();
                            return;
                        }


                        user = new Tile(x + 1, y);
                        repaint();
                    }
                }
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
                    showEndGameDialog(false);
                    pathTimer.stop();
                    timer.stop();

                    RemoveKeyListener();
                    algorithm.reset();
                    algorithm.updateUI();

                    System.out.println("게임이 끝났습니다.");
                    setRequestFocusEnabled(false);
                    check = false;
                    controls.resetGameSetting();
                    algorithm.reset();
                    algorithm.updateUI();
                    easyMap();
                }
            }
        };
        return UserMovement;
    }

    public void disableMouseEvents() {
        addMouseListener(new MouseAdapter() {
        });
        addMouseMotionListener(new MouseAdapter() {
        });
    }

    @Override
    protected void paintComponent(Graphics g1) {
        super.paintComponent(g1);

        Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

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
            g2d.dispose();
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
                    g.setColor(Color.GRAY);
                    int x = (t.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 10;
                    int y = (t.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 10;

                    g.fillRoundRect(x, y, 20, 20, 10, 10);
                }
            }
        }

        g.drawRect(getWidth() - 1, 0, 1, getHeight());
        g.drawRect(0, getHeight() - 1, getWidth(), 1);
    }

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
            timer.start();
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
