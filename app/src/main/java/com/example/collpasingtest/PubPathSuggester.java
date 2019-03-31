package com.example.collpasingtest;

import android.content.Context;

import com.example.collpasingtest.interfaces.PathRequestCallback;
import com.example.collpasingtest.models.PassStop;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathInfo;
import com.example.collpasingtest.views.RouteAddActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PubPathSuggester {

    private static final String odsayKey = "zX1V+oNUTWiXMA+Xk0/J5uzNsC7ZgDFVhtLwLIDgVhI";
    private Context context;
    private ODsayService service;

    private int searchType = 0;

    public PubPathSuggester(Context context) {
        this.context = context;
        this.service = ODsayService.init(context, odsayKey);
        this.service.setReadTimeout(1000 * 5);
        this.service.setConnectionTimeout(1000 * 5);

    }

    public void setSearchType(int type) {
        this.searchType = type;
    }

    // 최단거리
    private Path getShortestPath(ArrayList<Path> pathList) {
        return pathList.size() > 0 ? pathList.get(0) : null;
    }

    //
    private Path getLowestPaymentPath(ArrayList<Path> pathList) {

        // 돈 확인
        int lowestPaymentIdx = -1;
        int lowestPayment = 987654321;
        for(int i = 0; i < pathList.size(); ++i)
            if(pathList.get(i).getPayment() < lowestPayment)
                lowestPaymentIdx = i;

        return lowestPaymentIdx == -1 ? null : pathList.get(lowestPaymentIdx);
    }

    private Path getShortestWalkPath(ArrayList<Path> pathList) {

        int shortestWalkDistIdx = -1;
        int shortestWalkDist = 987654321;

        for(int i = 0; i < pathList.size(); ++i)
            if(pathList.get(i).getTotalWalk() < shortestWalkDist)
                shortestWalkDist = i;

        return shortestWalkDistIdx == -1 ? null : pathList.get(shortestWalkDist);
    }

    public Path getPath(double x1, double y1, double x2, double y2) {

        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        ArrayList<Path> pathList = new ArrayList<>();
        String data = "";

        try {
            URL url = new URL("https://api.odsay.com/v1/api/searchPubTransPath?lang=0" +
                    "&SX=" + URLEncoder.encode(String.valueOf(x1), "UTF-8") +
                    "&SY=" + URLEncoder.encode(String.valueOf(y1), "UTF-8") +
                    "&EX=" + URLEncoder.encode(String.valueOf(x2), "UTF-8") +
                    "&EY=" + URLEncoder.encode(String.valueOf(y2), "UTF-8") +
                    "&apiKey=" + URLEncoder.encode(odsayKey, "UTF-8"));


            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.connect();

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            while ((data = reader.readLine()) != null) {
                sb.append(data);
            }

            data = sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return parseJson(data);
    }

    private Path parseJson(String json) {

        if(json.equals("")) return null;

        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        ArrayList<Path> pathList = new ArrayList<>();

        try {

            JsonParser parser = new JsonParser();
            JsonElement top = parser.parse(json);

            JsonArray arr1 = ((JsonObject) top).get("result").getAsJsonObject().get("path").getAsJsonArray();

            for (int i = 0; i < arr1.size(); ++i) {

                // 저장할 Path
                Path path = new Path();

                JsonObject pathInfo = ((JsonObject) arr1.get(i)).getAsJsonObject("info");
                JsonArray subPathInfoList = ((JsonObject) arr1.get(i)).getAsJsonArray("subPath");

                // PathType 설정
                path.setPathType(((JsonObject) arr1.get(i)).get("pathType").getAsInt());
                path.setPayment(pathInfo.get("payment").getAsInt());
                path.setBusTransitCount(pathInfo.get("busTransitCount").getAsInt());
                path.setBusStationCount(pathInfo.get("busStationCount").getAsInt());
                path.setSubTransitCount(pathInfo.get("subwayTransitCount").getAsInt());
                path.setSubStationCount(pathInfo.get("subwayStationCount").getAsInt());
                path.setTotalTime(pathInfo.get("totalTime").getAsInt());
                path.setFirstStationName(pathInfo.get("firstStartStation").getAsString());
                path.setEndStationName(pathInfo.get("lastEndStation").getAsString());
                path.setTotalWalk(pathInfo.get("totalWalk").getAsInt());
                path.setTotalDistance(pathInfo.get("totalDistance").getAsInt());

                int subPathLength = subPathInfoList.size();

                // PathInfo 와 subPathList

                ArrayList<PathInfo> pathInfoList = new ArrayList<>();

                for (int j = 0; j < subPathLength; ++j) {

                    JsonObject subPathInfo = (JsonObject) subPathInfoList.get(j);
                    ArrayList<PassStop> passStopList = new ArrayList<>();
                    PathInfo pInfo = new PathInfo();

                    int subPathType = subPathInfo.get("trafficType").getAsInt();

                    // 기본 정보 설정
                    pInfo.setTrafficType(subPathType);
                    pInfo.setDistance(subPathInfo.get("distance").getAsInt());
                    pInfo.setSectionTime(subPathInfo.get("sectionTime").getAsInt());

                    if(subPathType == 1) {
                        pInfo.setVehicleName(((JsonObject)subPathInfo.getAsJsonArray("lane").get(0)).get("name").getAsString());
                    } else if(subPathType == 2) {
                        pInfo.setVehicleName(((JsonObject)subPathInfo.getAsJsonArray("lane").get(0)).get("busNo").getAsString());
                    } else {
                        pathInfoList.add(pInfo);
                        continue;
                    }

                    JsonArray passStopListObj = subPathInfo.get("passStopList").getAsJsonObject().getAsJsonArray("stations");

                    // pass stop 정보 가져오기
                    for (int k = 0; k < passStopListObj.size(); ++k) {
                        JsonObject passStopObj = passStopListObj.get(k).getAsJsonObject();
                        PassStop passStop = new PassStop(
                                passStopObj.get("index").getAsInt(),
                                passStopObj.get("stationID").getAsInt(),
                                passStopObj.get("x").getAsDouble(),
                                passStopObj.get("y").getAsDouble(),
                                passStopObj.get("stationName").getAsString()
                        );
                        passStopList.add(passStop);
                    }
                    pInfo.setPassStopList(passStopList);
                    pathInfoList.add(pInfo);
                }
                path.setPathInfoList(pathInfoList);
                pathList.add(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 최단거리
        if(searchType == 1)
            return getShortestPath(pathList);
        // 최쇠비용
        else if(searchType == 2)
            return getLowestPaymentPath(pathList);
        // 최단도보
        else
            return getShortestWalkPath(pathList);
    }

}
