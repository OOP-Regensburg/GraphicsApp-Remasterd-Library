package de.ur.mi.oop.app;

import de.ur.mi.oop.graphics.*;
import de.ur.mi.oop.graphics.Image;
import de.ur.mi.oop.graphics.Label;

import javax.swing.*;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Zeichenfläche zur Darstellung der einzelnen GraphicsObjects
 */
public class Canvas extends JPanel {

    private GraphicsObject[] components;
    private RenderingHints renderingHints;

    public Canvas() {
        this.setOpaque(false);
        createRenderingHints();
    }

    private void createRenderingHints() {
        renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

    }

    public void setComponents(GraphicsObject[] components) {
        this.components = components;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHints(renderingHints);
        draw(g2d);
    }

    private void draw(Graphics2D g2d) {
        if (components != null) {
            for (GraphicsObject component : components) {
                drawComponent(g2d, component);
            }
        }
    }

    private void drawComponent(Graphics2D g2d, GraphicsObject object) {
        switch (object.getType()) {
            case BACKGROUND:
            case RECTANGLE:
                drawRectangle(g2d, (de.ur.mi.oop.graphics.Rectangle) object);
                break;
            case LINE:
                drawLine(g2d, (Line) object);
                break;
            case CIRCLE:
                drawCircle(g2d, (Circle) object);
                break;
            case ARC:
                drawArc(g2d, (Arc) object);
                break;
            case PIE_ARC:
                drawPieArc(g2d, (Arc) object);
                break;
            case ELLIPSE:
                drawEllipse(g2d, (Ellipse) object);
                break;
            case LABEL:
                drawLabel(g2d, (de.ur.mi.oop.graphics.Label) object);
                break;
            case IMAGE:
                drawImage(g2d, (de.ur.mi.oop.graphics.Image) object);
                break;
            default:
                break;
        }
    }

    private void drawLine(Graphics2D g2d, Line line) {
        AffineTransform originalTransformation = g2d.getTransform();
        g2d.setTransform(getRotationTransformationForObject(line));
        g2d.setColor(line.getColor().asAWTColor());
        Stroke stroke = new BasicStroke(line.getLineWidth());
        g2d.setStroke(stroke);
        g2d.drawLine(
                (int) line.getXPos(),
                (int) line.getYPos(),
                (int) line.getEndpointX(),
                (int) line.getEndpointY());
        g2d.setTransform(originalTransformation);
    }

    private void drawEllipse(Graphics2D g2d, Ellipse ellipse) {
        Ellipse2D ellipseShape = new Ellipse2D.Float(
                ellipse.getXPos() - ellipse.getRadiusX(),
                ellipse.getYPos() - ellipse.getRadiusY(),
                ellipse.getRadiusX() * 2,
                ellipse.getRadiusY() * 2);

        drawAndStrokeShape(g2d, ellipse, ellipseShape);
    }

    private void drawRectangle(Graphics2D g2d, de.ur.mi.oop.graphics.Rectangle rect) {
        Rectangle2D rectShape = new Rectangle.Float(
                rect.getXPos(),
                rect.getYPos(),
                rect.getWidth(),
                rect.getHeight());

        drawAndStrokeShape(g2d, rect, rectShape);
    }

    private void drawCircle(Graphics2D g2d, Circle circle) {
        Ellipse2D circleShape = new Ellipse2D.Float(
                circle.getXPos() - circle.getRadius(),
                circle.getYPos() - circle.getRadius(),
                circle.getRadius() * 2,
                circle.getRadius() * 2);

        drawAndStrokeShape(g2d, circle, circleShape);
    }

    private void drawArc(Graphics2D g2d, Arc arc) {
        Arc2D arcShape = new Arc2D.Float(
                arc.getXPos() - arc.getRadius(),
                arc.getYPos() - arc.getRadius(),
                arc.getRadius() * 2, arc.getRadius() * 2,
                arc.getStart(),
                arc.getEnd(),
                Arc2D.OPEN);

        drawAndStrokeShape(g2d, arc, arcShape);
    }

