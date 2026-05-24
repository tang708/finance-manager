package com.finance.manager.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {

    private final String apiKey = System.getenv("DEEPSEEK_API_KEY");
    private final String baseUrl = System.getenv().getOrDefault("DEEPSEEK_BASE_URL", "https://api.deepseek.com/v1");
    private final RestTemplate restTemplate = new RestTemplate();

    public String askQuestion(String question) {
        try {
            // 构建请求URL
            String url = baseUrl + "/chat/completions";

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            // 构建消息列表
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个智能财务助手，专门回答用户的财务问题，提供财务知识和建议，提醒用户重要的财务事项。请保持专业、友好的语气，提供准确的财务信息。");

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", question);

            requestBody.put("messages", new Object[] { systemMessage, userMessage });
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);

            // 发送请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            // 解析响应
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                Object choices = responseBody.get("choices");
                if (choices instanceof java.util.List) {
                    java.util.List<?> choicesList = (java.util.List<?>) choices;
                    if (!choicesList.isEmpty()) {
                        Object firstChoice = choicesList.get(0);
                        if (firstChoice instanceof Map) {
                            Map<?, ?> choiceMap = (Map<?, ?>) firstChoice;
                            if (choiceMap.containsKey("message")) {
                                Object message = choiceMap.get("message");
                                if (message instanceof Map) {
                                    Map<?, ?> messageMap = (Map<?, ?>) message;
                                    if (messageMap.containsKey("content")) {
                                        String content = messageMap.get("content").toString();
                                        // 处理输出，去除特殊字符，确保分段合理
                                        content = content.replaceAll("[\\r\\n]+", "\n\n"); // 确保段落间有两个换行
                                        content = content.replaceAll("[\\s]{2,}", " "); // 去除多余空格
                                        content = content.replaceAll("[\\p{Cntrl}&&[^\n]]", ""); // 去除控制字符
                                        return content;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return "抱歉，我暂时无法回答您的问题，请稍后再试。";
        } catch (Exception e) {
            // 记录异常信息
            System.err.println("AI服务调用失败: " + e.getMessage());
            e.printStackTrace();
            // 返回友好的错误信息
            return "抱歉，我暂时无法连接到AI服务，请检查网络连接或稍后再试。";
        }
    }
}
