package com.ideafactory.client.business.touchQuery.bean;


import java.util.List;

/**
 * Created by Administrator on 2016/8/18 0018.
 */
public class TouchQueryDetail {

    /**
     * background : http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/dc8d8be5-7318-4201-9771-4cb3660b41b2.jpg
     * content : [{"content":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/1b2c44f3-b697-4e37-b045-1e92d01963bd_s.jpg","imagedetail":{"isPlay":false,"playTime":5000},"textdetail":{"background":"#000000","fontStyle":"黑体","isscroll":false,"postion":"left","size":"25.2707996368408","textColor":"#ffffff"},"postion":{"h":"10.26%","w":"20.53%","x":"3.38%","y":"3.37%"},"type":1},{"content":"云标物联智慧型多功能查询系统","postion":{"h":"5.87%","w":"41.71%","x":"28.94%","y":"6.30%"},"textdetail":{"background":"#000000","fontStyle":"黑体","isscroll":false,"postion":"left","size":"25.2707996368408","textColor":"#ffffff"},"type":2}]
     * buttons : [{"btnStyle":{"background":"#008cba","fontStyle":"宋体","icon":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/d435a372-f848-4558-8658-a58ffe94ee40_s.jpg","iconSize":"25.2708","text":"你好","textColor":"#ffffff"},"postion":{"h":"8.94%","w":"19.95%","x":"35.02%","y":"39.16%"},"pages":{"background":"#0072e3","content":[{"content":"云标物联智慧型多功能查询系统","postion":{"h":"5.87%","w":"41.71%","x":"30%","y":"6.30%"},"textdetail":{"background":"#000000","fontStyle":"黑体","isscroll":false,"postion":"left","size":"45.2707996368408","textColor":"#996633"},"imagedetail":{"isPlay":false,"playTime":5000},"type":2},{"content":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/dc8d8be5-7318-4201-9771-4cb3660b41b2.jpg","imagedetail":{"isPlay":false,"playTime":5000},"postion":{"h":"9.38%","w":"18.22%","x":"3.38%","y":"3.37%"},"type":1}],"btn":[{"fontStyle":"默认","icon":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/d435a372-f848-4558-8658-a58ffe94ee40_s.jpg","iconSize":"28","text":"目录名称","textColor":"#ffffff","background":"#008cba","content":{"content":["http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/1c386695-8073-4fed-8a99-86e037fa9cbd.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/d9003318-9910-48cb-aa85-c07cc79c96b1.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/5055f93a-01f4-4fd2-a3ca-1cd176e56f88.jpg"],"imagedetail":{"isPlay":true,"playTime":5000},"textdetail":{"isscroll":"false","background":"#ca8eff","textColor":"#ffffff","postion":"1","size":"70","fontStyle":"默认"},"webdetail":{"isAutoFlus":"true|false","flusTime":"1000"},"type":1}}]}},{"btnStyle":{"background":"#008cba","fontStyle":"宋体","icon":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/d435a372-f848-4558-8658-a58ffe94ee40_s.jpg","iconSize":"30","text":"按钮","textColor":"#ffffff"},"postion":{"h":"8.94%","w":"8.24%","x":"59.01%","y":"55.00%"}}]
     */

    private String background;
    /**
     * content : http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/1b2c44f3-b697-4e37-b045-1e92d01963bd_s.jpg
     * imagedetail : {"isPlay":false,"playTime":5000}
     * textdetail : {"background":"#000000","fontStyle":"黑体","isscroll":false,"postion":"left","size":"25.2707996368408","textColor":"#ffffff"}
     * postion : {"h":"10.26%","w":"20.53%","x":"3.38%","y":"3.37%"}
     * type : 1
     */

