package team.wego.wegobackend.user.presentation;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.wego.wegobackend.common.response.ApiResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {

        log.info(LocalDateTime.now() + "test endpoint call");
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(
                200,
                true,
                "Test Success"
            ));
    }
}
