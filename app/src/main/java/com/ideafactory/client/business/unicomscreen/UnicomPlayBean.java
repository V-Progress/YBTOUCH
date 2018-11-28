package com.ideafactory.client.business.unicomscreen;

/**
 * Created by Administrator on 2018/1/2.
 */

public class UnicomPlayBean {
    private long startLong ;//开始时间毫秒数
    private long endLong ;//结束时间毫秒数
    private long offTimeLong ;//结束时间毫秒数

    public UnicomPlayBean( long startLong, long endLong, long offTimeLong) {
        this.startLong = startLong;
        this.endLong = endLong;
        this.offTimeLong = offTimeLong;
    }


    public long getStartLong() {
        return startLong;
    }

    public void setStartLong(long startLong) {
        this.startLong = startLong;
    }

    public long getEndLong() {
        return endLong;
    }

    public void setEndLong(long endLong) {
        this.endLong = endLong;
    }

    public long getOffTimeLong() {
        return offTimeLong;
    }

    public void setOffTimeLong(long offTimeLong) {
        this.offTimeLong = offTimeLong;
    }
}