    private List<ContentBean> content;
    /**
     * btnStyle : {"background":"#008cba","fontStyle":"宋体","icon":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/d435a372-f848-4558-8658-a58ffe94ee40_s.jpg","iconSize":"25.2708","text":"你好","textColor":"#ffffff"}
     * postion : {"h":"8.94%","w":"19.95%","x":"35.02%","y":"39.16%"}
     * pages : {"background":"#0072e3","content":[{"content":"云标物联智慧型多功能查询系统","postion":{"h":"5.87%","w":"41.71%","x":"30%","y":"6.30%"},"textdetail":{"background":"#000000","fontStyle":"黑体","isscroll":false,"postion":"left","size":"45.2707996368408","textColor":"#996633"},"imagedetail":{"isPlay":false,"playTime":5000},"type":2},{"content":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/dc8d8be5-7318-4201-9771-4cb3660b41b2.jpg","imagedetail":{"isPlay":false,"playTime":5000},"postion":{"h":"9.38%","w":"18.22%","x":"3.38%","y":"3.37%"},"type":1}],"btn":[{"fontStyle":"默认","icon":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/d435a372-f848-4558-8658-a58ffe94ee40_s.jpg","iconSize":"28","text":"目录名称","textColor":"#ffffff","background":"#008cba","content":{"content":["http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/1c386695-8073-4fed-8a99-86e037fa9cbd.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/d9003318-9910-48cb-aa85-c07cc79c96b1.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/5055f93a-01f4-4fd2-a3ca-1cd176e56f88.jpg"],"imagedetail":{"isPlay":true,"playTime":5000},"textdetail":{"isscroll":"false","background":"#ca8eff","textColor":"#ffffff","postion":"1","size":"70","fontStyle":"默认"},"webdetail":{"isAutoFlus":"true|false","flusTime":"1000"},"type":1}}]}
     */

    private List<ButtonsBean> buttons;

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public List<ContentBean> getContent() {
        return content;
    }

    public void setContent(List<ContentBean> content) {
        this.content = content;
    }

    public List<ButtonsBean> getButtons() {
        return buttons;
    }

    public void setButtons(List<ButtonsBean> buttons) {
        this.buttons = buttons;
    }

    public static class ContentBean {
        private String content;
        /**
         * isPlay : false
         * playTime : 5000
         */

        private ImagedetailBean imagedetail;
        /**
         * background : #000000
         * fontStyle : 黑体
         * isscroll : false
         * postion : left
         * size : 25.2707996368408
         * textColor : #ffffff
         */

        private TextdetailBean textdetail;
        /**
         * h : 10.26%
         * w : 20.53%
         * x : 3.38%
         * y : 3.37%
         */

        private PostionBean postion;
        private int type;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public ImagedetailBean getImagedetail() {
            return imagedetail;
        }

        public void setImagedetail(ImagedetailBean imagedetail) {
            this.imagedetail = imagedetail;
        }

        public TextdetailBean getTextdetail() {
            return textdetail;
        }

        public void setTextdetail(TextdetailBean textdetail) {
            this.textdetail = textdetail;
        }

        public PostionBean getPostion() {
            return postion;
        }

        public void setPostion(PostionBean postion) {
            this.postion = postion;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public static class ImagedetailBean {
            private boolean isPlay;
            private int playTime;

            public boolean isIsPlay() {
                return isPlay;
            }

            public void setIsPlay(boolean isPlay) {
                this.isPlay = isPlay;
            }

            public int getPlayTime() {
                return playTime;
            }

            public void setPlayTime(int playTime) {
                this.playTime = playTime;
            }
        }

        public static class TextdetailBean {
            private String background;
            private String fontStyle;
            private boolean isscroll;
            private String postion;
            private String size;
            private String textColor;

            public String getBackground() {
                return background;
            }

            public void setBackground(String background) {
                this.background = background;
            }

            public String getFontStyle() {
                return fontStyle;
            }

            public void setFontStyle(String fontStyle) {
                this.fontStyle = fontStyle;
            }

            public boolean isIsscroll() {
                return isscroll;
            }

            public void setIsscroll(boolean isscroll) {
                this.isscroll = isscroll;
            }

            public String getPostion() {
                return postion;
            }

            public void setPostion(String postion) {
                this.postion = postion;
            }

            public String getSize() {
                return size;
            }

            public void setSize(String size) {
                this.size = size;
            }

            public String getTextColor() {
                return textColor;
            }

            public void setTextColor(String textColor) {
                this.textColor = textColor;
            }
        }

        public static class PostionBean {
            private String h;
            private String w;
            private String x;
            private String y;

            public String getH() {
                return h;
            }

            public void setH(String h) {
                this.h = h;
            }

            public String getW() {
                return w;
            }

            public void setW(String w) {
                this.w = w;
            }

            public String getX() {
                return x;
            }

            public void setX(String x) {
                this.x = x;
            }

            public String getY() {
                return y;
            }

            public void setY(String y) {
                this.y = y;
            }
        }
    }

    public static class ButtonsBean {
        /**
         * background : #008cba
         * fontStyle : 宋体
         * icon : http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/d435a372-f848-4558-8658-a58ffe94ee40_s.jpg
         * iconSize : 25.2708
         * text : 你好
         * textColor : #ffffff
         */

