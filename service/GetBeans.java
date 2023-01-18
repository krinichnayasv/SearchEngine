package service;

public class GetBeans {

    private DBMethods dbMethods = SpringContext.getBean(DBMethods.class);

    public GetBeans(){

    }

    public DBMethods getDbMethods() {
        return dbMethods;
    }

    public void setDbMethods(DBMethods dbMethods) {
        this.dbMethods = dbMethods;
    }

}
