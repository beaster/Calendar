package com.ostrovok.calendar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ostrovok.calendar.CalendarUtils;

public class CalendarView extends View {

	private enum Direction {
		LEFT, RIGHT, NONE;
	}

	private class StateArrow {
		boolean down;
		boolean up;
		int color;
		Rect bounds = new Rect();

		public void reset() {
			down = up = false;
		}
	}

	private class Cell {
		Time mTime;
		Rect mRect = new Rect();
		int julianDay;

		public Cell() {

		}

		public Cell(Time t, Rect r) {
			mTime = t;
			mRect = r;
		}

		void initTime(Time t) {
			if (t != null) {
				mTime = t;
				julianDay = Time.getJulianDay(mTime.toMillis(true),
						mTime.gmtoff);
			}
		}

		void reset() {
			mRect.setEmpty();
			julianDay = 0;
			mTime = null;
		}
	}

	private final static int NUM_DAY = 7;

    private static int WIDTH_SEPARATOR_LINE = 1;// ширина линии 

	private static int PADDING_TITLE = 70; //смещенние заголовка с названием месяца и дней недели

	private static int PADDING_WEEK_DAY_FROM_BOTTOM = 10;//смещение дней недели относительно низа заголовка

	private static int WIDTH_TRIANGLE = 16;//длина треугольника( стрелки управления)

    private static float SCALE = -1;
	
    //TODO: Все строки и значения для простоты определил здесь, потом можно вынести все в соответствующие файлы
	
	// TODO: need localized ? DateUtils
    private static final String[] WEEK = {
            "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"
    };

	// TODO: need localized ? DateUtils.getMonthString(2,
	// DateUtils.LENGTH_LONG);
    private static final String[] MONTH = {
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь",
            "Октябрь", "Ноябрь", "Декабрь"
    };

	private int mWidthCell;
	private int mHeigthCell;
	private int mWidthArea;
	private int mHeightArea;
	private int mHeightAreaMonth;

	// Colors
	private int mLineColor;
	private int mLineRangeCellColor;
	private int mDefaultCellColor;
	private int mSelectedCellColor;
	private int mRangeCellColor;
	private int mTextWeekColor;
	private int mArrowDefaultColor;
	private int mArrowSelectedColor;

	private int mOtherMonthColor;
	private int mSelectMonthColor;
	private int mDefaultMonthColor;
	private int mRangeMonthColor;

	// Rect
	private Cell mCellSelectedStart = new Cell();
	private Cell mCellSelectedEnd = new Cell();

	private Rect mTemp = new Rect();

	// text
	private static int WEEK_FONT_SIZE = 12;
	private static int DAY_FONT_SIZE = 24;

	private static Typeface BOLD = Typeface.DEFAULT_BOLD;

	private Paint mTextPaint = new Paint();
	private Paint mArrowPaint = new Paint();
	private Paint mCellPaint = new Paint();
	private Paint mTempPaint = new Paint();

	private StateArrow mLeftArrow = new StateArrow();
	private StateArrow mRightArrow = new StateArrow();

	// time
	private Time mCurrentTime = new Time();

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CalendarView(Context ctx) {
		super(ctx);
		init();
	}

