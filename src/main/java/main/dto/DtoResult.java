package main.dto;

public class DtoResult {

    private boolean result;
    private String error;
    private String resultMessage;

    public DtoResult() {
        this.result = true;
    }

    public DtoResult(boolean result) {
        this.result = result;
    }

    public DtoResult(boolean result, String resultMessage) {
        this.result = result;
        this.resultMessage = resultMessage;

    }

    public DtoResult(String errorMessage) {
        this.result = false;
        this.error = errorMessage;
    }


    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
