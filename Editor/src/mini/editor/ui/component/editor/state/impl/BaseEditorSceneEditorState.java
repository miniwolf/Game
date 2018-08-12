package mini.editor.ui.component.editor.state.impl;

import mini.editor.annotation.FxThread;

public abstract class BaseEditorSceneEditorState extends Editor3DWithEditorToolEditorState {
    private boolean enabledSelection;
    private boolean enabledGrid;
    private int transformationMode;
    private int transformationType;
    private boolean enableGrid;

    @FxThread
    public void setEnableSelection(final boolean enableSelection) {
        final boolean changed = isEnabledSelection() != enableSelection;
        this.enabledSelection = enableSelection;
        if (changed) {
            notifyChanged();
        }
    }

    @FxThread
    public boolean isEnabledSelection() {
        return enabledSelection;
    }

    @FxThread
    public boolean isEnabledGrid() {
        return enabledGrid;
    }

    @FxThread
    public void setEnabledGrid(boolean enabledGrid) {
        final boolean changed = isEnabledGrid() != enabledGrid;
        this.enabledGrid = enabledGrid;
        if (changed) {
            notifyChanged();
        }
    }

    @FxThread
    public int getTransformationMode() {
        return transformationMode;
    }

    @FxThread
    public void setTransformationMode(int transformationMode) {
        final boolean changed = getTransformationMode() != transformationMode;
        this.transformationMode = transformationMode;
        if (changed) {
            notifyChanged();
        }
    }

    @FxThread
    public int getTransformationType() {
        return transformationType;
    }

    @FxThread
    public void setTransformationType(final int transformationType) {
        final boolean changed = getTransformationType() != transformationType;
        this.transformationType = transformationType;
        if (changed) {
            notifyChanged();
        }
    }

    @FxThread
    public void setEnableGrid(final boolean enableGrid) {
        final boolean changed = getEnableGrid() != enableGrid;
        this.enableGrid = enableGrid;
        if (changed) {
            notifyChanged();
        }
    }

    public boolean getEnableGrid() {
        return enableGrid;
    }
}
