package example.element;

import pathfinding.element.Network;
import pathfinding.element.Node;

import java.awt.*;
import java.util.ArrayList;

public class Tile extends Node {

    private int x, y, weight;
    public static int TILE_SIZE = 30;
    private boolean check = false;

    private Grid grid;


    public void setCheck(boolean check) {
        this.check = check;
    }

    public boolean isCheck() {
        return check;
    }

    public Tile(int x, int y, int weight) {
        this.x = x;
        this.y = y;
        this.weight = weight;
        setValid(true);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWeight() {
        return weight;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public void calculateNeighbours(Network network) {

        Grid grid = (Grid) network;

        ArrayList<Node> nodes = new ArrayList<>();

        extracted(grid, nodes);

        setNeighbours(nodes);

    }

    public void extracted(Grid grid, ArrayList<Node> nodes) {
        int minX = 0;
        int minY = 0;
        int maxX = grid.getWidth() - 1;
        int maxY = grid.getHeight() - 1;

        if (x > minX) {
            nodes.add(grid.find(x - 1, y)); //west
        }

        if (x < maxX) {
            nodes.add(grid.find(x + 1, y)); //east
        }

        if (y > minY) {
            nodes.add(grid.find(x, y - 1)); //north
        }

        if (y < maxY) {
            nodes.add(grid.find(x, y + 1)); //south
        }

        if (x > minX && y > minY) {
            nodes.add(grid.find(x - 1, y - 1)); //northwest
        }

        if (x < maxX && y < maxY) {
            nodes.add(grid.find(x + 1, y + 1)); //southeast
        }

        if (x < maxX && y > minY) {
            nodes.add(grid.find(x + 1, y - 1)); //northeast
        }

        if (x > minY && y < maxY) {
            nodes.add(grid.find(x - 1, y + 1)); //southwest
        }
    }

    @Override
    public double heuristic(Tile dest, Grid grid) {

        if (dest.getWeight() == 3) {
            System.out.println("웨이트");
            return distanceToWeight(dest);
        } else {
            return distanceTo(dest);
        }
    }

    @Override
    public double distanceTo(Node dest) {
        Tile d = (Tile) dest;
        return new Point(x, y).distance(new Point(d.x, d.y));
    }

    public double distanceToWeight(Node dest) {
        Tile d = (Tile) dest;
        return new Point(x, y).distance(new Point(d.x, d.y)) * d.weight;
    }

}
