package example.ui;

import example.element.Grid;
import example.element.Tile;
import pathfinding.AStarAlgorithm;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import static example.ui.GridPanel.contentSize;

public class ControlsPanel extends JPanel {

    public static final String TIME_60 = "시간: 60";
    public static final String TIME_40 = "시간: 40";
    public static final String TIME_20 = "시간: 20";
    private AStarAlgorithm algorithm;
    public static SelectionType selectionType;
    public static LevelType levelType;

    private JComboBox<String> selector;
    private GridPanel canvas;

    public Timer timer;
    public static JLabel timerLabel;
    public static JLabel lifeLabel;
    public static JTextField setSpeedText;
    public static JTextField setTimeText;
    public static JTextField setLifeText;
    public static JTable rankList;

    public static JScrollPane scrollPane;

    public static int remainingTime;

    private int lifeCount = 3;

    public static int endTime;
    public static int endLife;

    Font dodum = new Font("고딕", Font.PLAIN, 13);

    //게임 설정 초기화
    public void resetGameSetting() {
        timer.stop();
        canvas.timer.stop();
        canvas.pathTimer.stop();
        lifeCount = 3;
        lifeLabel.setText("생명: " + lifeCount);
        algorithm.reset();
        algorithm.updateUI();
        timerLabel.setText(TIME_60);
    }

    //생명 카운트 감소
    public void lifeDown() {
        this.lifeCount--;
        this.lifeLabel.setText("생명: " + this.lifeCount);
    }

    //생명 카운트 증가
    public void lifeUp() {
        this.lifeCount++;
        this.lifeLabel.setText("생명: " + this.lifeCount);
    }

    //생명 카운트 0인지 확인
    public boolean isLifeZero() {
        if (lifeCount == 0) return true;
        else return false;
    }

    //생명 카운트 반환
    public int getLifeCount() {
        return this.lifeCount;
    }

    //현재 타이머 반환
    public Timer getTimer() {
        return this.timer;
    }

    //생명 카운트 설정
    public void setLifeCount(int lifeCount) {
        this.lifeCount = lifeCount;
    }

