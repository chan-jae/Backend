package com.team.student_calendar.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicConfig {

    // 내부에 커넥션 풀을 가지므로 요청마다 만들지 않고 빈 하나를 재사용
    @Bean
    public AnthropicClient anthropicClient(@Value("${claude-token}") String claudeToken) {
        return AnthropicOkHttpClient.builder().apiKey(claudeToken).build();
    }
}
