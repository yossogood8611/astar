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

        JButton reset = new JButton("Reset");
        reset.setBounds(10, height - 40, 80, 30);
        reset.addActionListener((ActionEvent ae) -> {
            algorithm.reset();
            algorithm.updateUI();
            selectionType = SelectionType.START;
        });
        add(reset);

        JButton start = new JButton("Start");
        start.setBounds(110, height - 40, 80, 30);
        start.addActionListener((ActionEvent ae) -> {
            algorithm.solve();
            canvas.startUserMovement();
        });
        add(start);
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

    public void setGridPanel(GridPanel canvas) {
        this.canvas = canvas;
    }

    private enum SelectionType {
        START, END, REVERSE
    }

}