    private void drawPieArc(Graphics2D g2d, Arc arc) {
        Arc2D arcShape = new Arc2D.Float(
                arc.getXPos() - arc.getRadius(),
                arc.getYPos() - arc.getRadius(),
                arc.getRadius() * 2, arc.getRadius() * 2,
                arc.getStart(),
                arc.getEnd(),
                Arc2D.PIE);
        drawAndStrokeShape(g2d, arc, arcShape);
    }

    private void drawImage(Graphics2D g2d, de.ur.mi.oop.graphics.Image image) {
        AffineTransform transformation = getRotationTransformationForObject(image);
        transformation.translate(image.getXPos(), image.getYPos());
        transformation.scale(1, 1);
        g2d.drawImage(image.getImage(), transformation, null);
    }

    private void drawLabel(Graphics2D g2d, Label label) {
        AffineTransform originalTransformation = g2d.getTransform();
        g2d.setTransform(getRotationTransformationForObject(label));
        g2d.setColor(label.getColor().asAWTColor());
        g2d.setFont(new Font(label.getFont(), Font.PLAIN, label.getFontSize()));
        /**
         * Keep in mind: drawString's y parameter defines the text's baseline position not the label's upper left corner.
         * To keep this call consistent with similar drawing operations (see drawRectangle or drawImage) we are
         * translating the label's origin by using the current font size as an offset for the y position. This is only
         * an estimate, since font specific measures (top, ascent, descent or bottom) are not considered.
         */
        g2d.drawString(label.getText(), label.getXPos(), label.getYPos() + label.getFontSize());
        g2d.setTransform(originalTransformation);
    }

    private void drawAndStrokeShape(Graphics2D g2d, GraphicsObject graphicsObject, Shape shape) {
        AffineTransform originalTransformation = g2d.getTransform();
        g2d.setTransform(getRotationTransformationForObject(graphicsObject));
        g2d.setPaint(graphicsObject.getColor().asAWTColor());
        g2d.fill(shape);
        if (graphicsObject.getBorderWeight() != 0.f) {
            Stroke stroke = new BasicStroke(graphicsObject.getBorderWeight());
            Color strokeColor = graphicsObject.getBorderColor().asAWTColor();
            g2d.setStroke(stroke);
            g2d.setPaint(strokeColor);
            g2d.draw(shape);
        }
        g2d.setTransform(originalTransformation);
    }

    /**
     * Return an AffineTransform instance to draw a rotated version of an given GraphicsObject. Objects are rotated around their
     * center point.
     *
     * @param object The object to be rotated
     * @return The AffineTransform to be applied before or during drawing
     */
    private AffineTransform getRotationTransformationForObject(GraphicsObject object) {
        // Since Lines do not return proper values for width and height, we create a bounding box to translate the rotation origin
        if (object instanceof Line) {
            Line line = (Line) object;
            object = new de.ur.mi.oop.graphics.Rectangle(line.getStartpointX(), line.getStartpointY(), line.getLength(), line.getLineWidth());
            object.setRotationAngle(line.getRotationAngle());
        }
        // Since Label does use different Getters to return (estimated) dimensions, we create a bounding box to translate the rotation origin
        if (object instanceof Label) {
            Label label = (Label) object;
            object = new de.ur.mi.oop.graphics.Rectangle(label.getXPos(), label.getYPos(), label.getWidthEstimate(), label.getHeightEstimate());
            object.setRotationAngle(label.getRotationAngle());
        }
        double rotationAngleInRadians = Math.toRadians(object.getRotationAngle());
        // Some GraphicsObjects do not use center point as origin and must be translated accordingly
        boolean translateOrigin = object instanceof Image || object instanceof de.ur.mi.oop.graphics.Rectangle;
        float rotationOffsetX = translateOrigin ? object.getWidth() / 2 : 0;
        float rotationOffsetY = translateOrigin ? object.getHeight() / 2 : 0;
        float rotationOriginX = object.getXPos() + rotationOffsetX;
        float rotationOriginY = object.getYPos() + rotationOffsetY;
        return AffineTransform.getRotateInstance(rotationAngleInRadians, rotationOriginX, rotationOriginY);
    }

}
