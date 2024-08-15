package lyricsystem;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.PathElement;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;


// Reference: https://stackoverflow.com/questions/77124891/how-to-search-and-format-text-block-in-text-area-with-javafx
class SearchTextArea {
    public TextArea textArea;
    /**
     * Start index of the highlight.
     */
    private int highlightStartIndex;

    /**
     * End index of the highlight.
     */
    private int highlightEndIndex;

    /**
     * Rectangle node to act as highlight.
     */
    private final Path highlightPath;

    /**
     * Node to keep reference of the all contents of the TextArea.
     */
    private StackPane contentPane;

    boolean highlightRequired = false;

    public SearchTextArea(TextArea textArea) {
        this.textArea = textArea;
        /* Setting default values */
        highlightStartIndex = -1;
        highlightEndIndex = -1;

        /* Settings for text highlighting */
        highlightPath = new Path();
        highlightPath.getStyleClass().add("textarea-highlight");
        highlightPath.setMouseTransparent(true);
        highlightPath.setManaged(false);
        highlightPath.setStroke(null);
        highlightPath.setStyle("-fx-fill:red;-fx-stroke-width:0px;");
        highlightPath.setBlendMode(BlendMode.ADD);

        textArea.textProperty().addListener((obs, oldVal, newVal) -> removeHighlight());
        /*
         * When the width of the TextArea changes, we need to update the selection path bounds.
         */
        textArea.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (highlightStartIndex > -1) {
                doHighlightIndex();
            }
        });

        textArea.needsLayoutProperty().addListener((obs, oldval, needsLayout) -> {
            if (!needsLayout && highlightRequired) {
                doHighlightIndex();
                highlightRequired = false;
            }
        });
    }

    /**
     * Removes the highlight in the text area.
     */
    public final void removeHighlight() {
        if (contentPane != null) {
            contentPane.getChildren().remove(highlightPath);
        }
        highlightStartIndex = -1;
        highlightEndIndex = -1;
    }

    /**
     * Highlights the character at the provided index in the text area.
     *
     * @param highlightPos Position of the character in the text
     */
    public final void highlight(final int highlightPos) {
        highlightRange(highlightPos, highlightPos);
    }

    /**
     * Highlights the characters for the provided index range in the text area.
     *
     * @param start Start character index for highlighting
     * @param end   End character index for highlighting
     */
    public final void highlightRange(final int start, final int end) {
        if (end < start) {
            throw new IllegalArgumentException("Caret cannot be less than the anchor index");
        }
        if (textArea.getLength() > 0) {
            highlightStartIndex = start;
            highlightEndIndex = end;

            if (highlightStartIndex >= textArea.getLength()) {
                highlightStartIndex = textArea.getLength() - 1;
            } else if (highlightStartIndex < 0) {
                highlightStartIndex = 0;
            }

            if (highlightEndIndex >= textArea.getLength()) {
                highlightEndIndex = textArea.getLength() - 1;
            } else if (highlightEndIndex < 0) {
                highlightEndIndex = 0;
            }

            if (textArea.getSkin() != null) {
                doHighlightIndex();
            } else {
                highlightRequired = true;
            }
        }

    }

    /**
     * Highlights the character at the specified index.
     */
    private void doHighlightIndex() {
        if (highlightStartIndex > -1) {
            /* Compute the highlight bounds based on the index range. Handles multi line highlighting as well. */
            List<HighlightBound> highlightBounds = computeHighlightBounds();

            /* Building the selection path based on the character bounds */
            final List<PathElement> elements = new ArrayList<>();
            highlightBounds.forEach(bound -> {
                elements.add(new MoveTo(bound.point1.getX(), bound.point1.getY()));
                elements.add(new LineTo(bound.point2.getX(), bound.point2.getY()));
                elements.add(new LineTo(bound.point3.getX(), bound.point3.getY()));
                elements.add(new LineTo(bound.point4.getX(), bound.point4.getY()));
                elements.add(new LineTo(bound.point1.getX(), bound.point1.getY()));
            });
            highlightPath.getElements().clear();
            highlightPath.getElements().addAll(elements);

            /* Ensuring to lookup contentPane if it not yet loaded */
            if (contentPane == null) {
                lookupContentPane();
            }

            /* If the highlightPath is not yet added in the pane then adding in the contentPane */
            if (contentPane != null && !contentPane.getChildren().contains(highlightPath)) {
                contentPane.getChildren().add(highlightPath);
            }
        }
    }

    /**
     * Lookup for the content pane in which all nodes are rendered.
     */
    private void lookupContentPane() {
        final Region content = (Region) textArea.lookup(".content");
        if (content != null) {
            contentPane = (StackPane) content.getParent();
        }
    }


    protected final void layoutChildren() {
        textArea.requestLayout();
        /*
         * Looking for appropriate nodes that are required to determine text selection. Using these nodes, we build
         * an extra node(Path) inside the TextArea to show the custom selection.
         */
        if (contentPane == null) {
            lookupContentPane();
        }
    }

    public Path getHighlightPath() {
        return highlightPath;
    }

    /**
     * Computes the bounds for each highlight line based on the provided highlight indexes.
     *
     * @return List of HighLightBounds for multiple lines
     */
    private List<HighlightBound> computeHighlightBounds() {
        final List<HighlightBound> list = new ArrayList<>();
        /* If it is single character highlighting, including only one character bounds. */
        if (highlightEndIndex <= highlightStartIndex) {
            /* If it is single character highlighting, including only one character bounds. */
            final Rectangle2D bounds = ((TextAreaSkin) textArea.getSkin()).getCharacterBounds(highlightStartIndex);
            list.add(new HighlightBound(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()));
            return list;
        }

        /* If it is a range highlighting... */
        double minX = -1;
        double minY = -1;
        double maxX = -1;
        double maxY = -1;
        /*
         * Looping through each character in the range and taking its bound to compute each line highlight bound
         */
        for (int index = highlightStartIndex; index <= highlightEndIndex; index++) {
            final Rectangle2D bounds = ((TextAreaSkin) textArea.getSkin()).getCharacterBounds(index);
            if (index == highlightStartIndex) {
                minX = bounds.getMinX();
                minY = bounds.getMinY();
                maxX = bounds.getMaxX();
                maxY = bounds.getMaxY();
            } else {
                /* If the new character minX is less than previous minX, then it is a new line */
                if (bounds.getMinX() <= minX) {
                    /* Registering the previous bounds for the line */
                    list.add(new HighlightBound(minX, minY, maxX, maxY));

                    /* ... and starting a new line bounds */
                    minX = bounds.getMinX();
                    minY = bounds.getMinY();
                    maxX = bounds.getMaxX();
                    maxY = bounds.getMaxY();
                } else {
                    /*
                     * If the character falls next to the previous character, then updating the highlight end
                     * bounds
                     */
                    maxX = bounds.getMaxX();
                    maxY = bounds.getMaxY();
                }
            }
        }

        /* Registering the last highlight bound */
        if (minX > -1) {
            list.add(new HighlightBound(minX, minY, maxX, maxY));
        }
        return list;
    }

    /**
     * Class to hold the bounds of the highlight for each line. This class provided the four corners of the bounds.
     */
    final class HighlightBound {
        /* Top left point of the bound */
        private final Point2D point1;
        /* Top right point of the bound */
        private final Point2D point2;
        /* Bottom right point of the bound */
        private final Point2D point3;
        /* Bottom left point of the bound */
        private final Point2D point4;

        /**
         * Constructor
         *
         * @param minX Minimun X value of the bound
         * @param minY Minimum Y value of the bound
         * @param maxX Maximum X value of the bound
         * @param maxY Maximum Y value of the bound
         */
        public HighlightBound(double minX, double minY, double maxX, double maxY) {
            point1 = new Point2D(minX, minY);
            point2 = new Point2D(maxX, minY);
            point3 = new Point2D(maxX, maxY);
            point4 = new Point2D(minX, maxY);
        }
    }
}
