package com.ideafactory.client.business.unicomscreen;

import java.util.List;

/**
 * Created by Administrator on 2018/1/4.
 */

public class UnicomBean3 {


    /**
     * col : 2
     * isServer : 2
     * resource : [{"name":"01-2","playtime":"15","type":"2","url":"http://img.yunbiaowulian.com/imgserver//resource/2018/01/02/10b86493-d176-4a85-8645-cf708e4c6a10.mp4"}]
     * num : YB018499
     * start : 2
     * serverIp : 192.168.31.170
     * row : 1
     * time : {"startslot":"","endslot":"","offtime":""}
     * unionScreen : {"rowall":"","device":[{"col":"","ip":"","num":"","row":""}],"colall":""}
     */
    private int col;
    private String isServer;
    private List<ResourceEntity> resource;
    private String num;
    private String start;
    private String serverIp;
    private int row;
    private TimeEntity time;
    private UnionScreenEntity unionScreen;

    public void setCol(int col) {
        this.col = col;
    }

    public void setIsServer(String isServer) {
        this.isServer = isServer;
    }

    public void setResource(List<ResourceEntity> resource) {
        this.resource = resource;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setTime(TimeEntity time) {
        this.time = time;
    }

    public void setUnionScreen(UnionScreenEntity unionScreen) {
        this.unionScreen = unionScreen;
    }

    public int getCol() {
        return col;
    }

    public String getIsServer() {
        return isServer;
    }

    public List<ResourceEntity> getResource() {
        return resource;
    }

    public String getNum() {
        return num;
    }

    public String getStart() {
        return start;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getRow() {
        return row;
    }

    public TimeEntity getTime() {
        return time;
    }

    public UnionScreenEntity getUnionScreen() {
        return unionScreen;
    }

    public class ResourceEntity {
        /**
         * name : 01-2
         * playtime : 15
         * type : 2
         * url : http://img.yunbiaowulian.com/imgserver//resource/2018/01/02/10b86493-d176-4a85-8645-cf708e4c6a10.mp4
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
         * startslot :
         * endslot :
         * offtime :
         */
        private String startslot;
        private String endslot;
        private String offtime;

        public void setStartslot(String startslot) {
            this.startslot = startslot;
        }

        public void setEndslot(String endslot) {
            this.endslot = endslot;
        }

        public void setOfftime(String offtime) {
            this.offtime = offtime;
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
    }

    public class UnionScreenEntity {
        /**
         * rowall :
         * device : [{"col":"","ip":"","num":"","row":""}]
         * colall :
         */
        private String rowall;
        private List<DeviceEntity> device;
        private String colall;

        public void setRowall(String rowall) {
            this.rowall = rowall;
        }

        public void setDevice(List<DeviceEntity> device) {
            this.device = device;
        }

        public void setColall(String colall) {
            this.colall = colall;
        }

        public String getRowall() {
            return rowall;
        }

        public List<DeviceEntity> getDevice() {
            return device;
        }

        public String getColall() {
            return colall;
        }

        public class DeviceEntity {
            /**
             * col :
             * ip :
             * num :
             * row :
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
