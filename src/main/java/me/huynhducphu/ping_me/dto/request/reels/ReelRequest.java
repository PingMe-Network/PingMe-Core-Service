package me.huynhducphu.ping_me.dto.request.reels;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReelRequest {

    @Size(max = 200, message = "Caption không quá 200 ký tự")
    private String caption;

    private List<String> hashtags;
}
