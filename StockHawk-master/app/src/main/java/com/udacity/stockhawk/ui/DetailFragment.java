package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.util.Utility;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.udacity.stockhawk.R.id.chart;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";
    private Uri mUri;
    private boolean mTransitionAnimation;

    private static final int DETAIL_LOADER = 0;

    private TextView mSymbolView;
    private TextView mBidPriceView;
    private TextView mChangeView;
    private TextView mChangePercentView;
    private TextView mTimeStamp;
    private View mDaysLowView;
    private View mDaysHighView;
    private View mYearLowView;
    private View mYearHighView;
    private View mOpenView;
    private View mCloseView;
    private View mVolumeView;
    private View mAvgVolumeView;
    private LineChart mLineChart;

    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;
    private DecimalFormat percentageFormat;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dollarFormat = Utility.getDollarFormat();
        dollarFormatWithPlus = Utility.getDollarFormatWithPlus();
        percentageFormat = Utility.getPercentageFormat();

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mSymbolView = (TextView) rootView.findViewById(R.id.detail_stock_symbol);
        mBidPriceView = (TextView) rootView.findViewById(R.id.detail_stock_bid_price);
        mChangeView = (TextView) rootView.findViewById(R.id.detail_stock_change);
        mChangePercentView = (TextView) rootView.findViewById(R.id.detail_stock_change_percent);
        mTimeStamp = (TextView) rootView.findViewById(R.id.detail_stock_timestamp);
        mDaysLowView = rootView.findViewById(R.id.detail_stock_days_low);
        mDaysHighView = rootView.findViewById(R.id.detail_stock_days_high);
        mYearLowView = rootView.findViewById(R.id.detail_stock_year_low);
        mYearHighView = rootView.findViewById(R.id.detail_stock_year_high);
        mOpenView = rootView.findViewById(R.id.detail_stock_open);
        mCloseView = rootView.findViewById(R.id.detail_stock_close);
        mVolumeView = rootView.findViewById(R.id.detail_stock_vol);
        mAvgVolumeView = rootView.findViewById(R.id.detail_stock_avg_vol);

        mLineChart = (LineChart) rootView.findViewById(chart);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(getActivity(),
                    mUri,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    null
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            setTextViews(data);
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);
//
//        // We need to start the enter transition after the data has loaded
//        if ( mTransitionAnimation ) {
//            activity.supportStartPostponedEnterTransition();
//
//            if ( null != toolbarView ) {
//                activity.setSupportActionBar(toolbarView);
//
//                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
//                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            }
//        } else {
//            if ( null != toolbarView ) {
//                Menu menu = toolbarView.getMenu();
//                if ( null != menu ) menu.clear();
//            }
//        }
    }

    private void setTextViews(Cursor data) {
        String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
        mSymbolView.setText(stockSymbol);
        mBidPriceView.setText(dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        mChangeView.setText(change);
        mChangePercentView.setText("("+percentage+")");

        int color;
        if (rawAbsoluteChange > 0) {
            color = ResourcesCompat.getColor(getResources(), R.color.material_green_A700, null);
        } else {
            color = ResourcesCompat.getColor(getResources(), R.color.material_red_700, null);
        }
        mChangeView.setTextColor(color);
        mChangePercentView.setTextColor(color);

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a z  -  MMM d, yyyy", Locale.getDefault());
        Date quoteDate = new Date(data.getLong(Contract.Quote.POSITION_CREATED));

        mTimeStamp.setText( sdf.format(quoteDate).toString());

        setLabelDetailText(mDaysLowView,
                getActivity().getString(R.string.detail_stock_days_low_label),
                dollarFormat.format(data.getFloat(Contract.Quote.POSITION_DAYS_LOW)));
        //mDaysLowView.setText(data.getString(COL_STOCK_DAY_LOW));

        //mDaysHighView.setText(data.getString(COL_STOCK_DAY_HIGH));
        setLabelDetailText(mDaysHighView,
                getActivity().getString(R.string.detail_stock_days_high_label),
                dollarFormat.format(data.getFloat(Contract.Quote.POSITION_DAYS_HIGH)));

        //mYearLowView.setText(data.getString(COL_STOCK_YEAR_LOW));
        setLabelDetailText(mYearLowView,
                getActivity().getString(R.string.detail_stock_year_low_label),
                dollarFormat.format(data.getFloat(Contract.Quote.POSITION_YEAR_LOW)));
        //mYearHighView.setText(data.getString(COL_STOCK_YEAR_HIGH));
        setLabelDetailText(mYearHighView,
                getActivity().getString(R.string.detail_stock_year_high_label),
                dollarFormat.format(data.getFloat(Contract.Quote.POSITION_YEAR_HIGH)));
        //mOpenView.setText(data.getString(COL_STOCK_OPEN));
        setLabelDetailText(mOpenView,
                getActivity().getString(R.string.detail_stock_open_label),
                dollarFormat.format(data.getFloat(Contract.Quote.POSITION_OPEN)));
        //mCloseView.setText(data.getString(COL_STOCK_CLOSE));
        setLabelDetailText(mCloseView,
                getActivity().getString(R.string.detail_stock_close_label),
                dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PREVIOUS_CLOSE)));
        //mVolumeView.setText(data.getString(COL_STOCK_VOL));
        setLabelDetailText(mVolumeView,
                getActivity().getString(R.string.detail_stock_volume_label),
                data.getString(Contract.Quote.POSITION_VOLUME));
        //mAvgVolumeView.setText(data.getString(COL_STOCK_AVG_VOL));
        setLabelDetailText(mAvgVolumeView,
                getActivity().getString(R.string.detail_stock_avg_volume_label),
                data.getString(Contract.Quote.POSITION_AVG_DAILY_VOLUME));

        List<String> items = Arrays.asList(data.getString(Contract.Quote.POSITION_HISTORY).split("\\r?\\n"));
        displayChart(items, stockSymbol);
    }

    private void displayChart(List<String> items, String stockSymbol) {
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setTextSize(11f);

        final ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> quoteVals = new ArrayList<>();

        Collections.reverse(items);

        for (int i = 0; i < items.size(); i++) {
            String[] stockData = items.get(i).split(",");
            DateFormat df = new SimpleDateFormat("MMM ''yy");
            String formattedDate = df.format(Long.valueOf(stockData[0]));
            xVals.add(i, formattedDate);
            quoteVals.add(new Entry(i, Float.valueOf(stockData[1])));
        }

        IAxisValueFormatter xAxisFormatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xVals.get((int) value);
            }
        };

        xAxis.setValueFormatter(xAxisFormatter);

        LineDataSet dataSet = new LineDataSet(quoteVals, stockSymbol);
        LineData lineData = new LineData(dataSet);
        Description lineDesc = new Description();
        lineDesc.setText(getActivity().getString(R.string.detail_chart_detail));
        mLineChart.setData(lineData);
        mLineChart.setContentDescription(getActivity().getString(R.string.detail_chart_detail));
        mLineChart.setDescription(lineDesc);
        mLineChart.getLegend().setTextSize(12f);
        mLineChart.setPinchZoom(false);
        mLineChart.invalidate();
    }

    private void setLabelDetailText(View view, String labelTxt, String dataTxt) {
        ((TextView) view.findViewById(R.id.detail_text_label)).setText(labelTxt);
        ((TextView) view.findViewById(R.id.detail_text_data)).setText(dataTxt);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}