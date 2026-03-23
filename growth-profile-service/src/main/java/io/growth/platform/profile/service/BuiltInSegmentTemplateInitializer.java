package io.growth.platform.profile.service;

import io.growth.platform.profile.domain.model.ConditionSlot;
import io.growth.platform.profile.domain.model.SegmentTemplate;
import io.growth.platform.profile.domain.repository.SegmentTemplateRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BuiltInSegmentTemplateInitializer {

    private static final Logger log = LoggerFactory.getLogger(BuiltInSegmentTemplateInitializer.class);

    private final SegmentTemplateRepository segmentTemplateRepository;

    public BuiltInSegmentTemplateInitializer(SegmentTemplateRepository segmentTemplateRepository) {
        this.segmentTemplateRepository = segmentTemplateRepository;
    }

    @PostConstruct
    public void init() {
        insertIfAbsent(buildOrderRepeat());
        insertIfAbsent(buildRecentActive());
        insertIfAbsent(buildSearchIntent());
        insertIfAbsent(buildActiveBuyer());
        log.info("Built-in segment templates initialized");
    }

    private void insertIfAbsent(SegmentTemplate template) {
        segmentTemplateRepository.findByTemplateKey(template.getTemplateKey()).ifPresentOrElse(
                existing -> log.debug("Built-in template already exists: {}", template.getTemplateKey()),
                () -> {
                    segmentTemplateRepository.insert(template);
                    log.info("Inserted built-in template: {}", template.getTemplateKey());
                }
        );
    }

    private SegmentTemplate buildOrderRepeat() {
        SegmentTemplate t = new SegmentTemplate();
        t.setTemplateKey("order_repeat");
        t.setTitle("30天复购用户");
        t.setDescription("自动使用下单事件，预填最近30天下单至少2次");
        t.setSortOrder(1);
        t.setBuiltIn(true);
        t.setSlots(List.of(
                new ConditionSlot("复购条件", List.of("ORDER"), 30, 2)
        ));
        return t;
    }

    private SegmentTemplate buildRecentActive() {
        SegmentTemplate t = new SegmentTemplate();
        t.setTemplateKey("recent_active");
        t.setTitle("7天活跃用户");
        t.setDescription("自动使用登录或浏览事件，预填最近7天活跃至少3次");
        t.setSortOrder(2);
        t.setBuiltIn(true);
        t.setSlots(List.of(
                new ConditionSlot("活跃条件", List.of("LOGIN", "PAGE_VIEW"), 7, 3)
        ));
        return t;
    }

    private SegmentTemplate buildSearchIntent() {
        SegmentTemplate t = new SegmentTemplate();
        t.setTemplateKey("search_intent");
        t.setTitle("7天高意向搜索");
        t.setDescription("自动使用搜索事件，预填最近7天搜索至少3次");
        t.setSortOrder(3);
        t.setBuiltIn(true);
        t.setSlots(List.of(
                new ConditionSlot("搜索条件", List.of("SEARCH"), 7, 3)
        ));
        return t;
    }

    private SegmentTemplate buildActiveBuyer() {
        SegmentTemplate t = new SegmentTemplate();
        t.setTemplateKey("active_buyer");
        t.setTitle("活跃且已下单");
        t.setDescription("自动组合活跃事件和下单事件，适合筛选近期高潜或高价值人群");
        t.setSortOrder(4);
        t.setBuiltIn(true);
        t.setSlots(List.of(
                new ConditionSlot("活跃条件", List.of("LOGIN", "PAGE_VIEW"), 7, 3),
                new ConditionSlot("下单条件", List.of("ORDER"), 30, 1)
        ));
        return t;
    }
}
