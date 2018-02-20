package angelo.itl.arduinoairqualitymonitor.adapter.main;

    public class DrawerItem {
        private int imgResID;
        private String ItemName;
        private String title;

        public DrawerItem(String itemName, int imgResID) {
            super();
            this.imgResID = imgResID;
            this.ItemName = itemName;
        }

        public int getImgResID()                {return imgResID;}

        public void setImgResID(int imgResID)   {this.imgResID = imgResID;}

        public String getItemName()             {return ItemName;}

        public void setItemName(String itemName){ItemName = itemName;}

        public DrawerItem(String title)         {this.title = title;}

        public String getTitle()                {return title;}

        public void setTitle(String title)      {this.title = title;}
    }