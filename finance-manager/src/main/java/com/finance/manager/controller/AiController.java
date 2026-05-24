package com.finance.manager.controller;

import com.finance.manager.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/ask")
    public Map<String, Object> askQuestion(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String question = request.get("question");
            if (question == null || question.isEmpty()) {
                result.put("code", 400);
                result.put("message", "问题不能为空");
                return result;
            }

            String answer = aiService.askQuestion(question);
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", answer);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
}
