package com.zhongshu.zswy.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 为了让你可以用 http://localhost:8080/zswy.html 直接访问当前工程根目录的页面文件。
 * 注意：该方式是开发期“读本地文件返回”的实现，方便你快速联调 fetch 接口。
 */
@RestController
public class ZswyPageController {

    @GetMapping(value = "/zswy.html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> zswyHtml() throws IOException {
        // 后端以 backend-zswy 目录作为工作目录启动时，页面在上一层目录：../zswy.html
        Path htmlPath = Path.of("..", "zswy.html").normalize().toAbsolutePath();
        if (!Files.exists(htmlPath)) {
            return ResponseEntity.notFound().build();
        }
        String html = Files.readString(htmlPath, StandardCharsets.UTF_8);
        return ResponseEntity.ok(html);
    }
}

