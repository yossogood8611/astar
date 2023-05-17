package example.ui;

import example.element.Tile;
import pathfinding.AStarAlgorithm;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ControlsPanel extends JPanel {

    private AStarAlgorithm algorithm;
    private SelectionType selectionType;

    private JComboBox<String> selector;
    private GridPanel canvas;

    private Timer timer;
    JLabel timerLabel;
    JLabel lifeLabel;

    private int lifeCount = 3;

    public void resetGameSetting(){
        timer.stop();
        timerLabel.setText("Time: 60");
        lifeCount = 3;
        lifeLabel.setText("Life: " + lifeCount);
    }

    public void lifeDown(){
        this.lifeCount--;
        this.lifeLabel.setText("Life: " + this.lifeCount);
    }

    public boolean isLifeZero(){
        if(lifeCount==0) return true;
        else return false;
    }

    public int getLifeCount(){
        return this.lifeCount;
    }

    public Timer getTimer(){
        return this.timer;
    }
    public void setLifeCount(int lifeCount){
       this.lifeCount = lifeCount;
    }

    public void setTimer(Timer timer){
        this.timer = timer;
    }

    public ControlsPanel(int width, int height, AStarAlgorithm algorithm) {

        this.algorithm = algorithm;
        this.selectionType = SelectionType.START;

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
        levelSelector.addItem("Medium");
        levelSelector.addItem("Hard");
        levelSelector.setBounds(10, height - 50, width - 20, 30);
        levelSelector.addActionListener((ActionEvent e) -> {
            String selectedLevel = (String) levelSelector.getSelectedItem();
           });
        add(levelSelector);


        JButton reset = new JButton("Reset");
        reset.setBounds(10, height - 15, 80, 30);
        reset.addActionListener((ActionEvent ae) -> {
            algorithm.reset();
            algorithm.updateUI();
            selectionType = SelectionType.START;
            timer.stop();
            timerLabel.setText("Time: 60");
            lifeCount = 3;
            lifeLabel.setText("life: " + lifeCount);
        });
        add(reset);

        JButton start = new JButton("Start");
        start.setBounds(110, height - 15, 80, 30);
        start.addActionListener((ActionEvent ae) -> {
            algorithm.solve();
            canvas.startUserMovement();
            canvas.setCheck(false);
            canvas.disableMouseEvents();
            timer.start();
        });
        add(start);

        // Inside the ControlsPanel constructor
        timerLabel = new JLabel("Time: 10"); // Initial time can be set to 60 seconds
        timerLabel.setBounds(20, height+20 , 80, 30);
        add(timerLabel);

        timer = new Timer(1000, (ActionEvent e) -> {
            int remainingTime = Integer.parseInt(timerLabel.getText().replace("Time: ", ""));
            remainingTime--;
            timerLabel.setText("Time: " + remainingTime);
            if (remainingTime == 0) {
                canvas.showEndGameDialog(true);
                algorithm.reset();
                algorithm.updateUI();
                selectionType = SelectionType.START;
                timer.stop();
                timerLabel.setText("Time: 60");
                lifeCount = 3;
                lifeLabel.setText("life: " + lifeCount);
            }
        });

        // Inside the ControlsPanel constructor
        lifeLabel = new JLabel("Life: " + lifeCount); // Initial life count can be set to 3
        lifeLabel.setBounds(120, height+20 , 80, 30);
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
                selector.setSelectedIndex(2);
                break;
            default:
                t.reverseValidation();
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

    private enum SelectionType {
        START, END, REVERSE
    }

}
