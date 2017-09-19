package com.example.summer.lilinhaicoordinator.widget;

/**
 * AndroidTech
 * Created by lilinhai on 2017/7/11.
 */

public class ATMathUtils {

    public static int constrain(int amount, int low, int high) {
        int ret = amount < low ? low : amount;
        ret = ret > high ? high : ret;
        return ret;
    }

    public static float constrain(float amount, float low, float high) {
        float ret = amount < low ? low : amount;
        ret = ret > high ? high : ret;
        return ret;
    }
}
