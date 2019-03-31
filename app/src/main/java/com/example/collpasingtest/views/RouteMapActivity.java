package com.example.collpasingtest.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.collpasingtest.R;
import com.example.collpasingtest.models.PassStop;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathInfo;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.SearchInfo;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

public class RouteMapActivity extends AppCompatActivity {

    private TMapView mMapView;
    private Bitmap bitmap;
    private TMapPolyLine tMapPolyLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
        tMapPolyLine = new TMapPolyLine();
        mMapView = findViewById(R.id.tmap_route_show);
        // set app key
        mMapView.setSKTMapApiKey(getString(R.string.app_api_key));

        RouteInfo passedInfo = (RouteInfo) getIntent().getSerializableExtra("info");

        ArrayList<PathType> info = passedInfo.getRouteList();

        mMapView.setCenterPoint(((SearchInfo) info.get(0)).getLongitude(), ((SearchInfo) info.get(0)).getLatitude());

        new DrawPathAsyncTask(info).execute();
    }

    private void addPolyLineToMap(double latitude1, double longitude1, double latitude2, double longitude2, int color) {
        TMapPolyLine line = new TMapPolyLine();
        line.addLinePoint(new TMapPoint(latitude1, longitude1));
        line.addLinePoint(new TMapPoint(latitude2, longitude2));
        line.setLineColor(color);
        line.setOutLineColor(color);
        line.setLineWidth(3);

        mMapView.addTMapPolyLine(Double.toString(latitude1 + longitude1), line);
    }

    private void addMarker(Bitmap marker, double latitude, double longitude, String title) {
        TMapMarkerItem item = new TMapMarkerItem();
        item.setTMapPoint(new TMapPoint(latitude, longitude));
        item.setIcon(marker);
        item.setCanShowCallout(true);
        item.setCalloutTitle(title);
        item.setAutoCalloutVisible(true);

        mMapView.addMarkerItem(Double.toString(latitude + longitude), item);
    }

    private class DrawPathAsyncTask extends AsyncTask<Void, Void, Void> {

        private ArrayList<PathType> info;
        private TMapPolyLine line;
        private Bitmap pedIcon, busIcon, subIcon, startIcon, endIcon, termIcon;

        public DrawPathAsyncTask(ArrayList<PathType> pathList) {
            this.info = pathList;
            this.pedIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pedestrian);
            this.busIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bus_24);
            this.subIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_subway_24);
            this.startIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_start_24);
            this.endIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_end_24);
            this.termIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_term_24);
        }

        @Override
        protected Void doInBackground(Void... voids) {


            int pathSize = info.size();

            if (pathSize == 0) return null;

            for (int i = 1; i < pathSize; ++i) {

                try {

                    // 지역 - 경로 인 경우는 구성
                    if (info.get(i) instanceof Path && info.get(i - 1) instanceof SearchInfo && info.get(i + 1) instanceof SearchInfo) {

                        TMapMarkerItem markerItem = new TMapMarkerItem();

                        SearchInfo startPoint = (SearchInfo) info.get(i - 1); // 시작 위치
                        SearchInfo endPoint = (SearchInfo) info.get(i + 1); // 종료 위치
                        Path path = (Path) info.get(i); // 두 지점 사이의 경로

                        ArrayList<PathInfo> pathList = path.getPathInfoList();
                        ArrayList<PassStop> stopList;
                        int pathCount = pathList.size();

                        for (int j = 0; j < pathCount; ++j) {

                            int trafficType = pathList.get(j).getTrafficType();

                            // 이번 경로가 걷는 경로인 경우
                            if (trafficType == 3) {
                                // 시작 정류장으로 이동
                                if (j == 0) {
                                    PassStop nextStop = pathList.get(j + 1).getPassStopList().get(0);
                                    line = new TMapData().findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,
                                            new TMapPoint(startPoint.getLatitude(), startPoint.getLongitude()),
                                            new TMapPoint(nextStop.getY(), nextStop.getX()));
                                    addMarker(startIcon, startPoint.getLatitude(), startPoint.getLongitude(), startPoint.getTitle());
                                }
                                // 마지막 지점으로 감
                                else if (j == pathCount - 1) {
                                    PassStop preStop = pathList.get(j - 1).getPassStopList().get(pathList.get(j - 1).getPassStopList().size() - 1);
                                    line = new TMapData().findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,
                                            new TMapPoint(endPoint.getLatitude(), endPoint.getLongitude()),
                                            new TMapPoint(preStop.getY(), preStop.getX()));
                                    addMarker(endIcon, endPoint.getLatitude(), endPoint.getLongitude(), endPoint.getTitle());
                                }
                                // 중간지점
                                else {
                                    PassStop preStop = pathList.get(j - 1).getPassStopList().get(pathList.get(j - 1).getPassStopList().size() - 1);
                                    PassStop nextStop = pathList.get(j + 1).getPassStopList().get(0);
                                    line = new TMapData().findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,
                                            new TMapPoint(preStop.getY(), preStop.getX()),
                                            new TMapPoint(nextStop.getY(), nextStop.getX()));
                                    addMarker(pedIcon, preStop.getY(), preStop.getX(), preStop.getStationName());
                                    addMarker(pedIcon, nextStop.getY(), nextStop.getX(), preStop.getStationName());
                                }

                                if (line != null) {
                                    line.setLineColor(Color.RED);
                                    line.setOutLineColor(Color.RED);
                                    line.setPathEffect(new DashPathEffect(new float[] {40.f, 10.f}, 20.f));
                                    line.setOutLinePathEffect(new DashPathEffect(new float[] {40.f, 10.f}, 20.f));
                                    mMapView.addTMapPolyLine("ped_l" + j + "" + i, line);
                                }
                            } else {
                                stopList = pathList.get(j).getPassStopList();

                                int len = stopList.size() - 1;
                                for(int k = 0; k < len; ++k) {
                                    PassStop preStop = stopList.get(k),
                                            nextStop = stopList.get(k + 1);

                                    int color = trafficType == 2 ? Color.GREEN : Color.BLUE;

                                    if(trafficType == 2) {
                                        addMarker(busIcon, preStop.getY(), preStop.getX(), preStop.getStationName());
                                        addMarker(busIcon, nextStop.getY(), nextStop.getX(), nextStop.getStationName());
                                        addPolyLineToMap(preStop.getY(), preStop.getX(),
                                                nextStop.getY(), nextStop.getX(), Color.GREEN);
                                    } else {
                                        addMarker(subIcon, preStop.getY(), preStop.getX(), preStop.getStationName());
                                        addMarker(subIcon, nextStop.getY(), nextStop.getX(), nextStop.getStationName());
                                        addPolyLineToMap(preStop.getY(), preStop.getX(),
                                                nextStop.getY(), nextStop.getX(), Color.BLUE);
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
