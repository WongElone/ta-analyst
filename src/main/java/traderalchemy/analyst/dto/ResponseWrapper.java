package traderalchemy.analyst.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseWrapper<T>(
    boolean success,
    int code,
    String msg,
    T data
) {
    // Standard error codes
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_DATA_NOT_AVAILABLE = 10001;
    public static final String MSG_DATA_NOT_AVAILABLE = "Data not available";
    
    public static final int CODE_DB_ERROR = 10002;
    public static final String MSG_DB_ERROR = "Database error";

    public static final int CODE_INVALID_ARGUMENT = 10003;
    public static final String MSG_INVALID_ARGUMENT = "Invalid argument";

    public static final int CODE_OPERATION_NOT_ALLOWED = 10004;
    public static final String MSG_OPERATION_NOT_ALLOWED = "Operation not allowed";

    /**
     * Create a success response with data
     */
    public static <T> ResponseWrapper<T> success(T data) {
        return new ResponseWrapper<>(true, CODE_SUCCESS, null, data);
    }

    /**
     * Create a failure response with custom error code and message
     */
    public static <T> ResponseWrapper<T> failure(int code, String msg) {
        return new ResponseWrapper<>(false, code, msg, null);
    }

    /**
     * Create a failure response for data not available
     */
    public static <T> ResponseWrapper<T> dataNotAvailable() {
        return new ResponseWrapper<>(false, CODE_DATA_NOT_AVAILABLE, MSG_DATA_NOT_AVAILABLE, null);
    }
}
