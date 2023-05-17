package example.ui;

import example.element.Tile;
import pathfinding.AStarAlgorithm;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControlsPanel extends JPanel {

    public static final String TIME_60 = "Time: 60";
    public static final String TIME_40 = "Time: 40";
    public static final String TIME_20 = "Time: 2";
    private AStarAlgorithm algorithm;
    public static SelectionType selectionType;
    private LevelType levelType;

    private JComboBox<String> selector;
    private GridPanel canvas;

    public Timer timer;
    JLabel timerLabel;
    JLabel lifeLabel;
    public static JTextField setSpeedText;
    public static JTextField setTimeText;
    public static JTextField setLifeText;

    int remainingTime;

    LevelType levelState;

    private int lifeCount = 3;

    public void resetGameSetting() {
        timer.stop();
        canvas.timer.stop();
        canvas.pathTimer.stop();
        timerLabel.setText(TIME_60);
        lifeCount = 3;
        lifeLabel.setText("Life: " + lifeCount);
        algorithm.reset();
        algorithm.updateUI();
    }

    public void lifeDown() {
        this.lifeCount--;
        this.lifeLabel.setText("Life: " + this.lifeCount);
    }

    public boolean isLifeZero() {
        if (lifeCount == 0) return true;
        else return false;
    }

    public int getLifeCount() {
        return this.lifeCount;
    }

    public Timer getTimer() {
        return this.timer;
    }

    public void setLifeCount(int lifeCount) {
        this.lifeCount = lifeCount;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public ControlsPanel(int width, int height, AStarAlgorithm algorithm) {

        this.algorithm = algorithm;
        this.selectionType = SelectionType.START;
        this.levelType = LevelType.EASY;
        this.levelState = LevelType.EASY;

        setBorder(new LineBorder(Color.gray));
        setLayout(null);

        Label selectionLabel = new Label("Selection type:");
        selectionLabel.setBounds(7, 10, width - 20, 25);
        add(selectionLabel);

        selector = new JComboBox<>();
        selector.addItem("Start");
        selector.addItem("End");
        selector.addItem("Obstacle");
        selector.setBounds(10, 35, width - 20, 30);
        selector.addActionListener((ActionEvent e) -> {
            selectionType = selectionType.values()[selector.getSelectedIndex()];
        });
        add(selector);

        JComboBox<String> levelSelector = new JComboBox<>();
        levelSelector.addItem("Easy");
        levelSelector.addItem("Normal");
        levelSelector.addItem("Hard");
        levelSelector.addItem("Custom");
        levelSelector.setBounds(10, height - 50, width - 20, 30);
        levelSelector.addActionListener((ActionEvent e) -> {
            levelType = levelType.values()[levelSelector.getSelectedIndex()];
            selectLevel();
        });
        add(levelSelector);

        Label speedLabel = new Label("speed:");
        speedLabel.setBounds(10, height - 10, width - 20, 10);
        add(speedLabel);
        setSpeedText = new JTextField();
        setSpeedText.disable();
        setSpeedText.setText(String.valueOf(300));
        setSpeedText.setBounds(10, height + 5, width - 20, 30);
        add(setSpeedText);

        Label timeLabelText = new Label("time:");
        timeLabelText.setBounds(10, height + 40, 30, 10);
        add(timeLabelText);
        setTimeText = new JTextField();
        setTimeText.disable();
        setTimeText.setText(String.valueOf(60));
        setTimeText.setBounds(10, height + 55, 85, 30);
        add(setTimeText);

        Label lifeLabelText = new Label("life:");
        lifeLabelText.setBounds(105, height + 40, 30, 10);
        add(lifeLabelText);
        setLifeText = new JTextField();
        setLifeText.disable();
        setLifeText.setText(String.valueOf(3));
        setLifeText.setBounds(105, height + 55, 85, 30);
        add(setLifeText);

        JButton reset = new JButton("Reset");
        reset.setBounds(10, height + 100, 85, 30);
        reset.addActionListener((ActionEvent ae) -> {
            algorithm.reset();
            algorithm.updateUI();
            selectionType = SelectionType.START;
            canvas.setCheck(true);
            timer.stop();
            timerLabel.setText(TIME_60);
            lifeCount = 3;
            lifeLabel.setText("life: " + lifeCount);
        });
        add(reset);

        JButton start = new JButton("Start");
        start.setBounds(105, height + 100, 85, 30);
        start.addActionListener((ActionEvent ae) -> {
            remainingTime = Integer.parseInt(setTimeText.getText().replace("Time: ", ""));
            lifeCount = Integer.parseInt(setLifeText.getText());
            algorithm.solve();
            canvas.startUserMovement(levelType);
            canvas.setCheck(false);
            canvas.disableMouseEvents();
            timer.start();
        });
        add(start);

        // Inside the ControlsPanel constructor
        timerLabel = new JLabel(TIME_20); // Initial time can be set to 60 seconds
        timerLabel.setBounds(30, height + 130, 80, 30);
        add(timerLabel);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remainingTime--;
                timerLabel.setText("Time: " + remainingTime);
                if (remainingTime == 0) {
                    timer.stop();
                    canvas.timer.stop();
                    canvas.pathTimer.stop();
                    canvas.RemoveKeyListener();
                    System.out.println("게임이 끝났습니다.");
                    canvas.showEndGameDialog(true);
                    selectionType = SelectionType.START;
                    timerLabel.setText(TIME_60);
                    lifeCount = 3;
                    lifeLabel.setText("life: " + lifeCount);
                    algorithm.reset();
                    algorithm.updateUI();
                    canvas.easyMap();
                }
            }
        });
        // Inside the ControlsPanel constructor
        lifeLabel = new JLabel("Life: " + lifeCount); // Initial life count can be set to 3
        lifeLabel.setBounds(130, height + 130, 80, 30);
        add(lifeLabel);
    }

    public void selectTile(Tile t) {
        switch (selectionType) {
            case START:
                algorithm.setStart(t);
                selectionType = SelectionType.END;
                selector.setSelectedIndex(1);
                break;
            case END:
                algorithm.setEnd(t);
                selectionType = SelectionType.REVERSE;
                try {
                    selector.setSelectedIndex(2);
                } catch (Exception e) {

                }

                break;
            default:
                t.reverseValidation();
                break;
        }

        algorithm.updateUI();
    }

    public void selectLevel() {
        switch (levelType) {
            case EASY:
                canvas.easyMap();
                setTimeText.setText(TIME_20.replace("Time: ", ""));
                setLifeText.setText(String.valueOf(lifeCount));
                setSpeedText.disable();
                setLifeText.disable();
                setTimeText.disable();
                timerLabel.setText(TIME_20);
                break;
            case NORMAL:
                canvas.normalMap();
                setTimeText.setText(TIME_40.replace("Time: ", ""));
                setLifeText.setText(String.valueOf(lifeCount));
                setSpeedText.disable();
                setLifeText.disable();
                setTimeText.disable();
                timerLabel.setText(TIME_40);
                break;
            case HARD:
                canvas.hardMap();
                setTimeText.setText(TIME_60.replace("Time: ", ""));
                setLifeText.setText(String.valueOf(lifeCount));
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
        START, END, REVERSE
    }

    public enum LevelType {
        EASY, NORMAL, HARD, CUSTOM
    }
}
