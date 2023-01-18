package service;


public class ExceptionByIndexing {

    private int code;
    private String message;

    public ExceptionByIndexing() {
    }


    public String getErrorMessageForReindexingPage(int code) {
        switch (code) {
            case(400) -> message = "Некорректный запрос к серверу. Сайт не доступен";
            case(401) -> message = "Не хватает данных для авторизации на запрос данных с сайта";
            case(403) -> message = "Доступ к главной странице сайта запрещен";
            case(404) -> message = "Главная страница сайта не доступна/не найдена";
            case(405) -> message = "Метод запроса известен серверу, но был отключён и не может быть использован";
            case(500) -> message = "Внутренняя ошибка сервера. Сайт не доступен";
            default -> message = "Главная страница сайта не доступна";
        }
           return message;
    }

    public String getErrorMessageForSite(int code) {
        switch (code) {
            case(400) -> message = "Некорректный запрос к серверу. Сайт не доступен";
            case(401) -> message = "Не хватает данных для авторизации на запрос данных с сайта";
            case(403) -> message = "Доступ к странице сайта запрещен";
            case(404) -> message = "Искомая страница сайта не доступна/не найдена";
            case(405) -> message = "Метод запроса известен серверу, но был отключён и не может быть использован";
            case(500) -> message = "Внутренняя ошибка сервера. Сайт не доступен";
            default -> message = "Искомая страница сайта не доступна";
        }
        return message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }




}