	private void init() {
		mLineColor = Color.rgb(125, 125, 125);
		mDefaultCellColor = Color.rgb(75, 75, 75);
		mSelectedCellColor = Color.rgb(164, 164, 164);
		mRangeCellColor = Color.rgb(123, 123, 123);
		mLineRangeCellColor = Color.rgb(145, 145, 145);
		mTextWeekColor = Color.WHITE;
		mArrowDefaultColor = Color.WHITE;
		mArrowSelectedColor = mDefaultCellColor;

		// month color
		mOtherMonthColor = Color.rgb(98, 98, 98);
		mSelectMonthColor = Color.BLACK;
		mDefaultMonthColor = Color.WHITE;
		mRangeMonthColor = Color.rgb(206, 206, 206);

		mLeftArrow.color = mArrowDefaultColor;
		mRightArrow.color = mArrowDefaultColor;

        if (SCALE == -1) {
            SCALE = getResources().getDisplayMetrics().density;
            float mScale = SCALE;
            if (mScale != 1) {
                WIDTH_SEPARATOR_LINE *= mScale;
                PADDING_TITLE *= mScale;
                PADDING_WEEK_DAY_FROM_BOTTOM *= mScale;
                WIDTH_TRIANGLE *= mScale;
                WEEK_FONT_SIZE *= mScale;
                DAY_FONT_SIZE *= mScale;
            }
		}

		mCurrentTime.set(System.currentTimeMillis());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mWidthCell = (w - (NUM_DAY + 1) * WIDTH_SEPARATOR_LINE) / NUM_DAY;
		mHeigthCell = mWidthCell;
		
        //Область рисования
		mWidthArea = (mWidthCell * NUM_DAY) + (NUM_DAY + 1)
				* WIDTH_SEPARATOR_LINE;
		mHeightArea = (mHeigthCell * (NUM_DAY - 1)) + (NUM_DAY)
				* WIDTH_SEPARATOR_LINE;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

        // потом можно пооптимизировать, рисовать элементы по одному разу,
        // которые не меняются
		drawTitle(canvas);
		drawArrow(canvas);
		drawMonth(canvas);

		canvas.save();
		canvas.translate(0, PADDING_TITLE);

		drawBackground(canvas);
		drawGrid(canvas);
		drawSelectedCell(canvas);
		drawDayMonth(canvas);

		canvas.restore();
	}

	private void drawTitle(Canvas canvas) {
		Rect r = new Rect(0, 0, mWidthArea, PADDING_TITLE);
		Paint p = new Paint();
		p.setColor(mLineColor);
		p.setStyle(Style.FILL);
		canvas.drawRect(r, p);

		// draw week
		int x = 0;
		int y = PADDING_TITLE - PADDING_WEEK_DAY_FROM_BOTTOM;
		for (int i = 0; i < NUM_DAY; i++) {
			drawWeekDay(canvas, WEEK[i], mTextPaint, x, y);
			x += mWidthCell + WIDTH_SEPARATOR_LINE;
		}
	}
	
    //Стрелки управления
	private void drawArrow(Canvas canvas) {
		// calculate area
		mTextPaint.setTextSize(WEEK_FONT_SIZE);
		int heightWeek = (int) Math.abs((mTextPaint.descent() + mTextPaint
				.ascent()));

		mHeightAreaMonth = PADDING_TITLE - PADDING_WEEK_DAY_FROM_BOTTOM
				- heightWeek;
        int hTriangle = (int)(WIDTH_TRIANGLE * Math.sin(Math.PI / 3));//высота треугольника

		Path mPath = new Path();
		mPath.moveTo(WIDTH_TRIANGLE / 2, 0);

		mPath.lineTo(0, hTriangle);
		mPath.lineTo(WIDTH_TRIANGLE, hTriangle);
		mPath.lineTo(WIDTH_TRIANGLE / 2, 0);

		mArrowPaint.setColor(mRightArrow.color);
		mArrowPaint.setStyle(Style.FILL);
		mArrowPaint.setAntiAlias(true);
		
        //Координаты центра
		int centrX = mWidthCell * (NUM_DAY - 1) + WIDTH_SEPARATOR_LINE
				* NUM_DAY + mWidthCell / 2;
		int centerY = mHeightAreaMonth / 2;

        // simple rect - область нажатия на стрелку
		int delta = WIDTH_TRIANGLE * 2;
		mRightArrow.bounds.set(centrX - delta, centerY - delta, centrX + delta,
				centerY + delta);

		// right
		canvas.save();
		canvas.rotate(90, centrX, centerY);
		canvas.translate(centrX - WIDTH_TRIANGLE / 2, centerY - hTriangle / 2);
		canvas.drawPath(mPath, mArrowPaint);
		canvas.restore();

		// left
		centrX = WIDTH_SEPARATOR_LINE + mWidthCell / 2;
		mLeftArrow.bounds.set(centrX - WIDTH_TRIANGLE,
				centerY - WIDTH_TRIANGLE, centrX + WIDTH_TRIANGLE, centerY
						+ WIDTH_TRIANGLE);
		mArrowPaint.setColor(mLeftArrow.color);

		canvas.save();
		canvas.rotate(-90, centrX, centerY);
		canvas.translate(centrX - WIDTH_TRIANGLE / 2, centerY - hTriangle / 2);
		canvas.drawPath(mPath, mArrowPaint);
		canvas.restore();
	}
	
