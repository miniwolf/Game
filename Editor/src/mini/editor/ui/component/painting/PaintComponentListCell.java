package mini.editor.ui.component.painting;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;

public class PaintComponentListCell extends ListCell<PaintingComponent> {
    private final ImageView imageView;

    public PaintComponentListCell(ListView<PaintingComponent> listView) {
        imageView = new ImageView();
        setGraphic(imageView);
    }

    @Override
    protected void updateItem(PaintingComponent item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null) {
            imageView.setImage(null);
            setText("");
            return;
        }

        imageView.setImage(item.getIcon());
        setText(item.getName());
    }
}
