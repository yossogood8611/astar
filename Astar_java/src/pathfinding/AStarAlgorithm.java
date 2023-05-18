package pathfinding;

import example.element.Grid;
import example.element.Tile;
import pathfinding.element.Network;

import java.util.ArrayList;
import java.util.Observable;

public class AStarAlgorithm extends Observable {

    private Network network;
    private ArrayList<Tile> path;

    private Tile start;
    private Tile end;

    private ArrayList<Tile> openList;
    private ArrayList<Tile> closedList;

    public AStarAlgorithm(Network network) {
        this.network = network;
    }

    public void solve() {

        if (start == null && end == null) {
            return;
        }

        if (start.equals(end)) {
            this.path = new ArrayList<>();
            return;
        }

        this.path = new ArrayList<>();

        this.openList = new ArrayList<>();
        this.closedList = new ArrayList<>();

        this.openList.add(start);

        while (!openList.isEmpty()) {
            Tile current = getLowestF();

            if (current.equals(end)) {
                retracePath(current);
                break;
            }

            openList.remove(current);
            closedList.add(current);

            for (Tile n : current.getNeighbours()) {

                if (closedList.contains(n) || !n.isValid()) {
                    continue;
                }

                double tempScore = current.getCost() + current.distanceTo(n);

                if (openList.contains(n)) {
                    if (tempScore < n.getCost()) {
                        n.setCost(tempScore);
                        n.setParent(current);
                    }
                } else {
                    n.setCost(tempScore);
                    openList.add(n);
                    n.setParent(current);
                }

                n.setHeuristic(n.heuristic(n, (Grid) network));
                n.setFunction(n.getCost() + n.getHeuristic());

            }

        }

        updateUI();
    }

    public void reset() {
        this.start = null;
        this.end = null;
        this.path = null;
        this.openList = null;
        this.closedList = null;
        for (Tile n : network.getNodes()) {
            n.setValid(true);
            n.setWeight(1);
        }
    }
    public void reset(Tile start,Tile end) {
        this.start = start;
        this.end = end;
        this.path = null;
        this.openList = null;
        this.closedList = null;
    }

    private void retracePath(Tile current) {
        Tile temp = current;
        this.path.add(current);

        while (temp.getParent() != null) {
            this.path.add(temp.getParent());
            temp = temp.getParent();
            if(temp.equals(start)){
                break;
            }
        }

        this.path.add(start);
    }

    private Tile getLowestF() {
        Tile lowest = openList.get(0);
        for (Tile n : openList) {
            if (n.getFunction() < lowest.getFunction()) {
                lowest = n;
            }
        }
        return lowest;
    }

    public void updateUI() {
        setChanged();
        notifyObservers();
        clearChanged();
    }

    public Network getNetwork() {
        return network;
    }

    public ArrayList<Tile> getPath() {
        return path;
    }

    public Tile getStart() {
        return start;
    }

    public Tile getEnd() {
        return end;
    }

    public void setStart(Tile start) {
        this.start = start;
    }

    public void setEnd(Tile end){
            this.end = end;
    }

}
