package http;

public enum Status {
    OK(200,"OK"),
    Bad_Request(400,"Bad_Request"),
    Not_Found(404,"Not_Found"),
    Method_Not_Allowed(405,"Method_Not_Allowed"),
    Internal_Server_Error(500,"Internal_Server_Error");

    private final int code;
    private final String reason;


    Status(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }
}
