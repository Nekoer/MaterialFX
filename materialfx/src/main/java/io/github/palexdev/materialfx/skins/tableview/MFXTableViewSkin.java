package io.github.palexdev.materialfx.skins.tableview;

import io.github.palexdev.materialfx.controls.factories.MFXAnimationFactory;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.Set;

public class MFXTableViewSkin<T> extends TableViewSkin<T> {
    //================================================================================
    // Properties
    //================================================================================
    private final VirtualFlow<?> virtualFlow;
    private final Pane header;

    private final ScrollBar vBar;
    private final ScrollBar hBar;

    private final Timeline hideBars;
    private final Timeline showBars;

    public MFXTableViewSkin(TableView<T> tableView) {
        super(tableView);

        virtualFlow = (VirtualFlow<?>) tableView.lookup(".virtual-flow");
        header = (Pane) tableView.lookup("TableHeaderRow");

        this.vBar = new ScrollBar();
        this.hBar = new ScrollBar();
        bindScrollBars(tableView);
        getChildren().addAll(vBar, hBar);

        vBar.setManaged(false);
        vBar.setOrientation(Orientation.VERTICAL);
        vBar.getStyleClass().add("mfx-scroll-bar");
        vBar.visibleProperty().bind(vBar.visibleAmountProperty().isNotEqualTo(0));

        hBar.setManaged(false);
        hBar.setOrientation(Orientation.HORIZONTAL);
        hBar.getStyleClass().add("mfx-scroll-bar");
        hBar.visibleProperty().bind(hBar.visibleAmountProperty().isNotEqualTo(0));

        hideBars = new Timeline(
                new KeyFrame(Duration.millis(400),
                        new KeyValue(vBar.opacityProperty(), 0.0, MFXAnimationFactory.getInterpolator()),
                        new KeyValue(hBar.opacityProperty(), 0.0, MFXAnimationFactory.getInterpolator()))
        );
        showBars = new Timeline(
                new KeyFrame(Duration.millis(400),
                        new KeyValue(vBar.opacityProperty(), 1.0, MFXAnimationFactory.getInterpolator()),
                        new KeyValue(hBar.opacityProperty(), 1.0, MFXAnimationFactory.getInterpolator()))
        );
    }

    private void bindScrollBars(TableView<?> tableView) {
        final Set<Node> nodes = tableView.lookupAll("VirtualScrollBar");
        for (Node node : nodes) {
            if (node instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) node;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    bindScrollBars(vBar, bar);
                } else if (bar.getOrientation().equals(Orientation.HORIZONTAL)) {
                    bindScrollBars(hBar, bar);
                }
            }
        }
    }

    private void bindScrollBars(ScrollBar scrollBarA, ScrollBar scrollBarB) {
        scrollBarA.valueProperty().bindBidirectional(scrollBarB.valueProperty());
        scrollBarA.minProperty().bindBidirectional(scrollBarB.minProperty());
        scrollBarA.maxProperty().bindBidirectional(scrollBarB.maxProperty());
        scrollBarA.visibleAmountProperty().bindBidirectional(scrollBarB.visibleAmountProperty());
        scrollBarA.unitIncrementProperty().bindBidirectional(scrollBarB.unitIncrementProperty());
        scrollBarA.blockIncrementProperty().bindBidirectional(scrollBarB.blockIncrementProperty());
    }

    private double estimateHeight() {
        double borderWidth = snapVerticalInsets();

        double cellsHeight = 0;
        for (int i = 0; i < virtualFlow.getCellCount(); i++) {
            TableRow<?> cell = (TableRow<?>) virtualFlow.getCell(i);

            cellsHeight += cell.getHeight();
        }

        return cellsHeight + borderWidth;
    }

    private double snapVerticalInsets() {
        return getSkinnable().snappedBottomInset() + getSkinnable().snappedTopInset();
    }

    //================================================================================
    // Override Methods
    //================================================================================
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final int itemsCount = getSkinnable().getItems().size();
        if (getSkinnable().maxHeightProperty().isBound() || itemsCount <= 0) {
            return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        }

        final double fixedCellSize = getSkinnable().getFixedCellSize();
        double computedHeight = fixedCellSize != Region.USE_COMPUTED_SIZE ?
                fixedCellSize * itemsCount + snapVerticalInsets() : estimateHeight();
        double height = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        if (height > computedHeight) {
            height = computedHeight;
        }

        if (getSkinnable().getMaxHeight() > 0 && computedHeight > getSkinnable().getMaxHeight()) {
            return getSkinnable().getMaxHeight();
        }

        return height;
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);

        final double headerH = header.getHeight();

        Insets insets = getSkinnable().getInsets();
        final double prefWidth = vBar.prefWidth(-1);
        vBar.resizeRelocate(w - prefWidth - insets.getRight(), insets.getTop() + headerH, prefWidth, h - insets.getTop() - insets.getBottom() - headerH);

        final double prefHeight = hBar.prefHeight(-1);
        hBar.resizeRelocate(insets.getLeft() + 5, h - prefHeight - insets.getBottom() + 20, w - insets.getLeft() - insets.getRight() - 20, prefHeight);
    }
}