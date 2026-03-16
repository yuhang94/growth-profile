package io.growth.platform.profile.infrastructure.persistence.repository;

import io.growth.platform.profile.BaseMyBatisTest;
import io.growth.platform.profile.api.enums.CompareOperator;
import io.growth.platform.profile.api.enums.ConditionOperator;
import io.growth.platform.profile.domain.model.Segment;
import io.growth.platform.profile.domain.model.SegmentCondition;
import io.growth.platform.profile.domain.repository.SegmentRepository;
import io.growth.platform.profile.infrastructure.persistence.mapper.SegmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SegmentRepositoryImplIT extends BaseMyBatisTest {

    @Autowired
    private SegmentRepository repository;

    @Autowired
    private SegmentMapper mapper;

    @BeforeEach
    void cleanUp() {
        mapper.delete(null);
    }

    @Test
    void insertAndFindById() {
        Segment segment = newSegment("高价值用户", "年龄>25的男性用户");

        repository.insert(segment);

        assertNotNull(segment.getId());
        Optional<Segment> found = repository.findById(segment.getId());
        assertTrue(found.isPresent());
        assertEquals("高价值用户", found.get().getSegmentName());
        assertNotNull(found.get().getRootCondition());
        assertEquals(ConditionOperator.AND, found.get().getRootCondition().getOperator());
        assertEquals(2, found.get().getRootCondition().getChildren().size());
    }

    @Test
    void update() {
        Segment segment = newSegment("测试分群", "描述");
        repository.insert(segment);

        segment.setSegmentName("更新后分群");
        segment.setLastUserCount(1000L);
        repository.update(segment);

        Segment updated = repository.findById(segment.getId()).orElseThrow();
        assertEquals("更新后分群", updated.getSegmentName());
        assertEquals(1000L, updated.getLastUserCount());
    }

    @Test
    void deleteById() {
        Segment segment = newSegment("待删除分群", "测试删除");
        repository.insert(segment);
        Long id = segment.getId();

        repository.deleteById(id);

        assertFalse(repository.findById(id).isPresent());
    }

    @Test
    void findAllAndCount() {
        repository.insert(newSegment("分群A", "描述A"));
        repository.insert(newSegment("分群B", "描述B"));
        repository.insert(newSegment("分群C", "描述C"));

        assertEquals(3, repository.count());

        List<Segment> page1 = repository.findAll(1, 2);
        assertEquals(2, page1.size());

        List<Segment> page2 = repository.findAll(2, 2);
        assertEquals(1, page2.size());
    }

    @Test
    void conditionJsonSerializationRoundTrip() {
        Segment segment = newSegment("JSON测试", "验证JSON序列化");
        repository.insert(segment);

        Segment loaded = repository.findById(segment.getId()).orElseThrow();
        SegmentCondition root = loaded.getRootCondition();

        assertEquals(ConditionOperator.AND, root.getOperator());
        assertFalse(root.isLeaf());

        SegmentCondition child1 = root.getChildren().get(0);
        assertTrue(child1.isLeaf());
        assertEquals("age", child1.getTagKey());
        assertEquals(CompareOperator.GT, child1.getCompareOp());
        assertEquals(List.of("25"), child1.getValues());

        SegmentCondition child2 = root.getChildren().get(1);
        assertTrue(child2.isLeaf());
        assertEquals("gender", child2.getTagKey());
        assertEquals(CompareOperator.EQ, child2.getCompareOp());
        assertEquals(List.of("male"), child2.getValues());
    }

    private Segment newSegment(String name, String description) {
        Segment segment = new Segment();
        segment.setSegmentName(name);
        segment.setDescription(description);
        segment.setStatus(1);

        SegmentCondition leaf1 = new SegmentCondition();
        leaf1.setTagKey("age");
        leaf1.setCompareOp(CompareOperator.GT);
        leaf1.setValues(List.of("25"));

        SegmentCondition leaf2 = new SegmentCondition();
        leaf2.setTagKey("gender");
        leaf2.setCompareOp(CompareOperator.EQ);
        leaf2.setValues(List.of("male"));

        SegmentCondition root = new SegmentCondition();
        root.setOperator(ConditionOperator.AND);
        root.setChildren(List.of(leaf1, leaf2));

        segment.setRootCondition(root);
        return segment;
    }
}
