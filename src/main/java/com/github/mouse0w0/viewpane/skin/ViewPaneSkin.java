package com.github.mouse0w0.viewpane.skin;

import com.github.mouse0w0.viewpane.DividerType;
import com.github.mouse0w0.viewpane.ViewGroup;
import com.github.mouse0w0.viewpane.ViewPane;
import com.github.mouse0w0.viewpane.ViewTab;
import com.github.mouse0w0.viewpane.geometry.EightPos;
import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import com.sun.javafx.scene.control.skin.LabeledSkinBase;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;

public class ViewPaneSkin extends SkinBase<ViewPane> {
    private final SideBarArea sideBarArea;
    private final DivisionArea divisionArea;

    public ViewPaneSkin(ViewPane control) {
        super(control);

        sideBarArea = new SideBarArea();
        getChildren().add(sideBarArea);

        divisionArea = new DivisionArea(this);
        sideBarArea.setCenter(divisionArea);

        getViewGroups().forEach(this::createTabButtonBar);
        getViewGroups().addListener(new ListChangeListener<ViewGroup>() {
            @Override
            public void onChanged(Change<? extends ViewGroup> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        for (ViewGroup viewGroup : c.getRemoved()) {
                            createTabButtonBar(viewGroup);
                        }
                    }
                    if (c.wasAdded()) {
                        for (ViewGroup viewGroup : c.getAddedSubList()) {
                            removeTabButtonBar(viewGroup);
                        }
                    }
                }
            }
        });

        Node center = getSkinnable().getCenter();
        if (center != null) divisionArea.setCenter(center);
        control.centerProperty().addListener(observable -> divisionArea.setCenter(getSkinnable().getCenter()));
    }

    public ObservableList<ViewGroup> getViewGroups() {
        return getSkinnable().getViewGroups();
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return snappedLeftInset() + snapSize(sideBarArea.prefWidth(-1)) + snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return snappedTopInset() + snapSize(sideBarArea.prefHeight(-1)) + snappedBottomInset();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        sideBarArea.resize(contentWidth, contentHeight);
        sideBarArea.relocate(contentX, contentY);
    }

    private void createTabButtonBar(ViewGroup viewGroup) {
        EightPos pos = viewGroup.getPos();
        SideBar sideBar = getSideBar(pos.getPrimary());

        Side secondary = pos.getSecondary();
        if (secondary == Side.TOP || secondary == Side.LEFT) {
            TabButtonBar tabButtonBar = sideBar.getTopLeftBar();
            if (tabButtonBar == null || tabButtonBar.getViewGroup() != viewGroup) {
                tabButtonBar = new TabButtonBar(this, viewGroup);
                sideBar.setTopLeftBar(tabButtonBar);
            }
        } else {
            TabButtonBar tabButtonBar = sideBar.getBottomRightBar();
            if (tabButtonBar == null || tabButtonBar.getViewGroup() != viewGroup) {
                tabButtonBar = new TabButtonBar(this, viewGroup);
                sideBar.setBottomRightBar(tabButtonBar);
            }
        }
    }

    private void removeTabButtonBar(ViewGroup viewGroup) {
        EightPos pos = viewGroup.getPos();
        SideBar sideBar = getSideBar(pos.getPrimary());

        Side secondary = pos.getSecondary();
        if (secondary == Side.TOP || secondary == Side.LEFT) {
            TabButtonBar bar = sideBar.getTopLeftBar();
            if (bar.getViewGroup() == viewGroup) {
                sideBar.setTopLeftBar(null);
                bar.dispose();
            }
        } else {
            TabButtonBar bar = sideBar.getBottomRightBar();
            if (bar.getViewGroup() == viewGroup) {
                sideBar.setBottomRightBar(null);
                bar.dispose();
            }
        }
    }

    private SideBar getSideBar(Side side) {
        SideBar sideBar = sideBarArea.getSideBar(side);
        if (sideBar == null) {
            sideBar = new SideBar(side);
            if (side == Side.LEFT)
                sideBarArea.setLeft(sideBar);
            else if (side == Side.RIGHT)
                sideBarArea.setRight(sideBar);
            else if (side == Side.BOTTOM)
                sideBarArea.setBottom(sideBar);
            else
                sideBarArea.setTop(sideBar);
        }
        return sideBar;
    }

    static class SideBarArea extends Region {
        private SideBar top;
        private SideBar left;
        private SideBar bottom;
        private SideBar right;
        private Node center;

        public SideBarArea() {
            getStyleClass().setAll("side-bar-area");
        }

        public SideBar getSideBar(Side side) {
            switch (side) {
                case TOP:
                    return top;
                case LEFT:
                    return left;
                case BOTTOM:
                    return bottom;
                case RIGHT:
                    return right;
                default:
                    throw new NullPointerException();
            }
        }

        public void setTop(SideBar top) {
            if (this.top != null) getChildren().remove(this.top);
            this.top = top;
            if (top != null) getChildren().add(top);
        }

        public void setLeft(SideBar left) {
            if (this.left != null) getChildren().remove(this.left);
            this.left = left;
            if (left != null) getChildren().add(left);
        }

        public void setBottom(SideBar bottom) {
            if (this.bottom != null) getChildren().remove(this.bottom);
            this.bottom = bottom;
            if (bottom != null) getChildren().add(bottom);
        }

        public void setRight(SideBar right) {
            if (this.right != null) getChildren().remove(this.right);
            this.right = right;
            if (right != null) getChildren().add(right);
        }

        public void setCenter(Node center) {
            if (this.center != null) getChildren().remove(this.center);
            this.center = center;
            if (center != null) getChildren().add(center);
        }

        @Override
        protected double computePrefWidth(double height) {
            return snappedLeftInset() +
                    computeChildPrefHeight(left) +
                    max(computeChildPrefWidth(top), computeChildPrefWidth(bottom), computeChildPrefWidth(center)) +
                    computeChildPrefHeight(right) +
                    snappedRightInset();
        }

        @Override
        protected double computePrefHeight(double width) {
            return snappedTopInset() +
                    computeChildPrefHeight(top) +
                    max(computeChildPrefWidth(left), computeChildPrefWidth(right), computeChildPrefHeight(center)) +
                    computeChildPrefHeight(bottom) +
                    snappedBottomInset();
        }

        private double computeChildPrefWidth(Node child) {
            return child != null && child.isManaged() ? snapSize(child.prefWidth(-1)) : 0;
        }

        private double computeChildPrefHeight(Node child) {
            return child != null && child.isManaged() ? snapSize(child.prefHeight(-1)) : 0;
        }

        @Override
        protected void layoutChildren() {
            double topOffset = snappedTopInset();
            double leftOffset = snappedLeftInset();
            double bottomOffset = snappedBottomInset();
            double rightOffset = snappedRightInset();
            double width = getWidth();
            double height = getHeight();
            double contentWidth = width - leftOffset - rightOffset;
            double contentHeight = height - topOffset - bottomOffset;

            boolean hasTop = top != null && top.isManaged();
            boolean hasLeft = left != null && left.isManaged();
            boolean hasBottom = bottom != null && bottom.isManaged();
            boolean hasRight = right != null && right.isManaged();
            boolean hasCenter = center != null && center.isManaged();

            double leftWidth = hasLeft ? snapSize(left.prefHeight(-1)) : 0;
            double rightWidth = hasRight ? snapSize(right.prefHeight(-1)) : 0;
            double topHeight = hasTop ? snapSize(top.prefHeight(-1)) : 0;
            double bottomHeight = hasBottom ? snapSize(bottom.prefHeight(-1)) : 0;

            double centerX = leftOffset + leftWidth;
            double centerY = topOffset + topHeight;
            double centerWidth = contentWidth - leftWidth - rightWidth;
            double centerHeight = contentHeight - topHeight - bottomHeight;

            if (hasTop) {
                top.resize(centerWidth, topHeight);
                top.relocate(centerX, topOffset);
            }

            if (hasLeft) {
                // Swap width and height because the bar is rotated 90 degrees.
                left.resize(centerHeight, leftWidth);
                double rotateOffset = (centerHeight - leftWidth) / 2;
                left.relocate(leftOffset - rotateOffset, centerY + rotateOffset);
            }

            if (hasRight) {
                // Swap width and height because the bar is rotated 90 degrees.
                right.resize(centerHeight, rightWidth);
                double rotateOffset = (centerHeight - rightWidth) / 2;
                right.relocate(leftOffset + contentWidth - rightWidth - rotateOffset, centerY + rotateOffset);
            }

            if (hasBottom) {
                bottom.resize(centerWidth, bottomHeight);
                bottom.relocate(leftOffset + leftWidth, topOffset + contentHeight - bottomHeight);
            }

            if (hasCenter) {
                center.resize(centerWidth, centerHeight);
                center.relocate(centerX, centerY);
            }
        }
    }

    static class SideBar extends Region {
        private TabButtonBar topLeftBar;
        private TabButtonBar bottomRightBar;

        public TabButtonBar getTopLeftBar() {
            return topLeftBar;
        }

        public void setTopLeftBar(TabButtonBar topLeftBar) {
            if (this.topLeftBar != null) getChildren().remove(this.topLeftBar);
            this.topLeftBar = topLeftBar;
            if (topLeftBar != null) getChildren().add(topLeftBar);
        }

        public TabButtonBar getBottomRightBar() {
            return bottomRightBar;
        }

        public void setBottomRightBar(TabButtonBar bottomRightBar) {
            if (this.bottomRightBar != null) getChildren().remove(this.bottomRightBar);
            this.bottomRightBar = bottomRightBar;
            if (bottomRightBar != null) getChildren().add(bottomRightBar);
        }

        SideBar(Side side) {
            setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);

            getStyleClass().setAll("side-bar");
            pseudoClassStateChanged(StyleHelper.getPseudoClass(side), true);

            getChildren().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (getChildren().isEmpty()) {
                        setManaged(false);
                        setVisible(false);
                    }
                }
            });

            if (side.isVertical()) setRotate(90);
        }

        @Override
        protected double computePrefWidth(double height) {
            double width = 0;
            for (Node child : getChildren()) {
                if (child.isManaged()) {
                    width += snapSize(child.prefWidth(-1));
                }
            }
            return snappedLeftInset() + width + snappedRightInset();
        }

        @Override
        protected double computePrefHeight(double width) {
            double height = 0;
            for (Node child : getChildren()) {
                if (child.isManaged()) {
                    double childHeight = snapSize(child.prefHeight(-1));
                    if (childHeight > height) height = childHeight;
                }
            }
            return snappedTopInset() + height + snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double height = getHeight();
            double left = snappedLeftInset();
            double right = snappedRightInset();
            double top = snappedTopInset();
            double bottom = snappedBottomInset();
            double contentWidth = width - left - right;
            double contentHeight = height - top - bottom;

            if (topLeftBar != null && topLeftBar.isManaged()) {
                layoutInArea(topLeftBar, left, top, contentWidth, contentHeight, 0, HPos.LEFT, VPos.CENTER);
                double childWidth = snapSize(topLeftBar.prefWidth(-1));
                left += childWidth;
                contentWidth -= childWidth;
            }

            if (bottomRightBar != null && bottomRightBar.isManaged()) {
                layoutInArea(bottomRightBar, left, top, contentWidth, contentHeight, 0, HPos.RIGHT, VPos.CENTER);
            }
        }
    }

    static class TabButtonBar extends Region {
        private final ViewPaneSkin viewPaneSkin;
        private final ViewGroup viewGroup;

        private final ListChangeListener<ViewTab> tabChangeListener = new ListChangeListener<ViewTab>() {
            @Override
            public void onChanged(Change<? extends ViewTab> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        for (ViewTab viewTab : c.getRemoved()) {
                            removeViewTab(viewTab);
                        }
                    }
                    if (c.wasAdded()) {
                        for (ViewTab viewTab : c.getAddedSubList()) {
                            addViewTab(viewTab);
                        }
                    }
                }
            }
        };

        private final ChangeListener<ViewTab> selectedItemListener = new ChangeListener<ViewTab>() {
            @Override
            public void changed(ObservableValue<? extends ViewTab> observable, ViewTab oldValue, ViewTab newValue) {
                if (oldValue != null) {
                    setView(null);
                    oldValue.contentProperty().removeListener(tabContentListener);
                }
                if (newValue != null) {
                    setView(newValue.getContent());
                    newValue.contentProperty().addListener(tabContentListener);
                }
            }
        };

        private final ChangeListener<Node> tabContentListener = new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
                setView(newValue);
            }
        };

        public TabButtonBar(ViewPaneSkin viewPaneSkin, ViewGroup viewGroup) {
            this.viewPaneSkin = viewPaneSkin;
            this.viewGroup = viewGroup;

            getStyleClass().setAll("tab-button-bar");

            pseudoClassStateChanged(StyleHelper.getPseudoClass(viewGroup.getPos()), true);

            viewGroup.getTabs().forEach(this::addViewTab);
            viewGroup.getTabs().addListener(tabChangeListener);

            ViewTab selectedItem = viewGroup.getSelectionModel().getSelectedItem();
            if (selectedItem != null) setView(selectedItem.getContent());
            viewGroup.getSelectionModel().selectedItemProperty().addListener(selectedItemListener);
        }

        private void setView(Node content) {
            viewPaneSkin.divisionArea.setView(viewGroup.getPos(), content);
        }

        public ViewGroup getViewGroup() {
            return viewGroup;
        }

        public void addViewTab(ViewTab viewTab) {
            getChildren().add(new TabButton(viewTab));
        }

        public void removeViewTab(ViewTab viewTab) {
            getChildren().removeIf(node -> ((TabButton) node).getTab() == viewTab);
        }

        public void dispose() {
            setView(null);

            viewGroup.getTabs().removeListener(tabChangeListener);
            viewGroup.getSelectionModel().selectedItemProperty().removeListener(selectedItemListener);

            getChildren().clear();
        }

        @Override
        protected double computePrefWidth(double height) {
            double width = 0;
            for (Node child : getChildren()) {
                if (child.isManaged()) {
                    width += snapSize(child.prefWidth(-1));
                }
            }
            return snappedLeftInset() + width + snappedRightInset();
        }

        @Override
        protected double computePrefHeight(double width) {
            double height = 0;
            for (Node child : getChildren()) {
                if (child.isManaged()) {
                    double childHeight = snapSize(child.prefHeight(-1));
                    if (childHeight > height) height = childHeight;
                }
            }
            return snappedTopInset() + height + snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double height = getHeight();
            double left = snappedLeftInset();
            double right = snappedRightInset();
            double top = snappedTopInset();
            double bottom = snappedBottomInset();
            double contentWidth = width - left - right;
            double contentHeight = height - top - bottom;

            Side secondary = viewGroup.getPos().getSecondary();

            if (secondary == Side.TOP || secondary == Side.LEFT) {// 自左向右布局
                for (Node child : getChildren()) {
                    if (child.isManaged()) {
                        layoutInArea(child, left, top, contentWidth, contentHeight, 0, HPos.LEFT, VPos.CENTER);
                        double childWidth = snapSize(child.prefWidth(-1));
                        left += childWidth;
                        contentWidth -= childWidth;
                    }
                }
            } else {// 自右向左布局
                for (Node child : getChildren()) {
                    if (child.isManaged()) {
                        layoutInArea(child, left, top, contentWidth, contentHeight, 0, HPos.RIGHT, VPos.CENTER);
                        contentWidth -= snapSize(child.prefWidth(-1));
                    }
                }
            }
        }
    }

    static class DivisionArea extends Region {
        private final ViewPaneSkin viewPaneSkin;
        private final LayoutHelper layoutHelper;

        private final ContentArea[] views = new ContentArea[8];
        private ContentArea center;
        private final ContentDivider[] dividers = new ContentDivider[8];

        private boolean performingLayout = false;

        public DivisionArea(ViewPaneSkin viewPaneSkin) {
            this.viewPaneSkin = viewPaneSkin;
            this.layoutHelper = new LayoutHelper();

            getStyleClass().setAll("division-area");
        }

        public void setView(EightPos pos, Node content) {
            ContentArea view = views[pos.ordinal()];
            if (view == null) {
                view = new ContentArea(pos);
                getChildren().add(view);
                views[pos.ordinal()] = view;
            }
            view.setContent(content);
            layoutHelper.areas[pos.ordinal()].peer = view;
            updateDivider(pos);
        }

        public void setCenter(Node content) {
            if (center == null) {
                center = new ContentArea(null);
                getChildren().add(center);
            }
            center.setContent(content);
            layoutHelper.center.peer = center;
        }

        private void updateDivider(EightPos pos) {
            Side primary = pos.getPrimary();
            if (primary == Side.TOP) {
                int viewCount = getManagedViewCount(EightPos.TOP_LEFT, EightPos.TOP_RIGHT);
                setDivider(DividerType.TOP_PRIMARY, viewCount >= 1);
                setDivider(DividerType.TOP_SECONDARY, viewCount == 2);
            } else if (primary == Side.LEFT) {
                int viewCount = getManagedViewCount(EightPos.LEFT_TOP, EightPos.LEFT_BOTTOM);
                setDivider(DividerType.LEFT_PRIMARY, viewCount >= 1);
                setDivider(DividerType.LEFT_SECONDARY, viewCount == 2);
            } else if (primary == Side.BOTTOM) {
                int viewCount = getManagedViewCount(EightPos.BOTTOM_LEFT, EightPos.BOTTOM_RIGHT);
                setDivider(DividerType.BOTTOM_PRIMARY, viewCount >= 1);
                setDivider(DividerType.BOTTOM_SECONDARY, viewCount == 2);
            } else {
                int viewCount = getManagedViewCount(EightPos.RIGHT_TOP, EightPos.RIGHT_BOTTOM);
                setDivider(DividerType.RIGHT_PRIMARY, viewCount >= 1);
                setDivider(DividerType.RIGHT_SECONDARY, viewCount == 2);
            }
        }

        private void setDivider(DividerType type, boolean enable) {
            ContentDivider divider = dividers[type.ordinal()];
            if (divider == null) {
                divider = new ContentDivider(viewPaneSkin.getSkinnable().getDivider(type));
                dividers[type.ordinal()] = divider;
                layoutHelper.dividers[type.ordinal()].peer = divider;
                getChildren().add(divider);
            }
            divider.setManaged(enable);
            divider.setVisible(enable);
        }

        private int getManagedViewCount(EightPos a, EightPos b) {
            int result = 0;
            LayoutHelper.Area[] areas = layoutHelper.areas;
            if (areas[a.ordinal()].isManaged()) result++;
            if (areas[b.ordinal()].isManaged()) result++;
            return result;
        }

        @Override
        protected void layoutChildren() {
            if (performingLayout) return;
            performingLayout = true;

            double top = snappedTopInset();
            double left = snappedLeftInset();
            double bottom = snappedBottomInset();
            double right = snappedRightInset();
            double width = getWidth();
            double height = getHeight();

            layoutHelper.layout(left, top, width - left - right, height - top - bottom, isSnapToPixel());

            performingLayout = false;
        }
    }

    static class ContentDivider extends Region {
        private final ViewPane.Divider peer;

        private double position;

        private double size;
        private double min = 0;
        private double max = 1;

        private double initialPos;
        private double mousePos;

        public ContentDivider(ViewPane.Divider peer) {
            this.peer = peer;
            this.position = peer.getPosition();

            getStyleClass().setAll("divider");

            setCursor(isVertical() ? Cursor.H_RESIZE : Cursor.V_RESIZE);

            DividerType type = peer.getType();
            pseudoClassStateChanged(StyleHelper.getPseudoClass(type), true);
            pseudoClassStateChanged(StyleHelper.getPseudoClass(type.getSide()), true);
            pseudoClassStateChanged(StyleHelper.getPseudoClass(type.getOrientation()), true);

            addEventHandler(MouseEvent.ANY, Event::consume);
            setOnMousePressed(event -> {
                initialPos = getPosition();
                mousePos = isVertical() ? event.getSceneX() : event.getSceneY();
            });
            setOnMouseDragged(event -> {
                double nowMousePos = isVertical() ? event.getSceneX() : event.getSceneY();
                double delta = nowMousePos - mousePos;
                setPosition(initialPos + delta / size);
            });

            peer.positionProperty().addListener(observable -> {
                setPosition(peer.getPosition());
                requestParentLayout();
            });
        }

        public boolean isVertical() {
            return peer.getType().isVertical();
        }

        public double getPosition() {
            return position;
        }

        private void setPosition(double position) {
            this.position = clamp(position, min, max);
            this.peer.setPosition(position);
        }

        public double getDividerWidth() {
            return isVertical() ? prefWidth(-1) : prefHeight(-1);
        }

        public void validDividerPosition(double size, double min, double max) {
            this.size = size;
            this.min = min;
            this.max = max;
            setPosition(getPosition());
        }

        @Override
        protected double computePrefWidth(double height) {
            return snappedLeftInset() + snappedRightInset();
        }

        @Override
        protected double computePrefHeight(double width) {
            return snappedTopInset() + snappedBottomInset();
        }
    }

    static class ContentArea extends Region {
        public static final PseudoClass CENTER = PseudoClass.getPseudoClass("center");

        private Node content;

        public ContentArea(EightPos pos) {
            if (pos != null) {
                pseudoClassStateChanged(StyleHelper.getPseudoClass(pos), true);
            } else {
                pseudoClassStateChanged(CENTER, true);
            }

            getStyleClass().setAll("content-area");
        }

        public void setContent(Node content) {
            if (this.content != null) {
                getChildren().remove(this.content);
            }
            this.content = content;

            boolean flag = content != null;
            if (flag) {
                getChildren().add(content);
            }
            setManaged(flag);
            setVisible(flag);
        }

        @Override
        protected double computeMinWidth(double height) {
            double contentWidth = content != null && content.isManaged() ? snapSize(content.minWidth(-1)) : 0;
            return snappedLeftInset() + contentWidth + snappedRightInset();
        }

        @Override
        protected double computeMinHeight(double width) {
            double contentHeight = content != null && content.isManaged() ? snapSize(content.minHeight(-1)) : 0;
            return snappedTopInset() + contentHeight + snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            if (content != null && content.isManaged()) {
                double top = snappedTopInset();
                double left = snappedLeftInset();
                double bottom = snappedBottomInset();
                double right = snappedRightInset();
                content.resize(getWidth() - left - right, getHeight() - top - bottom);
                content.relocate(left, top);
            }
        }
    }

    static class TabButton extends ButtonBase {
        private static final PseudoClass SELECTED_PSEUDO_CLASS =
                PseudoClass.getPseudoClass("selected");

        private final ViewTab tab;

        private final InvalidationListener graphicsInvalidationListener = observable -> updateGraphic();
        private final InvalidationListener viewGroupInvalidationListener = observable -> updatePos();
        private final InvalidationListener selectedInvalidationListener = observable -> updateSelected();

        public TabButton(ViewTab tab) {
            this.tab = tab;

            getStyleClass().setAll("view-tab-button");

            textProperty().bind(tab.textProperty());

            tab.graphicProperty().addListener(new WeakInvalidationListener(graphicsInvalidationListener));
            tab.viewGroupProperty().addListener(new WeakInvalidationListener(viewGroupInvalidationListener));
            tab.selectedProperty().addListener(new WeakInvalidationListener(selectedInvalidationListener));

            updatePos();
            updateGraphic();
            updateSelected();
        }

        public ViewTab getTab() {
            return tab;
        }

        public EightPos getPos() {
            ViewGroup viewGroup = tab.getViewGroup();
            assert viewGroup != null;
            return viewGroup.getPos();
        }

        private void updatePos() {
            if (getPos().getPrimary() == Side.LEFT) {
                setRotate(180);
            } else {
                setRotate(0);
            }
            updateGraphic();
        }

        private void updateGraphic() {
            Node graphic = tab.getGraphic();
            setGraphic(graphic);

            if (graphic == null) return;
            if (getPos().getPrimary() == Side.LEFT) {
                graphic.setRotate(90);
                setTextAlignment(TextAlignment.RIGHT);
            } else {
                graphic.setRotate(0);
                setTextAlignment(TextAlignment.LEFT);
            }
        }

        private void updateSelected() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, tab.isSelected());
        }

        @Override
        public void fire() {
            if (!isDisabled()) {
                tab.setSelected(!tab.isSelected());
                fireEvent(new ActionEvent());
            }
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new TabButtonSkin(this);
        }
    }

    static class TabButtonSkin extends LabeledSkinBase<TabButton, ButtonBehavior<TabButton>> {

        public TabButtonSkin(TabButton labeled) {
            super(labeled, new ButtonBehavior<>(labeled));
        }
    }

    private static double max(double a, double b, double c) {
        return Math.max(Math.max(a, b), c);
    }

    private static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
}
