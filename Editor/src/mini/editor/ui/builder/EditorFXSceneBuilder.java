package mini.editor.ui.builder;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mini.editor.Messages;
import mini.editor.config.EditorConfig;
import mini.editor.ui.component.asset.AssetComponent;
import mini.editor.ui.component.bar.EditorMenuBarComponent;
import mini.editor.ui.component.editor.area.EditorAreaComponent;
import mini.editor.ui.component.log.LogView;
import mini.editor.ui.component.splt.pane.GlobalBottomToolSplitPane;
import mini.editor.ui.component.splt.pane.GlobalLeftToolSplitPane;
import mini.editor.ui.component.tab.GlobalBottomToolComponent;
import mini.editor.ui.component.tab.GlobalLeftToolComponent;
import mini.editor.ui.css.CssClasses;
import mini.editor.ui.scene.EditorFXScene;

import static javafx.scene.paint.Color.TRANSPARENT;

public class EditorFXSceneBuilder {

    public static EditorFXScene build(Stage stage) {
        EditorConfig instance = EditorConfig.getInstance();
        var root = new Group();

        var scene = new EditorFXScene(root);
        scene.setFill(TRANSPARENT);
        scene.setRoot(root);

        // TODO: Handling stylesheets

        var container = scene.getContainer();

        build(scene, container, stage);

        stage.setScene(scene);

        return scene;
    }

    private static void build(EditorFXScene scene, StackPane container, Stage stage) {
        var canvas = scene.getCanvas();
        var barComponent = new EditorMenuBarComponent();
        var editorAreaComponent = new EditorAreaComponent();

        var leftSplitContainer = new GlobalLeftToolSplitPane(scene);
        leftSplitContainer.prefHeightProperty()
                          .bind(container.heightProperty());

        var bottomSplitContainer = new GlobalBottomToolSplitPane(scene);

        var globalLeftToolComponent = new GlobalLeftToolComponent(leftSplitContainer);
        globalLeftToolComponent.addComponent(new AssetComponent(), Messages.EDITOR_TOOL_ASSET);

        var globalBottomToolComponent = new GlobalBottomToolComponent(
                bottomSplitContainer);
        globalBottomToolComponent.addComponent(LogView.getInstance(), Messages.LOG_VIEW_TITLE);

        leftSplitContainer.initFor(globalLeftToolComponent, bottomSplitContainer);
        bottomSplitContainer.initFor(globalBottomToolComponent, editorAreaComponent);

        leftSplitContainer.getStyleClass().add(CssClasses.MAIN_SPLIT_PANEL);
        bottomSplitContainer.getStyleClass().add(CssClasses.MAIN_SPLIT_PANEL);

        container.getChildren().add(new VBox(barComponent, leftSplitContainer));

        leftSplitContainer.prefHeightProperty().bind(container.widthProperty());
        barComponent.prefWidthProperty().bind(container.widthProperty());
    }
}
