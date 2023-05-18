package pathfinding.element;

import example.element.Grid;
import example.element.Tile;

import java.util.ArrayList;

public abstract class Node {

    private Tile parent;
    private ArrayList<Tile> neighbours;
    private double cost, heuristic, function;
    private boolean valid;

    public abstract void calculateNeighbours(Network network);

    public abstract double distanceTo(Tile dest);

    public abstract double heuristic(Tile dest, Grid grid);

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(double heuristic) {
        this.heuristic = heuristic;
    }

    public double getFunction() {
        return function;
    }

    public void setFunction(double function) {
        this.function = function;
    }


    public ArrayList<Tile> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(ArrayList<Tile> neighbours) {
        this.neighbours = neighbours;
    }

    public Tile getParent() {
        return parent;
    }

    public void setParent(Tile parent) {
        this.parent = parent;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void reverseValidation() {
        valid = !valid;
    }

}
