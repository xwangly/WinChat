package com.ty.winchat.widget;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ty.winchat.R;


public class PullToRefreshExpandableListView extends ExpandableListView implements OnScrollListener {

	/** �ɿ�ˢ�� */
	private final static int RELEASE_TO_REFRESH = 0;
	/** ����ˢ�� */
	private final static int PULL_TO_REFRESH = 1;
	/** ����ˢ���� */
	private final static int REFRESHING = 2;
	/** ��� */
	private final static int DONE = 3;

	// ʵ�ʵ�padding�ľ����������ƫ�ƾ���ı���
	private final static int RATIO = 3;

	private LayoutInflater inflater;

	private LinearLayout headView;

	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	/** ��ͷͼ�� */
	private ImageView arrowImageView;
	private ProgressBar progressBar;

	private RotateAnimation animation;
	// ��ת����
	private RotateAnimation reverseAnimation;

	private int headContentWidth;
	private int headContentHeight;

	/** ���ư��µ����λ�� */
	private int startY;
	private int firstItemIndex;

	private int state;

	private boolean isBack;

	private OnRefreshListener refreshListener;

	private boolean isRefreshable;

	public PullToRefreshExpandableListView(Context context) {
		super(context);
		init(context);
	}

	public PullToRefreshExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		inflater = LayoutInflater.from(context);

		headView = (LinearLayout) inflater.inflate(R.layout.refresh_head, null);//����ͷ����

		arrowImageView = (ImageView) headView.findViewById(R.id.head_arrowImageView);//��ͷ
		arrowImageView.setMinimumWidth(70);
		arrowImageView.setMinimumHeight(50);
		progressBar = (ProgressBar) headView.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView.findViewById(R.id.head_lastUpdatedTextView);

		// measureView(headView);
		headView.measure(0, 0);
		headContentHeight = headView.getMeasuredHeight();//ͷ���߶�
		headContentWidth = headView.getMeasuredWidth();//ͷ�����
		/*
		 * ����padding -1 * headContentHeight�Ϳ��԰Ѹ�headview��������Ļ������
		 * ǰ����Ҫ�õ�headview��ȷ�и߶�
		 */
		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		/*
		 * Invalidate the whole view. If the view is visible,
		 * onDraw(android.graphics.Canvas) will be called at some point in the
		 * future. This must be called from a UI thread. To call from a non-UI
		 * thread, call postInvalidate().
		 */
		headView.invalidate();

//		Log.v("size", "width:" + headContentWidth + " height:" + headContentHeight);

		addHeaderView(headView, null, false);
		setOnScrollListener(this);

		animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);

		state = DONE;
		isRefreshable = false;
	}

	/**
	 * ʵ��OnScrollListener�ӿڵ���������
	 */
	public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2, int arg3) {
		firstItemIndex = firstVisiableItem;
	}

	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}

	/**
	 * ���ô����¼� �ܵ�˼·����
	 * 
	 * 1 ACTION_DOWN����¼��ʼλ��
	 * 
	 * 2 ACTION_MOVE�����㵱ǰλ������ʼλ�õľ��룬������state��״̬
	 * 
	 * 3 ACTION_UP������state��״̬���ж��Ƿ�����
	 */
	public boolean onTouchEvent(MotionEvent event) {

		if (isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (firstItemIndex == 0) {
					startY = (int) event.getY();
//					Log.v(TAG, "��downʱ���¼��ǰλ�á�");
				}
				break;
			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();
				if (state == PULL_TO_REFRESH) {
					setSelection(0);// ���������Ҫ
					// ���������Խ���RELEASE_TO_REFRESH��״̬
					if ((tempY - startY) / RATIO >= headContentHeight) {
						state = RELEASE_TO_REFRESH;
						isBack = true;
						changeHeaderViewByState();
//						Log.v(TAG, "��done��������ˢ��״̬ת�䵽�ɿ�ˢ��");
					}
					// ���Ƶ�����
					else if (tempY - startY <= 0) {
						state = DONE;
						changeHeaderViewByState();
//						Log.v(TAG, "��DOne��������ˢ��״̬ת�䵽done״̬");
					}
					headView.setPadding(0, -headContentHeight + (tempY - startY) / RATIO, 0, 0);
				}
				if (state == RELEASE_TO_REFRESH) {
					setSelection(0);
					// �������ˣ��Ƶ�����Ļ�㹻�ڸ�head�ĳ̶ȣ����ǻ�û���Ƶ�ȫ���ڸǵĵز�
					if (((tempY - startY) / RATIO < headContentHeight) && (tempY - startY) > 0) {
						state = PULL_TO_REFRESH;
						changeHeaderViewByState();
//						Log.v(TAG, "���ɿ�ˢ��״̬ת�䵽����ˢ��״̬");
					}
					headView.setPadding(0, -headContentHeight + (tempY - startY) / RATIO, 0, 0);
				}
				// done״̬��
				if (state == DONE) {
					if (tempY - startY > 0) {
						state = PULL_TO_REFRESH;
						changeHeaderViewByState();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if (state != REFRESHING) {
					// ����ˢ��״̬
					if (state == PULL_TO_REFRESH) {
						state = DONE;
						changeHeaderViewByState();
//						Log.v(TAG, "����ˢ��״̬����done״̬");
					}
					if (state == RELEASE_TO_REFRESH) {
						state = REFRESHING;
						changeHeaderViewByState();
						onRefresh();
//						Log.v(TAG, "�ɿ�ˢ��״̬����done״̬");
					}
				}
				isBack = false;
				break;

			}
		}

		return super.onTouchEvent(event);
	}

	/**
	 * ��״̬�ı�ʱ�򣬵��ø÷������Ը��½���
	 */
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_TO_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);

			tipsTextview.setText("�ɿ�ˢ��");

//			Log.v(TAG, "��ǰ״̬���ɿ�ˢ��");
			break;
		case PULL_TO_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			tipsTextview.setText("����ˢ��");
			// ��RELEASE_To_REFRESH״̬ת������
			if (isBack) {
				isBack = false;
				arrowImageView.startAnimation(reverseAnimation);
			}
//			Log.v(TAG, "��ǰ״̬������ˢ��");
			break;

		case REFRESHING:
			headView.setPadding(0, 0, 0, 0);
			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText("����ˢ��...");
			break;
		case DONE:
			headView.setPadding(0, -headContentHeight, 0, 0);
			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.arrow);
			tipsTextview.setText("����ˢ��");
			break;
		}
	}

	public void setOnRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void onRefreshComplete() {
		state = DONE;
		lastUpdatedTextView.setText("�������:" + new Date().toLocaleString());
		changeHeaderViewByState();
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}

	// �˷���ֱ���հ��������ϵ�һ������ˢ�µ�demo���˴��ǡ����ơ�headView��width�Լ�height
//	private void measureView(View child) {
//		ViewGroup.LayoutParams p = child.getLayoutParams();
//		if (p == null) {
//			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//		}
//		// Does the hard part of measureChildren: figuring out the MeasureSpec
//		// to pass to a particular child.
//		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
//		int lpHeight = p.height;
//		int childHeightSpec;
//		if (lpHeight > 0) {
//			// Creates a measure specification based on the supplied size and
//			// mode.
//			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
//		} else {
//			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
//		}
//		child.measure(childWidthSpec, childHeightSpec);
//	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		lastUpdatedTextView.setText("�������:" + new Date().toLocaleString());
		super.setAdapter(adapter);
	}

}
