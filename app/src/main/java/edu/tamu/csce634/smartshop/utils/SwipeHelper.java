package edu.tamu.csce634.smartshop.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import edu.tamu.csce634.smartshop.R;

public abstract class SwipeHelper extends ItemTouchHelper.SimpleCallback {
    private Context context;
    private int swipedPosition = -1;
    private static final float DELETE_BUTTON_WIDTH_DP = 100f;
    private float deleteButtonWidth;
    private boolean isAnimating = false;

    public SwipeHelper(Context context) {
        super(0, ItemTouchHelper.LEFT);
        this.context = context;
        // Convert dp to pixels
        deleteButtonWidth = DELETE_BUTTON_WIDTH_DP * context.getResources().getDisplayMetrics().density;
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
        // Never called due to high threshold
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder,
                           float dX, float dY, int actionState, boolean isCurrentlyActive) {
        
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            View foregroundView = itemView.findViewById(R.id.foreground_card);
            
            if (foregroundView != null) {
                int position = viewHolder.getAdapterPosition();
                
                // If we're animating or if this position is already swiped open and user is not actively swiping
                if (isAnimating || (position == swipedPosition && !isCurrentlyActive)) {
                    // Don't update the position - either animation is in progress or it's already open
                    return;
                }
                
                // Limit swipe distance to delete button width
                float translationX = Math.max(dX, -deleteButtonWidth);
                
                // Apply translation to foreground card only (only when actively swiping)
                if (isCurrentlyActive) {
                    foregroundView.setTranslationX(translationX);
                } else {
                    // User just released - start animation based on threshold
                    isAnimating = true;
                    float currentTranslation = foregroundView.getTranslationX();
                    
                    if (Math.abs(currentTranslation) > deleteButtonWidth * 0.3f) {
                        // Keep it open at delete button width
                        foregroundView.animate()
                                .translationX(-deleteButtonWidth)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    swipedPosition = position;
                                    isAnimating = false;
                                    setupDeleteListener(itemView, foregroundView, position);
                                })
                                .start();
                    } else {
                        // Close it
                        foregroundView.animate()
                                .translationX(0)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    isAnimating = false;
                                    if (swipedPosition == position) {
                                        swipedPosition = -1;
                                    }
                                })
                                .start();
                    }
                }
            }
        }
    }

    private void setupDeleteListener(View itemView, View foregroundView, int position) {
        View deleteBackground = itemView.findViewById(R.id.delete_background);
        View deleteIcon = itemView.findViewById(R.id.delete_icon);
        
        if (deleteBackground != null) {
            View.OnClickListener deleteListener = v -> {
                HapticFeedback.mediumClick(v);
                onDeleteClick(position);
                // Close the swipe after delete
                closeSwipe(foregroundView, position);
            };
            
            deleteBackground.setOnClickListener(deleteListener);
            if (deleteIcon != null) {
                deleteIcon.setOnClickListener(deleteListener);
            }
        }
        
        // Close swipe when tapping the foreground card
        foregroundView.setOnClickListener(v -> {
            closeSwipe(foregroundView, position);
        });
    }

    private void closeSwipe(View foregroundView, int position) {
        if (foregroundView != null && !isAnimating) {
            isAnimating = true;
            foregroundView.animate()
                    .translationX(0)
                    .setDuration(200)
                    .withEndAction(() -> {
                        isAnimating = false;
                        if (swipedPosition == position) {
                            swipedPosition = -1;
                        }
                        // Remove click listeners
                        foregroundView.setOnClickListener(null);
                        View parent = (View) foregroundView.getParent();
                        if (parent != null) {
                            View deleteBackground = parent.findViewById(R.id.delete_background);
                            View deleteIcon = parent.findViewById(R.id.delete_icon);
                            if (deleteBackground != null) deleteBackground.setOnClickListener(null);
                            if (deleteIcon != null) deleteIcon.setOnClickListener(null);
                        }
                    })
                    .start();
        }
    }

    public void closeOpenItem(RecyclerView recyclerView) {
        if (swipedPosition != -1) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPosition);
            if (viewHolder != null) {
                View foregroundView = viewHolder.itemView.findViewById(R.id.foreground_card);
                closeSwipe(foregroundView, swipedPosition);
            } else {
                swipedPosition = -1;
            }
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Critical: Don't call super and don't reset the view
        // We maintain the swiped state ourselves
        int position = viewHolder.getAdapterPosition();
        View foregroundView = viewHolder.itemView.findViewById(R.id.foreground_card);
        
        if (foregroundView != null) {
            // If this is the swiped item, keep it at the swiped position
            if (position == swipedPosition) {
                foregroundView.setTranslationX(-deleteButtonWidth);
            } else {
                // Otherwise ensure it's closed
                foregroundView.setTranslationX(0);
            }
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        // Return a value greater than 1.0 to prevent onSwiped from ever being called
        return 2.0f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        // Prevent fling from triggering swipe
        return Float.MAX_VALUE;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        // Prevent fast swipe from triggering delete
        return Float.MAX_VALUE;
    }

    public abstract boolean canSwipe(int position);
    public abstract void onDeleteClick(int position);
}