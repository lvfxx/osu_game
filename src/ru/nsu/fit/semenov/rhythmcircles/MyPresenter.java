package ru.nsu.fit.semenov.rhythmcircles;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;
import ru.nsu.fit.semenov.rhythmcircles.events.SlideEvent;
import ru.nsu.fit.semenov.rhythmcircles.events.TapEvent;
import ru.nsu.fit.semenov.rhythmcircles.views.TapView;

import java.util.HashMap;

import static ru.nsu.fit.semenov.rhythmcircles.views.TapView.RADIUS;

public class MyPresenter implements GamePresenter {

    public MyPresenter(Group root, Group cg) {
        circlesGroup = cg;
        rootGroup = root;
    }


    @Override
    public void addTapEventView(TapEvent tapEvent) {
        TapView circle = new TapView();
        circle.setCenterX(tapEvent.getX());
        circle.setCenterY(tapEvent.getY());
        circle.setOpacity(0);

        tapEvntToView.put(tapEvent, circle);
        viewToTapEvnt.put(circle, tapEvent);

        circlesGroup.getChildren().add(circle);

        // event handlers
        circle.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> tapEvent.tap());

        // chowing animation
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(circle.opacityProperty(), 1)));
        timeline.play();

        // add ring animation
        Circle ring = new Circle(150, Color.web("white", 0));
        ring.setCenterX(((TapEvent) tapEvent).getX());
        ring.setCenterY(((TapEvent) tapEvent).getY());
        ring.setStrokeType(StrokeType.OUTSIDE);
        ring.setStroke(Color.web("white", 0.7));
        ring.setStrokeWidth(2);
        ring.setEffect(new BoxBlur(10, 10, 3));
        ring.setMouseTransparent(true);

        rootGroup.getChildren().add(ring);
        Timeline ringTimeline = new Timeline();
        ringTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(ring.radiusProperty(), 150)),
                // convert java.time.Duration to javafx.util.Duration
                new KeyFrame(Duration.millis(TapEvent.TOO_EARLY.plus(TapEvent.REGULAR).
                        plus(TapEvent.PERFECT).toMillis()),
                        new KeyValue(ring.radiusProperty(), RADIUS)));

        ringTimeline.setOnFinished(event -> rootGroup.getChildren().remove(ring));
        ringTimeline.play();
    }


    @Override
    public void removeTapEventView(TapEvent tapEvent) {
        TapView circle = tapEvntToView.get(tapEvent);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(circle.opacityProperty(), 0)));
        timeline.setOnFinished(event -> {
            rootGroup.getChildren().remove(circle);
            tapEvntToView.remove(tapEvent);
            viewToTapEvnt.remove(circle);
        });
        timeline.play();

        // show scores
        double x = circle.getBoundsInParent().getMaxX() - circle.getRadius() * 0.5;
//        x = circle.getCenterX();
        double y = circle.getBoundsInParent().getMaxY() - circle.getRadius() * 0.5;
        showScores(x, y, tapEvent.getScores());
    }


    @Override
    public void addSlideEventView(SlideEvent slideEvent) {
        TapView startCircle = new TapView();
        TapView finishCircle = new TapView();

        startCircle.setCenterX(slideEvent.getStartX());
        startCircle.setCenterY(slideEvent.getStartY());
        finishCircle.setCenterX(slideEvent.getFinishX());
        finishCircle.setCenterY(slideEvent.getFinishY());

        double rectWidth = Math.sqrt(
                        Math.pow(slideEvent.getFinishX() - slideEvent.getStartX(), 2.0) +
                        Math.pow(slideEvent.getFinishY() - slideEvent.getStartY(), 2.0));
        double rectHeight = 2 * TapView.RADIUS;

        Rectangle pathRectangle = new Rectangle(rectWidth, rectHeight, Color.web("white", 0.05));
        pathRectangle.setStrokeType(StrokeType.OUTSIDE);
        pathRectangle.setStroke(Color.web("white", 0.6));
        pathRectangle.setStrokeWidth(4);

        // set center of rectangle to path center
        double rectCenterX = (slideEvent.getStartX() + slideEvent.getFinishX()) / 2;
        double rectCenterY = (slideEvent.getStartY() + slideEvent.getFinishY()) / 2;
        pathRectangle.setX(rectCenterX - rectWidth / 2);
        pathRectangle.setY(rectCenterY - rectHeight / 2);

        // rotate rectangle
        double rotationDegree = Math.toDegrees(Math.atan( (slideEvent.getFinishY() - slideEvent.getStartY()) /
                (slideEvent.getFinishX() - slideEvent.getStartX())));
        pathRectangle.setRotate(rotationDegree);

        Group slideGroup = new Group(pathRectangle, startCircle, finishCircle);

        slideGroup.setOpacity(0);

        slideEvntToView.put(slideEvent, slideGroup);
        viewToSlideEvnt.put(slideGroup, slideEvent);

        circlesGroup.getChildren().add(slideGroup);

        // event handlers
        startCircle.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> slideEvent.tap());

        // showing animation
        Timeline showingTimeline = new Timeline();
        showingTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(slideGroup.opacityProperty(), 1)));
        showingTimeline.play();


        // add ring animation
        Circle ring = new Circle(150, Color.web("white", 0));
        ring.setCenterX(slideEvent.getStartX());
        ring.setCenterY(slideEvent.getStartY());
        ring.setStrokeType(StrokeType.OUTSIDE);
        ring.setStroke(Color.web("white", 0.7));
        ring.setStrokeWidth(2);
        ring.setEffect(new BoxBlur(10, 10, 3));
        ring.setMouseTransparent(true);

        rootGroup.getChildren().add(ring);
        Timeline ringTimeline = new Timeline();
        ringTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(ring.radiusProperty(), 150)),
                // convert java.time.Duration to javafx.util.Duration
                new KeyFrame(Duration.millis(TapEvent.TOO_EARLY.plus(TapEvent.REGULAR).
                        plus(TapEvent.PERFECT).toMillis()),
                        new KeyValue(ring.radiusProperty(), RADIUS)));

        ringTimeline.setOnFinished(event -> rootGroup.getChildren().remove(ring));
        ringTimeline.play();

    }

    @Override
    public void removeSlideEventView(SlideEvent slideEvent) {

    }


    private void showScores(double x, double y, int scores) {
        Text scoreText = new Text();
        rootGroup.getChildren().add(scoreText);
        scoreText.setId("scoretext");
        scoreText.setText(String.valueOf(scores));

        Timeline timeline = new Timeline();

        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scoreText.opacityProperty(), 1),
                        new KeyValue(scoreText.xProperty(), x),
                        new KeyValue(scoreText.yProperty(), y)),
                new KeyFrame(new Duration(1000),
                        new KeyValue(scoreText.opacityProperty(), 0),
                        new KeyValue(scoreText.xProperty(), x + 15),
                        new KeyValue(scoreText.yProperty(), y + 30)));
        timeline.setOnFinished(event -> rootGroup.getChildren().remove(scoreText));
        timeline.play();
    }

    private final Group rootGroup;
    private final Group circlesGroup;

    private HashMap<TapEvent, TapView> tapEvntToView = new HashMap<>();
    private HashMap<TapView, TapEvent> viewToTapEvnt = new HashMap<>();

    private HashMap<SlideEvent, Group> slideEvntToView = new HashMap<>();
    private HashMap<Group, SlideEvent> viewToSlideEvnt = new HashMap<>();


}
