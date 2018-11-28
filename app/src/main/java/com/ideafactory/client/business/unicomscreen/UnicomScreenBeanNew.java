package com.ideafactory.client.business.unicomscreen;

import java.util.List;

/**
 * Created by Administrator on 2017/12/5.
 */
public class UnicomScreenBeanNew {


    /**
     * col : 1
     * num : YB000268
     * resource : [{"name":"222","type":"2","playtime":"173","url":"http://192.168.1.101:8080/imgserver//resource/2017/12/19/a708c73b-e205-42dc-b1b0-dfbdd09ee18d.mp4"},{"name":"bg_big","type":"1","playtime":"10","url":"http://192.168.1.101:8080/imgserver//resource/2017/12/22/d9ac83ff-ea7c-44dd-814d-16bac4ad2db3.png"}]
     * serverIp : 192.168.31.201
     * isServer : 1
     * unionScreen : {"col":"4","device":[{"col":"2","num":"YB000272","row":"1","ip":"192.168.31.154"},{"col":"3","num":"YB000271","row":"1","ip":"192.168.31.229"},{"col":"4","num":"YB000270","row":"1","ip":"192.168.31.102"}],"row":"1"}
     * row : 1
     */
    private String col;
    private String num;
    private List<ResourceEntity> resource;
    private String serverIp;
    private String isServer;
    private UnionScreenEntity unionScreen;
    private String row;

    public void setCol(String col) {
        this.col = col;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setResource(List<ResourceEntity> resource) {
        this.resource = resource;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setIsServer(String isServer) {
        this.isServer = isServer;
    }

    public void setUnionScreen(UnionScreenEntity unionScreen) {
        this.unionScreen = unionScreen;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getCol() {
        return col;
    }

    public String getNum() {
        return num;
    }

    public List<ResourceEntity> getResource() {
        return resource;
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getIsServer() {
        return isServer;
    }

    public UnionScreenEntity getUnionScreen() {
        return unionScreen;
    }

    public String getRow() {
        return row;
    }

    public class ResourceEntity {
        /**
         * name : 222
         * type : 2
         * playtime : 173
         * url : http://192.168.1.101:8080/imgserver//resource/2017/12/19/a708c73b-e205-42dc-b1b0-dfbdd09ee18d.mp4
         */
        private String name;
        private String type;
        private String playtime;
        private String url;

        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setPlaytime(String playtime) {
            this.playtime = playtime;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getPlaytime() {
            return playtime;
        }

        public String getUrl() {
            return url;
        }
    }

    public class UnionScreenEntity {
        /**
         * col : 4
         * device : [{"col":"2","num":"YB000272","row":"1","ip":"192.168.31.154"},{"col":"3","num":"YB000271","row":"1","ip":"192.168.31.229"},{"col":"4","num":"YB000270","row":"1","ip":"192.168.31.102"}]
         * row : 1
         */
        private String col;
        private List<DeviceEntity> device;
        private String row;

        public void setCol(String col) {
            this.col = col;
        }

        public void setDevice(List<DeviceEntity> device) {
            this.device = device;
        }

        public void setRow(String row) {
            this.row = row;
        }

        public String getCol() {
            return col;
        }

        public List<DeviceEntity> getDevice() {
            return device;
        }

        public String getRow() {
            return row;
        }

        public class DeviceEntity {
            /**
             * col : 2
             * num : YB000272
             * row : 1
             * ip : 192.168.31.154
             */
            private String col;
            private String num;
            private String row;
            private String ip;

            public void setCol(String col) {
                this.col = col;
            }

            public void setNum(String num) {
                this.num = num;
            }

            public void setRow(String row) {
                this.row = row;
            }

            public void setIp(String ip) {
                this.ip = ip;
            }

            public String getCol() {
                return col;
            }

            public String getNum() {
                return num;
            }

            public String getRow() {
                return row;
            }

            public String getIp() {
                return ip;
            }
        }
    }
}