        private BtnStyleBean btnStyle;
        /**
         * h : 8.94%
         * w : 19.95%
         * x : 35.02%
         * y : 39.16%
         */

        private PostionBean postion;
        /**
         * background : #0072e3
         * content : [{"content":"云标物联智慧型多功能查询系统","postion":{"h":"5.87%","w":"41.71%","x":"30%","y":"6.30%"},"textdetail":{"background":"#000000","fontStyle":"黑体","isscroll":false,"postion":"left","size":"45.2707996368408","textColor":"#996633"},"imagedetail":{"isPlay":false,"playTime":5000},"type":2},{"content":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/dc8d8be5-7318-4201-9771-4cb3660b41b2.jpg","imagedetail":{"isPlay":false,"playTime":5000},"postion":{"h":"9.38%","w":"18.22%","x":"3.38%","y":"3.37%"},"type":1}]
         * btn : [{"fontStyle":"默认","icon":"http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/d435a372-f848-4558-8658-a58ffe94ee40_s.jpg","iconSize":"28","text":"目录名称","textColor":"#ffffff","background":"#008cba","content":{"content":["http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/1c386695-8073-4fed-8a99-86e037fa9cbd.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/d9003318-9910-48cb-aa85-c07cc79c96b1.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/5055f93a-01f4-4fd2-a3ca-1cd176e56f88.jpg"],"imagedetail":{"isPlay":true,"playTime":5000},"textdetail":{"isscroll":"false","background":"#ca8eff","textColor":"#ffffff","postion":"1","size":"70","fontStyle":"默认"},"webdetail":{"isAutoFlus":"true|false","flusTime":"1000"},"type":1}}]
         */

        private PagesBean pages;

        public BtnStyleBean getBtnStyle() {
            return btnStyle;
        }

        public void setBtnStyle(BtnStyleBean btnStyle) {
            this.btnStyle = btnStyle;
        }

        public PostionBean getPostion() {
            return postion;
        }

        public void setPostion(PostionBean postion) {
            this.postion = postion;
        }

        public PagesBean getPages() {
            return pages;
        }

        public void setPages(PagesBean pages) {
            this.pages = pages;
        }

        public static class BtnStyleBean {
            private String background;
            private String fontStyle;
            private String fontSize;
            private String icon;
            private String iconSize;
            private String text;
            private String textColor;

            public String getFontSize() {
                return fontSize;
            }

            public void setFontSize(String fontSize) {
                this.fontSize = fontSize;
            }

            public String getBackground() {
                return background;
            }

            public void setBackground(String background) {
                this.background = background;
            }

            public String getFontStyle() {
                return fontStyle;
            }

            public void setFontStyle(String fontStyle) {
                this.fontStyle = fontStyle;
            }

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }

            public String getIconSize() {
                return iconSize;
            }

            public void setIconSize(String iconSize) {
                this.iconSize = iconSize;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public String getTextColor() {
                return textColor;
            }

            public void setTextColor(String textColor) {
                this.textColor = textColor;
            }
        }

        public static class PostionBean {
            private String h;
            private String w;
            private String x;
            private String y;

            public String getH() {
                return h;
            }

            public void setH(String h) {
                this.h = h;
            }

            public String getW() {
                return w;
            }

            public void setW(String w) {
                this.w = w;
            }

            public String getX() {
                return x;
            }

            public void setX(String x) {
                this.x = x;
            }

            public String getY() {
                return y;
            }

            public void setY(String y) {
                this.y = y;
            }
        }

        public static class PagesBean {
            private String background;
            /**
             * content : 云标物联智慧型多功能查询系统
             * postion : {"h":"5.87%","w":"41.71%","x":"30%","y":"6.30%"}
             * textdetail : {"background":"#000000","fontStyle":"黑体","isscroll":false,"postion":"left","size":"45.2707996368408","textColor":"#996633"}
             * imagedetail : {"isPlay":false,"playTime":5000}
             * type : 2
             */

            private List<ContentBean> content;
            /**
             * fontStyle : 默认
             * icon : http://test.yunbiaowulian.com//imgserver/resource/2016/07/01/d435a372-f848-4558-8658-a58ffe94ee40_s.jpg
             * iconSize : 28
             * text : 目录名称
             * textColor : #ffffff
             * background : #008cba
             * content : {"content":["http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/1c386695-8073-4fed-8a99-86e037fa9cbd.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/d9003318-9910-48cb-aa85-c07cc79c96b1.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/5055f93a-01f4-4fd2-a3ca-1cd176e56f88.jpg"],"imagedetail":{"isPlay":true,"playTime":5000},"textdetail":{"isscroll":"false","background":"#ca8eff","textColor":"#ffffff","postion":"1","size":"70","fontStyle":"默认"},"webdetail":{"isAutoFlus":"true|false","flusTime":"1000"},"type":1}
             */

