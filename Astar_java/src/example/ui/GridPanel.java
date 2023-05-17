package example.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

import pathfinding.AStarAlgorithm;
import pathfinding.element.Network;
import pathfinding.element.Node;
import example.element.Grid;
import example.element.Tile;

import static example.element.Tile.TILE_SIZE;

public class GridPanel extends JPanel implements Observer {

    private boolean check=true;

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

    private KeyAdapter userMovement;
    public GridPanel(ControlsPanel controls, AStarAlgorithm algorithm) {
        this.controls = controls;

        this.defaultStroke = new BasicStroke();
        this.widerStroke = new BasicStroke(2);
        this.algorithm = algorithm;

        setBorder(new LineBorder(Color.gray));

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if(!check){
                    return;
                }
                int x = evt.getX();
                int y = evt.getY();

                int tileX = x / TILE_SIZE;
                int tileY = y / TILE_SIZE;

                Tile t = grid.find(tileX, tileY);

                if (t != null) {
                    controls.selectTile(t);
                }
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
                    timer.stop();
                    removeKeyListener(userMovement);
                    System.out.println("게임이 끝났습니다.");
                    check=true;
                    algorithm.reset();
                    algorithm.updateUI();
                    setRequestFocusEnabled(false);
                    controls.resetGameSetting();
                    showEndGameDialog(false);
                }
            }
        });
    }

    public void showEndGameDialog(boolean isGameWon) {
        String message;
        if (isGameWon) {
            message = "Congratulations! You won the game.";
        } else {
            message = "Game Over. You lost the game.";
        }

        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    public void startUserMovement(ControlsPanel.LevelType levelType) {
        userMovement = getUserMoveMent(algorithm, algorithm.getNetwork());
        addKeyListener(userMovement);
        setFocusable(true);
        requestFocusInWindow(); // 포커스를 요청하여 키보드 입력을 받을 수 있도록 합니다.
        int speed=500;
        switch (levelType){
            case EASY:speed=500; break;
            case NORMAL:speed=250; break;
            case HARD:speed=100; break;
        }
        pathTimer = new Timer(speed, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (path != null && currentIndex < path.size()) {
                    monster = path.get(currentIndex);
                    currentIndex++;
                    repaint();
                } else {
                    pathTimer.stop();
                    timer.stop();
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
                        if (y == TILE_SIZE - 1) {
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
                        if (x == TILE_SIZE - 1) {
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
                if(controls.isLifeZero()){
                    pathTimer.stop();
                    timer.stop();
                    removeKeyListener(userMovement);
                    algorithm.reset();
                    algorithm.updateUI();
                    System.out.println("게임이 끝났습니다.");
                    setRequestFocusEnabled(false);
                    check=false;
                    controls.resetGameSetting();
                    showEndGameDialog(false);
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
            int x = (user.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 10;
            int y = (user.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 10;

            g.setColor(new Color(20, 122, 17));
            g.setStroke(widerStroke);
            g.fillOval(x, y, 20, 20);
        }

        if (monster != null) {
            int x = (monster.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 15;
            int y = (monster.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 15;

            Image image = null;
            try {
                image = ImageIO.read(new File("monster.png"));

            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedImage myImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = myImage.createGraphics();
            g2d.drawImage(image, 0, 0, 30, 30, null);
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
}
