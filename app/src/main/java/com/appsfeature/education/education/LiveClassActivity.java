package com.appsfeature.education.education;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appsfeature.education.R;
import com.appsfeature.education.activity.BaseActivity;
import com.appsfeature.education.entity.PresenterModel;
import com.appsfeature.education.model.EducationModel;
import com.appsfeature.education.player.util.YTUtility;
import com.appsfeature.education.util.SupportUtil;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class LiveClassActivity extends BaseActivity {

    private View llNoData;
    private EducationModel mVideoModel;
    private TextView tvTitle, tvSubject, tvDate;
    private Button btnLiveClass;
    private boolean isActiveLiveClass = false;
    private ImageView ivPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_class);
//        setUpToolBar("Live Class");
        initUi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchDataFromServer();
    }

    private void fetchDataFromServer() {
        active = true;
        if (getExtraProperty() != null) {
            appPresenter.getDynamicData(getExtraProperty());
        }
    }

    private void initUi() {
        llNoData = findViewById(R.id.ll_no_data);
        tvTitle = findViewById(R.id.tv_live_title);
        tvSubject = findViewById(R.id.tv_live_subject);
        tvDate = findViewById(R.id.tv_live_date);
        ivPreview = findViewById(R.id.pic);
        btnLiveClass = findViewById(R.id.btn_open_live_class);
        btnLiveClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isActiveLiveClass && mVideoModel != null) {
                    YTUtility.playVideo(LiveClassActivity.this, mVideoModel, true);
                } else {
                    SupportUtil.showToast(LiveClassActivity.this, "Class was not live, Please wait.");
                }
            }
        });
        ivPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLiveClass.performClick();
            }
        });
        hideViews();
    }

    @Override
    public void onUpdateUI(PresenterModel response) {
        SupportUtil.showNoData(llNoData, View.GONE);
        llNoData.setVisibility(View.GONE);
        if (response.getEducationList() != null && response.getEducationList().size() > 0) {
            mVideoModel = response.getEducationList().get(0);
            loadData();
            if (menuRefresh != null) {
                menuRefresh.setVisible(false);
            }
        } else {
            SupportUtil.showNoData(llNoData, View.VISIBLE);
            ivPreview.setVisibility(View.GONE);
            hideViews();
            if (menuRefresh != null) {
                menuRefresh.setVisible(true);
            }
        }
    }

    @Override
    public void onErrorOccurred(Exception e) {
        onUpdateUI(new PresenterModel());
    }

    private void hideViews() {
        tvTitle.setVisibility(View.GONE);
        tvSubject.setVisibility(View.GONE);
        tvDate.setVisibility(View.GONE);
        btnLiveClass.setText(getString(R.string.no_live_class_available));
    }

    private void loadData() {
        tvTitle.setText(mVideoModel.getLectureName());
        tvSubject.setText(mVideoModel.getSubjectName());
        tvDate.setText(SupportUtil.getDateFormatted(mVideoModel.getLiveClassDate(), mVideoModel.getLiveClassTime()));
        btnLiveClass.setText(getString(R.string.open_live_class));
        tvTitle.setVisibility(View.VISIBLE);
        tvSubject.setVisibility(View.VISIBLE);
        tvDate.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(mVideoModel.getLectureVideo())) {
            String videoPreviewUrl = YTUtility.getYoutubePlaceholderImage(YTUtility.getVideoIdFromUrl(mVideoModel.getLectureVideo()));
            Picasso.get().load(videoPreviewUrl)
                    .placeholder(R.drawable.ic_yt_placeholder)
                    .error(R.drawable.ic_yt_placeholder)
                    .into(ivPreview);
            ivPreview.setVisibility(View.VISIBLE);
        }else {
            ivPreview.setVisibility(View.INVISIBLE);
        }

        calculateTimeLeft();
        startCountDown();
    }

    private void calculateTimeLeft() {
        Calendar mCalender = getCalendar();
        if (mCalender != null) {
            long seconds = ((mCalender.getTimeInMillis() - System.currentTimeMillis()) / 1000);

            int day = (int) TimeUnit.SECONDS.toDays(seconds);
            long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24L);
            long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
            long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
            String timeLeft = String.format(Locale.US, "%d Days %d hr %d min %d sec", day, (int) hours, (int) minute, (int) second);
            btnLiveClass.setText(timeLeft);
            if (day <= 0 && hours <= 0 && minute <= 0 && second <= 0) {
                isActiveLiveClass = true;
                btnLiveClass.setText(getString(R.string.open_live_class));
            }
        }
    }

    private String calTimeLeft() {
        Calendar mCalender = getCalendar();
        long seconds = ((mCalender.getTimeInMillis() - System.currentTimeMillis()) / 1000);
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24L);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
        return String.format(Locale.US, "%d Days %d hr %d min %d sec", day, (int) hours, (int) minute, (int) second);
    }

    public Calendar getCalendar() {
        try {
            Date inputDate = new SimpleDateFormat("yyyy-MM-dd-HH:mm", Locale.US).parse(mVideoModel.getLiveClassDate() + "-" + mVideoModel.getLiveClassTime());
            Calendar date = Calendar.getInstance();
            if (inputDate != null) {
                date.setTime(inputDate);
            }
//            date.add(Calendar.MONTH, 1);
//            date.set(inputDate.getYear(), date.get(Calendar.MONTH), 1, 0, 0, 0);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Calendar getCalendar(Date inputDate) {
        Calendar date = Calendar.getInstance();
        if (inputDate != null) {
            date.setTime(inputDate);
        }
        return date;
    }

    private boolean active;

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
        if (countDown != null) {
            countDown.cancel();
        }
    }

    private CountDownTimer countDown;

    private void startCountDown() {
        if (countDown != null) {
            countDown.cancel();
        }
        countDown = new CountDownTimer(24 * 60 * 60, 1000) {
            public void onTick(long millisUntilFinished) {
                if (active) {
                    calculateTimeLeft();
                } else
                    this.cancel();
            }
            public void onFinish() {
                if (active) {
                    calculateTimeLeft();
                } else
                    this.cancel();
            }
        }.start();
    }


    private void startCountDown(long maxTimeCount) {
        if (countDown != null) {
            countDown.cancel();
        }
        countDown = new CountDownTimer(maxTimeCount, 1000) {
            public void onTick(long millisUntilFinished) {
                if (active) {
                    calculateTimeLeft();
                } else
                    this.cancel();
            }
            public void onFinish() {
                if (active) {
                    calculateTimeLeft();
                } else
                    this.cancel();
            }
        }.start();
    }

    @Override
    public void onStartProgressBar() {
        SupportUtil.showNoDataProgress(llNoData);
    }

    @Override
    public void onStopProgressBar() {

    }

    private MenuItem menuRefresh;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        menuRefresh = menu.findItem(R.id.action_refresh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_refresh) {
            fetchDataFromServer();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