            private List<BtnBean> btn;

            public String getBackground() {
                return background;
            }

            public void setBackground(String background) {
                this.background = background;
            }

            public List<ContentBean> getContent() {
                return content;
            }

            public void setContent(List<ContentBean> content) {
                this.content = content;
            }

            public List<BtnBean> getBtn() {
                return btn;
            }

            public void setBtn(List<BtnBean> btn) {
                this.btn = btn;
            }

            public static class ContentBean {
                private String content;
                /**
                 * h : 5.87%
                 * w : 41.71%
                 * x : 30%
                 * y : 6.30%
                 */

                private PostionBean postion;
                /**
                 * background : #000000
                 * fontStyle : 黑体
                 * isscroll : false
                 * postion : left
                 * size : 45.2707996368408
                 * textColor : #996633
                 */

                private TextdetailBean textdetail;
                /**
                 * isPlay : false
                 * playTime : 5000
                 */

                private ImagedetailBean imagedetail;
                private int type;

                public String getContent() {
                    return content;
                }

                public void setContent(String content) {
                    this.content = content;
                }

                public PostionBean getPostion() {
                    return postion;
                }

                public void setPostion(PostionBean postion) {
                    this.postion = postion;
                }

                public TextdetailBean getTextdetail() {
                    return textdetail;
                }

                public void setTextdetail(TextdetailBean textdetail) {
                    this.textdetail = textdetail;
                }

                public ImagedetailBean getImagedetail() {
                    return imagedetail;
                }

                public void setImagedetail(ImagedetailBean imagedetail) {
                    this.imagedetail = imagedetail;
                }

                public int getType() {
                    return type;
                }

                public void setType(int type) {
                    this.type = type;
                }

                public static class PostionBean {
                    private String h;
                    private String w;
                    private String x;
                    private String y;

                    public String getH() {
                        return h;
                    }

                    public void setH(String h) {
                        this.h = h;
                    }

                    public String getW() {
                        return w;
                    }

                    public void setW(String w) {
                        this.w = w;
                    }

                    public String getX() {
                        return x;
                    }

                    public void setX(String x) {
                        this.x = x;
                    }

                    public String getY() {
                        return y;
                    }

                    public void setY(String y) {
                        this.y = y;
                    }
                }

                public static class TextdetailBean {
                    private String background;
                    private String fontStyle;
                    private boolean isscroll;
                    private String postion;
                    private String size;
                    private String textColor;

                    public String getBackground() {
                        return background;
                    }

                    public void setBackground(String background) {
                        this.background = background;
                    }

                    public String getFontStyle() {
                        return fontStyle;
                    }

                    public void setFontStyle(String fontStyle) {
                        this.fontStyle = fontStyle;
                    }

                    public boolean isIsscroll() {
                        return isscroll;
                    }

                    public void setIsscroll(boolean isscroll) {
                        this.isscroll = isscroll;
                    }

                    public String getPostion() {
                        return postion;
                    }

                    public void setPostion(String postion) {
                        this.postion = postion;
                    }

                    public String getSize() {
                        return size;
                    }

                    public void setSize(String size) {
                        this.size = size;
                    }

                    public String getTextColor() {
                        return textColor;
                    }

                    public void setTextColor(String textColor) {
                        this.textColor = textColor;
                    }
                }

                public static class ImagedetailBean {
                    private boolean isPlay;
                    private int playTime;

                    public boolean isIsPlay() {
                        return isPlay;
                    }

                    public void setIsPlay(boolean isPlay) {
                        this.isPlay = isPlay;
                    }

                    public int getPlayTime() {
                        return playTime;
                    }

                    public void setPlayTime(int playTime) {
                        this.playTime = playTime;
                    }
                }
            }

            public static class BtnBean {
                private String fontStyle;
                private String icon;
                private String iconSize;
                private String text;
                private String textColor;
                private String background;
                /**
                 * content : ["http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/1c386695-8073-4fed-8a99-86e037fa9cbd.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/d9003318-9910-48cb-aa85-c07cc79c96b1.jpg","http://test.yunbiaowulian.com//imgserver/resource/2016/07/22/5055f93a-01f4-4fd2-a3ca-1cd176e56f88.jpg"]
                 * imagedetail : {"isPlay":true,"playTime":5000}
                 * textdetail : {"isscroll":"false","background":"#ca8eff","textColor":"#ffffff","postion":"1","size":"70","fontStyle":"默认"}
                 * webdetail : {"isAutoFlus":"true|false","flusTime":"1000"}
                 * type : 1
                 */

