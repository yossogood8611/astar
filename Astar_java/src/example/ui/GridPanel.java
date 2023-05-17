package example.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.border.LineBorder;

import pathfinding.AStarAlgorithm;
import pathfinding.element.Network;
import pathfinding.element.Node;
import example.element.Grid;
import example.element.Tile;

import static example.element.Tile.TILE_SIZE;

public class GridPanel extends JPanel implements Observer {

    private Grid grid;
    private ArrayList<Tile> path;

    private Tile user;
    private Tile monster;

    private ControlsPanel controls;

    private BasicStroke defaultStroke;
    private BasicStroke widerStroke;

    // 추가된 변수
//    private int currentIndex; // 현재 경로 인덱스
    private Timer pathTimer; // 경로 이동 타이머

    private int currentIndex = 0;
    private Timer timer;

    private KeyAdapter userMovement;

    public GridPanel(ControlsPanel controls) {
        this.controls = controls;

        this.defaultStroke = new BasicStroke();
        this.widerStroke = new BasicStroke(2);

        setBorder(new LineBorder(Color.gray));

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
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
                    setRequestFocusEnabled(false);
                }
            }
        });
    }

    public void startUserMovement(AStarAlgorithm algorithm, Network network) {
        userMovement =getUserMoveMent(algorithm, network);
        addKeyListener(userMovement);
        setFocusable(true);
        requestFocusInWindow(); // 포커스를 요청하여 키보드 입력을 받을 수 있도록 합니다.

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
                            System.out.println("범위 벗어남");
                            return;
                        }
                        user = new Tile(x, y - 1);
                        repaint();
                    }
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    if (user != null) {
                        if (y == TILE_SIZE-1) {
                            System.out.println("범위 벗어남");
                            return;
                        }
                        user = new Tile(x, y + 1);
                        repaint();
                    }
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    if (user != null) {
                        if (x == 0) {
                            System.out.println("범위 벗어남");
                            return;
                        }
                        user = new Tile(x - 1, y);
                        repaint();
                    }
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    if (user != null) {
                        if (x == TILE_SIZE-1) {
                            System.out.println("범위 벗어남");
                            return;
                        }
                        user = new Tile(x + 1, y);
                        repaint();
                    }
                }
                user.calculateNeighbours(network);
                algorithm.reset(user, monster);
                algorithm.solve();
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
            int x = (user.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 6;
            int y = (user.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 6;

            g.setColor(new Color(20, 122, 17));
            g.setStroke(widerStroke);
            g.fillOval(x, y, 12, 12);
        }

        if (monster != null) {
            int x = (monster.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 6;
            int y = (monster.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 6;

            g.setColor(new Color(16, 49, 119));
            g.setStroke(widerStroke);
            g.fillOval(x, y, 12, 12);
        }

        g.setStroke(defaultStroke);

        if (grid != null && grid.getTiles() != null) {
            for (Tile t : grid.getTiles()) {
                g.setColor(new Color(220, 220, 220));
                g.drawRect(t.getX() * TILE_SIZE, t.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                if (!t.isValid()) {
                    g.setColor(Color.GRAY);
                    int x = (t.getX() * TILE_SIZE) + (TILE_SIZE / 2) - 5;
                    int y = (t.getY() * TILE_SIZE) + (TILE_SIZE / 2) - 5;

                    g.fillOval(x, y, 10, 10);
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
}