    //Рисуем текущий месяц
	private void drawMonth(Canvas canvas) {
		mTextPaint.setColor(mTextWeekColor);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTypeface(BOLD);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setTextSize(DAY_FONT_SIZE);

		int heightMonth = (int) Math.abs((mTextPaint.descent() + mTextPaint
				.ascent()));
		canvas.drawText(MONTH[mCurrentTime.month] + " " + mCurrentTime.year,
				mWidthArea / 2, mHeightAreaMonth / 2 + heightMonth / 2,
				mTextPaint);
	}
	
	private void drawWeekDay(Canvas canvas, String week, Paint p, int x, int y) {
		p.setTypeface(BOLD);
		p.setTextSize(WEEK_FONT_SIZE);
		p.setTextAlign(Paint.Align.CENTER);
		p.setAntiAlias(true);
		p.setColor(mTextWeekColor);

		canvas.drawText(week, x + WIDTH_SEPARATOR_LINE + mWidthCell / 2, y, p);
	}

	private void drawBackground(Canvas canvas) {

		Paint p = mTempPaint;
		p.setColor(mDefaultCellColor);
		p.setStyle(Style.FILL);

		// draw background
		Rect r = new Rect();
		r.left = 0;
		r.top = 0;
		r.right = mWidthArea;
		r.bottom = mHeightArea;
		canvas.drawRect(r, p);

	}

	// draw grid 7*6
	private void drawGrid(Canvas canvas) {
		// draw horisontal lines
		Paint p = mTempPaint;
		p.setColor(mLineColor);
		p.setStrokeWidth(0);
		p.setStyle(Style.STROKE);

		int startY = 0;
		for (int i = 0; i < NUM_DAY; i++) {
			canvas.drawLine(0, startY, mWidthArea, startY, p);
			startY += (mHeigthCell + WIDTH_SEPARATOR_LINE);
		}

		// draw vertical lines
		int startX = 0;
		for (int i = 0; i <= NUM_DAY; i++) {
			canvas.drawLine(startX, 0, startX, mHeightArea, p);
			startX += (mWidthCell + WIDTH_SEPARATOR_LINE);
		}
	}
	
    //Находим область нажатия
	private Rect findRectByXY(int x, int y) {
		y = y - PADDING_TITLE;
		Rect r = new Rect();

		// find day week
		final int fullCellWidth = mWidthCell + WIDTH_SEPARATOR_LINE;
		int startX = 0, endX = fullCellWidth;
		for (int i = 0; i <= NUM_DAY; i++) {
			if (x >= startX && x < endX) {
				break;
			}

			startX += fullCellWidth;
			endX += fullCellWidth;
		}

		final int fullCellHeight = mHeigthCell + WIDTH_SEPARATOR_LINE;
		int startY = 0, endY = fullCellHeight;
		for (int i = 0; i <= NUM_DAY; i++) {
			if (y >= startY && y < endY) {
				break;
			}

			startY += fullCellHeight;
			endY += fullCellHeight;
		}

		if (endX > mWidthArea || endY > mHeightArea) {
			return null;
		} else {
			r.left = startX + WIDTH_SEPARATOR_LINE;
			r.top = startY + WIDTH_SEPARATOR_LINE;
			r.right = endX;
			r.bottom = endY;
			return r;
		}
	}

