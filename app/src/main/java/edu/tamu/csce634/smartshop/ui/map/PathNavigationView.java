package edu.tamu.csce634.smartshop.ui.map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import edu.tamu.csce634.smartshop.models.world.Aisle;
import edu.tamu.csce634.smartshop.models.world.SupermarketLayout;

public class PathNavigationView extends View {

    // --- Paint objects ---
    private final Paint pathPaint;
    private final Paint markerPaint;
    private final Paint aislePaint;
    private final Paint textPaint;

    // --- Path objects ---
    private final Path fullPath = new Path();
    private final Path animatedPath = new Path();
    private final PathMeasure pathMeasure = new PathMeasure();
    private PointF currentMarkerPosition;
    private float markerRadius = 24f;
    private ValueAnimator pathAnimator;

    // --- Data ---
    private SupermarketLayout supermarketLayout;

    // --- Destination Image ---
    private Bitmap destinationBitmap;
    private PointF destinationPoint;

    // --- A* Fields ---
    private static final int GRID_WIDTH = 40;
    private static final int GRID_HEIGHT = 60;
    private Node[][] grid;
    private boolean aStarGridInitialized = false;

    // --- A* Node Class ---
    static class Node {
        int x, y;
        int g, h;
        Node parent;
        boolean isObstacle;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void reset() {
            g = 0;
            h = 0;
            parent = null;
        }

        int getF() {
            return g + h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }
    }

    public PathNavigationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(Color.parseColor("#4CAF50"));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(18f);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setPathEffect(new DashPathEffect(new float[]{40, 20}, 0));

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(Color.parseColor("#E91E63"));
        markerPaint.setStyle(Paint.Style.FILL);

        aislePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        aislePaint.setColor(Color.parseColor("#E0E0E0"));
        aislePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        setupAnimator();
    }

    private void setupAnimator() {
        pathAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        pathAnimator.setDuration(1200);
        pathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pathAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            float pathLength = pathMeasure.getLength();
            animatedPath.reset();
            pathMeasure.getSegment(0, pathLength * fraction, animatedPath, true);

            float[] pos = new float[2];
            pathMeasure.getPosTan(pathLength * fraction, pos, null);
            currentMarkerPosition = new PointF(pos[0], pos[1]);

            invalidate();
        });

        pathAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // Snap the marker to the final destination when the animation ends
                if (destinationPoint != null) {
                    currentMarkerPosition = new PointF(destinationPoint.x, destinationPoint.y);
                    invalidate();
                }
            }
        });
    }

    public void setSupermarketLayout(SupermarketLayout layout) {
        this.supermarketLayout = layout;
        aStarGridInitialized = false;
        invalidate();
    }

    public void setDestinationImage(Bitmap bitmap) {
        if (this.destinationBitmap != null) {
            this.destinationBitmap.recycle();
            this.destinationBitmap = null;
        }
        if (bitmap != null) {
            this.destinationBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false);
        } else {
            this.destinationBitmap = null;
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            initializeAStarGrid(w, h);
        }
    }

    private void initializeAStarGrid(int viewWidth, int viewHeight) {
        grid = new Node[GRID_WIDTH][GRID_HEIGHT];
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                grid[x][y] = new Node(x, y);
            }
        }

        if (supermarketLayout != null && supermarketLayout.aisles != null) {
            for (Aisle aisle : supermarketLayout.aisles) {
                int startX = (int) (aisle.x * GRID_WIDTH);
                int endX = (int) ((aisle.x + aisle.width) * GRID_WIDTH);
                int startY = (int) (aisle.y * GRID_HEIGHT);
                int endY = (int) ((aisle.y + aisle.height) * GRID_HEIGHT);

                for (int x = startX; x < endX; x++) {
                    for (int y = startY; y < endY; y++) {
                        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                            grid[x][y].isObstacle = true;
                        }
                    }
                }
            }
        }
        aStarGridInitialized = true;
    }

    public void drawPath(PointF startPointInPixels, PointF endPointInPixels) {
        if (startPointInPixels == null || endPointInPixels == null || !aStarGridInitialized || getWidth() == 0 || getHeight() == 0) {
            clearPath();
            return;
        }

        float distance = (float) Math.sqrt(Math.pow(endPointInPixels.x - startPointInPixels.x, 2) + Math.pow(endPointInPixels.y - startPointInPixels.y, 2));

        // If the destination is very close to the start, don't draw a path.
        // Just place the marker at the destination. The threshold is slightly bigger than the marker radius.
        if (distance < markerRadius * 1.5) {
            this.destinationPoint = endPointInPixels;
            this.currentMarkerPosition = new PointF(endPointInPixels.x, endPointInPixels.y);
            fullPath.reset();
            animatedPath.reset();
            if (pathAnimator != null && pathAnimator.isRunning()) {
                pathAnimator.cancel();
            }
            invalidate();
            return;
        }

        this.destinationPoint = endPointInPixels;

        int startGridX = (int) (startPointInPixels.x / getWidth() * GRID_WIDTH);
        int startGridY = (int) (startPointInPixels.y / getHeight() * GRID_HEIGHT);
        int endGridX = (int) (endPointInPixels.x / getWidth() * GRID_WIDTH);
        int endGridY = (int) (endPointInPixels.y / getHeight() * GRID_HEIGHT);

        List<Node> pathNodes = findPath(startGridX, startGridY, endGridX, endGridY);

        fullPath.reset();
        if (pathNodes != null && !pathNodes.isEmpty()) {
            float cellWidth = (float) getWidth() / GRID_WIDTH;
            float cellHeight = (float) getHeight() / GRID_HEIGHT;

            Node firstNode = pathNodes.get(0);
            fullPath.moveTo(firstNode.x * cellWidth + cellWidth / 2, firstNode.y * cellHeight + cellHeight / 2);

            for (int i = 1; i < pathNodes.size(); i++) {
                Node node = pathNodes.get(i);
                fullPath.lineTo(node.x * cellWidth + cellWidth / 2, node.y * cellHeight + cellHeight / 2);
            }
        } else {
            fullPath.moveTo(startPointInPixels.x, startPointInPixels.y);
            fullPath.lineTo(endPointInPixels.x, endPointInPixels.y);
        }

        pathMeasure.setPath(fullPath, false);

        if (pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        pathAnimator.start();
    }

    private List<Node> findPath(int startX, int startY, int endX, int endY) {
        if (startX < 0 || startX >= GRID_WIDTH || startY < 0 || startY >= GRID_HEIGHT ||
            endX < 0 || endX >= GRID_WIDTH || endY < 0 || endY >= GRID_HEIGHT) {
            return null;
        }

        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                grid[x][y].reset();
            }
        }

        Node startNode = grid[startX][startY];
        Node endNode = grid[endX][endY];

        if (startNode.isObstacle) {
            Node alternativeStartNode = findNearestValidNode(startNode);
            if (alternativeStartNode != null) {
                startNode = alternativeStartNode;
            } else {
                return null;
            }
        }

        if (endNode.isObstacle) {
            Node alternativeEndNode = findNearestValidNode(endNode);
            if (alternativeEndNode != null) {
                endNode = alternativeEndNode;
            } else {
                return null;
            }
        }

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(Node::getF));
        boolean[][] closedList = new boolean[GRID_WIDTH][GRID_HEIGHT];

        startNode.g = 0;
        startNode.h = calculateHeuristic(startNode, endNode);
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            if (currentNode.equals(endNode)) {
                return reconstructPath(currentNode);
            }

            if (closedList[currentNode.x][currentNode.y]) {
                continue;
            }
            closedList[currentNode.x][currentNode.y] = true;

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;

                    int neighborX = currentNode.x + i;
                    int neighborY = currentNode.y + j;

                    if (neighborX < 0 || neighborX >= GRID_WIDTH || neighborY < 0 || neighborY >= GRID_HEIGHT) continue;

                    Node neighbor = grid[neighborX][neighborY];
                    if (neighbor.isObstacle || closedList[neighborX][neighborY]) continue;

                    if (i != 0 && j != 0) {
                        if (grid[currentNode.x + i][currentNode.y].isObstacle || grid[currentNode.x][currentNode.y + j].isObstacle) {
                            continue;
                        }
                    }

                    int newG = currentNode.g + ((i == 0 || j == 0) ? 10 : 14);
                    if (newG < neighbor.g || !openList.contains(neighbor)) {
                        neighbor.g = newG;
                        neighbor.h = calculateHeuristic(neighbor, endNode);
                        neighbor.parent = currentNode;
                        openList.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    private Node findNearestValidNode(Node originalNode) {
        for (int radius = 1; radius < 10; radius++) {
            for (int i = -radius; i <= radius; i++) {
                for (int j = -radius; j <= radius; j++) {
                    if (Math.abs(i) != radius && Math.abs(j) != radius) continue;
                    int checkX = originalNode.x + i;
                    int checkY = originalNode.y + j;
                    if (checkX >= 0 && checkX < GRID_WIDTH && checkY >= 0 && checkY < GRID_HEIGHT) {
                        Node candidate = grid[checkX][checkY];
                        if (!candidate.isObstacle) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private int calculateHeuristic(Node a, Node b) {
        return 10 * (Math.abs(a.x - b.x) + Math.abs(a.y - b.y));
    }

    private List<Node> reconstructPath(Node endNode) {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;
        while (currentNode != null) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public void clearPath() {
        if (pathAnimator != null && pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        fullPath.reset();
        animatedPath.reset();
        currentMarkerPosition = null;
        setDestinationImage(null);
        destinationPoint = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackgroundLayout(canvas);
        canvas.drawPath(animatedPath, pathPaint);

        if (destinationBitmap != null && destinationPoint != null) {
            float bitmapX = destinationPoint.x - destinationBitmap.getWidth() / 2f;
            float bitmapY = destinationPoint.y - destinationBitmap.getHeight() / 2f;
            canvas.drawBitmap(destinationBitmap, bitmapX, bitmapY, null);
        }

        if (currentMarkerPosition != null) {
            canvas.drawCircle(currentMarkerPosition.x, currentMarkerPosition.y, markerRadius, markerPaint);
        }
    }

    private void drawBackgroundLayout(Canvas canvas) {
        if (supermarketLayout == null || supermarketLayout.aisles == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        for (Aisle aisle : supermarketLayout.aisles) {
            float left = aisle.x * viewWidth;
            float top = aisle.y * viewHeight;
            float right = (aisle.x + aisle.width) * viewWidth;
            float bottom = (aisle.y + aisle.height) * viewHeight;
            canvas.drawRect(left, top, right, bottom, aislePaint);

            String aisleName = aisle.id.replace('_', ' ');
            float centerX = left + (right - left) / 2;
            float centerY = top + (bottom - top) / 2;
            float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(aisleName, centerX, textY, textPaint);
        }
    }
}
