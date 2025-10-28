package edu.tamu.csce634.smartshop.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import edu.tamu.csce634.smartshop.R;

public abstract class SwipeHelper extends ItemTouchHelper.Callback {
    private Context context;
    private Drawable deleteIcon;
    private int swipedPosition = -1;
    private static final float DELETE_BUTTON_WIDTH = 250f;

    public SwipeHelper(Context context) {
        this.context = context;
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        if (deleteIcon != null) {
            deleteIcon.setTint(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        if (canSwipe(position)) {
            return makeMovementFlags(0, ItemTouchHelper.LEFT);
        }
        return 0;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder,
                           float dX, float dY, int actionState, boolean isCurrentlyActive) {
        
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            int position = viewHolder.getAdapterPosition();

            // Limit swipe distance
            float translationX = Math.max(dX, -DELETE_BUTTON_WIDTH);
            
            // Draw delete icon
            if (translationX < 0) {
                drawDeleteIcon(c, itemView);
            }
            
            // Apply translation
            itemView.setTranslationX(translationX);

            // When user releases finger
            if (!isCurrentlyActive) {
                if (Math.abs(translationX) > DELETE_BUTTON_WIDTH * 0.3f) {
                    // Keep it open
                    itemView.animate()
                            .translationX(-DELETE_BUTTON_WIDTH)
                            .setDuration(200)
                            .withEndAction(() -> {
                                swipedPosition = position;
                                setupTouchListener(itemView, position);
                            })
                            .start();
                } else {
                    // Close it
                    itemView.animate()
                            .translationX(0)
                            .setDuration(200)
                            .start();
                }
            }
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        // Draw delete icon on top when item is swiped
        if (viewHolder.getAdapterPosition() == swipedPosition && viewHolder.itemView.getTranslationX() != 0) {
            drawDeleteIcon(c, viewHolder.itemView);
        }
    }

    private void drawDeleteIcon(Canvas c, View itemView) {
        if (deleteIcon != null) {
            int itemHeight = itemView.getHeight();
            int iconSize = 80;
            int iconMargin = (itemHeight - iconSize) / 2;

            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + iconSize;
            int iconLeft = itemView.getRight() - iconMargin - iconSize;
            int iconRight = itemView.getRight() - iconMargin;

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(c);
        }
    }

    private void setupTouchListener(View itemView, int position) {
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                float x = e.getX();
                float itemWidth = itemView.getWidth();

                // Check if tap is in delete button area
                if (x > itemWidth - DELETE_BUTTON_WIDTH) {
                    onDeleteClick(position);
                    return true;
                } else {
                    // Tap elsewhere - close
                    closeSwipe(itemView, position);
                    return true;
                }
            }
        });

        itemView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void closeSwipe(View itemView, int position) {
        itemView.animate()
                .translationX(0)
                .setDuration(200)
                .withEndAction(() -> {
                    if (swipedPosition == position) {
                        swipedPosition = -1;
                    }
                    itemView.setOnTouchListener(null);
                })
                .start();
    }

    public void closeOpenItem(RecyclerView recyclerView) {
        if (swipedPosition != -1) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPosition);
            if (viewHolder != null) {
                closeSwipe(viewHolder.itemView, swipedPosition);
            } else {
                swipedPosition = -1;
            }
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Don't call super to prevent automatic snap back
        // The animation is handled in onChildDraw
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.99f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return Float.MAX_VALUE;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return Float.MAX_VALUE;
    }

    public abstract boolean canSwipe(int position);
    public abstract void onDeleteClick(int position);
}