    //타이머 설정
    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    //패널 생성
    public ControlsPanel(int width, int height, AStarAlgorithm algorithm) {
        this.algorithm = algorithm;
        this.selectionType = SelectionType.START;
        this.levelType = LevelType.EASY;

        setBorder(new LineBorder(Color.gray));
        setLayout(null);

        Label selectionLabel = new Label("Select Type: ");
        selectionLabel.setBounds(7, 10, width - 20, 25);
        selectionLabel.setFont(dodum);
        add(selectionLabel);

        selector = new JComboBox<>();
        selector.addItem("유저");
        selector.addItem("몬스터");
        selector.addItem("언덕");
        selector.addItem("벽");
        selector.setBounds(10, 35, width - 20, 30);
        selector.setFont(dodum);
        selector.addActionListener((ActionEvent e) -> {
            selectionType = selectionType.values()[selector.getSelectedIndex()];
        });
        add(selector);

        JComboBox<String> levelSelector = new JComboBox<>();
        levelSelector.addItem("Easy");
        levelSelector.addItem("Normal");
        levelSelector.addItem("Hard");
        levelSelector.addItem("Custom");
        levelSelector.setFont(dodum);
        levelSelector.setBounds(10, height - 50, width - 20, 30);
        levelSelector.addActionListener((ActionEvent e) -> {
            levelType = levelType.values()[levelSelector.getSelectedIndex()];
            selectLevel();
        });
        add(levelSelector);

        Label speedLabel = new Label("Speed:");
        speedLabel.setBounds(10, height - 10, width - 20, 10);
        speedLabel.setFont(dodum);
        add(speedLabel);
        setSpeedText = new JTextField();
        setSpeedText.disable();
        setSpeedText.setText(String.valueOf(300));
        setSpeedText.setFont(dodum);
        setSpeedText.setBounds(10, height + 5, width - 20, 30);
        add(setSpeedText);

        Label timeLabelText = new Label("Time:");
        timeLabelText.setBounds(10, height + 40, 50, 10);
        timeLabelText.setFont(dodum);
        add(timeLabelText);
        setTimeText = new JTextField();
        setTimeText.disable();
        setTimeText.setText(TIME_20.replace("시간: ", ""));
        setTimeText.setBounds(10, height + 55, 130, 30);
        setTimeText.setFont(dodum);
        add(setTimeText);

        Label lifeLabelText = new Label("Life: ");
        lifeLabelText.setBounds(160, height + 40, 40, 10);
        lifeLabelText.setFont(dodum);
        add(lifeLabelText);
        setLifeText = new JTextField();
        setLifeText.disable();
        setLifeText.setText(String.valueOf(3));
        setLifeText.setFont(dodum);
        setLifeText.setBounds(160, height + 55, 130, 30);
        add(setLifeText);

        JButton reset = new JButton("다시하기");
        reset.setFont(dodum);
        reset.setBounds(10, height + 100, 130, 30);
        reset.addActionListener((ActionEvent ae) -> {
            algorithm.reset();
            algorithm.updateUI();
            canvas.item = null;
            selectionType = SelectionType.START;
            canvas.setCheck(true);
            timer.stop();
            timerLabel.setText(TIME_60);
            lifeCount = 3;
            lifeLabel.setText("생명: " + lifeCount);
        });
        add(reset);


        JButton start = new JButton("시작");
        start.setBounds(160, height + 100, 130, 30);
        start.setFont(dodum);
        start.addActionListener((ActionEvent ae) -> {
            canvas.item = new Tile(0, 0, 1);
            remainingTime = Integer.parseInt(setTimeText.getText().replace("시간: ", ""));
            lifeCount = Integer.parseInt(setLifeText.getText());
            algorithm.solve();
            canvas.startUserMovement(levelType);
            canvas.setCheck(false);
            canvas.disableMouseEvents();
            timer.start();
            canvas.itemTimer.start();
        });
        add(start);

        add(putRank());

        timerLabel = new JLabel(TIME_20);
        timerLabel.setFont(dodum);
        timerLabel.setBounds(30, height + 140, 80, 15);
        add(timerLabel);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remainingTime--;
                timerLabel.setText("시간: " + remainingTime);
                if (remainingTime == 0) {
                    endLife = Integer.parseInt(lifeLabel.getText().replace("생명: ", ""));
                    endTime = remainingTime;
                    algorithm.reset();
                    algorithm.updateUI();
                    timer.stop();
                    canvas.timer.stop();
                    canvas.item = null;
                    canvas.pathTimer.stop();
                    canvas.RemoveKeyListener();
                    System.out.println("게임이 끝났습니다.");
                    canvas.easyMap();
                    selectionType = SelectionType.START;
                    lifeCount = 3;
                    lifeLabel.setText("생명: " + lifeCount);
                    canvas.easyMap();
                    timerLabel.setText(TIME_60);
                    canvas.showEndGameDialog(true);
                }
            }
        });

        lifeLabel = new JLabel("생명: " + lifeCount);
        lifeLabel.setFont(dodum);
        lifeLabel.setBounds(30, height + 160, 80, 15);
        add(lifeLabel);

        JButton save = new JButton("save");
        save.setFont(dodum);
        save.setBounds(140, height + 150, 70, 30);
        save.addActionListener((ActionEvent ae) -> {
            try {
                Writer w = new FileWriter("test.txt");
                TableModel model = rankList.getModel();
                // 행(row)의 수와 열(column)의 수 가져오기
                int rowCount = model.getRowCount();
                int columnCount = model.getColumnCount();
                StringBuilder sb = new StringBuilder();
                // 각 행과 열의 값을 가져오기
                for (int row = 0; row < rowCount; row++) {
                    boolean check = true;
                    for (int column = 0; column < columnCount; column++) {
                        if (model.getValueAt(row, column) == null) {
                            check = false;
                            break;
                        }
                        if (check) sb.append(model.getValueAt(row, column) + " ");
                    }
                    sb.append("\n");
                }
                w.write(sb.toString());
                w.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        add(save);

        JButton load = new JButton("load");
        load.setFont(dodum);
        load.setBounds(220, height + 150, 70, 30);
        load.addActionListener((ActionEvent ae) -> {
            try {
                Reader r = new FileReader("test.txt");
                StringBuilder sb = new StringBuilder();
                while (true) {
                    int read = r.read();
                    sb.append((char) read);
                    if (read == -1) break;
                }
                String s = sb.toString();
                r.close();
                String[] split = s.split("\n");
                for (String content : split) {
                    String[] s1 = content.split(" ");
                    if (s1.length == 4) {
                        canvas.contents[contentSize][0] = s1[0];
                        canvas.contents[contentSize][1] = s1[1];
                        canvas.contents[contentSize][2] = s1[2];
                        canvas.contents[contentSize][3] = s1[3];
                        canvas.controls.putRank();
                        contentSize++;
                    }

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        add(load);
    }

    //랭크 넣기
    public JScrollPane putRank() {
        rankList = new JTable(GridPanel.contents, GridPanel.header);
        rankList.disable();
        rankList.setFont(dodum);
        JScrollPane scrollPane = new JScrollPane(rankList);
        scrollPane.setBounds(10, 320, 280, 270);
        add(scrollPane);

        return scrollPane;
    }

    //주어진 타일을 선택해서 유저 몬스터 벽을 생성
    public void selectTile(Tile t) {
        Tile start = (Tile) algorithm.getStart();
        Tile end = (Tile) algorithm.getEnd();
        switch (selectionType) {
            case START:
                if (t.isValid()) {
                    algorithm.setStart(t);
                    selectionType = SelectionType.END;
                    selector.setSelectedIndex(1);
                } else {
                    canvas.showCanNotBuild();
                }
                break;
            case END:
                if (t.isValid()) {
                    algorithm.setEnd(t);
                    selectionType = SelectionType.HILL;
                    selector.setSelectedIndex(2);
                } else {
                    canvas.showCanNotBuild();
                }
                break;
            case HILL:
                if (((t.getX() == start.getX()) && (t.getY() == start.getY())) || ((t.getX() == end.getX()) && (t.getY() == end.getY()))) {
                    canvas.showCanNotBuild();
                } else {
                    t.setWeight(3000);
                }
                break;
            default:
                if (start == null || end == null) {
                    t.reverseValidation();
                }
                if (((t.getX() == start.getX()) && (t.getY() == start.getY())) || ((t.getX() == end.getX()) && (t.getY() == end.getY()))) {
                    canvas.showCanNotBuild();
                } else {
                    t.reverseValidation();
                }
                break;
        }

        algorithm.updateUI();
    }

    //레벨 타입 선택
    public void selectLevel() {
        switch (levelType) {
            case EASY:
                canvas.easyMap();
                setTimeText.setText(TIME_20.replace("시간: ", ""));
                setLifeText.setText(String.valueOf(lifeCount));
                setSpeedText.setText("500");
                setSpeedText.disable();
                setLifeText.disable();
                setTimeText.disable();
                timerLabel.setText(TIME_20);
                break;
            case NORMAL:
                canvas.normalMap();
                setTimeText.setText(TIME_40.replace("시간: ", ""));
                setLifeText.setText(String.valueOf(lifeCount));
                setSpeedText.setText("250");
                setSpeedText.disable();
                setLifeText.disable();
                setTimeText.disable();
                timerLabel.setText(TIME_40);
                break;
            case HARD:
                canvas.hardMap();
                setTimeText.setText(TIME_60.replace("시간: ", ""));
                setLifeText.setText(String.valueOf(lifeCount));
                setSpeedText.setText("100");
                setSpeedText.disable();
                setLifeText.disable();
                setTimeText.disable();
                timerLabel.setText(TIME_60);
                break;
            case CUSTOM:
                canvas.customMap();
                setSpeedText.enable();
                setLifeText.enable();
                setTimeText.enable();
                timerLabel.setText(TIME_60);
                break;
        }

        algorithm.updateUI();
    }

    public void selectTiles(Tile t) {
        algorithm.setStart(t);
        algorithm.updateUI();
    }

    public void setGridPanel(GridPanel canvas) {
        this.canvas = canvas;
    }

    enum SelectionType {
        START, END, HILL, REVERSE
    }

    public enum LevelType {
        EASY, NORMAL, HARD, CUSTOM
    }
}
