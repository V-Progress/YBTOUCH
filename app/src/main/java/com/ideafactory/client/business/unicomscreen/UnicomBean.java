package com.ideafactory.client.business.unicomscreen;

import java.util.List;

/**
 * Created by Administrator on 2017/12/27.
 */

public class UnicomBean {

    /**
     * col : 1
     * isServer : 1
     * resource : [{"name":"资源名称","playtime":"20","type":"1","url":"http://imgs.yunbiaowulian.com/imgserver/resource/2017/06/08/dbe48a54-f92f-4674-84c2-b57c2c86f9ee.mp4"}]
     * start : 4
     * serverIp : 192.168.31.170
     * row : 1
     * time : {"startslot":"9","endslot":"18","offtime":"15","type":"2"}
     * unionScreen : {"col":"2","row":"2","device":[{"col":"2","ip":"192.168.1.2","num":"Yb0001","row":"1"}]}
     * local : 192.168.31.170
     */
    private String col;
    private String isServer;
    private List<ResourceEntity> resource;
    private String start;
    private String serverIp;
    private String row;
    private TimeEntity time;
    private UnionScreenEntity unionScreen;
    private String local;

    public void setCol(String col) {
        this.col = col;
    }

    public void setIsServer(String isServer) {
        this.isServer = isServer;
    }

    public void setResource(List<ResourceEntity> resource) {
        this.resource = resource;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public void setTime(TimeEntity time) {
        this.time = time;
    }

    public void setUnionScreen(UnionScreenEntity unionScreen) {
        this.unionScreen = unionScreen;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getCol() {
        return col;
    }

    public String getIsServer() {
        return isServer;
    }

    public List<ResourceEntity> getResource() {
        return resource;
    }

    public String getStart() {
        return start;
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getRow() {
        return row;
    }

    public TimeEntity getTime() {
        return time;
    }

    public UnionScreenEntity getUnionScreen() {
        return unionScreen;
    }

    public String getLocal() {
        return local;
    }

    public class ResourceEntity {
        /**
         * name : 资源名称
         * playtime : 20
         * type : 1
         * url : http://imgs.yunbiaowulian.com/imgserver/resource/2017/06/08/dbe48a54-f92f-4674-84c2-b57c2c86f9ee.mp4
         */
        private String name;
        private String playtime;
        private String type;
        private String url;

        public void setName(String name) {
            this.name = name;
        }

        public void setPlaytime(String playtime) {
            this.playtime = playtime;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getPlaytime() {
            return playtime;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }
    }

    public class TimeEntity {
        /**
         * startslot : 9
         * endslot : 18
         * offtime : 15
         * type : 2
         */
        private String startslot;
        private String endslot;
        private String offtime;
        private String type;

        public void setStartslot(String startslot) {
            this.startslot = startslot;
        }

        public void setEndslot(String endslot) {
            this.endslot = endslot;
        }

        public void setOfftime(String offtime) {
            this.offtime = offtime;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStartslot() {
            return startslot;
        }

        public String getEndslot() {
            return endslot;
        }

        public String getOfftime() {
            return offtime;
        }

        public String getType() {
            return type;
        }
    }

    public class UnionScreenEntity {
        /**
         * col : 2
         * row : 2
         * device : [{"col":"2","ip":"192.168.1.2","num":"Yb0001","row":"1"}]
         */
        private String col;
        private String row;
        private List<DeviceEntity> device;

        public void setCol(String col) {
            this.col = col;
        }

        public void setRow(String row) {
            this.row = row;
        }

        public void setDevice(List<DeviceEntity> device) {
            this.device = device;
        }

        public String getCol() {
            return col;
        }

        public String getRow() {
            return row;
        }

        public List<DeviceEntity> getDevice() {
            return device;
        }

        public class DeviceEntity {
            /**
             * col : 2
             * ip : 192.168.1.2
             * num : Yb0001
             * row : 1
             */
            private String col;
            private String ip;
            private String num;
            private String row;

            public void setCol(String col) {
                this.col = col;
            }

            public void setIp(String ip) {
                this.ip = ip;
            }

            public void setNum(String num) {
                this.num = num;
            }

            public void setRow(String row) {
                this.row = row;
            }

            public String getCol() {
                return col;
            }

            public String getIp() {
                return ip;
            }

            public String getNum() {
                return num;
            }

            public String getRow() {
                return row;
            }
        }
    }
}
