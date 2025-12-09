package team.wego.wegobackend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        boolean success,
        T data
) {

    public static <T> ApiResponse<T> success(int status, T data) {
        return new ApiResponse<>(status, true, data);
    }

    public static <T> ApiResponse<T> success(int status, boolean isSuccess, T data) {
        return new ApiResponse<>(status, isSuccess, data);
    }

    /**
     * No Content (ex : 204)
     * */
    public static <T> ApiResponse<T> success(int status, boolean isSuccess) {
        return new ApiResponse<>(status, true, null);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, false, null);
    }

    public static <T> ApiResponse<T> error(int status, boolean isSuccess, T data) {
        return new ApiResponse<>(status, isSuccess, data);
    }
}