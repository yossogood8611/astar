package example.element;

import pathfinding.element.Network;
import pathfinding.element.Node;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

public class Grid extends Network {

    private int width, height;
    private ArrayList<Tile> tiles;

    public Grid(int width, int height, ArrayList<Tile> tiles) {
        this.width = width;
        this.height = height;
        this.tiles = tiles;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

    public Tile find(int x, int y) {
        for (Tile t : tiles) {
            if (t.getX() == x && t.getY() == y)
                return t;
        }
        return null;
    }

    public Tile findEmptyTile(){
        List<Tile> emptyTiles = new ArrayList<>();
        for (Tile t : tiles) {
            if (t.isValid()) {
                emptyTiles.add(t);
            }
        }
        if (!emptyTiles.isEmpty()) {
            Collections.shuffle(emptyTiles, new Random());
            return emptyTiles.get(0);
        }
        return null;
    }

    public boolean hasTileWithWeight(){
        for(Tile tile : tiles){
            if(tile.getWeight() == 3){
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<Node> getNodes() {
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.addAll(tiles);
        return nodes;
    }
}
