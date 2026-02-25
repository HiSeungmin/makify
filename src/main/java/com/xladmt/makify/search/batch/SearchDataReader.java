package com.xladmt.makify.search.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Component
public class SearchDataReader {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\[SEARCH\\] keyword=(.+)$");

    @Value("${autocomplete.log-file:logs/data.log}")
    private String logFilePath;

    @Value("${autocomplete.since-date:}")
    private String sinceDate;

    /**
     * 지난 7일간의 검색어 빈도수를 계산 (비속어 필터링 포함)
     *
     * @return 검색어-빈도수 맵 (비속어 제외)
     */
    public Map<String, Integer> readSearchDataForLastWeek() {
        Map<String, Integer> searchFrequency = new HashMap<>();

        // since-date 설정값이 있으면 해당 날짜, 없으면 7일 전부터
        LocalDate fromDate;
        if (sinceDate != null && !sinceDate.isBlank()) {
            fromDate = LocalDate.parse(sinceDate);
            log.info("[SEARCH_DATA_READER] 날짜 필터: {} 이후 데이터 사용", fromDate);
        } else {
            fromDate = LocalDate.now().minusDays(7);
            log.info("[SEARCH_DATA_READER] 날짜 필터: 최근 7일 ({} 이후) 데이터 사용", fromDate);
        }
        
        // 통계용 변수
        int totalLines = 0;
        int parsedLines = 0;
        int profanityLines = 0;  // 비속어 감지 줄 수

        try {
            log.info("[SEARCH_DATA_READER] 로그 파일 읽기 시작: {}", logFilePath);

            // 파일에서 모든 라인을 읽음
            try (Stream<String> lines = Files.lines(Paths.get(logFilePath), StandardCharsets.UTF_8)) {
                lines.forEach(line -> {
                    try {
                        // 날짜 파싱 (밀리초 포함: 23자)
                        // 예: "2025-11-20 15:05:04.631"
                        if (line.length() < 23) {
                            log.debug("[SEARCH_DATA_READER] 라인이 너무 짧음: {}", line);
                            return;
                        }

                        String dateString = line.substring(0, 23); // "2025-11-20 15:05:04.631"
                        LocalDateTime logDateTime = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
                        LocalDate logDate = logDateTime.toLocalDate();

                        // 날짜 필터 적용
//                        if (logDate.isBefore(fromDate)) {
//                            return; // 7일 이전 데이터는 무시
//                        }

                        // 검색어 추출
                        String keyword = extractKeyword(line);
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            
                            // 클린 데이터만 추가
                            searchFrequency.put(keyword, searchFrequency.getOrDefault(keyword, 0) + 1);
                            // parsedLines++;
                        }

                    } catch (Exception e) {
                        log.debug("[SEARCH_DATA_READER] 라인 파싱 실패: {}", line, e);
                    }
                });
            }

            // 상세 통계 로깅
            int totalSearchCount = searchFrequency.values().stream().mapToInt(Integer::intValue).sum();
            log.info("[SEARCH_DATA_READER] ========== 로그 읽기 완료 ==========");
            log.info("[SEARCH_DATA_READER] 전체 라인: {}줄", totalLines);
            log.info("[SEARCH_DATA_READER] 파싱된 라인: {}줄", parsedLines);
            log.info("[SEARCH_DATA_READER] 비속어로 제외된 라인: {}줄", profanityLines);
            log.info("[SEARCH_DATA_READER] 최종 검색어 개수: {}개", searchFrequency.size());
            log.info("[SEARCH_DATA_READER] 총 검색 횟수: {}회", totalSearchCount);
            log.info("[SEARCH_DATA_READER] ============================");

            return searchFrequency;

        } catch (IOException e) {
            log.error("[SEARCH_DATA_READER] 로그 파일 읽기 실패: {}", logFilePath, e);
            return new HashMap<>(); // 오류 시 빈 Map 반환
        }
    }

    /**
     * 로그 라인에서 검색어 추출
     *
     * @param line 로그 라인
     *            예: "2025-11-20 15:05:04.631 [SEARCH] keyword=챌린지"
     * @return 검색어 (예: "챌린지")
     */
    private String extractKeyword(String line) {
        try {
            // "[SEARCH] keyword=..." 부분 추출
            int searchIndex = line.indexOf("[SEARCH]");
            if (searchIndex == -1) {
                return null;
            }

            String searchPart = line.substring(searchIndex);
            Matcher matcher = KEYWORD_PATTERN.matcher(searchPart);

            if (matcher.find()) {
                String keyword = matcher.group(1).trim();
                return keyword.isEmpty() ? null : keyword;
            }

            return null;

        } catch (Exception e) {
            log.debug("[SEARCH_DATA_READER] 키워드 추출 실패: {}", line, e);
            return null;
        }
    }
}
