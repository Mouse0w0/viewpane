package com.github.mouse0w0.viewpane;

import com.github.mouse0w0.viewpane.geometry.EightPos;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;

public class ViewGroup {

    public ViewGroup(EightPos pos) {
        this.pos = pos;

        this.selectionModel = new ViewGroupSelectionModel(this);

        getTabs().addListener(new ListChangeListener<ViewTab>() {
            @Override
            public void onChanged(Change<? extends ViewTab> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        for (ViewTab viewTab : c.getRemoved()) {
                            viewTab.setViewGroup(null);
                        }
                    }
                    if (c.wasAdded()) {
                        for (ViewTab viewTab : c.getAddedSubList()) {
                            viewTab.setViewGroup(ViewGroup.this);
                        }
                    }
                }
            }
        });
    }

    private final EightPos pos;

    public final EightPos getPos() {
        return pos;
    }

    private final ObservableList<ViewTab> tabs = FXCollections.observableArrayList();

    public final ObservableList<ViewTab> getTabs() {
        return tabs;
    }

    private final SingleSelectionModel<ViewTab> selectionModel;

    public final SingleSelectionModel<ViewTab> getSelectionModel() {
        return selectionModel;
    }

    private ReadOnlyObjectWrapper<ViewPane> viewPane;

    private ReadOnlyObjectWrapper<ViewPane> viewPanePropertyImpl() {
        if (viewPane == null) {
            viewPane = new ReadOnlyObjectWrapper<>(this, "viewPane");
        }
        return viewPane;
    }

    final void setViewPane(ViewPane viewPane) {
        viewPanePropertyImpl().set(viewPane);
    }

    public final ReadOnlyObjectProperty<ViewPane> viewPaneProperty() {
        return viewPanePropertyImpl().getReadOnlyProperty();
    }

    public final ViewPane getViewPane() {
        return viewPane == null ? null : viewPane.get();
    }

    static class ViewGroupSelectionModel extends SingleSelectionModel<ViewTab> {

        private final ViewGroup viewGroup;

        public ViewGroupSelectionModel(ViewGroup viewGroup) {
            this.viewGroup = viewGroup;

            viewGroup.getTabs().addListener(new ListChangeListener<ViewTab>() {
                @Override
                public void onChanged(Change<? extends ViewTab> c) {
                    while (c.next()) {
                        if (c.wasRemoved()) {
                            for (ViewTab viewTab : c.getRemoved()) {
                                if (viewTab.isSelected()) clearSelection();
                            }
                        }
                    }
                }
            });
            selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue != null) oldValue.setSelected(false);
                if (newValue != null) newValue.setSelected(true);
            });
        }

        @Override
        protected ViewTab getModelItem(int index) {
            if (index < 0 || index > getItemCount()) return null;
            return viewGroup.getTabs().get(index);
        }

        @Override
        protected int getItemCount() {
            return viewGroup.getTabs().size();
        }
    }
}
