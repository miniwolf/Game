package mini.editor.util;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class FXUtils {
    public static void bindFixed(final Region region,
                                 final ObservableValue<? extends Number> width,
                                 final ObservableValue<? extends Number> height) {
        region.minWidthProperty().bind(width);
        region.maxWidthProperty().bind(width);
        region.minHeightProperty().bind(height);
        region.maxHeightProperty().bind(height);
    }

    public static void setFixedSize(final Region region,
                                    final double width,
                                    final double height) {
        region.setMaxHeight(height);
        region.setMinHeight(height);
        region.setMaxWidth(width);
        region.setMinWidth(width);
    }

    public static void setFixedWidth(final Region region,
                                     final double width) {
        region.setMinWidth(width);
        region.setMaxWidth(width);
    }

    public static void setFixedHeight(final Region region,
                                      final double height) {
        region.setMinHeight(height);
        region.setMaxHeight(height);
    }

    public static void addChild(Pane parent, Node... nodes) {
        parent.getChildren().addAll(nodes);
    }
}
