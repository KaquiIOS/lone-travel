package com.example.collpasingtest;

import android.content.Context;
import android.os.AsyncTask;

import com.example.collpasingtest.adapters.POISearchAdapter;
import com.example.collpasingtest.models.POI;
import com.example.collpasingtest.models.SearchInfo;
import com.example.collpasingtest.models.TMapPOISearchInfo;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

// 결과 파싱해주기
public class POIResultParser extends AsyncTask<String, Void, ArrayList<SearchInfo>> {

    private static final String UTF8 = "UTF-8";

    private ArrayList<SearchInfo> mSearchList;
    private POISearchAdapter mPOIAdapter;
    private Context context;

    public POIResultParser(POISearchAdapter adapter, Context context) {
        this.mPOIAdapter = adapter;
        this.mSearchList = new ArrayList<>();
        this.context = context;
    }

    @Override
    protected void onPostExecute(ArrayList<SearchInfo> searchInfos) {
        super.onPostExecute(searchInfos);
        mPOIAdapter.setSearchList(searchInfos);
        mPOIAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected ArrayList<SearchInfo> doInBackground(String... strings) {
        return getParsedSearchInfo(strings[0]);
    }

    // input 검색 결과 리턴
    public ArrayList<SearchInfo> getParsedSearchInfo(String input) {
        String encodedInput = "";

        try {
            encodedInput = URLEncoder.encode(input, UTF8);

            URL url = new URL(
                    "https://api2.sktelecom.com/tmap/pois?version=1&"
                    + "searchKeyword=" + encodedInput + "&"
                    + "count=" + 20 + "&"
                    + "resCoordType=" + "WGS84GEO"
            );

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("appKey", context.getString(R.string.app_api_key));

            // get stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            mSearchList.clear(); // 이전 정보 삭제

            while(line == null || line.isEmpty()) {
                return mSearchList;
            }

            // 받아온 정보 변경
            TMapPOISearchInfo parsedInfo = new Gson().fromJson(line, TMapPOISearchInfo.class);

            ArrayList<POI> poi = parsedInfo.getSearchPoiInfo().getPois().getPois();

            if(poi == null) return mSearchList;

            for(POI temp : poi) {
                String addr = temp.getUpperAddrName() + " " + temp.getMiddleAddrName() + " " + temp.getLowerAddrName() + " "
                        + temp.getDetailAddrName();
                // add search info
                mSearchList.add(new SearchInfo(temp.getName(), addr,
                        Double.valueOf(temp.getNoorLat()), Double.valueOf(temp.getNoorLon()),
                        temp.getTelNo(), temp.getDetailBizName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mSearchList;
    }
}
