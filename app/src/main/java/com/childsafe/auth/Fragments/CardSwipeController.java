package com.childsafe.auth.Fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.childsafe.auth.R;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

class CardSwipeController extends ItemTouchHelper.Callback {

    public static class SwipeControllerActions {
        //implement this class for your button action
        public void onClicked(int position) {
        }

    }

    private boolean swipeBack = false;

    private boolean buttonShowedState = false;

    private int cardposition = -1;

    private RectF buttonInstance = null;

    private RecyclerView.ViewHolder currentItemViewHolder = null;

    private SwipeControllerActions buttonsActions = null;

    private static final float buttonWidth = 280;

    private static final double swiperatio=0.8;

    private Context mContext;

    public CardSwipeController(SwipeControllerActions buttonsActions, Context context) {
        this.buttonsActions = buttonsActions;
        this.mContext = context;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof View.OnLongClickListener) {
            if (buttonShowedState) {
                if (viewHolder.getAdapterPosition() == cardposition) {
                    return makeMovementFlags(0, RIGHT);
                } else {
                    return makeMovementFlags(0, 0);
                }
            } else {
                return makeMovementFlags(0, LEFT);
            }
        } else {
            return makeMovementFlags(0, 0);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }


    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if (actionState == ACTION_STATE_SWIPE) {
            Log.i("swipe ", ((Float) dX).toString());
            if (buttonShowedState && dX <= 0) {
                super.onChildDraw(c, recyclerView, viewHolder, Math.min(dX, -buttonWidth), dY, actionState, isCurrentlyActive);
            } else if (buttonShowedState && dX > 0) {
                super.onChildDraw(c, recyclerView, viewHolder, Math.min(0, -buttonWidth + dX), dY, actionState, isCurrentlyActive);
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        } else {
            //not swipe
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        currentItemViewHolder = viewHolder;

    }

    private void setTouchListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                if (swipeBack) {
                    if (dX < -buttonWidth * swiperatio) {
                        //turn on  a button
                        buttonShowedState = true;
                        cardposition = viewHolder.getAdapterPosition();
                        setItemsClickable(recyclerView, false);
                    }
                    if (dX > buttonWidth * swiperatio && viewHolder.getAdapterPosition() == cardposition) {
                        //turn off the button
                        buttonShowedState = false;
                        cardposition = -1;
                        setItemsClickable(recyclerView, true);

                    }
                }

                if (buttonShowedState && event.getAction() == MotionEvent.ACTION_UP && dX == 0) {
                    if (buttonsActions != null && buttonInstance != null && buttonInstance.contains(event.getX(), event.getY())) {
                        buttonsActions.onClicked(viewHolder.getAdapterPosition());
                    }

                }
                return false;
            }
        });
    }

    //disable all other card when button is active
    private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setEnabled(isClickable);
        }
    }

    private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
        float buttonWidthWithoutPadding = buttonWidth - 10;
        float corners = 16;

        View itemView = viewHolder.itemView;
        Paint p = new Paint();

        RectF rightButton = new RectF(itemView.getRight() - buttonWidthWithoutPadding, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        p.setColor(Color.RED);
        c.drawRoundRect(rightButton, corners, corners, p);

        TextPaint pp = new TextPaint();
        drawText(mContext.getText(R.string.action_unlink).toString(), c, rightButton, pp);

        buttonInstance = null;
        if (buttonShowedState) {
            buttonInstance = rightButton;
        }
    }

    private void drawText(String text, Canvas c, RectF button, TextPaint pp) {
        float textSize = 40;
        pp.setColor(Color.WHITE);
        pp.setAntiAlias(true);
        pp.setTextSize(textSize);
        pp.setTextScaleX(1.06F);
        pp.setTypeface(Typeface.create(null, 600, false));

        float textWidth = pp.measureText(text);
        c.drawText(text, button.centerX() - (textWidth / 2), button.centerY() + (textSize / 2), pp);
    }

    public void onDraw(Canvas c) {
        if (currentItemViewHolder != null) {
            drawButtons(c, currentItemViewHolder);
        }
    }

    public void clearCache() {
        buttonShowedState = false;
        cardposition = -1;
        buttonInstance = null;
        currentItemViewHolder = null;
    }
}