	private void drawDayMonth(Canvas canvas) {
        int weekDay = CalendarUtils.getFirstDayOfWeek(mCurrentTime);// Понедельник - 0

		mTextPaint.setColor(mTextWeekColor);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTypeface(BOLD);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setTextSize(DAY_FONT_SIZE);

		int maxDay = mCurrentTime.getActualMaximum(Time.MONTH_DAY);

		int startX = 0 + WIDTH_SEPARATOR_LINE;
		int startY = 0 + WIDTH_SEPARATOR_LINE;

		Time t = CalendarUtils.getFirstTime(new Time(mCurrentTime));
		
        //При  рисовании чисел месяца используем JulianDay для удобства.
		int startCurrentMonthDay = Time.getJulianDay(t.toMillis(true), t.gmtoff);
		int endCurrentMontDay = startCurrentMonthDay + maxDay;
		
        //Начало и конец дней, которые поместятся в календарь
		int startJulianDay = startCurrentMonthDay - weekDay; // start julian day
		int maxJulianday = startJulianDay + 41;
		
		boolean hasRangeSelected = !mCellSelectedStart.mRect.isEmpty()
				&& !mCellSelectedEnd.mRect.isEmpty();

		do {
			t.setJulianDay(startJulianDay);
			int color = getCellColor(startJulianDay, startCurrentMonthDay,
					endCurrentMontDay, hasRangeSelected);
			mTextPaint.setColor(color);

			drawDayNumber(canvas, String.valueOf(t.monthDay), startX, startY,
					mTextPaint);
			// next iteration
			startX += WIDTH_SEPARATOR_LINE + mWidthCell;
			if (startX > mWidthArea) {
				startX = WIDTH_SEPARATOR_LINE;
				startY += WIDTH_SEPARATOR_LINE + mWidthCell;
				continue;
			}
			startJulianDay++;
		} while (startJulianDay <= maxJulianday);

	}

	private int getCellColor(int julianDay, int startCurrentJulianDay,
			int endCurrentJulianDay, boolean hasRange) {

		if (hasRange) {
			if (julianDay > mCellSelectedStart.julianDay
					&& julianDay < mCellSelectedEnd.julianDay) {
				return mRangeMonthColor;
			}
		}

		if (julianDay == mCellSelectedStart.julianDay
				|| julianDay == mCellSelectedEnd.julianDay) {
			return mSelectMonthColor;
		} else if (julianDay < startCurrentJulianDay
				|| julianDay >= endCurrentJulianDay) {
			return mOtherMonthColor;
		} else {
			return mDefaultMonthColor;
		}

	}
	
    //Рисуем область выделения
	private void drawRangeDay(Canvas canvas, Cell c1, Cell c2) {
		int first = c1.julianDay;
		int last = c2.julianDay;

		mCellPaint.setAntiAlias(false);
		Time t = new Time();

		for (int i = first; i <= last; i++) {
			mCellPaint.setStyle(Style.FILL);
			mCellPaint.setStrokeWidth(0);
			mCellPaint.setColor(i == first || i == last ? mSelectedCellColor
					: mRangeCellColor);

			t.setJulianDay(i);
			Rect r = getRectByTime(t, mTemp);
			canvas.drawRect(r, mCellPaint);

			// draw bouns
			r.left--;
			r.top--;
			mCellPaint.setStrokeWidth(1.0f);
			mCellPaint.setStyle(Style.STROKE);
			mCellPaint.setColor(i == first || i == last ? mSelectedCellColor
					: mLineRangeCellColor);
			canvas.drawRect(r, mCellPaint);
		}
	}

	private void drawDayNumber(Canvas c, String day, int x, int y, Paint p) {
		float heightDay = (p.descent() + p.ascent());
		c.drawText(day, x + mWidthCell / 2,
				y + mHeigthCell / 2 - heightDay / 2, p);
	}

	private void drawSelectedCell(Canvas canvas) {
		Rect r1 = mCellSelectedStart.mRect;
		Rect r2 = mCellSelectedEnd.mRect;

		// fill state cell
		if (!r1.isEmpty()) {
			fillCell(r1, mCellSelectedStart);
		}

		if (!r2.isEmpty()) {
			fillCell(r2, mCellSelectedEnd);
		}

		if (!r1.isEmpty() && !r2.isEmpty()) {
			drawRangeDay(canvas, mCellSelectedStart, mCellSelectedEnd);
		} else {
			Paint p = mTempPaint;
			p.setColor(mSelectedCellColor);
			p.setStyle(Style.FILL);

			if (!r1.isEmpty()) {
				canvas.drawRect(r1, p);
			}
			if (!r2.isEmpty()) {
				canvas.drawRect(r2, p);
			}
		}
	}

	private Cell rectToCell(Rect r) {
		return fillCell(r, new Cell());
	}

	private Cell fillCell(Rect r, Cell c) {
		int currentWeekDay = CalendarUtils.getFirstDayOfWeek(mCurrentTime);
		int rectWeekDay = r.left / mWidthCell;
		int dec = r.top / mHeigthCell;

		int day = dec * NUM_DAY + rectWeekDay - currentWeekDay + 1; // day current
		Time t = new Time(mCurrentTime);
		t.monthDay = day;
		t.normalize(true);

		c.initTime(t);
		c.mRect.set(r);
		return c;
	}