                private ContentBean content;

                public String getFontStyle() {
                    return fontStyle;
                }

                public void setFontStyle(String fontStyle) {
                    this.fontStyle = fontStyle;
                }

                public String getIcon() {
                    return icon;
                }

                public void setIcon(String icon) {
                    this.icon = icon;
                }

                public String getIconSize() {
                    return iconSize;
                }

                public void setIconSize(String iconSize) {
                    this.iconSize = iconSize;
                }

                public String getText() {
                    return text;
                }

                public void setText(String text) {
                    this.text = text;
                }

                public String getTextColor() {
                    return textColor;
                }

                public void setTextColor(String textColor) {
                    this.textColor = textColor;
                }

                public String getBackground() {
                    return background;
                }

                public void setBackground(String background) {
                    this.background = background;
                }

                public ContentBean getContent() {
                    return content;
                }

                public void setContent(ContentBean content) {
                    this.content = content;
                }

                public static class ContentBean {
                    /**
                     * isPlay : true
                     * playTime : 5000
                     */

                    private ImagedetailBean imagedetail;
                    /**
                     * isscroll : false
                     * background : #ca8eff
                     * textColor : #ffffff
                     * postion : 1
                     * size : 70
                     * fontStyle : 默认
                     */

                    private TextdetailBean textdetail;
                    /**
                     * isAutoFlus : true|false
                     * flusTime : 1000
                     */

                    private WebdetailBean webdetail;
                    private int type;
                    private List<String> content;

                    public ImagedetailBean getImagedetail() {
                        return imagedetail;
                    }

                    public void setImagedetail(ImagedetailBean imagedetail) {
                        this.imagedetail = imagedetail;
                    }

                    public TextdetailBean getTextdetail() {
                        return textdetail;
                    }

                    public void setTextdetail(TextdetailBean textdetail) {
                        this.textdetail = textdetail;
                    }

                    public WebdetailBean getWebdetail() {
                        return webdetail;
                    }

                    public void setWebdetail(WebdetailBean webdetail) {
                        this.webdetail = webdetail;
                    }

                    public int getType() {
                        return type;
                    }

                    public void setType(int type) {
                        this.type = type;
                    }

                    public List<String> getContent() {
                        return content;
                    }

                    public void setContent(List<String> content) {
                        this.content = content;
                    }

                    public static class ImagedetailBean {
                        private boolean isPlay;
                        private int playTime;

                        public boolean isIsPlay() {
                            return isPlay;
                        }

                        public void setIsPlay(boolean isPlay) {
                            this.isPlay = isPlay;
                        }

                        public int getPlayTime() {
                            return playTime;
                        }

                        public void setPlayTime(int playTime) {
                            this.playTime = playTime;
                        }
                    }

                    public static class TextdetailBean {
                        private String isscroll;
                        private String background;
                        private String textColor;
                        private String postion;
                        private String size;
                        private String fontStyle;

                        public String getIsscroll() {
                            return isscroll;
                        }

                        public void setIsscroll(String isscroll) {
                            this.isscroll = isscroll;
                        }

                        public String getBackground() {
                            return background;
                        }

                        public void setBackground(String background) {
                            this.background = background;
                        }

                        public String getTextColor() {
                            return textColor;
                        }

                        public void setTextColor(String textColor) {
                            this.textColor = textColor;
                        }

                        public String getPostion() {
                            return postion;
                        }

                        public void setPostion(String postion) {
                            this.postion = postion;
                        }

                        public String getSize() {
                            return size;
                        }

                        public void setSize(String size) {
                            this.size = size;
                        }

                        public String getFontStyle() {
                            return fontStyle;
                        }

                        public void setFontStyle(String fontStyle) {
                            this.fontStyle = fontStyle;
                        }
                    }

                    public static class WebdetailBean {
                        private String isAutoFlus;
                        private String flusTime;

                        public String getIsAutoFlus() {
                            return isAutoFlus;
                        }

                        public void setIsAutoFlus(String isAutoFlus) {
                            this.isAutoFlus = isAutoFlus;
                        }

                        public String getFlusTime() {
                            return flusTime;
                        }

                        public void setFlusTime(String flusTime) {
                            this.flusTime = flusTime;
                        }
                    }
                }
            }
        }
    }
}
