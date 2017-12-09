package com.vannakittikun.parkingmemo;

/**
 * Created by Rule on 12/8/2017.
 */

public interface GetGeoLocationResponse {
    void onTaskDone(String responseData);

    void onError();
}