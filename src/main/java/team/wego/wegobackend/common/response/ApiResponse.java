package team.wego.wegobackend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> success(boolean isSuccess, T data) {
        return new ApiResponse<>(isSuccess, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null);
    }

    public static <T> ApiResponse<T> error(boolean isSuccess, T data) {
        return new ApiResponse<>(isSuccess, data);
    }
}