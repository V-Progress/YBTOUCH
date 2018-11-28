package com.ideafactory.client.common.net;

public class DownloadCounter {

    private Integer allCount;
    private Integer counter;

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    private Integer failCount = 0;//失败的数量

    public String getCountStr() {
        return this.counter + "/" + this.allCount;
    }

    public boolean isEquals() {
        return allCount.equals(counter);
    }

    public void delete() {
        counter--;
    }

    public Integer getAllCount() {
        return allCount;
    }

    public void setAllCount(Integer allCount) {
        this.allCount = allCount;
        this.counter = 0;
    }

    public Integer getCounter() {
        return counter;
    }

    public void add() {
        counter++;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public void cler() {
        counter = 0;
        this.allCount = 0;
    }

    public boolean isEmpty() {
        return counter == 0;
    }

}
