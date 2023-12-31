package com.nowfloats.util;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by guru on 29/04/15.
 */
public class BusProvider {
    private static BusProvider instance;
    private Bus bus;

    private BusProvider() {
        bus = new Bus(ThreadEnforcer.ANY);
    }

    public static BusProvider getInstance() {
        if (instance == null)
            instance = new BusProvider();
        return instance;
    }

    public Bus getBus() {
        return bus;
    }
}
