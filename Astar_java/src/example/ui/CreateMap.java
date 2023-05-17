package example.ui;

public class CreateMap {
    private GridPanel gridPanel;

    public CreateMap(GridPanel gridPanel) {
        this.gridPanel = gridPanel;
    }

    public void easyMap() {
        gridPanel.createWall(1, 2);
    }
}