	private Rect getRectByTime(Time mTime, Rect mTemp) {
		int currentWeekDay = CalendarUtils.getFirstDayOfWeek(mCurrentTime);

		// first day of week
		final Time t = CalendarUtils.getFirstTime(new Time(mCurrentTime));

		int checkedJulianDay = Time.getJulianDay(mTime.toMillis(true),
				mTime.gmtoff);
		int firstJulianDayWeek = Time.getJulianDay(t.toMillis(true), t.gmtoff);

		int delta = checkedJulianDay - firstJulianDayWeek;

		int countY = (delta + currentWeekDay) / 7;
		int countX = CalendarUtils.getDayOfWeek(mTime);

		Rect r = mTemp;
		r.left = countX * (WIDTH_SEPARATOR_LINE + mWidthCell)
				+ WIDTH_SEPARATOR_LINE;
		r.top = countY * (WIDTH_SEPARATOR_LINE + mHeigthCell)
				+ WIDTH_SEPARATOR_LINE;
		r.right = r.left + mWidthCell;
		r.bottom = r.top + mHeigthCell;

		return r;
	}

	private boolean isNeedSelectedRange(Rect first, Rect last) {
		if (first.contains(last)) {
			return false;
		}

		// check by coord
		final int firstX = first.centerX();
		final int firstY = first.centerY();

		final int lastX = last.centerX();
		final int lastY = last.centerY();

		if (firstY < lastY) {
			return true;
		}

		if (firstY == lastY) {
			if (firstX < lastX) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}
	
    //Вычисляем, куда нажал юзер, и делаем соответствующие пометки
	private void calculateRangeSelection(int x, int y) {
		Rect r = findRectByXY(x, y);
		Rect r1 = mCellSelectedStart.mRect;
		Rect r2 = mCellSelectedEnd.mRect;

		if (r != null) {
			if (r1.isEmpty()) {
				r1.set(r);
			} else if (isNeedSelectedRange(r1, r)) {
				r2.set(r);
			} else {
				// update selected rect
				r1.set(r);
				mCellSelectedEnd.reset();
			}
		} else {
			// reset
			// mCellSelectedStart.reset();
			// mCellSelectedEnd.reset();
		}
	}

	private void resetSelectedCell() {
		mCellSelectedStart.reset();
		mCellSelectedEnd.reset();
	}

	private void calculateArrowState(int x, int y, boolean press) {

		mLeftArrow.color = mArrowDefaultColor;
		mRightArrow.color = mArrowDefaultColor;

		if (mLeftArrow.bounds.contains(x, y)) {
			if (press) {
				mLeftArrow.color = mArrowSelectedColor;
				mLeftArrow.down = true;
			} else {
				mLeftArrow.up = true;
			}
		}

		if (mRightArrow.bounds.contains(x, y)) {
			if (press) {
				mRightArrow.color = mArrowSelectedColor;
				mRightArrow.down = true;
			} else {
				mRightArrow.up = true;
			}
		}

		if (!press) {// up
			if (mRightArrow.down && mRightArrow.up) {
				onPressArrow(Direction.RIGHT);
			} else if (mLeftArrow.down && mLeftArrow.up) {
				onPressArrow(Direction.LEFT);
			}

			mRightArrow.reset();
			mLeftArrow.reset();
		}
	}

	private void onPressArrow(Direction dir) {
		resetSelectedCell();

		switch (dir) {
		case LEFT:
			mCurrentTime.month--;
			mCurrentTime.normalize(true);
			break;
		case RIGHT:
			mCurrentTime.month++;
			mCurrentTime.normalize(true);
			break;
		default:
			break;
		}
	}
	
    //TODO:Сделан просто переопределение onTouch
    //Лучше потом сделать GestureDetector
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();

		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			calculateRangeSelection(x, y);
			calculateArrowState(x, y, true);
			invalidate();
			return true;
		case MotionEvent.ACTION_UP:
			calculateArrowState(x, y, false);
			invalidate();
			return true;
		default:
			return super.onTouchEvent(event);
		}

	}
}
